package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.DayNightMode
import com.example.model.NavTab
import com.example.ui.components.GoogleMapView
import com.example.ui.theme.BorderCanvas
import com.example.ui.theme.DeepSlateDark
import com.example.ui.theme.DeepSlateText
import com.example.ui.theme.EmergencyRose
import com.example.ui.theme.MistBlue
import com.example.ui.theme.MonospaceDataMd
import com.example.ui.theme.MonospaceDataSm
import com.example.ui.theme.MutedButter
import com.example.ui.theme.PrimaryActionBlue
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SoftSage
import com.example.ui.theme.StreetlightLitYellow
import com.example.ui.theme.Typography
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.WarmCloudBackground
import com.example.viewmodel.LighthouseViewModel

@Composable
fun ActiveWalkScreen(
    viewModel: LighthouseViewModel,
    modifier: Modifier = Modifier
) {
    val isPocketMode by viewModel.isPocketVoiceMode.collectAsStateWithLifecycle()
    val dayNightMode by viewModel.dayNightMode.collectAsStateWithLifecycle()
    val activeRouteData by viewModel.activeRouteData.collectAsStateWithLifecycle()
    val destinationName by viewModel.destinationName.collectAsStateWithLifecycle()

    if (isPocketMode) {
        PocketVoiceScreen(viewModel = viewModel, modifier = modifier)
        return
    }

    var isEvidenceExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmCloudBackground)
            .testTag("active_walk_screen")
    ) {
        // 1. Map occupies upper ~58-60% of viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.58f)
                .testTag("active_walk_map_container")
        ) {
            GoogleMapView(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize(),
                isActiveWalk = true,
                showControls = false
            )

            // Top Status Bar with Live Indicator & Exit Walk button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 14.dp, end = 14.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(PureWhiteCard)
                        .border(1.dp, BorderCanvas, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
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
                            text = "LIVE NAVIGATION",
                            style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = VerifiedGreen
                        )
                    }
                }

                // End Walk Button
                Button(
                    onClick = { viewModel.selectTab(NavTab.MAP) },
                    colors = ButtonDefaults.buttonColors(containerColor = PureWhiteCard),
                    border = BorderStroke(1.dp, BorderCanvas),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("end_walk_button")
                ) {
                    Text(
                        text = "End Walk",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmergencyRose
                    )
                }
            }
        }

        // 2. Lower ~40-42%: Compact Card, 2 Prominent Actions, Collapsible Evidence
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.42f)
                .background(WarmCloudBackground)
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // A. Compact Card: Next Instruction, ETA/Distance, Day/Night State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(PureWhiteCard)
                    .border(1.dp, BorderCanvas, RoundedCornerShape(16.dp))
                    .padding(12.dp)
                    .testTag("compact_navigation_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Instruction + Day/Night Badge Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PrimaryActionBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Turn Right",
                                    tint = PureWhiteCard,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "In 180 ft",
                                    style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryActionBlue
                                )
                                Text(
                                    text = "Turn right onto 18th St",
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DeepSlateText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Day / Night State Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (dayNightMode == DayNightMode.DAY) MutedButter else DeepSlateDark)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (dayNightMode == DayNightMode.DAY) Icons.Default.WbSunny else Icons.Default.Bedtime,
                                    contentDescription = null,
                                    tint = if (dayNightMode == DayNightMode.DAY) DeepSlateText else StreetlightLitYellow,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (dayNightMode == DayNightMode.DAY) "DAY ROUTE" else "NIGHT ROUTE",
                                    style = MonospaceDataSm.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dayNightMode == DayNightMode.DAY) DeepSlateText else PureWhiteCard
                                    )
                                )
                            }
                        }
                    }

                    // ETA & Distance Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MistBlue.copy(alpha = 0.45f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${activeRouteData?.durationMinutes ?: 13} min left",
                                style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryActionBlue
                            )
                            Text(text = "•", color = SlateLight)
                            Text(
                                text = "${activeRouteData?.distanceMiles ?: 0.6f} mi",
                                style = MonospaceDataSm,
                                color = DeepSlateText
                            )
                            Text(text = "•", color = SlateLight)
                            Text(
                                text = "To ${destinationName.take(16)}",
                                style = MonospaceDataSm,
                                color = SlateMuted
                            )
                        }
                        Text(
                            text = "ETA 9:42 PM",
                            style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold),
                            color = DeepSlateText
                        )
                    }
                }
            }

            // B. Exactly Two Prominent Actions Visible: Gemini Live & Call Trusted Person
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Action 1: Gemini Live
                Button(
                    onClick = { viewModel.startLiveVoiceSession() },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("action_gemini_live"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryActionBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = PureWhiteCard,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Gemini Live",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = PureWhiteCard
                        )
                    }
                }

                // Action 2: Call Trusted Person
                Button(
                    onClick = { viewModel.callPrimaryContact() },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("action_call_trusted_person"),
                    colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = PureWhiteCard,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Call Trusted Person",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = PureWhiteCard
                        )
                    }
                }
            }

            // C. Secondary Evidence & Details into One Collapsible Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(PureWhiteCard)
                    .border(1.dp, BorderCanvas, RoundedCornerShape(14.dp))
                    .padding(10.dp)
                    .testTag("collapsible_evidence_section")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Collapsible Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isEvidenceExpanded = !isEvidenceExpanded }
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = PrimaryActionBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Route Safety & Street Evidence",
                                style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = DeepSlateText
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isEvidenceExpanded) "Hide" else "Show Details",
                                style = MonospaceDataSm.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                color = PrimaryActionBlue
                            )
                            Icon(
                                imageVector = if (isEvidenceExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = PrimaryActionBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Collapsed preview chips
                    if (!isEvidenceExpanded) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            EvidenceSummaryChip(
                                label = "Sidewalk",
                                value = "Paved",
                                color = VerifiedGreen,
                                modifier = Modifier.weight(1f)
                            )
                            EvidenceSummaryChip(
                                label = "Streetlights",
                                value = if (dayNightMode == DayNightMode.NIGHT) "Mapped" else "Mapped",
                                color = PrimaryActionBlue,
                                modifier = Modifier.weight(1f)
                            )
                            EvidenceSummaryChip(
                                label = "Shade",
                                value = "Unknown",
                                color = SlateMuted,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Expanded secondary details
                    AnimatedVisibility(visible = isEvidenceExpanded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            // Corridor Status Notice
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MistBlue.copy(alpha = 0.4f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = if (dayNightMode == DayNightMode.DAY) {
                                        "Shade data unavailable. Compare sun exposure when a verified feed is connected. Open SF 311 to check current sidewalk reports."
                                    } else {
                                        "Streetlight assets mapped along Valencia corridor. Working status unknown. Open SF 311 to check current reports."
                                    },
                                    style = Typography.bodySmall,
                                    color = DeepSlateText
                                )
                            }

                            // Source attribution label
                            Text(
                                text = "Source: SFPUC Streetlights FeatureServer & SF 311 (vw6y-z8j6) • Working status unknown",
                                style = MonospaceDataSm.copy(fontSize = 9.sp),
                                color = SlateMuted
                            )

                            // Quick Safety actions row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.confirmArrivedSafely()
                                        viewModel.selectTab(NavTab.MAP)
                                    },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftSage),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TaskAlt,
                                            contentDescription = null,
                                            tint = VerifiedGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Arrived Safely",
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = VerifiedGreen
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.selectTab(NavTab.SAFETY) },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFDAD6)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Emergency,
                                            contentDescription = null,
                                            tint = EmergencyRose,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Safety Hub / SOS",
                                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = EmergencyRose
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EvidenceSummaryChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MistBlue.copy(alpha = 0.5f))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MonospaceDataSm.copy(fontSize = 9.sp),
                color = SlateMuted
            )
            Text(
                text = value,
                style = MonospaceDataSm.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
private fun FeatureScoreBox(
    title: String,
    pct: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PureWhiteCard)
            .border(1.dp, BorderCanvas, RoundedCornerShape(6.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MonospaceDataSm.copy(fontSize = 9.sp),
                color = SlateMuted
            )
            Text(
                text = "$pct%",
                style = MonospaceDataSm.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = PrimaryActionBlue
            )
        }
    }
}
