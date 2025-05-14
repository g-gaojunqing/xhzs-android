package com.example.dzxh_app.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by smile on 02/03/2019.
 */

public class SharedPreferencesUtil {
    private static final String TAG = "TAG";
    private static final String KEY_LOGIN = "KEY_LOGIN";

    private static SharedPreferences mPreferences;
    private static SharedPreferences.Editor mEditor;
    private static SharedPreferencesUtil mSharedPreferencesUtil;
    private final Context context;

    public SharedPreferencesUtil(Context context) {
        this.context = context.getApplicationContext();
        mPreferences = this.context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
    }

    /**
     * 单例简单实现（因为Android中没有Java Web那样的高并发量，所以单例可以这样简单实现）
     * @param context
     * @return
     */
    public static SharedPreferencesUtil getInstance(Context context) {
        if (mSharedPreferencesUtil ==null){
            mSharedPreferencesUtil =new SharedPreferencesUtil(context);
        }
        return  mSharedPreferencesUtil;
    }

    /**私有方法*/
    public void putString(String key, String value) {
        mEditor.putString(key,value);
        mEditor.commit();
    }

    public String getString(String key,String defaultValue) {
        return mPreferences.getString(key,defaultValue);
    }
    public void putBoolean(String key, boolean value) {
        mEditor.putBoolean(key,value);
        mEditor.commit();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return mPreferences.getBoolean(key,defaultValue);
    }


    public void putInt(String key, int value) {
        mEditor.putInt(key,value);
        mEditor.apply();
    }

    public int getInt(String key, int defaultValue) {
        return mPreferences.getInt(key,defaultValue);
    }

    public void putLong(String key, long value) {
        mEditor.putLong(key,value);
        mEditor.apply();
    }

    public long getLong(String key, long defaultValue) {
        return mPreferences.getLong(key, defaultValue);
    }
}
