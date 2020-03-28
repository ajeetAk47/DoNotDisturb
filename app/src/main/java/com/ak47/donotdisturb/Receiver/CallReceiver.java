package com.ak47.donotdisturb.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.ak47.donotdisturb.Database.CallDatabaseHandler;
import com.ak47.donotdisturb.Model.Contact;
import com.ak47.donotdisturb.Service.RingtonePlayingService;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class CallReceiver extends BroadcastReceiver {
    String TAG = "Logging - CallReceiver ";
    private static final String TABLE_CONTACTS_CALL = "contacts";
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
        SharedPreferences sharedPreferences = context.getSharedPreferences("initial_setup", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (state == TelephonyManager.CALL_STATE_RINGING && checkExistenceInDataBase(number,context))
        {

            Log.e(TAG,"Playing");
            if(mode.equals("Do Not Disturb"))
            {
                Log.e(TAG,"Test");
                try
                {
                    editor.putBoolean("Ringing_mode",false).apply();
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
                editor.putBoolean("Ringing_mode",false).apply();
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
                        editor.putBoolean("Ringing_mode",true).apply();


                    }
                    else if(mode.equals("Silent"))
                    {
                        //Silent  Mode

                        myAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                        editor.putBoolean("Ringing_mode",true).apply();
                    }
        }

    }

    private boolean checkExistenceInDataBase(String number, Context context)
    {
   //     Log.e(TAG,"checkExistence");
        CallDatabaseHandler db = new CallDatabaseHandler(context);
        if(number.contains(" ")){
            number=number.replaceAll(" ","");
        }
        List<Contact> contacts = db.getAllContacts(TABLE_CONTACTS_CALL);  // TABLE_CONTACTS_CALL is table name
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
