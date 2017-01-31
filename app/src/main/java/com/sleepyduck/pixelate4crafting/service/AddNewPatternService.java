package com.sleepyduck.pixelate4crafting.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.control.Constants;
import com.sleepyduck.pixelate4crafting.model.DatabaseManager;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.tasks.FindBestColorsTask;
import com.sleepyduck.pixelate4crafting.util.BetterLog;

import java.util.Map;

import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_IMAGE_STORED;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_STORING_IMAGE;

/**
 * Created by fredrikmetcalf on 25/01/17.
 */

public class AddNewPatternService extends IntentService {
    Handler handler;

    public AddNewPatternService() {
        super(AddNewPatternService.class.getSimpleName());
        HandlerThread handlerThread = new HandlerThread(AddNewPatternService.class.getSimpleName());
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        BetterLog.d(this, "New pattern %s", intent);

        final Uri imageUri = intent.getData();

        try {
            getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
        }

        if ("content".equals(imageUri.getScheme())) {
            final String fileName = BitmapHandler.getFileName(this, imageUri);
            final String title = Pattern.createTitleFromFileName(fileName);

            final int id = new Pattern.Empty(this)
                    .edit()
                    .setTitle(title)
                    .setTime(System.currentTimeMillis())
                    .setFlag(FLAG_STORING_IMAGE)
                    .apply(true);

            handler.post(new Runnable() {
                public void run() {
                    BitmapHandler.storeLocally(AddNewPatternService.this, imageUri, fileName, new BitmapHandler.OnFileStoredListener() {
                        @Override
                        public void onFileStored(String file, String thumbnail) {
                            DatabaseManager.getPattern(AddNewPatternService.this, id)
                                    .edit()
                                    .setFile(file)
                                    .setFileThumb(thumbnail)
                                    .setWidth(Constants.DEFAULT_WIDTH)
                                    .setFlag(FLAG_IMAGE_STORED)
                                    .apply(false);

                            new FindBestColorsTask() {
                                @Override
                                protected void onPostExecute(Map<Integer, Float> colors) {
                                    DatabaseManager.getPattern(AddNewPatternService.this, id)
                                            .edit()
                                            .setColors(colors)
                                            .apply(false);
                                }
                            }.execute(AddNewPatternService.this, file, Constants.DEFAULT_NUM_COLORS);
                        }
                    });
                }
            });
        }
    }
}
