package com.crrl.beatplayer.ui.adapters

import android.app.Activity
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.crrl.beatplayer.R
import com.crrl.beatplayer.databinding.SearchItemBinding
import com.crrl.beatplayer.extensions.deepEquals
import com.crrl.beatplayer.extensions.inflateWithBinding
import com.crrl.beatplayer.interfaces.ItemClickListener
import com.crrl.beatplayer.models.*
import com.crrl.beatplayer.ui.viewmodels.MainViewModel
import com.crrl.beatplayer.ui.viewmodels.SearchViewModel
import com.crrl.beatplayer.utils.PlayerConstants.ALBUM_TYPE
import com.crrl.beatplayer.utils.PlayerConstants.ARTIST_TYPE
import com.crrl.beatplayer.utils.PlayerConstants.SONG_TYPE

@Suppress("UNCHECKED_CAST")
class SearchAdapter(
    activity: Activity,
    mainViewModel: MainViewModel,
    private val viewModel: SearchViewModel,
    itemClickListener: ItemClickListener<MediaItem>,
    private val sc: Int = 2
) : RecyclerView.Adapter<SearchAdapter.ViewHolderSearch>() {

    val songAdapter: SongAdapter = SongAdapter(activity, mainViewModel).apply {
        this.itemClickListener = itemClickListener as ItemClickListener<Song>
        showHeader = false
        isSearchView = true
    }
    private val albumAdapter = AlbumAdapter(activity, mainViewModel).apply {
        this.itemClickListener = itemClickListener as ItemClickListener<Album>
        spanCount = sc
    }
    private val artistAdapter = ArtistAdapter(activity, mainViewModel).apply {
        this.itemClickListener = itemClickListener as ItemClickListener<Artist>
        spanCount = sc
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderSearch {
        val viewBinding = parent.inflateWithBinding<SearchItemBinding>(R.layout.search_item)
        return ViewHolderSearch(viewBinding)
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: ViewHolderSearch, position: Int) {
        holder.bind()
    }

    fun updateDataSet(data: SearchData) {
        if (!songAdapter.songList.deepEquals(data.songList)) {
            songAdapter.updateDataSet(data.songList)
            notifyItemChanged(0)
        }
        if (!albumAdapter.albumList.deepEquals(data.albumList)) {
            albumAdapter.updateDataSet(data.albumList)
            notifyItemChanged(1)
        }
        if (!artistAdapter.artistList.deepEquals(data.artistList)) {
            artistAdapter.updateDataSet(data.artistList)
            notifyItemChanged(2)
        }
    }

    inner class ViewHolderSearch(private val binding: SearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.let {
                it.viewModel = viewModel
                it.type = SONG_TYPE
                it.executePendingBindings()
            }
            when (adapterPosition) {
                0 -> {
                    binding.apply {
                        type = SONG_TYPE
                        list.apply {
                            layoutManager = LinearLayoutManager(context)
                            adapter = songAdapter
                            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                        }
                    }
                }
                1 -> {
                    binding.apply {
                        type = ALBUM_TYPE
                        list.apply {
                            layoutManager = GridLayoutManager(context, sc)
                            adapter = albumAdapter
                        }
                    }
                }
                2 -> {
                    binding.apply {
                        type = ARTIST_TYPE
                        list.apply {
                            layoutManager = GridLayoutManager(context, sc)
                            adapter = artistAdapter
                        }
                    }
                }
            }
        }
    }
}