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

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crrl.beatplayer.R
import com.crrl.beatplayer.databinding.PlaylistItemBinding
import com.crrl.beatplayer.extensions.deepEquals
import com.crrl.beatplayer.extensions.inflateWithBinding
import com.crrl.beatplayer.interfaces.ItemClickListener
import com.crrl.beatplayer.models.Playlist

class PlaylistAdapter : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    var playlists: MutableList<Playlist> = mutableListOf()

    var itemClickListener: ItemClickListener<Playlist>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewBinding = parent.inflateWithBinding<PlaylistItemBinding>(R.layout.playlist_item)
        return ViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    fun updateDataSet(newList: List<Playlist>) {
        if (!playlists.deepEquals(newList)) {
            playlists = newList.toMutableList()
            notifyDataSetChanged()
        }
    }

    private fun getItem(position: Int) = playlists[position]

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: PlaylistItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        fun bind(playlist: Playlist) {
            binding.apply {
                this.playlist = playlist
                this.size = itemCount
                position = adapterPosition
                itemMenu.setOnClickListener(this@ViewHolder)
                container.setOnClickListener(this@ViewHolder)
                executePendingBindings()
            }
        }

        override fun onClick(view: View?) {
            when (view!!.id) {
                R.id.container -> if (itemClickListener != null) itemClickListener!!.onItemClick(
                    view,
                    layoutPosition,
                    getItem(layoutPosition)
                )
                R.id.item_menu -> if (itemClickListener != null) itemClickListener!!.onPopupMenuClick(
                    view,
                    layoutPosition,
                    getItem(layoutPosition),
                    playlists
                )
            }
        }
    }
}