package com.sleepyduck.pixelate4crafting.view.colour;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.R.layout;
import com.sleepyduck.pixelate4crafting.data.ColorPalettes;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

public class ColourPalettesActivity extends Activity {

	private ViewGroup mPaletteList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_colour_palettes);

		mPaletteList = (ViewGroup) findViewById(android.R.id.list);
		for (int id : ColorPalettes.GetIds()) {
			mPaletteList.addView(new ColourPaletteItem(this, ColorPalettes
					.Get(id)));
		}
	}
}
