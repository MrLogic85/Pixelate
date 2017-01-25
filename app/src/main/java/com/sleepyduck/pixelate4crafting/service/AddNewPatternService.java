package com.sleepyduck.pixelate4crafting.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.control.configuration.ConfigurationImageActivity;
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;
import com.sleepyduck.pixelate4crafting.model.DatabaseManager;
import com.sleepyduck.pixelate4crafting.model.Pattern;

/**
 * Created by fredrikmetcalf on 25/01/17.
 */

public class AddNewPatternService extends IntentService {

    public AddNewPatternService() {
        super(AddNewPatternService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        BetterLog.d(this, "New pattern %s", intent);

        final Uri imageUri = intent.getData();
        /*try {
            getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception e) {
            Log.e(ConfigurationImageActivity.class.getSimpleName(), "Failed to take persistable permission", e);
        }*/
        if ("content".equals(imageUri.getScheme())) {
            String fileName = BitmapHandler.getFileName(this, imageUri);
            final String title = Pattern.createTitleFromFileName(fileName);

            final int id = new Pattern.Empty(this)
                    .edit()
                    .setTitle(title)
                    .setTime(System.currentTimeMillis())
                    .apply();

            new Thread() {
                @Override
                public void run() {
                    BitmapHandler.storeLocally(AddNewPatternService.this, imageUri, title, new BitmapHandler.OnFileStoredListener() {
                        @Override
                        public void onFileStored(String file, String thumbnail) {
                            DatabaseManager.getPattern(AddNewPatternService.this, id)
                                    .edit()
                                    .setFile(file)
                                    .setFileThumb(thumbnail)
                                    .apply();
                        }
                    });
                }
            }.start();
        }
    }
}
