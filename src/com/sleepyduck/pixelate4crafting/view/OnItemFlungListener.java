package com.sleepyduck.pixelate4crafting.view;

import android.view.View;

public interface OnItemFlungListener {
	/**
	 * 
	 * @param view
	 * @return true if the view has been removed from the LinearLayoutFling
	 */
	public boolean onItemFlung(View view);
}
