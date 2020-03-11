package com.ak47.donotdisturb.Service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.ak47.donotdisturb.Activities.MainActivity;
import com.ak47.donotdisturb.R;
import com.ak47.donotdisturb.Receiver.CallReceiver;
import com.ak47.donotdisturb.Receiver.RingerModeStateChangeReceiver;

public class HelperForegroundService extends Service {
    String TAG = "Logging - HelperForegroundService ";
    String notificationChannelIdForHelperService = "1000";
//    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    int foregroundServiceID = 100;
//    private NotificationManager mNotificationManager;
    private  AudioManager mAudioManager;

    CallReceiver callReceiver = new CallReceiver();
    RingerModeStateChangeReceiver ringerModeStateChangeReceiver=new RingerModeStateChangeReceiver();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        Log.e(TAG, "onCreate: " + "called");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand: " + "service started " + startId);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        String status=PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("mode_preference","Silent");
        String visibility = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("notification_visibility", "0");
        Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                300,
                mainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notification = new NotificationCompat.Builder(this,notificationChannelIdForHelperService )
                .setSmallIcon(R.drawable.ic_check)
                .setContentIntent(mainActivityPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(status+" Mode")
                        .setSummaryText("Running")
                ).setColor(ContextCompat.getColor(this,R.color.colorAccent))
                .setVisibility(Integer.parseInt(visibility))
                .build();
        IntentFilter intentFilterCallReceiver = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(callReceiver, intentFilterCallReceiver);
        IntentFilter intentFilterRingerModeStateChangeReceiver =new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
        registerReceiver(ringerModeStateChangeReceiver,intentFilterRingerModeStateChangeReceiver);
        changeMode();
        startForeground(foregroundServiceID, notification);
        return START_STICKY;
    }

    @SuppressLint("WrongConstant")
    private void changeMode()
    {
       String mode=PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("mode_preference","Silent");
        if(mode.equals("Do Not Disturb"))
        {
            //Do Not  Disturb Mode

            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

        }

        else if(mode.equals("Silent"))
        {
            //Silent  Mode
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }
        Log.e(TAG, "Change Mode - " + mode);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: " + "called");
        try {
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
           // mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            unregisterReceiver(ringerModeStateChangeReceiver);
            unregisterReceiver(callReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
