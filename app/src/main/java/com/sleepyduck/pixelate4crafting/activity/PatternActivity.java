package com.sleepyduck.pixelate4crafting.activity;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sleepyduck.pixelate4crafting.BuildConfig;
import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns;
import com.sleepyduck.pixelate4crafting.model.DatabaseManager;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.sleepyduck.pixelate4crafting.tasks.PixelBitmapTask;
import com.sleepyduck.pixelate4crafting.util.BetterLog;
import com.sleepyduck.pixelate4crafting.util.CursorDiffUtilCallback;
import com.sleepyduck.pixelate4crafting.util.ListUpdateCallbackAdaptor;
import com.sleepyduck.pixelate4crafting.view.CircleColorView;
import com.sleepyduck.pixelate4crafting.view.InteractiveImageView;
import com.sleepyduck.pixelate4crafting.view.PatternImageView;

import java.util.Random;
import java.util.Set;

public class PatternActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private int mPatternId;
    private PatternImageView mCanvas;

    private InteractiveImageView.OnImageClickListener mImageClickListener = new InteractiveImageView.OnImageClickListener() {
        @Override
        public void onImageClicked(final Bitmap bitmap, final int x, final int y, float posX, float posY) {
            CircleColorView circleColorView = (CircleColorView) findViewById(R.id.circle_color_view);
            if (circleColorView.getVisibility() != View.VISIBLE) {
                if (x > 0 && y > 0 && x < bitmap.getWidth() && y < bitmap.getHeight()) {
                    final Pattern pattern = DatabaseManager.getPattern(PatternActivity.this, mPatternId);
                    Set<Integer> colorSet = pattern.getColors().keySet();
                    final int[] colors = new int[colorSet.size()];
                    int i = 0;
                    for (int color : colorSet) {
                        colors[i++] = color;
                    }
                    circleColorView.setColors(colors);
                    circleColorView.setRawPos(posX, posY);
                    circleColorView.show();

                    circleColorView.setOnColorClickListener(new CircleColorView.OnColorClickListener() {
                        @Override
                        public void onColorClicked(int colorIndex) {
                            BetterLog.d(this, "Color clicked: %d", colorIndex);
                            int patternX = x / PixelBitmapTask.PIXEL_SIZE - 1;
                            int patternY = y / PixelBitmapTask.PIXEL_SIZE - 1;
                            pattern.edit()
                                    .changePixelAt(patternX, patternY, colors[colorIndex])
                                    .apply();
                        }
                    });
                }
            } else {
                circleColorView.hide();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("");
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                toolbar.setNavigationOnClickListener(null);
            }
        });

        mPatternId = getIntent().getIntExtra(Patterns.INTENT_EXTRA_ID, 0);

        final Pattern pattern = DatabaseManager.getPattern(this, mPatternId);

        final EditText title = (EditText) toolbar.findViewById(R.id.editable_title);
        title.setVisibility(View.VISIBLE);
        title.setText(pattern.getTitle());
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                BetterLog.d(this, "Before: %s", s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String name = s.toString();
                if (name.contains("\n")) {
                    name = name.replaceAll("\n", "");
                    s.replace(0, s.length(), name);
                    hideSoftKeyboard(title);
                    title.clearFocus();
                    BetterLog.d(this, "Return typed");
                }
                pattern.edit().setTitle(name).apply();
            }
        });

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(title.getWindowToken(), 0);

        mCanvas = (PatternImageView) findViewById(R.id.canvas);
        mCanvas.setImageBitmap(BitmapHandler.getFromFileName(this, pattern.getPatternFileName()));
        mCanvas.setOnImageClickListener(mImageClickListener);

        getLoaderManager().initLoader(new Random().nextInt(), null, this);

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        if (BuildConfig.DEBUG) {
            adRequestBuilder.addTestDevice("C39E64851CA596B020F5A5C95550CBDA");
        }
        AdRequest adRequest = adRequestBuilder.build();
        adView.loadAd(adRequest);
    }

    /**
     * Hide keyboard while focus is moved
     */
    public void hideSoftKeyboard(View view) {
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                if (android.os.Build.VERSION.SDK_INT < 11) {
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                            0);
                } else {
                    if (getCurrentFocus() != null) {
                        inputManager.hideSoftInputFromWindow(this
                                        .getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    view.clearFocus();
                }
                view.clearFocus();
            }
        }
    }

    public void onEditClicked(View view) {
        Intent intent = new Intent(this, ChangeParametersActivity.class);
        intent.putExtra(Patterns.INTENT_EXTRA_ID, mPatternId);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String selection = String.format("%s=?", PatternColumns._ID);
        String[] selectionArgs = {Integer.toString(mPatternId)};
        return new CursorLoader(this, PatternColumns.URI, null, selection, selectionArgs, null);
    }

    private Cursor mCursor;

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
        DiffUtil.calculateDiff(new CursorDiffUtilCallback(mCursor, cursor)).dispatchUpdatesTo(new ListUpdateCallbackAdaptor() {
            @Override
            public void onChanged(int position, int count, Object payload) {
                if (payload != null && payload instanceof Bundle) {
                    Bundle bundle = (Bundle) payload;
                    if (bundle.containsKey(PatternColumns.FLAG)) {
                        if (bundle.getInt(PatternColumns.FLAG) == PatternColumns.FLAG_COMPLETE) {
                            Pattern pattern = new Pattern(PatternActivity.this, cursor);
                            mCanvas.setImageBitmap(BitmapHandler.getFromFileName(PatternActivity.this, pattern.getPatternFileName()));
                        } else {
                            // TODO Update some progress bar
                            mCanvas.setImageAlpha(0xff / 2);
                        }
                    }
                }
            }
        });
        mCursor = cursor;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
