package com.example.pixelate4craftign;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ColourPalette extends LinearLayout {
	TextView mTitle;
	LinearLayout mColoursLayout;

	public ColourPalette(Context context) {
		super(context);
		setup();
	}

	public ColourPalette(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup();
	}

	private void setup() {
		setOrientation(LinearLayout.VERTICAL);
		mTitle = new TextView(getContext());
		mColoursLayout = new LinearLayout(getContext());
		mColoursLayout.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		mColoursLayout.setOrientation(LinearLayout.HORIZONTAL);
		addView(mTitle);
		addView(mColoursLayout);
	}

	public void addColours(int[] colours) {
		float colourSize = getResources().getDimension(R.dimen.colour_size);
		for (int color : colours) {
			View view = new View(getContext());
			view.setLayoutParams(new LayoutParams((int) colourSize,
					(int) colourSize));
			view.setBackgroundColor(color);
		}
	}

	public void setTitle(String title) {
		mTitle.setText(title);
	}
}
