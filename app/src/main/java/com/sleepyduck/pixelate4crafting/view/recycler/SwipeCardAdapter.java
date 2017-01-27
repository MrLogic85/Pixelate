package com.sleepyduck.pixelate4crafting.view.recycler;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private View.OnClickListener mOnRightButtonClickListener;
    private View.OnClickListener mOnLeftButtonClickListener;

    public SwipeCardAdapter(Context context) {
        super();
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
    public void bindToRow(Cursor cursor, ViewHolder holder, int position) {
        int id = cursor.getInt(cursor.getColumnIndex(DatabaseContract.PatternColumns._ID));
        Pattern pattern = DatabaseManager.getPattern(mContext, id);
        holder.setData(pattern);
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

        public void setData(Pattern pattern) {
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
            recyclerView.setAdapter(new ColorsAdapter(pattern.getColors()));

            LineProgressBar lineProgressBar = (LineProgressBar) mItemView.findViewById(R.id.progress_bar);
            int flag = pattern.getFlag();
            if (flag != FLAG_COMPLETE) {
                lineProgressBar.setVisibility(View.VISIBLE);
                lineProgressBar.setProgress(pattern.getProgress());
            } else {
                lineProgressBar.setVisibility(View.INVISIBLE);
            }

            mItemView.setTag(pattern);
            mItemView.findViewById(R.id.header).setVisibility(View.GONE);
            mItemView.findViewById(R.id.card).setVisibility(View.VISIBLE);

            if (pattern.getState() == DatabaseContract.PatternColumns.STATE_COMPLETED) {
                mItemView.findViewById(R.id.button2).setVisibility(View.GONE);
                mItemView.findViewById(R.id.button3).setVisibility(View.VISIBLE);
            } else {
                mItemView.findViewById(R.id.button2).setVisibility(View.VISIBLE);
                mItemView.findViewById(R.id.button3).setVisibility(View.GONE);
            }

            mItemView.restore();
        }
    }
}
