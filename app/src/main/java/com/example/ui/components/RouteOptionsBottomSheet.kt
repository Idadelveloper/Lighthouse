package com.example.ui.components

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.AccessibleForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.DayNightMode
import com.example.model.NavTab
import com.example.model.RankedRoute
import com.example.model.RouteData
import com.example.model.RouteOption
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
import com.example.ui.theme.Typography
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.WarmCloudBackground
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.LighthouseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteOptionsBottomSheet(
    viewModel: LighthouseViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
        modifier = modifier.testTag("route_options_bottom_sheet")
    ) {
        RouteOptionsSheetContent(
            viewModel = viewModel,
            onClose = onDismiss
        )
    }
}

@Composable
fun RouteOptionsSheetContent(
    viewModel: LighthouseViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayNightMode by viewModel.dayNightMode.collectAsStateWithLifecycle()
    val selectedDayId by viewModel.selectedDayRouteId.collectAsStateWithLifecycle()
    val selectedNightId by viewModel.selectedNightRouteId.collectAsStateWithLifecycle()
    val originName by viewModel.originName.collectAsStateWithLifecycle()
    val destinationName by viewModel.destinationName.collectAsStateWithLifecycle()
    val isUsingCurrentLocation by viewModel.isUsingCurrentLocation.collectAsStateWithLifecycle()
    val activeCivicLayer by viewModel.activeCivicLayer.collectAsStateWithLifecycle()

    val currentRoutes = if (dayNightMode == DayNightMode.DAY) viewModel.dayRoutes else viewModel.nightRoutes
    val selectedId = if (dayNightMode == DayNightMode.DAY) selectedDayId else selectedNightId

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header Row: Title & Close Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PEDESTRIAN SAFE ROUTES",
                    style = MonospaceDataSm.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = PrimaryActionBlue
                )
                Text(
                    text = "Civic Illumination & Shade Model",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepSlateText
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MistBlue)
                    .testTag("close_route_sheet_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close routes sheet",
                    tint = DeepSlateText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 1. Origin & Destination Waypoints Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(PureWhiteCard)
                .border(1.dp, BorderCanvas, RoundedCornerShape(16.dp))
                .padding(14.dp)
                .testTag("sheet_waypoints_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Origin
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MistBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isUsingCurrentLocation) Icons.Default.MyLocation else Icons.Default.DirectionsSubway,
                                contentDescription = null,
                                tint = PrimaryActionBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "START POINT",
                                style = MonospaceDataSm.copy(fontSize = 10.sp),
                                color = SlateMuted
                            )
                            Text(
                                text = originName,
                                style = Typography.titleMedium,
                                color = DeepSlateText
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isUsingCurrentLocation) MistBlue else SoftSage)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isUsingCurrentLocation) "Live GPS" else if (dayNightMode == DayNightMode.DAY) "Plaza W" else "Plaza Exit",
                            style = MonospaceDataSm,
                            color = if (isUsingCurrentLocation) PrimaryActionBlue else VerifiedGreen
                        )
                    }
                }

                // Connector
                Row(
                    modifier = Modifier.padding(start = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(18.dp)
                            .background(BorderCanvas)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Park,
                            contentDescription = null,
                            tint = VerifiedGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (dayNightMode == DayNightMode.DAY) "Greenway Path • +32ft" else "Lit Valencia Corridor",
                            style = MonospaceDataSm,
                            color = VerifiedGreen
                        )
                    }
                }

                // Destination
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SoftSage),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Park,
                                contentDescription = null,
                                tint = VerifiedGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "DESTINATION",
                                style = MonospaceDataSm.copy(fontSize = 10.sp),
                                color = SlateMuted
                            )
                            Text(
                                text = destinationName,
                                style = Typography.titleMedium,
                                color = DeepSlateText
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.swapLocations() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Swap directions",
                            tint = PrimaryActionBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Day / Ambience Weather & Day/Night Toggle Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (dayNightMode == DayNightMode.DAY) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Weather",
                        tint = WarningAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "71°F · Sunny · UV 6",
                        style = MonospaceDataSm,
                        color = DeepSlateText
                    )
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(SlateLight)
                    )
                    Text(
                        text = "Golden Hour 6:42 PM",
                        style = MonospaceDataSm,
                        color = SlateMuted
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NightsStay,
                        contentDescription = "Night Weather",
                        tint = PrimaryActionBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "58°F · Clear · Sunset 7:14 PM",
                        style = MonospaceDataSm,
                        color = DeepSlateText
                    )
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(VerifiedGreen)
                    )
                    Text(
                        text = "HIGH VIS",
                        style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold),
                        color = VerifiedGreen
                    )
                }
            }

            // Day / Night Switcher
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MistBlue.copy(alpha = 0.6f))
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isDay = dayNightMode == DayNightMode.DAY
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDay) DeepSlateText else Color.Transparent)
                        .clickable { viewModel.setDayNightMode(DayNightMode.DAY) }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                        .testTag("sheet_toggle_day_mode")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isDay) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(MutedButter)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = if (isDay) MutedButter else SlateMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Day",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDay) PureWhiteCard else SlateMuted
                        )
                    }
                }

                val isNight = dayNightMode == DayNightMode.NIGHT
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isNight) DeepSlateText else Color.Transparent)
                        .clickable { viewModel.setDayNightMode(DayNightMode.NIGHT) }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                        .testTag("sheet_toggle_night_mode")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isNight) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(SoftSage)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = if (isNight) SoftSage else SlateMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Night",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isNight) PureWhiteCard else SlateMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Interactive Data Layers Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem(
                label = if (dayNightMode == DayNightMode.DAY) "Shade Analysis" else "SFPUC Lighting",
                icon = if (dayNightMode == DayNightMode.DAY) Icons.Default.WbTwilight else Icons.Default.FlashOn,
                isSelected = activeCivicLayer == "ALL" || activeCivicLayer == "LIGHTING",
                badgeDot = true,
                onClick = { viewModel.setActiveCivicLayer("LIGHTING") }
            )
            FilterChipItem(
                label = "Curb Ramps & Slopes",
                icon = Icons.Default.AccessibleForward,
                isSelected = activeCivicLayer == "CURB",
                onClick = { viewModel.setActiveCivicLayer("CURB") }
            )
            FilterChipItem(
                label = "Open Cafes / Havens",
                icon = Icons.Default.LocalCafe,
                isSelected = activeCivicLayer == "HAVENS",
                onClick = { viewModel.setActiveCivicLayer("HAVENS") }
            )
            FilterChipItem(
                label = "SF 311 Sidewalks",
                icon = Icons.Default.Report,
                isSelected = activeCivicLayer == "SF311",
                onClick = { viewModel.setActiveCivicLayer("SF311") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Route Selection Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = if (dayNightMode == DayNightMode.DAY) "LIVE PEDESTRIAN OPTIONS" else "NIGHT CORRIDORS",
                style = Typography.labelLarge.copy(letterSpacing = 1.sp),
                color = DeepSlateText
            )
            Text(
                text = if (dayNightMode == DayNightMode.DAY) "Synced 2m ago" else "Ranked by Illumination",
                style = MonospaceDataSm,
                color = SlateMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Route Option Cards
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            currentRoutes.forEach { route ->
                val isSelected = route.id == selectedId
                RouteCardItem(
                    route = route,
                    isSelected = isSelected,
                    onClick = {
                        if (dayNightMode == DayNightMode.DAY) {
                            viewModel.selectDayRoute(route.id)
                        } else {
                            viewModel.selectNightRoute(route.id)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 6. Action Commit Button
        Button(
            onClick = {
                onClose()
                viewModel.selectTab(NavTab.WALK)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .testTag("sheet_start_walk_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (dayNightMode == DayNightMode.DAY) PrimaryActionBlue else DeepSlateDark
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (dayNightMode == DayNightMode.DAY) Icons.Default.DirectionsWalk else Icons.Default.Explore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = PureWhiteCard
                )
                Text(
                    text = if (dayNightMode == DayNightMode.DAY) {
                        "Start Day Walk (Best Conditions)"
                    } else {
                        "Start Safe Night Walk (Valencia · 16 min)"
                    },
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PureWhiteCard
                )
                if (dayNightMode == DayNightMode.NIGHT) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = SoftSage,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Evidence Sheet Shortcut Button
        OutlinedButton(
            onClick = {
                viewModel.setEvidenceSheetVisible(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("sheet_view_evidence_button"),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCanvas)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = VerifiedGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "View Route Evidence & Civic Provenance",
                    style = Typography.labelMedium,
                    color = DeepSlateText
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Epistemic Grounding Note
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = SlateLight,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (dayNightMode == DayNightMode.DAY) {
                    "SF 311 & Urban Forestry tree data • Real-world conditions vary"
                } else {
                    "SFPUC Smart Lighting + SF 311 active logs. Verified physical fixtures."
                },
                style = MonospaceDataSm.copy(fontSize = 10.sp),
                color = SlateMuted
            )
        }
    }
}

@Composable
private fun RouteCardItem(
    route: RouteOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) PrimaryActionBlue else BorderCanvas
    val cardBackground = when {
        isSelected && route.badgeColorType == "STEP_FREE" -> SoftSage.copy(alpha = 0.5f)
        isSelected -> PureWhiteCard
        else -> PureWhiteCard.copy(alpha = 0.95f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isSelected) 3.dp else 1.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("sheet_route_card_${route.id}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Badge row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val badgeBg = when (route.badgeColorType) {
                    "RECOMMENDED" -> MutedButter
                    "NIGHT_RECOMMENDED" -> SoftSage
                    "FASTEST" -> Color(0xFFFFDAD6)
                    "STEP_FREE" -> SoftSage
                    else -> MistBlue
                }
                val badgeColor = when (route.badgeColorType) {
                    "RECOMMENDED" -> DeepSlateText
                    "NIGHT_RECOMMENDED" -> DeepSlateText
                    "FASTEST" -> EmergencyRose
                    "STEP_FREE" -> VerifiedGreen
                    else -> DeepSlateText
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(badgeBg)
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = route.badge,
                        style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = badgeColor
                    )
                }

                Text(
                    text = route.clarityOrLitScore,
                    style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold),
                    color = if (route.isRecommended) VerifiedGreen else SlateMuted
                )
            }

            // Duration & Distance line with Steps & Calories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${route.durationMinutes}",
                        style = Typography.displayLarge.copy(fontSize = 28.sp),
                        color = DeepSlateText
                    )
                    Text(
                        text = "min",
                        style = Typography.labelMedium,
                        color = SlateMuted,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "·",
                        style = Typography.bodyMedium,
                        color = SlateLight,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "${route.distanceMiles} mi",
                        style = MonospaceDataMd,
                        color = DeepSlateText,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "·",
                        style = Typography.bodyMedium,
                        color = SlateLight,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "~${route.estimatedSteps} steps",
                        style = MonospaceDataMd.copy(fontWeight = FontWeight.Bold),
                        color = VerifiedGreen,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "·",
                        style = Typography.bodyMedium,
                        color = SlateLight,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "${route.estimatedCalories} kcal",
                        style = MonospaceDataMd,
                        color = SlateMuted,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(VerifiedGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = PureWhiteCard,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MistBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Select",
                            tint = SlateLight,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Bullet points
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                route.bulletPoints.forEach { (iconKey, text) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val iconVector: ImageVector = when (iconKey) {
                            "park" -> Icons.Default.Park
                            "accessible" -> Icons.Default.Accessible
                            "storefront" -> Icons.Default.Storefront
                            "wb_sunny" -> Icons.Default.WbSunny
                            "warning" -> Icons.Default.Warning
                            "check_circle", "shelves" -> Icons.Default.CheckCircle
                            "groups" -> Icons.Default.Groups
                            "flash_on" -> Icons.Default.FlashOn
                            "visibility_off" -> Icons.Default.VisibilityOff
                            "directions_bus" -> Icons.Default.DirectionsBus
                            "security" -> Icons.Default.Security
                            else -> Icons.Default.Check
                        }
                        val iconColor = when (iconKey) {
                            "park", "check_circle", "flash_on", "security" -> VerifiedGreen
                            "warning", "visibility_off" -> EmergencyRose
                            "wb_sunny" -> WarningAmber
                            "storefront" -> PrimaryActionBlue
                            else -> SlateMuted
                        }

                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = text,
                            style = Typography.bodySmall,
                            color = DeepSlateText
                        )
                    }
                }
            }

            // Context Note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MistBlue.copy(alpha = 0.45f))
                    .padding(8.dp)
            ) {
                Text(
                    text = route.contextNote,
                    style = MonospaceDataSm.copy(fontSize = 11.sp),
                    color = DeepSlateText
                )
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    badgeDot: Boolean = false,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) VerifiedGreen else PureWhiteCard)
            .border(1.dp, if (isSelected) VerifiedGreen else BorderCanvas, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) PureWhiteCard else SlateMuted,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                style = Typography.labelMedium,
                color = if (isSelected) PureWhiteCard else DeepSlateText
            )
            if (badgeDot) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(MutedButter)
                )
            }
        }
    }
}
