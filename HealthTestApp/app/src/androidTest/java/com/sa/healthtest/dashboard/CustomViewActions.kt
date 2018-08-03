package com.sa.healthtest.dashboard

import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.GeneralLocation
import android.support.test.espresso.action.GeneralSwipeAction
import android.support.test.espresso.action.Press
import android.support.test.espresso.action.Swipe

fun swipeFromLeftEdgeHorizotal(): ViewAction {
    return GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER_LEFT, GeneralLocation.CENTER, Press.FINGER)
}