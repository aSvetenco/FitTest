package com.sa.healthtest.dashboard

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.DrawerActions.close
import android.support.test.espresso.contrib.DrawerActions.open
import android.support.test.espresso.contrib.DrawerMatchers.isClosed
import android.support.test.espresso.contrib.DrawerMatchers.isOpen
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.sa.healthtest.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class DashboardActivityTest {
    @get:Rule
    val mDashboardActivityRule = ActivityTestRule<DashboardActivity>(DashboardActivity::class.java)
    private lateinit var mDashboardActivity: DashboardActivity
    @Before
    fun setUp() {
        mDashboardActivity = mDashboardActivityRule.activity
    }

    @Test
    fun checkStartLabelIsShown() {
//        Check if label is visible
        onView(withId(R.id.tv_alert)).check(matches(isDisplayed()))
    }

    @Test
    fun openCloseDrawerLayout() {
//        Check if drawer is closed at startup
        onView(withId(R.id.nav_menu))
                .check(matches(isClosed()))

//        Open drawer
        onView(withId(R.id.nav_menu)).perform(open())

//        Check if drawer is opened
        onView(withId(R.id.nav_menu))
                .check(matches(isOpen()))

//        Close drawer
        onView(withId(R.id.nav_menu)).perform(close())

//        Check if drawer is closed
        onView(withId(R.id.nav_menu))
                .check(matches(isClosed()))
    }

    @Test
    fun clickOnAndroidHomeIcon_OpensNavigation() {
        // Check that left drawer is closed at startup
        onView(withId(R.id.nav_menu))
                .check(matches(isClosed())) // Left Drawer should be closed.

        // Open Drawer
        val navigateUpDesc = mDashboardActivity
                .getString(android.support.v7.appcompat.R.string.abc_action_bar_up_description)
        onView(withContentDescription(navigateUpDesc)).perform(click())

        // Check if drawer is open
        onView(withId(R.id.nav_menu))
                .check(matches(isOpen()))
    }

    @Test
    fun drawerLabelIsDisplayed() {
        onView(withId(R.id.nav_menu)).perform(open())
        onView(withId(R.id.drawerLabel)).check(matches(isDisplayed()))
        onView(withId(R.id.drawerLabel)).check(matches(withText(mDashboardActivity.getString(R.string.app_name))))
    }
}