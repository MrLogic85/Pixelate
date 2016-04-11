package com.sleepyduck.pixelate4crafting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;

public class ChooseImageActivity extends Activity {
    private static final int REQUEST_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_image);
    }

    public void onOkClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            finish();
            return;
        }

        if (requestCode == REQUEST_IMAGE) {
            Uri imageUri = data.getData();
            try {
                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
            } catch (Exception e) {
            }
            if ("content".equals(imageUri.getScheme())) {
                String fileName = BitmapHandler.getFileName(this, imageUri);
                BitmapHandler.storeLocally(this, imageUri, fileName);
                Pattern pattern = new Pattern(Pattern.createTitleFromFileName(fileName));
                pattern.setFileName(fileName);
                Patterns.Add(pattern);
                Patterns.MakeLatest(pattern);
                Patterns.Save(this);

                Intent intent = new Intent(this, PatternActivity.class);
                intent.putExtra(Patterns.INTENT_EXTRA_ID, pattern.Id);
                startActivityForResult(intent, 0);
                return;
            }
        }

        finish();
    }
}
