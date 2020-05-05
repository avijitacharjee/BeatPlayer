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

package com.crrl.beatplayer.ui.adapters

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crrl.beatplayer.R
import com.crrl.beatplayer.databinding.SongItemBinding
import com.crrl.beatplayer.databinding.SongItemHeaderBinding
import com.crrl.beatplayer.extensions.*
import com.crrl.beatplayer.interfaces.ItemClickListener
import com.crrl.beatplayer.models.Song
import com.crrl.beatplayer.ui.viewmodels.MainViewModel

private const val HEADER_TYPE = 0
private const val ITEM_TYPE = 1

class SongAdapter(
    private val context: Context?,
    private val mainViewModel: MainViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val songList = mutableListOf<Song>()
    var showHeader = false
    var isPlaylist = false
    var isAlbumDetail = false
    var isSearchView = false
    var itemClickListener: ItemClickListener<Song>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER_TYPE -> {
                val viewBinding =
                    parent.inflateWithBinding<SongItemHeaderBinding>(R.layout.song_item_header)
                ViewHolderSongHeader(viewBinding)
            }
            ITEM_TYPE -> {
                val viewBinding = parent.inflateWithBinding<SongItemBinding>(R.layout.song_item)
                ViewHolderSong(viewBinding)
            }
            else -> {
                val viewBinding = parent.inflateWithBinding<SongItemBinding>(R.layout.song_item)
                ViewHolderSong(viewBinding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentSong = if (!songList.isNullOrEmpty()) getItem(position) else Song()
        when (getItemViewType(position)) {
            HEADER_TYPE -> {
                (holder as ViewHolderSongHeader).bind(songList.size)
            }
            ITEM_TYPE -> {
                (holder as ViewHolderSong).bind(currentSong!!)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (showHeader && position == 0) {
            HEADER_TYPE
        } else {
            ITEM_TYPE
        }
    }

    override fun getItemCount() = if (showHeader) {
        songList.size + 1
    } else {
        songList.size
    }

    fun getItem(position: Int): Song? {
        return if (showHeader) {
            if (position == 0) {
                null
            } else {
                songList[position - 1]
            }
        } else {
            songList[position]
        }
    }

    fun getPosition(position: Int): Int {
        return if (showHeader) {
            if (position == 0) {
                position
            } else position - 1
        } else position
    }

    fun updateDataSet(newList: List<Song>) {
        if (!songList.deepEquals(newList)) {
            songList.setAll(newList.toMutableList())
            notifyDataSetChanged()
        }
    }

    inner class ViewHolderSong(private val binding: SongItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(song: Song) {
            binding.apply {
                this.song = song
                position = adapterPosition
                this.size = itemCount
                this.isSearch = isSearchView
                executePendingBindings()

                if (songList.isNotEmpty()) setSelectedSongColor(this, song)

                container.setOnClickListener(this@ViewHolderSong)
                itemMenu.setOnClickListener(this@ViewHolderSong)
            }
        }

        override fun onClick(view: View) {
            if (itemClickListener != null)
                when (view.id) {
                    R.id.item_menu -> itemClickListener!!.onPopupMenuClick(
                        view,
                        adapterPosition,
                        getItem(adapterPosition)!!,
                        songList
                    )
                    R.id.container -> itemClickListener!!.onItemClick(
                        view,
                        adapterPosition,
                        getItem(adapterPosition)!!
                    )
                }
        }

        private fun setSelectedSongColor(binding: SongItemBinding, song: Song) {
            context as Activity
            binding.apply {
                if (mainViewModel.getCurrentSong().value!!.compare(song)) {
                    val color = context.getColorByTheme(R.attr.colorAccentOpacity)
                    song.isSelected = true
                    title.isSelected = true
                    title.setCustomColor(context.getColorByTheme(R.attr.colorAccent))
                    artist.setCustomColor(color, opacity = true)
                    sep.setCustomColor(color, opacity = true)
                    album.setCustomColor(color, opacity = true)
                } else {
                    song.isSelected = false
                    val color = context.getColorByTheme(R.attr.subTitleTextColor)
                    title.setCustomColor(context.getColorByTheme(R.attr.titleTextColor))
                    artist.setCustomColor(color)
                    sep.setCustomColor(color)
                    album.setCustomColor(color)
                }
            }
            songList[getPosition(adapterPosition)] = song
        }
    }

    inner class ViewHolderSongHeader(private val binding: SongItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(songCount: Int) {
            binding.apply {
                this.songCount = songCount
                viewModel = mainViewModel
                if (isAlbumDetail) sortSong.hide()
                if (isPlaylist) {
                    sortSong.hide()
                    songSize.hide()
                }
                executePendingBindings()

                shuffleSong.setOnClickListener(this@ViewHolderSongHeader)
                sortSong.setOnClickListener(this@ViewHolderSongHeader)
                playAllSong.setOnClickListener(this@ViewHolderSongHeader)
            }
        }

        override fun onClick(view: View) {
            if (itemClickListener != null)
                when (view.id) {
                    R.id.shuffle_song -> itemClickListener!!.onShuffleClick(view)
                    R.id.sort_song -> itemClickListener!!.onSortClick(view)
                    R.id.play_all_song -> itemClickListener!!.onPlayAllClick(view)
                }
        }

    }
}
