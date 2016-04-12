package com.sleepyduck.pixelate4crafting;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sleepyduck.pixelate4crafting.control.CalculatePixelsTask;
import com.sleepyduck.pixelate4crafting.control.FindBestColorsTask;
import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.control.CountColorsTask;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.control.Constants;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.sleepyduck.pixelate4crafting.util.BetterLog;
import com.vi.swipenumberpicker.OnValueChangeListener;
import com.vi.swipenumberpicker.SwipeNumberPicker;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ConfigurationActivity extends Activity {
    private static final int REQUEST_IMAGE = 1;
    private static final Integer NUMBER_PICKER_WIDTH = 1;
    private static final Integer NUMBER_PICKER_COLOR_COUNT = 2;
    private Pattern mPattern;
    private OnDestroyListener destroyListener;

    private interface OnDestroyListener {
        void onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        setFinishOnTouchOutside(false);
        setupSwipeNumberPicker();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (destroyListener != null) {
            destroyListener.onDestroy();
        }
        if (mPattern != null) {
            mPattern.destroy(this);
        }
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
                //TODO Add unique string to file name
                BitmapHandler.storeLocally(this, imageUri, fileName);
                mPattern = new Pattern(Pattern.createTitleFromFileName(fileName));
                mPattern.setFileName(fileName);
                findViewById(R.id.dialog_set_number).setTag(NUMBER_PICKER_WIDTH);
                findViewById(R.id.dialog_set_number).setVisibility(VISIBLE);
                return;
            }
        }

        finish();
    }

    public void onChooseNumberClicked(View view) {
        final View dialog = findViewById(R.id.dialog_set_number);
        if (dialog.getTag() == NUMBER_PICKER_WIDTH) {
            SwipeNumberPicker picker = (SwipeNumberPicker) findViewById(R.id.number_picker);
            int val = picker.getValue();
            mPattern.setPixelWidth(val);

            TextView description = (TextView) findViewById(R.id.number_text_view);
            description.setText(R.string.change_color_count);
            picker.setValue(Constants.DEFAULT_MAX_COLORS, false);
            picker.setVisibility(VISIBLE);
            final EditText edit = (EditText) findViewById(R.id.number_edit_text);
            edit.setVisibility(GONE);
            dialog.setTag(NUMBER_PICKER_COLOR_COUNT);
        } else if (dialog.getTag() == NUMBER_PICKER_COLOR_COUNT) {
            SwipeNumberPicker picker = (SwipeNumberPicker) findViewById(R.id.number_picker);
            int colorCount = picker.getValue();

            findViewById(R.id.dialog_set_number).setVisibility(GONE);
            findViewById(R.id.dialog_analyzing_image).setVisibility(VISIBLE);
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_analyze);

            final FindBestColorsTask task = new FindBestColorsTask() {
                @Override
                protected void onProgressUpdate(Integer... values) {
                    progressBar.setProgress(values[0]);
                }

                @Override
                protected void onPostExecute(Integer integer) {
                    BetterLog.d(ConfigurationActivity.this, "Found " + integer + " colors in picture");
                    destroyListener = null;
                    findViewById(R.id.dialog_analyzing_image).setVisibility(GONE);
                    setupShowColors();
                }
            };
            task.execute(this, mPattern, colorCount);
            destroyListener = new OnDestroyListener() {
                @Override
                public void onDestroy() {
                    task.cancel(true);
                }
            };
        }
    }

    private void setupShowColors() {
        findViewById(R.id.dialog_colors_found).setVisibility(VISIBLE);

        int countColors = mPattern.getColors().size();

        TextView title = (TextView) findViewById(R.id.color_title);
        title.setText(getString(R.string.colors_found, countColors));

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

    public void onColorsDoneClicked(View view) {
        findViewById(R.id.dialog_colors_found).setVisibility(GONE);
        findViewById(R.id.dialog_analyzing_image).setVisibility(VISIBLE);
        final ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar_analyze);
        bar.setProgress(0);
        bar.setMax(200);
        TextView title = (TextView) findViewById(R.id.progress_title);
        title.setText(R.string.prepare_pattern);

        final CalculatePixelsTask task2 = new CalculatePixelsTask() {
            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                destroyListener = null;
                Patterns.Add(mPattern);
                Patterns.MakeLatest(mPattern);
                Patterns.Save(ConfigurationActivity.this);
                Intent intent = new Intent(ConfigurationActivity.this, PatternActivity.class);
                intent.putExtra(Patterns.INTENT_EXTRA_ID, mPattern.Id);
                startActivityForResult(intent, 0);
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
                task2.execute(ConfigurationActivity.this, mPattern);
                destroyListener = new OnDestroyListener() {
                    @Override
                    public void onDestroy() {
                        task2.cancel(true);
                    }
                };
            }
        };
        task.execute(this, mPattern);
        destroyListener = new OnDestroyListener() {
            @Override
            public void onDestroy() {
                task.cancel(true);
            }
        };
    }

    public void onColorsBackClicked(View view) {
        findViewById(R.id.dialog_colors_found).setVisibility(GONE);
        GridLayout grid = (GridLayout) findViewById(R.id.color_grid);
        grid.removeAllViews();
        findViewById(R.id.dialog_set_number).setVisibility(VISIBLE);
        findViewById(R.id.dialog_set_number).setTag(NUMBER_PICKER_COLOR_COUNT);
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
