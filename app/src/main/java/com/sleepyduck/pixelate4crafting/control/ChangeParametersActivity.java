package com.sleepyduck.pixelate4crafting.control;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.configuration.ConfigurationWidthActivity;
import com.sleepyduck.pixelate4crafting.control.tasks.FindBestColorsTask;
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.sleepyduck.pixelate4crafting.view.InteractiveImageView;
import com.sleepyduck.pixelate4crafting.view.PatternImageView;

import java.util.Arrays;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.sleepyduck.pixelate4crafting.view.PatternImageView.Style.Simple;

/**
 * Created by fredrikmetcalf on 20/04/16.
 */
public class ChangeParametersActivity extends AppCompatActivity {
    private static final int DEFAULT_INITIAL_COLORS = 5;

    private static final int STATE_FOCUSED_OFF = 0;
    private static final int STATE_FOCUSED_PALETTE = 1;

    private static final int REQUEST_CHANGE_WIDTH = 1;

    private Pattern mPattern;
    private InteractiveImageView mOriginalImage;
    private PatternImageView mPatternApproxImage;
    private FindBestColorsTask mFindBestColorsTask;
    private GridView mPaletteGrid;
    private GridAdapter mGridAdapter;
    private int mState = STATE_FOCUSED_OFF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_paramerters);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        ViewGroup group = (ViewGroup) findViewById(R.id.view_group);
        LayoutTransition layoutTransition = group.getLayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);

        mPattern = Patterns.GetPattern(getIntent().getIntExtra(Patterns.INTENT_EXTRA_ID, 0));
        mOriginalImage = (InteractiveImageView) findViewById(R.id.image_original);
        mPatternApproxImage = (PatternImageView) findViewById(R.id.image_approximated);

        mOriginalImage.setImageBitmap(BitmapHandler.getFromFileName(this, mPattern.getFileName()));
        mPatternApproxImage.setPattern(mPattern, Simple);

        mOriginalImage.setOnImageClickListener(new InteractiveImageView.OnImageClickListener() {
            @Override
            public void onImageClicked(int pixel) {
                BetterLog.d(this, "On image clicked %08x", pixel);
                mPattern.addColor(pixel);
                Patterns.Save(ChangeParametersActivity.this);
                mPatternApproxImage.setPattern(mPattern, Simple);
                mGridAdapter.updateColors(mPattern);
                mGridAdapter.notifyDataSetChanged();
            }
        });

        mPaletteGrid = (GridView) findViewById(R.id.palette_grid);
        mGridAdapter = new GridAdapter(mPattern);
        mPaletteGrid.setAdapter(mGridAdapter);
        mPaletteGrid.setOnItemClickListener(mItemClickListener);

        checkForZeroColors();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.configure_menu, menu);
        return true;
    }

    private void checkForZeroColors() {
        if (mPattern.getColors() == null) {
            mFindBestColorsTask = new FindBestColorsTask() {
                @Override
                protected void onPostExecute(Integer integer) {
                    Patterns.Save(ChangeParametersActivity.this);
                    mPatternApproxImage.setPattern(mPattern, Simple);
                    mGridAdapter.updateColors(mPattern);
                    mGridAdapter.notifyDataSetChanged();
                }
            };
            mFindBestColorsTask.execute(this, mPattern, DEFAULT_INITIAL_COLORS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFindBestColorsTask != null) {
            mFindBestColorsTask.cancel(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (mState != STATE_FOCUSED_OFF) {
            setState(STATE_FOCUSED_OFF);
        } else {
            Intent intent = new Intent();
            intent.putExtra(Patterns.INTENT_EXTRA_ID, mPattern.Id);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        BetterLog.d(this, "On option clicked");
        if (menuItem.getItemId() == android.R.id.home) {
            Intent intent = new Intent();
            intent.putExtra(Patterns.INTENT_EXTRA_ID, mPattern.Id);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        } else if (menuItem.getItemId() == R.id.menu_item_change_width) {
            Intent intent = new Intent(this, ConfigurationWidthActivity.class);
            intent.putExtra(Patterns.INTENT_EXTRA_ID, mPattern.Id);
            startActivityForResult(intent, REQUEST_CHANGE_WIDTH);
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mState != STATE_FOCUSED_PALETTE) {
                setState(STATE_FOCUSED_PALETTE);
            } else {
                removeColor((int) mGridAdapter.getItem(position));
            }
        }
    };

    private void removeColor(int color) {
        mPattern.removeColor(color);
        Patterns.Save(this);
        mGridAdapter.updateColors(mPattern);
        mGridAdapter.notifyDataSetChanged();
        mPatternApproxImage.executeRedraw(Simple);
    }

    public void onPaletteTextClicked(View view) {
        if (mGridAdapter.getState() == GridAdapter.STATE_SMALL) {
            setState(STATE_FOCUSED_PALETTE);
        } else {
            setState(STATE_FOCUSED_OFF);
        }
    }

    private void setState(int state) {
        mState = state;
        switch (state) {
            case STATE_FOCUSED_OFF: {
                int size = (int) getResources().getDimension(R.dimen.palette_2x_size_small);
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mPaletteGrid.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, size));
                } else {
                    mPaletteGrid.setLayoutParams(new LinearLayout.LayoutParams(size, MATCH_PARENT));
                }
                mPaletteGrid.setColumnWidth((int) getResources().getDimension(R.dimen.color_square_size_small));
                mGridAdapter.setState(GridAdapter.STATE_SMALL);
                break;
            }
            case STATE_FOCUSED_PALETTE: {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mPaletteGrid.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, 0, 2));
                } else {
                    int size = (int) getResources().getDimension(R.dimen.palette_2x_size);
                    mPaletteGrid.setLayoutParams(new LinearLayout.LayoutParams(size, MATCH_PARENT));
                }
                mPaletteGrid.setColumnWidth((int) getResources().getDimension(R.dimen.color_square_size));
                mGridAdapter.setState(GridAdapter.STATE_LARGE);
                mGridAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CHANGE_WIDTH) {
                mPatternApproxImage.setPattern(mPattern, Simple);
                mPatternApproxImage.scaleToFit();
                Patterns.Save(this);
            }
        }
    }

    private static class GridAdapter extends BaseAdapter {
        public static final int STATE_SMALL = 1;
        public static final int STATE_LARGE = 2;

        private int[] mColors;
        private int mState = STATE_SMALL;

        public GridAdapter(Pattern pattern) {
            updateColors(pattern);
        }

        public void updateColors(Pattern pattern) {
            if (pattern.getColors() != null) {
                mColors = new int[pattern.getColors().size()];
                int count = 0;
                for (int color : pattern.getColors().keySet()) {
                    mColors[count++] = color;
                }
                Arrays.sort(mColors);
            } else {
                mColors = new int[0];
            }
        }

        @Override
        public int getCount() {
            return mColors.length;
        }

        @Override
        public Object getItem(int position) {
            return mColors[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FrameLayout layout = new FrameLayout(parent.getContext());
            CardView card = new CardView(parent.getContext());
            layout.addView(card);
            //TODO Do not create a new view each time!

            // Set color
            card.setBackgroundColor(mColors[position]);

            // Layout frame
            int size;
            if (mState == STATE_LARGE) {
                size = (int) parent.getContext().getResources().getDimension(R.dimen.color_square_size);
            } else {
                size = (int) parent.getContext().getResources().getDimension(R.dimen.color_square_size_small);
            }
            layout.setLayoutParams(new FrameLayout.LayoutParams(size, size));

            // Layout card
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    MATCH_PARENT,
                    MATCH_PARENT);
            int margin = (int) card.getCardElevation();
            params.setMargins(margin, margin, margin * 2, margin * 2);
            card.setLayoutParams(params);

            return layout;
        }

        public void setState(int state) {
            mState = state;
        }

        public int getState() {
            return mState;
        }
    }
}
