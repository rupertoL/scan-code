package cn.shequren.scancode;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by liupeng on 2018-08-25.
 */

public class SharedPreferencesUtils {
    public SharedPreferences mPref;

    private static class Holder {
        private static SharedPreferencesUtils mSharedPreferencesUtils = new SharedPreferencesUtils();
    }


    private SharedPreferencesUtils() {

        mPref = PreferenceManager.getDefaultSharedPreferences(ScanCodeMangerUtils.newInstance().getContext());
    }

    public static SharedPreferencesUtils newInstance() {
        return Holder.mSharedPreferencesUtils;
    }

    public void putString(String value, String key) {
        mPref.edit().putString(value, key).commit();
    }

    public String getString(String value, String defaultString) {
        return mPref.getString(value, defaultString);
    }

    public void putBoolean(String value, boolean key) {
        mPref.edit().putBoolean(value, key).commit();
    }

    public boolean getString(String value, boolean defaultBoolean) {
        return mPref.getBoolean(value, defaultBoolean);
    }

    public void putInt(String value, int key) {
        mPref.edit().putInt(value, key).commit();
    }

    public int getString(String value, int defaultInt) {
        return mPref.getInt(value, defaultInt);
    }
}
