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

public class ColorsAdapter extends RecyclerView.Adapter<ColorsAdapter.ViewHolder> {
    private static final int COMPARE_COLOR = Color.WHITE;

    private final ArrayList<Integer> colors;

    public ColorsAdapter(Map<Integer, Float> colorMap) {
        if (colorMap != null) {
            colors = new ArrayList<>(colorMap.keySet());
            ColorUtil.Sort(colors);
        } else {
            colors = new ArrayList<>();
        }
    }

    @Override
    public ColorsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.color_grid_item_small, parent, false));
    }

    @Override
    public void onBindViewHolder(ColorsAdapter.ViewHolder holder, int position) {
        holder.cardView.setCardBackgroundColor(colors.get(holder.getAdapterPosition()));
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
        }
    }
}
