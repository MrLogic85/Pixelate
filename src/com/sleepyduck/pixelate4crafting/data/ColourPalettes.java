package com.sleepyduck.pixelate4crafting.data;

import java.util.HashMap;
import java.util.Set;

import android.graphics.Color;

public class ColourPalettes extends HashMap<Integer, ColourPalette> {
	private static final long serialVersionUID = 6731564352874914461L;
	
	private static ColourPalettes Instance = new ColourPalettes();
	
	private ColourPalettes () {
		super();
	}
	
	public static void Load() {		
		ColourPalette palette = new ColourPalette(0, "Black & White");
		palette.addAllColours(Color.BLACK,Color.WHITE);
		Add(palette);

		palette = new ColourPalette(1, "Colours");
		palette.addAllColours(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.BLACK, Color.WHITE);
		Add(palette);
	}

	public static void Add(ColourPalette palette) {
		Instance.put(palette.Id, palette);
	}
	
	public static ColourPalette Get(int id) {
		return Instance.get(id);
	}

	public static Set<Integer> GetIds() {
		return Instance.keySet();
	}
}
