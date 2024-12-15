package com.example.submissionintermediate.view.auth

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.submissionintermediate.JsonConverter
import com.example.submissionintermediate.R
import com.example.submissionintermediate.utils.EspressoIdlingResource
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest {

    @get:Rule
    val activity = ActivityScenarioRule(LoginActivity::class.java)

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        mockWebServer.start(8080)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        mockWebServer.shutdown()
    }

    @Test
    fun testLoginSuccess() {
        val successJson = JsonConverter.readStringFromFile("success.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(successJson))

        
        onView(withId(R.id.ed_login_email))
            .perform(typeText("valid@example.com"), closeSoftKeyboard())

        onView(withId(R.id.ed_login_password))
            .perform(typeText("correct_password"), closeSoftKeyboard())

        onView(withId(R.id.login_button)).perform(click())


        onView(withId(R.id.main_activity)).check(matches(isDisplayed()))
    }
    @Test
    fun testLogoutSuccess() {
        val successJson = JsonConverter.readStringFromFile("success.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(successJson))

        onView(withId(R.id.action_logout)).perform(click())


        onView(withId(R.id.login_activity)).check(matches(isDisplayed()))
    }

    @Test
    fun testLoginFailed() {

        onView(withId(R.id.ed_login_email))
            .perform(typeText("invalid@example.com"), closeSoftKeyboard())

        onView(withId(R.id.ed_login_password))
            .perform(typeText("wrong_password"), closeSoftKeyboard())

        onView(withId(R.id.login_button)).perform(click())

    }
}
