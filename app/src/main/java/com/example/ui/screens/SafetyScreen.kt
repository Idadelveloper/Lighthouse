package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.RingVolume
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun SafetyScreen(
    viewModel: LighthouseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sirenArmed by viewModel.sirenArmed.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var isHoldingSos by remember { mutableStateOf(false) }
    var sosHoldProgress by remember { mutableFloatStateOf(0f) }
    var sosTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(isHoldingSos) {
        if (isHoldingSos && !sosTriggered) {
            val totalSteps = 30
            for (step in 1..totalSteps) {
                if (!isHoldingSos) break
                delay(100)
                sosHoldProgress = step.toFloat() / totalSteps
            }
            if (isHoldingSos) {
                sosTriggered = true
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
                    text = "EMERGENCY SAFETY SHORTCUTS",
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
                    text = "ACTIVE",
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
                text = "Hold button below to open dialer to 911 · Local device tools",
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
                            .background(if (sosTriggered) EmergencyRose else Color(0xFFB95D66))
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
                                text = if (sosTriggered) "OPENING" else if (isHoldingSos) "HOLDING" else "HOLD 911",
                                style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhiteCard
                            )
                            Text(
                                text = if (sosTriggered) "DIALER READY" else if (isHoldingSos) "${((1f - sosHoldProgress) * 3).toInt() + 1}s REMAINING" else "3 SECONDS",
                                style = MonospaceDataSm.copy(fontSize = 10.sp),
                                color = PureWhiteCard.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                Text(
                    text = "Press and hold for 3 seconds to prompt a phone call to 911",
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
                            text = "Release prior to 3 seconds to cancel",
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
                                text = "Emergency Shortcut Guidance",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DeepSlateText
                            )
                            Text(
                            text = "In immediate danger, call 911 or use your device emergency shortcut if configured",
                                style = Typography.bodySmall,
                                color = SlateMuted
                            )
                        }
                    }
                }
            }
        }

        // Device Connection & Contacts Layer
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
                        text = "DEVICE STATUS & CONTACTS",
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
                            text = "Network not checked",
                            style = MonospaceDataSm,
                            color = SlateMuted
                        )
                    }
                }

                // Row 1: Location approximation
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
                            Text(text = "Corridor waypoint estimate", style = MonospaceDataSm, color = SlateMuted)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PureWhiteCard)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "STANDBY", style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold), color = DeepSlateText)
                    }
                }

                // Row 2: Cellular
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
                            Text(text = "Cellular Network Connection", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = DeepSlateText)
                            Text(text = "Standard cellular call and SMS service", style = Typography.bodySmall, color = SlateMuted)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SoftSage)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "SYSTEM", style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold), color = DeepSlateText)
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
                            Text(text = "Trusted contact", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = DeepSlateText)
                            Text(text = "Opens a configured number in the device dialer", style = Typography.bodySmall, color = SlateMuted)
                        }
                    }

                    Button(
                        onClick = { viewModel.callPrimaryContact() },
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PureWhiteCard),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(14.dp))
                            Text(text = "Call", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = VerifiedGreen)
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
                    text = "LOCAL SAFETY TOOLS",
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
                subtitle = "Local Simulated Audio Ring",
                tag = "LOCAL DEMO",
                description = "Plays a realistic phone ring and audio check-in sound locally on device to provide an excuse to step away from uncomfortable interactions.",
                actionLabel = "Trigger Simulated Call",
                actionTag = "trigger_fake_call_button",
                onClick = { viewModel.triggerFakeCall() }
            )

            // 2. Screen Blackout Mode
            DefenseModuleCard(
                icon = Icons.Default.VisibilityOff,
                iconBg = DeepSlateDark,
                title = "Screen Blackout Mode",
                subtitle = "Screen Camouflage",
                tag = "LOCAL DISPLAY",
                description = "Displays a black overlay while app navigation remains loaded. Triple-tap anywhere on the dark screen to return to the display.",
                actionLabel = "Enable Blackout Display",
                actionTag = "enable_blackout_button",
                onClick = { viewModel.enableBlackout() }
            )

            // 3. 911 Text Composer
            DefenseModuleCard(
                icon = Icons.Default.Sms,
                iconBg = VerifiedGreen,
                title = "Compose Text to 911",
                subtitle = "Standard SMS Application",
                tag = "SMS LAUNCHER",
                description = "Opens your phone's default text messaging app addressed to 911 with your approximate street location pre-filled. You must review and press send.",
                actionLabel = "Open Text to 911",
                actionTag = "compose_911_text_button",
                onClick = {
                    try {
                        val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:911")).apply {
                            putExtra("sms_body", "I need assistance near 18th St and Valencia, San Francisco.")
                        }
                        context.startActivity(smsIntent)
                    } catch (_: Exception) {
                        viewModel.showToast("Please open your phone messaging app and text 911 directly.")
                    }
                }
            )

            // 4. Local Audible Alarm
            DefenseModuleCard(
                icon = Icons.Default.VolumeUp,
                iconBg = EmergencyRose,
                title = "Audible Alarm & Vibration",
                subtitle = "Device Speaker Alarm",
                tag = "LOCAL ALARM",
                description = "Sounds your device speaker alert and vibrates the phone after a 2-second safety buffer to attract nearby bystander attention.",
                actionLabel = if (sirenArmed) "Disarm Local Alarm" else "Arm Local Alarm (2s Delay)",
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
