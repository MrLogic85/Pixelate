package com.sleepyduck.pixelate4crafting.control.configuration;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;

/**
 * Created by fredrik.metcalf on 2016-04-13.
 */
public class ConfigurationImageActivity extends Activity {
    private static final int REQUEST_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_image);
        setFinishOnTouchOutside(false);
    }

    public void onChooseImageClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE) {
            final Uri imageUri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception e) {
            }
            if ("content".equals(imageUri.getScheme())) {
                String fileName = BitmapHandler.getFileName(ConfigurationImageActivity.this, imageUri);
                Pattern pattern = new Pattern(Pattern.createTitleFromFileName(fileName));
                fileName += String.format("%8x", pattern.Id);
                BetterLog.d(this, "File name created: " + fileName);
                BitmapHandler.storeLocally(ConfigurationImageActivity.this, imageUri, fileName);
                pattern.setFileName(fileName);
                Patterns.Add(pattern);
                Patterns.Save(ConfigurationImageActivity.this);

                Intent result = new Intent();
                result.putExtra(Patterns.INTENT_EXTRA_ID, pattern.Id);
                setResult(RESULT_OK, result);
                finish();
            }
        }
        setResult(RESULT_CANCELED);
        finish();
    }
}
