package com.sleepyduck.pixelate4crafting.service;

import android.app.Service;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.tasks.CalculatePixelsTask;
import com.sleepyduck.pixelate4crafting.tasks.CountColorsTask;
import com.sleepyduck.pixelate4crafting.tasks.PixelBitmapTask;
import com.sleepyduck.pixelate4crafting.util.BetterLog;
import com.sleepyduck.pixelate4crafting.util.CursorDiffUtilCallback;

import java.util.HashMap;
import java.util.Map;

import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_COLORS_CALCULATED;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_COLORS_CALCULATING;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_PATTERN_DRAWING;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_PIXELS_CALCULATED;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_PIXELS_CALCULATING;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_SIZE_OR_COLOR_CHANGED;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_SIZE_OR_COLOR_CHANGING;

public class CalculateService extends Service implements Loader.OnLoadCompleteListener<Cursor> {

    private CursorLoader loader;
    Handler handler;

    final Map<Integer, AsyncTask> runningTasks = new HashMap<>();
    private Cursor mCursor;

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
        loader.unregisterListener(this);
        loader.cancelLoad();
        loader.stopLoading();
        super.onDestroy();
    }

    synchronized private void stop() {
        handler.getLooper().quit();
        stopSelf();
    }

    private void processPattern(final Pattern pattern) {
        BetterLog.d(this, "Processing %d", pattern.Id);

        cancelTask(pattern.Id);

        switch (pattern.getFlag()) {
            case FLAG_SIZE_OR_COLOR_CHANGED: {
                BetterLog.d(this, "Start counting colors: %d", pattern.Id);
                pattern.edit()
                        .setProgress(0)
                        .setFlag(FLAG_COLORS_CALCULATING)
                        .apply(false);
                final CountColorsTask colorsTask = new CountColorsTask() {
                    @Override
                    protected void onProgressUpdate(Integer... values) {
                        pattern.edit()
                                .setProgress(values[0] / 2)
                                .apply(false);
                    }

                    @Override
                    protected void onPostExecute(Map<Integer, Float> colors) {
                        BetterLog.d(this, "CountColors finished: %d", pattern.Id);
                        removeTask(pattern.Id);
                        if (colors != null) {
                            pattern.edit()
                                    .setColors(colors)
                                    .setFlag(FLAG_COLORS_CALCULATED)
                                    .apply(false);
                        }
                    }

                    @Override
                    protected void onCancelled() {
                        BetterLog.d(this, "CountColors cancelled: %d", pattern.Id);
                        removeTask(pattern.Id);
                    }
                };
                addTask(pattern.Id, colorsTask);
                synchronized (this) {
                    try {
                        wait(50);
                    } catch (InterruptedException ignored) {
                    }
                }
                colorsTask.execute(this, pattern);
            } break;
            case FLAG_COLORS_CALCULATED: {
                BetterLog.d(this, "Start calculating pixels: %d", pattern.Id);
                pattern.edit()
                        .setFlag(FLAG_PIXELS_CALCULATING)
                        .apply(false);
                CalculatePixelsTask pixelsTask = new CalculatePixelsTask() {
                    @Override
                    protected void onProgressUpdate(Integer... values) {
                        pattern.edit()
                                .setProgress(values[0] / 2 + 50)
                                .apply(false);
                    }

                    @Override
                    protected void onPostExecute(int[][] pixels) {
                        BetterLog.d(this, "CalcPixels finished: %d", pattern.Id);
                        removeTask(pattern.Id);
                        if (pixels != null) {
                            pattern.edit()
                                    .setPixels(pixels)
                                    .apply(false);
                        }
                    }

                    @Override
                    protected void onCancelled() {
                        BetterLog.d(this, "CalcPixels cancelled: %d", pattern.Id);
                        removeTask(pattern.Id);
                    }
                };
                addTask(pattern.Id, pixelsTask);
                pixelsTask.execute(this, pattern);
            } break;
            case FLAG_PIXELS_CALCULATED: {
                BetterLog.d(this, "Start storing PixelBitmap: %d", pattern.Id);
                pattern.edit()
                        .setFlag(FLAG_PATTERN_DRAWING)
                        .apply(false);
                PixelBitmapTask bitmapTask = new PixelBitmapTask() {
                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        BetterLog.d(this, "PixelBitmap finished: %d", pattern.Id);
                        removeTask(pattern.Id);
                        if (bitmap != null) {
                            String patternName = BitmapHandler.storePattern(CalculateService.this, bitmap, pattern.getFileName());
                            pattern.edit()
                                    .setFilePattern(patternName)
                                    .apply(false);
                        }
                    }

                    @Override
                    protected void onCancelled() {
                        BetterLog.d(this, "PixelBitmap cancelled: %d", pattern.Id);
                        removeTask(pattern.Id);
                    }
                };
                addTask(pattern.Id, bitmapTask);
                bitmapTask.execute(this, pattern);
            }
        }
    }

    private void cancelTask(int id) {
        synchronized (runningTasks) {
            if (runningTasks.containsKey(id)) {
                BetterLog.d(this, "Cancelling task: %d", id);
                runningTasks.get(id).cancel(false);
            }
        }
    }

    private void addTask(int id, AsyncTask asyncTask) {
        synchronized (runningTasks) {
            while(runningTasks.containsKey(id)) {
                BetterLog.d(this, "Waiting for task to finnish: %d", id);
                try {
                    runningTasks.wait(1000);
                } catch (InterruptedException ignored) {
                }
            }
            runningTasks.put(id, asyncTask);
        }
    }

    private void removeTask(int id) {
        BetterLog.d(this, "Processing finished: %d", id);
        synchronized (runningTasks) {
            runningTasks.remove(id);
            runningTasks.notifyAll();
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
                    BetterLog.d(this, "Inserted %s", pattern.getTitle());
                    handlePattern(pattern);
                }
            }

            @Override
            public void onRemoved(int position, int count) {
                for (int i = position; i < position + count; ++i){
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
                for (int i = position; i < position + count; ++i){
                    Bundle bundle = (Bundle) payload;
                    if (bundle != null && bundle.containsKey(DatabaseContract.PatternColumns.FLAG)) {
                        cursor.moveToPosition(-1);
                        int idColumn = mCursor.getColumnIndex(DatabaseContract.PatternColumns._ID);
                        int id = mCursor.getInt(idColumn);
                        while (cursor.moveToNext() && cursor.getInt(idColumn) != id);
                        if (cursor.getInt(idColumn) == id) {
                            handlePattern(new Pattern(CalculateService.this, cursor));
                        }
                    }
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
            BetterLog.d(this, "Loading %s, %d", pattern.getTitle(), pattern.Id);
            Message message = handler.obtainMessage();
            message.obj = pattern;
            handler.sendMessage(message);
        }
    }

    // ========== BIND ==========

    @Override
    public IBinder onBind(Intent intent) {
        startService(new Intent(this, CalculateService.class));
        return new Binder();
    }

    @Override
    synchronized public boolean onUnbind(Intent intent) {
        if (handler.hasMessages(0)) {
            stop();
        }
        return super.onUnbind(intent);
    }

    private class Binder extends android.os.Binder {
        CalculateService service = CalculateService.this;
    }
}
