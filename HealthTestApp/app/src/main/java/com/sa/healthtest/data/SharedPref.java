package com.sa.healthtest.data;

import android.content.SharedPreferences;

public class SharedPref {

    private static final boolean NOT_CONNECTED = false;
    private final SharedPreferences preferences;

    public SharedPref(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public boolean isConnected(String flag) {
        return preferences.getBoolean(flag, NOT_CONNECTED);
    }


    public void setConnected(String flag, boolean isConnected){
        preferences.edit().putBoolean(flag, isConnected).apply();
    }
}
