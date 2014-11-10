package com.sleepyduck.pixelate4crafting.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.graphics.Color;

public class ColourPalettes {
	private static Map<Integer, Palette> MAP = new HashMap<Integer, Palette>();

	private ColourPalettes() {
		super();
	}

	public static void Load() {
		Palette palette = new Palette(0, "Black & White");
		palette.addAllColours(Color.BLACK, Color.WHITE);
		Add(palette);

		palette = new Palette(1, "Colours");
		palette.addAllColours(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
				Color.BLACK, Color.WHITE);
		Add(palette);
	}

	public static void Add(Palette palette) {
		MAP.put(palette.Id, palette);
	}

	public static Palette Get(int id) {
		return MAP.get(id);
	}

	public static Set<Integer> GetIds() {
		return MAP.keySet();
	}

	public static class Palette {
		public final int Id;
		public final String Title;
		private List<Integer> mColours;

		public Palette(String title) {
			Id = (int) Math.random() * Integer.MAX_VALUE;
			Title = title;
			mColours = new ArrayList<Integer>();
		}

		public Palette(int id, String title) {
			Id = id;
			Title = title;
			mColours = new ArrayList<Integer>();
		}

		public void addColour(int colour) {
			mColours.add(colour);
		}

		public void addAllColours(int... colours) {
			for (int colour : colours) {
				mColours.add(colour);
			}
		}

		public List<Integer> getColours() {
			return mColours;
		}
	}
}
