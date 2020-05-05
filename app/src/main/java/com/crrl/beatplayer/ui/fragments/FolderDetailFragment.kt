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
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.crrl.beatplayer.R
import com.crrl.beatplayer.databinding.FragmentFolderDetailBinding
import com.crrl.beatplayer.extensions.inflateWithBinding
import com.crrl.beatplayer.extensions.observe
import com.crrl.beatplayer.extensions.toIDList
import com.crrl.beatplayer.models.Song
import com.crrl.beatplayer.repository.FavoritesRepository
import com.crrl.beatplayer.ui.adapters.SongAdapter
import com.crrl.beatplayer.ui.fragments.base.BaseFragment
import com.crrl.beatplayer.ui.viewmodels.FavoriteViewModel
import com.crrl.beatplayer.ui.viewmodels.FolderViewModel
import com.crrl.beatplayer.ui.viewmodels.PlaylistViewModel
import com.crrl.beatplayer.utils.PlayerConstants.FAVORITE_NAME
import com.crrl.beatplayer.utils.PlayerConstants.FOLDER_KEY
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class FolderDetailFragment : BaseFragment<Song>() {

    private lateinit var binding: FragmentFolderDetailBinding
    private lateinit var songAdapter: SongAdapter

    private val folderViewModel by viewModel<FolderViewModel>()
    private val favoriteViewModel by inject<FavoriteViewModel>()
    private val playlistViewModel by inject<PlaylistViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.fragment_folder_detail, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
    }

    fun init() {
        val id = arguments?.getString(FOLDER_KEY)!!
        binding.isLoading = true
        binding.name = arguments?.getString(FAVORITE_NAME)

        songAdapter = SongAdapter(context, mainViewModel).apply {
            showHeader = true
            isAlbumDetail = true
            itemClickListener = this@FolderDetailFragment
        }
        postponeEnterTransition()
        folderViewModel.getFolder(id).observe(this){ folder ->
            binding.folder = folder
            folderViewModel.getSongsByFolder(folder.realPath).observe(this) {
                songAdapter.updateDataSet(it)
                binding.isLoading = false
                if (it.isEmpty()) {
                    favoriteViewModel.deleteFavorites(longArrayOf(folder.id))
                    activity?.onBackPressed()
                }
                (view?.parent as? ViewGroup)?.doOnPreDraw {
                    startPostponedEnterTransition()
                }
            }
        }

        mainViewModel.getLastSong().observe(this){ song ->
            val position = songAdapter.songList.indexOfFirst { it.compare(song)} + 1
            songAdapter.notifyItemChanged(position)
        }

        mainViewModel.getCurrentSong().observe(this){
            val position = songAdapter.songList.indexOf(it) + 1
            songAdapter.notifyItemChanged(position)
        }

        binding.apply {
            songList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = songAdapter
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            }
            addFavorites.setOnClickListener { toggleAddFav() }
        }

        binding.let {
            it.mainViewModel = mainViewModel
            it.lifecycleOwner = this
            it.executePendingBindings()
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
        if (favoritesRepository.favExist(binding.folder!!.id)) {
            val resp = favoritesRepository.deleteFavorites(longArrayOf(binding.folder!!.id))
            showSnackBar(view, resp, 0, R.string.folder_no_fav_ok)
        } else {
            val resp = favoritesRepository.createFavorite(binding.folder!!.toFavorite())
            showSnackBar(view, resp, 1, R.string.folder_fav_ok)
        }
    }
}
