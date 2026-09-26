package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FmdBad
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.SpatialAudio
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.DeepSlateDark
import com.example.ui.theme.DeepSlateText
import com.example.ui.theme.EmergencyRose
import com.example.ui.theme.MistBlue
import com.example.ui.theme.MonospaceDataLg
import com.example.ui.theme.MonospaceDataMd
import com.example.ui.theme.MonospaceDataSm
import com.example.ui.theme.PrimaryActionBlue
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SoftSage
import com.example.ui.theme.Typography
import com.example.ui.theme.VerifiedGreen
import com.example.viewmodel.LighthouseViewModel

@Composable
fun PocketVoiceScreen(
    viewModel: LighthouseViewModel,
    modifier: Modifier = Modifier
) {
    val isThinking by viewModel.isGeminiThinking.collectAsStateWithLifecycle()
    val geminiText by viewModel.geminiLiveVoiceResponse.collectAsStateWithLifecycle()
    val audioState by viewModel.audioSessionState.collectAsStateWithLifecycle()
    val walkSteps by viewModel.walkSteps.collectAsStateWithLifecycle()
    val walkCalories by viewModel.walkCalories.collectAsStateWithLifecycle()

    val pulseTransition = rememberInfiniteTransition(label = "pocket_pulse")
    val pulseRing by pulseTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRing"
    )

    var sosHoldProgress by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSlateDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Low-power status badge
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(SoftLavender.copy(alpha = 0.2f))
                .border(1.dp, SoftLavender.copy(alpha = 0.4f), CircleShape)
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(PrimaryActionBlue)
                )
                Text(
                    text = "POCKET VOICE MODE ACTIVE",
                    style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = SoftLavender
                )
            }
        }

        Text(
            text = "Display low-power lock · Audio & haptic guidance enabled",
            style = Typography.bodySmall,
            color = SlateLight
        )

        // Slate Enclosure Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E2E38))
                .border(1.dp, Color(0xFF2C4350), RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Next Maneuver target
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF142028))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PrimaryActionBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TurnLeft,
                                    contentDescription = null,
                                    tint = PureWhiteCard,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "NEXT MANEUVER",
                                    style = MonospaceDataSm.copy(fontSize = 10.sp),
                                    color = MistBlue
                                )
                                Text(
                                    text = "Turn left onto Dolores St in 350 ft",
                                    style = Typography.titleMedium,
                                    color = PureWhiteCard
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                tint = SoftSage,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "2 gentle pulses = turn ahead · 1 long = check route",
                                style = Typography.bodySmall,
                                color = SoftSage
                            )
                        }
                    }
                }

                // Central Gemini Waveform & Interactive Mic Orb
                Box(
                    modifier = Modifier
                        .size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer pulse ring
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(pulseRing)
                            .clip(CircleShape)
                            .background(SoftLavender.copy(alpha = 0.12f))
                    )

                    // Secondary ring
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(PrimaryActionBlue.copy(alpha = 0.25f))
                    )

                    // Center orb
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(SoftLavender)
                            .clickable { viewModel.askGemini("Check my current location and surroundings.") }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            WaveformVisualizer(
                                modifier = Modifier.height(28.dp),
                                isThinking = isThinking,
                                waveColor = PrimaryActionBlue
                            )
                            Text(
                                text = if (isThinking) "THINKING" else "ACTIVE",
                                style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = PrimaryActionBlue
                            )
                        }
                    }
                }

                // Voice guidance description
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Gemini Continuous Audio",
                        style = Typography.headlineSmall,
                        color = PureWhiteCard
                    )
                    Text(
                        text = "Listening · Safe to store in pocket or keep screen shielded",
                        style = Typography.bodySmall,
                        color = SlateLight
                    )
                }

                // Spoken text summary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF142028))
                        .padding(10.dp)
                ) {
                    Text(
                        text = geminiText,
                        style = Typography.bodySmall,
                        color = PureWhiteCard
                    )
                }

                // Prompt shortcuts
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF263945))
                            .clickable { viewModel.processLiveVoiceInput("Check my surroundings and street lighting.") }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = MistBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "“Hey Lighthouse, check my surroundings”",
                                style = Typography.bodySmall,
                                color = PureWhiteCard
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF263945))
                            .clickable { viewModel.processLiveVoiceInput("Read next turn direction.") }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = MistBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "“Hey Lighthouse, read next turn”",
                                style = Typography.bodySmall,
                                color = PureWhiteCard
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF263945))
                            .clickable { viewModel.processLiveVoiceInput("Where is the nearest storefront?") }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = MistBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "“Hey Lighthouse, nearest storefront”",
                                style = Typography.bodySmall,
                                color = PureWhiteCard
                            )
                        }
                    }
                }

                // Live Steps & Calories Wellness Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF142028))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(16.dp))
                        Text(text = "$walkSteps steps", style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold), color = PureWhiteCard)
                    }
                    Text(text = "•", style = MonospaceDataSm, color = SlateLight)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(15.dp))
                        Text(text = "${String.format("%.0f", walkCalories)} kcal burned", style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold), color = PureWhiteCard)
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SoftSage.copy(alpha = 0.25f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "PACING", style = MonospaceDataSm.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = SoftSage)
                    }
                }

                // Telemetry Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF142028))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = SoftSage, modifier = Modifier.size(14.dp))
                        Text(text = "±9 FT", style = MonospaceDataSm, color = SoftSage)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.Battery5Bar, contentDescription = null, tint = MistBlue, modifier = Modifier.size(14.dp))
                        Text(text = "82%", style = MonospaceDataSm, color = MistBlue)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.CellTower, contentDescription = null, tint = MistBlue, modifier = Modifier.size(14.dp))
                        Text(text = "5G ULTRA", style = MonospaceDataSm, color = MistBlue)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.ShareLocation, contentDescription = null, tint = SoftSage, modifier = Modifier.size(14.dp))
                        Text(text = "2 PEERS", style = MonospaceDataSm, color = SoftSage)
                    }
                }
            }
        }

        // Hardware trigger hint
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1A262E))
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF283944)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                        contentDescription = null,
                        tint = PureWhiteCard,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "Use your device emergency shortcut if configured. This app does not silently dispatch a distress ping.",
                    style = Typography.bodySmall,
                    color = SlateLight
                )
            }
        }

        // Primary Actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Return to Full Map
            Button(
                onClick = { viewModel.togglePocketMode(false) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("return_to_map_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryActionBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = PureWhiteCard,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Return to Full Map",
                        style = Typography.titleMedium,
                        color = PureWhiteCard
                    )
                }
            }

            // Call Maya Speed Dial
            Button(
                onClick = { viewModel.callPrimaryContact() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pocket_call_maya_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SoftSage),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneInTalk,
                            contentDescription = null,
                            tint = DeepSlateText,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Call Trusted Contact • Maya",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepSlateText
                        )
                    }
                    Text(
                        text = "SPEED DIAL",
                        style = MonospaceDataSm,
                        color = VerifiedGreen
                    )
                }
            }

            // Discreet SOS Button
            Button(
                onClick = { viewModel.triggerSosDispatch() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pocket_sos_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFDAD6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FmdBad,
                        contentDescription = null,
                        tint = EmergencyRose,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Discreet SOS (Hold 3s)",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmergencyRose
                    )
                }
            }
        }
    }
}
