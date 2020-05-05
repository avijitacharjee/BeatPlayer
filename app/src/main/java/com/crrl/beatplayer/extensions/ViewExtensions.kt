/*
 * Copyright (c) 2020. Carlos René Ramos López. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.crrl.beatplayer.extensions

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.budiyev.android.circularprogressbar.CircularProgressBar
import com.crrl.beatplayer.R
import com.crrl.beatplayer.ui.widgets.SimpleCustomSnackbar
import com.crrl.beatplayer.utils.GeneralUtils
import com.google.android.material.tabs.TabLayout
import rm.com.audiowave.AudioWaveView


const val DEFAULT = "com.crrl.beatplayer.DEFAULT"
const val SUCCESS = "com.crrl.beatplayer.SUCCESS"
const val ERROR = "com.crrl.beatplayer.ERROR"
const val CUSTOM = "com.crrl.beatplayer.CUSTOM"

fun <T : ViewDataBinding> ViewGroup.inflateWithBinding(
    @LayoutRes layoutRes: Int,
    attachToRoot: Boolean = false
): T {
    val layoutInflater = LayoutInflater.from(context)
    return DataBindingUtil.inflate(layoutInflater, layoutRes, this, attachToRoot) as T
}

fun View?.show() {
    this ?: return
    visibility = VISIBLE
}

fun View?.hide() {
    this ?: return
    visibility = GONE
}

fun View?.toggleShow(show: Boolean) {
    if (show) show() else hide()
}

fun View.setMargins(
    left: Int? = null,
    top: Int? = null,
    right: Int? = null,
    bottom: Int? = null
) {
    val lp = layoutParams as? ViewGroup.MarginLayoutParams
        ?: return

    lp.setMargins(
        left ?: lp.leftMargin,
        top ?: lp.topMargin,
        right ?: lp.rightMargin,
        bottom ?: lp.rightMargin
    )

    layoutParams = lp
}

fun View.setPaddings(
    left: Int? = null,
    top: Int? = null,
    right: Int? = null,
    bottom: Int? = null
) {
    setPadding(
        left ?: paddingLeft,
        top ?: paddingTop,
        right ?: paddingRight,
        bottom ?: paddingBottom
    )
}

fun View?.animateScale(from: Float, to: Float, dur: Long) {
    this ?: return
    val scaleX = ObjectAnimator.ofFloat(this, View.SCALE_X, from, to)
    val scaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y, from, to)
    val animatorSet = AnimatorSet().apply {
        interpolator = OvershootInterpolator()
        duration = dur
        playTogether(scaleX, scaleY)
    }
    animatorSet.start()
}

fun View?.snackbar(
    type: String,
    msg: String,
    dur: Int,
    custom: Int = R.drawable.app_icon,
    action: String? = null,
    clickListener: View.OnClickListener? = null
) {
    this ?: return
    val icon = when (type) {
        DEFAULT -> R.drawable.ic_notification
        SUCCESS -> R.drawable.ic_success
        ERROR -> R.drawable.ic_error
        else -> custom
    }

    val color = when (type) {
        DEFAULT -> R.drawable.background_default
        SUCCESS -> R.drawable.background_success
        ERROR -> R.drawable.background_error
        else -> R.drawable.background_default
    }
    SimpleCustomSnackbar.make(this, msg, dur, clickListener, icon, action, color)?.show()
}

internal fun View?.findSuitableParent(): ViewGroup? {
    var view = this
    var fallback: ViewGroup? = null
    do {
        if (view is CoordinatorLayout) {
            return view
        } else if (view is FrameLayout) {
            if (view.id == android.R.id.content) {
                return view
            } else {
                fallback = view
            }
        }

        if (view != null) {
            val parent = view.parent
            view = if (parent is View) parent else null
        }
    } while (view != null)

    return fallback
}

fun View?.setCustomColor(color: Int, hasBackground: Boolean = false, opacity: Boolean = false) {
    this ?: return
    val cHex = "#${Integer.toHexString(color).replace("ff", "80")}"
    val c = Color.parseColor(if (cHex != "#0") cHex else "#80000000")
    when (this) {
        is ImageButton -> {
            if (hasBackground) {
                this.apply {
                    if (!opacity) {
                        background =
                            context.getDrawable(R.drawable.btn_play_header_background).apply {
                                this?.setTint(color)
                            }
                    } else {
                        imageTintList =
                            ColorStateList.valueOf(GeneralUtils.getBlackWhiteColor(color))
                    }
                }
            } else {
                imageTintList = ColorStateList.valueOf(color)
            }
        }
        is TabLayout -> {
            val default = (context as Activity).getColorByTheme(R.attr.subTitleTextColor)
            setTabTextColors(default, color)
            setSelectedTabIndicatorColor(color)
        }
        is CircularProgressBar -> {
            backgroundStrokeColor = c
            foregroundStrokeColor = color
        }
        is LinearLayout -> {
            background = if (hasBackground) {
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.btn_ripple_with_stroke
                ).apply { this?.setTint(color) }
            } else AppCompatResources.getDrawable(context, R.drawable.btn_play_header_background)
                .apply { this?.setTint(color) }
        }
        is AudioWaveView -> {
            waveColor = color
        }
        is TextView -> if (hasBackground) {
            setTextColor(GeneralUtils.getBlackWhiteColor(color))
        } else {
            setTextColor(if (opacity) c else color)
        }
    }
}