package com.sleepyduck.pixelate4crafting.service;

import android.app.Service;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract;
import com.sleepyduck.pixelate4crafting.model.DatabaseManager;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.tasks.CalculatePixelsTask;
import com.sleepyduck.pixelate4crafting.tasks.CancellableProcess;
import com.sleepyduck.pixelate4crafting.tasks.CountColorsTask;
import com.sleepyduck.pixelate4crafting.tasks.PixelBitmapTask;
import com.sleepyduck.pixelate4crafting.util.BetterLog;
import com.sleepyduck.pixelate4crafting.util.CursorDiffUtilCallback;
import com.sleepyduck.pixelate4crafting.util.DebugToast;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_COLORS_CALCULATED;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_COLORS_CALCULATING;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_IMAGE_STORED;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_PATTERN_DRAWING;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_PIXELS_CALCULATED;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_PIXELS_CALCULATING;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_SIZE_OR_COLOR_CHANGED;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_SIZE_OR_COLOR_CHANGING;

public class CalculateService extends Service implements Loader.OnLoadCompleteListener<Cursor> {

    private CursorLoader loader;
    Handler handler;

    private final List<Integer> queue = new LinkedList<>();
    //private final SortedMap<Integer, Pattern> queue = new TreeMap<>();
    private final Map<Integer, CancellableProcess<?, ?, ?>> currentProcess = new HashMap<>();
    private final Object mutex = new Object();
    private Cursor mCursor;
    private boolean isDestroyed = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DebugToast.makeText(this, "Starting " + CalculateService.class.getSimpleName());
        loader = new CursorLoader(this, DatabaseContract.PatternColumns.URI, null, null, null, null);
        loader.registerListener(0, this);
        loader.startLoading();

        final HandlerThread handlerThread = new HandlerThread(CalculateService.class.getSimpleName());
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    int patternId;
                    synchronized (mutex) {
                        while (queue.isEmpty()) {
                            if (isDestroyed) {
                                BetterLog.d(this, "CalculateService is destroyed");
                                return;
                            }
                            BetterLog.d(this, "Waiting for a pattern to process");
                            try {
                                mutex.wait();
                            } catch (InterruptedException ignored) {
                            }
                        }
                        patternId = queue.remove(0);
                    }
                    processPattern(patternId);
                }
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        DebugToast.makeText(this, "Destroying " + CalculateService.class.getSimpleName());
        if (handler != null && handler.getLooper() != null) {
            handler.getLooper().quit();
        }
        isDestroyed = true;
        synchronized (mutex) {
            mutex.notifyAll();
        }

        if (loader != null) {
            loader.unregisterListener(this);
            loader.cancelLoad();
            loader.stopLoading();
        }
        super.onDestroy();
    }

    public void stop() {
        DebugToast.makeText(this, "Stopping " + CalculateService.class.getSimpleName());
        stopSelf();
    }

    private void processPattern(final int patternId) {
        final Pattern pattern = DatabaseManager.getPattern(this, patternId);
        BetterLog.d(this, "Processing %d", pattern.Id);

        switch (pattern.getFlag()) {
            case FLAG_COLORS_CALCULATING:
            case FLAG_SIZE_OR_COLOR_CHANGED: {
                BetterLog.d(this, "Start counting colors: %d", pattern.Id);
                pattern.edit().setProgress(0).setFlag(FLAG_COLORS_CALCULATING).apply(false);
                final CountColorsTask colorsTask = new CountColorsTask() {
                    @Override
                    public void onPublishProgress(Integer progress) {
                        pattern.edit().setProgress(progress / 2).apply(false);
                    }
                };
                synchronized (mutex) {
                    currentProcess.put(pattern.Id, colorsTask);
                }
                Map<Integer, Float> colors = colorsTask.execute(this, pattern);
                synchronized (mutex) {
                    currentProcess.remove(pattern.Id);
                }

                if (colorsTask.isCancelled()) {
                    BetterLog.d(this, "CountColors cancelled: %d", pattern.Id);
                    return;
                }

                BetterLog.d(this, "CountColors finished: %d", pattern.Id);
                if (colors != null) {
                    final Pattern endPattern = DatabaseManager.getPattern(this, patternId);
                    if (endPattern.getFlag() == FLAG_COLORS_CALCULATING) {
                        endPattern.edit().setColors(colors).setFlag(FLAG_COLORS_CALCULATED).apply(false);
                        handlePattern(DatabaseManager.getPattern(this, patternId));
                    }
                }
            }
            break;
            case FLAG_PIXELS_CALCULATING:
            case FLAG_COLORS_CALCULATED: {
                BetterLog.d(this, "Start calculating pixels: %d", pattern.Id);
                pattern.edit().setFlag(FLAG_PIXELS_CALCULATING).apply(false);
                CalculatePixelsTask pixelsTask = new CalculatePixelsTask() {
                    @Override
                    public void onPublishProgress(Integer progress) {
                        pattern.edit().setProgress(progress / 2 + 50).apply(false);
                    }
                };
                synchronized (mutex) {
                    currentProcess.put(pattern.Id, pixelsTask);
                }
                int[][] pixels = pixelsTask.execute(this, pattern);
                synchronized (mutex) {
                    currentProcess.remove(pattern.Id);
                }

                if (pixelsTask.isCancelled()) {
                    BetterLog.d(this, "CalcPixels cancelled: %d", pattern.Id);
                    return;
                }

                BetterLog.d(this, "CalcPixels finished: %d", pattern.Id);
                if (pixels != null) {
                    final Pattern endPattern = DatabaseManager.getPattern(this, patternId);
                    if (endPattern.getFlag() == FLAG_PIXELS_CALCULATING) {
                        endPattern.edit().setPixels(pixels).apply(true);
                        handlePattern(DatabaseManager.getPattern(this, patternId));
                    }
                }
            }
            break;
            case FLAG_PATTERN_DRAWING:
            case FLAG_PIXELS_CALCULATED: {
                BetterLog.d(this, "Start storing PixelBitmap: %d", pattern.Id);
                pattern.edit().setFlag(FLAG_PATTERN_DRAWING).apply(false);
                PixelBitmapTask bitmapTask = new PixelBitmapTask() {
                    @Override
                    public void onPublishProgress(Object progress) {
                    }
                };
                synchronized (mutex) {
                    currentProcess.put(pattern.Id, bitmapTask);
                }
                Bitmap bitmap = bitmapTask.execute(pattern);
                synchronized (mutex) {
                    currentProcess.remove(pattern.Id);
                }

                if (bitmapTask.isCancelled()) {
                    BetterLog.d(this, "PixelBitmap cancelled: %d", pattern.Id);
                    return;
                }

                BetterLog.d(this, "PixelBitmap finished: %d", pattern.Id);
                if (bitmap != null) {
                    final Pattern endPattern = DatabaseManager.getPattern(this, patternId);
                    if (endPattern.getFlag() == FLAG_PATTERN_DRAWING) {
                        String patternName = BitmapHandler.storePattern(CalculateService.this, bitmap, endPattern.getFileName());
                        endPattern.edit().setFilePattern(patternName).apply(false);
                        handlePattern(DatabaseManager.getPattern(this, patternId));
                    }
                }
            }
            break;
        }
    }

    private void cancelTask(int id) {
        synchronized (mutex) {
            CancellableProcess<?, ?, ?> cancellableProcess = currentProcess.remove(id);
            if (cancellableProcess != null) {
                BetterLog.d(this, "Cancelling task: %d", id);
                cancellableProcess.cancel();
            }
        }
    }

    @Override
    synchronized public void onLoadComplete(Loader<Cursor> loader, final Cursor cursor) {
        DiffUtil.calculateDiff(new CursorDiffUtilCallback(mCursor, cursor)).dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                for (int i = position; i < position + count; ++i) {
                    cursor.moveToPosition(i);
                    Pattern pattern = new Pattern(CalculateService.this, cursor);
                    BetterLog.d(this, "Inserted \"%s\", ", pattern.getTitle(), pattern.Id);
                    if (pattern.getFlag() == FLAG_IMAGE_STORED
                            || pattern.getFlag() == FLAG_COLORS_CALCULATING
                            || pattern.getFlag() == FLAG_PIXELS_CALCULATING
                            || pattern.getFlag() == FLAG_SIZE_OR_COLOR_CHANGING) {
                        pattern.edit()
                                .setFlag(FLAG_SIZE_OR_COLOR_CHANGED)
                                .apply(false);
                    } else {
                        handlePattern(pattern);
                    }
                }
            }

            @Override
            public void onRemoved(int position, int count) {
                for (int i = position; i < position + count; ++i) {
                    mCursor.moveToPosition(i);
                    Pattern pattern = new Pattern(CalculateService.this, mCursor);
                    BetterLog.d(this, "Removed %s", pattern.getTitle());
                    cancelTask(pattern.Id);
                }
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                // Ignore
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                for (int i = position; i < position + count; ++i) {
                    cursor.moveToPosition(position);
                    handlePattern(new Pattern(CalculateService.this, cursor));
                }
            }
        });
        mCursor = cursor;
    }

    private void handlePattern(Pattern pattern) {
        if (pattern.getFlag() == FLAG_SIZE_OR_COLOR_CHANGING) {
            cancelTask(pattern.Id);
        } else if (pattern.getFlag() == FLAG_SIZE_OR_COLOR_CHANGED
                || pattern.getFlag() == FLAG_COLORS_CALCULATED
                || pattern.getFlag() == FLAG_PIXELS_CALCULATED) {
            synchronized (mutex) {
                if (!currentProcess.containsKey(pattern.Id)) {
                    BetterLog.d(this, "Handling \"%s\", %d", pattern.getTitle(), pattern.Id);
                    queue.add(pattern.Id);
                    Collections.sort(queue);
                    mutex.notifyAll();
                }
            }
        }
    }

    // ========== BIND ==========

    @Override
    public IBinder onBind(Intent intent) {
        DebugToast.makeText(this, "Binding " + CalculateService.class.getSimpleName());
        startService(new Intent(this, CalculateService.class));
        return new Binder();
    }

    public class Binder extends android.os.Binder {
        public CalculateService getService() {
            return CalculateService.this;
        }
    }
}
