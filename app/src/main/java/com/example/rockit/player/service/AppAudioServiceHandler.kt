package com.example.rockit.player.service

import android.provider.MediaStore.Audio
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.rockit.Utils.Utils.logger
import com.example.rockit.ui.viewmodels.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppAudioServiceHandler @Inject constructor(
    private val exoPlayer: ExoPlayer,
) : Player.Listener
{

    init {
        exoPlayer.addListener(this)
    }

    private val _audioState : MutableStateFlow<AudioState> = MutableStateFlow(AudioState.Initial)
    val audioState = _audioState.asStateFlow()

    private var job : Job? = null

    lateinit var progressCoroutineScope: CoroutineScope

    fun addMediaItem(mediaItem: MediaItem) {
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    @MainThread
    fun addMediaItemList(mediaItem: List<MediaItem>) {
        exoPlayer.setMediaItems(mediaItem)
        exoPlayer.prepare()
    }

    suspend fun onPlayerEvents(
        playerEvent: PlayerEvent,
        selectedMediaIndex : Int = -1,
        seekPosition : Long = 0
    ) {
        when(playerEvent) {
            PlayerEvent.PlayPause -> playOrPause()
            PlayerEvent.SeekTo -> {
                exoPlayer.seekTo(seekPosition)
            }
            PlayerEvent.SeekToNext -> {
                exoPlayer.seekToNextMediaItem()
                _audioState.value = AudioState.Buffering(
                    progress = exoPlayer.currentPosition
                )
                startProgressUpdate()
            }
            PlayerEvent.SeekToPrevious -> {
                exoPlayer.seekToPreviousMediaItem()
                _audioState.value = AudioState.Buffering(
                    progress = exoPlayer.currentPosition
                )
                startProgressUpdate()
            }
            PlayerEvent.SelectedAudioChange -> {
                when(selectedMediaIndex) {
                    exoPlayer.currentMediaItemIndex -> {
                        playOrPause()
                    }
                    else -> {
                        exoPlayer.seekToDefaultPosition(selectedMediaIndex)
                        _audioState.value = AudioState.Buffering(
                            progress = exoPlayer.currentPosition
                        )
                        exoPlayer.playWhenReady = true
                        startProgressUpdate()
                    }
                }
            }
            PlayerEvent.Stop -> {
                exoPlayer.stop()
                stopProgressUpdate()
            }
            is PlayerEvent.UpdateProgress -> {
                exoPlayer.seekTo(
                    (exoPlayer.duration * playerEvent.newProgress).toLong()
                )
            }
        }
    }

    private suspend fun playOrPause() {
        if(exoPlayer.isPlaying) {
            exoPlayer.pause()
            stopProgressUpdate()
        } else {
            exoPlayer.play()
            _audioState.value = AudioState.Playing(
                isPlaying = true,
            )
            startProgressUpdate()
        }
    }

//    private suspend fun startProgressUpdate() = job.run {
//        while (true) {
//            delay(500)
//            _audioState.value = AudioState.Progress(
//                progress = exoPlayer.currentPosition
//            )
//        }
//    }

    private fun startProgressUpdate() {
        job = progressCoroutineScope.launch {
            while (true) {
                delay(800)
                _audioState.value = AudioState.Progress(
                    progress = exoPlayer.currentPosition
                )
            }
        }
    }

    private fun stopProgressUpdate() {
        job?.cancel()
        _audioState.value = AudioState.Playing(
            isPlaying = false
        )
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when(playbackState) {
            ExoPlayer.STATE_BUFFERING -> _audioState.value = AudioState.Buffering(exoPlayer.currentPosition)
            ExoPlayer.STATE_READY -> _audioState.value = AudioState.Ready(exoPlayer.duration)
            Player.STATE_ENDED -> {}
            Player.STATE_IDLE -> {}
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        _audioState.value = AudioState.Playing(
            isPlaying = isPlaying
        )
        _audioState.value = AudioState.CurrentPlaying(
            mediaItemIndex = exoPlayer.currentMediaItemIndex
        )
        if (isPlaying) {
            progressCoroutineScope.launch {
                startProgressUpdate()
            }
        } else {
            stopProgressUpdate()
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        _audioState.value = AudioState.PlayingNext
    }

}

sealed class PlayerEvent {
    data object PlayPause : PlayerEvent()
    data object SelectedAudioChange : PlayerEvent()
    data object SeekToNext : PlayerEvent()
    data object SeekToPrevious : PlayerEvent()
    data object SeekTo : PlayerEvent()
    data object Stop : PlayerEvent()
    data class UpdateProgress(val newProgress : Float) : PlayerEvent()
}

sealed class AudioState {
    data object Initial : AudioState()
    data class Ready(val duration : Long) : AudioState()
    data class Progress(val progress : Long) : AudioState()
    data class Buffering(val progress : Long) : AudioState()
    data class Playing(val isPlaying : Boolean) : AudioState()
    data class CurrentPlaying(val mediaItemIndex : Int) : AudioState()
    data object PlayingNext : AudioState()
}