package com.sleepyduck.pixelate4crafting.view.colour;

import java.util.List;

import com.sleepyduck.pixelate4crafting.BetterLog;
import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.R.dimen;
import com.sleepyduck.pixelate4crafting.data.ColourPalettes.Palette;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ColourPaletteItem extends LinearLayout {
	TextView mTitle;
	LinearLayout mColoursLayout;

	public ColourPaletteItem(Context context, Palette colourPalette) {
		super(context);
		setup();
		addColours(colourPalette.getColours());
		setTitle(colourPalette.Title);
	}

	public ColourPaletteItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup();
	}

	private void setup() {
		setOrientation(LinearLayout.VERTICAL);
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		mTitle = new TextView(getContext());
		mColoursLayout = new LinearLayout(getContext());
		mColoursLayout.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		mColoursLayout.setOrientation(LinearLayout.HORIZONTAL);
		addView(mTitle);
		addView(mColoursLayout);
	}

	public void addColours(List<Integer> list) {
		float colourSize = getResources().getDimension(R.dimen.colour_size);
		BetterLog.d(this, "addClours(): colourSize = " + colourSize);
		for (int color : list) {
			View view = new View(getContext());
			view.setLayoutParams(new LayoutParams((int) colourSize,
					(int) colourSize));
			view.setBackgroundColor(color);
			mColoursLayout.addView(view);
		}
	}

	public void setTitle(String title) {
		mTitle.setText(title);
	}
}
