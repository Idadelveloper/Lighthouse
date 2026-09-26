package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.AudioSessionState
import com.example.ui.theme.BorderCanvas
import com.example.ui.theme.DeepSlateDark
import com.example.ui.theme.DeepSlateText
import com.example.ui.theme.EmergencyRose
import com.example.ui.theme.MistBlue
import com.example.ui.theme.MonospaceDataSm
import com.example.ui.theme.MutedButter
import com.example.ui.theme.PrimaryActionBlue
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SoftSage
import com.example.ui.theme.Typography
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.WarmCloudBackground
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.LighthouseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiLiveVoiceModal(
    viewModel: LighthouseViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val audioSessionState by viewModel.audioSessionState.collectAsStateWithLifecycle()
    val transcript by viewModel.liveSpeechTranscript.collectAsStateWithLifecycle()
    val geminiResponse by viewModel.geminiLiveVoiceResponse.collectAsStateWithLifecycle()
    val audioRms by viewModel.audioRms.collectAsStateWithLifecycle()
    val walkSteps by viewModel.walkSteps.collectAsStateWithLifecycle()
    val walkCalories by viewModel.walkCalories.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startLiveVoiceSession()
        } else {
            viewModel.showToast("Microphone permission required for Gemini Live voice.")
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.stopLiveVoiceSession()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = WarmCloudBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(SlateLight)
            )
        },
        modifier = modifier.testTag("gemini_live_voice_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title & Model badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PrimaryActionBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = PureWhiteCard,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Gemini 3.8 Live",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DeepSlateText
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SoftSage)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AUDIO REAL-TIME",
                                    style = MonospaceDataSm.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = VerifiedGreen
                                )
                            }
                        }
                        Text(
                            text = "Say \"Hey Lighthouse\" or speak naturally",
                            style = Typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                }

                IconButton(
                    onClick = {
                        viewModel.stopLiveVoiceSession()
                        onDismiss()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Live Voice",
                        tint = DeepSlateText
                    )
                }
            }

            // Anti-Snatch & Pocket Voice Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeepSlateDark)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SoftSage,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Anti-Snatch Protection Active",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhiteCard
                            )
                            Text(
                                text = "Keep phone in pocket. Audio guides your turns.",
                                style = MonospaceDataSm.copy(fontSize = 10.sp),
                                color = SlateLight
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.togglePocketMode(true)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftSage),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenLockPortrait,
                                contentDescription = null,
                                tint = DeepSlateText,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Pocket Mode",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DeepSlateText
                            )
                        }
                    }
                }
            }

            // Central Audio Visualizer & Orb
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(PureWhiteCard)
                    .border(1.dp, BorderCanvas, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Reactive Waveform Bars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(48.dp)
                    ) {
                        val baseScale = if (audioSessionState == AudioSessionState.LISTENING) {
                            audioRms.coerceIn(0.2f, 1f)
                        } else if (audioSessionState == AudioSessionState.SPEAKING) {
                            0.75f
                        } else {
                            0.15f
                        }

                        val barHeights = listOf(
                            18.dp * (baseScale * 1.1f),
                            30.dp * (baseScale * 1.5f),
                            42.dp * (baseScale * 1.8f),
                            24.dp * (baseScale * 1.3f),
                            48.dp * (baseScale * 2.0f),
                            36.dp * (baseScale * 1.6f),
                            22.dp * (baseScale * 1.2f),
                            44.dp * (baseScale * 1.9f),
                            28.dp * (baseScale * 1.4f),
                            16.dp * (baseScale * 1.0f)
                        )

                        barHeights.forEachIndexed { index, height ->
                            val barColor = when (audioSessionState) {
                                AudioSessionState.LISTENING -> PrimaryActionBlue
                                AudioSessionState.SPEAKING -> VerifiedGreen
                                AudioSessionState.PROCESSING -> WarningAmber
                                else -> SlateLight
                            }
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .height(height.coerceAtLeast(6.dp))
                                    .clip(CircleShape)
                                    .background(barColor)
                            )
                        }
                    }

                    // Session Status Indicator
                    val statusText = when (audioSessionState) {
                        AudioSessionState.LISTENING -> "Listening hands-free • Sustained dialogue active"
                        AudioSessionState.PROCESSING -> "Gemini thinking..."
                        AudioSessionState.SPEAKING -> "Gemini speaking • Listening will resume"
                        AudioSessionState.ERROR -> "Microphone ready. Tap orb to speak."
                        AudioSessionState.IDLE -> "Continuous dialogue ready • Say \"Hey Lighthouse\""
                    }

                    val statusColor = when (audioSessionState) {
                        AudioSessionState.LISTENING -> PrimaryActionBlue
                        AudioSessionState.SPEAKING -> VerifiedGreen
                        AudioSessionState.PROCESSING -> WarningAmber
                        else -> SlateMuted
                    }

                    Text(
                        text = statusText,
                        style = MonospaceDataSm.copy(fontWeight = FontWeight.SemiBold),
                        color = statusColor
                    )
                }
            }

            // Transcript Box: User Query & Gemini Spoken Response
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PureWhiteCard)
                    .border(1.dp, BorderCanvas, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // User Spoken Transcript
                if (transcript.isNotBlank() && transcript != "Listening...") {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MistBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hearing,
                                contentDescription = null,
                                tint = PrimaryActionBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "YOU SAID",
                                style = MonospaceDataSm.copy(fontSize = 10.sp),
                                color = SlateMuted
                            )
                            Text(
                                text = "\"$transcript\"",
                                style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = DeepSlateText
                            )
                        }
                    }
                }

                // Gemini Spoken Response
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SoftSage),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = VerifiedGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "LIGHTHOUSE GEMINI RESPONSE",
                            style = MonospaceDataSm.copy(fontSize = 10.sp),
                            color = VerifiedGreen
                        )
                        Text(
                            text = geminiResponse,
                            style = Typography.bodyMedium,
                            color = DeepSlateText
                        )
                    }
                }
            }

            // Quick Spoken Command Chips
            Text(
                text = "SUGGESTED VOICE COMMANDS",
                style = MonospaceDataSm.copy(fontSize = 10.sp),
                color = SlateMuted,
                modifier = Modifier.align(Alignment.Start)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VocalCommandChip(
                    text = "Find a safe walk to train station",
                    onClick = { viewModel.processLiveVoiceInput("Find me a safe walk back to the train station") }
                )
                VocalCommandChip(
                    text = "Yes, start navigation",
                    onClick = { viewModel.processLiveVoiceInput("Yes, start navigation") }
                )
                VocalCommandChip(
                    text = "How many steps taken?",
                    onClick = { viewModel.processLiveVoiceInput("How many steps have I taken and calories burned?") }
                )
                VocalCommandChip(
                    text = "Go to Dolores Park",
                    onClick = { viewModel.processLiveVoiceInput("Take me to Mission Dolores Park") }
                )
                VocalCommandChip(
                    text = "Where is nearest haven?",
                    onClick = { viewModel.processLiveVoiceInput("Where is the nearest safe haven?") }
                )
                VocalCommandChip(
                    text = "Read next turn",
                    onClick = { viewModel.processLiveVoiceInput("Read next turn direction") }
                )
            }

            // Action Microphone Control Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isListening = audioSessionState == AudioSessionState.LISTENING
                val isSpeaking = audioSessionState == AudioSessionState.SPEAKING

                Button(
                    onClick = {
                        if (isListening) {
                            viewModel.stopLiveVoiceSession()
                        } else if (isSpeaking) {
                            viewModel.stopLiveVoiceSession()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(54.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .testTag("gemini_live_mic_toggle_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening) EmergencyRose else PrimaryActionBlue
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = null,
                            tint = PureWhiteCard,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = if (isListening) "Tap to Stop Listening" else if (isSpeaking) "Tap to Stop Audio" else "Tap to Speak to Gemini",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PureWhiteCard
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VocalCommandChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(PureWhiteCard)
            .border(1.dp, BorderCanvas, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.RecordVoiceOver,
                contentDescription = null,
                tint = PrimaryActionBlue,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = DeepSlateText
            )
        }
    }
}
