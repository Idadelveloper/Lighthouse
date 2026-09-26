package com.example.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AudioTranscriptionConfig
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.LiveAudioConversationConfig
import com.google.firebase.ai.type.LiveGenerationConfig
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.Part
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.Voice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

enum class AudioSessionState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

/**
 * Gemini Live Audio Engine using official Firebase AI Logic Live API.
 *
 * Requirements:
 * - Model: gemini-3.1-flash-live-preview
 * - Backend: GenerativeBackend.googleAI()
 * - Voice: Warm female "Sulafat"
 * - Modalities: Audio response, bidirectional audio conversation with input & output transcription.
 * - Anti-snatch / fail-closed design: No client secrets, no SpeechRecognizer/TextToSpeech duplicates.
 * - Safe session lifecycle: Every stop, release, failed reconnect, and new start closes the prior session
 *   with the official LiveSession close API. Cleanup is idempotent and coroutine-safe.
 * - Fragment accumulation: Streamed input fragments accumulate into a complete utterance, debounce,
 *   dispatch exactly once, and clear the input buffer. Output fragments accumulate for complete spoken
 *   display and reset at turn boundaries.
 * - Documented Kotlin incremental-content API: send(content(role = "user") { ... }, endOfTurn = true)
 *   via Content("user", listOf(TextPart(announcementText))) and turnComplete=true.
 */
@OptIn(PublicPreviewAPI::class)
class GeminiLiveAudioEngine(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        const val LIVE_MODEL_NAME = "gemini-3.1-flash-live-preview"
        const val LIVE_VOICE_NAME = "Sulafat"
        private const val INPUT_DEBOUNCE_MS = 650L

        const val SYSTEM_INSTRUCTION =
            "You are Lighthouse, a calm audio walking companion for pedestrians walking along the 16th St Mission BART " +
            "to Mission Dolores Park corridor in San Francisco. Keep replies to one or two warm, concise sentences suitable for spoken audio. " +
            "Never guarantee safety. Distinguish reported and current conditions from static infrastructure inventory: " +
            "SFPUC light pole inventory is not proof that a lamp currently works, and 311 municipal reports are not exhaustive. " +
            "Always ask before suggesting a route change. Never infer danger from protected class, housing status, wealth, or neighborhood identity. " +
            "In emergencies, defer immediately to 911 and user-controlled trusted contacts."
    }

    private val _sessionState = MutableStateFlow(AudioSessionState.IDLE)
    val sessionState: StateFlow<AudioSessionState> = _sessionState.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _modelOutputTranscript = MutableStateFlow("")
    val modelOutputTranscript: StateFlow<String> = _modelOutputTranscript.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    var onUserQueryRecognized: ((String) -> Unit)? = null
    var onWakeWordDetected: (() -> Unit)? = null
    var onModelTranscriptReceived: ((String) -> Unit)? = null

    private var activeSession: LiveSession? = null
    private val sessionMutex = Mutex()
    private var sessionJob: Job? = null
    private var inputDebounceJob: Job? = null

    // Fragment accumulators for streamed transcription
    private val inputAccumulator = StringBuilder()
    private val outputAccumulator = StringBuilder()

    fun startListening() {
        startLiveSession()
    }

    fun stopListening() {
        stopLiveSession()
    }

    fun stopSpeaking() {
        stopLiveSession()
    }

    fun setProcessing() {
        _sessionState.value = AudioSessionState.PROCESSING
    }

    fun setIdle() {
        _sessionState.value = AudioSessionState.IDLE
    }

    fun startLiveSession() {
        sessionJob?.cancel()
        sessionJob = coroutineScope.launch {
            // Idempotently close and clean up any prior session before starting a new one
            cleanUpSession()

            // Check microphone permission
            val hasMicPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasMicPermission) {
                _sessionState.value = AudioSessionState.ERROR
                _statusMessage.value = "Microphone permission required for Gemini Live audio."
                return@launch
            }

            // Verify FirebaseApp availability
            val app = try {
                FirebaseApp.getInstance()
            } catch (_: Exception) {
                null
            }

            if (app == null) {
                _sessionState.value = AudioSessionState.ERROR
                _statusMessage.value = "Firebase AI Logic Live requires project configuration. Offline fallback active."
                return@launch
            }

            try {
                _sessionState.value = AudioSessionState.PROCESSING
                _statusMessage.value = "Connecting to Gemini Live..."

                val liveConfig = LiveGenerationConfig.Builder().apply {
                    temperature = 0.4f
                    responseModality = ResponseModality.AUDIO
                    speechConfig = SpeechConfig(Voice(LIVE_VOICE_NAME))
                    inputAudioTranscription = AudioTranscriptionConfig()
                    outputAudioTranscription = AudioTranscriptionConfig()
                }.build()

                val systemInstructionContent = Content("system", listOf<Part>(TextPart(SYSTEM_INSTRUCTION)))

                val aiClient = Firebase.ai(app = app, backend = GenerativeBackend.googleAI())
                val liveModel = aiClient.liveModel(
                    modelName = LIVE_MODEL_NAME,
                    generationConfig = liveConfig,
                    systemInstruction = systemInstructionContent
                )

                val session = liveModel.connect()
                sessionMutex.withLock {
                    activeSession = session
                }

                _sessionState.value = AudioSessionState.LISTENING
                _statusMessage.value = "Connected to Gemini Live (Sulafat)"

                // Reset transcription buffers for the new session
                withContext(Dispatchers.Main) {
                    resetInputBuffer()
                    resetOutputBuffer()
                }

                // Bidirectional audio conversation with input and output transcriptions.
                // Do not launch a secondary stream listener flow to avoid socket conflict.
                val audioConfig = LiveAudioConversationConfig.Builder().apply {
                    setTranscriptHandler { inTranscription, outTranscription ->
                        handleTranscriptions(inTranscription?.text, outTranscription?.text)
                    }
                }.build()

                session.startAudioConversation(audioConfig)
            } catch (e: Exception) {
                // Fail closed cleanly without leaking secrets, tokens, or exceptions to logs
                _sessionState.value = AudioSessionState.ERROR
                _statusMessage.value = "Live connection unavailable. Using offline safety fallback."
                cleanUpSession()
            }
        }
    }

    private fun handleTranscriptions(inputText: String?, outputText: String?) {
        coroutineScope.launch(Dispatchers.Main) {
            // 1. Process output transcription from model
            // Streamed fragments are accumulated so the UI displays the complete spoken model response.
            if (!outputText.isNullOrBlank()) {
                // If a user utterance was recently processed and we transition back to speaking,
                // ensure we accumulate for the active model turn.
                _sessionState.value = AudioSessionState.SPEAKING
                outputAccumulator.append(outputText)
                val fullSpokenText = outputAccumulator.toString()
                _modelOutputTranscript.value = fullSpokenText
                onModelTranscriptReceived?.invoke(fullSpokenText)
            }

            // 2. Process user input transcription fragments
            // Accumulate input fragments into a complete utterance, debounce, dispatch exactly once,
            // then clear the input buffer.
            if (!inputText.isNullOrBlank()) {
                _sessionState.value = AudioSessionState.LISTENING
                // If starting a new user turn while previous output is displayed, reset output accumulator
                // so the upcoming model response starts fresh.
                if (outputAccumulator.isNotEmpty()) {
                    resetOutputBuffer()
                }

                inputAccumulator.append(inputText)
                val currentAccumulation = inputAccumulator.toString()
                _liveTranscript.value = currentAccumulation

                // Check wake word if uttered
                if (currentAccumulation.contains("hey lighthouse", ignoreCase = true) ||
                    currentAccumulation.contains("lighthouse", ignoreCase = true)
                ) {
                    onWakeWordDetected?.invoke()
                }

                // Debounce into one final complete user utterance before feeding to deterministic action runner
                inputDebounceJob?.cancel()
                inputDebounceJob = coroutineScope.launch {
                    delay(INPUT_DEBOUNCE_MS)
                    val finalUtterance = inputAccumulator.toString().trim()
                    if (finalUtterance.isNotBlank()) {
                        val cleanQuery = finalUtterance
                            .replace("hey lighthouse", "", ignoreCase = true)
                            .replace("lighthouse", "", ignoreCase = true)
                            .trim()
                        val sanitized = if (cleanQuery.isBlank()) finalUtterance else cleanQuery

                        // Clear the input accumulator after debounced dispatch
                        resetInputBuffer()

                        // Dispatch exactly once to deterministic companion
                        onUserQueryRecognized?.invoke(sanitized)
                    }
                }
            }
        }
    }

    private fun resetInputBuffer() {
        inputAccumulator.setLength(0)
    }

    private fun resetOutputBuffer() {
        outputAccumulator.setLength(0)
    }

    /**
     * Sends an app-generated text update into an active Live session for spoken delivery.
     * Uses documented Kotlin incremental-content API:
     * send(Content("user", listOf(TextPart(announcementText))), endOfTurn = true)
     * This is an explicit app-triggered push, not an autonomous proactive voice loop.
     */
    suspend fun announceRouteSuggestion(announcementText: String) = withContext(Dispatchers.IO) {
        if (announcementText.isBlank()) return@withContext

        sessionMutex.withLock {
            val session = activeSession ?: return@withContext
            try {
                val userContent = Content(
                    role = "user",
                    parts = listOf<Part>(TextPart(announcementText))
                )
                session.send(userContent, true)
            } catch (_: Exception) {
                // Fail-closed
            }
        }
    }

    fun stopLiveSession() {
        coroutineScope.launch {
            cleanUpSession()
            _sessionState.value = AudioSessionState.IDLE
        }
    }

    /**
     * Idempotently and safely closes the WebSocket session and audio conversation.
     * Guaranteed coroutine-safe with mutex to prevent connection leaks.
     */
    private suspend fun cleanUpSession() {
        inputDebounceJob?.cancel()
        inputDebounceJob = null

        withContext(Dispatchers.Main) {
            resetInputBuffer()
        }

        sessionMutex.withLock {
            val session = activeSession
            if (session != null) {
                try {
                    session.stopAudioConversation()
                } catch (_: Exception) {}
                try {
                    session.close()
                } catch (_: Exception) {}
                activeSession = null
            }
        }
    }

    fun release() {
        sessionJob?.cancel()
        sessionJob = null
        coroutineScope.launch {
            cleanUpSession()
            _sessionState.value = AudioSessionState.IDLE
        }
    }
}
