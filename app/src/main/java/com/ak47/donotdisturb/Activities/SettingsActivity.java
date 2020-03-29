package com.ak47.donotdisturb.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.ak47.donotdisturb.Fragment.CallDialogFragment;
import com.ak47.donotdisturb.Fragment.WhatsAppDialogFragment;
import com.ak47.donotdisturb.Fragment.WhatsAppWordDialogFragment;
import com.ak47.donotdisturb.R;
import com.ak47.donotdisturb.Service.HelperForegroundService;

public class SettingsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private Intent helperForegroundServiceIntent;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            helperForegroundServiceIntent = new Intent(getContext(), HelperForegroundService.class);

            Preference contact = findPreference("manage_contacts");
            Preference whatsAppContact = findPreference("WhatsAppContact_key");
            Preference whatsAppWord = findPreference("WhatsAppWord_key");
            Preference about = findPreference("appAbout");
            ListPreference modePreference = findPreference("mode_preference");


            modePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    getContext().stopService(helperForegroundServiceIntent);

                    return true;
                }
            });

            contact.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    CallDialogFragment.display(getActivity().getSupportFragmentManager());
                    return true;
                }
            });

            whatsAppContact.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    WhatsAppDialogFragment.display(getActivity().getSupportFragmentManager());
                    return true;
                }
            });

            whatsAppWord.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    WhatsAppWordDialogFragment.display(getActivity().getSupportFragmentManager());
                    return true;
                }
            });


            about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent aboutIntent = new Intent(getActivity(), AboutActivity.class);
                    startActivity(aboutIntent);
                    return true;
                }
            });


        }
    }
}