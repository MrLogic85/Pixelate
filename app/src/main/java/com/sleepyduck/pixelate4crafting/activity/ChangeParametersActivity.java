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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.control.ChooseColorDialog;
import com.sleepyduck.pixelate4crafting.firebase.FirebaseLogger;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract;
import com.sleepyduck.pixelate4crafting.model.DatabaseManager;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.util.BetterLog;
import com.sleepyduck.pixelate4crafting.util.ColorUtil;
import com.sleepyduck.pixelate4crafting.util.MMCQ;
import com.sleepyduck.pixelate4crafting.util.history.AddColor;
import com.sleepyduck.pixelate4crafting.util.history.History;
import com.sleepyduck.pixelate4crafting.util.history.OnHistoryDo;
import com.sleepyduck.pixelate4crafting.util.history.RemoveColor;
import com.sleepyduck.pixelate4crafting.view.ApproxPatternImageView;
import com.sleepyduck.pixelate4crafting.view.InteractiveImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_COMPLETE;
import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_SIZE_OR_COLOR_CHANGED;

public class ChangeParametersActivity extends AppCompatActivity {
    private static final int CHECK_SQUARE_SIZE = 5;
    private static final int CHECK_SQUARE_RADIUS = (CHECK_SQUARE_SIZE - 1) / 2;
    private static final int CHECK_SQUARE_COLORS = 9;

    private static final int STATE_FOCUSED_OFF = 0;
    private static final int STATE_FOCUSED_PALETTE = 1;

    private static final int REQUEST_CHOOSE_COLOR = 2;

    private int mPatternId;
    private ApproxPatternImageView mPatternApproxImage;
    private GridView mPaletteGrid;
    private GridAdapter mGridAdapter;
    private int mState = STATE_FOCUSED_OFF;
    private final Stack<History> mHistory = new Stack<>();
    private final Stack<History> mUndoneHistory = new Stack<>();
    private Menu mOptionMenu;
    private int mOrigFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_paramerters);
        FirebaseLogger.getInstance(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        ViewGroup group = (ViewGroup) findViewById(R.id.view_group);
        LayoutTransition layoutTransition = group.getLayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);

        mPatternId = getIntent().getIntExtra(Pattern.INTENT_EXTRA_ID, 0);
        Pattern pattern = DatabaseManager.getPattern(this, mPatternId);
        mOrigFlag = pattern.getFlag();
        pattern.edit()
                .setFlag(DatabaseContract.PatternColumns.FLAG_SIZE_OR_COLOR_CHANGING)
                .apply(false);

        mPatternApproxImage = (ApproxPatternImageView) findViewById(R.id.image_approximated);
        mPatternApproxImage.setPattern(pattern);

        InteractiveImageView originalImage = (InteractiveImageView) findViewById(R.id.image_original);
        originalImage.setImageBitmap(BitmapHandler.getFromFileName(this, pattern.getFileName()));
        originalImage.setOnImageClickListener(mOnImageClickListener);

        mPaletteGrid = (GridView) findViewById(R.id.palette_grid);
        mGridAdapter = new GridAdapter(pattern);
        mPaletteGrid.setAdapter(mGridAdapter);
        mPaletteGrid.setOnItemClickListener(mItemClickListener);
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
                intent.putExtra(Pattern.INTENT_EXTRA_ID, mPatternId);
                setResult(RESULT_OK, intent);
                finish();
                return true;
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
        Pattern pattern = DatabaseManager.getPattern(this, mPatternId);
        if (mHistory.size() > 0 || mOrigFlag != FLAG_COMPLETE) {
            final Pattern.Edit edit = pattern.edit();
            final int[] palette = new int[pattern.getColorCount()];
            pattern.getColors(palette);
            Arrays.sort(palette);
            pattern.foreachChangedPixel(new Pattern.PixelCallback() {
                @Override
                public void execute(int x, int y, int color) {
                    if (Arrays.binarySearch(palette, color) < 0) {
                        edit.eraseChangedPixelAt(x, y);
                    }
                }
            });
            edit.setFlag(FLAG_SIZE_OR_COLOR_CHANGED)
                    .apply(false);
        } else {
            pattern.edit()
                    .setFlag(mOrigFlag)
                    .apply(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (mState != STATE_FOCUSED_OFF) {
            setState(STATE_FOCUSED_OFF);
        } else {
            Intent intent = new Intent();
            intent.putExtra(Pattern.INTENT_EXTRA_ID, mPatternId);
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

    private final OnHistoryDo mDoHistory = new OnHistoryDo() {
        @Override
        public void removeColor(int color) {
            FirebaseLogger.getInstance(ChangeParametersActivity.this).colorRemoved();
            Pattern pattern = DatabaseManager.getPattern(ChangeParametersActivity.this, mPatternId);
            pattern.edit()
                    .removeColor(color)
                    .setFlag(DatabaseContract.PatternColumns.FLAG_SIZE_OR_COLOR_CHANGING)
                    .apply(false);
            mGridAdapter.updateColors(pattern);
            mGridAdapter.notifyDataSetChanged();
            pattern = DatabaseManager.getPattern(ChangeParametersActivity.this, mPatternId);
            mPatternApproxImage.setPattern(pattern);
        }

        @Override
        public void addColor(int color) {
            FirebaseLogger.getInstance(ChangeParametersActivity.this).colorAdded();
            Pattern pattern = DatabaseManager.getPattern(ChangeParametersActivity.this, mPatternId);
            pattern.edit()
                    .addColor(color)
                    .setFlag(DatabaseContract.PatternColumns.FLAG_SIZE_OR_COLOR_CHANGING)
                    .apply(false);
            mGridAdapter.updateColors(pattern);
            mGridAdapter.notifyDataSetChanged();
            pattern = DatabaseManager.getPattern(ChangeParametersActivity.this, mPatternId);
            mPatternApproxImage.setPattern(pattern);
        }
    };

    private final AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mState != STATE_FOCUSED_PALETTE) {
                setState(STATE_FOCUSED_PALETTE);
            } else {
                removeColor((int) mGridAdapter.getItem(position));
            }
        }
    };

    private final InteractiveImageView.OnImageClickListener mOnImageClickListener = new InteractiveImageView.OnImageClickListener() {
        @Override
        public void onImageClicked(Bitmap bitmap, int x, int y, float rawX, float rawY) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            List<int[]> colorsFound = new ArrayList<>();
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
        FirebaseLogger.getInstance(this).colorRemoved();
        Pattern pattern = DatabaseManager.getPattern(ChangeParametersActivity.this, mPatternId);
        pattern.edit()
                .removeColor(color)
                .setFlag(DatabaseContract.PatternColumns.FLAG_SIZE_OR_COLOR_CHANGING)
                .apply(false);
        addHistory(new RemoveColor(color));
        pattern = DatabaseManager.getPattern(ChangeParametersActivity.this, mPatternId);
        mGridAdapter.updateColors(pattern);
        mGridAdapter.notifyDataSetChanged();
        pattern = DatabaseManager.getPattern(ChangeParametersActivity.this, mPatternId);
        mPatternApproxImage.setPattern(pattern);
        mPatternApproxImage.executeRedraw();
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
            Pattern pattern = DatabaseManager.getPattern(ChangeParametersActivity.this, mPatternId);
            if (requestCode == REQUEST_CHOOSE_COLOR) {
                FirebaseLogger.getInstance(this).colorAdded();
                int pixel = data.getIntExtra("pixel", 0);
                pattern.edit()
                        .addColor(pixel)
                        .setFlag(DatabaseContract.PatternColumns.FLAG_SIZE_OR_COLOR_CHANGING)
                        .apply(false);
                addHistory(new AddColor(pixel));
                pattern = DatabaseManager.getPattern(ChangeParametersActivity.this, mPatternId);
                mPatternApproxImage.setPattern(pattern);
                mGridAdapter.updateColors(pattern);
                mGridAdapter.notifyDataSetChanged();
            }
        }
    }

    private static class GridAdapter extends BaseAdapter {
        static final int STATE_SMALL = 1;
        static final int STATE_LARGE = 2;

        private int[] mColors;
        private int mState = STATE_SMALL;

        GridAdapter(Pattern pattern) {
            updateColors(pattern);
        }

        void updateColors(Pattern pattern) {
            mColors = new int[pattern.getColorCount()];
            pattern.getColors(mColors);
            Arrays.sort(mColors);
            notifyDataSetChanged();
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
            layout.setLayoutParams(new AbsListView.LayoutParams(size, size));

            // Layout card
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    MATCH_PARENT,
                    MATCH_PARENT);
            int margin = (int) card.getCardElevation();
            params.setMargins(margin, margin, margin * 2, margin * 2);
            card.setLayoutParams(params);

            return layout;
        }

        void setState(int state) {
            mState = state;
        }

        int getState() {
            return mState;
        }
    }
}
