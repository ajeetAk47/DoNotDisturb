package com.ak47.donotdisturb.Receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import androidx.preference.PreferenceManager;

import com.ak47.donotdisturb.Database.DatabaseHandler;
import com.ak47.donotdisturb.Model.Contact;
import com.ak47.donotdisturb.Service.RingtonePlayingService;

import java.util.List;

public class CallReceiver extends BroadcastReceiver {
    String TAG = "Logging - CallReceiver ";

    @Override
    public void onReceive(Context context, Intent intent) {
        String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
        String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
        int state = 0;
        if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            state = TelephonyManager.CALL_STATE_IDLE;
        } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            state = TelephonyManager.CALL_STATE_OFFHOOK;
        } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            state = TelephonyManager.CALL_STATE_RINGING;
        }
        onCallStateChanged(context, state, number);
    }

    private void onCallStateChanged(Context context, int state, String number) {
        AudioManager myAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        String mode= PreferenceManager.getDefaultSharedPreferences(context).getString("mode_preference","Silent");
        if (state == TelephonyManager.CALL_STATE_RINGING && checkExistenceInDataBase(number,context))
        {

            Log.e(TAG,"Playing");

            if(mode.equals("Do Not Disturb"))
            {
                Log.e(TAG,"Test");
                try
                {
                    Intent startIntent = new Intent(context, RingtonePlayingService.class);
                    Uri ringtoneUri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    startIntent.putExtra("ringtone-uri", ringtoneUri.toString());
                    context.startService(startIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                    }
            }
            else if(mode.equals("Silent"))
            {
                //Silent  Mode to Normal During Calls
                myAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }

        }
        else {
                    if(mode.equals("Do Not Disturb"))
                    {
                        //Do Not  Disturb Mode
                        Intent stopIntent = new Intent(context, RingtonePlayingService.class);
                        context.stopService(stopIntent);
                       myAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);


                    }
                    else if(mode.equals("Silent"))
                    {
                        //Silent  Mode
                        myAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    }
        }
    }

    private boolean checkExistenceInDataBase(String number, Context context)
    {
   //     Log.e(TAG,"checkExistence");
        DatabaseHandler db=new DatabaseHandler(context);
        if(number.contains(" ")){
            number=number.replaceAll(" ","");
        }
        List<Contact> contacts = db.getAllContacts();
        for (Contact contactList : contacts)
        {
         //   Log.e(TAG,contactList.getPhoneNumber()+ " "+ number);
            if(contactList.getPhoneNumber().equals(number)){
                return true;
            }
        }
        return false;
    }

}
