package com.sleepyduck.pixelate4crafting.model;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.database.Cursor;

import com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns;

/**
 * Created by fredrikmetcalf on 20/10/16.
 */
public class DatabaseManager {

    private DatabaseManager() {
    }

    public static Pattern getPattern(ContentProvider provider, int id) {
        try (Cursor cursor = provider.query(ContentUris.withAppendedId(PatternColumns.URI, id),
                null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return getPattern(cursor);
            }
        }

        return new Pattern.Empty(id);
    }

    private static Pattern getPattern(Cursor cursor) {
        final int id = cursor.getInt(cursor.getColumnIndex(PatternColumns._ID));
        final String title = cursor.getString(cursor.getColumnIndex(PatternColumns.TIME));

        return new Pattern(id, title);
    }
}
