package com.sleepyduck.pixelate4crafting.model;

import android.content.Context;
import android.content.SharedPreferences;

public class Patterns {
    private static final String PREFERENCE_NAME = "PATTERNS";
    private static final String PREF_COUNT = "COUNT";
    private static final String PREF_USING_DATABASE = "DATABASE";

    public static final String INTENT_EXTRA_ID = "EXTRA_ID";

    private Patterns() {
    }

    public static synchronized void Load(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, 0);
        if (pref.getBoolean(PREF_USING_DATABASE, false)) {
            //context.getContentResolver().delete(DatabaseContract.PatternColumns.URI, null, null);
            return;
            // TODO, reverse the lines above, uncomment the return and remove the delete
        }
        int size = pref.getInt(PREF_COUNT, 0);
        for (int i = 0; i < size; ++i) {
            Pattern pattern = new Pattern(context, i, pref);

            Pattern p = DatabaseManager.getPattern(context, pattern.Id);
            p.edit().set(pattern).apply();
        }
        pref.edit().putBoolean(PREF_USING_DATABASE, true).apply();
    }
}
