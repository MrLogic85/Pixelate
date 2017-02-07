package com.sleepyduck.pixelate4crafting.service;

import android.app.Service;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.widget.Toast;

import com.sleepyduck.pixelate4crafting.BuildConfig;
import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.tasks.CalculatePixelsTask;
import com.sleepyduck.pixelate4crafting.tasks.CancellableProcess;
import com.sleepyduck.pixelate4crafting.tasks.CountColorsTask;
import com.sleepyduck.pixelate4crafting.tasks.PixelBitmapTask;
import com.sleepyduck.pixelate4crafting.util.BetterLog;
import com.sleepyduck.pixelate4crafting.util.CursorDiffUtilCallback;
import com.sleepyduck.pixelate4crafting.util.DebugToast;

import java.util.HashMap;
import java.util.Map;

import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_COLORS_CALCULATED;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_COLORS_CALCULATING;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_COMPLETE;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_IMAGE_STORED;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_PATTERN_DRAWING;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_PIXELS_CALCULATED;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_PIXELS_CALCULATING;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_SIZE_OR_COLOR_CHANGED;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_SIZE_OR_COLOR_CHANGING;

public class CalculateService extends Service implements Loader.OnLoadCompleteListener<Cursor> {

    private CursorLoader loader;
    Handler handler;

    private final Map<Integer, CancellableProcess<?, ?, ?>> currentProcess = new HashMap<>();
    private Cursor mCursor;
    private boolean mIsBound;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        loader = new CursorLoader(this, DatabaseContract.PatternColumns.URI, null, null, null, null);
        loader.registerListener(0, this);
        loader.startLoading();

        HandlerThread handlerThread = new HandlerThread(CalculateService.class.getSimpleName());
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                processPattern((Pattern) msg.obj);
            }
        };
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        handler.getLooper().quit();

        loader.unregisterListener(this);
        loader.cancelLoad();
        loader.stopLoading();
        super.onDestroy();
    }

    synchronized private void stop() {
        DebugToast.makeText(this, "Stopping " + CalculateService.class.getSimpleName());
        stopSelf();
    }

    private void processPattern(final Pattern pattern) {
        BetterLog.d(this, "Processing %d", pattern.Id);

        switch (pattern.getFlag()) {
            case FLAG_SIZE_OR_COLOR_CHANGED: {
                BetterLog.d(this, "Start counting colors: %d", pattern.Id);
                pattern.edit()
                        .setProgress(0)
                        .setFlag(FLAG_COLORS_CALCULATING)
                        .apply(false);
                final CountColorsTask colorsTask = new CountColorsTask() {
                    @Override
                    public void onPublishProgress(Integer progress) {
                        pattern.edit()
                                .setProgress(progress / 2)
                                .apply(false);
                    }
                };
                synchronized (currentProcess) {
                    currentProcess.put(pattern.Id, colorsTask);
                }
                Map<Integer, Float> colors = colorsTask.execute(this, pattern);
                synchronized (currentProcess) {
                    currentProcess.remove(pattern.Id);
                }

                if (colorsTask.isCancelled()) {
                    BetterLog.d(this, "CountColors cancelled: %d", pattern.Id);
                    return;
                }

                BetterLog.d(this, "CountColors finished: %d", pattern.Id);
                if (colors != null) {
                    pattern.edit()
                            .setColors(colors)
                            .setFlag(FLAG_COLORS_CALCULATED)
                            .apply(false);
                }
            }
            break;
            case FLAG_COLORS_CALCULATED: {
                BetterLog.d(this, "Start calculating pixels: %d", pattern.Id);
                pattern.edit()
                        .setFlag(FLAG_PIXELS_CALCULATING)
                        .apply(false);
                CalculatePixelsTask pixelsTask = new CalculatePixelsTask() {
                    @Override
                    public void onPublishProgress(Integer progress) {
                        pattern.edit()
                                .setProgress(progress / 2 + 50)
                                .apply(false);
                    }
                };
                synchronized (currentProcess) {
                    currentProcess.put(pattern.Id, pixelsTask);
                }
                int[][] pixels = pixelsTask.execute(this, pattern);
                synchronized (currentProcess) {
                    currentProcess.remove(pattern.Id);
                }

                if (pixelsTask.isCancelled()) {
                    BetterLog.d(this, "CalcPixels cancelled: %d", pattern.Id);
                    return;
                }

                BetterLog.d(this, "CalcPixels finished: %d", pattern.Id);
                if (pixels != null) {
                    pattern.edit()
                            .setPixels(pixels)
                            .apply(false);
                }
            }
            break;
            case FLAG_PIXELS_CALCULATED: {
                BetterLog.d(this, "Start storing PixelBitmap: %d", pattern.Id);
                pattern.edit()
                        .setFlag(FLAG_PATTERN_DRAWING)
                        .apply(false);
                PixelBitmapTask bitmapTask = new PixelBitmapTask() {
                    @Override
                    public void onPublishProgress(Object progress) {
                    }
                };
                synchronized (currentProcess) {
                    currentProcess.put(pattern.Id, bitmapTask);
                }
                Bitmap bitmap = bitmapTask.execute(pattern);
                synchronized (currentProcess) {
                    currentProcess.remove(pattern.Id);
                }

                if (bitmapTask.isCancelled()) {
                    BetterLog.d(this, "PixelBitmap cancelled: %d", pattern.Id);
                    return;
                }

                BetterLog.d(this, "PixelBitmap finished: %d", pattern.Id);
                if (bitmap != null) {
                    String patternName = BitmapHandler.storePattern(CalculateService.this, bitmap, pattern.getFileName());
                    pattern.edit()
                            .setFilePattern(patternName)
                            .apply(false);
                }
            }
        }

        if (!mIsBound) {
            stop();
        }
    }

    private void cancelTask(int id) {
        synchronized (currentProcess) {
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
                            ||pattern.getFlag() == FLAG_COLORS_CALCULATING
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
            BetterLog.d(this, "Handling \"%s\", %d", pattern.getTitle(), pattern.Id);
            cancelTask(pattern.Id);
            Message message = handler.obtainMessage(pattern.Id);
            message.obj = pattern;
            handler.removeMessages(pattern.Id);
            handler.sendMessage(message);
        }
    }

    // ========== BIND ==========

    @Override
    public IBinder onBind(Intent intent) {
        startService(new Intent(this, CalculateService.class));
        mIsBound = true;
        return new Binder();
    }

    @Override
    synchronized public boolean onUnbind(Intent intent) {
        if (handler != null && handler.hasMessages(0)) {
            stop();
        }
        mIsBound = false;
        return super.onUnbind(intent);
    }

    private class Binder extends android.os.Binder {
        CalculateService service = CalculateService.this;
    }
}
