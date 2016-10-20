package com.sleepyduck.pixelate4crafting.model;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by fredrikmetcalf on 20/10/16.
 */

public final class DatabaseContract {

    private DatabaseContract() {}

    public static class PatternColumns implements BaseColumns {
        public static final String TABLE_NAME = "patterns";

        public static final Uri URI = Uri.parse(DatabaseProvider.AUTHORITY + "/" + TABLE_NAME);

        public static final String TITLE = "title";

        public static final String STATE = "state";
        public static final int STATE_ACTIVE = 1;
        public static final int STATE_COMPLETED = 2;

        public static final String FILE = "file";

        public static final String FILE_THUMB = "file_thumb";

        public static final String FILE_PATTERN = "file_pattern";

        public static final String PIXEL_WIDTH = "pixel_width";

        public static final String PIXEL_HEIGHT = "pixel_height";

        /**
         * Last time the pattern was accessed, used for sorting
         */
        public static final String TIME = "time";
    }
}
