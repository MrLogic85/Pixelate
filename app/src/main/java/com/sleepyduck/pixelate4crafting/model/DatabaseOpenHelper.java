package com.sleepyduck.pixelate4crafting.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 8;

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
                PatternColumns.CHANGED_PIXELS + " TEXT, " +
                PatternColumns.TIME + " BIGINT, " +
                PatternColumns.FLAG + " INTEGER, " +
                PatternColumns.PROGRESS + " INTEGER, " +
                PatternColumns.MARKER + " TEXT DEFAULT '0:0', " +
                PatternColumns.PENDING_DELETE + " INTEGER DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == newVersion) {
            return;
        }

        switch (oldVersion) {
            case 4:
                db.execSQL("ALTER TABLE " + PatternColumns.TABLE_NAME +
                        " ADD " + PatternColumns.CHANGED_PIXELS + " TEXT");
            case 5:
                db.execSQL("ALTER TABLE " + PatternColumns.TABLE_NAME +
                        " ADD " + PatternColumns.PENDING_DELETE + " INTEGER DEFAULT 0");
                db.execSQL("UPDATE " + PatternColumns.TABLE_NAME
                        + " SET " + PatternColumns.PIXELS + "=''"
                        + " WHERE LENGTH(" + PatternColumns.PIXELS + ") > 10000");
            case 6:
                db.execSQL("UPDATE " + PatternColumns.TABLE_NAME
                        + " SET " + PatternColumns.FLAG + "=" + PatternColumns.FLAG_PIXELS_CALCULATED
                        + " WHERE " + PatternColumns.FLAG + " = " + PatternColumns.FLAG_COMPLETE);
            case 7:
                db.execSQL("ALTER TABLE " + PatternColumns.TABLE_NAME +
                        " ADD " + PatternColumns.MARKER + " TEXT DEFAULT '0:0'");
        }

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE " + PatternColumns.TABLE_NAME);
        } catch (SQLiteException ignored) {
        }
        onCreate(db);
    }
}
