package com.sleepyduck.pixelate4crafting.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.sleepyduck.pixelate4crafting.util.BetterLog;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class DatabaseManager {

    private DatabaseManager() {
    }

    public static Pattern getPattern(Context context, int id) {
        try (Cursor cursor = context.getContentResolver()
                .query(ContentUris.withAppendedId(PatternColumns.URI, id),
                null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return getPattern(context, cursor);
            }
        }

        return new Pattern.Empty(context);
    }

    public static Pattern getPattern(Context context, Cursor cursor) {
        return new Pattern(context, cursor);
    }

    public static int update(ContentResolver resolver, Pattern.Edit edit) {
        ContentValues values = new ContentValues();

        for (String key : edit.getChangeKeys()) {
            switch (key) {
                case PatternColumns.TITLE:
                case PatternColumns.FILE:
                case PatternColumns.FILE_THUMB:
                case PatternColumns.FILE_PATTERN:
                    values.put(key, edit.getString(key));
                    break;
                case PatternColumns.STATE:
                case PatternColumns.WIDTH:
                case PatternColumns.HEIGHT:
                case PatternColumns.FLAG:
                case PatternColumns.PROGRESS:
                    values.put(key, edit.getInt(key));
                    break;
                case PatternColumns.TIME:
                    values.put(key, edit.getLong(key));
                    break;
                case PatternColumns.COLORS: {
                    Object value = edit.get(key);
                    if (value == null) {
                        values.put(key, "");
                        break;
                    }
                    @SuppressWarnings("unchecked")
                    String colors = colorsToString((Map<Integer, Float>) value);
                    values.put(key, colors);
                    break;
                }
                case PatternColumns.PIXELS: {
                    Object value = edit.get(key);
                    if (value == null) {
                        values.put(key, "");
                        break;
                    }
                    @SuppressWarnings("unchecked")
                    String pixels = pixelsToString((int[][]) value);
                    values.put(key, pixels);
                    break;
                }
            }
        }

        int id = edit.getInt(PatternColumns._ID);
        resolver.update(ContentUris.withAppendedId(PatternColumns.URI, id), values, null, null);
        return id;
    }

    public static int create(ContentResolver resolver, Pattern.EmptyEdit edit) {
        ContentValues values = new ContentValues();

        values.put(PatternColumns.TITLE, edit.getString(PatternColumns.TITLE));
        values.put(PatternColumns.STATE, edit.getInt(PatternColumns.STATE));
        values.put(PatternColumns.FILE, edit.getString(PatternColumns.FILE));
        values.put(PatternColumns.FILE_THUMB, edit.getString(PatternColumns.FILE_THUMB));
        values.put(PatternColumns.FILE_PATTERN, edit.getString(PatternColumns.FILE_PATTERN));
        values.put(PatternColumns.WIDTH, edit.getInt(PatternColumns.WIDTH));
        values.put(PatternColumns.HEIGHT, edit.getInt(PatternColumns.HEIGHT));
        values.put(PatternColumns.TIME, edit.getLong(PatternColumns.TIME));
        values.put(PatternColumns.FLAG, edit.getInt(PatternColumns.FLAG));
        values.put(PatternColumns.PROGRESS, edit.getInt(PatternColumns.PROGRESS));

        Object colorObj = edit.get(PatternColumns.COLORS);
        if (colorObj == null) {
            values.put(PatternColumns.COLORS, "");
        } else {
            @SuppressWarnings("unchecked")
            String colors = colorsToString((Map<Integer, Float>) colorObj);
            values.put(PatternColumns.COLORS, colors);
        }

        Object pixelsObj = edit.get(PatternColumns.PIXELS);
        if (pixelsObj == null) {
            values.put(PatternColumns.PIXELS, "");
        } else {
            @SuppressWarnings("unchecked")
            String colors = pixelsToString((int[][]) pixelsObj);
            values.put(PatternColumns.PIXELS, colors);
        }

        Uri uri = resolver.insert(PatternColumns.URI, values);
        BetterLog.d(DatabaseManager.class, "Inserted, %s", uri);
        return (int) ContentUris.parseId(uri);
    }

    public static void deletePattern(ContentResolver resolver, int id) {
        resolver.delete(ContentUris.withAppendedId(PatternColumns.URI, id), null, null);
    }

    private static String pixelsToString(int[][] pixels) {
        JSONObject json = new JSONObject();
        try {
            json.put("width", pixels.length);
            json.put("height", pixels[0].length);
            for (int w = 0; w < pixels.length; ++w) {
                for (int h = 0; h < pixels[w].length; ++h) {
                    json.put("" + w + "," + h, pixels[w][h]);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    private static String colorsToString(Map<Integer, Float> colors) {
        JSONObject json = new JSONObject();
        try {
            json.put("size", colors.size());
            int count = 0;
            for (Map.Entry<Integer, Float> color : colors.entrySet()) {
                json.put("color" + count, color.getKey().toString());
                json.put("weight" + count++, color.getValue().toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
