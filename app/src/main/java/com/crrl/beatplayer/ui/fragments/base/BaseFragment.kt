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

package com.crrl.beatplayer.ui.fragments.base

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.crrl.beatplayer.R
import com.crrl.beatplayer.extensions.*
import com.crrl.beatplayer.interfaces.ItemClickListener
import com.crrl.beatplayer.models.MediaItem
import com.crrl.beatplayer.models.Playlist
import com.crrl.beatplayer.models.Song
import com.crrl.beatplayer.repository.PlaylistRepository
import com.crrl.beatplayer.repository.SongsRepository
import com.crrl.beatplayer.ui.activities.SelectSongActivity
import com.crrl.beatplayer.ui.fragments.FavoriteDetailFragment
import com.crrl.beatplayer.ui.fragments.PlaylistDetailFragment
import com.crrl.beatplayer.ui.viewmodels.FavoriteViewModel
import com.crrl.beatplayer.ui.viewmodels.MainViewModel
import com.crrl.beatplayer.ui.viewmodels.PlaylistViewModel
import com.crrl.beatplayer.ui.widgets.AlertDialog
import com.crrl.beatplayer.ui.widgets.actions.AlertItemAction
import com.crrl.beatplayer.ui.widgets.stylers.AlertItemStyle
import com.crrl.beatplayer.ui.widgets.stylers.AlertItemTheme
import com.crrl.beatplayer.ui.widgets.stylers.AlertType
import com.crrl.beatplayer.ui.widgets.stylers.InputStyle
import com.crrl.beatplayer.utils.GeneralUtils.addZeros
import com.crrl.beatplayer.utils.PlayerConstants
import com.crrl.beatplayer.utils.PlayerConstants.FAVORITE_ID
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import org.koin.android.ext.android.inject


open class BaseFragment<T : MediaItem> : CoroutineFragment(), ItemClickListener<T> {

    private lateinit var currentItemList: List<T>
    private lateinit var currentItem: T
    private var alertPlaylists: AlertDialog? = null
    protected lateinit var dialog: AlertDialog
    protected val mainViewModel by inject<MainViewModel>()
    protected var powerMenu: PowerMenu? = null

    private val favoriteViewModel by inject<FavoriteViewModel>()
    private val playlistViewModel by inject<PlaylistViewModel>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
    }

    private fun init() {
        powerMenu = initPopUpMenu().setOnMenuItemClickListener(onMenuItemClickListener).build()
    }

    protected open fun buildPlaylistMenu(playlists: List<Playlist>, song: Song) {
        val style = AlertItemStyle().apply {
            textColor = activity?.getColorByTheme(R.attr.titleTextColor)!!
            selectedTextColor = activity?.getColorByTheme(R.attr.colorAccent)!!
            backgroundColor =
                activity?.getColorByTheme(R.attr.colorPrimarySecondary2)!!
        }
        val alert = AlertDialog(
            getString(R.string.playlists),
            getString(R.string.choose_playlist),
            style,
            AlertType.BOTTOM_SHEET
        ).apply {
            addItem(AlertItemAction(getString(R.string.favorites), false) {
                addFavorite()
            })
            playlists.forEach { playlist ->
                addItem(AlertItemAction(playlist.name, false) {
                    addToList(playlist.id, song)
                })
            }
            addItem(
                AlertItemAction(getString(R.string.new_playlist), false, AlertItemTheme.ACCEPT) {
                    createDialog(song)
                })
        }
        alertPlaylists = alert
    }

    private fun addFavorite() {
        val added = favoriteViewModel.addToFavorite(
            FAVORITE_ID,
            listOf(currentItem as Song)
        )
        if (added > 0)
            mainViewModel.binding.mainContainer.snackbar(
                SUCCESS, getString(R.string.song_added_success), LENGTH_SHORT
            )
        else
            mainViewModel.binding.mainContainer.snackbar(
                ERROR,
                getString(R.string.song_added_error),
                LENGTH_SHORT,
                action = getString(R.string.retry),
                clickListener = View.OnClickListener {
                    addFavorite()
                })
    }

    protected fun buildSortModesDialog(actions: List<AlertItemAction>): AlertDialog {
        val style = AlertItemStyle()
        style.apply {
            textColor = safeActivity.getColorByTheme(R.attr.titleTextColor)
            selectedTextColor = safeActivity.getColorByTheme(R.attr.colorAccent)
            backgroundColor = safeActivity.getColorByTheme(R.attr.colorPrimarySecondary2)
            cornerRadius = resources.getDimension(R.dimen.bottom_panel_radius)
        }
        return AlertDialog(
            getString(R.string.sort_title),
            getString(R.string.sort_msg),
            style,
            AlertType.BOTTOM_SHEET
        ).apply {
            for (action in actions) addItem(action)
        }
    }

    protected open fun createDialog(song: Song? = null, text: String? = null) {
        val style = InputStyle(
            safeActivity.getColorByTheme(R.attr.colorPrimarySecondary2),
            safeActivity.getColorByTheme(R.attr.colorPrimaryOpacity),
            safeActivity.getColorByTheme(R.attr.titleTextColor),
            safeActivity.getColorByTheme(R.attr.bodyTextColor),
            safeActivity.getColorByTheme(R.attr.colorAccent),
            text ?: "${safeActivity.getString(R.string.playlist)} ${addZeros(
                PlaylistRepository(context).getPlayListsCount() + 1
            )}",
            resources.getDimension(R.dimen.bottom_panel_radius)
        )
        AlertDialog(
            getString(R.string.new_playlist),
            getString(R.string.create_playlist),
            style,
            AlertType.INPUT,
            getString(R.string.input_hint)
        ).apply {
            addItem(AlertItemAction("Cancel", false, AlertItemTheme.DEFAULT) {
            })
            addItem(AlertItemAction("OK", false, AlertItemTheme.ACCEPT) { action ->
                val exists = playlistViewModel.exists(action.input!!)
                if (exists) mainViewModel.binding.mainContainer.snackbar(
                    ERROR,
                    getString(R.string.playlist_name_error),
                    LENGTH_SHORT,
                    action = getString(R.string.retry),
                    clickListener = View.OnClickListener {
                        createDialog(song, action.input)
                    }
                )
                else addSongs(action.input!!, song)
            })
        }.show(safeActivity as AppCompatActivity)
    }

    private fun addSongs(name: String, song: Song?) {
        if (song != null) {
            val id = playlistViewModel.create(name, listOf(song))
            if (id != -1L) {
                mainViewModel.binding.mainContainer.snackbar(
                    SUCCESS,
                    getString(R.string.playlist_added_success),
                    LENGTH_SHORT
                )
            } else {
                mainViewModel.binding.mainContainer.snackbar(
                    ERROR,
                    getString(R.string.playlist_added_error, name),
                    LENGTH_LONG
                )
            }
        } else {
            val options = ActivityOptions.makeSceneTransitionAnimation(safeActivity)
            val intent = Intent(safeActivity, SelectSongActivity::class.java).apply {
                putExtra(PlayerConstants.PLAY_LIST_DETAIL, name)
            }
            safeActivity.startActivityForResult(intent, 1, options.toBundle())
        }
    }

    private fun initPopUpMenu(): PowerMenu.Builder {
        return PowerMenu.Builder(context).apply {
            addItem(PowerMenuItem(getString(R.string.play)))
            if (this@BaseFragment !is PlaylistDetailFragment && this@BaseFragment !is FavoriteDetailFragment)
                addItem(PowerMenuItem(getString(R.string.add)))
            addItem(PowerMenuItem(getString(R.string.share)))
            setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT)
            setMenuRadius(resources.getDimension(R.dimen.popupMenuRadius))
            setOnBackgroundClickListener { powerMenu!!.dismiss() }
            setMenuShadow(5f)
            setShowBackground(false)
            setTextColor(safeActivity.getColorByTheme(R.attr.titleTextColor))
            setTextGravity(Gravity.START)
            setSelectedTextColor(safeActivity.getColorByTheme(R.attr.colorAccent))
            setTextSize(16)
            setTextTypeface(
                Typeface.createFromAsset(
                    context!!.assets,
                    "fonts/product_sans_regular.ttf"
                )
            )
            setMenuColor(safeActivity.getColorByTheme(R.attr.colorPrimarySecondary))
            setSelectedMenuColor(safeActivity.getColorByTheme(R.attr.colorPrimarySecondary))
            if (this@BaseFragment is PlaylistDetailFragment || this@BaseFragment is FavoriteDetailFragment)
                addItem(PowerMenuItem(getString(R.string.remove)))
            else {
                addItem(PowerMenuItem(getString(R.string.delete)))
            }
        }

    }

    private fun createConfDialog(song: Song): AlertDialog {
        val style = AlertItemStyle().apply {
            textColor = safeActivity.getColorByTheme(R.attr.titleTextColor)
            selectedTextColor = safeActivity.getColorByTheme(R.attr.colorAccent)
            selectedBackgroundColor = safeActivity.getColorByTheme(R.attr.colorAccentOpacity)
            backgroundColor =
                activity?.getColorByTheme(R.attr.colorPrimarySecondary2)!!
        }

        return AlertDialog(
            getString(R.string.delete_conf_title),
            getString(R.string.delete_conf, song.title),
            style,
            AlertType.BOTTOM_SHEET
        ).apply {
            addItem(AlertItemAction(getString(R.string.delete), false, AlertItemTheme.ACCEPT) {
                deleteItem(song.id)
            })
        }
    }

    private fun deleteItem(id: Long) {
        val resp =
            SongsRepository(context).deleteTracks(longArrayOf(id))
        if (resp > 0)
            view.snackbar(SUCCESS, getString(R.string.deleted_ok), LENGTH_SHORT)
        else
            view.snackbar(ERROR, getString(R.string.deleted_err), LENGTH_SHORT,
                action = getString(R.string.retry),
                clickListener = View.OnClickListener {
                    deleteItem(id)
                })
    }

    private val onMenuItemClickListener = OnMenuItemClickListener<PowerMenuItem> { position, _ ->
        when (position) {
            0 -> {
                mainViewModel.update(currentItem as Song)
                mainViewModel.update(currentItemList.toIDList())
            }
            1 -> {
                when (this) {
                    is PlaylistDetailFragment, is FavoriteDetailFragment -> shareItem()
                    else -> alertPlaylists?.show(safeActivity as AppCompatActivity)
                }
            }
            2 -> {
                when (this) {
                    is PlaylistDetailFragment -> removeFromList(binding.playlist!!.id, currentItem)
                    is FavoriteDetailFragment -> favoriteViewModel.remove(
                        FAVORITE_ID,
                        longArrayOf(currentItem._id)
                    )
                    else -> shareItem()
                }
            }
            3 -> {
                createConfDialog(currentItem as Song).show(safeActivity as AppCompatActivity)
            }
        }
        powerMenu!!.dismiss()
    }

    private fun addToList(playListId: Long, song: Song) {
        val added = playlistViewModel.addToPlaylist(playListId, listOf(song))
        if (added != -1L)
            mainViewModel.binding.mainContainer.snackbar(
                SUCCESS,
                getString(R.string.song_added_success),
                LENGTH_SHORT
            )
        else
            mainViewModel.binding.mainContainer.snackbar(
                ERROR,
                getString(R.string.song_added_error),
                LENGTH_SHORT
            )
    }

    fun initNeeded(item: T, itemList: List<T>) {
        currentItem = item
        currentItemList = itemList
    }

    fun shareItem() {
        val song = currentItem as Song
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, Uri.parse(song.path))
            type = "audio/mp3"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    open fun onBackPressed(): Boolean {
        return if (powerMenu != null) {
            if (powerMenu!!.isShowing) {
                powerMenu!!.dismiss()
                false
            } else {
                true
            }
        } else {
            true
        }
    }

    protected open fun showSnackBar(view: View?, resp: Int, type: Int, @StringRes message: Int) {
        val custom = when (type) {
            1 -> R.drawable.ic_success
            else -> R.drawable.ic_dislike
        }
        if (resp > 0) view.snackbar(
            CUSTOM,
            getString(message),
            BaseTransientBottomBar.LENGTH_SHORT,
            custom = custom
        )
    }

    open fun removeFromList(playListId: Long, item: T?) {}
    override fun onItemClick(view: View, position: Int, item: T) {}
    override fun onShuffleClick(view: View) {}
    override fun onSortClick(view: View) {}
    override fun onPlayAllClick(view: View) {}

    override fun onPopupMenuClick(view: View, position: Int, item: T, itemList: List<T>) {
        initNeeded(item, itemList)
    }
}

