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

package com.crrl.beatplayer.repository

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns._ID
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
import com.crrl.beatplayer.extensions.forEach
import com.crrl.beatplayer.extensions.toList
import com.crrl.beatplayer.models.Song
import com.crrl.beatplayer.utils.SettingsUtility
import java.io.File

interface SongsRepositoryInterface {
    fun loadSongs(): List<Song>
    fun getSongForId(id: Long): Song
    fun search(searchString: String, limit: Int = Int.MAX_VALUE): List<Song>
    fun deleteTracks(ids: LongArray): Int
}

class SongsRepository(private val context: Context?) : SongsRepositoryInterface {

    private val contentResolver = context!!.contentResolver
    private val settingsUtility = SettingsUtility.getInstance(context)

    override fun loadSongs(): List<Song> {
        return makeSongCursor(null, null)
            .toList(true) {
                Song.createFromCursor(this)
            }
    }

    override fun getSongForId(id: Long): Song {
        val cursor = makeSongCursor("_id=$id", null)!!
        cursor.use {
            return if (it.moveToFirst()) {
                Song.createFromCursor(it)
            } else {
                Song()
            }
        }
    }

    override fun search(searchString: String, limit: Int): List<Song> {
        val result = makeSongCursor("title LIKE ?", arrayOf("$searchString%"))
            .toList(true) { Song.createFromCursor(this) }
        if (result.size < limit) {
            val moreSongs = makeSongCursor("title LIKE ?", arrayOf("%_$searchString%"))
                .toList(true) { Song.createFromCursor(this) }
            result += moreSongs
        }
        return if (result.size < limit) {
            result
        } else {
            result.subList(0, limit)
        }
    }

    override fun deleteTracks(ids: LongArray): Int {
        val projection = arrayOf(
            _ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.Audio.AudioColumns.ALBUM_ID
        )
        val selection = StringBuilder().apply {
            append("$_ID IN (")
            for (i in ids.indices) {
                append(ids[i])
                if (i < ids.size - 1) {
                    append(",")
                }
            }
            append(")")
        }.toString()

        var deleted = ids.size
        contentResolver.query(
            EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )?.use {
            val del = if (it.moveToFirst()) {
                contentResolver.delete(EXTERNAL_CONTENT_URI, selection, null)
            } else -1
            if (del < 1) return -1
            it.forEach(true) {
                val name = it.getString(1)
                val f = File(name)
                try {
                    if (f.exists())
                        if (!f.delete())
                            if (!f.canonicalFile.delete())
                                if (!context!!.deleteFile(f.name)) {
                                    deleted--
                                }
                } catch (_: SecurityException) {
                }
            }
        }
        return deleted
    }

    @SuppressLint("Recycle")
    private fun makeSongCursor(
        selection: String?,
        paramArrayOfString: Array<String>?,
        sortOrder: String?
    ): Cursor {
        val selectionStatement = StringBuilder("is_music=1 AND title != ''")
        if (!selection.isNullOrEmpty()) {
            selectionStatement.append(" AND $selection")
        }
        val projection =
            arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id")

        return contentResolver.query(
            EXTERNAL_CONTENT_URI,
            projection,
            selectionStatement.toString(),
            paramArrayOfString,
            sortOrder
        )
            ?: throw IllegalStateException("Unable to query $EXTERNAL_CONTENT_URI, system returned null.")
    }

    private fun makeSongCursor(selection: String?, paramArrayOfString: Array<String>?): Cursor? {
        val songSortOrder = settingsUtility.songSortOrder
        return makeSongCursor(selection, paramArrayOfString, songSortOrder)
    }
}
