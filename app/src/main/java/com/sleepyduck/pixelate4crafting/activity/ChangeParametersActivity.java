package com.sleepyduck.pixelate4crafting.activity;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
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
import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.control.ChooseColorDialog;
import com.sleepyduck.pixelate4crafting.control.Constants;
import com.sleepyduck.pixelate4crafting.control.configuration.ConfigurationWidthActivity;
import com.sleepyduck.pixelate4crafting.control.tasks.FindBestColorsTask;
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;
import com.sleepyduck.pixelate4crafting.control.util.ColorUtil;
import com.sleepyduck.pixelate4crafting.control.util.MMCQ;
import com.sleepyduck.pixelate4crafting.control.util.history.AddColor;
import com.sleepyduck.pixelate4crafting.control.util.history.ChangeWidth;
import com.sleepyduck.pixelate4crafting.control.util.history.History;
import com.sleepyduck.pixelate4crafting.control.util.history.OnHistoryDo;
import com.sleepyduck.pixelate4crafting.control.util.history.RemoveColor;
import com.sleepyduck.pixelate4crafting.model.DatabaseManager;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.sleepyduck.pixelate4crafting.view.InteractiveImageView;
import com.sleepyduck.pixelate4crafting.view.PatternImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.sleepyduck.pixelate4crafting.view.PatternImageView.Style.Simple;

public class ChangeParametersActivity extends AppCompatActivity {
    private static final int DEFAULT_INITIAL_COLORS = 4;
    private static final int CHECK_SQUARE_SIZE = 5;
    private static final int CHECK_SQUARE_RADIUS = (CHECK_SQUARE_SIZE - 1) / 2;
    private static final int CHECK_SQUARE_COLORS = 9;

    private static final int STATE_FOCUSED_OFF = 0;
    private static final int STATE_FOCUSED_PALETTE = 1;

    private static final int REQUEST_CHANGE_WIDTH = 1;
    private static final int REQUEST_CHOOSE_COLOR = 2;

    private Pattern mPattern;
    private InteractiveImageView mOriginalImage;
    private PatternImageView mPatternApproxImage;
    private FindBestColorsTask mFindBestColorsTask;
    private GridView mPaletteGrid;
    private GridAdapter mGridAdapter;
    private int mState = STATE_FOCUSED_OFF;
    private Stack<History> mHistory = new Stack<>();
    private Stack<History> mUndoneHistory = new Stack<>();
    private Menu mOptionMenu;

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

        int patternId = getIntent().getIntExtra(Patterns.INTENT_EXTRA_ID, 0);
        mPattern = DatabaseManager.getPattern(this, patternId);
        mOriginalImage = (InteractiveImageView) findViewById(R.id.image_original);
        mPatternApproxImage = (PatternImageView) findViewById(R.id.image_approximated);

        mOriginalImage.setImageBitmap(BitmapHandler.getFromFileName(this, mPattern.getFileName()));
        mPatternApproxImage.setPattern(mPattern, Simple);

        mOriginalImage.setOnImageClickListener(mOnImageClickListener);

        mPaletteGrid = (GridView) findViewById(R.id.palette_grid);
        mGridAdapter = new GridAdapter(mPattern);
        mPaletteGrid.setAdapter(mGridAdapter);
        mPaletteGrid.setOnItemClickListener(mItemClickListener);

        checkForZeroColors();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionMenu = menu;
        getMenuInflater().inflate(R.menu.configure_menu, menu);
        menu.findItem(R.id.menu_item_redo).setEnabled(false);
        menu.findItem(R.id.menu_item_undo).setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home: {
                Intent intent = new Intent();
                intent.putExtra(Patterns.INTENT_EXTRA_ID, mPattern.Id);
                setResult(RESULT_OK, intent);
                finish();
                return true;
            }
            case R.id.menu_item_change_width: {
                Intent intent = new Intent(this, ConfigurationWidthActivity.class);
                intent.putExtra(Patterns.INTENT_EXTRA_ID, mPattern.Id);
                startActivityForResult(intent, REQUEST_CHANGE_WIDTH);
                return super.onOptionsItemSelected(menuItem);
            }
            case R.id.menu_item_undo: {
                undoHistory();
                return true;
            }
            case R.id.menu_item_redo: {
                redoHistory();
                return true;
            }
        }
        return false;
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

    private void addHistory(History hist) {
        mHistory.add(hist);
        if (mHistory.size() == 1) {
            mOptionMenu.findItem(R.id.menu_item_undo).setEnabled(true);
        }
        if (mUndoneHistory.size() > 0) {
            mUndoneHistory.clear();
            mOptionMenu.findItem(R.id.menu_item_redo).setEnabled(false);
        }
    }

    private void undoHistory() {
        if (mHistory.size() > 0) {
            History hist = mHistory.pop();
            mUndoneHistory.add(hist);
            hist.undo(mDoHistory);
        }
        if (mHistory.size() == 0) {
            mOptionMenu.findItem(R.id.menu_item_undo).setEnabled(false);
        }
        if (mUndoneHistory.size() == 1) {
            mOptionMenu.findItem(R.id.menu_item_redo).setEnabled(true);
        }
    }

    private void redoHistory() {
        if (mUndoneHistory.size() > 0) {
            History hist = mUndoneHistory.pop();
            mHistory.add(hist);
            hist.redo(mDoHistory);
        }
        if (mHistory.size() == 1) {
            mOptionMenu.findItem(R.id.menu_item_undo).setEnabled(true);
        }
        if (mUndoneHistory.size() == 0) {
            mOptionMenu.findItem(R.id.menu_item_redo).setEnabled(false);
        }
    }

    private void checkForZeroColors() {
        if (mPattern.getColors() == null) {
            mFindBestColorsTask = new FindBestColorsTask() {
                @Override
                protected void onPostExecute(Map<Integer, Float> colors) {
                    int patternId = getIntent().getIntExtra(Patterns.INTENT_EXTRA_ID, 0);
                    mPatternApproxImage.setPattern(mPattern, Simple);
                    mGridAdapter.updateColors(mPattern);
                    mGridAdapter.notifyDataSetChanged();
                    mPattern.edit()
                            .setColors(colors)
                            .apply();
                    mPattern = DatabaseManager.getPattern(ChangeParametersActivity.this, patternId);
                }
            };
            mFindBestColorsTask.execute(this, mPattern.getFileName(), DEFAULT_INITIAL_COLORS);
        }
    }

    private OnHistoryDo mDoHistory = new OnHistoryDo() {
        @Override
        public void removeColor(int color) {
            mPattern.edit()
                    .removeColor(color)
                    .apply();
            mGridAdapter.updateColors(mPattern);
            mGridAdapter.notifyDataSetChanged();
            mPatternApproxImage.executeRedraw(Simple);
        }

        @Override
        public void addColor(int color) {
            mPattern.edit()
                    .addColor(color)
                    .apply();
            mGridAdapter.updateColors(mPattern);
            mGridAdapter.notifyDataSetChanged();
            mPatternApproxImage.executeRedraw(Simple);
        }

        @Override
        public void setWidth(int width) {
            mPattern.edit()
                    .setWidth(width)
                    .apply();
            mPatternApproxImage.setPattern(mPattern, Simple);
            mPatternApproxImage.scaleToFit();
        }
    };

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

    private InteractiveImageView.OnImageClickListener mOnImageClickListener = new InteractiveImageView.OnImageClickListener() {
        @Override
        public void onImageClicked(Bitmap bitmap, int x, int y) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            List<int[]> colorsFound = new ArrayList<int[]>();
            for (int ix = 0; ix < CHECK_SQUARE_SIZE; ++ix) {
                for (int iy = 0; iy < CHECK_SQUARE_SIZE; ++iy) {
                    int tx = x - CHECK_SQUARE_RADIUS + ix;
                    int ty = y - CHECK_SQUARE_RADIUS + iy;
                    if (tx >= 0 && ty >= 0 && tx < width && ty < height) {
                        int pixel = bitmap.getPixel(tx, ty);
                        if ((pixel & ColorUtil.ALPHA_CHANNEL) != ColorUtil.ALPHA_CHANNEL) {
                            continue;
                        }
                        colorsFound.add(ColorUtil.splitColor(pixel));
                    }
                }
            }

            if (colorsFound.size() > 0) {
                colorsFound = MMCQ.compute(colorsFound, CHECK_SQUARE_COLORS);
                BetterLog.d(this, "Found colors " + colorsFound.size());
                int[] pixels = new int[colorsFound.size()];
                int i = 0;
                for (int[] color : colorsFound) {
                    pixels[i++] = Color.rgb(color[0], color[1], color[2]);
                }
                Intent intent = new Intent(ChangeParametersActivity.this, ChooseColorDialog.class);
                intent.putExtra("pixels", pixels);
                startActivityForResult(intent, REQUEST_CHOOSE_COLOR);
            } else {
                BetterLog.d(this, "No colors, or only transparent colors, found");
            }
        }
    };

    private void removeColor(int color) {
        mPattern.edit()
                .removeColor(color)
                .apply();
        addHistory(new RemoveColor(color));
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
                int newWidth = data.getIntExtra(ConfigurationWidthActivity.EXTRA_WIDTH
                        , Constants.DEFAULT_WIDTH);
                addHistory(new ChangeWidth(mPattern.getPixelWidth(), newWidth));
                mPattern.edit()
                        .setWidth(newWidth)
                        .apply();
                mPatternApproxImage.setPattern(mPattern, Simple);
                mPatternApproxImage.scaleToFit();
            } else if (requestCode == REQUEST_CHOOSE_COLOR) {
                int pixel = data.getIntExtra("pixel", 0);
                mPattern.edit()
                        .addColor(pixel)
                        .apply();
                addHistory(new AddColor(pixel));
                mPatternApproxImage.setPattern(mPattern, Simple);
                mGridAdapter.updateColors(mPattern);
                mGridAdapter.notifyDataSetChanged();
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
