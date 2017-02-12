package com.sleepyduck.pixelate4crafting.testing.espresso;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.activity.MainActivity;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract;
import com.sleepyduck.pixelate4crafting.model.DatabaseManager;
import com.sleepyduck.pixelate4crafting.model.Pattern;
import com.sleepyduck.pixelate4crafting.service.CalculateService;
import com.sleepyduck.pixelate4crafting.util.Callback;
import com.sleepyduck.pixelate4crafting.view.PatternInteractiveView;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasCategories;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasType;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.sleepyduck.pixelate4crafting.testing.espresso.CustomEspresso.clickChild;
import static com.sleepyduck.pixelate4crafting.testing.espresso.CustomEspresso.clickOnColor;
import static com.sleepyduck.pixelate4crafting.testing.espresso.CustomEspresso.clickXY;
import static com.sleepyduck.pixelate4crafting.testing.espresso.CustomEspresso.deleteText;
import static com.sleepyduck.pixelate4crafting.testing.espresso.CustomEspresso.getCount;
import static com.sleepyduck.pixelate4crafting.testing.espresso.CustomEspresso.withSize;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by fredrikmetcalf on 02/02/17.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EspressoTests {
    private static final String GET_CONTENT_TYPE = "image/*";
    private static final Set<String> GET_CONTENT_CATEGORIES = new HashSet<>();

    private WaitForPatternComplete idlingResource;

    static {
        GET_CONTENT_CATEGORIES.add(Intent.CATEGORY_OPENABLE);
    }

    public ViewInteraction onView(Matcher<View> matcher) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Espresso.onView(matcher);
    }

    @Rule
    public IntentsTestRule<MainActivity> mMainActivityRule =
            new IntentsTestRule<>(MainActivity.class);

    @Before
    public void setup() {
        Intent intent = new Intent();
        intent.setData(Uri.parse("nightmare_test_image.jpg"));
        intending(allOf(
                hasAction(Intent.ACTION_GET_CONTENT),
                hasCategories(GET_CONTENT_CATEGORIES),
                hasType(GET_CONTENT_TYPE)))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, intent));

        IdlingPolicies.setIdlingResourceTimeout(5, TimeUnit.MINUTES);
        IdlingPolicies.setMasterPolicyTimeout(5, TimeUnit.MINUTES);
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        idlingResource = new WaitForPatternComplete(instrumentation.getTargetContext());
        Espresso.registerIdlingResources(idlingResource);
    }

    @After
    public void tearDown() {
        idlingResource.stopLoader();
        Espresso.unregisterIdlingResources(idlingResource);
        mMainActivityRule.getActivity().bindService(new Intent(mMainActivityRule.getActivity(), CalculateService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                ((CalculateService.Binder) binder).getService().stop();
                mMainActivityRule.getActivity().unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Service.BIND_AUTO_CREATE);
    }

    private void clearPatterns() {
        Pattern[] patterns = DatabaseManager.getPatterns(mMainActivityRule.getActivity());
        for (Pattern pattern : patterns) {
            pattern.delete();
        }

        if (patterns.length > 0) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unused")
    private ViewInteraction onViewAfter(int pause, Matcher<View> viewMatcher) {
        try {
            Thread.sleep(pause);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return onView(viewMatcher);
    }

    @Test
    public void testA_pickImage() {
        clearPatterns();

        // Check that recycler is empty
        onView(withId(R.id.recycler)).check(matches(withSize(0)));

        // Remove idling resource
        Espresso.unregisterIdlingResources(idlingResource);

        // Click add button twice
        onView(withId(R.id.fab_add)).perform(click());
        onView(withId(R.id.fab_add)).perform(click());

        // Readd idling resource
        Espresso.registerIdlingResources(idlingResource);

        // Check that a pattern was added
        onView(withId(R.id.recycler)).check(matches(withSize(2)));

        // Delete one
        Pattern[] patterns = DatabaseManager.getPatterns(mMainActivityRule.getActivity());
        patterns[1].delete();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check that a pattern was added
        onView(withId(R.id.recycler)).check(matches(withSize(1)));
    }

    @Test
    public void testB_openPattern() {
        // Open pattern
        onView(withId(R.id.card)).perform(click());

        // Verify that the activity opened
        onView(withId(R.id.canvas)).check(matches(isDisplayed()));

        // Go back to main
        pressBack();
    }

    @Test
    public void testC_editTitle() {
        // Verify title
        String title = Pattern.createTitleFromFileName("nightmare_test_image.jpg");
        onView(withId(R.id.title)).check(matches(withText(title)));

        // Open pattern
        onView(withId(R.id.card)).perform(click());

        // Open Menu
        onView(withId(R.id.fab)).perform(click());

        // Verify menu open
        onView(withId(R.id.edit_menu_card)).check(matches(isDisplayed()));

        // Click Change Name
        onView(withId(R.id.menu_change_name)).perform(click());

        // Verify title focus
        onView(withId(R.id.editable_title)).check(matches(hasFocus()));

        // Verify title
        onView(withId(R.id.editable_title)).check(matches(withText(title)));

        // Erase 3 characters
        onView(withId(R.id.editable_title)).perform(deleteText(3));

        // Verify name change
        title = title.substring(0, title.length() - 3);
        onView(withId(R.id.editable_title)).check(matches(withText(title)));

        // Close keyboard
        closeSoftKeyboard();

        // Press done
        onView(withId(R.id.fab)).perform(click());

        // Press back
        onView(withId(R.id.canvas)).perform(pressBack());

        // Check that only one pattern exists
        onView(withId(R.id.recycler)).check(matches(withSize(1)));

        // Verify title
        onView(withId(R.id.title)).check(matches(withText(title)));
    }

    @Test
    public void testD_testEditColors() {
        // Open pattern
        onView(withId(R.id.card)).perform(click());

        // Open Menu
        onView(withId(R.id.fab)).perform(click());

        // Verify menu open
        onView(withId(R.id.edit_menu_card)).check(matches(isDisplayed()));

        // Click Change Colors
        onView(withId(R.id.menu_change_colors)).perform(click());

        // Verify that ChangeParametersActivity is started
        onView(withId(R.id.image_approximated)).check(matches(isDisplayed()));

        // Open colors by clicking a color
        onView(withId(R.id.palette_grid)).perform(clickChild(0));

        // Count colors
        final int[] count = {0};
        onView(withId(R.id.palette_grid)).perform(getCount(new Callback<Integer>() {
            @Override
            public void onCallback(Integer obj) {
                count[0] = obj;
            }
        }));

        // Click the first color
        onView(withId(R.id.palette_grid)).perform(clickChild(0));

        // Check color count
        onView(withId(R.id.palette_grid)).check(matches(withSize(count[0] - 1)));

        // Verify Pattern color count
        Pattern[] patterns = DatabaseManager.getPatterns(mMainActivityRule.getActivity());
        assertTrue(patterns.length == 1);
        assertTrue(patterns[0].getColorCount() == count[0] - 1);
    }

    @Test
    public void testE_testEditPixels() {
        // Check that a pattern was added
        onView(withId(R.id.recycler)).check(matches(withSize(1)));

        // Open pattern
        onView(withId(R.id.card)).perform(click());

        // Open menu
        onView(withId(R.id.fab)).perform(click());

        // Verify menu open
        onView(withId(R.id.edit_menu_card)).check(matches(isDisplayed()));

        // Click edit pixels
        onView(withId(R.id.menu_change_pixles)).perform(click());

        // Verify Color Palette open
        onView(withId(R.id.circle_color_view)).check(matches(isDisplayed()));

        // Verify Color Edit list visible
        onView(withId(R.id.color_edit_list_view)).check(matches(isDisplayed()));

        // Click on a color
        onView(withId(R.id.circle_color_view)).perform(clickOnColor(7));

        // Verify Color Palette closed
        onView(withId(R.id.circle_color_view)).check(matches(not(isDisplayed())));

        // Verify Color Edit list visible
        onView(withId(R.id.color_edit_list_view)).check(matches(isDisplayed()));

        // Verify Color Edit list size
        onView(withId(R.id.color_edit_list_view)).check(matches(withSize(3)));

        // Click done
        onView(withId(R.id.fab)).perform(click());

        // Verify Color Edit list not visible
        //onView(withId(R.id.color_edit_list_view)).check(matches(not(isCompletelyDisplayed())));

        // Verify Color Palette not visible
        onView(withId(R.id.circle_color_view)).check(matches(not(isDisplayed())));

        // Open menu
        onView(withId(R.id.fab)).perform(click());

        // Click edit pixels
        onView(withId(R.id.menu_change_pixles)).perform(click());

        // Verify Color Palette not visible
        onView(withId(R.id.circle_color_view)).check(matches(not(isDisplayed())));

        // Verify Color Edit list not visible
        //onView(withId(R.id.color_edit_list_view)).check(matches(not(isDisplayed())));

        // Verify Color Edit list size
        onView(withId(R.id.color_edit_list_view)).check(matches(withSize(3)));

        // Click "palette"
        onView(withId(R.id.color_edit_list_view)).perform(clickOnColor(0));

        // Verify Color Palette visible
        onView(withId(R.id.circle_color_view)).check(matches(isDisplayed()));

        // Click outside Palette
        onView(withId(R.id.circle_color_view)).perform(clickOnColor(-1));

        // Select color
        onView(withId(R.id.color_edit_list_view)).perform(clickOnColor(2));

        // Paint pixels
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.01f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.1f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.2f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.3f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.4f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.5f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.6f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.8f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.9f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.99f));
        onView(withId(R.id.canvas)).perform(clickXY(0.01f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.2f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.3f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.4f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.5f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.6f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.7f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.8f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.9f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.99f, 0.7f));

        // Press done
        onView(withId(R.id.fab)).perform(click());

        // Verify Pattern pixel edits
        Pattern[] patterns = DatabaseManager.getPatterns(mMainActivityRule.getActivity());
        assertTrue(patterns.length == 1);
        assertTrue(patterns[0].hasChangedPixels());
        assertTrue(patterns[0].getChangedPixelsCount() > 0);

        // Open menu
        onView(withId(R.id.fab)).perform(click());

        // Select edit pixels
        onView(withId(R.id.menu_change_pixles)).perform(click());

        // Select eraser
        onView(withId(R.id.color_edit_list_view)).perform(clickOnColor(1));

        // Erase pixels
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.01f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.1f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.2f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.3f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.4f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.5f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.6f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.8f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.9f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.99f));
        onView(withId(R.id.canvas)).perform(clickXY(0.01f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.1f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.2f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.3f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.4f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.5f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.6f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.7f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.8f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.9f, 0.7f));
        onView(withId(R.id.canvas)).perform(clickXY(0.99f, 0.7f));

        // Press done
        onView(withId(R.id.fab)).perform(click());

        // Verify Pattern pixel no edits
        patterns[0] = DatabaseManager.getPattern(mMainActivityRule.getActivity(), patterns[0].Id);
        assertFalse(patterns[0].hasChangedPixels());
    }

    @Test
    public void testF_changeSize() {
        // Check that a pattern was added
        onView(withId(R.id.recycler)).check(matches(withSize(1)));

        // Open pattern
        onView(withId(R.id.card)).perform(click());

        // Open menu
        onView(withId(R.id.fab)).perform(click());

        // Click change size
        onView(withId(R.id.menu_change_size)).perform(click());

        // Verify that dialog is visible
        onView(withId(R.id.done_button)).check(matches(isDisplayed()));

        // Click numberpicker to open edit text
        onView(withId(R.id.number_picker)).perform(click());

        // Change size
        onView(withId(R.id.number_edit_text)).perform(replaceText("40"));

        // Close keyboard
        closeSoftKeyboard();

        // Click done
        onView(withId(R.id.done_button)).perform(click());

        // Verify pattern size
        Pattern[] patterns = DatabaseManager.getPatterns(mMainActivityRule.getActivity());
        assertTrue(patterns[0].getPixelWidth() == 40);

        // Open menu
        onView(withId(R.id.fab)).perform(click());

        // Click change size
        onView(withId(R.id.menu_change_size)).perform(click());

        // Verify that size is correct
        onView(withId(R.id.number_picker)).check(matches(withText("40")));

        // Click done
        onView(withId(R.id.done_button)).perform(click());

        // Verify that flag is still COMPLETE
        patterns[0] = DatabaseManager.getPattern(mMainActivityRule.getActivity(), patterns[0].Id);
        assertTrue(patterns[0].getFlag() == DatabaseContract.PatternColumns.FLAG_COMPLETE);

        // Open menu
        onView(withId(R.id.fab)).perform(click());

        // Click edit pixels
        onView(withId(R.id.menu_change_pixles)).perform(click());

        // Choose a color
        onView(withId(R.id.circle_color_view)).perform(clickOnColor(0));

        // Paint a pixel
        onView(withId(R.id.canvas)).perform(clickXY(0.5f, 0.5f));

        // Click done
        onView(withId(R.id.fab)).perform(click());

        // Open menu
        onView(withId(R.id.fab)).perform(click());

        // Click change size
        onView(withId(R.id.menu_change_size)).perform(click());

        // Verify that warning appeared
        onView(withText(R.string.warning_remove_custom_pixels)).check(matches(isDisplayed()));
    }

    @Test
    public void testG_addColor() {
        // Check that a pattern was added
        onView(withId(R.id.recycler)).check(matches(withSize(1)));

        // Open pattern
        onView(withId(R.id.card)).perform(click());

        // Open menu
        onView(withId(R.id.fab)).perform(click());

        // Click change colors
        onView(withId(R.id.menu_change_colors)).perform(click());

        // Verify that ChangeParametersActivity is started
        onView(withId(R.id.image_approximated)).check(matches(isDisplayed()));

        // Count colors
        final int[] count = {0, 0};
        onView(withId(R.id.palette_grid)).perform(getCount(new Callback<Integer>() {
            @Override
            public void onCallback(Integer obj) {
                count[0] = obj;
            }
        }));

        // Click on a spot on the image
        onView(withId(R.id.image_original)).perform(clickXY(0.5f, 0.5f));

        // Verify dialog open
        onView(withId(R.id.color_grid)).check(matches(isDisplayed()));

        // Verify that at least one color is lister
        onView(withId(R.id.color_grid)).check(matches(withSize(1, 9)));

        // Click on a color
        onView(withId(R.id.color_grid)).perform(clickChild(0));

        // Verify that dialog closed
        onView(withId(R.id.color_grid)).check(doesNotExist());

        // Count colors
        onView(withId(R.id.palette_grid)).perform(getCount(new Callback<Integer>() {
            @Override
            public void onCallback(Integer obj) {
                count[1] = obj;
            }
        }));

        // Verify that size is one larger
        assertTrue(count[0] == count[1] - 1);

        // Verify that pattern has stored the colors
        Pattern[] patterns = DatabaseManager.getPatterns(mMainActivityRule.getActivity());
        assertTrue(patterns[0].getColorCount() == count[1]);

        // Go back
        onView(withId(R.id.image_original)).perform(pressBack());

        // Go back
        onView(withId(R.id.canvas)).perform(pressBack());

        // Verify that the colors shown matches count
        onView(withId(R.id.color_recycler)).check(matches(withSize(count[1])));
    }

    @Test
    public void testH_shareImage() {
        clearPatterns();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        String mineType = "image/jpg";
        intent.setType(mineType);
        ClipData.Item item = new ClipData.Item(Uri.parse("nightmare_test_image.jpg"));
        ClipData clipData = new ClipData(new ClipDescription("", new String[]{mineType}), item);
        intent.setClipData(clipData);

        mMainActivityRule.getActivity().onNewIntent(intent);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify that pattern was added
        onView(withId(R.id.recycler)).check(matches(withSize(1)));
    }

    @Test
    public void testI_deleteAllColors() {
        // Check that a pattern was added
        onView(withId(R.id.recycler)).check(matches(withSize(1)));

        // Remove colors
        Pattern[] patterns = DatabaseManager.getPatterns(mMainActivityRule.getActivity());
        patterns[0].edit().setColors(new HashMap<Integer, Float>()).apply(false);

        // Verify that black and white was re added
        onView(withId(R.id.color_recycler)).check(matches(withSize(2)));
    }

    @Test
    public void testJ_setWidth500() {
        // Check that a pattern was added
        onView(withId(R.id.recycler)).check(matches(withSize(1)));

        // Open pattern
        onView(withId(R.id.card)).perform(click());

        // Open menu
        onView(withId(R.id.fab)).perform(click());

        // Click change size
        onView(withId(R.id.menu_change_size)).perform(click());

        // Verify that dialog is visible
        onView(withId(R.id.done_button)).check(matches(isDisplayed()));

        // Click numberpicker to open edit text
        onView(withId(R.id.number_picker)).perform(click());

        // Change size
        onView(withId(R.id.number_edit_text)).perform(replaceText("500"));

        // Close keyboard
        closeSoftKeyboard();

        // Click done
        onView(withId(R.id.done_button)).perform(click());

        // Scale to fit
        onView(withId(R.id.canvas)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return new BaseMatcher<View>() {
                    @Override
                    public boolean matches(Object item) {
                        return item instanceof PatternInteractiveView;
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendText(getDescription());
                    }
                };
            }

            @Override
            public String getDescription() {
                return "Item should be of type " + PatternInteractiveView.class.getSimpleName();
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (view instanceof PatternInteractiveView) {
                    ((PatternInteractiveView) view).scaleToFit();
                }
            }
        });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }

        // Click back
        onView(withId(R.id.canvas)).perform(pressBack());

        // Verify pattern size
        Pattern[] patterns = DatabaseManager.getPatterns(mMainActivityRule.getActivity());
        assertTrue(patterns[0].getPixelWidth() == 500);
    }

    @Test
    public void textX_deletePattern() {
        // Check that a pattern was added
        onView(withId(R.id.recycler)).check(matches(withSize(1)));

        // Swipe delete
        mMainActivityRule.getActivity().mOnRightSwipeListener.onSwipe(DatabaseManager.getPatterns(mMainActivityRule.getActivity())[0]);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify snackbar shown
        onView(withId(android.support.design.R.id.snackbar_text)).check(matches(isDisplayed()));

        // Verify pattern not shown
        onView(withId(R.id.recycler)).check(matches(withSize(0)));

        // Verify pattern exists and is pending delete
        try (Cursor cursor = mMainActivityRule.getActivity().getContentResolver().query(DatabaseContract.PatternColumns.URI, null, null, null, null)) {
            assertNotNull(cursor);
            assertEquals(1, cursor.getCount());
            cursor.moveToFirst();
            assertEquals(1, cursor.getInt(cursor.getColumnIndex(DatabaseContract.PatternColumns.PENDING_DELETE)));
        }

        // Undo
        onView(withId(android.support.design.R.id.snackbar_action)).perform(click());

        // Verify pattern visible
        onView(withId(R.id.recycler)).check(matches(withSize(1)));

        // Swipe delete
        mMainActivityRule.getActivity().mOnRightSwipeListener.onSwipe(DatabaseManager.getPatterns(mMainActivityRule.getActivity())[0]);

        // Dismiss snackbar
        onView(withId(android.support.design.R.id.snackbar_text)).perform(swipeRight());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify pattern deleted
        try (Cursor cursor = mMainActivityRule.getActivity().getContentResolver().query(DatabaseContract.PatternColumns.URI, null, null, null, null)) {
            assertNotNull(cursor);
            assertEquals(0, cursor.getCount());
        }
    }
}
