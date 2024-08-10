package it.deviato.droidpppwn;

import static it.deviato.droidpppwn.App.*;

import android.graphics.Color;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected static SharedPreferences sharedPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d("Droid","-Settings-OnCreate-");
        sharedPrefs=PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if(savedInstanceState==null)
            getSupportFragmentManager().beginTransaction().replace(R.id.settings,new SettingsFragment()).commit();
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null) actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, @Nullable String key) {
        switch(key) {
            case "NW":
                selNw=pref.getBoolean(key,false);
                break;
            case "RS":
                selRs=pref.getBoolean(key,false);
                break;
            case "ARUN":
                autoRun=pref.getBoolean(key,false);
                PPPwnService.startstopService();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String str;
                        if(autoRun) str=getString(R.string.run_service_msg);
                        else str=getString(R.string.run_manual_msg);
                        MainActivity.txtOut.append(str);
                    }
                });
                break;
            case "ASHUT":
                autoShut=pref.getBoolean(key,false);
                break;
        }
        //Log.d("Droid",key+":"+pref.getBoolean(key,true));
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings, rootKey);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }
}