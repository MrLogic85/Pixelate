package com.sleepyduck.pixelate4crafting.model;

import android.content.SharedPreferences;

import com.sleepyduck.pixelate4crafting.control.Constants;
import com.sleepyduck.pixelate4crafting.util.BetterLog;

/**
 * Created by fredrik.metcalf on 2016-04-08.
 */
public class Pattern implements Comparable<Pattern> {
    private static final String PREF_ID = "ID";
    private static final String PREF_TITLE = "TITLE";
    private static final String PREF_STATE = "STATE";
    private static final String PREF_FILE = "FILE";
    private static final String PREF_FILE_THUMB = "FILE_THUMB";
    private static final String PREF_PIXEL_WIDTH = "PIXEL_WIDTH";
    private static final String PREF_PIXEL_HEIGHT = "PIXEL_HEIGHT";
    private static final String PREF_WEIGHT = "WEIGHT";

    public final int Id;
    private String mTitle = "";
    private State mState = State.ACTIVE;
    private String mFileName = "";
    private String mFileNameThumb = "";
    private int mPixelWidth = Constants.DEFAULT_PIXELS;
    private int mPixelHeight = Constants.DEFAULT_PIXELS;
    private int mWeight = 0;
    private int[] mColors;

    public void setColors(int[] colors) {
        mColors = colors;
    }

    public int[] getColors() {
        return mColors;
    }

    public enum State{
        LATEST,
        ACTIVE,
        COMPLETED;

        public static State valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < State.values().length) {
                return State.values()[ordinal];
            }
            return State.ACTIVE;
        }
    }

    public Pattern(String title) {
        Id = (int) (Math.random() * Integer.MAX_VALUE);
        mTitle = title;
    }

    public Pattern(int prefCounter, SharedPreferences pref) {
        Id = pref.getInt("" + prefCounter + PREF_ID, -1);
        mTitle = pref.getString("" + prefCounter + PREF_TITLE, mTitle);
        mState = State.valueOf(pref.getInt("" + prefCounter + PREF_STATE, State.ACTIVE.ordinal()));
        mFileName = pref.getString("" + prefCounter + PREF_FILE, mFileName);
        mFileNameThumb = pref.getString("" + prefCounter + PREF_FILE_THUMB, mFileNameThumb);
        mPixelWidth = pref.getInt("" + prefCounter + PREF_PIXEL_WIDTH, mPixelWidth);
        mPixelHeight = pref.getInt("" + prefCounter + PREF_PIXEL_HEIGHT, mPixelHeight);
        mWeight = pref.getInt("" + prefCounter + PREF_WEIGHT, mWeight);
    }

    public void save(int prefCounter, SharedPreferences.Editor editor) {
        editor.putInt("" + prefCounter + PREF_ID, Id);
        editor.putString("" + prefCounter + PREF_TITLE, mTitle);
        editor.putInt("" + prefCounter + PREF_STATE, mState.ordinal());
        editor.putString("" + prefCounter + PREF_FILE, mFileName);
        editor.putString("" + prefCounter + PREF_FILE_THUMB, mFileNameThumb);
        editor.putInt("" + prefCounter + PREF_PIXEL_WIDTH, mPixelWidth);
        editor.putInt("" + prefCounter + PREF_PIXEL_HEIGHT, mPixelHeight);
        editor.putInt("" + prefCounter + PREF_WEIGHT, mWeight);
    }

    @Override
    public int compareTo(Pattern another) {
        return this.mTitle.compareTo(another.mTitle);
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
        setFileNameThumbnail(fileName + Constants.FILE_THUMBNAIL);
    }

    public String getFileName() {
        return mFileName;
    }

    public void setState(State newState) {
        mState = newState;
    }

    public State getState() {
        return mState;
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

    public int getWeight() {
        return mWeight;
    }

    public void setWeight(int weight) {
        mWeight = weight;
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
