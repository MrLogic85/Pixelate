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
import android.widget.TextView;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.Constants;
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
public class ConfigurationColorsActivity extends Activity {

    private Pattern mPattern;
    private OnDestroyListener mDestroyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_colors);
        setFinishOnTouchOutside(false);
        setupSwipeNumberPicker();

        mPattern = Patterns.GetPattern(getIntent().getIntExtra(Patterns.INTENT_EXTRA_ID, 0));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDestroyListener != null) {
            mDestroyListener.onDestroy();
            mDestroyListener = null;
        }
    }

    /**
     * Step one, choose the ammount of colors to use
     * @param view
     */
    public void onChooseColorCountClicked(View view) {
        SwipeNumberPicker picker = (SwipeNumberPicker) findViewById(R.id.number_picker);
        int colorCount = picker.getValue();

        findViewById(R.id.swipe_number_picker).setVisibility(GONE);
        findViewById(R.id.dialog_analyzing_image).setVisibility(VISIBLE);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_analyze);

        final FindBestColorsTask task = new FindBestColorsTask() {
            @Override
            protected void onProgressUpdate(Integer... values) {
                progressBar.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                BetterLog.d(this, "Found " + integer + " colors in picture");
                mDestroyListener = null;
                findViewById(R.id.dialog_analyzing_image).setVisibility(GONE);
                setupShowColors();
            }
        };
        task.execute(this, mPattern, colorCount);
        mDestroyListener = new OnDestroyListener() {
            @Override
            public void onDestroy() {
                task.cancel(true);
            }
        };
    }

    /**
     * Step two, show the colors found in image
     */
    private void setupShowColors() {
        findViewById(R.id.dialog_colors_found).setVisibility(VISIBLE);

        int countColors = mPattern.getColors().size();

        TextView text = (TextView) findViewById(R.id.color_title);
        text.setText(getString(R.string.colors_found, countColors));


        GridLayout grid = (GridLayout) findViewById(R.id.color_grid);
        int rowCount = countColors / grid.getColumnCount()
                + (countColors % grid.getColumnCount() > 0 ? 1 : 0);
        grid.setRowCount(rowCount);

        int x = 0, y = 0;
        for (int color : mPattern.getColors().keySet()) {
            View view = new View(this);
            view.setBackgroundColor(color);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = (int) getResources().getDimension(R.dimen.color_square_size);
            params.height = (int) getResources().getDimension(R.dimen.color_square_size);
            params.columnSpec = GridLayout.spec(x);
            params.rowSpec = GridLayout.spec(y);
            grid.addView(view, params);

            x++;
            if (x == grid.getColumnCount()) {
                y++;
                x = 0;
            }
        }
    }

    /**
     * Step three, we now have the colors we want. Prepare pattern.
     * @param view
     */
    public void onColorsDoneClicked(View view) {
        Patterns.Save(ConfigurationColorsActivity.this);
        Intent result = new Intent();
        result.putExtra(Patterns.INTENT_EXTRA_ID, mPattern.Id);
        setResult(RESULT_OK, result);
        finish();
    }

    /**
     * Back pressed, go bak to choose ammount of colors
     * @param view
     */
    public void onColorsBackClicked(View view) {
        findViewById(R.id.dialog_colors_found).setVisibility(GONE);
        GridLayout grid = (GridLayout) findViewById(R.id.color_grid);
        grid.removeAllViews();
        findViewById(R.id.swipe_number_picker).setVisibility(VISIBLE);
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

    private interface OnDestroyListener {
        void onDestroy();
    }
}
