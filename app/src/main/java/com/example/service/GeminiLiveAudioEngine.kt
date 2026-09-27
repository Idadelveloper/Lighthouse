package com.example.service

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AudioSessionState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

/**
 * Public-build voice boundary.
 *
 * This implementation deliberately performs no microphone, speech-recognition,
 * text-to-speech, Firebase, or network work. It preserves the UI/ViewModel
 * contract while the real voice implementation remains disabled until its
 * permission, lifecycle, privacy, and service behavior have been validated.
 */
@Suppress("UNUSED_PARAMETER")
class GeminiLiveAudioEngine(
    context: Context,
    coroutineScope: CoroutineScope
) {
    companion object {
        const val UNAVAILABLE_MESSAGE = "Voice is not enabled in this public build."
    }

    private val _sessionState = MutableStateFlow(AudioSessionState.IDLE)
    val sessionState: StateFlow<AudioSessionState> = _sessionState.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    var onUserQueryRecognized: ((String) -> Unit)? = null
    var onWakeWordDetected: (() -> Unit)? = null

    fun startListening() {
        _sessionState.value = AudioSessionState.ERROR
        _liveTranscript.value = UNAVAILABLE_MESSAGE
        _audioRms.value = 0f
    }

    fun stopListening() {
        _sessionState.value = AudioSessionState.IDLE
        _liveTranscript.value = ""
        _audioRms.value = 0f
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        onDone?.invoke()
    }

    fun stopSpeaking() {
        if (_sessionState.value == AudioSessionState.SPEAKING) {
            _sessionState.value = AudioSessionState.IDLE
        }
    }

    fun setProcessing() {
        _sessionState.value = AudioSessionState.ERROR
        _liveTranscript.value = UNAVAILABLE_MESSAGE
    }

    fun setIdle() {
        _sessionState.value = AudioSessionState.IDLE
    }

    fun isContinuousSessionActive(): Boolean = false

    fun release() {
        stopListening()
        onUserQueryRecognized = null
        onWakeWordDetected = null
    }
}
