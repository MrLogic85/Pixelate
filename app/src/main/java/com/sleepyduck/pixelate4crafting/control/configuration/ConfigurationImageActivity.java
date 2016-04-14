package com.sleepyduck.pixelate4crafting.control.configuration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.control.Constants;
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.vi.swipenumberpicker.OnValueChangeListener;
import com.vi.swipenumberpicker.SwipeNumberPicker;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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
                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
            } catch (Exception e) {
            }
            if ("content".equals(imageUri.getScheme())) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
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
                        return null;
                    }
                }.execute();
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}
