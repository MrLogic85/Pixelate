package com.sleepyduck.pixelate4crafting.util.history;

/**
 * Created by fredrikmetcalf on 21/04/16.
 */
public class ChangeWidth implements History {

    private final int mPrevWidth;
    private final int mNewWidth;

    public ChangeWidth(int prevWidth, int newWidth) {
        mPrevWidth = prevWidth;
        mNewWidth = newWidth;
    }

    @Override
    public void undo(OnHistoryDo doHistory) {
        doHistory.setWidth(mPrevWidth);
    }

    @Override
    public void redo(OnHistoryDo doHistory) {
        doHistory.setWidth(mNewWidth);
    }
}
