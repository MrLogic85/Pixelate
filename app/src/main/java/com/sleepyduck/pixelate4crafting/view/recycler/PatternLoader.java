package com.sleepyduck.pixelate4crafting.view.recycler;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sleepyduck.pixelate4crafting.model.DatabaseContract;

import java.util.Random;

public class PatternLoader implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String SORT_ORDER =
            DatabaseContract.PatternColumns.STATE + " ASC, "
                    + DatabaseContract.PatternColumns.TIME + " DESC";
    private static final String SELECTION = String.format("%s = 0",
            DatabaseContract.PatternColumns.PENDING_DELETE);

    private final SwipeCardAdapter mAdapter;
    private final int mLoaderId;
    private final AppCompatActivity mActivity;

    public PatternLoader(AppCompatActivity activity, SwipeCardAdapter adapter) {
        this.mAdapter = adapter;
        this.mActivity = activity;
        mLoaderId = new Random().nextInt();
        activity.getLoaderManager().initLoader(mLoaderId, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new CursorLoader(mActivity,
                DatabaseContract.PatternColumns.URI, null, SELECTION, null, SORT_ORDER);
        //        DatabaseContract.PatternColumns.URI, null, null, null, SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
