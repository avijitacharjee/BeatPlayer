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

package com.crrl.beatplayer.utils

import android.content.ContentUris.withAppendedId
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.view.Surface.*
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.crrl.beatplayer.models.Song
import com.crrl.beatplayer.utils.PlayerConstants.ARTWORK_URI
import java.io.FileInputStream
import java.io.FileNotFoundException


object GeneralUtils {

    const val VERTICAL = 0
    const val HORIZONTAL = 1

    val screenWidth: Int
        get() = Resources.getSystem().displayMetrics.widthPixels

    val screenHeight: Int
        get() = Resources.getSystem().displayMetrics.heightPixels

    @Throws(IllegalArgumentException::class)
    fun getRotation(context: Context): Int {
        val rotation =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
        return when (rotation) {
            ROTATION_0, ROTATION_180 -> VERTICAL
            ROTATION_90 -> HORIZONTAL
            else -> HORIZONTAL
        }
    }

    fun formatMilliseconds(duration: Long): String {
        val seconds = (duration / 1000).toInt() % 60
        val minutes = (duration / (1000 * 60) % 60).toInt()
        val hours = (duration / (1000 * 60 * 60) % 24).toInt()
        val hh: String
        val mm: String
        val ss: String
        hh = if (hours in 1..9) {
            "0$hours:"
        } else {
            if (hours >= 10) {
                "$hours:"
            } else {
                ""
            }
        }
        mm = if (minutes in 1..9) {
            "0$minutes:"
        } else {
            if (minutes >= 10) {
                "$minutes:"
            } else {
                "00:"
            }
        }
        ss = if (seconds in 1..9) {
            "0$seconds"
        } else {
            if (seconds >= 10) {
                "" + seconds
            } else {
                "00"
            }
        }
        return hh + mm + ss
    }

    fun getTotalTime(songList: List<Song>): Long {
        var minutes = 0L
        var hours = 0L
        var seconds = 0L
        for (song in songList) {
            seconds += (song.duration / 1000 % 60).toLong()
            minutes += (song.duration / (1000 * 60) % 60).toLong()
            hours += (song.duration / (1000 * 60 * 60) % 24).toLong()
        }
        return hours * (1000 * 60 * 60) + minutes * (1000 * 60) + seconds * 1000
    }

    fun audio2Raw(context: Context, uri: Uri): ByteArray? {
        val parcelFileDescriptor = try {
            context.contentResolver.openFileDescriptor(uri, "r", null) ?: return null
        } catch (ex: FileNotFoundException) {
            return null
        }
        return FileInputStream(parcelFileDescriptor.fileDescriptor).readBytes()
    }

    fun toggleShowKeyBoard(context: Context?, editText: EditText, show: Boolean) {
        if (show) {
            editText.apply {
                requestFocus()
                val imm =
                    context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm!!.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        } else {
            editText.apply {
                clearFocus()
                val imm =
                    context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm!!.hideSoftInputFromWindow(editText.windowToken, 0)
            }
        }
    }

    fun dip2px(context: Context, dpValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun addZeros(number: Int?): String {
        if (number!! < 10) return "00${number}"
        if (number < 100) return "0${number}"
        return number.toString()
    }

    fun getAlbumArtUri(albumId: Long) = withAppendedId(ARTWORK_URI, albumId)
}
