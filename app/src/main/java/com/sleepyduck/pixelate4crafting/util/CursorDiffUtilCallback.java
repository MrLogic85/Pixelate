package com.sleepyduck.pixelate4crafting.util;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.text.TextUtils;

import java.util.Arrays;

/**
 * Created by fredrikmetcalf on 29/01/17.
 */

public class CursorDiffUtilCallback extends DiffUtil.Callback {

    private final Cursor oldCursor;
    private final Cursor newCursor;

    public CursorDiffUtilCallback(Cursor oldCursor, Cursor newCursor) {
        this.oldCursor = oldCursor;
        this.newCursor = newCursor;
    }

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

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        final int numNewColumns = newCursor.getColumnCount();
        final int numOldColumns = oldCursor.getColumnCount();
        if (numNewColumns != numOldColumns) {
            return null;
        }

        oldCursor.moveToPosition(oldItemPosition);
        newCursor.moveToPosition(newItemPosition);

        Bundle payload = new Bundle();
        for (int colIndex = 0; colIndex < numNewColumns; colIndex++) {
            final int newType = newCursor.getType(colIndex);
            final int oldType = oldCursor.getType(colIndex);

            if (newType != oldType) {
                return null;
            }

            switch (newType) {
                case Cursor.FIELD_TYPE_BLOB: {
                    if (!Arrays.equals(newCursor.getBlob(colIndex), oldCursor.getBlob(colIndex))) {
                        payload.putByteArray(oldCursor.getColumnName(colIndex), newCursor.getBlob(colIndex));
                    }
                    break;
                }
                case Cursor.FIELD_TYPE_FLOAT: {
                    if (newCursor.getFloat(colIndex) != oldCursor.getFloat(colIndex)) {
                        payload.putFloat(oldCursor.getColumnName(colIndex), newCursor.getFloat(colIndex));
                    }
                    break;
                }
                case Cursor.FIELD_TYPE_INTEGER: {
                    if (newCursor.getInt(colIndex) != oldCursor.getInt(colIndex)) {
                        payload.putInt(oldCursor.getColumnName(colIndex), newCursor.getInt(colIndex));
                    }
                    break;
                }

                case Cursor.FIELD_TYPE_NULL: {
                    break;
                }

                case Cursor.FIELD_TYPE_STRING: {
                    if (!TextUtils.equals(newCursor.getString(colIndex), oldCursor.getString(colIndex))) {
                        payload.putString(oldCursor.getColumnName(colIndex), newCursor.getString(colIndex));
                    }
                    break;
                }
            }
        }

        return payload;
    }
}
