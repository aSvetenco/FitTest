package com.sa.healthtest.utils

import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

fun View.invisible() = { this.visibility = View.GONE }
fun View.visible() = { this.visibility == View.VISIBLE }
fun ViewGroup.inflate(@LayoutRes layout: Int) : View = LayoutInflater.from(context).inflate(layout, this, false)



