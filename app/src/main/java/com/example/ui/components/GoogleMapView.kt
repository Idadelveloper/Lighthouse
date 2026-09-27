package com.example.ui.components

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.DayNightMode
import com.example.model.MapDataDefaults
import com.example.model.SafeHavenMarker
import com.example.ui.theme.BorderCanvas
import com.example.ui.theme.DeepSlateDark
import com.example.ui.theme.DeepSlateText
import com.example.ui.theme.EmergencyRose
import com.example.ui.theme.MistBlue
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
import com.example.ui.theme.WarningAmber
import com.example.util.LocationHelper
import com.example.viewmodel.LighthouseViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import androidx.compose.ui.platform.LocalDensity
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
fun GoogleMapView(
    viewModel: LighthouseViewModel,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    showControls: Boolean = true,
    isActiveWalk: Boolean = false,
    onMapClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isDestinationSelected by viewModel.isDestinationSelected.collectAsStateWithLifecycle()
    val dayNightMode by viewModel.dayNightMode.collectAsStateWithLifecycle()
    val selectedDayId by viewModel.selectedDayRouteId.collectAsStateWithLifecycle()
    val selectedNightId by viewModel.selectedNightRouteId.collectAsStateWithLifecycle()
    val originName by viewModel.originName.collectAsStateWithLifecycle()
    val destinationName by viewModel.destinationName.collectAsStateWithLifecycle()
    val originLatLng by viewModel.originLatLng.collectAsStateWithLifecycle()
    val destinationLatLng by viewModel.destinationLatLng.collectAsStateWithLifecycle()
    val userLiveLocation by viewModel.userLiveLocation.collectAsStateWithLifecycle()
    val isUsingCurrentLocation by viewModel.isUsingCurrentLocation.collectAsStateWithLifecycle()
    val selectedHaven by viewModel.selectedSafeHaven.collectAsStateWithLifecycle()
    val activeRouteData by viewModel.activeRouteData.collectAsStateWithLifecycle()
    val selectedSamplePoint by viewModel.selectedSamplePoint.collectAsStateWithLifecycle()

    // Camera positioning focused strictly on SF Mission District corridor (never whole SF)
    val defaultCenter = LatLng(37.7633, -122.4228)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 15.8f)
    }

    // Permission launcher for Live GPS location
    var hasLocationPermission by remember {
        mutableStateOf(LocationHelper.hasLocationPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            LocationHelper.getCurrentLocation(
                context = context,
                onSuccess = { loc ->
                    viewModel.updateUserLiveLocation(loc)
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(loc, 16.5f))
                    }
                    viewModel.showToast("Live GPS active: Accuracy ±8ft")
                },
                onError = { err ->
                    viewModel.showToast(err)
                }
            )
        } else {
            viewModel.showToast("Location permission required for live GPS navigation.")
        }
    }

    // Initial location fetch if permission already granted
    LaunchedEffect(Unit) {
        if (hasLocationPermission && userLiveLocation == null) {
            LocationHelper.getCurrentLocation(
                context = context,
                onSuccess = { loc ->
                    viewModel.updateUserLiveLocation(loc)
                },
                onError = {}
            )
        }
    }

    // Select polyline path based on bundle active route or fallback
    val currentPolyline = remember(activeRouteData, originLatLng, dayNightMode) {
        val path = activeRouteData?.path
        if (!path.isNullOrEmpty()) {
            if (isUsingCurrentLocation) {
                listOf(originLatLng) + path
            } else {
                path
            }
        } else {
            if (dayNightMode == DayNightMode.DAY) MapDataDefaults.dayBestPolyline else MapDataDefaults.nightIlluminatedPolyline
        }
    }

    val polylineColor = if (dayNightMode == DayNightMode.DAY) {
        PrimaryActionBlue
    } else {
        StreetlightLitYellow
    }

    // Fit camera to origin, destination, and route polyline with 80dp padding in preview and active walk
    val density = LocalDensity.current
    val paddingPx = with(density) { 80.dp.roundToPx() }

    LaunchedEffect(currentPolyline, originLatLng, destinationLatLng, isDestinationSelected, isActiveWalk) {
        if ((isDestinationSelected || isActiveWalk) && currentPolyline.isNotEmpty()) {
            try {
                val builder = LatLngBounds.builder()
                builder.include(originLatLng)
                builder.include(destinationLatLng)
                currentPolyline.forEach { builder.include(it) }
                val bounds = builder.build()
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, paddingPx),
                    durationMs = 600
                )
            } catch (_: Exception) {
                val builder = LatLngBounds.builder()
                builder.include(originLatLng)
                builder.include(destinationLatLng)
                currentPolyline.forEach { builder.include(it) }
                val bounds = builder.build()
                cameraPositionState.position = CameraPosition.fromLatLngZoom(bounds.center, 15.6f)
            }
        }
    }

    // Pulse animation for Live GPS dot
    val infiniteTransition = rememberInfiniteTransition(label = "gpsPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("google_map_container")
    ) {
        // Real Google Map implementation
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false,
                mapType = MapType.NORMAL,
                isTrafficEnabled = false
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = true,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            ),
            onMapClick = {
                viewModel.selectSafeHaven(null)
                onMapClick()
            }
        ) {
            // Overlays and landmarks along visible route only when destination is selected or during active walk
            if (isDestinationSelected || isActiveWalk) {
                // 1. Visible Walk Polyline
                Polyline(
                    points = currentPolyline,
                    color = polylineColor,
                    width = 12f
                )

                // At most three compact landmark markers along the visible route:
                // 1. 16th St Mission BART
                Marker(
                    state = MarkerState(position = originLatLng),
                    title = "16th St BART",
                    snippet = "Start • Transit Connection",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )

                // 2. Recognizable Intermediate Landmark: Tartine Bakery (18th & Guerrero)
                val intermediateLandmark = LatLng(37.76142, -122.42412)
                Marker(
                    state = MarkerState(position = intermediateLandmark),
                    title = "Tartine Bakery",
                    snippet = "18th & Guerrero Waypoint",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )

                // 3. Mission Dolores Park Entrance
                Marker(
                    state = MarkerState(position = destinationLatLng),
                    title = "Dolores Park",
                    snippet = "18th & Dolores Entrance",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )
            } else {
                // Plain map: Show compact landmark indicators without route polylines or clutter
                Marker(
                    state = MarkerState(position = MapDataDefaults.BART_16TH),
                    title = "16th St BART",
                    snippet = "16th St Mission Station",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
                Marker(
                    state = MarkerState(position = MapDataDefaults.DOLORES_PARK),
                    title = "Dolores Park",
                    snippet = "Mission Dolores Park",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )
            }

            // User Live Location Marker (if available) - compact beacon, no raw polygons or safety zones
            userLiveLocation?.let { loc ->
                Marker(
                    state = MarkerState(position = loc),
                    title = "You Are Here",
                    snippet = "Live GPS Beacon",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
                )
            }
        }

        // Floating Action Controls (Right side of Map)
        if (showControls) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Day / Night Toggle Pill on Map
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(if (dayNightMode == DayNightMode.DAY) MutedButter else DeepSlateDark)
                        .border(1.dp, BorderCanvas, CircleShape)
                        .clickable {
                            val newMode = if (dayNightMode == DayNightMode.DAY) DayNightMode.NIGHT else DayNightMode.DAY
                            viewModel.setDayNightMode(newMode)
                            viewModel.showToast(if (newMode == DayNightMode.DAY) "Switched to Day Shade Model" else "Switched to SFPUC Illumination Model")
                        }
                        .testTag("map_day_night_toggle_fab"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (dayNightMode == DayNightMode.DAY) Icons.Default.WbSunny else Icons.Default.Bedtime,
                        contentDescription = "Toggle Day / Night map mode",
                        tint = if (dayNightMode == DayNightMode.DAY) DeepSlateText else SoftSage,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Live Location / Recenter GPS Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(if (isUsingCurrentLocation) PrimaryActionBlue else PureWhiteCard)
                        .border(1.dp, BorderCanvas, CircleShape)
                        .clickable {
                            if (hasLocationPermission) {
                                LocationHelper.getCurrentLocation(
                                    context = context,
                                    onSuccess = { loc ->
                                        viewModel.useCurrentLocationAsOrigin(loc)
                                        coroutineScope.launch {
                                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(loc, 16.5f))
                                        }
                                    },
                                    onError = {
                                        viewModel.showToast("GPS signal acquiring...")
                                    }
                                )
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                        .testTag("map_live_location_fab"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Live Location",
                        tint = if (isUsingCurrentLocation) PureWhiteCard else PrimaryActionBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Zoom In
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(PureWhiteCard)
                        .border(1.dp, BorderCanvas, CircleShape)
                        .clickable {
                            coroutineScope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                            }
                        }
                        .testTag("map_zoom_in_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = DeepSlateText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Zoom Out
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(PureWhiteCard)
                        .border(1.dp, BorderCanvas, CircleShape)
                        .clickable {
                            coroutineScope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                            }
                        }
                        .testTag("map_zoom_out_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = DeepSlateText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Recenter on Route
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(PureWhiteCard)
                        .border(1.dp, BorderCanvas, CircleShape)
                        .clickable {
                            coroutineScope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(defaultCenter, 15.2f))
                            }
                        }
                        .testTag("map_recenter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = "Recenter on Corridor",
                        tint = SlateMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Selected Safe Haven Detail Card (Pops up when user taps a haven marker)
        AnimatedVisibility(
            visible = selectedHaven != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp, start = 16.dp, end = 16.dp)
        ) {
            selectedHaven?.let { haven ->
                SafeHavenDetailCard(
                    haven = haven,
                    onNavigate = {
                        viewModel.setCustomDestination(haven.name, haven.position)
                        viewModel.selectSafeHaven(null)
                    },
                    onDismiss = {
                        viewModel.selectSafeHaven(null)
                    }
                )
            }
        }

        // Street View Inspection Sheet (Pops up when sample point is selected)
        if (selectedSamplePoint != null && activeRouteData != null) {
            StreetViewInspectionSheet(
                sample = selectedSamplePoint!!,
                route = activeRouteData!!,
                onSelectSample = { viewModel.selectSamplePoint(it) },
                onDismiss = { viewModel.selectSamplePoint(null) }
            )
        }
    }
}

@Composable
private fun SafeHavenDetailCard(
    haven: SafeHavenMarker,
    onNavigate: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(PureWhiteCard)
            .border(1.dp, BorderCanvas, RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("safe_haven_detail_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SoftSage),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = VerifiedGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = haven.name,
                                style = Typography.titleMedium,
                                color = DeepSlateText
                            )
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Storefront candidate",
                                tint = VerifiedGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = haven.address,
                            style = Typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SlateLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SoftSage)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = haven.hours,
                        style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold),
                        color = VerifiedGreen
                    )
                }
                Text(
                    text = haven.phone,
                    style = MonospaceDataSm,
                    color = PrimaryActionBlue
                )
            }

            Text(
                text = haven.description,
                style = Typography.bodySmall,
                color = DeepSlateText
            )

            // Navigate Button
            Button(
                onClick = onNavigate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("haven_navigate_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryActionBlue),
                shape = RoundedCornerShape(10.dp)
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
                        text = "Route to this location",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PureWhiteCard
                    )
                }
            }
        }
    }
}
