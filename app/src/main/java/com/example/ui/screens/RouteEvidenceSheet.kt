package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Elevator
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.NavTab
import com.example.ui.theme.BorderCanvas
import com.example.ui.theme.DeepSlateText
import com.example.ui.theme.MistBlue
import com.example.ui.theme.MonospaceDataLg
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

@Composable
fun RouteEvidenceSheet(
    viewModel: LighthouseViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhiteCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Close
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
                        tint = PrimaryActionBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "EPISTEMIC PROVENANCE v2.4",
                        style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = PrimaryActionBlue
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_evidence_sheet")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = DeepSlateText)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Route Evidence & Condition Provenance",
                    style = Typography.headlineLarge,
                    color = DeepSlateText
                )
                Text(
                    text = "Transparent pedestrian routing telemetry and civic sensor verification",
                    style = Typography.bodyMedium,
                    color = SlateMuted
                )
            }

            // Waypoints Module
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MistBlue.copy(alpha = 0.5f))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "WAYPOINTS", style = MonospaceDataSm, color = SlateMuted)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SoftSage)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Verified Active", style = MonospaceDataSm, color = VerifiedGreen)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "16th St Mission BART", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DeepSlateText)
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = PrimaryActionBlue, modifier = Modifier.size(16.dp))
                        Text(text = "Mission Dolores Park", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DeepSlateText)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(VerifiedGreen))
                        Text(text = "Recommended: Valencia Illuminated Corridor (14 min • 0.6 mi)", style = Typography.bodySmall, color = DeepSlateText)
                    }
                }
            }

            // Epistemic Warnings
            // Banner 1: Conditions Can Change
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MistBlue.copy(alpha = 0.35f))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PureWhiteCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = PrimaryActionBlue, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(text = "Conditions Can Change", style = Typography.titleMedium, color = DeepSlateText)
                        Text(
                            text = "Ground observations may differ from civic sensor feeds. Street conditions adapt faster than scheduled municipal database syncs.",
                            style = Typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                }
            }

            // Banner 2: Absences Are Not Guarantees
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MistBlue.copy(alpha = 0.35f))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PureWhiteCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = PrimaryActionBlue, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(text = "Epistemic Rule: Absences Are Not Guarantees", style = Typography.titleMedium, color = DeepSlateText)
                        Text(
                            text = "No reports does NOT mean zero risk. Installed physical streetlamps do not guarantee active lumen output.",
                            style = Typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                }
            }

            // Anti-bias assurance
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SoftSage.copy(alpha = 0.4f))
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Balance, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Lighthouse NEVER uses property values or neighborhood demographics as safety proxies.",
                        style = Typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = DeepSlateText
                    )
                }
            }

            // Visual corridor snapshot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.map_day_sf),
                    contentDescription = "Corridor view",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC16252D))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(VerifiedGreen))
                        Text(text = "Valencia Greenway Transition · Optimal Visibility", style = MonospaceDataSm, color = PureWhiteCard)
                    }
                }
            }

            // 6 Verification Datasets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "VERIFICATION EVIDENCE", style = Typography.labelLarge.copy(letterSpacing = 1.sp), color = DeepSlateText)
                Text(text = "6 CIVIC DATASETS", style = MonospaceDataSm, color = SlateMuted)
            }

            // Dataset 1: Lighting
            EvidenceCard(
                title = "Lighting & Streetlamp Integrity",
                source = "SFPUC Luminescence Stream · Synced 12m ago",
                icon = Icons.Default.Lightbulb,
                rows = listOf(
                    "Smart Pole Grid" to "98% Density along Valencia corridor",
                    "SF 311 Outage Tickets" to "0 Open Tickets (2 on 17th bypass)",
                    "Estimated Mean Foot-Candles" to "3.8 fc (Nominal)"
                )
            )

            // Dataset 2: Accessibility
            EvidenceCard(
                title = "Sidewalk Accessibility & Terrain",
                source = "ADA & Topography Ingestion · 100% Curb Cuts",
                icon = Icons.Default.Accessible,
                rows = listOf(
                    "Curb Ramps" to "6 of 6 intersections compliant",
                    "Slope Grade" to "Mild 2.8% (vs 6.4% on 17th St)",
                    "Surface Path Status" to "Clear (Guerrero retrofit bypassed)"
                )
            )

            // Dataset 3: Open-at-Arrival
            EvidenceCard(
                title = "Open-at-Arrival & POPOS",
                source = "Civic Activity Eyes-on-Street · 4 Open Haven Anchors",
                icon = Icons.Default.Storefront,
                rows = listOf(
                    "Tartine Bakery" to "Open till 8 PM · Active facade",
                    "Bi-Rite Creamery" to "Open till 11 PM · Staff inside",
                    "Walgreens Pharmacy" to "24/7 Active · Illuminated storefront"
                )
            )

            // Dataset 4: Transit
            EvidenceCard(
                title = "Transit & Elevator Verification",
                source = "BART & SFMTA GTFS-RT Feed",
                icon = Icons.Default.DirectionsSubway,
                rows = listOf(
                    "16th St BART West Elevator" to "OPERATIONAL (Synced 4m ago)",
                    "Muni 24-Divisadero & 14-Mission" to "Stops within 150 ft radius (< 3m walk)"
                )
            )

            // Dataset 5: Vision Zero
            EvidenceCard(
                title = "High Injury Network (Vision Zero)",
                source = "SFCTA Intersection Safety · Protected",
                icon = Icons.Default.Traffic,
                rows = listOf(
                    "Vehicle Conflict Bypass" to "Path skirts 16th & Mission vehicle conflict zone",
                    "Audible Pedestrian Chirps" to "Present on all crossings"
                )
            )

            // Dataset 6: Ground Truth
            EvidenceCard(
                title = "Context & Community Ground Truth",
                source = "Anonymized Block Ledger · Privacy Preserved",
                icon = Icons.Default.Group,
                rows = listOf(
                    "Tonight's Community Walkers" to "3 opt-in reviews (clear sidewalks & open illumination)",
                    "Deduplicated Incident History" to "Zero incidents (72h block rolling)"
                )
            )

            // Action Button
            Button(
                onClick = {
                    onDismiss()
                    viewModel.selectTab(NavTab.WALK)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("apply_route_and_return_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryActionBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = null, tint = PureWhiteCard, modifier = Modifier.size(20.dp))
                    Text(text = "Apply Route & Return to Walk", style = Typography.titleMedium, color = PureWhiteCard)
                }
            }

            Text(
                text = "Audit Hash: #0x9F41_CORRIDOR_VALENCIA_BART",
                style = MonospaceDataSm.copy(fontSize = 10.sp),
                color = SlateLight,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun EvidenceCard(
    title: String,
    source: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    rows: List<Pair<String, String>>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(PureWhiteCard)
            .border(1.dp, BorderCanvas, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MistBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = PrimaryActionBlue, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(text = title, style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DeepSlateText)
                    Text(text = source, style = MonospaceDataSm.copy(fontSize = 10.sp), color = SlateMuted)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                rows.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MistBlue.copy(alpha = 0.3f))
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = label, style = Typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = DeepSlateText)
                        Text(text = value, style = MonospaceDataSm, color = SlateMuted)
                    }
                }
            }
        }
    }
}
