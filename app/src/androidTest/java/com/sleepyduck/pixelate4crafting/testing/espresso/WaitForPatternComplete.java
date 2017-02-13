package com.sleepyduck.pixelate4crafting.testing.espresso;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.support.test.espresso.IdlingResource;
import android.widget.Toast;

import com.sleepyduck.pixelate4crafting.model.DatabaseContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by fredrikmetcalf on 02/02/17.
 */

class WaitForPatternComplete implements IdlingResource {

    private boolean isIdle;
    private Context context;
    private int flagSum = 0;

    private static final List<Integer> IdleFlags = new ArrayList<>();

    static {
        IdleFlags.add(DatabaseContract.PatternColumns.FLAG_SIZE_OR_COLOR_CHANGING);
        IdleFlags.add(DatabaseContract.PatternColumns.FLAG_COMPLETE);
    }

    private CursorLoader loader;

    WaitForPatternComplete(Context context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return WaitForPatternComplete.class.getSimpleName();
    }

    @Override
    public boolean isIdleNow() {
        return isIdle;
    }

    @Override
    public void registerIdleTransitionCallback(final ResourceCallback callback) {
        String[] PROJ = {DatabaseContract.PatternColumns.FLAG,
                DatabaseContract.PatternColumns.PENDING_DELETE};
        final String SELECTION = String.format("%s = 0",
                DatabaseContract.PatternColumns.PENDING_DELETE);
        loader = new CursorLoader(context, DatabaseContract.PatternColumns.URI, PROJ, SELECTION, null, null);
        loader.registerListener(0, new Loader.OnLoadCompleteListener<Cursor>() {
            @Override
            public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
                int[] flags = new int[data.getCount()];
                int i = 0;
                boolean isIdle = true;
                int newFlagSum = 0;
                while (data.moveToNext()) {
                    int flag = data.getInt(0);
                    newFlagSum += flag;
                    flags[i++] = flag;
                    isIdle &= IdleFlags.contains(flag);
                }
                if (flagSum != newFlagSum) {
                    Toast.makeText(context, "Pattern (" + (isIdle ? "idle" : "not idle") + ") flags are " + Arrays.toString(flags), Toast.LENGTH_SHORT).show();
                    flagSum = newFlagSum;
                }
                if (isIdle) {
                    callback.onTransitionToIdle();
                }
                WaitForPatternComplete.this.isIdle = isIdle;
            }
        });
        loader.startLoading();
    }

    void stopLoader() {
        loader.stopLoading();
    }
}
