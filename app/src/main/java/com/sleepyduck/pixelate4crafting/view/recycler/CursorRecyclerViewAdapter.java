package com.sleepyduck.pixelate4crafting.view.recycler;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.sleepyduck.pixelate4crafting.util.CursorDiffUtilCallback;

import java.util.Arrays;

public abstract class CursorRecyclerViewAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {

    private Cursor mCursor;

    public CursorRecyclerViewAdapter() {
        setHasStableIds(true);
    }

    @Override
    public abstract T onCreateViewHolder(ViewGroup parent, int viewType);

    /**
     * Bind ViewHolder to the current row of the cursor.
     */
    public abstract void bindToRow(Cursor cursor, T holder, int position);

    @Override
    public void onBindViewHolder(T holder, int position) {
        mCursor.moveToPosition(holder.getAdapterPosition());

        bindToRow(mCursor, holder, position);
    }

    @Override
    public final long getItemId(int position) {

        if (mCursor == null) {
            return RecyclerView.NO_ID;
        }

        mCursor.moveToPosition(position);
        return mCursor.getLong(mCursor.getColumnIndexOrThrow(BaseColumns._ID));
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    /**
     * Set the new Cursor to use as data source. Previous Cursor is not closed.
     */
    public void swapCursor(final Cursor newCursor) {
        DiffUtil.calculateDiff(new CursorDiffUtilCallback(mCursor, newCursor)).dispatchUpdatesTo(this);
        mCursor = newCursor;
    }
}
