package com.sleepyduck.pixelate4crafting.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.sleepyduck.pixelate4crafting.control.util.Callback;
import com.sleepyduck.pixelate4crafting.view.SwipeCard;

import static com.sleepyduck.pixelate4crafting.model.Pattern.State.ACTIVE;
import static com.sleepyduck.pixelate4crafting.model.Pattern.State.COMPLETED;
import static com.sleepyduck.pixelate4crafting.model.Pattern.State.LATEST;

/**
 * Created by fredrik.metcalf on 2016-04-08.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private static final int NUM_HEADERS = 3;
    private static final int HEADER_LATEST = R.string.latest;
    private static final int HEADER_ACTIVE = R.string.active;
    private static final int HEADER_COMPLETED = R.string.completed;

    private final Context mContext;
    private View.OnClickListener mOnItemClickListener;
    private View.OnClickListener mOnRightButtonClickListener;
    private View.OnClickListener mOnLeftButtonClickListener;
    private int mCountCompleted;
    private int mCountActive;
    private int mCountLatest;

    public RecyclerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SwipeCard card = (SwipeCard) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.swipe_card, parent, false);
        card.setOnClickListener(mOnItemClickListener);
        card.setOnClickListener(mOnLeftButtonClickListener, 0);
        card.setOnClickListener(mOnRightButtonClickListener, 1);
        card.setOnClickListener(mOnRightButtonClickListener, 2);
        return new ViewHolder(card);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (position == 0 && position < mCountLatest) {
            holder.setData(mContext.getString(HEADER_LATEST));
        } else if (position == mCountLatest && position < mCountLatest + mCountActive) {
            holder.setData(mContext.getString(HEADER_ACTIVE));
        } else if (position == mCountLatest + mCountActive && position < mCountLatest + mCountActive + mCountCompleted) {
            holder.setData(mContext.getString(HEADER_COMPLETED));
        } else if (position < mCountLatest) {
            Patterns.GetPatternAt(LATEST, position - 1, new Callback<Pattern>() {
                @Override
                public void onCallback(Pattern obj) {
                    holder.setData(obj);
                }
            });
        } else if (position < mCountLatest + mCountActive) {
            Patterns.GetPatternAt(ACTIVE, position - 1 - mCountLatest, new Callback<Pattern>() {
                @Override
                public void onCallback(Pattern obj) {
                    holder.setData(obj);
                }
            });
        } else {
            Patterns.GetPatternAt(COMPLETED, position - 1 - mCountLatest - mCountActive, new Callback<Pattern>() {
                @Override
                public void onCallback(Pattern obj) {
                    holder.setData(obj);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        mCountLatest = Patterns.Count(LATEST);
        mCountActive = Patterns.Count(ACTIVE);
        mCountCompleted = Patterns.Count(COMPLETED);
        return (mCountLatest > 0 ? ++mCountLatest : 0)
                + (mCountActive > 0 ? ++mCountActive : 0)
                + (mCountCompleted > 0 ? ++mCountCompleted : 0);
    }

    public void setOnItemClickListener(View.OnClickListener onClickListener) {
        mOnItemClickListener = onClickListener;
    }

    public void setOnRightButtonClickListener(View.OnClickListener l) {
        mOnRightButtonClickListener = l;
    }

    public void setOnLeftButtonClickListener(View.OnClickListener l) {
        mOnLeftButtonClickListener = l;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        SwipeCard mItemView;

        public ViewHolder(SwipeCard itemView) {
            super(itemView);
            mItemView = itemView;
        }

        public void setData(Object data) {
            if (data instanceof Pattern) {
                Pattern pattern = (Pattern) data;
                ImageView icon = (ImageView) mItemView.findViewById(R.id.icon);
                if (pattern.getFileNameThumbnail() != null
                        && pattern.getFileNameThumbnail().length() > 0) {
                    Bitmap bitmapIcon = BitmapHandler.getFromFileName(mContext, pattern.getFileNameThumbnail());
                    if (bitmapIcon != null) {
                        icon.setImageBitmap(bitmapIcon);
                    } else {
                        icon.setImageResource(R.drawable.ic_launcher);
                    }
                } else {
                    icon.setImageResource(R.drawable.ic_launcher);
                }

                TextView title = (TextView) mItemView.findViewById(R.id.title);
                title.setText(pattern.getTitle());

                if (pattern.getColors() != null) {
                    GridLayout grid = (GridLayout) mItemView.findViewById(R.id.pattern_colors);
                    grid.removeAllViews();
                    int countColors = pattern.getColors().size();
                    int margin = (int) mItemView.getContext().getResources().getDimension(R.dimen.spacing);
                    int colorSize = (int) mItemView.getContext().getResources().getDimension(R.dimen.color_square_size_small);
                    float numColorsInARow = (float)(grid.getWidth()) / (colorSize + margin);
                    int columnCount = (int) numColorsInARow;
                    grid.setColumnCount(columnCount);
                    int rowCount = countColors / columnCount + (countColors % grid.getColumnCount() > 0 ? 1 : 0);
                    grid.setRowCount(rowCount);

                    int x = 0, y = 0;
                    for (int color : pattern.getColors().keySet()) {
                        View view = new View(mItemView.getContext());
                        view.setBackgroundColor(color);
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = colorSize;
                        params.height = colorSize;
                        params.columnSpec = GridLayout.spec(x);
                        params.rowSpec = GridLayout.spec(y);
                        params.setMargins(0, 0, margin, margin);
                        grid.addView(view, params);

                        x++;
                        if (x == grid.getColumnCount()) {
                            y++;
                            x = 0;
                        }
                    }
                }

                mItemView.setTag(pattern);
                mItemView.findViewById(R.id.header).setVisibility(View.GONE);
                mItemView.findViewById(R.id.card).setVisibility(View.VISIBLE);

                if (pattern.getState() == COMPLETED) {
                    mItemView.findViewById(R.id.button2).setVisibility(View.GONE);
                    mItemView.findViewById(R.id.button3).setVisibility(View.VISIBLE);
                } else {
                    mItemView.findViewById(R.id.button2).setVisibility(View.VISIBLE);
                    mItemView.findViewById(R.id.button3).setVisibility(View.GONE);
                }
            } else if (data instanceof String) {
                mItemView.findViewById(R.id.header).setVisibility(View.VISIBLE);
                mItemView.findViewById(R.id.card).setVisibility(View.GONE);
                ((TextView) mItemView.findViewById(R.id.header)).setText((String) data);
            }

            mItemView.restore();
        }
    }
}
