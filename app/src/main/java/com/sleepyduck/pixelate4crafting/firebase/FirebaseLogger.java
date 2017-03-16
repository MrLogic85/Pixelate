package com.sleepyduck.pixelate4crafting.firebase;

import android.app.Activity;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.sleepyduck.pixelate4crafting.BuildConfig;

/**
 * Created by fredrikmetcalf on 02/02/17.
 */

public class FirebaseLogger {
    private static final String EVENT_PATTERN_CREATED = "pattern_created";
    private static final String EVENT_PATTERN_DELETED = "pattern_deleted";
    private static final String EVENT_PATTERN_OPEN = "pattern_open";
    private static final String EVENT_PIXEL_CHANGED = "pixel_changed";
    private static final String EVENT_COLOR_REMOVED = "color_removed";
    private static final String EVENT_COLOR_ADDED = "color_added";
    private static final String EVENT_NAME_CHANGED = "name_changed";
    private static final String EVENT_SIZE_CHANGED = "size_changed";
    private static final String EVENT_RECEIVE_IMAGE = "receive_image";

    private static final String PARAM_WIDTH_OLD = "width_old";
    private static final String PARAM_WIDTH_NEW = "width_new";
    private final FirebaseAnalytics mFirebase;

    public static FirebaseLogger getInstance(Activity activity) {
        return new FirebaseLogger(FirebaseAnalytics.getInstance(activity));
    }

    private FirebaseLogger(FirebaseAnalytics instance) {
        mFirebase = instance;
        mFirebase.setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);
    }

    public void patternCreated() {
        mFirebase.logEvent(EVENT_PATTERN_CREATED, new Bundle());
    }

    public void patternDeleted() {
        mFirebase.logEvent(EVENT_PATTERN_DELETED, new Bundle());
    }

    public void patternOpened() {
        mFirebase.logEvent(EVENT_PATTERN_OPEN, new Bundle());
    }

    public void pixelChanged() {
        mFirebase.logEvent(EVENT_PIXEL_CHANGED, new Bundle());
    }

    public void colorRemoved() {
        mFirebase.logEvent(EVENT_COLOR_REMOVED, new Bundle());
    }

    public void colorAdded() {
        mFirebase.logEvent(EVENT_COLOR_ADDED, new Bundle());
    }

    public void nameChanged() {
        mFirebase.logEvent(EVENT_NAME_CHANGED, new Bundle());
    }

    public void sizeChanged(int newWidth, int fromWidth) {
        Bundle bundle = new Bundle();
        bundle.putInt(PARAM_WIDTH_OLD, fromWidth);
        bundle.putInt(PARAM_WIDTH_NEW, newWidth);
        mFirebase.logEvent(EVENT_SIZE_CHANGED, bundle);
    }

    public void logShareReceived(int itemCount) {
        Bundle bundle = new Bundle();
        bundle.putInt(FirebaseAnalytics.Param.VALUE, itemCount);
        mFirebase.logEvent(EVENT_RECEIVE_IMAGE, bundle);
    }
}
