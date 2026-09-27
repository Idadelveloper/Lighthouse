package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AccessibleForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Elevator
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.CommunityReportEntity
import com.example.model.CommunityFilter
import com.example.ui.theme.BorderCanvas
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

@Composable
fun CommunityScreen(
    viewModel: LighthouseViewModel,
    modifier: Modifier = Modifier
) {
    val reports by viewModel.communityReports.collectAsStateWithLifecycle()
    val activeFilter by viewModel.communityFilter.collectAsStateWithLifecycle()

    var showReportDialog by remember { mutableStateOf(false) }
    var reportCategoryToSubmit by remember { mutableStateOf("OUTAGES") }
    var reportTitleInput by remember { mutableStateOf("") }
    var reportDescInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Civic Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(MistBlue.copy(alpha = 0.6f))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PrimaryActionBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RssFeed,
                            contentDescription = null,
                            tint = PureWhiteCard,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Community Observations",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepSlateText
                        )
                        Text(
                            text = "Unverified peer observations & reported conditions",
                            style = Typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MistBlue)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(SlateMuted)
                        )
                        Text(
                            text = "COMMUNITY",
                            style = MonospaceDataSm.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = SlateMuted
                        )
                    }
                }
            }
        }

        // Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CommunityFilter.values().forEach { filter ->
                val isSelected = filter == activeFilter
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) PrimaryActionBlue else PureWhiteCard)
                        .border(1.dp, if (isSelected) PrimaryActionBlue else BorderCanvas, CircleShape)
                        .clickable { viewModel.setCommunityFilter(filter) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("filter_${filter.name.lowercase()}")
                ) {
                    Text(
                        text = filter.label,
                        style = Typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                        color = if (isSelected) PureWhiteCard else DeepSlateText
                    )
                }
            }
        }

        // Feed Items
        val filteredReports = reports.filter {
            when (activeFilter) {
                CommunityFilter.ALL -> true
                CommunityFilter.OUTAGES -> it.category == "OUTAGES"
                CommunityFilter.TRANSIT -> it.category == "TRANSIT"
                CommunityFilter.HAVENS -> it.category == "HAVEN"
                CommunityFilter.COMMUNITY -> it.category == "COMMUNITY" || it.category == "ACCESSIBILITY"
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            filteredReports.forEach { report ->
                ReportCardItem(
                    report = report,
                    onConfirmClick = { viewModel.confirmCommunityReport(report.id) },
                    onNavigateClick = {
                        viewModel.showToast("Rerouting walk via ${report.title}")
                    }
                )
            }
        }

        // Quickly Report a Condition Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUICKLY REPORT A CONDITION",
                    style = Typography.labelLarge.copy(letterSpacing = 1.sp),
                    color = DeepSlateText
                )
                Text(
                    text = "CIVIC & PEER LOG",
                    style = MonospaceDataSm,
                    color = SlateMuted
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickReportBtn(
                    emoji = "💡",
                    title = "Streetlight Out",
                    subtitle = "Auto-tag SF 311",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        reportCategoryToSubmit = "OUTAGES"
                        reportTitleInput = "Streetlight Outage on Corridor"
                        showReportDialog = true
                    }
                )
                QuickReportBtn(
                    emoji = "🚧",
                    title = "Blocked Path",
                    subtitle = "Obstruction / works",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        reportCategoryToSubmit = "ACCESSIBILITY"
                        reportTitleInput = "Blocked sidewalk / works"
                        showReportDialog = true
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickReportBtn(
                    emoji = "🛗",
                    title = "Broken Elevator",
                    subtitle = "Accessibility alert",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        reportCategoryToSubmit = "TRANSIT"
                        reportTitleInput = "Elevator out of service"
                        showReportDialog = true
                    }
                )
                QuickReportBtn(
                    emoji = "⚠️",
                    title = "Safety Concern",
                    subtitle = "Discreet guardian",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        reportCategoryToSubmit = "COMMUNITY"
                        reportTitleInput = "Street safety observation"
                        showReportDialog = true
                    }
                )
            }
        }

        // Epistemic Clarity Notice
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(PureWhiteCard)
                .border(1.dp, BorderCanvas, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = PrimaryActionBlue,
                    modifier = Modifier.size(20.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Epistemic Clarity & Sourcing Policy",
                        style = Typography.titleMedium,
                        color = DeepSlateText
                    )
                    Text(
                        text = "Clear separation between official source links, unverified storefront candidates, and opt-in pedestrian reports. No reports ≠ zero risk. Trust current conditions around you.",
                        style = Typography.bodySmall,
                        color = SlateMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    // Submit Report Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = {
                Text(
                    text = "Report Condition: $reportTitleInput",
                    style = Typography.headlineSmall,
                    color = DeepSlateText
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Your submission will be saved as an in-app community observation pending moderation and sync. It is not transmitted to SF 311.",
                        style = Typography.bodySmall,
                        color = SlateMuted
                    )
                    OutlinedTextField(
                        value = reportDescInput,
                        onValueChange = { reportDescInput = it },
                        placeholder = { Text("Describe specific street details...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitNewReport(
                            title = reportTitleInput,
                            category = reportCategoryToSubmit,
                            desc = reportDescInput.ifBlank { "Reported by user in vicinity of 16th-Valencia corridor." }
                        )
                        showReportDialog = false
                        reportDescInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryActionBlue)
                ) {
                    Text("Submit Report", color = PureWhiteCard)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel", color = SlateMuted)
                }
            }
        )
    }
}

@Composable
private fun ReportCardItem(
    report: CommunityReportEntity,
    onConfirmClick: () -> Unit,
    onNavigateClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(PureWhiteCard)
            .border(1.dp, BorderCanvas, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("report_card_${report.id}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Source & Freshness Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.source,
                    style = MonospaceDataSm,
                    color = SlateMuted,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (report.verified) SoftSage else MistBlue)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = report.timeAgo,
                        style = MonospaceDataSm.copy(fontSize = 10.sp),
                        color = if (report.verified) VerifiedGreen else PrimaryActionBlue
                    )
                }
            }

            // Main Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                val icon = when (report.category) {
                    "OUTAGES" -> Icons.Default.Lightbulb
                    "TRANSIT" -> Icons.Default.Elevator
                    "HAVEN" -> Icons.Default.Storefront
                    "COMMUNITY" -> Icons.Default.SupervisedUserCircle
                    else -> Icons.Default.AccessibleForward
                }
                val iconBg = when (report.category) {
                    "OUTAGES" -> Color(0xFFFFDAD6)
                    "TRANSIT", "HAVEN" -> SoftSage
                    else -> MistBlue
                }
                val iconColor = when (report.category) {
                    "OUTAGES" -> EmergencyRose
                    "TRANSIT", "HAVEN" -> VerifiedGreen
                    else -> PrimaryActionBlue
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.title,
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepSlateText
                    )
                    Text(
                        text = report.description,
                        style = Typography.bodySmall,
                        color = DeepSlateText
                    )
                }
            }

            // Optional image for haven
            if (report.category == "HAVEN") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.haven_birite_storefront),
                        contentDescription = "Storefront candidate photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xCC16252D))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PinDrop,
                                contentDescription = null,
                                tint = PureWhiteCard,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "3 min walk (240m south)",
                                style = MonospaceDataSm,
                                color = PureWhiteCard
                            )
                        }
                    }
                }
            }

            // Action / Status Bottom Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MistBlue.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = report.status,
                        style = MonospaceDataSm.copy(fontSize = 10.sp),
                        color = DeepSlateText
                    )
                }

                if (report.category == "HAVEN") {
                    Row(
                        modifier = Modifier
                            .clickable(onClick = onNavigateClick)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Navigate Here",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryActionBlue
                        )
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = PrimaryActionBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else if (report.category == "COMMUNITY" && report.title.contains("Ambassador")) {
                    Button(
                        onClick = onNavigateClick,
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftSage),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                tint = DeepSlateText,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Request Walk",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DeepSlateText
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MistBlue.copy(alpha = 0.4f))
                            .clickable(onClick = onConfirmClick)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = null,
                            tint = PrimaryActionBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Confirm (${report.confirmations})",
                            style = MonospaceDataSm,
                            color = PrimaryActionBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickReportBtn(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(PureWhiteCard)
            .border(1.dp, BorderCanvas, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MistBlue.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 16.sp)
            }
            Column {
                Text(
                    text = title,
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepSlateText
                )
                Text(
                    text = subtitle,
                    style = MonospaceDataSm.copy(fontSize = 10.sp),
                    color = SlateMuted
                )
            }
        }
    }
}
