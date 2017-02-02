package com.sleepyduck.pixelate4crafting.testing.espresso;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.test.espresso.IdlingResource;

import com.sleepyduck.pixelate4crafting.activity.MainActivity;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract;
import com.sleepyduck.pixelate4crafting.model.DatabaseManager;
import com.sleepyduck.pixelate4crafting.model.Pattern;

/**
 * Created by fredrikmetcalf on 02/02/17.
 */

class WaitForPatternFlag implements IdlingResource {

    private MainActivity activity;
    private final int flag;
    private Pattern pattern;

    WaitForPatternFlag(MainActivity activity, int flag) {
        this.activity = activity;
        this.flag = flag;
    }

    @Override
    public String getName() {
        return WaitForPatternFlag.class.getSimpleName();
    }

    @Override
    public boolean isIdleNow() {
        return pattern != null && pattern.getFlag() == flag;
    }

    @Override
    public void registerIdleTransitionCallback(final ResourceCallback callback) {
        final int loaderId = 345987345;
        activity.getLoaderManager().initLoader(loaderId, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(activity, DatabaseContract.PatternColumns.URI, null, null, null, null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if (data.moveToFirst()) {
                    pattern = DatabaseManager.getPattern(activity, data);
                    if (isIdleNow()) {
                        activity.getLoaderManager().destroyLoader(loaderId);
                        callback.onTransitionToIdle();
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });
    }
}
