package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepSlateDark
import com.example.ui.theme.EmergencyRose
import com.example.ui.theme.MonospaceDataMd
import com.example.ui.theme.MonospaceDataSm
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SoftSage
import com.example.ui.theme.Typography
import com.example.ui.theme.VerifiedGreen
import kotlinx.coroutines.delay

@Composable
fun FakeCallOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    var isCallAccepted by remember { mutableStateOf(false) }
    var callSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(isCallAccepted) {
        if (isCallAccepted) {
            while (true) {
                delay(1000)
                callSeconds++
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSlateDark)
            .padding(24.dp)
            .testTag("fake_call_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2B3F4B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Caller Avatar",
                        tint = PureWhiteCard,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Text(
                    text = "Dad",
                    style = Typography.headlineLarge.copy(fontSize = 32.sp),
                    color = PureWhiteCard
                )

                Text(
                    text = if (isCallAccepted) "Connected: 00:${if (callSeconds < 10) "0$callSeconds" else callSeconds}" else "Incoming Cellular Call...",
                    style = MonospaceDataMd,
                    color = SoftSage
                )
            }

            // In-call conversation cue if accepted
            if (isCallAccepted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF22343F))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Synthetic Voice Loop Active:",
                            style = MonospaceDataSm,
                            color = SlateLight
                        )
                        Text(
                            text = "“Hey sweetie, I'm waiting for you right outside at the corner. Where are you right now?”",
                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = PureWhiteCard
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(30.dp))
            }

            // Bottom Call Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decline / End Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(EmergencyRose)
                            .clickable {
                                isCallAccepted = false
                                callSeconds = 0
                                onDismiss()
                            }
                            .testTag("decline_fake_call_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Decline",
                            tint = PureWhiteCard,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = if (isCallAccepted) "End" else "Decline",
                        style = Typography.labelMedium,
                        color = PureWhiteCard
                    )
                }

                // Accept Button (if not yet accepted)
                if (!isCallAccepted) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(VerifiedGreen)
                                .clickable { isCallAccepted = true }
                                .testTag("accept_fake_call_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Accept",
                                tint = PureWhiteCard,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "Accept",
                            style = Typography.labelMedium,
                            color = PureWhiteCard
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BlackoutOverlay(
    isActive: Boolean,
    onTripleTap: () -> Unit
) {
    if (!isActive) return

    var tapCount by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        tapCount++
                        if (tapCount >= 3) {
                            tapCount = 0
                            onTripleTap()
                        }
                    }
                )
            }
            .testTag("blackout_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Ultra faint instruction shown briefly or faint enough that display looks turned off
        Text(
            text = if (tapCount > 0) "Tap ${3 - tapCount} more times to wake" else "",
            style = MonospaceDataSm,
            color = Color(0x33FFFFFF)
        )
    }
}
