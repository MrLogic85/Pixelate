package com.sleepyduck.pixelate4crafting.testing.espresso;

import android.support.test.espresso.InjectEventSecurityException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.sleepyduck.pixelate4crafting.util.BetterLog;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Created by fredrikmetcalf on 02/02/17.
 */

class CustomEspresso {
    static Matcher<View> withRecyclerSize(final int size) {
        return new TypeSafeMatcher<View>() {
            @Override public boolean matchesSafely (final View view) {
                return ((RecyclerView) view).getChildCount () == size;
            }

            @Override public void describeTo (final Description description) {
                description.appendText ("RecyclerView should have " + size + " children");
            }
        };
    }

    static ViewAction deleteText(final int count) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return new BaseMatcher<View>() {
                    @Override
                    public boolean matches(Object item) {
                        return item instanceof EditText;
                    }

                    @Override
                    public void describeMismatch(Object item, Description mismatchDescription) {
                        mismatchDescription.appendText("" + item.getClass().getSimpleName() + " is not a EditTExt");
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendText("Matching object should be of type TextView");
                    }
                };
            }

            @Override
            public String getDescription() {
                return "Affected view should be of type EditText";
            }

            @Override
            public void perform(UiController uiController, View view) {
                TextView textView = (TextView) view;
                String title = textView.getText().toString();
                textView.setText(title.substring(0, title.length() - count));
            }
        };
    }
}
