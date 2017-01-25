package com.sleepyduck.pixelate4crafting.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.control.Constants;
import com.sleepyduck.pixelate4crafting.control.DataManager;
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Pattern implements Comparable<Pattern> {
    private static final String PREF_ID = "ID";
    private static final String PREF_TITLE = "TITLE";
    private static final String PREF_STATE = "STATE";
    private static final String PREF_FILE = "FILE";
    private static final String PREF_FILE_THUMB = "FILE_THUMB";
    private static final String PREF_FILE_PATTERN = "FILE_PATTERN";
    private static final String PREF_PIXEL_WIDTH = "WIDTH";
    private static final String PREF_PIXEL_HEIGHT = "HEIGHT";
    private static final String PREF_WEIGHT = "WEIGHT";
    private static final String PREF_NEEDS_CALCULATION = "RECALC";

    public final int Id;
    private final Context mContext;
    private String mTitle = "";
    private int mState = PatternColumns.STATE_ACTIVE;
    private String mFileName = "";
    private String mFileNameThumb = "";
    private String mFileNamePattern = "";
    private boolean mNeedsRecalculation = true;
    private int mPixelWidth = Constants.DEFAULT_PIXELS;
    private int mPixelHeight = Constants.DEFAULT_PIXELS;
    private long mWeight = 0;
    private Map<Integer, Float> mColors;
    private int[][] mPixels;

    public Pattern(Context context, Cursor cursor) {
        mContext = context;
        Id = cursor.getInt(cursor.getColumnIndex(PatternColumns._ID));
        mTitle = cursor.getString(cursor.getColumnIndex(PatternColumns.TITLE));
        mState = cursor.getInt(cursor.getColumnIndex(PatternColumns.STATE));
        mFileName = cursor.getString(cursor.getColumnIndex(PatternColumns.FILE));
        mFileNameThumb = cursor.getString(cursor.getColumnIndex(PatternColumns.FILE_THUMB));
        mFileNamePattern = cursor.getString(cursor.getColumnIndex(PatternColumns.FILE_PATTERN));
        mPixelWidth = cursor.getInt(cursor.getColumnIndex(PatternColumns.WIDTH));
        mPixelHeight = cursor.getInt(cursor.getColumnIndex(PatternColumns.HEIGHT));
        mWeight = cursor.getLong(cursor.getColumnIndex(PatternColumns.TIME));
        mNeedsRecalculation = cursor.getInt(cursor.getColumnIndex(PatternColumns.NEEDS_RECALCULATION)) == 1;
        String colors = cursor.getString(cursor.getColumnIndex(PatternColumns.COLORS));
        if (colors == null || colors.isEmpty()) {
            mColors = null;
        } else {
            try {
                JSONObject json = new JSONObject(colors);
                int size = json.getInt("size");
                mColors = new HashMap<>(size);
                for (int i = 0; i < size; ++i) {
                    mColors.put(json.getInt("color" + i), (float) json.getDouble("weight" + i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String pixels = cursor.getString(cursor.getColumnIndex(PatternColumns.PIXELS));
        if (pixels == null || pixels.isEmpty()) {
            mPixels = null;
        } else {
            try {
                JSONObject json = new JSONObject(pixels);
                int width = json.getInt("width");
                int height = json.getInt("height");
                mPixels = new int[width][height];
                for (int w = 0; w < width; ++w) {
                    for (int h = 0; h  < height; ++h) {
                        mPixels[w][h] = json.getInt("" + w + "," + h);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private Pattern(Context context) {
        mContext = context;
        Id = -1;
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
        mContext = context;
        Id = pref.getInt("" + prefCounter + PREF_ID, -1);
        mTitle = pref.getString("" + prefCounter + PREF_TITLE, mTitle);
        mState = pref.getInt("" + prefCounter + PREF_STATE, PatternColumns.STATE_ACTIVE);
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

    @Override
    public int compareTo(Pattern another) {
        return this.mTitle.compareTo(another.mTitle);
    }

    public String getFileName() {
        return mFileName;
    }

    public int getState() {
        return mState;
    }

    public String getFileNameThumbnail() {
        return mFileNameThumb;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getPixelWidth() {
        return mPixelWidth;
    }

    public int getPixelHeight() {
        return mPixelHeight;
    }

    public Map<Integer, Float> getColors() {
        return mColors;
    }

    public boolean hasColors() {
        return getColors() != null && getColors().size() > 0;
    }

    public int[][] getPixels() {
        return mPixels;
    }

    public boolean needsRecalculation() {
        return mNeedsRecalculation;
    }

    public String getPatternFileName() {
        return mFileNamePattern;
    }

    public static String createTitleFromFileName(String fileName) {
        BetterLog.d(Pattern.class, fileName);
        String tmp = fileName.split("\\.")[0];
        BetterLog.d(Pattern.class, tmp);
        tmp = tmp.replaceAll("_", " ");
        BetterLog.d(Pattern.class, tmp);
        return tmp;
    }

    public Edit edit() {
        return new Edit(this);
    }

    public void delete(Context context) {
        destroy(context);
        DatabaseManager.deletePattern(context.getContentResolver(), Id);
    }

    public static class Empty extends Pattern {
        public Empty(Context context) {
            super(context);
        }

        @Override
        public Edit edit() {
            return new EmptyEdit(this);
        }
    }

    public static class Edit {
        private final Map<String, Object> mChanges = new HashMap<>();
        protected final Pattern mPattern;

        public Edit(Pattern pattern) {
            this.mPattern = pattern;
        }

        public Edit set(Pattern copyOf) {
            setTitle(copyOf.mTitle);
            setState(copyOf.mState);
            setFile(copyOf.mFileName);
            setFileThumb(copyOf.mFileNameThumb);
            setFilePattern(copyOf.mFileNamePattern);
            setWidth(copyOf.mPixelWidth);
            setTime(copyOf.mWeight);
            setColors(copyOf.mColors);
            setPixels(copyOf.mPixels);
            setNeedsRecalculation(copyOf.mNeedsRecalculation);
            return this;
        }

        public Set<String> getChangeKeys() {
            return mChanges.keySet();
        }

        private <T> void set(String key, T value, Comparable<T> originalValue) {
            if (originalValue == null || originalValue.compareTo(value) != 0) {
                mChanges.put(key, value);
            } else {
                mChanges.remove(key);
            }
        }

        public Edit setTitle(String title) {
            set(PatternColumns.TITLE, title, mPattern.mTitle);
            return this;
        }

        public Edit setState(int state) {
            set(PatternColumns.STATE, state, mPattern.mState);
            return this;
        }

        public Edit setFile(String file) {
            set(PatternColumns.FILE, file, mPattern.mFileName);
            return this;
        }

        public Edit setFileThumb(String file) {
            set(PatternColumns.FILE_THUMB, file, mPattern.mFileNameThumb);
            return this;
        }

        public Edit setFilePattern(String file) {
            set(PatternColumns.FILE_PATTERN, file, mPattern.mFileNamePattern);
            return this;
        }

        public Edit setWidth(int width) {
            set(PatternColumns.WIDTH, width, mPattern.mPixelWidth);

            setNeedsRecalculation(mChanges.containsKey(PatternColumns.WIDTH));
            Bitmap bitmap = BitmapHandler.getFromFileName(mPattern.mContext, getString(PatternColumns.FILE));
            float pixelSize = (float) bitmap.getWidth() / (float) width;
            int height = (int) (bitmap.getHeight() / pixelSize);
            set(PatternColumns.HEIGHT, height, mPattern.mPixelHeight);
            bitmap.recycle();
            return this;
        }

        public Edit setTime(long time) {
            set(PatternColumns.TIME, time, mPattern.mWeight);
            return this;
        }

        public Edit setColors(Map<Integer, Float> colors) {
            set(PatternColumns.COLORS, colors, null);
            setNeedsRecalculation(true);
            return this;
        }

        public Edit removeColor(int color) {
            @SuppressWarnings("unchecked")
            Map<Integer, Float> colors = (Map<Integer, Float>) get(PatternColumns.COLORS);
            if (colors.remove(color) != null) {
                setNeedsRecalculation(true);
            }
            return this;
        }

        public Edit addColor(int color) {
            @SuppressWarnings("unchecked")
            Map<Integer, Float> colors = (Map<Integer, Float>) get(PatternColumns.COLORS);
            colors.put(color, 0f);
            setNeedsRecalculation(true);
            return this;
        }

        public Edit setPixels(int[][] pixels) {
            setNeedsRecalculation(pixels == null);
            set(PatternColumns.PIXELS, pixels, null);
            return this;
        }

        public Edit setNeedsRecalculation(boolean recalc) {
            set(PatternColumns.NEEDS_RECALCULATION, recalc, mPattern.mNeedsRecalculation);
            if (recalc) {
                setFilePattern("");
            }
            return this;
        }

        public String getString(String key) {
            if (mChanges.containsKey(key)) {
                return (String) mChanges.get(key);
            } else {
                switch (key) {
                    case PatternColumns.TITLE:
                        return mPattern.mTitle;
                    case PatternColumns.FILE:
                        return mPattern.mFileName;
                    case PatternColumns.FILE_THUMB:
                        return mPattern.mFileNameThumb;
                    case PatternColumns.FILE_PATTERN:
                        return mPattern.mFileNamePattern;
                }
            }
            return null;
        }

        public long getLong(String key) {
            if (mChanges.containsKey(key)) {
                return (long) mChanges.get(key);
            } else {
                switch (key) {
                    case PatternColumns.TIME:
                        return mPattern.mWeight;
                }
            }
            return 0;
        }

        public int getInt(String key) {
            if (mChanges.containsKey(key)) {
                return (int) mChanges.get(key);
            } else {
                switch (key) {
                    case PatternColumns._ID:
                        return mPattern.Id;
                    case PatternColumns.WIDTH:
                        return mPattern.mPixelWidth;
                    case PatternColumns.HEIGHT:
                        return mPattern.mPixelHeight;
                }
            }
            return 0;
        }

        public boolean getBoolean(String key) {
            if (mChanges.containsKey(key)) {
                return (boolean) mChanges.get(key);
            } else {
                switch (key) {
                    case PatternColumns.NEEDS_RECALCULATION:
                        return mPattern.mNeedsRecalculation;
                }
            }
            return false;
        }

        public Object get(String key) {
            if (mChanges.containsKey(key)) {
                return mChanges.get(key);
            } else {
                switch (key) {
                    case PatternColumns.COLORS:
                        return mPattern.mColors;
                    case PatternColumns.PIXELS:
                        return mPattern.mPixels;
                }
            }
            return null;
        }

        public int apply() {
            return DatabaseManager.update(mPattern.mContext.getContentResolver(), this);
        }
    }

    public static class EmptyEdit extends Edit {

        public EmptyEdit(Pattern pattern) {
            super(pattern);
        }

        @Override
        public int apply() {
            return DatabaseManager.create(mPattern.mContext.getContentResolver(), this);
        }
    }
}
