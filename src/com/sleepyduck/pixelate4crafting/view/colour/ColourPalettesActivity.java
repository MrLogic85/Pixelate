package com.sleepyduck.pixelate4crafting.view.colour;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.R.layout;
import com.sleepyduck.pixelate4crafting.data.ColourPalettes;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

public class ColourPalettesActivity extends Activity {

	private ViewGroup mPaletteList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_colour_palettes);

		mPaletteList = (ViewGroup) findViewById(android.R.id.list);
		for (int id : ColourPalettes.GetIds()) {
			mPaletteList.addView(new ColourPaletteItem(this, ColourPalettes
					.Get(id)));
		}
	}
}
