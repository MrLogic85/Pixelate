package com.sleepyduck.pixelate4crafting.activity;

import android.animation.ObjectAnimator;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.sleepyduck.pixelate4crafting.BuildConfig;
import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.configuration.ConfigurationWidthActivity;
import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.control.Constants;
import com.sleepyduck.pixelate4crafting.firebase.FirebaseLogger;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns;
import com.sleepyduck.pixelate4crafting.model.DatabaseManager;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.tasks.PixelBitmapTask;
import com.sleepyduck.pixelate4crafting.util.BetterLog;
import com.sleepyduck.pixelate4crafting.util.CursorDiffUtilCallback;
import com.sleepyduck.pixelate4crafting.util.ListUpdateCallbackAdaptor;
import com.sleepyduck.pixelate4crafting.view.CircleColorView;
import com.sleepyduck.pixelate4crafting.view.ColorEditList;
import com.sleepyduck.pixelate4crafting.view.InteractiveImageView;
import com.sleepyduck.pixelate4crafting.view.PatternImageView;

import java.util.Random;

public class PatternActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int REQUEST_CHANGE_WIDTH = 1;

    private int mPatternId;
    private PatternImageView mCanvas;
    private EditText mTitle;
    private int mLoaderId;
    private ColorEditList mColorEditListView;
    private FirebaseLogger mFirebaseLogger;

    private int mMenuEditFlag = MENU_EDIT_DONE;
    private static final int MENU_EDIT_DONE = 0x00;
    private static final int MENU_EDIT_NAME = 0x01;
    private static final int MENU_EDIT_PIXELS = 0x02;

    Pattern.Edit editPattern;

    private InteractiveImageView.OnImageClickListener mImageClickListener = new InteractiveImageView.OnImageClickListener() {
        @Override
        public void onImageClicked(final Bitmap bitmap, final int x, final int y, float posX, float posY) {
            if (isEditMenuVisible
                    && x > PixelBitmapTask.PIXEL_SIZE
                    && y > PixelBitmapTask.PIXEL_SIZE
                    && x < bitmap.getWidth()
                    && y < bitmap.getWidth()) {
                Pattern pattern = DatabaseManager.getPattern(PatternActivity.this, mPatternId);
                int patternX = x / PixelBitmapTask.PIXEL_SIZE - 1;
                int patternY = y / PixelBitmapTask.PIXEL_SIZE - 1;
                mFirebaseLogger.pixelChanged();
                switch (mColorEditListView.getState()) {
                    case COLOR:
                        mCanvas.setPixel(patternX, patternY, mColorEditListView.getColor());
                        if (editPattern == null) {
                            editPattern = pattern.edit();
                        }
                        editPattern.changePixelAt(patternX, patternY, mColorEditListView.getColor());
                        break;
                    case ERASE:
                        int origColor = pattern.getPixel(patternX, patternY);
                        mCanvas.setPixel(patternX, patternY, origColor);
                        if (editPattern == null) {
                            editPattern = pattern.edit();
                        }
                        editPattern.eraseChangedPixelAt(patternX, patternY);
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);

        mFirebaseLogger = new FirebaseLogger(FirebaseAnalytics.getInstance(this));

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
            }
        });

        mPatternId = getIntent().getIntExtra(Pattern.INTENT_EXTRA_ID, 0);

        final Pattern pattern = DatabaseManager.getPattern(this, mPatternId);

        mTitle = (EditText) toolbar.findViewById(R.id.editable_title);
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText(pattern.getTitle());
        mTitle.addTextChangedListener(new TextWatcher() {
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
                    onDoneClicked(MENU_EDIT_NAME);
                    BetterLog.d(this, "Return typed");
                }
                pattern.edit().setTitle(name).apply(false);
            }
        });
        mTitle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!mTitle.hasFocus()) {
                    onEditNameClicked(v);
                }
                return false;
            }
        });

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mTitle.getWindowToken(), 0);

        mCanvas = (PatternImageView) findViewById(R.id.canvas);
        mCanvas.setImageBitmap(BitmapHandler.getFromFileName(this, pattern.getPatternFileName()));
        mCanvas.setOnImageClickListener(mImageClickListener);

        mColorEditListView = (ColorEditList) findViewById(R.id.color_edit_list_view);
        mColorEditListView.setOnColorEditListListener(new ColorEditList.OnColorEditListClickListener() {
            @Override
            public void onColorClicked(int color) {
                CircleColorView circleColorView = (CircleColorView) findViewById(R.id.circle_color_view);
                if (circleColorView.getVisibility() == View.VISIBLE) {
                    circleColorView.hide();
                }
            }

            @Override
            public void onEraseClicked() {
                CircleColorView circleColorView = (CircleColorView) findViewById(R.id.circle_color_view);
                if (circleColorView.getVisibility() == View.VISIBLE) {
                    circleColorView.hide();
                }
            }

            @Override
            public void onAddColorClicked() {
                onEditPixelsClicked(null);
            }
        });

        mLoaderId = new Random().nextInt();
        getLoaderManager().initLoader(mLoaderId, null, this);

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        if (BuildConfig.DEBUG) {
            adRequestBuilder.addTestDevice("C39E64851CA596B020F5A5C95550CBDA");
        }
        AdRequest adRequest = adRequestBuilder.build();
        adView.loadAd(adRequest);
    }

    @Override
    protected void onDestroy() {
        getLoaderManager().destroyLoader(mLoaderId);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mMenuEditFlag == MENU_EDIT_DONE) {
            super.onBackPressed();
        } else {
            onDoneClicked(mMenuEditFlag);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CHANGE_WIDTH) {
                int newWidth = data.getIntExtra(ConfigurationWidthActivity.EXTRA_WIDTH
                        , Constants.DEFAULT_WIDTH);
                Pattern pattern = DatabaseManager.getPattern(this, mPatternId);
                mFirebaseLogger.sizeChanged(newWidth, pattern.getPixelWidth());
                pattern.edit()
                        .setWidth(newWidth)
                        .apply(false);
            }
        }
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

    private void addMenuEditFlag(int flag) {
        mMenuEditFlag |= flag;
    }

    private void removeMenuEditFlag(int flag) {
        mMenuEditFlag &= ~flag;
    }

    private void setMenuEditDone() {
        mMenuEditFlag = MENU_EDIT_DONE;
    }

    private void onDoneClicked(int flag) {
        if ((flag & MENU_EDIT_NAME) != 0) {
            mFirebaseLogger.nameChanged();
            hideSoftKeyboard(mTitle);
            removeMenuEditFlag(MENU_EDIT_NAME);
        }

        if ((flag & MENU_EDIT_PIXELS) != 0) {
            if (editPattern != null) {
                editPattern.apply(false);
                editPattern = null;
            }
            hideEditPixelsMenu();
            CircleColorView circleColorView = (CircleColorView) findViewById(R.id.circle_color_view);
            if (circleColorView.getVisibility() == View.VISIBLE) {
                circleColorView.hide();
            }
            removeMenuEditFlag(MENU_EDIT_PIXELS);
        }

        if (mMenuEditFlag == MENU_EDIT_DONE) {
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setImageResource(R.drawable.ic_editor_mode_edit);
            setMenuEditDone();
        }
    }

    public void onOpenEditMenuClicked(View view) {
        if (mMenuEditFlag != MENU_EDIT_DONE) {
            onDoneClicked(mMenuEditFlag);
        } else {
            if (isMenuVisible) {
                hideMenu();
            } else {
                showMenu();
            }
        }
    }

    public void onEditNameClicked(View view) {
        addMenuEditFlag(MENU_EDIT_NAME);
        mTitle.setSelection(mTitle.getText().toString().length());
        mTitle.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mTitle, 0);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_action_done);
        hideMenu();
    }

    public void onEditColorsClicked(View view) {
        Intent intent = new Intent(this, ChangeParametersActivity.class);
        intent.putExtra(Pattern.INTENT_EXTRA_ID, mPatternId);
        startActivity(intent);
        hideMenu();
    }

    public void onEditPixelsClicked(View view) {
        addMenuEditFlag(MENU_EDIT_PIXELS);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_action_done);
        hideMenu();

        CircleColorView circleColorView = (CircleColorView) findViewById(R.id.circle_color_view);
        if (circleColorView.getVisibility() != View.VISIBLE) {
            final Pattern pattern = DatabaseManager.getPattern(PatternActivity.this, mPatternId);
            final int[] colors = new int[pattern.getColorCount()];
            pattern.getColors(colors);
            circleColorView.setColors(colors);
            if (isEditMenuVisible || mColorEditListView.getChildCount() <= 2) {
                circleColorView.show();
            }

            circleColorView.setOnColorClickListener(new CircleColorView.OnColorClickListener() {
                @Override
                public float[] onPreColorClicked(int colorIndex) {
                    return mColorEditListView.prepareAddColor(colors[colorIndex]);
                }

                @Override
                public void onColorClicked(int colorIndex) {
                    mColorEditListView.selectColor(colors[colorIndex]);
                }

                @Override
                public void onCancel() {
                }
            });
        }

        showEditPixelsMenu();
    }

    public void onEditWidthClicked(View view) {
        hideMenu();
        Pattern pattern = DatabaseManager.getPattern(this, mPatternId);
        if (pattern.hasChangedPixels()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.warning_remove_custom_pixels)
                    .setCancelable(true)
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editWidth();
                        }
                    }).show();
        } else {
            editWidth();
        }
    }

    private void editWidth() {
        Pattern pattern = DatabaseManager.getPattern(PatternActivity.this, mPatternId);
        Intent intent = new Intent(PatternActivity.this, ConfigurationWidthActivity.class);
        intent.putExtra(ConfigurationWidthActivity.EXTRA_WIDTH, pattern.getPixelWidth());
        startActivityForResult(intent, REQUEST_CHANGE_WIDTH);
    }

    ObjectAnimator menuAnimation;
    boolean isMenuVisible;

    private void animateMenu(boolean show) {
        final View menu = findViewById(R.id.edit_menu_card);

        final int menuHeight = menu.getHeight();
        if (menuAnimation == null) {
            menu.setTranslationY(-menuHeight);
            menu.setVisibility(View.VISIBLE);
        } else {
            menuAnimation.cancel();
        }

        isMenuVisible = show;

        final int to = show ? (int) -getResources().getDimension(R.dimen.padding_tiny) : -menuHeight;
        menuAnimation = ObjectAnimator.ofFloat(menu, View.TRANSLATION_Y, menu.getTranslationY(), to);
        menuAnimation.start();
    }

    private void showMenu() {
        animateMenu(true);
    }

    private void hideMenu() {
        animateMenu(false);
    }

    ObjectAnimator editMenuAnimation;
    boolean isEditMenuVisible;

    private void animateEditMenu(boolean show) {
        final View colorEdit = findViewById(R.id.color_edit_list_view_card);

        final int height = colorEdit.getHeight();
        if (editMenuAnimation == null) {
            colorEdit.setTranslationY(-height);
            colorEdit.setVisibility(View.VISIBLE);
        } else {
            editMenuAnimation.cancel();
        }

        isEditMenuVisible = show;

        final int to = show ? (int) -getResources().getDimension(R.dimen.padding_tiny) : -height;
        editMenuAnimation = ObjectAnimator.ofFloat(colorEdit, View.TRANSLATION_Y, colorEdit.getTranslationY(), to);
        editMenuAnimation.start();
    }

    private void showEditPixelsMenu() {
        animateEditMenu(true);
    }

    private void hideEditPixelsMenu() {
        animateEditMenu(false);
    }
}
