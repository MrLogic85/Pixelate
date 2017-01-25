package com.sleepyduck.pixelate4crafting.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 2;

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + PatternColumns.TABLE_NAME + " (" +
                PatternColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PatternColumns.TITLE + " TEXT," +
                PatternColumns.STATE + " INTEGER, " +
                PatternColumns.FILE + " TEXT, " +
                PatternColumns.FILE_THUMB + " TEXT, " +
                PatternColumns.FILE_PATTERN + " TEXT, " +
                PatternColumns.HEIGHT + " INTEGER, " +
                PatternColumns.WIDTH + " INTEGER, " +
                PatternColumns.PIXELS + " TEXT, " +
                PatternColumns.COLORS + " TEXT, " +
                PatternColumns.TIME + " BIGINT, " +
                PatternColumns.NEEDS_RECALCULATION + " BOOLEAN)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PatternColumns.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
