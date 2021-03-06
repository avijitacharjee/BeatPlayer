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

package com.crrl.beatplayer.models

import android.database.Cursor
import com.crrl.beatplayer.utils.PlayerConstants.ARTIST_TYPE
import com.google.gson.Gson

data class Artist(
    var id: Long = 0,
    var name: String = "",
    var albumId: Long = 0,
    var albumCount: Int = 0
) : MediaItem(id) {

    companion object {
        fun createFromCursor(cursor: Cursor): Artist {
            return Artist(
                cursor.getLong(0),
                cursor.getString(2),
                cursor.getLong(1)
            )
        }
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }

    fun toFavorite(): Favorite {
        return Favorite(id, name, name, albumId, 0, albumCount, ARTIST_TYPE)
    }
}