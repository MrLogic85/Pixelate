package com.sleepyduck.pixelate4crafting.old;

import android.content.Context;
import android.graphics.Color;

import com.sleepyduck.pixelate4crafting.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ColorPalettes {
	private static Map<Integer, Palette> MAP = new HashMap<Integer, Palette>();

	private ColorPalettes() {
		super();
	}

	public static void Load(Context context) {
		Palette palette = new Palette(0, "Black & White");
		palette.addAllColours(Color.BLACK, Color.WHITE);
		Add(palette);

		palette = new Palette(1, "Colours");
		palette.addAllColours(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.BLACK, Color.WHITE);
		Add(palette);

		palette = new Palette(2, "Android");
		int[] colourId = new int[] { R.color.blue, R.color.blue_dark, R.color.purple, R.color.purple_dark,
				R.color.green, R.color.green_dark, R.color.yellow, R.color.yellow_dark, R.color.red, R.color.red_dark };
		for (int colour : colourId) {
			palette.addAllColours(context.getResources().getColor(colour));
		}
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

		public List<Integer> getColors() {
			return mColours;
		}
	}
}
