package com.sleepyduck.pixelate4crafting.testing.espresso;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.sleepyduck.pixelate4crafting.util.Callback;
import com.sleepyduck.pixelate4crafting.view.CircleColorView;
import com.sleepyduck.pixelate4crafting.view.ColorEditList;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Created by fredrikmetcalf on 02/02/17.
 */

class CustomEspresso {
    static Matcher<View> withSize(final int size) {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(final View view) {
                if (view instanceof ViewGroup) {
                    return size == ((ViewGroup) view).getChildCount();
                }
                return false;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("ViewGroup should have " + size + " children");
            }
        };
    }

    static Matcher<View> withSize(final int sizeMin, final int sizeMax) {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(final View view) {
                if (view instanceof ViewGroup) {
                    int size = ((ViewGroup) view).getChildCount();
                    return sizeMin <= size && sizeMax >= size;
                }
                return false;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("ViewGroup should have between " + sizeMin + " and " + sizeMax);
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

    static ViewAction clickChild(final int pos) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return new BaseMatcher<View>() {
                    @Override
                    public boolean matches(Object item) {
                        return item instanceof ViewGroup;
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendText("Item must be of type ViewGroup");
                    }
                };
            }

            @Override
            public String getDescription() {
                return "Item must be of type ViewGroup";
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (view instanceof AdapterView<?>) {
                    AdapterView adapterView = (AdapterView) view;
                    adapterView.performItemClick(view, pos, adapterView.getItemIdAtPosition(pos));
                } else if (view instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) view;
                    viewGroup.getChildAt(pos).performClick();
                }
            }
        };
    }

    static ViewAction getCount(final Callback<Integer> callback) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return new BaseMatcher<View>() {
                    @Override
                    public boolean matches(Object item) {
                        return item instanceof ViewGroup;
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendText("Item must be of type GridView");
                    }
                };
            }

            @Override
            public String getDescription() {
                return "Item must be of type GridView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (view instanceof ViewGroup) {
                    callback.onCallback(((ViewGroup) view).getChildCount());
                }
            }
        };
    }

    static ViewAction clickOnColor(final int index) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return new BaseMatcher<View>() {
                    @Override
                    public boolean matches(Object item) {
                        return item instanceof CircleColorView
                                || item instanceof ColorEditList;
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendText("Item must be of type CircleColorView or ColorEditList");
                    }
                };
            }

            @Override
            public String getDescription() {
                return "Item must be of type CircleColorView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (view instanceof CircleColorView) {
                    CircleColorView circleColorView = (CircleColorView) view;
                    circleColorView.selectColor(index);
                } else if (view instanceof ColorEditList) {
                    ColorEditList colorEditList = (ColorEditList) view;
                    colorEditList.selectItem(index);
                }
            }
        };
    }

    static ViewAction clickXY(final float xPercent, final float yPercent) {
        return new GeneralClickAction(
                Tap.SINGLE,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        final int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);
                        final float screenX = screenPos[0] + view.getWidth() * xPercent;
                        final float screenY = screenPos[1] + view.getHeight() * yPercent;
                        return new float[]{screenX, screenY};
                    }
                },
                Press.FINGER);
    }
}
