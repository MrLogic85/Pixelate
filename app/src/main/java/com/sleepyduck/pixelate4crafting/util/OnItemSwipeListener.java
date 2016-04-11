package com.sleepyduck.pixelate4crafting.util;

import android.view.View;

public interface OnItemSwipeListener {
	/**
	 * 
	 * @param view
	 * @return true if the view has been removed from the LinearLayoutFling
	 */
	public boolean onItemSwipe(View view);
}
