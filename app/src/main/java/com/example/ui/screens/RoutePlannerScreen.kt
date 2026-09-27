package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.DayNightMode
import com.example.model.NavTab
import com.example.ui.components.GeminiLiveVoiceModal
import com.example.ui.components.GoogleMapView
import com.example.ui.components.MapSearchBar
import com.example.ui.components.RouteOptionsBottomSheet
import com.example.ui.theme.BorderCanvas
import com.example.ui.theme.DeepSlateDark
import com.example.ui.theme.DeepSlateText
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
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.LighthouseViewModel

@Composable
fun RoutePlannerScreen(
    viewModel: LighthouseViewModel,
    modifier: Modifier = Modifier
) {
    val isRouteSheetOpen by viewModel.isRouteSheetOpen.collectAsStateWithLifecycle()
    val dayNightMode by viewModel.dayNightMode.collectAsStateWithLifecycle()
    val originName by viewModel.originName.collectAsStateWithLifecycle()
    val destinationName by viewModel.destinationName.collectAsStateWithLifecycle()
    val isUsingCurrentLocation by viewModel.isUsingCurrentLocation.collectAsStateWithLifecycle()
    val selectedDayId by viewModel.selectedDayRouteId.collectAsStateWithLifecycle()
    val selectedNightId by viewModel.selectedNightRouteId.collectAsStateWithLifecycle()
    val isLiveVoiceOverlayVisible by viewModel.isLiveVoiceOverlayVisible.collectAsStateWithLifecycle()
    val hasActiveRoute by viewModel.hasActiveRoute.collectAsStateWithLifecycle()

    val currentRoutes = if (dayNightMode == DayNightMode.DAY) viewModel.dayRoutes else viewModel.nightRoutes
    val selectedRoute = currentRoutes.find {
        it.id == (if (dayNightMode == DayNightMode.DAY) selectedDayId else selectedNightId)
    } ?: currentRoutes.first()

    // Handle back press to dismiss bottom sheet if open
    BackHandler(enabled = isRouteSheetOpen) {
        viewModel.setRouteSheetVisible(false)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_map_screen")
    ) {
        // 1. Google Maps integration background
        GoogleMapView(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize(),
            onMapClick = {
                // Clicking map can dismiss sheet if open
                if (isRouteSheetOpen) {
                    viewModel.setRouteSheetVisible(false)
                }
            }
        )

        // 2. Floating Top Search Bar with auto-suggestions & quick chips
        MapSearchBar(
            viewModel = viewModel,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )

        // 3. Floating Bottom Route Peek Card (Only when a route is searched and modal sheet is closed)
        AnimatedVisibility(
            visible = hasActiveRoute && !isRouteSheetOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp, start = 14.dp, end = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(PureWhiteCard)
                    .border(1.dp, BorderCanvas, RoundedCornerShape(20.dp))
                    .clickable { viewModel.setRouteSheetVisible(true) }
                    .padding(14.dp)
                    .testTag("map_route_peek_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Route Header Row
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
                            val destIcon = when {
                                destinationName.contains("Haven", ignoreCase = true) || destinationName.contains("Bi-Rite", ignoreCase = true) || destinationName.contains("Tartine", ignoreCase = true) -> Icons.Default.Storefront
                                destinationName.contains("BART", ignoreCase = true) || destinationName.contains("Muni", ignoreCase = true) || destinationName.contains("Transit", ignoreCase = true) -> Icons.Default.DirectionsSubway
                                destinationName.contains("Hospital", ignoreCase = true) || destinationName.contains("Pharmacy", ignoreCase = true) || destinationName.contains("Walgreens", ignoreCase = true) -> Icons.Default.Storefront
                                destinationName.contains("Park", ignoreCase = true) || destinationName.contains("Square", ignoreCase = true) -> Icons.Default.Park
                                else -> Icons.Default.Explore
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SoftSage),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = destIcon,
                                    contentDescription = null,
                                    tint = VerifiedGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = destinationName,
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DeepSlateText
                                )
                                Text(
                                    text = "From: $originName",
                                    style = Typography.bodySmall,
                                    color = SlateMuted
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Badge
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (dayNightMode == DayNightMode.DAY) MutedButter else SoftSage)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = selectedRoute.clarityOrLitScore,
                                    style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    color = DeepSlateText
                                )
                            }

                            // Dismiss/Clear Route Button
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MistBlue)
                                    .clickable { viewModel.clearActiveRoute() }
                                    .testTag("peek_clear_route_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✕", style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold), color = DeepSlateText)
                            }
                        }
                    }

                    // Stats strip with steps & calories
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${selectedRoute.durationMinutes} min",
                                style = MonospaceDataMd.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryActionBlue
                            )
                            Text(text = "•", color = SlateLight)
                            Text(
                                text = "${selectedRoute.distanceMiles} mi",
                                style = MonospaceDataMd,
                                color = DeepSlateText
                            )
                            Text(text = "•", color = SlateLight)
                            Text(
                                text = "~${selectedRoute.estimatedSteps} steps",
                                style = MonospaceDataMd.copy(fontWeight = FontWeight.SemiBold),
                                color = VerifiedGreen
                            )
                            Text(text = "•", color = SlateLight)
                            Text(
                                text = "${selectedRoute.estimatedCalories} kcal",
                                style = MonospaceDataMd,
                                color = DeepSlateText
                            )
                        }

                        Text(
                            text = "${selectedRoute.openHavensCount} Havens",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = VerifiedGreen
                        )
                    }

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Open Full Route Options Button
                        Button(
                            onClick = { viewModel.setRouteSheetVisible(true) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("view_route_options_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryActionBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Directions,
                                    contentDescription = null,
                                    tint = PureWhiteCard,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "View Route Options",
                                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PureWhiteCard
                                )
                            }
                        }

                        // Direct Start Walk Button
                        Button(
                            onClick = { viewModel.selectTab(NavTab.WALK) },
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("peek_quick_start_walk_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dayNightMode == DayNightMode.DAY) VerifiedGreen else DeepSlateDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsWalk,
                                    contentDescription = null,
                                    tint = PureWhiteCard,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Start Walk",
                                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PureWhiteCard
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Modal Bottom Sheet for Route Options popping up over the background Google Map
        if (isRouteSheetOpen) {
            RouteOptionsBottomSheet(
                viewModel = viewModel,
                onDismiss = { viewModel.setRouteSheetVisible(false) }
            )
        }

        // 5. Disabled voice preview entry point
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    bottom = if (hasActiveRoute && !isRouteSheetOpen) 152.dp else 20.dp,
                    end = 16.dp
                )
                .shadow(5.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(PureWhiteCard)
                .border(1.5.dp, PrimaryActionBlue.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .clickable { viewModel.startLiveVoiceSession() }
                .padding(horizontal = 14.dp, vertical = 9.dp)
                .testTag("floating_hey_lighthouse_orb")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(PrimaryActionBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice companion preview unavailable",
                        tint = PureWhiteCard,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Voice preview",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepSlateText
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SlateMuted)
                        )
                    }
                    Text(
                        text = "Not enabled in this build",
                        style = MonospaceDataSm.copy(fontSize = 9.sp),
                        color = SlateMuted
                    )
                }
            }
        }

        // 6. Disabled voice preview modal
        if (isLiveVoiceOverlayVisible) {
            GeminiLiveVoiceModal(
                viewModel = viewModel,
                onDismiss = { viewModel.setLiveVoiceOverlayVisible(false) }
            )
        }
    }
}
