package com.sleepyduck.pixelate4crafting.control.configuration;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.Constants;
import com.sleepyduck.pixelate4crafting.model.DatabaseManager;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.vi.swipenumberpicker.OnValueChangeListener;
import com.vi.swipenumberpicker.SwipeNumberPicker;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by fredrik.metcalf on 2016-04-13.
 */
public class ConfigurationWidthActivity extends Activity {
    public static final String EXTRA_WIDTH = "width";
    //private Pattern mPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_width);
        setFinishOnTouchOutside(false);
        setupSwipeNumberPicker();
    }

    public void onChooseNumberClicked(View view) {
        SwipeNumberPicker picker = (SwipeNumberPicker) findViewById(R.id.number_picker);
        int val = picker.getValue();

        DatabaseManager.getPattern(this,
                getIntent().getIntExtra(Patterns.INTENT_EXTRA_ID, 0))
                .edit()
                .setWidth(val)
                .apply();
        setResult(RESULT_OK, getIntent());
        finish();
    }

    private void setupSwipeNumberPicker() {
        int width = getIntent().getIntExtra(EXTRA_WIDTH, Constants.DEFAULT_WIDTH);
        SwipeNumberPicker picker = (SwipeNumberPicker) findViewById(R.id.number_picker);
        picker.setValue(width, false);
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
                        if (edit.getText().length() > 0) {
                            int value = Integer.parseInt(edit.getText().toString());
                            if (value > Constants.MAX_PIXELS) {
                                edit.setText("" + Constants.MAX_PIXELS);
                                value = Constants.MAX_PIXELS;
                            } else if (value < 1) {
                                edit.setText("" + 1);
                                value = 1;
                            }
                            picker.setValue(value, false);
                        } else {
                            picker.setValue(0, false);
                        }
                    }
                });
            }
        });
    }
}
