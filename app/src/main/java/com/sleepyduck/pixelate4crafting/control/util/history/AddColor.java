package com.sleepyduck.pixelate4crafting.control.util.history;

/**
 * Created by fredrikmetcalf on 21/04/16.
 */
public class AddColor implements History {
    private final int mColor;

    public AddColor(int color) {
        mColor = color;
    }

    @Override
    public void undo(OnHistoryDo doHistory) {
        doHistory.removeColor(mColor);
    }

    @Override
    public void redo(OnHistoryDo doHistory) {
        doHistory.addColor(mColor);
    }
}
