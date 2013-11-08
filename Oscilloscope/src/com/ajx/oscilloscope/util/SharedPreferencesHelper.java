package com.ajx.oscilloscope.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class SharedPreferencesHelper {

    public static void putInt(Context context, String key, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = preferences.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    public static void putBoolean(Context context, String key, boolean val) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = preferences.edit();
        edit.putBoolean(key, val);
        edit.commit();
    }

    public static void putString(Context context, String key, String val) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = preferences.edit();
        edit.putString(key, val);
        edit.commit();
    }

    public static void putFloat(Context context, String key, float val) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = preferences.edit();
        edit.putFloat(key, val);
        edit.commit();
    }

    public static void putLong(Context context, String key, long val) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = preferences.edit();
        edit.putLong(key, val);
        edit.commit();
    }

    public static void putDouble(Context context, String key, double val) {
        putLong(context, key, Double.doubleToRawLongBits(val));
    }

    public static long getLong(Context context, String key, long _default) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getLong(key, _default);
    }

    public static double getDouble(Context context, String key, double _default) {
        return Double.longBitsToDouble(getLong(context, key, Double.doubleToLongBits(_default)));
    }

    public static float getFloat(Context context, String key, float _default) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getFloat(key, _default);
    }

    public static String getString(Context context, String key, String _default) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, _default);
    }

    public static int getInt(Context context, String key, int _default) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key, _default);
    }

    public static boolean getBoolean(Context context, String key, boolean _default) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, _default);
    }

    public static class SharedPreferencesKeys {
        /* Chart preferences */
        public static final String MIN_VALUE = "min_value";
        public static final String MAX_VALUE = "max_value";
        public static final String BUFFER_OFFSET = "buffer_offset";
        public static final String REFERENCE_VOLTAGE = "reference_voltage";
        public static final String MULTIPLIER = "multiplier";
        public static final String GAIN = "gain";
        public static final String ADC_RESOLUTION = "adc_resolution";
        public static final String CALIBRATE_ON_STARTUP = "calibrate_on_startup";
        public static final String PLOT_RAW_DATA = "plot_raw_data";
        /* Device preferences */
        public static final String BAUD_RATE = "baud_rate";
        public static final String DATA_BITS = "data_bits";
        public static final String STOP_BITS = "stop_bits";
        public static final String PARITY = "parity";
        public static final String START_COMMAND = "start_command";
        public static final String STOP_COMMAND = "stop_command";
        public static final String SWAP_BYTES = "swap_bytes";
        public static final String ALLOW_BAD_BYTES = "allow_bad_bytes";
    }

}
