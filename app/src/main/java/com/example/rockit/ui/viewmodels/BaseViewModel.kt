package com.example.rockit.ui.viewmodels

import android.content.Context
import android.icu.lang.UCharacterDirection
import android.net.Uri
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.rockit.Utils.Utils
import com.example.rockit.Utils.Utils.getArtistName
import com.example.rockit.Utils.Utils.logger
import com.example.rockit.data.preferences.AppPreferences
import com.example.rockit.data.repository.ConnectivityRepository
import com.example.rockit.data.repository.DataRepository
import com.example.rockit.models.Song
import com.example.rockit.player.service.AppAudioServiceHandler
import com.example.rockit.player.service.AudioServiceState
import com.example.rockit.player.service.AudioState
import com.example.rockit.player.service.PlayerEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.toImmutableList
import org.json.JSONObject
import retrofit2.Response
import java.net.URI
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.Exception

@HiltViewModel
class BaseViewModel
@Inject constructor (
    private val dataRepository: DataRepository,
    private val audioServiceHandler: AppAudioServiceHandler,
    private val connectivityRepository: ConnectivityRepository
) : ViewModel() {

    private val _isFetching = MutableStateFlow(true)
    val isFetching = _isFetching.asStateFlow()

    private val _currentProgress = MutableStateFlow(0f)
    val currentProgress = _currentProgress.asStateFlow()

    private val _currentSongIndex = MutableStateFlow<Int?>(null)
    val currentSongIndex = _currentSongIndex.asStateFlow()

    private lateinit var topSongs : List<Song>

    private val _currentPlayList = MutableStateFlow<List<Song>?>(null)
    val currentPlayList = _currentPlayList.asStateFlow()

    private val _visibleSongs = MutableStateFlow<List<Song>?>(null)
    val visibleSongs = _visibleSongs.asStateFlow()

    private val _uiState = MutableStateFlow<UIState>(UIState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _networkState = MutableStateFlow(connectivityRepository.isNetworkConnected())
    val networkState = _networkState.asStateFlow()

    private var searchJob : Job? = null

    init {
        audioServiceHandler.progressCoroutineScope = viewModelScope
        viewModelScope.launch {
            loadInitialSongs()
            observeAudioState()
        }
        viewModelScope.launch {
            observeNetworkState()
        }
    }

    private suspend fun observeNetworkState() {
        connectivityRepository.observeConnectivity().map { isConnected ->
            if (isConnected && (_visibleSongs.value.isNullOrEmpty() &&  !_isFetching.value)) {
                coroutineScope {
                    loadInitialSongs()
                }
            }
            return@map isConnected
        }.collect { isConnected ->
            _networkState.value = isConnected
        }
    }

    private suspend fun observeAudioState() {
        audioServiceHandler.audioState.collectLatest { mediaState ->
            when(mediaState){
                AudioState.Initial -> _uiState.value = UIState.Initial
                is AudioState.Buffering -> {
                    setProgressValue(mediaState.progress)
                    _uiState.value = UIState.Buffering
                }
                is AudioState.Playing -> {
                    changeUIState( if(mediaState.isPlaying) UIState.Playing else UIState.Paused )
                }
                is AudioState.Progress -> {
                    setProgressValue(mediaState.progress)
                }
                is AudioState.Ready -> {
                    _uiState.value = UIState.Ready
                    if (_currentSongIndex.value == null) _currentSongIndex.value = 0
                }
                is AudioState.CurrentPlaying -> {
                    _currentSongIndex.value = mediaState.mediaItemIndex
                }
                AudioState.PlayingNext -> {
                    _currentSongIndex.value?.let {
                        _currentSongIndex.value = it + 1
                    }
                }
            }
        }
    }

    @WorkerThread
    private suspend fun fetchTopSongs() : List<Song> {
        try {
            return dataRepository.getTopSongs()
        } catch (e : Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    fun searchSongs(searchText : String) {
        searchJob?.cancel()
        searchJob = null
        if (searchText.isBlank() && topSongs.isNotEmpty()) {
            _visibleSongs.value = topSongs
            changeFetchingStatus(false)
            return
        }
        changeFetchingStatus(true)
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val songs = dataRepository.getSearchedSongs(searchText)
                _visibleSongs.value = songs.toMutableList()
            } catch (e : Exception) {
                e.printStackTrace()
            } finally {
                changeFetchingStatus(false)
            }
        }
    }

    private suspend fun setCurrentPlaylist(songs : List<Song>) {
        _currentPlayList.value = songs
        songs.map { audio ->
            MediaItem.Builder()
                .setUri(audio.downloadUrl?.get(AppPreferences.audioQuality)?.url)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setArtist(audio.artists?.primary?.let { getArtistName(it) })
                        .setDisplayTitle(audio.name)
                        .setArtworkUri(Uri.parse(audio.image?.get(2)?.url))
                        .build()
                )
                .build()
        }.also {
            withContext(Dispatchers.Main) {
                audioServiceHandler.addMediaItemList(it)
            }
        }
    }

    private suspend fun loadInitialSongs() {
        changeFetchingStatus(true)
        withContext(Dispatchers.IO) {
            val songs = fetchTopSongs()
            topSongs = songs.toImmutableList()
            _visibleSongs.value = topSongs
        }
        changeFetchingStatus(false)
    }

    fun playOrPauseSong() {
        onUIEvents(UIEvents.PlayPause)
    }

    fun playSong(index: Int, onStart : () -> Unit, context: Context) {
        AudioServiceState.startAudioService(context)
        viewModelScope.launch {
            _visibleSongs.value?.let { setCurrentPlaylist(it) }
        }.invokeOnCompletion {
            onUIEvents(UIEvents.SelectedAudioChange(index))
            _currentSongIndex.value = index
            onStart()
        }
    }

    fun playNext() {
        onUIEvents(UIEvents.SeekToNext)
    }

    fun playPrevious() {
        onUIEvents(UIEvents.SeekToPrevious)
    }

    fun jumpToTimeStamp(float: Float) {
        _currentProgress.value = float
        onUIEvents(UIEvents.SeekTo(float))
    }

    private fun onUIEvents(uiEvents: UIEvents) {
        when(uiEvents) {
            UIEvents.SeekToNext -> {
                _currentSongIndex.value?.let { currentSongIndexValue ->
                    val currentPlayListSize = _currentPlayList.value?.size ?: 0
                    if (currentSongIndexValue != currentPlayListSize - 1) {
                        audioServiceHandler.onPlayerEvents(PlayerEvent.SeekToNext)
                        _currentSongIndex.value = currentSongIndexValue + 1
                    }
                }
            }
            UIEvents.SeekToPrevious -> {
                _currentSongIndex.value?.let { currentSongIndexValue ->
                    if (currentSongIndexValue != 0) {
                        audioServiceHandler.onPlayerEvents(PlayerEvent.SeekToPrevious)
                        _currentSongIndex.value = currentSongIndexValue - 1
                    }
                }
            }
            UIEvents.PlayPause -> {
                audioServiceHandler.onPlayerEvents(PlayerEvent.PlayPause)
            }
            is UIEvents.SelectedAudioChange -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SelectedAudioChange,
                    selectedMediaIndex = uiEvents.index
                )
            }
            is UIEvents.UpdateProgress -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.UpdateProgress(
                        uiEvents.newProgress
                    )
                )
            }
            is UIEvents.SeekTo -> _currentSongIndex.value?.let { index ->
                _currentPlayList.value?.let { songs ->
                    audioServiceHandler.onPlayerEvents(
                        PlayerEvent.SeekTo,
                        seekPosition = (uiEvents.position * 1000).toLong()
                    )
                }
            }
        }
    }

    private fun setProgressValue(currentProgress : Long) {
        _currentProgress.value = currentProgress / 1000f
    }

    private fun changeUIState(state : UIState) {
        _uiState.value = state
    }

    private fun changeFetchingStatus(status : Boolean) {
        _isFetching.value = status
    }

}

sealed class UIState {
    data object Initial : UIState()
    data object Ready : UIState()
    data object Playing : UIState()
    data object Buffering : UIState()
    data object Paused : UIState()
}

sealed class UIEvents {
    data object PlayPause : UIEvents()
    data object SeekToNext : UIEvents()
    data object SeekToPrevious : UIEvents()
    data class SelectedAudioChange(val index : Int) : UIEvents()
    data class SeekTo(val position : Float) : UIEvents()
    data class UpdateProgress(val newProgress : Float) : UIEvents()
}