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

package com.crrl.beatplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.crrl.beatplayer.R
import com.crrl.beatplayer.databinding.FragmentSongBinding
import com.crrl.beatplayer.extensions.inflateWithBinding
import com.crrl.beatplayer.extensions.observe
import com.crrl.beatplayer.extensions.toIDList
import com.crrl.beatplayer.models.Song
import com.crrl.beatplayer.ui.adapters.SongAdapter
import com.crrl.beatplayer.ui.fragments.base.BaseFragment
import com.crrl.beatplayer.ui.viewmodels.PlaylistViewModel
import com.crrl.beatplayer.ui.viewmodels.SongViewModel
import com.crrl.beatplayer.ui.widgets.actions.AlertItemAction
import com.crrl.beatplayer.ui.widgets.stylers.AlertItemTheme
import com.crrl.beatplayer.utils.SettingsUtility
import com.crrl.beatplayer.utils.SortModes
import org.koin.android.ext.android.inject

class SongFragment : BaseFragment<Song>() {

    private lateinit var songAdapter: SongAdapter
    private lateinit var binding: FragmentSongBinding

    private val playlistViewModel by inject<PlaylistViewModel>()
    private val viewModel by inject<SongViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.fragment_song, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
    }

    private fun init() {
        songAdapter = SongAdapter(activity, mainViewModel).apply {
            showHeader = true
            itemClickListener = this@SongFragment
        }

        binding.songList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songAdapter
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        mainViewModel.getLastSong().observe(this){ song ->
            val position = songAdapter.songList.indexOfFirst { it.compare(song)} + 1
            songAdapter.notifyItemChanged(position)
        }

        mainViewModel.getCurrentSong().observe(this){
            val position = songAdapter.songList.indexOf(it) + 1
            songAdapter.notifyItemChanged(position)
        }

        viewModel.getSongList().observe(this) {
            songAdapter.updateDataSet(it)
        }

        binding.let {
            it.viewModel = viewModel
            it.lifecycleOwner = this
            it.executePendingBindings()
        }

        createDialog()
    }

    private fun createDialog(){
        dialog = buildSortModesDialog(listOf(
            AlertItemAction(
                context!!.getString(R.string.sort_default),
                SettingsUtility.getInstance(context).songSortOrder == SortModes.SongModes.SONG_DEFAULT,
                AlertItemTheme.DEFAULT
            ) { action ->
                action.selected = true
                SettingsUtility.getInstance(context).songSortOrder =
                    SortModes.SongModes.SONG_DEFAULT
                reloadAdapter()
            },
            AlertItemAction(
                context!!.getString(R.string.sort_az),
                SettingsUtility.getInstance(context).songSortOrder == SortModes.SongModes.SONG_A_Z,
                AlertItemTheme.DEFAULT
            ) { action ->
                action.selected = true
                SettingsUtility.getInstance(context).songSortOrder = SortModes.SongModes.SONG_A_Z
                reloadAdapter()
            },
            AlertItemAction(
                context!!.getString(R.string.sort_za),
                SettingsUtility.getInstance(context).songSortOrder == SortModes.SongModes.SONG_Z_A,
                AlertItemTheme.DEFAULT
            ) { action ->
                action.selected = true
                SettingsUtility.getInstance(context).songSortOrder = SortModes.SongModes.SONG_Z_A
                reloadAdapter()
            },
            AlertItemAction(
                context!!.getString(R.string.sort_duration),
                SettingsUtility.getInstance(context).songSortOrder == SortModes.SongModes.SONG_DURATION,
                AlertItemTheme.DEFAULT
            ) { action ->
                action.selected = true
                SettingsUtility.getInstance(context).songSortOrder =
                    SortModes.SongModes.SONG_DURATION
                reloadAdapter()
            },
            AlertItemAction(
                context!!.getString(R.string.sort_year),
                SettingsUtility.getInstance(context).songSortOrder == SortModes.SongModes.SONG_YEAR,
                AlertItemTheme.DEFAULT
            ) { action ->
                action.selected = true
                SettingsUtility.getInstance(context).songSortOrder = SortModes.SongModes.SONG_YEAR
                reloadAdapter()
            },
            AlertItemAction(
                context!!.getString(R.string.sort_last_added),
                SettingsUtility.getInstance(context).songSortOrder == SortModes.SongModes.SONG_LAST_ADDED,
                AlertItemTheme.DEFAULT
            ) { action ->
                action.selected = true
                SettingsUtility.getInstance(context).songSortOrder =
                    SortModes.SongModes.SONG_LAST_ADDED
                reloadAdapter()
            }
        ))
    }

    private fun reloadAdapter() {
        viewModel.update()
    }

    override fun onItemClick(view: View, position: Int, item: Song) {
        mainViewModel.update(item)
        mainViewModel.update(songAdapter.songList.toIDList())
    }

    override fun onShuffleClick(view: View) {
        mainViewModel.update(songAdapter.songList.toIDList())
        mainViewModel.update(mainViewModel.random(-1))
    }

    override fun onSortClick(view: View) {
        dialog.show(activity as AppCompatActivity)
    }

    override fun onPlayAllClick(view: View) {
        mainViewModel.update(songAdapter.songList.first())
        mainViewModel.update(songAdapter.songList.toIDList())
    }

    override fun onPopupMenuClick(view: View, position: Int, item: Song, itemList: List<Song>) {
        super.onPopupMenuClick(view, position, item, itemList)
        powerMenu!!.showAsAnchorRightTop(view)
        playlistViewModel.playLists().observe(this) {
            buildPlaylistMenu(it, item)
        }
    }
}