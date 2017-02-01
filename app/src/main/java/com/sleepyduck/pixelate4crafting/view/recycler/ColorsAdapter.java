package com.sleepyduck.pixelate4crafting.view.recycler;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.util.ColorUtil;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by fredrik.metcalf on 2017-01-27.
 */

class ColorsAdapter extends RecyclerView.Adapter<ColorsAdapter.ViewHolder> {

    private final Integer[] colors;

    ColorsAdapter(int[] colors) {
        this.colors = new Integer[colors.length];
        for (int i = 0;i < colors.length; ++i) {
            this.colors[i] = colors[i];
        }
        ColorUtil.Sort(this.colors);
    }

    @Override
    public ColorsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.color_grid_item_small, parent, false));
    }

    @Override
    public void onBindViewHolder(ColorsAdapter.ViewHolder holder, int position) {
        holder.cardView.setCardBackgroundColor(colors[holder.getAdapterPosition()]);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return colors.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
        }
    }
}
