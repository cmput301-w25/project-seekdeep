package com.example.project_seekdeep;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import static org.hamcrest.CoreMatchers.anything;

import android.util.Log;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;


// Taken from Seth's lab-07 instructions on UI testing :)
// Taken by: Kevin Tu and Jachelle Chan
// Taken on: 2025-03-08
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MoodHistoryFragmentUITest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    private final UserProfile testUser = new UserProfile("kevtu2", "222");
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("UserDB");
    private final CollectionReference moodsRef = db.collection("MoodDB");

    @BeforeClass
    public static void setup() {
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Before
    public void seedDatabase() throws InterruptedException {
        Mood mood1 = new Mood(testUser, EmotionalStates.HAPPINESS, new String[]{"Done testing!","Alone"});

        // give time so we can sort the list
        Thread.sleep(5000);
        Mood mood2 = new Mood(testUser, EmotionalStates.CONFUSION, new String[]{"How to test?", "With Another Person"});
        Thread.sleep(5000);
        Mood mood3 = new Mood(testUser, EmotionalStates.SADNESS, new String[]{"Midterms", "With a Crowd"});
        // make mood with a date outside of recent week
        Thread.sleep(2000);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -10);
        Mood mood4 = new Mood(testUser, EmotionalStates.ANGER, SocialSituations.ALONE, calendar.getTime(), "Midterms are hard");

        usersRef.document().set(testUser);

        moodsRef.document().set(mood1);
        moodsRef.document().set(mood2);
        moodsRef.document().set(mood3);
        moodsRef.document().set(mood4);


        // log in first
        scenario.getScenario().onActivity(activity -> activity.successful_login());
        scenario.getScenario().onActivity(activity -> activity.setCurrentUser(testUser));
    }

    @After
    public void tearDown() {
        String projectId = "project-seekdeep";
        URL url = null;
        try {
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @Test
    public void appShouldDisplayExistingMoods() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);

        onView(withText("\uD83D\uDE04 Happiness")).check(matches(isDisplayed()));
        onView(withText("\uD83E\uDD14 Confusion")).check(matches(isDisplayed()));
        onView(withText("☹️ Sadness")).check(matches(isDisplayed()));
        // this one needs to be onData because espresso doesn't see it since it needs to scroll
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(3).onChildView(withId(R.id.emotion))
                .check(matches(withText("\uD83D\uDE20 Anger")));

        // click on edit button for sadness mood
        // The way to click a button inside a listview item is taken from https://stackoverflow.com/a/25373597
        // will continue to be used throughout this whole testing file
        // Taken by: Jachelle Chan
        // Taken on: March 13, 2025
        onData(new BoundedMatcher<Object, Mood>(Mood.class) {
            @Override
            protected boolean matchesSafely(Mood mood) {
                return mood.getEmotionalState().toString().equals("☹️ Sadness");
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with emotion: Sadness");
            }
        }).inAdapterView(withId(R.id.history_listview)).onChildView(withId(R.id.edit_mood_button)).perform(click());
        Thread.sleep(3000);

        // check that the mood details are displayed properly
        onView(withId(R.id.emotion_spinner)).check(matches(hasDescendant(withText("☹️ Sadness"))));
        onView(withId(R.id.edit_reason)).check(matches(withText("Midterms")));
        onView(withId(R.id.social_situation_spinner)).check(matches(hasDescendant(withText("With a Crowd"))));
    }


    @Test
    public void testAddMood() throws InterruptedException {
        // we're gonna make this mood a fear mood event
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.create_mood_bottom_nav)).perform(click());
        Thread.sleep(1000);
        // click
        onView(withId(R.id.edit_emotion_editText)).perform(click());
        Thread.sleep(1000);
        // select emotion as fear and create the mood
        onView(withId(R.id.buttonFear)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(R.id.edit_reason)).perform(ViewActions.typeText("Demo"));
        onView(withId(R.id.social_situation_spinner)).perform(ViewActions.click());
        onView(withText("With a Crowd")).perform(click());
        onView(withId(R.id.confirm_create_button)).perform(click());

        // check that the mood has been created and put in history/profile
        onView(withId(R.id.History)).perform(click());
        onView(withText("\uD83D\uDE28 Fear")).check(matches(isDisplayed()));

        onData(new BoundedMatcher<Object, Mood>(Mood.class) {
            @Override
            protected boolean matchesSafely(Mood mood) {
                return mood.getEmotionalState().toString().equals("😨 Fear");
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with emotion: Fear");
            }
        }).inAdapterView(withId(R.id.history_listview)).onChildView(withId(R.id.edit_mood_button)).perform(click());
        Thread.sleep(3000);

        // check that the mood details are displayed properly
        onView(withId(R.id.emotion_spinner)).check(matches(hasDescendant(withText("😨 Fear"))));
        onView(withId(R.id.edit_reason)).check(matches(withText("Demo")));
        onView(withId(R.id.social_situation_spinner)).check(matches(hasDescendant(withText("With a Crowd"))));
    }

    @Test
    public void testAddMoodWithInvalidReasonShouldShowError() throws InterruptedException {
        //give time for setup
        Thread.sleep(2000);
        //Click Create Mood
        onView(withId(R.id.create_mood_bottom_nav)).perform(click());
        Thread.sleep(1000);
        //Choose an emotion
        onView(withId(R.id.edit_emotion_editText)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.buttonFear)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());
        //Write an invalid reason (over 200 characters long - not including whitespaces)
        String invalidReason = "A".repeat(201);
        onView(withId(R.id.edit_reason)).perform(ViewActions.typeText(invalidReason));
        //Click the Create button
        onView(withId(R.id.confirm_create_button)).perform(click());
        //Check that the error is shown to user
        onView(withId(R.id.edit_reason)).check(matches(hasErrorText("Reason must be ≤ 200 characters")));
    }

    @Test
    public void testDeleteMood() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        // save the view with the sadness mood event still available
        ViewInteraction view = onView(withText("☹️ Sadness"));

        // choose the sadness mood to delete and click the delete button associated with that mood event
        onData(new BoundedMatcher<Object, Mood>(Mood.class) {
            @Override
            protected boolean matchesSafely(Mood mood) {
                return mood.getEmotionalState().toString().equals("☹️ Sadness");
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with emotion: Sadness");
            }
        }).inAdapterView(withId(R.id.history_listview)).onChildView(withId(R.id.delete_mood_button)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());

        // check to see if the mood still exists
        view.check(doesNotExist());

    }


    @Test
    public void testEditMood() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        // save the view with the sadness mood event still available
        ViewInteraction view = onView(withText("☹️ Sadness"));

        // choose the sadness mood to edit and press the edit button associated with that mood event
        onData(new BoundedMatcher<Object, Mood>(Mood.class) {
            @Override
            protected boolean matchesSafely(Mood mood) {
                return mood.getEmotionalState().toString().equals("☹️ Sadness");
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with emotion: Sadness");
            }
        }).inAdapterView(withId(R.id.history_listview)).onChildView(withId(R.id.edit_mood_button)).perform(click());
        // change reason to be Midterms oh no!
        onView(withId(R.id.edit_reason)).perform(clearText());
        onView(withId(R.id.edit_reason)).perform(typeText("Midterms oh no!"));

        onView(withId(R.id.emotion_spinner)).perform(click());
        // change sadness to fear
        // This next line is taken from https://stackoverflow.com/a/40015436
        // Taken by: Jachelle Chan
        // Taken on: March 13, 2025
        onView(withText("😨 Fear")).inRoot(isPlatformPopup()).perform(click());

        onView(withId(android.R.id.button1)).perform(click());
        // check to see if the edit has edited those details on the history page
        onView(withText("😨 Fear")).check(matches(isDisplayed()));

        // check if the edit has edited details when clicking
        onData(new BoundedMatcher<Object, Mood>(Mood.class) {
            @Override
            protected boolean matchesSafely(Mood mood) {
                return mood.getEmotionalState().toString().equals("😨 Fear");
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with emotion: Fear");
            }
        }).inAdapterView(withId(R.id.history_listview)).onChildView(withId(R.id.edit_mood_button)).perform(click());
        Thread.sleep(3000);

        // check that the mood details are displayed properly
        onView(withId(R.id.emotion_spinner)).check(matches(hasDescendant(withText("😨 Fear"))));
        onView(withId(R.id.edit_reason)).check(matches(withText("Midterms oh no!")));
        onView(withId(R.id.social_situation_spinner)).check(matches(hasDescendant(withText("With a Crowd"))));
    }

    @Test
    public void moodShouldBeInReverseChronologicalOrder() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        // the order should be ["Sadness", "Confusion", "Happiness] (top to bottom)
        // the first item in the listview should be a sadness mood event
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("☹️ Sadness")));

        // next confusion
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText("🤔 Confusion")));

        // next happiness
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(2).onChildView(withId(R.id.emotion))
                .check(matches(withText("😄 Happiness")));

        // next anger
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(3).onChildView(withId(R.id.emotion))
                .check(matches(withText("\uD83D\uDE20 Anger")));
    }

    @Test
    public void moodsBeforeRecentWeekShouldNotShowWhenFiltered() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        ViewInteraction view = onView(withText("\uD83D\uDE20 Anger"));  // save view that should be gone
        // the order should be ["Sadness", "Confusion", "Happiness", "Anger"] (top to bottom)
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("☹️ Sadness")));

        // next confusion
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText("🤔 Confusion")));

        // next happiness
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(2).onChildView(withId(R.id.emotion))
                .check(matches(withText("😄 Happiness")));

        // next anger
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(3).onChildView(withId(R.id.emotion))
                .check(matches(withText("\uD83D\uDE20 Anger")));

        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click past week on the dialog fragment
        onView(withId(R.id.recent_week_chip)).perform((click()));
        onView(withId(R.id.apply_filters_button)).perform(click());

        // anger should now be gone and left with ["Sadness", "Confusion", "Happiness"] (top to bottom)
        view.check(doesNotExist());

        // check if the moods other than anger are still there
        // the order should be ["Sadness", "Confusion", "Happiness"] (top to bottom)
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("☹️ Sadness")));

        // next confusion
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText("🤔 Confusion")));

        // next happiness
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(2).onChildView(withId(R.id.emotion))
                .check(matches(withText("😄 Happiness")));
    }

    @Test
    public void filterMoodBySadnessShouldOnlyShowSadness() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        // save views that should be gone
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("\uD83D\uDE20 Anger"));
        views.add(view);
        view = onView(withText("🤔 Confusion"));
        views.add(view);
        view = onView(withText("😄 Happiness"));
        views.add(view);

        // the order should be ["Sadness", "Confusion", "Happiness", "Anger"] (top to bottom)
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("☹️ Sadness")));

        // next confusion
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText("🤔 Confusion")));

        // next happiness
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(2).onChildView(withId(R.id.emotion))
                .check(matches(withText("😄 Happiness")));

        // next anger
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(3).onChildView(withId(R.id.emotion))
                .check(matches(withText("\uD83D\uDE20 Anger")));

        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click sadness on the dialog fragment
        onView(withId(R.id.sadness_chip)).perform((click()));
        onView(withId(R.id.apply_filters_button)).perform(click());

        // check if those views are gone
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }
        // check if sadness is there
        onView(withText("☹️ Sadness")).check(matches(isDisplayed()));
    }

    @Test
    public void filterMoodByConfusionShouldOnlyShowConfusion() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        // save views that should be gone
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("\uD83D\uDE20 Anger"));
        views.add(view);
        view = onView(withText("☹️ Sadness"));
        views.add(view);
        view = onView(withText("😄 Happiness"));
        views.add(view);

        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click confusion on the dialog fragment
        onView(withId(R.id.confusion_chip)).perform((click()));
        onView(withId(R.id.apply_filters_button)).perform(click());

        // check if those views are gone
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }
        // check confusion is there
        onView(withText("\uD83E\uDD14 Confusion")).check(matches(isDisplayed()));
    }

    @Test
    public void filterMoodByHappinessShouldOnlyShowHappiness() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        // save views that should be gone
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("\uD83D\uDE20 Anger"));
        views.add(view);
        view = onView(withText("☹️ Sadness"));
        views.add(view);
        view = onView(withText("\uD83E\uDD14 Confusion"));
        views.add(view);

        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click happiness on the dialog fragment
        onView(withId(R.id.happiness_chip)).perform((click()));
        onView(withId(R.id.apply_filters_button)).perform(click());

        // check if those views are gone
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }
        // check happiness is there
        onView(withText("😄 Happiness")).check(matches(isDisplayed()));
    }

    @Test
    public void filterMoodByDisgustShouldOnlyShowDisgust() {
        Mood mood = new Mood(testUser, EmotionalStates.DISGUST, new String[]{"What??", "With Another Person"});
        usersRef.document().set(testUser);
        moodsRef.document().set(mood);
        onView(withId(R.id.History)).perform(click());
        // save views that should be gone
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("\uD83D\uDE20 Anger"));
        views.add(view);
        view = onView(withText("☹️ Sadness"));
        views.add(view);
        view = onView(withText("\uD83E\uDD14 Confusion"));
        views.add(view);
        view = onView(withText("😄 Happiness"));
        views.add(view);

        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click disgust on the dialog fragment
        onView(withId(R.id.disgust_chip)).perform((click()));
        onView(withId(R.id.apply_filters_button)).perform(click());

        // check if those views are gone
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }
        // check disgust is there
        onView(withText("\uD83E\uDD22 Disgust")).check(matches(isDisplayed()));
    }

    @Test
    public void filterMoodBySurpriseShouldOnlyShowSurprise() {
        Mood mood = new Mood(testUser, EmotionalStates.SURPRISE, new String[]{"What??", "With Another Person"});
        usersRef.document().set(testUser);
        moodsRef.document().set(mood);
        onView(withId(R.id.History)).perform(click());
        // save views that should be gone
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("\uD83D\uDE20 Anger"));
        views.add(view);
        view = onView(withText("☹️ Sadness"));
        views.add(view);
        view = onView(withText("\uD83E\uDD14 Confusion"));
        views.add(view);
        view = onView(withText("😄 Happiness"));
        views.add(view);

        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click surprise on the dialog fragment
        onView(withId(R.id.surprise_chip)).perform((click()));
        onView(withId(R.id.apply_filters_button)).perform(click());

        // check if those views are gone
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }
        // check surprise is there
        onView(withText("\uD83D\uDE2F Surprise")).check(matches(isDisplayed()));
    }

    @Test
    public void filterMoodByFearShouldOnlyShowFear() {
        Mood mood = new Mood(testUser, EmotionalStates.FEAR, new String[]{"What??", "With Another Person"});
        usersRef.document().set(testUser);
        moodsRef.document().set(mood);
        onView(withId(R.id.History)).perform(click());
        // save views that should be gone
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("\uD83D\uDE20 Anger"));
        views.add(view);
        view = onView(withText("☹️ Sadness"));
        views.add(view);
        view = onView(withText("\uD83E\uDD14 Confusion"));
        views.add(view);
        view = onView(withText("😄 Happiness"));
        views.add(view);

        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click fear on the dialog fragment
        onView(withId(R.id.fear_chip)).perform((click()));
        onView(withId(R.id.apply_filters_button)).perform(click());

        // check if those views are gone
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }
        // check fear is there
        onView(withText("\uD83D\uDE28 Fear")).check(matches(isDisplayed()));
    }

    @Test
    public void filterMoodByShameShouldOnlyShowShame() {
        Mood mood = new Mood(testUser, EmotionalStates.SHAME, new String[]{"What??", "With Another Person"});
        usersRef.document().set(testUser);
        moodsRef.document().set(mood);
        onView(withId(R.id.History)).perform(click());
        // save views that should be gone
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("\uD83D\uDE20 Anger"));
        views.add(view);
        view = onView(withText("☹️ Sadness"));
        views.add(view);
        view = onView(withText("\uD83E\uDD14 Confusion"));
        views.add(view);
        view = onView(withText("😄 Happiness"));
        views.add(view);

        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click shame on the dialog fragment
        onView(withId(R.id.shame_chip)).perform((click()));
        onView(withId(R.id.apply_filters_button)).perform(click());

        // check if those views are gone
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }
        // check shame is there
        onView(withText("\uD83D\uDE14 Shame")).check(matches(isDisplayed()));
    }

    @Test
    public void filterMoodBySadnessAndPastWeekShouldShowOnlySadness() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        // save views that should be gone
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("\uD83D\uDE20 Anger"));
        views.add(view);
        view = onView(withText("🤔 Confusion"));
        views.add(view);
        view = onView(withText("😄 Happiness"));
        views.add(view);

        // the order should be ["Sadness", "Confusion", "Happiness", "Anger"] (top to bottom)
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("☹️ Sadness")));

        // next confusion
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText("🤔 Confusion")));

        // next happiness
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(2).onChildView(withId(R.id.emotion))
                .check(matches(withText("😄 Happiness")));

        // next anger
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(3).onChildView(withId(R.id.emotion))
                .check(matches(withText("\uD83D\uDE20 Anger")));

        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click sadness and recent week on the dialog fragment
        onView(withId(R.id.sadness_chip)).perform((click()));
        onView(withId(R.id.recent_week_chip)).perform(click());
        onView(withId(R.id.apply_filters_button)).perform(click());

        // check if those views are gone
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }
        // check if sadness is there
        onView(withText("☹️ Sadness")).check(matches(isDisplayed()));
    }

    @Test
    public void filterMoodByAngerShouldShowOnlyAnger() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        // save views that should be gone
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("🤔 Confusion"));
        views.add(view);
        view = onView(withText("😄 Happiness"));
        views.add(view);
        view = onView(withText("☹️ Sadness"));
        views.add(view);

        // the order should be ["Sadness", "Confusion", "Happiness", "Anger"] (top to bottom)
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("☹️ Sadness")));

        // next confusion
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText("🤔 Confusion")));

        // next happiness
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(2).onChildView(withId(R.id.emotion))
                .check(matches(withText("😄 Happiness")));

        // next anger
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(3).onChildView(withId(R.id.emotion))
                .check(matches(withText("\uD83D\uDE20 Anger")));

        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click anger on the dialog fragment
        onView(withId(R.id.anger_chip)).perform((click()));
        onView(withId(R.id.apply_filters_button)).perform(click());

        // check if other views are gone
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }

        // check if anger is there
        onView(withText("\uD83D\uDE20 Anger")).check(matches(isDisplayed()));
    }

    @Test
    public void filterMoodByAngerAndPastWeekShouldNotShowAnything() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        // save views that should be gone
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("\uD83D\uDE20 Anger"));
        views.add(view);
        view = onView(withText("🤔 Confusion"));
        views.add(view);
        view = onView(withText("😄 Happiness"));
        views.add(view);
        view = onView(withText("☹️ Sadness"));
        views.add(view);

        // the order should be ["Sadness", "Confusion", "Happiness", "Anger"] (top to bottom)
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("☹️ Sadness")));

        // next confusion
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText("🤔 Confusion")));

        // next happiness
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(2).onChildView(withId(R.id.emotion))
                .check(matches(withText("😄 Happiness")));

        // next anger
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(3).onChildView(withId(R.id.emotion))
                .check(matches(withText("\uD83D\uDE20 Anger")));

        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click anger and recent week on the dialog fragment
        onView(withId(R.id.anger_chip)).perform((click()));
        onView(withId(R.id.recent_week_chip)).perform(click());
        onView(withId(R.id.apply_filters_button)).perform(click());

        // check if all views are gone
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }
    }

    @Test
    public void filteringMoodsByConfusionAndHappinessShouldShowBoth() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        // save views that should be gone
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("\uD83D\uDE20 Anger"));
        views.add(view);
        view = onView(withText("☹️ Sadness"));
        views.add(view);

        // the order should be ["Sadness", "Confusion", "Happiness", "Anger"] (top to bottom)
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("☹️ Sadness")));

        // next confusion
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText("🤔 Confusion")));

        // next happiness
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(2).onChildView(withId(R.id.emotion))
                .check(matches(withText("😄 Happiness")));

        // next anger
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(3).onChildView(withId(R.id.emotion))
                .check(matches(withText("\uD83D\uDE20 Anger")));

        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click confusion and happiness on the dialog fragment
        onView(withId(R.id.confusion_chip)).perform((click()));
        onView(withId(R.id.happiness_chip)).perform(click());
        onView(withId(R.id.apply_filters_button)).perform(click());

        // check if all views are gone
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }

        // check if confusion and happiness are there in that order
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("🤔 Confusion")));

        // next happiness
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText("😄 Happiness")));
    }

    @Test
    public void filteringMoodByAngerAndConfusionAndRecentWeekShouldShowOnlyConfusion() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        // save views that should be gone
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("\uD83D\uDE20 Anger"));
        views.add(view);
        view = onView(withText("😄 Happiness"));
        views.add(view);
        view = onView(withText("☹️ Sadness"));
        views.add(view);

        // the order should be ["Sadness", "Confusion", "Happiness", "Anger"] (top to bottom)
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("☹️ Sadness")));

        // next confusion
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText("🤔 Confusion")));

        // next happiness
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(2).onChildView(withId(R.id.emotion))
                .check(matches(withText("😄 Happiness")));

        // next anger
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(3).onChildView(withId(R.id.emotion))
                .check(matches(withText("\uD83D\uDE20 Anger")));

        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click anger and confusion and recent week on the dialog fragment
        onView(withId(R.id.anger_chip)).perform((click()));
        onView(withId(R.id.confusion_chip)).perform((click()));
        onView(withId(R.id.recent_week_chip)).perform((click()));
        onView(withId(R.id.apply_filters_button)).perform(click());

        // check if other views are gone
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }

        // check if confusion is there
        onView(withText("🤔 Confusion")).check(matches(isDisplayed()));
    }

    @Test
    public void filteringMoodButDeselectingShouldNotFilterByDeselected() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        // save views that should be gone
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("\uD83D\uDE20 Anger"));
        views.add(view);
        view = onView(withText("😄 Happiness"));
        views.add(view);
        view = onView(withText("☹️ Sadness"));
        views.add(view);

        // the order should be ["Sadness", "Confusion", "Happiness", "Anger"] (top to bottom)
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("☹️ Sadness")));

        // next confusion
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText("🤔 Confusion")));

        // next happiness
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(2).onChildView(withId(R.id.emotion))
                .check(matches(withText("😄 Happiness")));

        // next anger
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(3).onChildView(withId(R.id.emotion))
                .check(matches(withText("\uD83D\uDE20 Anger")));

        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click anger and confusion and then click anger again
        onView(withId(R.id.anger_chip)).perform((click()));
        onView(withId(R.id.confusion_chip)).perform((click()));
        onView(withId(R.id.anger_chip)).perform((click()));
        onView(withId(R.id.apply_filters_button)).perform(click());

        // check if other views are gone including anger
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }

        // check if confusion is there
        onView(withText("🤔 Confusion")).check(matches(isDisplayed()));
    }

    @Test
    public void filterAndThenResetFiltersShouldReturnToOriginalState() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        // save views that should be gone for the initial filter
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("\uD83D\uDE20 Anger"));
        views.add(view);
        view = onView(withText("😄 Happiness"));
        views.add(view);
        view = onView(withText("☹️ Sadness"));
        views.add(view);

        // the order should be ["Sadness", "Confusion", "Happiness", "Anger"] (top to bottom)
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("☹️ Sadness")));

        // next confusion
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText("🤔 Confusion")));

        // next happiness
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(2).onChildView(withId(R.id.emotion))
                .check(matches(withText("😄 Happiness")));

        // next anger
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(3).onChildView(withId(R.id.emotion))
                .check(matches(withText("\uD83D\uDE20 Anger")));

        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click anger and confusion and recent week on the dialog fragment
        onView(withId(R.id.anger_chip)).perform((click()));
        onView(withId(R.id.confusion_chip)).perform((click()));
        onView(withId(R.id.recent_week_chip)).perform((click()));
        onView(withId(R.id.apply_filters_button)).perform(click());

        // check if other views are gone
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }

        // check if confusion is there
        onView(withText("🤔 Confusion")).check(matches(isDisplayed()));

        // now we reset filters
        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click reset filters
        onView(withId(R.id.reset_filters_button)).perform(click());

        // now everything should be back again
        // the order should be ["Sadness", "Confusion", "Happiness", "Anger"] (top to bottom)
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("☹️ Sadness")));

        // next confusion
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText("🤔 Confusion")));

        // next happiness
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(2).onChildView(withId(R.id.emotion))
                .check(matches(withText("😄 Happiness")));

        // next anger
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(3).onChildView(withId(R.id.emotion))
                .check(matches(withText("\uD83D\uDE20 Anger")));
    }

    @Test
    public void testFilterKeyword() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        // save views that should be gone
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("😄 Happiness"));
        views.add(view);
        view = onView(withText("🤔 Confusion"));
        views.add(view);

        // the order should be ["Sadness", "Confusion", "Happiness", "Anger"] (top to bottom)
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("☹️ Sadness")));

        // next confusion
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText("🤔 Confusion")));

        // next happiness
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(2).onChildView(withId(R.id.emotion))
                .check(matches(withText("😄 Happiness")));

        // next anger
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(3).onChildView(withId(R.id.emotion))
                .check(matches(withText("\uD83D\uDE20 Anger")));

        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // type keyword "Midterms"
        onView(withId(R.id.dialog_keyword_search)).perform(typeText("Midterms"));
        onView(withId(R.id.apply_filters_button)).perform(click());

        // now should only be the sadness mood and anger
        // check if other views are gone
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }

        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("☹️ Sadness")));
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.reason))
                .check(matches(withText("Midterms")));
    }

    @Test
    public void testFilterKeywordShouldErrorWhenMoreThanOneWord() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(1000);
        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // type 2 words
        onView(withId(R.id.dialog_keyword_search)).perform(typeText("testing code"));
        onView(withId(R.id.apply_filters_button)).perform(click());
        // check that the error is shown to user
        onView(withId(R.id.dialog_keyword_search)).check(matches(hasErrorText("1 keyword only!")));

    }

    @Test
    public void testKeywordFilterAndEmotionalState() throws InterruptedException {
        // add another anger mood to test with
        Mood mood = new Mood(testUser, EmotionalStates.ANGER, new String[]{"What??", "With Another Person"});
        usersRef.document().set(testUser);
        moodsRef.document().set(mood);
        // so now the listview will look like ["Anger', "Sadness", "Confusion", "Happiness", "Anger"]
        // give time
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(1000);
        // save views that should be gone
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("😄 Happiness"));
        views.add(view);
        view = onView(withText("🤔 Confusion"));
        views.add(view);
        view = onView(withText("What??"));
        views.add(view);
        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click on the anger filter
        onView(withId(R.id.anger_chip)).perform(click());
        // type "Midterms"
        onView(withId(R.id.dialog_keyword_search)).perform(typeText("Midterms"));
        onView(withId(R.id.apply_filters_button)).perform(click());

        // Should now only have 1 anger , which is the one that includes "Midterms" in the reason
        // check if other views are gone
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }

        // check if the anger event with the midterms reason is there
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("\uD83D\uDE20 Anger")));

        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.reason))
                .check(matches(withText("Midterms are hard")));
    }

    @Test
    public void testKeywordFilterAndEmotionalStateAndRecentWeek() throws InterruptedException {
        // add another anger mood to test with
        Mood mood = new Mood(testUser, EmotionalStates.SADNESS, new String[]{"What??", "With Another Person"});
        usersRef.document().set(testUser);
        moodsRef.document().set(mood);
        // so now the listview will look like ["Sadness', "Sadness", "Confusion", "Happiness", "Anger"]
        // give time
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(1000);
        // save views that should be gone
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("😄 Happiness"));
        views.add(view);
        view = onView(withText("What??"));
        views.add(view);
        view = onView(withText("🤔 Confusion"));
        views.add(view);
        view = onView(withText("\uD83D\uDE20 Anger"));
        views.add(view);
        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // click on the anger filter and the recent week filter
        onView(withId(R.id.sadness_chip)).perform(click());
        onView(withId(R.id.recent_week_chip)).perform(click());
        // type "Midterms"
        onView(withId(R.id.dialog_keyword_search)).perform(typeText("Midterms"));
        onView(withId(R.id.apply_filters_button)).perform(click());

        // now should only have the sadness mood that includes midterms in it
        // check if other views are gone
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }

        // check if the sadness event with the midterms reason is there
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("☹️ Sadness")));

        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.reason))
                .check(matches(withText("Midterms")));
    }

    @Test
    public void testKeywordFilterIsNotCaseSensitive() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        // save views that should be gone
        ArrayList<ViewInteraction> views = new ArrayList<>();
        ViewInteraction view = onView(withText("😄 Happiness"));
        views.add(view);
        view = onView(withText("🤔 Confusion"));
        views.add(view);

        // the order should be ["Sadness", "Confusion", "Happiness", "Anger"] (top to bottom)
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("☹️ Sadness")));

        // next confusion
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText("🤔 Confusion")));

        // next happiness
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(2).onChildView(withId(R.id.emotion))
                .check(matches(withText("😄 Happiness")));

        // next anger
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(3).onChildView(withId(R.id.emotion))
                .check(matches(withText("\uD83D\uDE20 Anger")));

        // click the filter button
        onView(withId(R.id.filter_button)).perform(click());
        // type keyword "Midterms"
        onView(withId(R.id.dialog_keyword_search)).perform(typeText("MiDTErMs"));
        onView(withId(R.id.apply_filters_button)).perform(click());

        // now should only be the sadness mood and anger
        // check if other views are gone
        for (ViewInteraction aview : views) {
            aview.check(doesNotExist());
        }

        // check if sadness is there with the midterms reason
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText("☹️ Sadness")));
        onData(anything()).inAdapterView(withId(R.id.history_listview)).atPosition(0).onChildView(withId(R.id.reason))
                .check(matches(withText("Midterms")));
    }
}
