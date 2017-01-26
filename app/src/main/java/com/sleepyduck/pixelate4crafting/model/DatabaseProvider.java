package com.sleepyduck.pixelate4crafting.model;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.sleepyduck.pixelate4crafting.BuildConfig;
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns;

public class DatabaseProvider extends ContentProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID;

    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int MATCH_TABLE = 1;
    private static final int MATCH_TABLE_ID = 2;

    static {
        matcher.addURI(AUTHORITY, PatternColumns.TABLE_NAME, MATCH_TABLE);
        matcher.addURI(AUTHORITY, PatternColumns.TABLE_NAME + "/#", MATCH_TABLE_ID);
    }

    private DatabaseOpenHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseOpenHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (matcher.match(uri)) {
            case MATCH_TABLE:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + "/" + PatternColumns.TABLE_NAME;
            case MATCH_TABLE_ID:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + "/" + PatternColumns.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Invalid Uri");
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        final long rowId = dbHelper.getWritableDatabase().insert(getTable(uri), null, values);

        if (rowId == -1) {
            BetterLog.d(this, "Failed to insert, uri='%s', %s", uri, values);
            return null;

        } else {
            BetterLog.d(this, "insert [uri=%s] | '%s'", uri, values);
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return ContentUris.withAppendedId(uri, rowId);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        switch (matcher.match(uri)) {
            case MATCH_TABLE_ID:
                selection = String.format("%s = ?", PatternColumns._ID);
                selectionArgs = new String[] {uri.getLastPathSegment()};
                break;
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return dbHelper.getWritableDatabase().delete(getTable(uri), selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) {
        if (values.size() == 0) {
            return 0;
        }

        switch (matcher.match(uri)) {
            case MATCH_TABLE_ID:
                selection = String.format("%s = ?", PatternColumns._ID);
                selectionArgs = new String[] {uri.getLastPathSegment()};
                break;
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return dbHelper.getWritableDatabase()
                .update(getTable(uri), values, selection, selectionArgs);
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        switch (matcher.match(uri)) {
            case MATCH_TABLE_ID:
                selection = String.format("%s = ?", PatternColumns._ID);
                selectionArgs = new String[] {uri.getLastPathSegment()};
                break;
        }

        Cursor cursor = dbHelper.getReadableDatabase()
                .query(getTable(uri), projection, selection, selectionArgs, null, null, sortOrder);

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    private String getTable(Uri uri) {
        switch (matcher.match(uri)) {
            case MATCH_TABLE:
            case MATCH_TABLE_ID:
                return PatternColumns.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Invalid Uri");
        }
    }
}
