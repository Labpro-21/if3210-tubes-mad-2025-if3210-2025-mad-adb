package com.example.adbpurrytify.ui.viewmodels

import android.media.session.PlaybackState
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlayerViewModel : ViewModel() {

    var mediaController: MediaController? = null
        private set

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    fun connect(controller: MediaController) {
        mediaController = controller
        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _duration.value = controller.duration
            }
        })
    }

    fun load(m: MediaItem) {
        mediaController?.setMediaItem(m)
        mediaController?.prepare()
    }

    fun playPause() {
        assert (mediaController != null)
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    override fun onCleared() {
        mediaController?.release()
        mediaController = null
        super.onCleared()
    }
}
