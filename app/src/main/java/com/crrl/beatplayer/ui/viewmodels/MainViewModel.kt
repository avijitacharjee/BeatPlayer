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

package com.crrl.beatplayer.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crrl.beatplayer.R
import com.crrl.beatplayer.databinding.ActivityMainBinding
import com.crrl.beatplayer.extensions.delete
import com.crrl.beatplayer.extensions.toIDList
import com.crrl.beatplayer.models.Song
import com.crrl.beatplayer.playback.MusicService
import com.crrl.beatplayer.repository.*
import com.crrl.beatplayer.ui.viewmodels.base.CoroutineViewModel
import com.crrl.beatplayer.utils.GeneralUtils
import com.crrl.beatplayer.utils.LyricsExtractor
import com.crrl.beatplayer.utils.PlayerConstants.ALBUM_TYPE
import com.crrl.beatplayer.utils.PlayerConstants.FAVORITE_TYPE
import com.crrl.beatplayer.utils.PlayerConstants.FOLDER_TYPE
import com.crrl.beatplayer.utils.PlayerConstants.PLAY_LIST_TYPE
import com.crrl.beatplayer.utils.PlayerConstants.SONG_TYPE
import com.crrl.beatplayer.utils.SettingsUtility
import com.github.florent37.kotlin.pleaseanimate.please
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext

class MainViewModel(
    private val context: Context,
    private val songRepository: SongsRepository,
    private val albumsRepository: AlbumsRepository,
    private val playlistRepository: PlaylistRepository,
    private val foldersRepository: FoldersRepository,
    private val favoritesRepository: FavoritesRepository,
    val settingsUtility: SettingsUtility
) : CoroutineViewModel(Main) {

    private val liveSongData = MutableLiveData<Song>()
    private val timeLiveData = MutableLiveData<Int>()
    private val rawLiveData = MutableLiveData<ByteArray>()
    private val currentSongList = MutableLiveData<LongArray>()
    private val isFavLiveData = MutableLiveData<Boolean>()
    private val isAlbumFavLiveData = MutableLiveData<Boolean>()
    private val isSongFavLiveData = MutableLiveData<Boolean>()
    private val currentListName = MutableLiveData<String>()
    private val lyrics: MutableLiveData<String> = MutableLiveData()
    private val lastSong = MutableLiveData<Song>()

    val musicService = MusicService()
    lateinit var binding: ActivityMainBinding

    init {
        getSongsByType()
    }

    fun getLastSong(): LiveData<Song> = lastSong

    fun getLyrics(song: Song): LiveData<String> {
        loadLyrics(song)
        return lyrics
    }

    fun isFav(id: Long): LiveData<Boolean> {
        launch {
            val isFav = withContext(IO) {
                favoritesRepository.favExist(id)
            }
            isFavLiveData.postValue(isFav)
        }
        return isFavLiveData
    }

    fun isAlbumFav(id: Long): LiveData<Boolean> {
        launch {
            val isFav = withContext(IO) {
                favoritesRepository.favExist(id)
            }
            isAlbumFavLiveData.postValue(isFav)
        }
        return isAlbumFavLiveData
    }

    fun isSongFav(id: Long): LiveData<Boolean> {
        launch {
            val isFav = withContext(IO) {
                favoritesRepository.songExist(id)
            }
            isSongFavLiveData.postValue(isFav)
        }
        return isSongFavLiveData
    }

    private fun loadLyrics(song: Song) {
        val currentSong = getCurrentSong().value ?: return
        launch {
            val lyric = withContext(IO) {
                LyricsExtractor.getLyric(song) ?: context.getString(R.string.no_lyrics)
            }
            if (currentSong.id == song.id && lyric != lyrics.value) {
                lyrics.postValue(lyric)
            }
        }
    }

    private fun getSongsByType() {
        val str = settingsUtility.currentSongList ?: return
        when (str.split("<,>")[0]) {
            SONG_TYPE -> launch {
                val list = withContext(IO) {
                    songRepository.loadSongs()
                }
                update(list.toIDList())
            }
            ALBUM_TYPE -> launch {
                val list = withContext(IO) {
                    val id = str.split("<,>")
                    albumsRepository.getSongsForAlbum(if (id.isNotEmpty()) id[1].toLong() else 0L)
                }
                update(list.toIDList())
            }
            PLAY_LIST_TYPE -> launch {
                val list = withContext(IO) {
                    val id = str.split("<,>")
                    playlistRepository.getSongsInPlaylist((if (id.isNotEmpty()) id[1].toLong() else 0L))
                }
                update(list.toIDList())
            }
            FOLDER_TYPE -> launch {
                val list = withContext(IO) {
                    val id = str.split("<,>")
                    foldersRepository.getSongsForIds((if (id.isNotEmpty()) id[1] else ""))
                }
                update(list.toIDList())
            }
            FAVORITE_TYPE -> launch {
                val list = withContext(IO) {
                    val id = str.split("<,>")
                    favoritesRepository.getSongsForFavorite((if (id.isNotEmpty()) id[1].toLong() else 0L))
                }
                update(list.toIDList())
            }
        }
    }

    fun next(currentSong: Long) {
        currentSongList.value ?: return
        val song = songRepository.getSongForId(musicService.next(currentSong))
        update(song)
    }

    fun previous(currentSong: Long) {
        currentSongList.value ?: return
        val song = songRepository.getSongForId(musicService.previous(currentSong))
        update(song)
    }

    fun random(currentSong: Long = -1): Song {
        currentSongList.value ?: return Song()
        return songRepository.getSongForId(musicService.random(currentSong))
    }

    fun update(currentList: String) {
        currentListName.value = currentList
    }

    fun update(newTime: Int) {
        timeLiveData.postValue(if (newTime == -1) newTime else newTime / 1000 * 1000)
    }

    fun update(item: Song = Song()) {
        val song = liveSongData.value
        if (song?.id != item.id) {
            lastSong.postValue(song)
            liveSongData.value = item
            launch {
                val raw = withContext(IO) {
                    GeneralUtils.audio2Raw(context, Uri.parse(item.path))
                }
                update(raw)
            }
            update(-1)
        }
    }

    fun removeDeletedSong(id: Long) {
        val currentList = currentSongList.value ?: return
        update(currentList.toMutableList().apply { delete(id) }.toLongArray())
    }

    fun update(newList: LongArray) {
        currentSongList.value = newList
    }

    fun update(raw: ByteArray?) {
        raw ?: return
        rawLiveData.postValue(raw)
    }

    fun hideMiniPlayer() {
        binding.apply {
            bottomControls.isEnabled = false
            please(190) {
                animate(bottomControls) {
                    belowOf(mainContainer)
                }
            }.start()
        }
    }

    fun showMiniPlayer() {
        if (getCurrentSong().value != null && getCurrentSong().value?.id != -1L)
            binding.apply {
                bottomControls.isEnabled = true
                please(190) {
                    animate(bottomControls) {
                        bottomOfItsParent()
                    }
                }.start()
            }
    }

    fun getCurrentSong(): LiveData<Song> = liveSongData
    fun getCurrentSongList(): LiveData<LongArray> = currentSongList
    fun getTime(): LiveData<Int> = timeLiveData
    fun getRawData(): LiveData<ByteArray> = rawLiveData
}

