package com.sleepyduck.pixelate4crafting.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sleepyduck.pixelate4crafting.view.pattern.ListPatternItemView;

import android.util.AttributeSet;

public class Patterns {
	public static final String INTENT_EXTRA_ID = "EXTRA_ID";
	
	private static Map<Integer, Pattern> MAP = new HashMap<Integer, Pattern>();

	private Patterns() {

	}

	public static void Load() {
		Set<Integer> ids = ColorPalettes.GetIds();
		Iterator<Integer> iterator = ids.iterator();
		for (int i = 0; i < 6; i++) {
			Pattern pattern = new Pattern(i, "Test pattern " + i);
			pattern.setPaletteId(iterator.next());
			if (!iterator.hasNext()) {
				iterator = ids.iterator();
			}
			Add(pattern);
		}
	}

	public static void Add(Pattern pattern) {
		MAP.put(pattern.Id, pattern);
	}

	public static Set<Integer> GetIds() {
		return MAP.keySet();
	}

	public static Collection<Pattern> GetPatterns() {
		return MAP.values();
	}

	public static Pattern GetPattern(int id) {
		return MAP.get(id);
	}

	public static class Pattern implements Comparable<Pattern> {
		public final int Id;
		public final String Title;

		private int mPaletteId = -1;

		public Pattern(int id, String title) {
			Id = id;
			Title = title;
		}

		public void setPaletteId(int id) {
			mPaletteId = id;
		}

		public int getPaletteId() {
			return mPaletteId;
		}

		@Override
		public int compareTo(Pattern another) {
			return this.Title.compareTo(another.Title);
		}

	}

}
