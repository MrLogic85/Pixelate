package com.sleepyduck.pixelate4crafting.view.recycler;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ViewGroup;

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

        final Cursor oldCursor = mCursor;

        DiffUtil.calculateDiff(new DiffUtil.Callback() {

            @Override
            public int getOldListSize() {
                return oldCursor == null ? 0 : oldCursor.getCount();
            }

            @Override
            public int getNewListSize() {
                return newCursor == null ? 0 : newCursor.getCount();
            }

            private long getRowId(Cursor c, int position) {

                final int prevPosition = c.getPosition();

                c.moveToPosition(position);
                final long id = c.getLong(c.getColumnIndexOrThrow(BaseColumns._ID));

                // Restore previous position.
                c.moveToPosition(prevPosition);

                return id;
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return getRowId(newCursor, newItemPosition) == getRowId(oldCursor, oldItemPosition);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {

                newCursor.moveToPosition(newItemPosition);
                oldCursor.moveToPosition(oldItemPosition);

                final int numNewColumns = newCursor.getColumnCount();
                final int numOldColumns = oldCursor.getColumnCount();

                if (numNewColumns != numOldColumns) {
                    return false;
                }

                for (int colIndex = 0; colIndex < numNewColumns; colIndex++) {
                    final int newType = newCursor.getType(colIndex);
                    final int oldType = oldCursor.getType(colIndex);

                    if (newType != oldType) {
                        return false;
                    }

                    switch (newType) {
                        case Cursor.FIELD_TYPE_BLOB: {
                            if (!Arrays.equals(newCursor.getBlob(colIndex), oldCursor.getBlob(colIndex))) {
                                return false;
                            }
                            break;
                        }
                        case Cursor.FIELD_TYPE_FLOAT: {
                            if (newCursor.getFloat(colIndex) != oldCursor.getFloat(colIndex)) {
                                return false;
                            }
                            break;
                        }
                        case Cursor.FIELD_TYPE_INTEGER: {
                            if (newCursor.getInt(colIndex) != oldCursor.getInt(colIndex)) {
                                return false;
                            }
                            break;
                        }

                        case Cursor.FIELD_TYPE_NULL: {
                            break;
                        }

                        case Cursor.FIELD_TYPE_STRING: {
                            if (!TextUtils.equals(newCursor.getString(colIndex), oldCursor.getString(colIndex))) {
                                return false;
                            }
                            break;
                        }
                    }
                }

                return true;
            }

        }).dispatchUpdatesTo(this);

        mCursor = newCursor;
    }
}
