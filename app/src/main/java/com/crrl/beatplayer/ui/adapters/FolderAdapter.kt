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
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crrl.beatplayer.R
import com.crrl.beatplayer.databinding.FolderItemBinding
import com.crrl.beatplayer.extensions.deepEquals
import com.crrl.beatplayer.extensions.inflateWithBinding
import com.crrl.beatplayer.interfaces.ItemClickListener
import com.crrl.beatplayer.models.Folder

class FolderAdapter(private val context: Context?) :
    RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    var folderList: MutableList<Folder> = mutableListOf()
    var itemClickListener: ItemClickListener<Folder>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewBinding = parent.inflateWithBinding<FolderItemBinding>(R.layout.folder_item)
        return ViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return folderList.size
    }

    fun updateDataSet(newList: List<Folder>) {
        if (!folderList.deepEquals(newList)) {
            folderList = newList.toMutableList()
            notifyDataSetChanged()
        }
    }

    private fun getItem(position: Int) = folderList[position]

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: FolderItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        fun bind(folder: Folder) {
            binding.apply {
                this.folder = folder
                this.size = itemCount
                position = adapterPosition
                itemMenu.setOnClickListener(this@ViewHolder)
                container.setOnClickListener(this@ViewHolder)
                executePendingBindings()
            }
        }

        override fun onClick(view: View) {
            if (itemClickListener != null)
                when (view.id) {
                    R.id.item_menu -> itemClickListener!!.onPopupMenuClick(
                        view,
                        adapterPosition,
                        getItem(adapterPosition),
                        folderList
                    )
                    R.id.container -> itemClickListener!!.onItemClick(
                        view,
                        adapterPosition,
                        getItem(adapterPosition)
                    )
                }
        }
    }
}