package com.sleepyduck.pixelate4crafting.util.history;

/**
 * Created by fredrikmetcalf on 21/04/16.
 */
public interface OnHistoryDo {
    void removeColor(int color);
    void addColor(int color);
    void setWidth(int width);
}
