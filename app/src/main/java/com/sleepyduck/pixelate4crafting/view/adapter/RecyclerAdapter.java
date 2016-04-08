package com.sleepyduck.pixelate4crafting.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.control.BitmapHandler;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.model.Patterns;
import com.sleepyduck.pixelate4crafting.util.BetterLog;
import com.sleepyduck.pixelate4crafting.view.SwipeCard;

/**
 * Created by fredrik.metcalf on 2016-04-08.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private View.OnClickListener mOnClickListener;

    public RecyclerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BetterLog.d(this, "Creating View Holder");
        SwipeCard card = (SwipeCard) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.swipe_card, parent, false);
        card.setOnClickListener(mOnClickListener);
        return new ViewHolder(card);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(Patterns.GetPatternAt(position));
    }

    @Override
    public int getItemCount() {
        return Patterns.Size();
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        SwipeCard mItemView;

        public ViewHolder(SwipeCard itemView) {
            super(itemView);
            mItemView = itemView;
        }

        public void setData(Pattern pattern) {
            ImageView icon = (ImageView) mItemView.getContentView().findViewById(R.id.icon);
            Bitmap bitmapIcon = BitmapHandler.getFromFileName(mContext, pattern.getFileNameThumbnail());
            icon.setImageBitmap(bitmapIcon);

            TextView title = (TextView) mItemView.getContentView().findViewById(R.id.title);
            title.setText(pattern.getTitle());
        }
    }
}
