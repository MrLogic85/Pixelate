package com.sleepyduck.pixelate4crafting.model;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;

import com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns;

/**
 * Created by fredrikmetcalf on 20/10/16.
 */
public class DatabaseManager {

    private DatabaseManager() {
    }

    public static Pattern getPattern(ContentResolver resolver, int id) {
        try (Cursor cursor = resolver.query(ContentUris.withAppendedId(PatternColumns.URI, id),
                null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return getPattern(cursor);
            }
        }

        return new Pattern.Empty(id);
    }

    public static Pattern getPattern(Cursor cursor) {
        return new Pattern(cursor);
    }

    public static void update(ContentResolver resolver, Pattern.Edit edit) {
        ContentValues values = new ContentValues();

        for (String key : edit.getChangeKeys()) {
            switch (key) {
                case PatternColumns.TITLE:
                    values.put(key, edit.getString(key));
                    break;
                case PatternColumns.STATE:
                    values.put(key, edit.getInt(key));
                    break;
                case PatternColumns.FILE:
                    values.put(key, edit.getString(key));
                    break;
                case PatternColumns.FILE_THUMB:
                    values.put(key, edit.getString(key));
                    break;
                case PatternColumns.FILE_PATTERN:
                    values.put(key, edit.getString(key));
                    break;
                case PatternColumns.PIXEL_WIDTH:
                    values.put(key, edit.getString(key));
                    break;
                case PatternColumns.PIXEL_HEIGHT:
                    values.put(key, edit.getInt(key));
                    break;
                case PatternColumns.TIME:
                    values.put(key, edit.getLong(key));
                    break;
            }
        }

        int id = edit.getInt(PatternColumns._ID);
        resolver.update(ContentUris.withAppendedId(PatternColumns.URI, id), values, null, null);
    }

    public static void create(ContentResolver resolver, Pattern.EmptyEdit edit) {
        ContentValues values = new ContentValues();

        values.put(PatternColumns.TITLE, edit.getString(PatternColumns.TITLE));
        values.put(PatternColumns.STATE, edit.getInt(PatternColumns.STATE));
        values.put(PatternColumns.FILE, edit.getString(PatternColumns.FILE));
        values.put(PatternColumns.FILE_THUMB, edit.getString(PatternColumns.FILE_THUMB));
        values.put(PatternColumns.FILE_PATTERN, edit.getString(PatternColumns.FILE_PATTERN));
        values.put(PatternColumns.PIXEL_WIDTH, edit.getString(PatternColumns.PIXEL_WIDTH));
        values.put(PatternColumns.PIXEL_HEIGHT, edit.getInt(PatternColumns.PIXEL_HEIGHT));
        values.put(PatternColumns.TIME, edit.getLong(PatternColumns.TIME));

        resolver.insert(PatternColumns.URI, values);
    }
}
