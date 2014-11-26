package com.sleepyduck.pixelate4crafting.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sleepyduck.pixelate4crafting.BetterLog;
import com.sleepyduck.pixelate4crafting.data.Patterns.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Patterns {
	public static final String PREFERENCE_NAME = "PATTERNS";
	private static final String PREF_COUNT = "COUNT";
	private static final String PREF_ID = "ID";
	private static final String PREF_TITLE = "TITLE";
	private static final String PREF_PALETTE = "PALETTE";
	private static final String PREF_FILE = "FILE";
	private static final String PREF_FILE_THUMB = "FILE_THUMB";
	private static final String PREF_PIXEL_WIDTH = "PIXEL_WIDTH";
	private static final String PREF_PIXEL_HEIGHT = "PIXEL_HEIGHT";

	public static final String INTENT_EXTRA_ID = "EXTRA_ID";

	private static Map<Integer, Pattern> MAP = new HashMap<Integer, Pattern>();

	private Patterns() {}

	public static void Load(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, 0);
		int size = pref.getInt(PREF_COUNT, 0);
		for (int i = 0; i < size; ++i) {
			Pattern pattern = new Pattern(i, pref);
			MAP.put(pattern.Id, pattern);
		}
	}

	public static void Save(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(PREF_COUNT, MAP.size());
		List<Pattern> patterns = new ArrayList<>(MAP.values());
		for (int i = 0; i < patterns.size(); ++i) {
			patterns.get(i).save(i, editor);
		}
		editor.commit();
	}

	public static void Add(Pattern pattern) {
		MAP.put(pattern.Id, pattern);
	}

	public static void Remove(Pattern pattern) {
		MAP.remove(pattern.Id);
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
		private String mTitle = "";
		private int mPaletteId = -1;
		private String mFileName = "";
		private String mFileNameThumb = "";
		private int mPixelWidth = Constants.DEFAULT_PIXELS;
		private int mPixelHeight = Constants.DEFAULT_PIXELS;

		public Pattern(String title) {
			Id = (int) (Math.random() * Integer.MAX_VALUE);
			mTitle = title;
		}

		public Pattern(int prefCounter, SharedPreferences pref) {
			Id = pref.getInt("" + prefCounter + PREF_ID, -1);
			mTitle = pref.getString("" + prefCounter + PREF_TITLE, mTitle);
			mPaletteId = pref.getInt("" + prefCounter + PREF_PALETTE, mPaletteId);
			mFileName = pref.getString("" + prefCounter + PREF_FILE, mFileName);
			mFileNameThumb = pref.getString("" + prefCounter + PREF_FILE_THUMB, mFileNameThumb);
			mPixelWidth = pref.getInt("" + prefCounter + PREF_PIXEL_WIDTH, mPixelWidth);
			mPixelHeight = pref.getInt("" + prefCounter + PREF_PIXEL_HEIGHT, mPixelHeight);
		}

		public void save(int prefCounter, Editor editor) {
			editor.putInt("" + prefCounter + PREF_ID, Id);
			editor.putString("" + prefCounter + PREF_TITLE, mTitle);
			editor.putInt("" + prefCounter + PREF_PALETTE, mPaletteId);
			editor.putString("" + prefCounter + PREF_FILE, mFileName);
			editor.putString("" + prefCounter + PREF_FILE_THUMB, mFileNameThumb);
			editor.putInt("" + prefCounter + PREF_PIXEL_WIDTH, mPixelWidth);
			editor.putInt("" + prefCounter + PREF_PIXEL_HEIGHT, mPixelHeight);
		}

		@Override
		public int compareTo(Pattern another) {
			return this.mTitle.compareTo(another.mTitle);
		}

		public void setPaletteId(int id) {
			mPaletteId = id;
		}

		public int getPaletteId() {
			return mPaletteId;
		}

		public void setFileName(String fileName) {
			mFileName = fileName;
			setFileNameThumbnail(fileName + Constants.FILE_THUMBNAIL);
		}

		public String getFileName() {
			return mFileName;
		}

		public void setFileNameThumbnail(String fileName) {
			mFileNameThumb = fileName;
		}

		public String getFileNameThumbnail() {
			return mFileNameThumb;
		}

		public String getTitle() {
			return mTitle;
		}

		public void setTitle(String title) {
			this.mTitle = title;
		}

		public static String createTitleFromFileName(String fileName) {
			BetterLog.d(Pattern.class, fileName);
			String tmp = fileName.split("\\.")[0];
			BetterLog.d(Pattern.class, tmp);
			tmp = tmp.replaceAll("_", " ");
			BetterLog.d(Pattern.class, tmp);
			return tmp;
		}

		public void setPixelWidth(int i) {
			mPixelWidth = i;
		}

		public void setPixelHeight(int i) {
			mPixelHeight = i;
		}

		public int getPixelWidth() {
			return mPixelWidth;
		}

		public int getPixelHeight() {
			return mPixelHeight;
		}
	}

}
