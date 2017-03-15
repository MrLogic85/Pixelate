package com.sleepyduck.pixelate4crafting.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.control.DataManager;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns;
import com.sleepyduck.pixelate4crafting.util.BetterLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Pattern implements Comparable<Pattern> {
    public static final String INTENT_EXTRA_ID = "EXTRA_ID";

    public final int Id;
    protected final Context mContext;
    protected final String mTitle;
    protected final String mFileName;
    protected final String mFileNameThumb;
    protected final String mFileNamePattern;
    protected final int mState;
    protected final int mFlag;
    protected final int mPixelWidth;
    protected final int mPixelHeight;
    protected final int mProgress;
    protected final long mWeight;
    protected final int mMarkerX;
    protected final int mMarkerY;

    protected final String mPixelsPath;
    protected int[][] mPixels;
    protected final Map<Integer, Float> mColors = new HashMap<>();
    protected final Map<Integer, Map<Integer, Integer>> mChangedPixels = new HashMap<>();

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
        mFlag = cursor.getInt(cursor.getColumnIndex(PatternColumns.FLAG));
        mProgress = cursor.getInt(cursor.getColumnIndex(PatternColumns.PROGRESS));

        String marker = cursor.getString(cursor.getColumnIndex(PatternColumns.MARKER));
        if (marker != null && marker.length() > 0 && marker.contains(":")) {
            String[] markerXY = marker.split(":");
            mMarkerX = Integer.valueOf(markerXY[0]);
            mMarkerY = Integer.valueOf(markerXY[1]);
        } else {
            mMarkerX = 0;
            mMarkerY = 0;
        }

        String colors = cursor.getString(cursor.getColumnIndex(PatternColumns.COLORS));
        if (colors != null && !colors.isEmpty()) {
            try {
                JSONObject json = new JSONObject(colors);
                int size = json.getInt("size");
                for (int i = 0; i < size; ++i) {
                    mColors.put(json.getInt("color" + i), (float) json.getDouble("weight" + i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mPixelsPath = cursor.getString(cursor.getColumnIndex(PatternColumns.PIXELS));
        mPixels = new int[0][0];

        String changedPixels = cursor.getString(cursor.getColumnIndex(PatternColumns.CHANGED_PIXELS));
        if (changedPixels != null && changedPixels.length() > 0) {
            try {
                JSONObject json = new JSONObject(changedPixels);
                int count = json.getInt("count");
                for (int c = 0; c < count; ++c) {
                    int x = json.getInt("x" + c);
                    int y = json.getInt("y" + c);
                    int color = json.getInt("color" + c);
                    if (!mChangedPixels.containsKey(x)) {
                        mChangedPixels.put(x, new HashMap<Integer, Integer>());
                    }
                    mChangedPixels.get(x).put(y, color);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private Pattern(Context context) {
        mContext = context;
        Id = -1;
        mTitle = "";
        mFileName = "";
        mFileNameThumb = "";
        mFileNamePattern = "";
        mState = PatternColumns.STATE_ACTIVE;
        mFlag = PatternColumns.FLAG_STORING_IMAGE;
        mPixelWidth = 2;
        mPixelHeight = 2;
        mProgress = 0;
        mWeight = SystemClock.elapsedRealtime();
        mPixelsPath = "";
        mPixels = new int[0][0];
        mMarkerX = 0;
        mMarkerY = 0;
    }

    private Pattern(Pattern other) {
        mContext = other.mContext;
        Id = other.Id;
        mTitle = "" + other.mTitle;
        mFileName = "" + other.mFileName;
        mFileNameThumb = "" + other.mFileNameThumb;
        mFileNamePattern = "" + other.mFileNamePattern;
        mState = other.mState;
        mFlag = other.mFlag;
        mPixelWidth = other.mPixelWidth;
        mPixelHeight = other.mPixelHeight;
        mProgress = other.mProgress;
        mWeight = other.mWeight;
        mMarkerX = other.mMarkerX;
        mMarkerY = other.mMarkerY;

        for (Map.Entry<Integer, Float> entry : other.mColors.entrySet()) {
            mColors.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Integer, Map<Integer, Integer>> entryX : other.mChangedPixels.entrySet()) {
            Map<Integer, Integer> mapX = new HashMap<>();
            mChangedPixels.put(entryX.getKey(), mapX);
            for (Map.Entry<Integer, Integer> entryY : entryX.getValue().entrySet()) {
                mapX.put(entryY.getKey(), entryY.getValue());
            }
        }

        mPixelsPath = other.mPixelsPath;
        if (other.mPixels.length > 0) {
            mPixels = new int[other.mPixels.length][other.mPixels[0].length];
            for (int x = 0; x < other.mPixels.length; ++x) {
                mPixels[x] = Arrays.copyOf(other.mPixels[x], other.mPixels[x].length);
            }
        } else {
            mPixels = new int[0][0];
        }
    }

    private void destroy(Context context) {
        if (mFileName != null && mFileName.length() > 0) {
            BitmapHandler.removeFileOfName(context, mFileName);
        }
        if (mFileNameThumb != null && mFileNameThumb.length() > 0) {
            BitmapHandler.removeFileOfName(context, mFileNameThumb);
        }
        if (mFileNamePattern != null && mFileNamePattern.length() > 0) {
            BitmapHandler.removeFileOfName(context, mFileNamePattern);
        }
        DataManager.DestroyPixels(mContext, Id);
    }

    @Override
    public int compareTo(@NonNull Pattern other) {
        return mTitle.compareTo(other.mTitle);
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

    public long getTime() {
        return mWeight;
    }

    public int getPixelWidth() {
        return mPixelWidth;
    }

    public int getPixelHeight() {
        return mPixelHeight;
    }

    public int getColorCount() {
        synchronized (mColors) {
            return mColors.size();
        }
    }

    public int[] getColors(int[] outColors) {
        synchronized (mColors) {
            int i = 0;
            for (Integer color : mColors.keySet()) {
                outColors[i++] = color;
            }
        }
        return outColors;
    }

    public float getColorWeight(int color) {
        synchronized (mColors) {
            if (mColors.containsKey(color)) {
                return mColors.get(color);
            }
            return 0;
        }
    }

    public boolean hasColors() {
        synchronized (mColors) {
            return mColors.size() > 0;
        }
    }

    protected void loadPixels() {
        synchronized (mPixels) {
            if (mPixels.length == 0 && mPixelsPath.length() > 0) {
                File file = new File(mPixelsPath);
                if (!file.exists() && mFlag == PatternColumns.FLAG_COMPLETE) {
                    mPixels = new int[mPixelWidth][mPixelHeight];
                    edit().setFlag(PatternColumns.FLAG_SIZE_OR_COLOR_CHANGED).apply(false);
                } else {
                    JSONObject json = null;
                    int width = 0;
                    int height = 0;
                    try {
                        json = new JSONObject(DataManager.LoadPixels(mContext, Id));
                        width = json.getInt("width");
                        height = json.getInt("height");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mPixels = new int[width][height];
                    try {
                        for (int w = 0; w < width; ++w) {
                            for (int h = 0; h < height; ++h) {
                                mPixels[w][h] = json.getInt("" + w + "," + h);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public int[][] getPixels(int[][] outPixels) {
        synchronized (mPixels) {
            loadPixels();
            for (int x = 0; x < mPixels.length; ++x) {
                System.arraycopy(mPixels[x], 0, outPixels[x], 0, mPixels[x].length);
            }
        }
        return outPixels;
    }

    public int getPixel(int x, int y) {
        synchronized (mPixels) {
            loadPixels();
            return mPixels[x][y];
        }
    }

    public boolean hasChangedPixelAt(int x, int y) {
        synchronized (mChangedPixels) {
            return mChangedPixels.containsKey(x) && mChangedPixels.get(x).containsKey(y);
        }
    }

    public boolean hasChangedPixels() {
        synchronized (mChangedPixels) {
            if (mChangedPixels.size() == 0) {
                return false;
            }
            for (Map<Integer, Integer> changesY : mChangedPixels.values()) {
                if (changesY.size() > 0) {
                    return true;
                }
            }
            return false;
        }
    }

    public int getChangedPixelAt(int x, int y) {
        synchronized (mChangedPixels) {
            return mChangedPixels.containsKey(x) ? mChangedPixels.get(x).get(y) : 0;
        }
    }

    public int getChangedPixelsCount() {
        synchronized (mChangedPixels) {
            final int[] count = {0};
            foreachChangedPixel(new PixelCallback() {
                @Override
                public void execute(int x, int y, int color) {
                    count[0]++;
                }
            });
            return count[0];
        }
    }

    public void foreachChangedPixel(PixelCallback pixelCallback) {
        synchronized (mChangedPixels) {
            for (Map.Entry<Integer, Map<Integer, Integer>> entryX : mChangedPixels.entrySet()) {
                for (Map.Entry<Integer, Integer> entryY : entryX.getValue().entrySet()) {
                    pixelCallback.execute(entryX.getKey(), entryY.getKey(), entryY.getValue());
                }
            }
        }
    }

    public interface PixelCallback {
        void execute(int x, int y, int color);
    }

    public int getFlag() {
        return mFlag;
    }

    public String getPatternFileName() {
        return mFileNamePattern;
    }

    public int getMarkerX() {
        return mMarkerX;
    }

    public int getMarkerY() {
        return mMarkerY;
    }

    public int getProgress() {
        return mProgress;
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

    public void delete() {
        destroy(mContext);
        DatabaseManager.deletePattern(mContext.getContentResolver(), Id);
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

    public static class Edit extends Pattern {
        private final Map<String, Object> mChanges = new HashMap<>();

        Edit(Pattern pattern) {
            super(pattern);
        }

        Set<String> getChangeKeys() {
            return mChanges.keySet();
        }

        private <T> void set(String key, T value, Comparable<T> originalValue) {
            synchronized (mChanges) {
                if (originalValue == null || originalValue.compareTo(value) != 0) {
                    mChanges.put(key, value);
                } else {
                    mChanges.remove(key);
                }
            }
        }

        private boolean hasChange(String key) {
            synchronized (mChanges) {
                return mChanges.containsKey(key);
            }
        }

        public Edit setTitle(String title) {
            set(PatternColumns.TITLE, title, mTitle);
            return this;
        }

        public Edit setState(int state) {
            set(PatternColumns.STATE, state, mState);
            return this;
        }

        public Edit setFile(String file) {
            set(PatternColumns.FILE, file, mFileName);
            return this;
        }

        public Edit setFileThumb(String file) {
            set(PatternColumns.FILE_THUMB, file, mFileNameThumb);
            return this;
        }

        public Edit setFilePattern(String file) {
            set(PatternColumns.FILE_PATTERN, file, mFileNamePattern);
            setFlag(PatternColumns.FLAG_COMPLETE);
            return this;
        }

        public Edit setWidth(int width) {
            set(PatternColumns.WIDTH, width, mPixelWidth);
            if (hasChange(PatternColumns.WIDTH)) {
                setFlag(PatternColumns.FLAG_SIZE_OR_COLOR_CHANGED);
                if (hasChangedPixels()) {
                    set(PatternColumns.CHANGED_PIXELS, new HashMap<Integer, Map<Integer, Integer>>(), null);
                }
            }
            Rect rect = new Rect();
            BitmapHandler.getSize(mContext, getString(PatternColumns.FILE), rect);
            float pixelSize = (float) rect.width() / (float) width;
            int height = Math.round(rect.height() / pixelSize);
            set(PatternColumns.HEIGHT, height, mPixelHeight);
            return this;
        }

        public Edit setTime(long time) {
            set(PatternColumns.TIME, time, mWeight);
            return this;
        }

        public Edit setColors(Map<Integer, Float> colors) {
            set(PatternColumns.COLORS, colors, null);
            setFlag(PatternColumns.FLAG_SIZE_OR_COLOR_CHANGED);
            return this;
        }

        public Edit setPendingDelete(boolean delete) {
            set(PatternColumns.PENDING_DELETE, delete ? 1 : 0, -1);
            return this;
        }

        public Edit removeColor(int color) {
            synchronized (mColors) {
                @SuppressWarnings("unchecked")
                Map<Integer, Float> colors = (Map<Integer, Float>) get(PatternColumns.COLORS);
                if (colors.remove(color) != null) {
                    setFlag(PatternColumns.FLAG_SIZE_OR_COLOR_CHANGED);
                    mChanges.put(PatternColumns.COLORS, colors);
                }
            }
            return this;
        }

        public Edit addColor(int color) {
            synchronized (mColors) {
                @SuppressWarnings("unchecked")
                Map<Integer, Float> colors = (Map<Integer, Float>) get(PatternColumns.COLORS);
                colors.put(color, 0f);
                setFlag(PatternColumns.FLAG_SIZE_OR_COLOR_CHANGED);
                mChanges.put(PatternColumns.COLORS, colors);
            }
            return this;
        }

        public Edit changePixelAt(int x, int y, int color) {
            synchronized (mChangedPixels) {
                @SuppressWarnings("unchecked")
                Map<Integer, Map<Integer, Integer>> changePixels =
                        (Map<Integer, Map<Integer, Integer>>) get(PatternColumns.CHANGED_PIXELS);
                if (!changePixels.containsKey(x)) {
                    changePixels.put(x, new HashMap<Integer, Integer>());
                }
                Integer replaced = changePixels.get(x).put(y, color);
                if (replaced == null || replaced != color) {
                    setFlag(PatternColumns.FLAG_PIXELS_CALCULATED);
                    mChanges.put(PatternColumns.CHANGED_PIXELS, changePixels);
                }
            }
            return this;
        }

        public Edit eraseChangedPixelAt(int x, int y) {
            synchronized (mChangedPixels) {
                @SuppressWarnings("unchecked")
                Map<Integer, Map<Integer, Integer>> changePixels =
                        (Map<Integer, Map<Integer, Integer>>) get(PatternColumns.CHANGED_PIXELS);
                Integer removed = null;
                if (changePixels.containsKey(x)) {
                    removed = changePixels.get(x).remove(y);
                }
                if (removed != null) {
                    setFlag(PatternColumns.FLAG_PIXELS_CALCULATED);
                    mChanges.put(PatternColumns.CHANGED_PIXELS, changePixels);
                }
            }
            return this;
        }

        public Edit setPixels(int[][] pixels) {
            set(PatternColumns.PIXELS, pixels, null);
            setFlag(PatternColumns.FLAG_PIXELS_CALCULATED);
            return this;
        }

        public Edit setFlag(int flag) {
            set(PatternColumns.FLAG, flag, mFlag);
            return this;
        }

        public Edit setMarker(int x, int y) {
            set(PatternColumns.MARKER, "" +  x + ":" + y, "" +  mMarkerX + ":" + mMarkerY);
            return this;
        }

        public Edit setProgress(int progress) {
            set(PatternColumns.PROGRESS, progress, mProgress);
            return this;
        }

        String getString(String key) {
            if (hasChange(key)) {
                return (String) mChanges.get(key);
            } else {
                switch (key) {
                    case PatternColumns.TITLE:
                        return mTitle;
                    case PatternColumns.FILE:
                        return mFileName;
                    case PatternColumns.FILE_THUMB:
                        return mFileNameThumb;
                    case PatternColumns.FILE_PATTERN:
                        return mFileNamePattern;
                    case PatternColumns.MARKER:
                        return "" + mMarkerX + ":" + mMarkerY;
                }
            }
            return null;
        }

        long getLong(String key) {
            if (hasChange(key)) {
                return (long) mChanges.get(key);
            } else {
                switch (key) {
                    case PatternColumns.TIME:
                        return mWeight;
                }
            }
            return 0;
        }

        int getInt(String key) {
            if (hasChange(key)) {
                return (int) mChanges.get(key);
            } else {
                switch (key) {
                    case PatternColumns._ID:
                        return Id;
                    case PatternColumns.WIDTH:
                        return mPixelWidth;
                    case PatternColumns.HEIGHT:
                        return mPixelHeight;
                    case PatternColumns.FLAG:
                        return mFlag;
                    case PatternColumns.PENDING_DELETE:
                        return 0;
                }
            }
            return 0;
        }

        public Object get(String key) {
            if (hasChange(key)) {
                return mChanges.get(key);
            } else {
                switch (key) {
                    case PatternColumns.COLORS:
                        return mColors;
                    case PatternColumns.PIXELS:
                        loadPixels();
                        return mPixels;
                    case PatternColumns.CHANGED_PIXELS:
                        return mChangedPixels;
                }
            }
            return null;
        }

        public int apply(boolean createIfEmpty) {
            return DatabaseManager.update(mContext, this);
        }
    }

    static class EmptyEdit extends Edit {

        EmptyEdit(Pattern pattern) {
            super(pattern);
        }

        @Override
        public int apply(boolean createIfEmpty) {
            if (createIfEmpty) {
                return DatabaseManager.create(mContext.getContentResolver(), this);
            }
            return 0;
        }
    }
}
