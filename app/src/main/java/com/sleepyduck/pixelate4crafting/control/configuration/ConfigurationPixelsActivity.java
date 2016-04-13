package com.sleepyduck.pixelate4crafting.control.configuration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ProgressBar;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.CalculatePixelsTask;
import com.sleepyduck.pixelate4crafting.control.Constants;
import com.sleepyduck.pixelate4crafting.control.CountColorsTask;
import com.sleepyduck.pixelate4crafting.control.FindBestColorsTask;
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
public class ConfigurationPixelsActivity extends Activity {

    private Pattern mPattern;
    private OnDestroyListener mDestroyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_pixels);
        setFinishOnTouchOutside(false);

        mPattern = Patterns.GetPattern(getIntent().getIntExtra(Patterns.INTENT_EXTRA_ID, 0));
        preparePixels();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDestroyListener != null) {
            mDestroyListener.onDestroy();
            mDestroyListener = null;
        }
    }

    public void preparePixels() {
        final ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar_preparing_pattern);

        final CalculatePixelsTask task2 = new CalculatePixelsTask() {
            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                bar.setProgress(100 + values[0]);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mDestroyListener = null;
                Patterns.Save(ConfigurationPixelsActivity.this);
                Intent result = new Intent();
                result.putExtra(Patterns.INTENT_EXTRA_ID, mPattern.Id);
                setResult(RESULT_OK, result);
                finish();
            }
        };

        final CountColorsTask task = new CountColorsTask() {
            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                bar.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mDestroyListener = null;
                task2.execute(ConfigurationPixelsActivity.this, mPattern);
                mDestroyListener = new OnDestroyListener() {
                    @Override
                    public void onDestroy() {
                        task2.cancel(true);
                    }
                };
            }
        };
        task.execute(this, mPattern);
        mDestroyListener = new OnDestroyListener() {
            @Override
            public void onDestroy() {
                task.cancel(true);
            }
        };
    }

    private interface OnDestroyListener {
        void onDestroy();
    }
}
