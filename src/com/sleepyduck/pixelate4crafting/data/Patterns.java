package com.sleepyduck.pixelate4crafting.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Patterns {
	private static Map<Integer, Pattern> MAP = new HashMap<Integer, Pattern>();

	private Patterns() {

	}

	public static void Load() {
		Pattern pattern1 = new Pattern(0, "Test pattern 1");
		Pattern pattern2 = new Pattern(1, "Test pattern 2");
		Add(pattern1);
		Add(pattern2);
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

	public static class Pattern {
		public final int Id;
		public final String Title;

		public Pattern(int id, String title) {
			Id = id;
			Title = title;
		}

	}

}
