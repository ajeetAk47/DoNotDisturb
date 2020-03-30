package com.ak47.doNotDisturb.Service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.ak47.doNotDisturb.Database.DatabaseHandler;
import com.ak47.doNotDisturb.Model.Contact;
import com.ak47.doNotDisturb.Model.Word;

import java.io.IOException;
import java.util.List;

public class CustomNotificationListenerService extends NotificationListenerService {

    private final String TAG = "CustomNotificationListenerService";
    Context context;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        sharedPreferences = getSharedPreferences("initial_setup", Context.MODE_PRIVATE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sharedPreferences.getBoolean("foregroundServiceStateUserPreference", false)
                && PreferenceManager.getDefaultSharedPreferences(context).getBoolean("whatsAppNotification", false)) {
            int notificationCode = matchNotificationCode(sbn);
            if (notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {
                Bundle extras = sbn.getNotification().extras;
                String contactName = extras.getString("android.title");
                String msg = extras.getCharSequence("android.text").toString();

//            Log.e(TAG,"Package "+sbn.getPackageName());


                if (checkNumber(contactName) || checkWord(msg)) {
                    Log.e(TAG, "Contact Name - " + contactName);
                    Log.e(TAG, "Msg - " + msg);
                    playNotificationSound();
                }
            }
        }
    }

    private void playNotificationSound() {
        MediaPlayer mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(context, RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            final AudioManager audioManager = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private boolean checkWord(String msg) {
        Log.e(TAG, "checkExistence Of Word");
        DatabaseHandler db = new DatabaseHandler(context);

        List<Word> wordList = db.getAllWords();  // TABLE_CONTACTS_CALL is table name
        for (Word word : wordList) {
            //   Log.e(TAG,contactList.getPhoneNumber()+ " "+ number);
            if (msg.contains(word.getWord())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkNumber(String contactName) {
        Log.e(TAG, "checkExistence Of contact Name");
        DatabaseHandler db = new DatabaseHandler(context);
        List<Contact> contactList = db.getAllContacts("whatsappContacts");  // TABLE_CONTACTS_CALL is table name
        for (Contact contact : contactList) {
            //   Log.e(TAG,contactList.getPhoneNumber()+ " "+ number);
            if (contactName.contains(contact.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "Notification Removed");
    }

    private int matchNotificationCode(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        if (packageName.equals(ApplicationPackageNames.WHATSAPP_PACK_NAME)) {
            return (InterceptedNotificationCode.WHATSAPP_CODE);
        }
//        else if(packageName.equals(ApplicationPackageNames.INSTAGRAM_PACK_NAME)){
//            return(InterceptedNotificationCode.INSTAGRAM_CODE);
//        }
//        else  if(packageName.equals(ApplicationPackageNames.FACEBOOK_PACK_NAME)
//                    || packageName.equals(ApplicationPackageNames.FACEBOOK_MESSENGER_PACK_NAME)){
//                return(InterceptedNotificationCode.FACEBOOK_CODE);
//        }
        else {
            return (InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
        }
    }

    /*
        These are the package names of the apps. for which we want to
        listen the notifications
     */
    private static final class ApplicationPackageNames {
        public static final String WHATSAPP_PACK_NAME = "com.whatsapp";
//        public static final String FACEBOOK_PACK_NAME = "com.facebook.katana";
//        public static final String FACEBOOK_MESSENGER_PACK_NAME = "com.facebook.orca";
//        public static final String INSTAGRAM_PACK_NAME = "com.instagram.android";
    }

    /*
        These are the return codes we use in the method which intercepts
        the notifications, to decide whether we should do something or not
     */
    public static final class InterceptedNotificationCode {
        public static final int WHATSAPP_CODE = 1;
        //        public static final int FACEBOOK_CODE = 2;
//        public static final int INSTAGRAM_CODE = 3;
        public static final int OTHER_NOTIFICATIONS_CODE = 4; // We ignore all notification with code == 4
    }
}
