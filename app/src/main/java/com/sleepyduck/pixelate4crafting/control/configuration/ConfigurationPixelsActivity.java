package com.sleepyduck.pixelate4crafting.control.configuration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.tasks.CalculatePixelsTask;
import com.sleepyduck.pixelate4crafting.control.tasks.CountColorsTask;
import com.sleepyduck.pixelate4crafting.model.DatabaseManager;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;

import java.util.Map;

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

        mPattern = DatabaseManager.getPattern(this,
                getIntent().getIntExtra(Patterns.INTENT_EXTRA_ID, 0));
        mPattern.edit()
                .setPixels(null)
                .apply();
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
            protected void onPostExecute(int[][] aVoid) {
                super.onPostExecute(aVoid);
                mDestroyListener = null;
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
            protected void onPostExecute(Map<Integer, Float> aVoid) {
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
