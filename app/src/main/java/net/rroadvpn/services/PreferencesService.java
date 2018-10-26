package net.rroadvpn.services;


import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class PreferencesService {
    private final Context context;
    private SharedPreferences preferences;

    public PreferencesService(Context context, String preferenceType) {
        this.context = context;
        this.preferences = context.getSharedPreferences(preferenceType, Context.MODE_PRIVATE);
        //todo check null
    }

    public void save(String prefLabel, String value) {
        SharedPreferences.Editor ed = this.preferences.edit();
        ed.putString(prefLabel, value);
        ed.apply();
    }

    public void save(String prefLabel, Integer value) {
        SharedPreferences.Editor ed = this.preferences.edit();
        ed.putInt(prefLabel, value);
        ed.apply();
    }

    public void clear(){
        SharedPreferences.Editor ed = this.preferences.edit();
        ed.clear();
        ed.apply();
    }



    public String getString(String prefLabel) {
        return this.preferences.getString(prefLabel, "");
    }

    public Integer getInteger(String prefLabel) {
        return this.preferences.getInt(prefLabel, -1);
    }

}

