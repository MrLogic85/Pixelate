package com.sleepyduck.pixelate4crafting.view.pattern;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ListPatternView extends LinearLayout {
	private List<ListPatternItemView> mPatternViews = new ArrayList<ListPatternItemView>();

	public ListPatternView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ListPatternView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ListPatternView(Context context) {
		super(context);
	}

	public void addPattern(ListPatternItemView item) {
		mPatternViews.add(item);
		addView(item, item.getLayoutParams());
	}

}
