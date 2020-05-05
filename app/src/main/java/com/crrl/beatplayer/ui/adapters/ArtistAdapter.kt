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

import android.content.Context
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crrl.beatplayer.R
import com.crrl.beatplayer.databinding.ArtistItemBinding
import com.crrl.beatplayer.databinding.ArtistItemHeaderBinding
import com.crrl.beatplayer.extensions.deepEquals
import com.crrl.beatplayer.extensions.inflateWithBinding
import com.crrl.beatplayer.interfaces.ItemClickListener
import com.crrl.beatplayer.models.Artist
import com.crrl.beatplayer.ui.viewmodels.MainViewModel
import com.crrl.beatplayer.utils.GeneralUtils.dip2px
import com.crrl.beatplayer.utils.GeneralUtils.screenWidth

private const val HEADER_TYPE = 0
private const val ITEM_TYPE = 1

class ArtistAdapter(private val context: Context?, private val mainViewModel: MainViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var artistList: List<Artist> = emptyList()
    var showHeader: Boolean = false
    var itemClickListener: ItemClickListener<Artist>? = null
    var spanCount: Int = 0
    private var lastClick = 0L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER_TYPE -> {
                val viewBinding =
                    parent.inflateWithBinding<ArtistItemHeaderBinding>(R.layout.artist_item_header)
                ViewHolderAlbumHeader(viewBinding)
            }
            ITEM_TYPE -> {
                val viewBinding = parent.inflateWithBinding<ArtistItemBinding>(R.layout.artist_item)
                ViewHolderArtist(viewBinding)
            }
            else -> {
                val viewBinding = parent.inflateWithBinding<ArtistItemBinding>(R.layout.artist_item)
                ViewHolderArtist(viewBinding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentSong = if (!artistList.isNullOrEmpty()) getItem(position) else null
        when (getItemViewType(position)) {
            HEADER_TYPE -> {
                (holder as ViewHolderAlbumHeader).bind(artistList.size)
            }
            ITEM_TYPE -> {
                (holder as ViewHolderArtist).bind(currentSong!!)
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
        artistList.size + 1
    } else {
        artistList.size
    }

    fun getItem(position: Int): Artist? {
        return if (showHeader) {
            if (position == 0) {
                null
            } else {
                artistList[position - 1]
            }
        } else {
            artistList[position]
        }
    }

    fun updateDataSet(newList: List<Artist>) {
        if (!artistList.deepEquals(newList)) {
            this.artistList = newList
            notifyDataSetChanged()
        }
    }

    inner class ViewHolderArtist(private val binding: ArtistItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(artist: Artist) {
            binding.apply {
                this.artist = artist
                viewModel = mainViewModel
                executePendingBindings()

                showDetails.setOnClickListener(this@ViewHolderArtist)
                container.layoutParams.apply {
                    height = screenWidth / spanCount + dip2px(context!!, 42)
                    width = screenWidth / spanCount - dip2px(context, 6)
                }
            }
        }

        override fun onClick(view: View) {
            if (SystemClock.elapsedRealtime() - lastClick < 300) {
                return
            }
            lastClick = SystemClock.elapsedRealtime()
            if (itemClickListener != null) itemClickListener!!.onItemClick(
                view,
                adapterPosition,
                getItem(adapterPosition)!!
            )
        }
    }

    inner class ViewHolderAlbumHeader(private val binding: ArtistItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(artistCount: Int) {
            binding.apply {
                this.artistCount = artistCount
                viewModel = mainViewModel
                executePendingBindings()

                sortArtist.setOnClickListener(this@ViewHolderAlbumHeader)
                playAllArtist.setOnClickListener(this@ViewHolderAlbumHeader)
            }
        }

        override fun onClick(view: View) {
            if (itemClickListener != null)
                when (view.id) {
                    R.id.play_all_artist -> itemClickListener!!.onPlayAllClick(view)
                    R.id.sort_artist -> itemClickListener!!.onSortClick(view)
                }
        }
    }
}