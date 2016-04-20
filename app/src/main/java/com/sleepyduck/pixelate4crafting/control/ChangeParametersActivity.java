package com.sleepyduck.pixelate4crafting.control;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.tasks.FindBestColorsTask;
import com.sleepyduck.pixelate4crafting.control.util.BetterLog;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.sleepyduck.pixelate4crafting.view.InteractiveImageView;
import com.sleepyduck.pixelate4crafting.view.PatternImageView;

import static com.sleepyduck.pixelate4crafting.view.PatternImageView.Style.Simple;

/**
 * Created by fredrikmetcalf on 20/04/16.
 */
public class ChangeParametersActivity extends AppCompatActivity {

    private Pattern mPattern;
    private InteractiveImageView mOriginalImage;
    private PatternImageView mPatternApproxImage;
    private FindBestColorsTask mFindBestColorsTask;
    private GridView mPaletteGrid;
    private GridAdapter mGridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_paramerters);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPattern = Patterns.GetPattern(getIntent().getIntExtra(Patterns.INTENT_EXTRA_ID, 0));
        mOriginalImage = (InteractiveImageView) findViewById(R.id.image_original);
        mPatternApproxImage = (PatternImageView) findViewById(R.id.image_approximated);

        mOriginalImage.setImageBitmap(BitmapHandler.getFromFileName(this, mPattern.getFileName()));
        mPatternApproxImage.setPattern(mPattern, Simple);

        mPaletteGrid = (GridView) findViewById(R.id.palette_grid);
        mGridAdapter = new GridAdapter(this, mPattern);
        mPaletteGrid.setAdapter(mGridAdapter);

        if (mPattern.getColors() == null) {
            mFindBestColorsTask = new FindBestColorsTask() {
                @Override
                protected void onPostExecute(Integer integer) {
                    mPatternApproxImage.setPattern(mPattern, Simple);
                    mGridAdapter.updateColors(mPattern);
                    mGridAdapter.notifyDataSetChanged();
                }
            };
            mFindBestColorsTask.execute(this, mPattern, 5);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFindBestColorsTask != null) {
            mFindBestColorsTask.cancel(true);
        }
    }

    private static class GridAdapter extends BaseAdapter {

        private int[] mColors;
        private final Context mContext;

        public GridAdapter(Context context, Pattern pattern) {
            mContext = context;
            updateColors(pattern);
        }

        public void updateColors(Pattern pattern) {
            if (pattern.getColors() != null) {
                mColors = new int[pattern.getColors().size()];
                int count = 0;
                for (int color : pattern.getColors().keySet()) {
                    mColors[count++] = color;
                }
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
            BetterLog.d(this, "Get view " + position + ": " + convertView);
            if (convertView == null) {
                convertView = new View(mContext);
                // TODO Create the correct view
            }
            convertView.setBackgroundColor(mColors[position]);
            return convertView;
        }
    }
}
