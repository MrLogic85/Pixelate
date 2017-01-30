package com.sleepyduck.pixelate4crafting.activity;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.sleepyduck.pixelate4crafting.BuildConfig;
import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.configuration.ConfigurationNameActivity;
import com.sleepyduck.pixelate4crafting.configuration.ConfigurationPixelsActivity;
import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract;
import com.sleepyduck.pixelate4crafting.model.DatabaseManager;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.sleepyduck.pixelate4crafting.util.BetterLog;
import com.sleepyduck.pixelate4crafting.view.PatternImageView;

import java.util.Random;

import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_COMPLETE;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_PIXELS_CALCULATING;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_SIZE_OR_COLOR_CHANGED;

public class PatternActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int REQUEST_CHANGE_PARAMETERS = 1;
    private static final int REQUEST_NEW_PIXELS = 2;
    private static final int REQUEST_CHANGE_NAME = 3;
    private Pattern mPattern;
	private PatternImageView mCanvas;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pattern);

		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                toolbar.setNavigationOnClickListener(null);
            }
        });
        ab.setTitle("");

        mPattern = DatabaseManager.getPattern(this,
                getIntent().getIntExtra(Patterns.INTENT_EXTRA_ID, -1));

        //ab.setTitle(mPattern.getTitle());
        final EditText title = (EditText) toolbar.findViewById(R.id.editable_title);
        title.setVisibility(View.VISIBLE);
        title.setText(mPattern.getTitle());
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
                mPattern.edit().setTitle(name).apply();
            }
        });

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
		
		mCanvas = (PatternImageView) findViewById(R.id.canvas);
        mCanvas.setImageBitmap(BitmapHandler.getFromFileName(this, mPattern.getPatternFileName()));

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.pattern_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_change_parameters) {
            Intent intent = new Intent(this, ChangeParametersActivity.class);
            intent.putExtra(Patterns.INTENT_EXTRA_ID, mPattern.Id);
            startActivityForResult(intent, REQUEST_CHANGE_PARAMETERS);
            return true;
        }
		return super.onOptionsItemSelected(item);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHANGE_PARAMETERS) {
            if (!mPattern.hasColors()) {
                finish();
            } else if (mPattern.getFlag() == FLAG_SIZE_OR_COLOR_CHANGED) {
                Intent intent = new Intent(this, ConfigurationPixelsActivity.class);
                intent.putExtra(Patterns.INTENT_EXTRA_ID, mPattern.Id);
                startActivityForResult(intent, REQUEST_NEW_PIXELS);
            }
        } else if (requestCode == REQUEST_NEW_PIXELS) {
            if (mPattern.getFlag() == FLAG_PIXELS_CALCULATING) {
                finish();
            } else {
                mCanvas.setPattern(mPattern);
            }
        } else if (requestCode == REQUEST_CHANGE_NAME) {
            getSupportActionBar().setTitle(mPattern.getTitle());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String selection = String.format("%s=?", DatabaseContract.PatternColumns._ID);
        String[] selectionArgs = { Integer.toString(mPattern.Id) };
        return new CursorLoader(this, DatabaseContract.PatternColumns.URI, null, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        while (cursor.moveToNext()) {
            mPattern = new Pattern(this, cursor);
            switch (mPattern.getFlag()) {
                case FLAG_COMPLETE:
                    mCanvas.setImageBitmap(BitmapHandler.getFromFileName(this, mPattern.getPatternFileName()));
                    mCanvas.scaleToFit();
                    break;
                default:
                    // TODO Update some progress bar
                    mCanvas.setImageAlpha(0xff / 2);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
