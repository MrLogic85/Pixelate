package com.sleepyduck.pixelate4crafting;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.sleepyduck.pixelate4crafting.old.Constants;
import com.sleepyduck.pixelate4crafting.util.BetterLog;
import com.vi.swipenumberpicker.OnValueChangeListener;
import com.vi.swipenumberpicker.SwipeNumberPicker;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ConfigurationActivity extends Activity {
    private static final int REQUEST_IMAGE = 1;
    private Pattern mPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        SwipeNumberPicker picker = (SwipeNumberPicker) findViewById(R.id.width_number_picker);
        picker.setOnValueChangeListener(new OnValueChangeListener() {
            @Override
            public boolean onValueChange(SwipeNumberPicker view, int oldValue, int newValue) {
                return true;
            }
        });
        picker.setShowNumberPickerDialog(false);
        picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SwipeNumberPicker picker = (SwipeNumberPicker) findViewById(R.id.width_number_picker);
                picker.setVisibility(GONE);
                final EditText edit = (EditText) findViewById(R.id.width_number_edit_text);
                edit.setVisibility(VISIBLE);
                edit.setText("" + picker.getValue());
                edit.requestFocus();
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
                edit.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        int value = Integer.parseInt(edit.getText().toString());
                        if (value > Constants.MAX_PIXELS) {
                            edit.setText("" + Constants.MAX_PIXELS);
                        } else if (value < 1) {
                            edit.setText("" + 1);
                        }
                    }
                });
            }
        });
    }

    public void onChooseImageClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE);
        findViewById(R.id.dialog_select_image).setVisibility(GONE);
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
                mPattern = new Pattern(Pattern.createTitleFromFileName(fileName));
                mPattern.setFileName(fileName);
                findViewById(R.id.dialog_set_width).setVisibility(VISIBLE);
                return;
            }
        }

        finish();
    }

    public void onChooseWidthClicked(View view) {
        SwipeNumberPicker picker = (SwipeNumberPicker) findViewById(R.id.width_number_picker);
        int val = picker.getValue();
        mPattern.setPixelWidth(val);
        Patterns.Add(mPattern);
        Patterns.MakeLatest(mPattern);
        Patterns.Save(this);

        Intent intent = new Intent(this, PatternActivity.class);
        intent.putExtra(Patterns.INTENT_EXTRA_ID, mPattern.Id);
        startActivityForResult(intent, 0);
    }
}
