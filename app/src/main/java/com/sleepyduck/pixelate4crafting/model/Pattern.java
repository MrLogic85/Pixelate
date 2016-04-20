package com.sleepyduck.pixelate4crafting.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.control.Constants;
import com.sleepyduck.pixelate4crafting.control.DataManager;
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;
import com.sleepyduck.pixelate4crafting.control.util.MMCQ;

import java.util.Map;

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
    private Map<Integer, Float> mColors;
    private int[][] mColorMatrix;
    private MMCQ.CMap mCMap;

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

    public void destroy(Context context) {
        if (mFileName != null && mFileName.length() > 0) {
            BitmapHandler.removeFileOfName(context, mFileName);
        }
        if (mFileNameThumb != null && mFileNameThumb.length() > 0) {
            BitmapHandler.removeFileOfName(context, mFileNameThumb);
        }
        if (mColors != null) {
            DataManager.DestroyColors(context, Id);
        }
        if (mColorMatrix != null) {
            DataManager.DestroyPixels(context, Id);
        }
    }

    public Pattern(Context context, int prefCounter, SharedPreferences pref) {
        Id = pref.getInt("" + prefCounter + PREF_ID, -1);
        mTitle = pref.getString("" + prefCounter + PREF_TITLE, mTitle);
        mState = State.valueOf(pref.getInt("" + prefCounter + PREF_STATE, State.ACTIVE.ordinal()));
        mFileName = pref.getString("" + prefCounter + PREF_FILE, mFileName);
        mFileNameThumb = pref.getString("" + prefCounter + PREF_FILE_THUMB, mFileNameThumb);
        mPixelWidth = pref.getInt("" + prefCounter + PREF_PIXEL_WIDTH, mPixelWidth);
        mPixelHeight = pref.getInt("" + prefCounter + PREF_PIXEL_HEIGHT, mPixelHeight);
        mWeight = pref.getInt("" + prefCounter + PREF_WEIGHT, mWeight);
        mColors = DataManager.LoadColors(context, Id);
        mColorMatrix = DataManager.LoadPixels(context, Id);
    }

    public void save(Context context, int prefCounter, SharedPreferences.Editor editor) {
        editor.putInt("" + prefCounter + PREF_ID, Id);
        editor.putString("" + prefCounter + PREF_TITLE, mTitle);
        editor.putInt("" + prefCounter + PREF_STATE, mState.ordinal());
        editor.putString("" + prefCounter + PREF_FILE, mFileName);
        editor.putString("" + prefCounter + PREF_FILE_THUMB, mFileNameThumb);
        editor.putInt("" + prefCounter + PREF_PIXEL_WIDTH, mPixelWidth);
        editor.putInt("" + prefCounter + PREF_PIXEL_HEIGHT, mPixelHeight);
        editor.putInt("" + prefCounter + PREF_WEIGHT, mWeight);
        if (mColors != null) {
            DataManager.SavePixels(context, Id, mColors);
        }
        if (mColorMatrix != null) {
            DataManager.SavePixels(context, Id, mColorMatrix);
        }
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

    public void setColors(Map<Integer, Float> colors) {
        mColors = colors;
    }

    public Map<Integer, Float> getColors() {
        return mColors;
    }

    public void setColorMatrix(int[][] colorMatrix) {
        mColorMatrix = colorMatrix;
    }

    public int[][] getColorMatrix() {
        return mColorMatrix;
    }

    public void setCMap(MMCQ.CMap cMap) {
        mCMap = cMap;
    }

    public MMCQ.CMap getCMap() {
        return mCMap;
    }
}
