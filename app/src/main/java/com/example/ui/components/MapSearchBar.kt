package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.MapDataDefaults
import com.example.model.SearchPlace
import com.example.ui.theme.BorderCanvas
import com.example.ui.theme.DeepSlateDark
import com.example.ui.theme.DeepSlateText
import com.example.ui.theme.MistBlue
import com.example.ui.theme.MonospaceDataSm
import com.example.ui.theme.MutedButter
import com.example.ui.theme.PrimaryActionBlue
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SoftSage
import com.example.ui.theme.Typography
import com.example.ui.theme.VerifiedGreen
import com.example.util.LocationHelper
import com.example.viewmodel.LighthouseViewModel

@Composable
fun MapSearchBar(
    viewModel: LighthouseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isUsingCurrentLocation by viewModel.isUsingCurrentLocation.collectAsStateWithLifecycle()
    var isSearchFocused by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            LocationHelper.getCurrentLocation(
                context = context,
                onSuccess = { loc ->
                    viewModel.useCurrentLocationAsOrigin(loc)
                },
                onError = { err -> viewModel.showToast(err) }
            )
        }
    }

    val filteredSuggestions = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            MapDataDefaults.searchSuggestions
        } else {
            MapDataDefaults.searchSuggestions.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.subtitle.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("map_search_bar_container")
    ) {
        // Floating Top Search Input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(PureWhiteCard)
                .border(1.dp, BorderCanvas, RoundedCornerShape(16.dp))
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        text = "Where to? (e.g. Dolores Park, Bi-Rite...)",
                        style = Typography.bodyMedium,
                        color = SlateMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = PrimaryActionBlue,
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = SlateLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Gemini 3.8 Live Voice Button
                        IconButton(
                            onClick = {
                                viewModel.startLiveVoiceSession()
                            },
                            modifier = Modifier.testTag("search_bar_voice_mic_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Hey Lighthouse - Talk to Gemini Live",
                                tint = PrimaryActionBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // My Location live target icon
                        IconButton(
                            onClick = {
                                if (LocationHelper.hasLocationPermission(context)) {
                                    LocationHelper.getCurrentLocation(
                                        context = context,
                                        onSuccess = { loc ->
                                            viewModel.useCurrentLocationAsOrigin(loc)
                                        },
                                        onError = { err -> viewModel.showToast(err) }
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
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Use My Location",
                                tint = if (isUsingCurrentLocation) VerifiedGreen else PrimaryActionBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Route Sheet Toggle Button
                        IconButton(
                            onClick = {
                                viewModel.setRouteSheetVisible(true)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Open Route Options",
                                tint = DeepSlateText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isSearchFocused = it.isFocused }
                    .testTag("map_search_text_field"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = PureWhiteCard,
                    unfocusedContainerColor = PureWhiteCard
                )
            )
        }

        // Suggestions Dropdown
        AnimatedVisibility(
            visible = isSearchFocused && (searchQuery.isNotEmpty() || filteredSuggestions.isNotEmpty()),
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(PureWhiteCard)
                    .border(1.dp, BorderCanvas, RoundedCornerShape(16.dp))
                    .padding(vertical = 6.dp)
            ) {
                Column {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SUGGESTED DESTINATIONS",
                            style = MonospaceDataSm.copy(fontSize = 10.sp),
                            color = SlateMuted
                        )
                        Text(
                            text = "Source status shown",
                            style = MonospaceDataSm.copy(fontSize = 10.sp),
                            color = VerifiedGreen
                        )
                    }

                    filteredSuggestions.forEach { place ->
                        SearchPlaceItem(
                            place = place,
                            onClick = {
                                viewModel.selectSearchPlace(place)
                                focusManager.clearFocus()
                            }
                        )
                    }
                }
            }
        }

        // Quick Category Destination Chips below Search
        if (!isSearchFocused) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickSearchChip(
                    label = "Hey Lighthouse",
                    icon = Icons.Default.GraphicEq,
                    isSelected = true,
                    onClick = {
                        viewModel.startLiveVoiceSession()
                    }
                )

                QuickSearchChip(
                    label = "Dolores Park",
                    icon = Icons.Default.Park,
                    onClick = {
                        viewModel.selectSearchPlace(MapDataDefaults.searchSuggestions[0])
                    }
                )

                QuickSearchChip(
                    label = "Bi-Rite Creamery",
                    icon = Icons.Default.Storefront,
                    badgeColor = SoftSage,
                    onClick = {
                        viewModel.selectSearchPlace(MapDataDefaults.searchSuggestions[1])
                    }
                )

                QuickSearchChip(
                    label = "Tartine Bakery",
                    icon = Icons.Default.Storefront,
                    onClick = {
                        viewModel.selectSearchPlace(MapDataDefaults.searchSuggestions[2])
                    }
                )

                QuickSearchChip(
                    label = "16th St BART",
                    icon = Icons.Default.DirectionsSubway,
                    onClick = {
                        viewModel.selectSearchPlace(MapDataDefaults.searchSuggestions[3])
                    }
                )

                QuickSearchChip(
                    label = if (isUsingCurrentLocation) "GPS Active" else "Use Live GPS",
                    icon = Icons.Default.MyLocation,
                    isSelected = isUsingCurrentLocation,
                    onClick = {
                        if (LocationHelper.hasLocationPermission(context)) {
                            LocationHelper.getCurrentLocation(
                                context = context,
                                onSuccess = { loc ->
                                    viewModel.useCurrentLocationAsOrigin(loc)
                                },
                                onError = { err -> viewModel.showToast(err) }
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
                )
            }
        }
    }
}

@Composable
private fun SearchPlaceItem(
    place: SearchPlace,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val icon = when (place.category) {
            "HAVEN" -> Icons.Default.Storefront
            "TRANSIT" -> Icons.Default.DirectionsSubway
            else -> Icons.Default.Park
        }
        val iconTint = when (place.category) {
            "HAVEN" -> VerifiedGreen
            "TRANSIT" -> PrimaryActionBlue
            else -> VerifiedGreen
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (place.isSafeHaven) SoftSage else MistBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = place.title,
                    style = Typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = DeepSlateText
                )
                if (place.isSafeHaven) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SoftSage)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "SUPPORT PLACE",
                            style = MonospaceDataSm.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = VerifiedGreen
                        )
                    }
                }
            }
            Text(
                text = place.subtitle,
                style = Typography.bodySmall,
                color = SlateMuted
            )
        }
    }
}

@Composable
private fun QuickSearchChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean = false,
    badgeColor: Color? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(1.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) PrimaryActionBlue else PureWhiteCard)
            .border(1.dp, if (isSelected) PrimaryActionBlue else BorderCanvas, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 6.dp)
            .testTag("quick_search_chip_$label")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) PureWhiteCard else SlateMuted,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = if (isSelected) PureWhiteCard else DeepSlateText
            )
            if (badgeColor != null && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                )
            }
        }
    }
}
