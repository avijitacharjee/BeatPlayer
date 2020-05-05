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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.crrl.beatplayer.R
import com.crrl.beatplayer.databinding.FragmentAlbumBinding
import com.crrl.beatplayer.extensions.addFragment
import com.crrl.beatplayer.extensions.inflateWithBinding
import com.crrl.beatplayer.extensions.observe
import com.crrl.beatplayer.extensions.safeActivity
import com.crrl.beatplayer.models.Album
import com.crrl.beatplayer.ui.adapters.AlbumAdapter
import com.crrl.beatplayer.ui.fragments.base.BaseFragment
import com.crrl.beatplayer.ui.viewmodels.AlbumViewModel
import com.crrl.beatplayer.ui.widgets.actions.AlertItemAction
import com.crrl.beatplayer.ui.widgets.stylers.AlertItemTheme
import com.crrl.beatplayer.utils.GeneralUtils
import com.crrl.beatplayer.utils.GeneralUtils.VERTICAL
import com.crrl.beatplayer.utils.PlayerConstants
import com.crrl.beatplayer.utils.PlayerConstants.ALBUM_KEY
import com.crrl.beatplayer.utils.SettingsUtility
import com.crrl.beatplayer.utils.SortModes
import org.koin.android.ext.android.inject

class AlbumFragment : BaseFragment<Album>() {

    private lateinit var albumAdapter: AlbumAdapter
    private lateinit var binding: FragmentAlbumBinding
    private val albumViewModel by inject<AlbumViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.fragment_album, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
    }

    private fun init() {
        val sc = if (GeneralUtils.getRotation(safeActivity) == VERTICAL) 2 else 5

        albumAdapter = AlbumAdapter(context, mainViewModel).apply {
            showHeader = true
            itemClickListener = this@AlbumFragment
            spanCount = sc
        }

        binding.albumList.apply {
            layoutManager = GridLayoutManager(context, sc).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (position == 0) sc else 1
                    }
                }
            }
            adapter = albumAdapter
        }

        albumViewModel.getAlbums().observe(this) { list ->
            albumAdapter.updateDataSet(list)
        }

        binding.let {
            it.viewModel = albumViewModel
            it.lifecycleOwner = this
            it.executePendingBindings()
        }

        createDialog()
    }

    private fun createDialog(){
        dialog = buildSortModesDialog(listOf(
            AlertItemAction(
                context!!.getString(R.string.sort_default),
                SettingsUtility.getInstance(context).albumSortOrder == SortModes.AlbumModes.ALBUM_DEFAULT,
                AlertItemTheme.DEFAULT
            ) { action ->
                action.selected = true
                SettingsUtility.getInstance(context).albumSortOrder =
                    SortModes.AlbumModes.ALBUM_DEFAULT
                reloadAdapter()
            },
            AlertItemAction(
                context!!.getString(R.string.sort_az),
                SettingsUtility.getInstance(context).albumSortOrder == SortModes.AlbumModes.ALBUM_A_Z,
                AlertItemTheme.DEFAULT
            ) { action ->
                action.selected = true
                SettingsUtility.getInstance(context).albumSortOrder = SortModes.AlbumModes.ALBUM_A_Z
                reloadAdapter()
            },
            AlertItemAction(
                context!!.getString(R.string.sort_za),
                SettingsUtility.getInstance(context).albumSortOrder == SortModes.AlbumModes.ALBUM_Z_A,
                AlertItemTheme.DEFAULT
            ) { action ->
                action.selected = true
                SettingsUtility.getInstance(context).albumSortOrder = SortModes.AlbumModes.ALBUM_Z_A
                reloadAdapter()
            },
            AlertItemAction(
                context!!.getString(R.string.sort_year),
                SettingsUtility.getInstance(context).albumSortOrder == SortModes.AlbumModes.ALBUM_YEAR,
                AlertItemTheme.DEFAULT
            ) { action ->
                action.selected = true
                SettingsUtility.getInstance(context).albumSortOrder =
                    SortModes.AlbumModes.ALBUM_YEAR
                reloadAdapter()
            }
        ))
    }

    private fun reloadAdapter() {
        albumViewModel.update()
    }

    override fun onItemClick(view: View, position: Int, item: Album) {
        val extras = Bundle()
        extras.putLong(ALBUM_KEY, item.id)
        activity!!.addFragment(
            R.id.nav_host_fragment,
            AlbumDetailFragment(),
            PlayerConstants.ALBUM_DETAIL,
            true,
            extras
        )
    }

    override fun onPopupMenuClick(view: View, position: Int, item: Album, itemList: List<Album>) {
        Toast.makeText(context, "Menu of " + item.title, Toast.LENGTH_SHORT).show()
    }

    override fun onPlayAllClick(view: View) {
        Toast.makeText(context, "Shuffle", Toast.LENGTH_SHORT).show()
    }

    override fun onSortClick(view: View) {
        dialog.show(activity as AppCompatActivity)
    }
}
