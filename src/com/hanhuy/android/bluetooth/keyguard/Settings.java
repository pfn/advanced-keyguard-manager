package com.hanhuy.android.bluetooth.keyguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Collections;
import java.util.List;

public class Settings {
    public final static Setting<Boolean> LOCK_DISABLED =
            new BooleanSetting("lock_disabled");
    public final static Setting<String> PASSWORD_HASH =
            new StringSetting("password_hash");
    public final static Setting<String> PASSWORD =
            new StringSetting("password");
    public final static Setting<List<String>> BLUETOOTH_DEVICES =
            new StringListSetting("bluetooth_devices");
    public final static Setting<List<String>> WIFI_NETWORKS =
            new StringListSetting("wifi_networks");
    public final static Setting<Boolean> WIFI_CLEAR_KEYGUARD =
            new BooleanSetting("wifi_clear_keyguard");
    public final static Setting<Boolean> BT_CLEAR_KEYGUARD =
            new BooleanSetting("bt_clear_keyguard");
    public final static Setting<List<String>> BLUETOOTH_CONNECTIONS =
            new StringListSetting("connected_devices");
    private static final String TAG = "Settings";

    private final Gson gson = new Gson();

    public abstract static class Setting<T> {
        public final String key;
        public Setting(String key) {
            this.key = key;
        }
        public String toString() {
            return key;
        }
    }

    public static class StringSetting extends Setting<String> {
        public StringSetting(String key) { super(key); }
    }
    public static class BooleanSetting extends Setting<Boolean> {
        public BooleanSetting(String key) { super(key); }
    }

    public static class StringListSetting extends Setting<List<String>> {
        public StringListSetting(String key) { super(key); }
    }

    private static Settings instance;
    private SharedPreferences prefs;

    private Settings(Context c) {
         prefs = PreferenceManager.getDefaultSharedPreferences(c);
    }

    public static Settings getInstance(Context c) {
        if (instance == null)
            instance = new Settings(c.getApplicationContext());
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T> void set(Setting<T> setting, T value) {
        SharedPreferences.Editor editor = prefs.edit();
        if (setting instanceof StringSetting) {
            editor.putString(setting.key, (String) value);
        } else if (setting instanceof BooleanSetting) {
            editor.putBoolean(setting.key, (Boolean) value);
        } else if (setting instanceof StringListSetting) {
            List<String> values = (List<String>) value;
            String json = gson.toJson(values);
            editor.putString(setting.key, json);
        } else {
            throw new RuntimeException("Unknown setting type");
        }
        editor.commit();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Setting<T> setting, T defaultValue) {
        if (setting instanceof StringSetting) {
            return (T) prefs.getString(setting.key, (String) defaultValue);
        } else if (setting instanceof BooleanSetting) {
            return (T) Boolean.valueOf(prefs.getBoolean(setting.key,
                    defaultValue == null ? false : (Boolean) defaultValue));
        } else if (setting instanceof StringListSetting) {
            String json = prefs.getString(setting.key, null);
            if (json == null) {
                return (T) Collections.EMPTY_LIST;
            } else {
                try {
                    List<String> strings = gson.fromJson(json, List.class);
                    return (T) (strings == null ?
                            Collections.EMPTY_LIST : strings);
                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "Bad json", e);
                    return (T) Collections.EMPTY_LIST;
                }
            }
        } else {
            throw new RuntimeException("Unknown setting type");
        }
    }

    public <T> T get(Setting<T> setting) {
        return get(setting, null);
    }
}
