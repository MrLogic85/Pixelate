package com.sleepyduck.pixelate4crafting.view.color;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.data.ColorPalettes;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

public class ColorPalettesActivity extends Activity {

	private ViewGroup mPaletteList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_color_palettes);

		mPaletteList = (ViewGroup) findViewById(android.R.id.list);
		for (int id : ColorPalettes.GetIds()) {
			mPaletteList.addView(new ColorPaletteItem(this, ColorPalettes
					.Get(id)));
		}
	}
}
