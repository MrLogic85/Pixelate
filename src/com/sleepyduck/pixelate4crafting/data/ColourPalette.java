package com.sleepyduck.pixelate4crafting.data;

import java.util.ArrayList;
import java.util.List;

public class ColourPalette {
	public final int Id;
	public final String Title;
	private List<Integer> mColours;

	public ColourPalette(String title) {
		Id = (int) Math.random() * Integer.MAX_VALUE;
		Title = title;
		mColours = new ArrayList<Integer>();
	}

	public ColourPalette(int id, String title) {
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
