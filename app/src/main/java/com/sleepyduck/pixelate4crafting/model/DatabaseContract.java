package com.sleepyduck.pixelate4crafting.model;

import android.net.Uri;
import android.provider.BaseColumns;

public final class DatabaseContract {

    private DatabaseContract() {}

    public static class PatternColumns implements BaseColumns {
        public static final String TABLE_NAME = "patterns";

        public static final Uri URI = Uri.parse("content://" + DatabaseProvider.AUTHORITY + "/" + TABLE_NAME);

        public static final String TITLE = "title";

        /**
         * Flag used to keep track of active and completed patterns
         */
        public static final String STATE = "state";
        public static final int STATE_LATEST = 0;
        public static final int STATE_ACTIVE = 1;
        public static final int STATE_COMPLETED = 2;

        public static final String FILE = "file";

        public static final String FILE_THUMB = "file_thumb";

        /**
         * Path to png file of a bitmap drawn as PIXELS
         */
        public static final String FILE_PATTERN = "file_pattern";

        public static final String WIDTH = "width";

        public static final String HEIGHT = "height";

        /**
         * Weighted Color map of all colors used in the pattern
         */
        public static final String COLORS = "colors";

        /**
         * Pixel map of all the colors in COLORS drawn as the image
         */
        public static final String PIXELS = "pixels";

        /**
         * Weighted Color map of all colors used in the pattern
         */
        public static final String CHANGED_PIXELS = "changed_pixels";

        /**
         * Flag for what needs to be done with the pattern
         */
        public static final String FLAG = "flag";
        public static final int FLAG_UNKNOWN = 0;
        public static final int FLAG_STORING_IMAGE = 1;
        public static final int FLAG_IMAGE_STORED = 2;
        public static final int FLAG_SIZE_OR_COLOR_CHANGING = 3;
        public static final int FLAG_SIZE_OR_COLOR_CHANGED = 4;
        public static final int FLAG_COLORS_CALCULATING = 5;
        public static final int FLAG_COLORS_CALCULATED = 6;
        public static final int FLAG_PIXELS_CALCULATING = 7;
        public static final int FLAG_PIXELS_CALCULATED = 8;
        public static final int FLAG_PATTERN_DRAWING = 9;
        public static final int FLAG_COMPLETE = 10;

        /**
         * Last time the mPattern was accessed, used for sorting
         */
        public static final String TIME = "time";

        public static final String PROGRESS = "progress";
    }
}
