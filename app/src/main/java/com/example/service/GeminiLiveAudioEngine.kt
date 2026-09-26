package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

enum class AudioSessionState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

class GeminiLiveAudioEngine(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false
    private var isSessionActive = false

    private val _sessionState = MutableStateFlow(AudioSessionState.IDLE)
    val sessionState: StateFlow<AudioSessionState> = _sessionState.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    var onUserQueryRecognized: ((String) -> Unit)? = null
    var onWakeWordDetected: (() -> Unit)? = null

    init {
        initializeTts()
    }

    private fun initializeTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
                textToSpeech?.setPitch(1.02f)
                textToSpeech?.setSpeechRate(0.96f) // Slightly slower, calm cadence
                isTtsReady = true

                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _sessionState.value = AudioSessionState.SPEAKING
                    }

                    override fun onDone(utteranceId: String?) {
                        // After speaking completes, if the continuous session is active, automatically listen again
                        if (isSessionActive) {
                            coroutineScope.launch(Dispatchers.Main) {
                                kotlinx.coroutines.delay(350)
                                if (isSessionActive) {
                                    startListeningInternal()
                                } else {
                                    _sessionState.value = AudioSessionState.IDLE
                                }
                            }
                        } else {
                            _sessionState.value = AudioSessionState.IDLE
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        if (isSessionActive) {
                            coroutineScope.launch(Dispatchers.Main) {
                                kotlinx.coroutines.delay(300)
                                if (isSessionActive) startListeningInternal()
                            }
                        } else {
                            _sessionState.value = AudioSessionState.IDLE
                        }
                    }
                })
            }
        }
    }

    fun startListening() {
        isSessionActive = true
        startListeningInternal()
    }

    private fun startListeningInternal() {
        coroutineScope.launch(Dispatchers.Main) {
            stopSpeaking()

            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                _sessionState.value = AudioSessionState.ERROR
                return@launch
            }

            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createRecognitionListener())
                    }
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                _liveTranscript.value = "Listening hands-free..."
                _sessionState.value = AudioSessionState.LISTENING
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _sessionState.value = AudioSessionState.ERROR
            }
        }
    }

    fun stopListening() {
        isSessionActive = false
        coroutineScope.launch(Dispatchers.Main) {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
            if (_sessionState.value == AudioSessionState.LISTENING) {
                _sessionState.value = AudioSessionState.IDLE
            }
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isTtsReady || text.isBlank()) return

        coroutineScope.launch(Dispatchers.Main) {
            _sessionState.value = AudioSessionState.SPEAKING
            val utteranceId = "gemini_live_${System.currentTimeMillis()}"
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
        } catch (_: Exception) {}
        if (_sessionState.value == AudioSessionState.SPEAKING) {
            _sessionState.value = AudioSessionState.IDLE
        }
    }

    fun setProcessing() {
        _sessionState.value = AudioSessionState.PROCESSING
    }

    fun setIdle() {
        _sessionState.value = AudioSessionState.IDLE
    }

    fun isContinuousSessionActive(): Boolean = isSessionActive

    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _sessionState.value = AudioSessionState.LISTENING
            _liveTranscript.value = "Listening hands-free..."
        }

        override fun onBeginningOfSpeech() {
            _sessionState.value = AudioSessionState.LISTENING
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Normalize rmsdB (-2 to 10 typical) to 0.0 .. 1.0 for waveform
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
            _audioRms.value = normalized
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _sessionState.value = AudioSessionState.PROCESSING
        }

        override fun onError(error: Int) {
            // If continuous session is active and error is due to silence / no match, auto-rearm
            if (isSessionActive && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                coroutineScope.launch(Dispatchers.Main) {
                    kotlinx.coroutines.delay(400)
                    if (isSessionActive) {
                        startListeningInternal()
                    }
                }
            } else {
                _sessionState.value = AudioSessionState.IDLE
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val query = matches?.firstOrNull() ?: ""

            if (query.isNotBlank()) {
                _liveTranscript.value = query
                processRecognizedSpeech(query)
            } else {
                if (isSessionActive) {
                    coroutineScope.launch(Dispatchers.Main) {
                        kotlinx.coroutines.delay(300)
                        if (isSessionActive) startListeningInternal()
                    }
                } else {
                    _sessionState.value = AudioSessionState.IDLE
                }
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partialText = partialMatches?.firstOrNull() ?: ""
            if (partialText.isNotBlank()) {
                _liveTranscript.value = partialText

                // Real-time wake word check
                if (partialText.contains("hey lighthouse", ignoreCase = true) ||
                    partialText.contains("lighthouse", ignoreCase = true)
                ) {
                    onWakeWordDetected?.invoke()
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun processRecognizedSpeech(rawQuery: String) {
        val cleanQuery = rawQuery
            .replace("hey lighthouse", "", ignoreCase = true)
            .replace("lighthouse", "", ignoreCase = true)
            .trim()

        val finalQuery = if (cleanQuery.isBlank()) rawQuery else cleanQuery
        onUserQueryRecognized?.invoke(finalQuery)
    }

    fun release() {
        try {
            speechRecognizer?.destroy()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (_: Exception) {}
    }
}
