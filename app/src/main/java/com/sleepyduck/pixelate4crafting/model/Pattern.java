package com.sleepyduck.pixelate4crafting.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.control.Constants;
import com.sleepyduck.pixelate4crafting.control.DataManager;
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;

import java.util.HashMap;
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
    private static final String PREF_FILE_PATTERN = "FILE_PATTERN";
    private static final String PREF_PIXEL_WIDTH = "PIXEL_WIDTH";
    private static final String PREF_PIXEL_HEIGHT = "PIXEL_HEIGHT";
    private static final String PREF_WEIGHT = "WEIGHT";
    private static final String PREF_NEEDS_CALCULATION = "RECALC";

    public final int Id;
    private String mTitle = "";
    private State mState = State.ACTIVE;
    private String mFileName = "";
    private String mFileNameThumb = "";
    private String mFileNamePattern = "";
    private boolean mNeedsRecalculation = true;
    private int mPixelWidth = Constants.DEFAULT_PIXELS;
    private int mPixelHeight = Constants.DEFAULT_PIXELS;
    private int mWeight = 0;
    private Map<Integer, Float> mColors;
    private int[][] mPixels;
    private boolean mColorsIsDirty = false;
    private boolean mPixelsIsDirty = false;

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
        if (mFileNamePattern != null && mFileNameThumb.length() > 0) {
            BitmapHandler.removeFileOfName(context, mFileNameThumb);
        }
        if (mColors != null) {
            DataManager.DestroyColors(context, Id);
        }
        if (mPixels != null) {
            DataManager.DestroyPixels(context, Id);
        }
    }

    public Pattern(Context context, int prefCounter, SharedPreferences pref) {
        Id = pref.getInt("" + prefCounter + PREF_ID, -1);
        mTitle = pref.getString("" + prefCounter + PREF_TITLE, mTitle);
        mState = State.valueOf(pref.getInt("" + prefCounter + PREF_STATE, State.ACTIVE.ordinal()));
        mFileName = pref.getString("" + prefCounter + PREF_FILE, mFileName);
        mFileNameThumb = pref.getString("" + prefCounter + PREF_FILE_THUMB, mFileNameThumb);
        mFileNamePattern = pref.getString("" + prefCounter + PREF_FILE_PATTERN, mFileNameThumb);
        mNeedsRecalculation = pref.getBoolean("" + prefCounter + PREF_NEEDS_CALCULATION, true);
        mPixelWidth = pref.getInt("" + prefCounter + PREF_PIXEL_WIDTH, mPixelWidth);
        mPixelHeight = pref.getInt("" + prefCounter + PREF_PIXEL_HEIGHT, mPixelHeight);
        mWeight = pref.getInt("" + prefCounter + PREF_WEIGHT, mWeight);
        mColors = DataManager.LoadColors(context, Id);
        mPixels = DataManager.LoadPixels(context, Id);
    }

    public void save(Context context, int prefCounter, SharedPreferences.Editor editor) {
        editor.putInt("" + prefCounter + PREF_ID, Id);
        editor.putString("" + prefCounter + PREF_TITLE, mTitle);
        editor.putInt("" + prefCounter + PREF_STATE, mState.ordinal());
        editor.putString("" + prefCounter + PREF_FILE, mFileName);
        editor.putString("" + prefCounter + PREF_FILE_THUMB, mFileNameThumb);
        editor.putString("" + prefCounter + PREF_FILE_PATTERN, mFileNamePattern);
        editor.putBoolean("" + prefCounter + PREF_NEEDS_CALCULATION, mNeedsRecalculation);
        editor.putInt("" + prefCounter + PREF_PIXEL_WIDTH, mPixelWidth);
        editor.putInt("" + prefCounter + PREF_PIXEL_HEIGHT, mPixelHeight);
        editor.putInt("" + prefCounter + PREF_WEIGHT, mWeight);
        if (mColors != null && mColorsIsDirty) {
            DataManager.SaveColors(context, Id, mColors);
            mColorsIsDirty = false;
        }
        if (mPixels != null && mPixelsIsDirty) {
            DataManager.SavePixels(context, Id, mPixels);
            mPixelsIsDirty = false;
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

    public void setPixelWidth(Context context, int i) {
        if (mPixelWidth != i) {
            setNeedsRecalculation(true);
        }
        mPixelWidth = i;

        Bitmap bitmap = BitmapHandler.getFromFileName(context, getFileName());
        float pixelSize = (float) bitmap.getWidth() / (float) getPixelWidth();
        int height = (int) (bitmap.getHeight() / pixelSize);
        setPixelHeight(height);
        bitmap.recycle();
    }

    public void setPixelHeight(int i) {
        if (mPixelHeight != i) {
            setNeedsRecalculation(true);
        }
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
        setNeedsRecalculation(true);
    }

    public Map<Integer, Float> getColors() {
        return mColors;
    }

    public boolean hasColors() {
        return getColors() != null && getColors().size() > 0;
    }

    public void removeColor(int color) {
        if (mColors.remove(color) != null) {
            setNeedsRecalculation(true);
            mColorsIsDirty = true;
        }
    }

    public void addColor(int pixel) {
        if (mColors == null) {
            mColors = new HashMap<>();
        }
        mColors.put(pixel, 0f);
        setNeedsRecalculation(true);
        mColorsIsDirty = true;
    }

    public void setPixels(int[][] pixels) {
        setNeedsRecalculation(pixels == null);
        mPixels = pixels;
        mPixelsIsDirty = true;
    }

    public int[][] getPixels() {
        return mPixels;
    }

    public void setNeedsRecalculation(boolean recalc) {
        mNeedsRecalculation = recalc;
        if (recalc) {
            setPatternFileName("");
        }
    }

    public boolean needsRecalculation() {
        return mNeedsRecalculation;
    }

    public void setPatternFileName(String patternFileName) {
        mFileNamePattern = patternFileName;
    }

    public String getPatternFileName() {
        return mFileNamePattern;
    }
}
