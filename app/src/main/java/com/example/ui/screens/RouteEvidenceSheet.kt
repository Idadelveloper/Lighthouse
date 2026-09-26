package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.DayNightMode
import com.example.model.EvidenceStatus
import com.example.model.KnownCorridorEvidence
import com.example.model.NavTab
import com.example.model.RouteEvidenceItem
import com.example.ui.theme.BorderCanvas
import com.example.ui.theme.DeepSlateText
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
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.LighthouseViewModel

@Composable
fun RouteEvidenceSheet(
    viewModel: LighthouseViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val dayNightMode by viewModel.dayNightMode.collectAsStateWithLifecycle()
    val evidenceList = KnownCorridorEvidence.getEvidence(dayNightMode)

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
                        text = "EPISTEMIC PROVENANCE",
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
                    text = "Route Evidence & Source Provenance",
                    style = Typography.headlineLarge,
                    color = DeepSlateText
                )
                Text(
                    text = "Official civic layers, dataset limitations, and unverified observation boundaries",
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
                            Text(text = "Mapped Corridor", style = MonospaceDataSm, color = VerifiedGreen)
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
                        Text(
                            text = if (dayNightMode == DayNightMode.DAY) {
                                "Route option: Valencia Corridor (14 min • 0.6 mi)"
                            } else {
                                "Route option: Valencia Corridor (16 min • 0.7 mi)"
                            },
                            style = Typography.bodySmall,
                            color = DeepSlateText
                        )
                    }
                }
            }

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
                            text = "No report does not mean no problem. SFPUC light pole inventory is not proof that a lamp currently works.",
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
                        text = "Lighthouse never infers danger from protected class, housing status, wealth, or neighborhood identity.",
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
                        Text(text = "Valencia Street Pedestrian Corridor • 16th St to 19th St", style = MonospaceDataSm, color = PureWhiteCard)
                    }
                }
            }

            // Normalized Evidence Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "CIVIC SOURCE EVIDENCE", style = Typography.labelLarge.copy(letterSpacing = 1.sp), color = DeepSlateText)
                Text(text = "${evidenceList.size} CONTRACT LAYERS", style = MonospaceDataSm, color = SlateMuted)
            }

            // Evidence Cards with full provenance, URLs, and limitations
            evidenceList.forEach { item ->
                EvidenceContractCard(item = item)
            }

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
                text = "Epistemic Provenance: 16th St Mission BART to Mission Dolores Park",
                style = MonospaceDataSm.copy(fontSize = 10.sp),
                color = SlateLight,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun EvidenceContractCard(item: RouteEvidenceItem) {
    val context = LocalContext.current
    val icon = when (item.category) {
        "LIGHTING" -> Icons.Default.Lightbulb
        "311_REPORTS" -> Icons.Default.Report
        "SHADE" -> Icons.Default.WbSunny
        "COMMUNITY" -> Icons.Default.Group
        else -> Icons.Default.Info
    }

    val (statusLabel, statusBg, statusColor) = when (item.status) {
        EvidenceStatus.AVAILABLE -> Triple("AVAILABLE", SoftSage, VerifiedGreen)
        EvidenceStatus.UNKNOWN -> Triple("UNKNOWN", MistBlue, SlateMuted)
        EvidenceStatus.COMMUNITY_UNVERIFIED -> Triple("UNVERIFIED", Color(0xFFFFF3CD), WarningAmber)
    }

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
            // Header Row: Icon + Title + Status Pill
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
                        Text(text = item.title, style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DeepSlateText)
                        Text(text = item.sourceName, style = MonospaceDataSm.copy(fontSize = 10.sp), color = SlateMuted)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = statusLabel, style = MonospaceDataSm.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = statusColor)
                }
            }

            // Summary
            Text(
                text = item.summary,
                style = Typography.bodySmall,
                color = DeepSlateText
            )

            // Provenance Limitation box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MistBlue.copy(alpha = 0.35f))
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "LIMITATION & PROVENANCE",
                        style = MonospaceDataSm.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = SlateMuted
                    )
                    Text(
                        text = item.limitation,
                        style = Typography.bodySmall.copy(fontSize = 11.sp),
                        color = DeepSlateText
                    )
                }
            }

            // Source URL Link & Dataset ID Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.sourceUrl))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dataset: ${item.sourceDatasetId}",
                    style = MonospaceDataSm.copy(fontSize = 10.sp),
                    color = SlateLight
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "View source",
                        style = Typography.labelSmall.copy(color = PrimaryActionBlue, fontSize = 11.sp)
                    )
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open source url",
                        tint = PrimaryActionBlue,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
