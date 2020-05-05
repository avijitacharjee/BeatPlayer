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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.crrl.beatplayer.R
import com.crrl.beatplayer.databinding.FragmentAlbumDetailBinding
import com.crrl.beatplayer.extensions.*
import com.crrl.beatplayer.models.Album
import com.crrl.beatplayer.models.Song
import com.crrl.beatplayer.repository.FavoritesRepository
import com.crrl.beatplayer.ui.adapters.SongAdapter
import com.crrl.beatplayer.ui.fragments.base.BaseFragment
import com.crrl.beatplayer.ui.viewmodels.AlbumViewModel
import com.crrl.beatplayer.ui.viewmodels.FavoriteViewModel
import com.crrl.beatplayer.ui.viewmodels.PlaylistViewModel
import com.crrl.beatplayer.utils.GeneralUtils
import com.crrl.beatplayer.utils.PlayerConstants
import org.koin.android.ext.android.inject

class AlbumDetailFragment : BaseFragment<Song>() {

    private lateinit var album: Album
    private lateinit var songAdapter: SongAdapter
    private lateinit var binding: FragmentAlbumDetailBinding
    private val favoriteViewModel by inject<FavoriteViewModel>()
    private val albumViewModel by inject<AlbumViewModel>()
    private val playlistViewModel by inject<PlaylistViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.fragment_album_detail, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        val id = arguments!!.getLong(PlayerConstants.ALBUM_KEY)
        album = albumViewModel.getAlbum(id)

        songAdapter = SongAdapter(context, mainViewModel).apply {
            itemClickListener = this@AlbumDetailFragment
            showHeader = true
            isAlbumDetail = true
        }

        binding.apply {
            albumSongList.apply {
                layoutManager = LinearLayoutManager(context)
                clipToOutline = true
                adapter = songAdapter
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            addFavorites.setOnClickListener { toggleAddFav() }
        }

        albumViewModel.getSongsByAlbum(album.id)!!.observe(this) {
            if (!songAdapter.songList.deepEquals(it)) {
                songAdapter.updateDataSet(it)
                binding.totalDuration = GeneralUtils.getTotalTime(songAdapter.songList).toInt()
            }
            if (it.isEmpty()) {
                favoriteViewModel.deleteSong(id)
                safeActivity.onBackPressed()
            }
        }

        mainViewModel.getCurrentSong().observe(this) {
            val position = songAdapter.songList.indexOf(it) + 1
            songAdapter.notifyItemChanged(position)
        }

        mainViewModel.getLastSong().observe(this){ song ->
            val position = songAdapter.songList.indexOfFirst { it.compare(song)} + 1
            songAdapter.notifyItemChanged(position)
        }

        binding.let {
            it.viewModel = albumViewModel
            it.mainViewModel = mainViewModel
            it.album = album
            it.executePendingBindings()

            it.lifecycleOwner = this
        }
    }

    override fun onItemClick(view: View, position: Int, item: Song) {
        mainViewModel.update(item)
        mainViewModel.update(songAdapter.songList.toIDList())
    }

    override fun onShuffleClick(view: View) {
        mainViewModel.update(songAdapter.songList.toIDList())
        mainViewModel.update(mainViewModel.random(-1))
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

    private fun toggleAddFav() {
        val favoritesRepository = FavoritesRepository(context)
        if (favoritesRepository.favExist(album.id)) {
            val resp = favoritesRepository.deleteFavorites(longArrayOf(album.id))
            showSnackBar(view, resp, 0, R.string.album_no_fav_ok)
        } else {
            val resp = favoritesRepository.createFavorite(album.toFavorite())
            showSnackBar(view, resp, 1, R.string.album_fav_ok)
        }
    }
}
