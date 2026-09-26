package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FmdGood
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.DayNightMode
import com.example.model.NavTab
import com.example.ui.components.MapPresentationView
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.BorderCanvas
import com.example.ui.theme.DeepSlateDark
import com.example.ui.theme.DeepSlateText
import com.example.ui.theme.EmergencyRose
import com.example.ui.theme.MistBlue
import com.example.ui.theme.MonospaceDataMd
import com.example.ui.theme.MonospaceDataSm
import com.example.ui.theme.PrimaryActionBlue
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SoftSage
import com.example.ui.theme.Typography
import com.example.ui.theme.VerifiedGreen
import com.example.viewmodel.LighthouseViewModel

@Composable
fun ActiveWalkScreen(
    viewModel: LighthouseViewModel,
    modifier: Modifier = Modifier
) {
    val isPocketMode by viewModel.isPocketVoiceMode.collectAsStateWithLifecycle()
    val isAiMuted by viewModel.isAiMuted.collectAsStateWithLifecycle()
    val geminiText by viewModel.geminiSpeechText.collectAsStateWithLifecycle()
    val isGeminiThinking by viewModel.isGeminiThinking.collectAsStateWithLifecycle()
    val dayNightMode by viewModel.dayNightMode.collectAsStateWithLifecycle()
    val walkSteps by viewModel.walkSteps.collectAsStateWithLifecycle()
    val walkCalories by viewModel.walkCalories.collectAsStateWithLifecycle()

    var customQuestionText by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }

    if (isPocketMode) {
        PocketVoiceScreen(viewModel = viewModel, modifier = modifier)
        return
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Maneuver & Telemetry Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(PureWhiteCard)
                .border(1.dp, BorderCanvas, RoundedCornerShape(18.dp))
                .padding(14.dp)
                .testTag("maneuver_panel")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryActionBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Turn Right",
                                tint = PureWhiteCard,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "In 180 ft",
                                    style = MonospaceDataMd.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryActionBlue
                                )
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(VerifiedGreen)
                                )
                                Text(
                                    text = "TURN RIGHT",
                                    style = MonospaceDataSm,
                                    color = SlateMuted
                                )
                            }
                            Text(
                                text = "Turn right onto 18th St",
                                style = Typography.headlineMedium,
                                color = DeepSlateText
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SoftSage)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(VerifiedGreen)
                            )
                            Text(
                                text = "SAFE CORRIDOR",
                                style = MonospaceDataSm.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = VerifiedGreen
                            )
                        }
                    }
                }

                // Landmark Guidance Cue
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MistBlue.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = VerifiedGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Past Tartine Bakery (Well-lit, staffed haven open)",
                            style = Typography.bodySmall,
                            color = DeepSlateText
                        )
                    }
                }

                // Monospace Progress Strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MistBlue.copy(alpha = 0.35f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "8 min left",
                                style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryActionBlue
                            )
                            Text(text = "•", style = MonospaceDataSm, color = SlateLight)
                            Text(text = "0.4 mi", style = MonospaceDataSm, color = DeepSlateText)
                            Text(text = "•", style = MonospaceDataSm, color = SlateLight)
                            Text(text = "To Dolores", style = MonospaceDataSm, color = SlateMuted)
                        }
                        Text(
                            text = "ETA 9:42 PM",
                            style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold),
                            color = DeepSlateText
                        )
                    }
                }

                // Micro verification feed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MicroTag(icon = Icons.Default.Lightbulb, text = "SFPUC Lamps 100% verified", color = PrimaryActionBlue)
                    MicroTag(icon = Icons.Default.CheckCircle, text = "Clear sidewalk", color = VerifiedGreen)
                    MicroTag(icon = Icons.Default.Hearing, text = "Audible chirp at Guerrero", color = SlateMuted)
                }
            }
        }

        // Live Step Counter & Calorie Burned Tracking Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(PureWhiteCard)
                .border(1.dp, BorderCanvas, RoundedCornerShape(16.dp))
                .padding(14.dp)
                .testTag("walk_step_calorie_tracker_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Steps
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SoftSage),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = "Steps",
                            tint = VerifiedGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "$walkSteps",
                            style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepSlateText
                        )
                        Text(
                            text = "STEPS WALKED",
                            style = MonospaceDataSm.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                            color = SlateMuted
                        )
                    }
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(BorderCanvas)
                )

                // Calories
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF0D4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Calories",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = String.format("%.0f", walkCalories),
                            style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepSlateText
                        )
                        Text(
                            text = "CALORIES (KCAL)",
                            style = MonospaceDataSm.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                            color = SlateMuted
                        )
                    }
                }

                // Live Sensor badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SoftSage)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "TRACKING",
                        style = MonospaceDataSm.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = VerifiedGreen
                    )
                }
            }
        }

        // 2. Google Map Static Guidance Corridor
        MapPresentationView(
            mode = dayNightMode,
            isActiveWalk = true
        )

        // 3. Pocket Voice Mode Quick Launcher Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DeepSlateDark)
                .clickable { viewModel.togglePocketMode(true) }
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("enter_pocket_mode_button")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PureWhiteCard.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenLockPortrait,
                            contentDescription = null,
                            tint = SoftLavender,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Switch to Pocket Voice Mode",
                            style = Typography.titleMedium,
                            color = PureWhiteCard
                        )
                        Text(
                            text = "Low-power lock screen · Audio & haptic only",
                            style = Typography.bodySmall,
                            color = SoftLavender
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PrimaryActionBlue)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Dim Screen",
                        style = MonospaceDataSm,
                        color = PureWhiteCard
                    )
                }
            }
        }

        // 4. Communication Channels Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "COMMUNICATION CHANNELS",
                style = Typography.labelLarge.copy(letterSpacing = 1.sp),
                color = DeepSlateText
            )
            Text(
                text = "INDEPENDENT SAFE LINKS",
                style = MonospaceDataSm,
                color = SlateMuted
            )
        }

        // ACTION A: Gemini Voice Companion (AI Voice Mode)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(SoftLavender)
                .border(1.dp, BorderCanvas, RoundedCornerShape(18.dp))
                .padding(14.dp)
                .testTag("gemini_voice_companion_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DeepSlateText),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = SoftLavender,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Gemini Voice Companion",
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DeepSlateText
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(DeepSlateText)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "AI VOICE",
                                        style = MonospaceDataSm.copy(fontSize = 10.sp),
                                        color = SoftLavender
                                    )
                                }
                            }
                            Text(
                                text = if (isAiMuted) "Muted • Tap to enable" else "Listening: Hands-free conversation active",
                                style = Typography.bodySmall,
                                color = SlateMuted
                            )
                        }
                    }

                    // Mute / Unmute Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(PureWhiteCard)
                            .clickable { viewModel.toggleAiMute() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("mute_ai_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isAiMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = null,
                                tint = DeepSlateText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isAiMuted) "Unmute" else "Mute AI",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DeepSlateText
                            )
                        }
                    }
                }

                // Harmonic Waveform Visualizer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PureWhiteCard.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    WaveformVisualizer(isThinking = isGeminiThinking)
                }

                // AI Response / Speech Text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isGeminiThinking) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp),
                            color = PrimaryActionBlue,
                            strokeWidth = 2.dp
                        )
                    }
                    Text(
                        text = geminiText,
                        style = Typography.bodyMedium,
                        color = DeepSlateText
                    )
                }

                // Suggested Prompt Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PureWhiteCard)
                            .clickable {
                                viewModel.askGemini("Where is the nearest open cafe or safe haven?")
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("prompt_chip_cafe")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = PrimaryActionBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Ask: \"Where is nearest cafe?\"",
                                style = Typography.labelSmall,
                                color = DeepSlateText
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PureWhiteCard)
                            .clickable {
                                viewModel.askGemini("Read the next pedestrian cue and describe crossing.")
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("prompt_chip_next_cue")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = PrimaryActionBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Ask: \"Read next cue\"",
                                style = Typography.labelSmall,
                                color = DeepSlateText
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PureWhiteCard)
                            .clickable { showCustomInput = !showCustomInput }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+ Custom Prompt",
                            style = Typography.labelSmall,
                            color = PrimaryActionBlue
                        )
                    }
                }

                // Custom Voice/Text Input field toggle
                AnimatedVisibility(visible = showCustomInput) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = customQuestionText,
                            onValueChange = { customQuestionText = it },
                            placeholder = { Text("Ask Gemini about route, light, transit...", style = Typography.bodySmall) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PureWhiteCard,
                                unfocusedContainerColor = PureWhiteCard,
                                focusedBorderColor = PrimaryActionBlue,
                                unfocusedBorderColor = BorderCanvas
                            )
                        )
                        IconButton(
                            onClick = {
                                if (customQuestionText.isNotBlank()) {
                                    viewModel.askGemini(customQuestionText)
                                    customQuestionText = ""
                                    showCustomInput = false
                                }
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryActionBlue)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = PureWhiteCard,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // ACTION B: Call a Trusted Person (Human Live Call)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(SoftSage)
                .border(1.dp, BorderCanvas, RoundedCornerShape(18.dp))
                .padding(14.dp)
                .testTag("human_call_contact_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(46.dp)) {
                            Image(
                                painter = painterResource(id = R.drawable.contact_maya_lin),
                                contentDescription = "Maya Lin (Mom)",
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(VerifiedGreen)
                                    .align(Alignment.BottomEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneInTalk,
                                    contentDescription = null,
                                    tint = PureWhiteCard,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Maya Lin (Mom)",
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DeepSlateText
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(VerifiedGreen)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "HUMAN CALL",
                                        style = MonospaceDataSm.copy(fontSize = 10.sp),
                                        color = PureWhiteCard
                                    )
                                }
                            }
                            Text(
                                text = "Ready to call with 1-tap · Sharing battery (82%) & GPS",
                                style = Typography.bodySmall,
                                color = SlateMuted
                            )
                        }
                    }
                }

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
                            imageVector = Icons.Default.BatteryStd,
                            contentDescription = null,
                            tint = VerifiedGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(text = "82%", style = MonospaceDataSm, color = DeepSlateText)
                        Text(text = "•", style = MonospaceDataSm, color = SlateLight)
                        Icon(
                            imageVector = Icons.Default.FmdGood,
                            contentDescription = null,
                            tint = VerifiedGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(text = "GPS Live", style = MonospaceDataSm, color = DeepSlateText)
                    }

                    Button(
                        onClick = { viewModel.callHumanContact("Maya Lin (Mom)") },
                        modifier = Modifier
                            .height(44.dp)
                            .shadow(2.dp, RoundedCornerShape(10.dp))
                            .testTag("call_maya_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = PureWhiteCard,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Call Maya",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhiteCard
                            )
                        }
                    }
                }
            }
        }

        // 5. Community Safety Layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(PureWhiteCard)
                .border(1.dp, BorderCanvas, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = VerifiedGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Community Safety Layer",
                            style = Typography.titleMedium,
                            color = DeepSlateText
                        )
                    }
                    Text(
                        text = "REAL-TIME ACTIVE",
                        style = MonospaceDataSm,
                        color = VerifiedGreen
                    )
                }

                // Nearest Haven Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MistBlue.copy(alpha = 0.4f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.haven_birite_thumb),
                            contentDescription = "Bi-Rite Creamery Haven",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column {
                            Text(
                                text = "Nearest Haven: Bi-Rite Creamery",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = DeepSlateText
                            )
                            Text(
                                text = "220 ft ahead • Open till 11:00 PM • Staff inside",
                                style = Typography.bodySmall,
                                color = SlateMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PureWhiteCard)
                            .clickable { viewModel.showToast("Bi-Rite Creamery: Staff present, landline & first aid verified.") }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Details",
                            style = Typography.labelSmall,
                            color = PrimaryActionBlue
                        )
                    }
                }

                // Live Circle sharing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryActionBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("M", style = MonospaceDataSm, color = PureWhiteCard)
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(VerifiedGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("S", style = MonospaceDataSm, color = PureWhiteCard)
                            }
                        }
                        Text(
                            text = "Live Circle: Mom & Sarah viewing your walk",
                            style = Typography.bodySmall,
                            color = DeepSlateText
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(VerifiedGreen)
                    )
                }
            }
        }

        // 6. Primary Action: Arrived Safely & Discreet SOS
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.confirmArrivedSafely()
                    viewModel.selectTab(NavTab.MAP)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(3.dp, RoundedCornerShape(12.dp))
                    .testTag("arrived_safely_button"),
                colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TaskAlt,
                        contentDescription = null,
                        tint = PureWhiteCard,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Arrived Safely • End Walk",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PureWhiteCard
                    )
                }
            }

            Button(
                onClick = { viewModel.selectTab(NavTab.SAFETY) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("active_walk_sos_shortcut"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFDAD6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = null,
                        tint = EmergencyRose,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Hold for SOS (3s) or Open Safety Hub",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmergencyRose
                    )
                }
            }
        }
    }
}

@Composable
private fun MicroTag(icon: ImageVector, text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(MistBlue.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                style = MonospaceDataSm.copy(fontSize = 10.sp),
                color = DeepSlateText
            )
        }
    }
}
