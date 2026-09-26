package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.RingVolume
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.ui.theme.SoftSage
import com.example.ui.theme.Typography
import com.example.ui.theme.VerifiedGreen
import com.example.viewmodel.LighthouseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SafetyScreen(
    viewModel: LighthouseViewModel,
    modifier: Modifier = Modifier
) {
    val sirenArmed by viewModel.sirenArmed.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var isHoldingSos by remember { mutableStateOf(false) }
    var sosHoldProgress by remember { mutableFloatStateOf(0f) }
    var sosDispatched by remember { mutableStateOf(false) }

    LaunchedEffect(isHoldingSos) {
        if (isHoldingSos && !sosDispatched) {
            val totalSteps = 30
            for (step in 1..totalSteps) {
                if (!isHoldingSos) break
                delay(100)
                sosHoldProgress = step.toFloat() / totalSteps
            }
            if (isHoldingSos) {
                sosDispatched = true
                viewModel.triggerSosDispatch()
            }
        } else {
            sosHoldProgress = 0f
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(VerifiedGreen)
                )
                Text(
                    text = "ACTIVE GUARDIAN EMERGENCY SYSTEM",
                    style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold),
                    color = DeepSlateText
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SoftSage)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "STABLE",
                    style = MonospaceDataSm.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = VerifiedGreen
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = VerifiedGreen,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Accidental activation guard enabled · Silent standby mode",
                style = Typography.bodySmall,
                color = SlateMuted
            )
        }

        // Primary SOS Radial Hold Zone
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(PureWhiteCard)
                .border(1.dp, BorderCanvas, RoundedCornerShape(20.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Interactive Circular Dial
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .testTag("sos_hold_zone"),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer progress track
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = MistBlue.copy(alpha = 0.5f),
                        strokeWidth = 8.dp
                    )

                    // Active progress arc
                    CircularProgressIndicator(
                        progress = { sosHoldProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = EmergencyRose,
                        strokeWidth = 8.dp,
                        strokeCap = StrokeCap.Round
                    )

                    // Touch Center Button
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(if (sosDispatched) EmergencyRose else Color(0xFFB95D66))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isHoldingSos = true
                                        tryAwaitRelease()
                                        isHoldingSos = false
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sos,
                                contentDescription = null,
                                tint = PureWhiteCard,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = if (sosDispatched) "DISPATCHED" else if (isHoldingSos) "HOLDING" else "HOLD",
                                style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhiteCard
                            )
                            Text(
                                text = if (sosDispatched) "ACTIVE ALERT" else if (isHoldingSos) "${((1f - sosHoldProgress) * 3).toInt() + 1}s REMAINING" else "3 SECONDS",
                                style = MonospaceDataSm.copy(fontSize = 10.sp),
                                color = PureWhiteCard.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                Text(
                    text = "Transmits live GPS, battery level, and silent audio to Maya & Sarah",
                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = DeepSlateText
                )

                // Cancellation Guard
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MistBlue.copy(alpha = 0.45f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = PrimaryActionBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Cancel anytime with biometric fingerprint or 4-digit PIN",
                            style = Typography.bodySmall,
                            color = DeepSlateText
                        )
                    }
                }

                // Hardware Shortcut Guide
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MistBlue.copy(alpha = 0.35f))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(PureWhiteCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = DeepSlateText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Hardware Shortcut Active",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DeepSlateText
                            )
                            Text(
                                text = "Double-press Volume Down from any screen or locked pocket",
                                style = Typography.bodySmall,
                                color = SlateMuted
                            )
                        }
                    }
                }
            }
        }

        // Live Telemetry & Mesh Layer
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
                    Text(
                        text = "LIVE TELEMETRY & MESH LAYER",
                        style = MonospaceDataSm.copy(fontSize = 10.sp),
                        color = SlateMuted
                    )
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
                            text = "99.8% Signal",
                            style = MonospaceDataSm,
                            color = VerifiedGreen
                        )
                    }
                }

                // Row 1: GPS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MistBlue.copy(alpha = 0.35f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.NearMe, contentDescription = null, tint = PrimaryActionBlue, modifier = Modifier.size(18.dp))
                        Column {
                            Text(text = "18th St & Valencia, SF", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = DeepSlateText)
                            Text(text = "Accuracy ±9 ft · Elevation 42m", style = MonospaceDataSm, color = SlateMuted)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PureWhiteCard)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "GPS HOT", style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold), color = DeepSlateText)
                    }
                }

                // Row 2: Offline Mesh
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MistBlue.copy(alpha = 0.35f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CellTower, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(18.dp))
                        Column {
                            Text(text = "Offline Mesh Fallback", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = DeepSlateText)
                            Text(text = "SMS relay & peer hops ready if 5G drops", style = Typography.bodySmall, color = SlateMuted)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SoftSage)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "ARMED", style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold), color = VerifiedGreen)
                    }
                }

                // Row 3: Primary Contact
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SoftSage)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PureWhiteCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.FamilyRestroom, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(text = "Maya Lin (Primary Contact)", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = DeepSlateText)
                            Text(text = "Silent live-link pre-authenticated", style = Typography.bodySmall, color = SlateMuted)
                        }
                    }

                    Button(
                        onClick = { viewModel.callHumanContact("Maya Lin (Mom)") },
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PureWhiteCard),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(14.dp))
                            Text(text = "1-Tap", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = VerifiedGreen)
                        }
                    }
                }
            }
        }

        // Discreet Defense Suite
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DISCREET DEFENSE SUITE",
                    style = Typography.labelLarge.copy(letterSpacing = 1.sp),
                    color = DeepSlateText
                )
                Text(
                    text = "4 MODULES",
                    style = MonospaceDataSm,
                    color = SlateMuted
                )
            }

            // 1. Fake Incoming Call
            DefenseModuleCard(
                icon = Icons.Default.RingVolume,
                iconBg = PrimaryActionBlue,
                title = "Simulate Incoming Call",
                subtitle = "Natural Voice Synthetic Audio",
                tag = "DISCREET",
                description = "Generates a realistic audio ring and conversational check-in from \"Dad\" to effortlessly de-escalate street tension without confrontation.",
                actionLabel = "Trigger Fake Call Now",
                actionTag = "trigger_fake_call_button",
                onClick = { viewModel.triggerFakeCall() }
            )

            // 2. Screen Blackout Mode
            DefenseModuleCard(
                icon = Icons.Default.VisibilityOff,
                iconBg = DeepSlateDark,
                title = "Screen Blackout Mode",
                subtitle = "Hardware Camouflage",
                tag = "STEALTH",
                description = "Completely blacks out display pixels while maintaining continuous high-accuracy GPS telemetry and silent microphone streaming in background. Tap screen 3x to revive.",
                actionLabel = "Enable Blackout Display",
                actionTag = "enable_blackout_button",
                onClick = { viewModel.enableBlackout() }
            )

            // 3. Silent 911 Text Dispatch
            DefenseModuleCard(
                icon = Icons.Default.Sms,
                iconBg = VerifiedGreen,
                title = "Silent 911 Text Dispatch",
                subtitle = "SF Emergency CAD API",
                tag = "CAD-CERTIFIED",
                description = "Silently sends your precise geocode, current street heading, battery telemetry, and audio descriptor directly to municipal emergency dispatch without placing an audible voice call.",
                actionLabel = "Compose Automated 911 Text",
                actionTag = "compose_911_text_button",
                onClick = {
                    viewModel.showToast("Automated dispatch packet prepared for SF CAD API.")
                }
            )

            // 4. Deterrent Siren & Strobe
            DefenseModuleCard(
                icon = Icons.Default.VolumeUp,
                iconBg = EmergencyRose,
                title = "Deterrent Siren & Strobe",
                subtitle = "105 dB High-Frequency Pulse",
                tag = "PUBLIC ALARM",
                description = "Instantly triggers high-candela camera flash strobe and a disorienting, piercing 105 dB sound pattern engineered to divert threats and attract immediate bystander intervention.",
                actionLabel = if (sirenArmed) "Disarm Strobe & Siren" else "Arm Strobe & Siren (2s Delay)",
                actionTag = "arm_siren_button",
                isErrorAction = true,
                onClick = { viewModel.toggleSiren() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DefenseModuleCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    tag: String,
    description: String,
    actionLabel: String,
    actionTag: String,
    isErrorAction: Boolean = false,
    onClick: () -> Unit
) {
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
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = PureWhiteCard,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepSlateText
                        )
                        Text(
                            text = subtitle,
                            style = MonospaceDataSm,
                            color = if (isErrorAction) EmergencyRose else PrimaryActionBlue
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isErrorAction) Color(0xFFFFDAD6) else MistBlue)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tag,
                        style = MonospaceDataSm.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = if (isErrorAction) EmergencyRose else DeepSlateText
                    )
                }
            }

            Text(
                text = description,
                style = Typography.bodyMedium,
                color = DeepSlateText
            )

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag(actionTag),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isErrorAction) EmergencyRose else MistBlue.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isErrorAction) PureWhiteCard else DeepSlateText
                )
            }
        }
    }
}
