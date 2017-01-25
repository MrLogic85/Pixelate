package com.sleepyduck.pixelate4crafting.view.recycler;

import android.support.v7.widget.DefaultItemAnimator;

/**
 * Created by fredrikmetcalf on 25/01/17.
 */

public class ItemAnimator extends DefaultItemAnimator {
    public ItemAnimator() {
        super();
        setSupportsChangeAnimations(false);
    }
}
