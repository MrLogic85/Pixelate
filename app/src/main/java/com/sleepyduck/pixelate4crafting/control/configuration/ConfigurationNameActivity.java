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
public class ConfigurationNameActivity extends Activity {

    private Pattern mPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_name);

        mPattern = Patterns.GetPattern(getIntent().getIntExtra(Patterns.INTENT_EXTRA_ID, 0));
        EditText text = (EditText) findViewById(R.id.edit_text);
        text.setText(mPattern.getTitle());

        setFinishOnTouchOutside(false);
    }

    public void onDoneClicked(View view) {
        EditText text = (EditText) findViewById(R.id.edit_text);
        if (text.getText().length() > 0) {
            mPattern.setTitle(text.getText().toString());
            Patterns.Save(this);
        }

        Intent result = new Intent();
        result.putExtra(Patterns.INTENT_EXTRA_ID, mPattern.Id);
        setResult(RESULT_OK, result);
        finish();
    }
}
