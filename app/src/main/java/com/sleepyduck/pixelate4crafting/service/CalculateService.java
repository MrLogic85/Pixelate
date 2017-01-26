package com.sleepyduck.pixelate4crafting.service;

import android.app.Service;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;

import com.sleepyduck.pixelate4crafting.control.tasks.CalculatePixelsTask;
import com.sleepyduck.pixelate4crafting.control.tasks.CountColorsTask;
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract;
import com.sleepyduck.pixelate4crafting.model.Pattern;

import java.util.Map;

import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_COLORS_CALCULATED;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_COLORS_CALCULATING;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_PIXELS_CALCULATED;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_PIXELS_CALCULATING;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_SIZE_OR_COLOR_CHANGED;

public class CalculateService extends Service implements Loader.OnLoadCompleteListener<Cursor> {

    private CursorLoader loader;
    Handler handler;

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
        BetterLog.d(CalculateService.class);
        switch (pattern.getFlag()) {
            case FLAG_SIZE_OR_COLOR_CHANGED: {
                pattern.edit()
                        .setFlag(FLAG_COLORS_CALCULATING)
                        .apply();
                new CountColorsTask() {
                    @Override
                    protected void onProgressUpdate(Integer... values) {
                        BetterLog.d(CalculateService.class, "Colors: " + values[0]);
                        pattern.edit()
                                .setProgress(values[0] / 2)
                                .apply();
                    }

                    @Override
                    protected void onPostExecute(Map<Integer, Float> colors) {
                        pattern.edit()
                                .setColors(colors)
                                .setFlag(FLAG_COLORS_CALCULATED)
                                .apply();
                    }
                }.execute(this, pattern);
            } break;
            case FLAG_COLORS_CALCULATED: {
                pattern.edit()
                        .setFlag(FLAG_PIXELS_CALCULATING)
                        .apply();
                new CalculatePixelsTask() {
                    @Override
                    protected void onProgressUpdate(Integer... values) {
                        BetterLog.d(CalculateService.class, "Pixels: " + values[0]);
                        pattern.edit()
                                .setProgress(values[0] / 2 + 50)
                                .apply();
                    }

                    @Override
                    protected void onPostExecute(int[][] pixels) {
                        pattern.edit()
                                .setPixels(pixels)
                                .apply();
                    }
                }.execute(this, pattern);
            } break;
        }
    }

    @Override
    synchronized public void onLoadComplete(Loader<Cursor> loader, Cursor cursor) {
        while (cursor.moveToNext()) {
            Pattern pattern = new Pattern(this, cursor);
            if (pattern.getFlag() == FLAG_SIZE_OR_COLOR_CHANGED
                    || pattern.getFlag() == FLAG_COLORS_CALCULATED) {
                BetterLog.d(this, "Loading %s", cursor);
                Message message = handler.obtainMessage();
                message.obj = pattern;
                handler.sendMessage(message);
            }
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

    public static CalculateService Get(IBinder binder) {
        if (binder instanceof Binder) {
            return ((Binder) binder).service;
        }
        return null;
    }

    private class Binder extends android.os.Binder {
        CalculateService service = CalculateService.this;
    }
}
