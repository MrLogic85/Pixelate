package com.sleepyduck.pixelate4crafting.model;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.control.Constants;
import com.sleepyduck.pixelate4crafting.control.DataManager;
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    private long mWeight = 0;
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

    public Pattern(Cursor cursor) {
        Id = cursor.getInt(cursor.getColumnIndex(PatternColumns._ID));
        mTitle = cursor.getString(cursor.getColumnIndex(PatternColumns.TITLE));
        mState = State.valueOf(cursor.getInt(cursor.getColumnIndex(PatternColumns.STATE)));
        mFileName = cursor.getString(cursor.getColumnIndex(PatternColumns.FILE));
        mFileNameThumb = cursor.getString(cursor.getColumnIndex(PatternColumns.FILE_THUMB));
        mFileNamePattern = cursor.getString(cursor.getColumnIndex(PatternColumns.FILE_PATTERN));
        mPixelWidth = cursor.getInt(cursor.getColumnIndex(PatternColumns.PIXEL_WIDTH));
        mPixelHeight = cursor.getInt(cursor.getColumnIndex(PatternColumns.PIXEL_HEIGHT));
        mWeight = cursor.getLong(cursor.getColumnIndex(PatternColumns.TIME));
    }

    public Pattern(int id) {
        this(id, "");
    }

    public Pattern(String title) {
        this((int) (Math.random() * Integer.MAX_VALUE), title);
    }

    public Pattern(int id, String title) {
        Id = id;
        mTitle = title;
    }

    public void destroy(Context context) {
        if (mFileName != null && mFileName.length() > 0) {
            BitmapHandler.removeFileOfName(context, mFileName);
        }
        if (mFileNameThumb != null && mFileNameThumb.length() > 0) {
            BitmapHandler.removeFileOfName(context, mFileNameThumb);
        }
        if (mFileNamePattern != null && mFileNamePattern.length() > 0) {
            BitmapHandler.removeFileOfName(context, mFileNamePattern);
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
        mWeight = pref.getInt("" + prefCounter + PREF_WEIGHT, (int) mWeight);
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
        editor.putInt("" + prefCounter + PREF_WEIGHT, (int) mWeight);
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

    public long getWeight() {
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

    public Edit edit(ContentResolver resolver) {
        return new Edit(this);
    }

    public static class Empty extends Pattern {
        public Empty(int id) {
            super(id);
        }

        @Override
        public Edit edit(ContentResolver resolver) {
            return new EmptyEdit(this);
        }
    }

    public static class Edit {
        private final Map<String, Object> changes = new HashMap<>();
        private final Pattern pattern;

        public Edit(Pattern pattern) {
            this.pattern = pattern;
        }

        public Edit set(Pattern copyOf) {
            setTitle(copyOf.mTitle);
            setState(copyOf.mState.ordinal());
            setFile(copyOf.mFileName);
            setFileThumb(copyOf.mFileNameThumb);
            setFilePattern(copyOf.mFileNamePattern);
            setPixelWidth(copyOf.mPixelWidth);
            setPixelHeight(copyOf.mPixelHeight);
            setTime(copyOf.mWeight);
            return this;
        }

        public Set<String> getChangeKeys() {
            return changes.keySet();
        }

        private <T> void set(String key, T value, Comparable<T> originalValue) {
            if (originalValue.compareTo(value) == 0) {
                changes.remove(key);
            } else {
                changes.put(key, value);
            }
        }

        public Edit setTitle(String title) {
            set(PatternColumns.TITLE, title, pattern.mTitle);
            return this;
        }

        public Edit setState(int state) {
            set(PatternColumns.STATE, state, pattern.mState.ordinal());
            return this;
        }

        public Edit setFile(String file) {
            set(PatternColumns.FILE, file, pattern.mFileName);
            return this;
        }

        public Edit setFileThumb(String file) {
            set(PatternColumns.FILE_THUMB, file, pattern.mFileNameThumb);
            return this;
        }

        public Edit setFilePattern(String file) {
            set(PatternColumns.FILE_PATTERN, file, pattern.mFileNamePattern);
            return this;
        }

        public Edit setPixelWidth(int width) {
            set(PatternColumns.PIXEL_WIDTH, width, pattern.mPixelWidth);
            return this;
        }

        public Edit setPixelHeight(int height) {
            set(PatternColumns.PIXEL_HEIGHT, height, pattern.mPixelHeight);
            return this;
        }

        public Edit setTime(long time) {
            set(PatternColumns.TIME, time, (long) pattern.mWeight);
            return this;
        }

        public String getString(String key) {
            if (changes.containsKey(key)) {
                return (String) changes.get(key);
            } else {
                switch (key) {
                    case PatternColumns.TITLE:
                        return pattern.mTitle;
                    case PatternColumns.FILE:
                        return pattern.mFileName;
                    case PatternColumns.FILE_THUMB:
                        return pattern.mFileNameThumb;
                    case PatternColumns.FILE_PATTERN:
                        return pattern.mFileNamePattern;
                }
            }
            return null;
        }

        public long getLong(String key) {
            if (changes.containsKey(key)) {
                return (long) changes.get(key);
            } else {
                switch (key) {
                    case PatternColumns.TIME:
                        return pattern.mWeight;
                }
            }
            return 0;
        }

        public int getInt(String key) {
            if (changes.containsKey(key)) {
                return (int) changes.get(key);
            } else {
                switch (key) {
                    case PatternColumns._ID:
                        return pattern.Id;
                    case PatternColumns.PIXEL_WIDTH:
                        return pattern.mPixelWidth;
                    case PatternColumns.PIXEL_HEIGHT:
                        return pattern.mPixelHeight;
                }
            }
            return 0;
        }

        public Object getBlob(String key) {
            if (changes.containsKey(key)) {
                return (int) changes.get(key);
            } else {
                switch (key) {
                    case PatternColumns.COLORS:
                        return pattern.mColors;
                    case PatternColumns.PIXELS:
                        return pattern.mPixels;
                }
            }
            return null;
        }

        public void apply(ContentResolver resolver) {
            DatabaseManager.update(resolver, this);
        }
    }

    public static class EmptyEdit extends Edit {

        public EmptyEdit(Pattern pattern) {
            super(pattern);
        }

        @Override
        public void apply(ContentResolver resolver) {
            DatabaseManager.create(resolver, this);
        }
    }
}
