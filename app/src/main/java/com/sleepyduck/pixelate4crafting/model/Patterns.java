package com.sleepyduck.pixelate4crafting.model;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by fredrik.metcalf on 2016-04-08.
 */
public class Patterns {
    public static final String PREFERENCE_NAME = "PATTERNS";
    private static final String PREF_COUNT = "COUNT";

    public static final String INTENT_EXTRA_ID = "EXTRA_ID";

    private static Map<Integer, Pattern> MAP = new HashMap<Integer, Pattern>();
    private static List<Pattern> LIST = new ArrayList<>();

    private Patterns() {}

    public static void Load(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, 0);
        int size = pref.getInt(PREF_COUNT, 0);
        for (int i = 0; i < size; ++i) {
            Pattern pattern = new Pattern(i, pref);
            MAP.put(pattern.Id, pattern);
            LIST.add(pattern);
            Collections.sort(LIST, new Comparator<Pattern>() {
                @Override
                public int compare(Pattern lhs, Pattern rhs) {
                    return lhs.getPaletteId() - rhs.getPaletteId();
                }
            });
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

    public static Pattern GetPatternAt(int id) {
        return LIST.get(id);
    }

    public static int Size() {
        return MAP.size();
    }
}
