package com.sleepyduck.pixelate4crafting.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.sleepyduck.pixelate4crafting.util.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sleepyduck.pixelate4crafting.model.Pattern.State.ACTIVE;
import static com.sleepyduck.pixelate4crafting.model.Pattern.State.LATEST;

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

    public static synchronized void Load(Context context) {
        if (LIST.size() == 0) {
            SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, 0);
            int size = pref.getInt(PREF_COUNT, 0);
            for (int i = 0; i < size; ++i) {
                Pattern pattern = new Pattern(i, pref);
                Add(pattern);
            }
        }
    }

    public static synchronized void Save(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(PREF_COUNT, MAP.size());
        List<Pattern> patterns = new ArrayList<>(MAP.values());
        for (int i = 0; i < patterns.size(); ++i) {
            patterns.get(i).save(i, editor);
        }
        editor.commit();
    }

    public static synchronized void Add(Pattern pattern) {
        MAP.put(pattern.Id, pattern);
        LIST.add(pattern);
        Sort();
    }

    public static synchronized void Remove(Pattern pattern) {
        LIST.remove(pattern);
        MAP.remove(pattern.Id);
    }

    public static synchronized void GetPatterns(Callback<Pattern> callback) {
        for (Pattern pattern : LIST) {
            callback.onCallback(pattern);
        }
    }

    private static void Sort() {
        Collections.sort(LIST, new Comparator<Pattern>() {
            @Override
            public int compare(Pattern lhs, Pattern rhs) {
                return rhs.getWeight() - lhs.getWeight();
            }
        });
    }

    public static synchronized Pattern GetPattern(int id) {
        return MAP.get(id);
    }

    public static synchronized void GetPatternsOfState(Pattern.State state, Callback<Pattern> callback) {
        for (Pattern pattern : LIST) {
            if (pattern.getState() == state) {
                callback.onCallback(pattern);
            }
        }
    }

    public static synchronized void GetPatternAt(Pattern.State state, int position, Callback<Pattern> callback) {
        int count = 0;
        for (Pattern pattern : LIST) {
            if (pattern.getState() == state) {
                if (count == position) {
                    callback.onCallback(pattern);
                    return;
                }
                count++;
            }
        }
    }

    public static synchronized int Count(Pattern.State state) {
        int count = 0;
        for (Pattern pattern : LIST) {
            if (pattern.getState() == state) {
                count++;
            }
        }
        return count;
    }

    public static synchronized void MakeLatest(Pattern pattern) {
        GetPatternsOfState(LATEST, new Callback<Pattern>() {
            @Override
            public void onCallback(Pattern obj) {
                obj.setState(ACTIVE);
            }
        });
        final int weight = GetHighestWeight() + 1;
        pattern.setWeight(weight);
        Sort();

        // Keep weights below 1000 to avoid too large numbers
        if (weight > 1000) {
            GetPatterns(new Callback<Pattern>() {
                @Override
                public void onCallback(Pattern obj) {
                    obj.setWeight(obj.getWeight() - weight);
                }
            });
        }

        pattern.setState(LATEST);
    }

    public static int GetHighestWeight() {
        final int[] weight = {Integer.MIN_VALUE};
        GetPatterns(new Callback<Pattern>() {
            @Override
            public void onCallback(Pattern obj) {
                weight[0] = Math.max(weight[0], obj.getWeight());
            }
        });
        return weight[0];
    }
}
