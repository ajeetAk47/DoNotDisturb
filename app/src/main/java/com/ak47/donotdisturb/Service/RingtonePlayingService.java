package com.ak47.donotdisturb.Service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class RingtonePlayingService extends Service {
    private Ringtone ringtone;
    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    String TAG = "Logging - RingtonePlayingService ";


    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Uri ringtoneUri = Uri.parse(intent.getExtras().getString("ringtone-uri"));
        Log.e(TAG,"Ringtone playing");
        try{
            NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            ringtone = RingtoneManager.getRingtone(getBaseContext(), ringtoneUri);
//            ringtone.setVolume(audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
            ringtone.play();
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        ringtone.stop();
    }

}
