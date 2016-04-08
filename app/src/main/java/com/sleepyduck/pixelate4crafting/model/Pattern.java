package com.sleepyduck.pixelate4crafting.model;

import android.content.SharedPreferences;

import com.sleepyduck.pixelate4crafting.old.Constants;
import com.sleepyduck.pixelate4crafting.util.BetterLog;

/**
 * Created by fredrik.metcalf on 2016-04-08.
 */
public class Pattern implements Comparable<Pattern> {
    private static final String PREF_ID = "ID";
    private static final String PREF_TITLE = "TITLE";
    private static final String PREF_PALETTE = "PALETTE";
    private static final String PREF_FILE = "FILE";
    private static final String PREF_FILE_THUMB = "FILE_THUMB";
    private static final String PREF_PIXEL_WIDTH = "PIXEL_WIDTH";
    private static final String PREF_PIXEL_HEIGHT = "PIXEL_HEIGHT";

    public final int Id;
    private String mTitle = "";
    private int mPaletteId = -1;
    private String mFileName = "";
    private String mFileNameThumb = "";
    private int mPixelWidth = Constants.DEFAULT_PIXELS;
    private int mPixelHeight = Constants.DEFAULT_PIXELS;

    public Pattern(String title) {
        Id = (int) (Math.random() * Integer.MAX_VALUE);
        mTitle = title;
    }

    public Pattern(int prefCounter, SharedPreferences pref) {
        Id = pref.getInt("" + prefCounter + PREF_ID, -1);
        mTitle = pref.getString("" + prefCounter + PREF_TITLE, mTitle);
        mPaletteId = pref.getInt("" + prefCounter + PREF_PALETTE, mPaletteId);
        mFileName = pref.getString("" + prefCounter + PREF_FILE, mFileName);
        mFileNameThumb = pref.getString("" + prefCounter + PREF_FILE_THUMB, mFileNameThumb);
        mPixelWidth = pref.getInt("" + prefCounter + PREF_PIXEL_WIDTH, mPixelWidth);
        mPixelHeight = pref.getInt("" + prefCounter + PREF_PIXEL_HEIGHT, mPixelHeight);
    }

    public void save(int prefCounter, SharedPreferences.Editor editor) {
        editor.putInt("" + prefCounter + PREF_ID, Id);
        editor.putString("" + prefCounter + PREF_TITLE, mTitle);
        editor.putInt("" + prefCounter + PREF_PALETTE, mPaletteId);
        editor.putString("" + prefCounter + PREF_FILE, mFileName);
        editor.putString("" + prefCounter + PREF_FILE_THUMB, mFileNameThumb);
        editor.putInt("" + prefCounter + PREF_PIXEL_WIDTH, mPixelWidth);
        editor.putInt("" + prefCounter + PREF_PIXEL_HEIGHT, mPixelHeight);
    }

    @Override
    public int compareTo(Pattern another) {
        return this.mTitle.compareTo(another.mTitle);
    }

    public void setPaletteId(int id) {
        mPaletteId = id;
    }

    public int getPaletteId() {
        return mPaletteId;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
        setFileNameThumbnail(fileName + Constants.FILE_THUMBNAIL);
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileNameThumbnail(String fileName) {
        mFileNameThumb = fileName;
    }

    public String getFileNameThumbnail() {
        return mFileNameThumb;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public static String createTitleFromFileName(String fileName) {
        BetterLog.d(Pattern.class, fileName);
        String tmp = fileName.split("\\.")[0];
        BetterLog.d(Pattern.class, tmp);
        tmp = tmp.replaceAll("_", " ");
        BetterLog.d(Pattern.class, tmp);
        return tmp;
    }

    public void setPixelWidth(int i) {
        mPixelWidth = i;
    }

    public void setPixelHeight(int i) {
        mPixelHeight = i;
    }

    public int getPixelWidth() {
        return mPixelWidth;
    }

    public int getPixelHeight() {
        return mPixelHeight;
    }
}
