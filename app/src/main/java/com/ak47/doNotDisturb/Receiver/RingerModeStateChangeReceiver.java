package com.ak47.doNotDisturb.Receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.ak47.doNotDisturb.Activities.MainActivity;
import com.ak47.doNotDisturb.R;
import com.ak47.doNotDisturb.Service.HelperForegroundService;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class RingerModeStateChangeReceiver extends BroadcastReceiver {
    String TAG = " Logging - RingerModeStateChangeReceiver";
    String channel_id = "RingingId", channel_name = "RingingName";
    String notificationChannelIdForRingerModeReceiver = "2000";
    private Intent helperForegroundServiceIntent;


    @Override
    public void onReceive(Context context, Intent intent) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        helperForegroundServiceIntent = new Intent(context, HelperForegroundService.class);
        String mode = PreferenceManager.getDefaultSharedPreferences(context).getString("mode_preference", "Silent");
        SharedPreferences sharedPreferences = context.getSharedPreferences("initial_setup", MODE_PRIVATE);
        boolean ringMode=sharedPreferences.getBoolean("Ringing_mode",true);
        String currentMode;
        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                Log.i("Do Not Disturb", "Silent mode");
                currentMode = "Do Not Disturb";
                break;

            case AudioManager.RINGER_MODE_VIBRATE:
                Log.i("Do Not Disturb", "Vibrate mode");
                currentMode = "Silent";
                break;

            case AudioManager.RINGER_MODE_NORMAL:
                Log.i("Do Not Disturb", "Normal mode");
                currentMode = "Normal";
                break;
            default:
//                currentMode = mode;
                throw new IllegalStateException("Unexpected value: " + audioManager.getRingerMode());

        }

        if (!currentMode.equals(mode) && ringMode) {
            Log.e(TAG, "Mode Not Equal " +ringMode);

            Intent mainActivityIntent = new Intent(context, MainActivity.class);
            PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(
                    context,
                    300,
                    mainActivityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            Notification notification = new NotificationCompat.Builder(context, notificationChannelIdForRingerModeReceiver)
                    .setSmallIcon(R.drawable.ic_cancel)
                    .setContentIntent(mainActivityPendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .setBigContentTitle("Auto Turned Off")
                            .bigText("Mismatch Mode of Audio Setting")
                            .setSummaryText(" Please Again visit App"))
                    .setColor(ContextCompat.getColor(context, R.color.bg_screen1))
                    .build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            Objects.requireNonNull(manager).notify(1, notification); // Objects.requireNonNull(manager) for Null value Exception
            context.stopService(helperForegroundServiceIntent);
        }
    }
}
