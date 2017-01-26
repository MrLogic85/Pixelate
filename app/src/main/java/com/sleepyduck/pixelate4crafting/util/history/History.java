package com.sleepyduck.pixelate4crafting.util.history;

/**
 * Created by fredrikmetcalf on 21/04/16.
 */
public interface History {
    void undo(OnHistoryDo doHistory);
    void redo(OnHistoryDo doHistory);
}
