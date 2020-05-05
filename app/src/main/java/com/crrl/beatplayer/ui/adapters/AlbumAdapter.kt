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
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.crrl.beatplayer.R
import com.crrl.beatplayer.databinding.AlbumItemBinding
import com.crrl.beatplayer.databinding.AlbumItemHeaderBinding
import com.crrl.beatplayer.databinding.ArtistDetailItemBinding
import com.crrl.beatplayer.extensions.deepEquals
import com.crrl.beatplayer.extensions.inflateWithBinding
import com.crrl.beatplayer.interfaces.ItemClickListener
import com.crrl.beatplayer.models.Album
import com.crrl.beatplayer.ui.viewmodels.MainViewModel
import com.crrl.beatplayer.utils.GeneralUtils.dip2px
import com.crrl.beatplayer.utils.GeneralUtils.screenWidth

private const val HEADER_TYPE = 0
private const val ITEM_TYPE = 1

class AlbumAdapter(private val context: Context?, private val mainViewModel: MainViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var lastClick = 0L

    var albumList: MutableList<Album> = mutableListOf()
    var showHeader = false
    var artistDetail = false
    var itemClickListener: ItemClickListener<Album>? = null
    var spanCount: Int = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemId = if (artistDetail) R.layout.artist_detail_item else R.layout.album_item
        return when (viewType) {
            HEADER_TYPE -> {
                val viewBinding =
                    parent.inflateWithBinding<AlbumItemHeaderBinding>(R.layout.album_item_header)
                ViewHolderAlbumHeader(viewBinding)
            }
            ITEM_TYPE -> {
                val viewBinding = parent.inflateWithBinding<ViewDataBinding>(itemId)
                ViewHolderAlbum(viewBinding)
            }
            else -> {
                val viewBinding = parent.inflateWithBinding<ViewDataBinding>(itemId)
                ViewHolderAlbum(viewBinding)
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
        albumList.size + 1
    } else {
        albumList.size
    }

    fun getItem(position: Int): Album? {
        return if (showHeader) {
            if (position == 0) {
                null
            } else {
                albumList[position - 1]
            }
        } else {
            albumList[position]
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentSong = if (!albumList.isNullOrEmpty()) getItem(position) else null
        when (getItemViewType(position)) {
            HEADER_TYPE -> {
                (holder as ViewHolderAlbumHeader).bind(albumList.size)
            }
            ITEM_TYPE -> {
                (holder as ViewHolderAlbum).bind(currentSong!!)
            }
        }
    }

    fun updateDataSet(newList: List<Album>) {
        if (!albumList.deepEquals(newList)) {
            albumList = newList.toMutableList()
            notifyDataSetChanged()
        }
    }

    inner class ViewHolderAlbum(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(album: Album) {
            if (binding is AlbumItemBinding)
                binding.apply {
                    this.album = album
                    binding.executePendingBindings()

                    showDetails.setOnClickListener(this@ViewHolderAlbum)
                    container.layoutParams.apply {
                        height = screenWidth / spanCount + dip2px(context!!, 42)
                        width = screenWidth / spanCount - dip2px(context, 6)
                    }
                }
            else {
                (binding as ArtistDetailItemBinding).apply {
                    this.album = album
                    this.size = itemCount
                    binding.executePendingBindings()

                    binding.root.setOnClickListener(this@ViewHolderAlbum)
                }
            }
        }

        override fun onClick(view: View) {
            if (SystemClock.elapsedRealtime() - lastClick < 500) {
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

    inner class ViewHolderAlbumHeader(private val binding: AlbumItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(albumCount: Int) {
            binding.apply {
                this.albumCount = albumCount
                viewModel = mainViewModel
                executePendingBindings()

                sortAlbum.setOnClickListener(this@ViewHolderAlbumHeader)
                playAllAlbum.setOnClickListener(this@ViewHolderAlbumHeader)
            }
        }

        override fun onClick(view: View) {
            if (itemClickListener != null)
                when (view.id) {
                    R.id.play_all_album -> itemClickListener!!.onPlayAllClick(view)
                    R.id.sort_album -> itemClickListener!!.onSortClick(view)
                }
        }
    }
}