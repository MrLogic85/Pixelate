package com.sleepyduck.pixelate4crafting.view.recycler;

import android.animation.ValueAnimator;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract;
import com.sleepyduck.pixelate4crafting.model.DatabaseManager;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.view.LineProgressBar;
import com.sleepyduck.pixelate4crafting.view.SwipeCard;

import static com.sleepyduck.pixelate4crafting.model.DatabaseContract.PatternColumns.FLAG_COMPLETE;

public class SwipeCardAdapter extends CursorRecyclerViewAdapter<SwipeCardAdapter.ViewHolder> {
    private final Context mContext;
    private View.OnClickListener mOnItemClickListener;
    private OnSwipeListener mOnRightSwipeListener;
    private OnSwipeListener mOnLeftSwipeListener;

    public SwipeCardAdapter(Context context) {
        super();
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SwipeCard card = (SwipeCard) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.swipe_card, parent, false);
        card.setOnClickListener(mOnItemClickListener);
        return new ViewHolder(card);
    }

    @Override
    public void bindToRow(Cursor cursor, ViewHolder holder, int position) {
        int id = cursor.getInt(cursor.getColumnIndex(DatabaseContract.PatternColumns._ID));
        Pattern pattern = DatabaseManager.getPattern(mContext, id);
        holder.setData(pattern);
    }

    public void setOnItemClickListener(View.OnClickListener onClickListener) {
        mOnItemClickListener = onClickListener;
    }

    public void setOnRightSwipeListener(OnSwipeListener l) {
        mOnRightSwipeListener = l;
    }

    public void setOnLeftSwipeListener(OnSwipeListener l) {
        mOnLeftSwipeListener = l;
    }

    public void swipeRight(final ViewHolder viewHolder) {
        mOnRightSwipeListener.onSwipe(viewHolder.pattern);
        new Handler(mContext.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                viewHolder.setTranslation(0);
            }
        }, 500);
    }

    public void swipeLeft(ViewHolder viewHolder) {
        mOnLeftSwipeListener.onSwipe(viewHolder.pattern);
    }

    public boolean onMove(ViewHolder from, ViewHolder to) {
        // TODO
        return false;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final SwipeCard mItemView;
        private Pattern pattern;
        private View content;
        private View buttonRight1;
        private View buttonRight2;
        private View alignLayout;

        ViewHolder(SwipeCard itemView) {
            super(itemView);
            mItemView = itemView;
        }

        public void setData(Pattern pattern) {
            this.pattern = pattern;

            content = mItemView.findViewById(R.id.content);
            buttonRight1 = mItemView.findViewById(R.id.imageRight1);
            buttonRight2 = mItemView.findViewById(R.id.imageRight2);
            alignLayout = mItemView.findViewById(R.id.align_layout);

            mItemView.findViewById(R.id.check_mark).setVisibility(
                    pattern.getState() == DatabaseContract.PatternColumns.STATE_COMPLETED ?
                            View.VISIBLE: View.INVISIBLE);

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

            RecyclerView recyclerView = (RecyclerView) mItemView.findViewById(R.id.color_recycler);
            recyclerView.setAdapter(new ColorsAdapter(pattern.Id,
                    pattern.getColors(new int[pattern.getColorCount()])));

            LineProgressBar lineProgressBar = (LineProgressBar) mItemView.findViewById(R.id.progress_bar);
            int flag = pattern.getFlag();
            if (flag != FLAG_COMPLETE) {
                lineProgressBar.setVisibility(View.VISIBLE);
                lineProgressBar.setProgress(pattern.getProgress());
            } else {
                lineProgressBar.setVisibility(View.INVISIBLE);
            }

            mItemView.setTag(pattern);
            mItemView.findViewById(R.id.card).setVisibility(View.VISIBLE);

            boolean show1 = pattern.getState() != DatabaseContract.PatternColumns.STATE_COMPLETED;
            if (show1 ^ buttonRight1.getVisibility() == View.VISIBLE) {
                ValueAnimator animator = ValueAnimator.ofFloat(content.getTranslationX(), 0);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        setTranslation((Float) animation.getAnimatedValue());
                    }
                });
                animator.start();
            }
            buttonRight1.setVisibility(show1 ? View.VISIBLE : View.INVISIBLE);
            buttonRight2.setVisibility(show1 ? View.INVISIBLE : View.VISIBLE);
        }

        public void setTranslation(float dX) {
            RelativeLayout.LayoutParams alignParams = (RelativeLayout.LayoutParams) alignLayout.getLayoutParams();
            if (dX > 0) {
                alignParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                alignParams.removeRule(RelativeLayout.ALIGN_PARENT_START);
            } else {
                alignParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                alignParams.removeRule(RelativeLayout.ALIGN_PARENT_END);
            }
            alignParams.width = (int) (mItemView.getWidth() - Math.abs(dX));
            alignLayout.setLayoutParams(alignParams);
            content.setTranslationX(dX);
        }
    }

    public interface OnSwipeListener {
        void onSwipe(Pattern pattern);
    }
}
