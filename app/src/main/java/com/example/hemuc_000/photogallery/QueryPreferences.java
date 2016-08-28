package com.example.hemuc_000.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by hemuc_000 on 7/19/2016.
 */
public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY="searchQuery";
    private static final String PREF_LAST_RESULT_ID="lastResultID";
    private static final String PREF_IS_ALARM_ON="isAlarmOn";

    public static String getStoredQuery(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY,null);
    }

    public static void setStoredQuery(Context context, String query){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY,query)
                .apply();
    }

    public static void setLastResultID(Context c, String lastResultID){
        PreferenceManager.getDefaultSharedPreferences(c)
                .edit()
                .putString(PREF_LAST_RESULT_ID,lastResultID)
                .apply();

    }
    public static String getLastResultID(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c)
                .getString(PREF_LAST_RESULT_ID,null);


    }
    public static boolean isAlarmOn(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c)
                .getBoolean(PREF_IS_ALARM_ON, false);

    }

    public static void setAlarmOn(Context c, boolean isOn){
        PreferenceManager.getDefaultSharedPreferences(c)
                .edit()
                .putBoolean(PREF_IS_ALARM_ON,isOn)
                .apply();
    }
}
