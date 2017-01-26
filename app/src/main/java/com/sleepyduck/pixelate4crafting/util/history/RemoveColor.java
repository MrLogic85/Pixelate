package com.sleepyduck.pixelate4crafting.util.history;

/**
 * Created by fredrikmetcalf on 21/04/16.
 */
public class RemoveColor implements History {
    private final int mColor;

    public RemoveColor(int color) {
        mColor = color;
    }

    @Override
    public void undo(OnHistoryDo doHistory) {
        doHistory.addColor(mColor);
    }

    @Override
    public void redo(OnHistoryDo doHistory) {
        doHistory.removeColor(mColor);
    }
}
