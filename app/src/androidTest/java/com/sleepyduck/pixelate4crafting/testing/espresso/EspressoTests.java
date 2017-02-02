package com.sleepyduck.pixelate4crafting.testing.espresso;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.sleepyduck.pixelate4crafting.R;
import com.sleepyduck.pixelate4crafting.activity.MainActivity;
import com.sleepyduck.pixelate4crafting.model.DatabaseContract;
import com.sleepyduck.pixelate4crafting.model.Pattern;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasCategories;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasType;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.sleepyduck.pixelate4crafting.testing.espresso.CustomEspresso.deleteText;
import static com.sleepyduck.pixelate4crafting.testing.espresso.CustomEspresso.withRecyclerSize;
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

    static {
        GET_CONTENT_CATEGORIES.add(Intent.CATEGORY_OPENABLE);
        IdlingPolicies.setIdlingResourceTimeout(5, TimeUnit.MINUTES);
    }

    @Rule
    public IntentsTestRule<MainActivity> mMainActivityRule =
            new IntentsTestRule<>(MainActivity.class);

    @Before
    public void grantPermissions() {

    }

    @Before
    public void stubAddButtonIntent() {
        Intent intent = new Intent();
        intent.setData(Uri.parse("nightmare_test_image.jpg"));
        intending(allOf(
                hasAction(Intent.ACTION_GET_CONTENT),
                hasCategories(GET_CONTENT_CATEGORIES),
                hasType(GET_CONTENT_TYPE)))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, intent));
    }

    @Test
    public void testA_pickImage() {
        // Check that recycler is empty
        onView(withId(R.id.recycler)).check(matches(withRecyclerSize(0)));

        // Click add button
        onView(withId(R.id.fab_add)).perform(click());

        // Verify that the intent sent to correct
        intended(allOf(
                hasAction(Intent.ACTION_GET_CONTENT),
                hasCategories(GET_CONTENT_CATEGORIES),
                hasType(GET_CONTENT_TYPE)));

        // Check that a pattern was added
        onView(withId(R.id.recycler)).check(matches(withRecyclerSize(1)));

        // Wait for new pattern to process
        Espresso.registerIdlingResources(new WaitForPatternFlag(mMainActivityRule.getActivity(), DatabaseContract.PatternColumns.FLAG_COMPLETE));

        // Check that a pattern was added
        onView(withId(R.id.recycler)).check(matches(withRecyclerSize(1)));

        // Check that the progressbar is not displayed
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testB_openPattern() {
        // Wait for new pattern to process
        Espresso.registerIdlingResources(new WaitForPatternFlag(mMainActivityRule.getActivity(), DatabaseContract.PatternColumns.FLAG_COMPLETE));

        // Open pattern
        onView(withId(R.id.card)).perform(click());

        // Verify that the activity opened
        onView(withId(R.id.canvas)).check(matches(isDisplayed()));

        // Go back to main
        pressBack();
    }

    @Test
    public void testC1_editTitlePattern() {
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
        onView(withId(R.id.menu_change_name));

        // Verify title focus
        //onView(withId(R.id.editable_title)).check(matches(hasFocus()));

        // Verify title
        onView(withId(R.id.editable_title)).check(matches(withText(title)));

        // Erase 3 characters
        onView(withId(R.id.editable_title)).perform(deleteText(3));

        // Verify name change
        title = title.substring(0, title.length() - 3);
        onView(withId(R.id.editable_title)).check(matches(withText(title)));

        // Press done
        onView(withId(R.id.fab)).perform(click());
    }


    @Test
    public void testC2_verifyTitlePattern() {
        String title = Pattern.createTitleFromFileName("nightmare_test_image.jpg");
        title = title.substring(0, title.length() - 3);

        // Check that only one pattern exists
        onView(withId(R.id.recycler)).check(matches(withRecyclerSize(1)));

        // Verify title
        onView(withId(R.id.title)).check(matches(withText(title)));
    }

    @Test
    public void testZ_deletePattern() {
        // Delete pattern
        onView(withId(R.id.card)).perform(swipeRight());
        onView(withId(R.id.button1)).perform(click());

        // Check that recycler is empty
        onView(withId(R.id.recycler)).check(matches(withRecyclerSize(0)));
    }
}
