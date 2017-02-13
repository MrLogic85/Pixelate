package com.sleepyduck.pixelate4crafting.view;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.sleepyduck.pixelate4crafting.view.recycler.SwipeCardAdapter;

/**
 * Created by fredrikmetcalf on 14/02/17.
 */

public class SwipeCardItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private final SwipeCardAdapter mAdapter;
    private final RecyclerView mRecyclerView;
    private final ItemTouchHelper mItemTouchHelper;

    public SwipeCardItemTouchHelperCallback(SwipeCardAdapter adapter, RecyclerView recyclerView) {
        mAdapter = adapter;
        mRecyclerView = recyclerView;
        mItemTouchHelper = new ItemTouchHelper(this);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return mAdapter.onMove((SwipeCardAdapter.ViewHolder) viewHolder, (SwipeCardAdapter.ViewHolder) target);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        switch (direction) {
            case ItemTouchHelper.END:
                mAdapter.swipeRight((SwipeCardAdapter.ViewHolder) viewHolder);
                break;
            case ItemTouchHelper.START:
                mAdapter.swipeLeft((SwipeCardAdapter.ViewHolder) viewHolder);
                mItemTouchHelper.attachToRecyclerView(null);
                mItemTouchHelper.attachToRecyclerView(mRecyclerView);
                break;
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE
                && viewHolder instanceof SwipeCardAdapter.ViewHolder) {
            SwipeCardAdapter.ViewHolder swipeCardViewHolder = (SwipeCardAdapter.ViewHolder) viewHolder;
            swipeCardViewHolder.setTranslation(dX);
            super.onChildDraw(c, recyclerView, viewHolder, 0, 0, actionState, isCurrentlyActive);
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }
}
