package com.example.pixelate4craftign;

import android.support.v7.app.ActionBarActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;

public class ColourPalettesActivity extends ActionBarActivity {

	private ViewGroup mPaletteList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_colour_palettes);
		
		mPaletteList = (ViewGroup) findViewById(android.R.id.list);
		
		ColourPalette paletteBW = new ColourPalette(this);
		paletteBW.addColours(new int[] { Color.BLACK, Color.WHITE });
		paletteBW.setTitle("Black & White");
		mPaletteList.addView(paletteBW);
		
		ColourPalette paletteRGB = new ColourPalette(this);
		paletteRGB.addColours(new int[] { Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.BLACK, Color.WHITE });
		paletteRGB.setTitle("Colours");
		mPaletteList.addView(paletteRGB);
	}
}
