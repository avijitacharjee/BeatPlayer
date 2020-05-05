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
import com.crrl.beatplayer.databinding.SelectSongItemBinding
import com.crrl.beatplayer.extensions.inflateWithBinding
import com.crrl.beatplayer.interfaces.ItemClickListener
import com.crrl.beatplayer.models.Song

class SelectSongAdapter(
    private val itemClickListener: ItemClickListener<Song>
) : RecyclerView.Adapter<SelectSongAdapter.ViewHolderSong>() {

    var songList: MutableList<Song> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderSong {
        val viewBinding =
            parent.inflateWithBinding<SelectSongItemBinding>(R.layout.select_song_item)
        return ViewHolderSong(viewBinding)
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    override fun onBindViewHolder(holder: ViewHolderSong, position: Int) {
        holder.bind(getItem(position))
    }

    fun getItem(position: Int): Song {
        return songList[position]
    }

    fun updateDataSet(list: MutableList<Song>) {
        songList = list
        notifyDataSetChanged()
    }

    inner class ViewHolderSong(private val binding: SelectSongItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(song: Song) {
            binding.apply {
                this.song = song
                this.position = adapterPosition
                this.size = itemCount
                executePendingBindings()

                container.setOnClickListener(this@ViewHolderSong)
                selected.setOnClickListener(this@ViewHolderSong)
            }
        }

        override fun onClick(view: View) {
            itemClickListener.onItemClick(
                binding.selected,
                adapterPosition,
                getItem(adapterPosition)
            )
        }
    }

}