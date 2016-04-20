package com.sleepyduck.pixelate4crafting.control.configuration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.control.Constants;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.vi.swipenumberpicker.OnValueChangeListener;
import com.vi.swipenumberpicker.SwipeNumberPicker;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by fredrik.metcalf on 2016-04-13.
 */
public class ConfigurationWidthActivity extends Activity {

    private Pattern mPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_width);
        setFinishOnTouchOutside(false);
        setupSwipeNumberPicker();

        mPattern = Patterns.GetPattern(getIntent().getIntExtra(Patterns.INTENT_EXTRA_ID, 0));
    }

    public void onChooseNumberClicked(View view) {
        SwipeNumberPicker picker = (SwipeNumberPicker) findViewById(R.id.number_picker);
        int val = picker.getValue();
        mPattern.setPixelWidth(val);

        Bitmap bitmap = BitmapHandler.getFromFileName(this, mPattern.getFileName());
        float pixelSize = (float) bitmap.getWidth() / (float) mPattern.getPixelWidth();
        int height = (int) (bitmap.getHeight() / pixelSize);
        mPattern.setPixelHeight(height);

        Patterns.Save(this);
        Intent result = new Intent();
        result.putExtra(Patterns.INTENT_EXTRA_ID, mPattern.Id);
        setResult(RESULT_OK, result);
        finish();
    }

    private void setupSwipeNumberPicker() {
        SwipeNumberPicker picker = (SwipeNumberPicker) findViewById(R.id.number_picker);
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
                final SwipeNumberPicker picker = (SwipeNumberPicker) findViewById(R.id.number_picker);
                picker.setVisibility(GONE);
                final EditText edit = (EditText) findViewById(R.id.number_edit_text);
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
                            value = Constants.MAX_PIXELS;
                        } else if (value < 1) {
                            edit.setText("" + 1);
                            value = 1;
                        }
                        picker.setValue(value, false);
                    }
                });
            }
        });
    }
}
