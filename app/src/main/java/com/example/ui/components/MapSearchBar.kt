package com.example.ui.components

import android.Manifest
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
    val searchCategoryFilter by viewModel.searchCategoryFilter.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val isUsingCurrentLocation by viewModel.isUsingCurrentLocation.collectAsStateWithLifecycle()
    val hasActiveRoute by viewModel.hasActiveRoute.collectAsStateWithLifecycle()
    var isSearchFocused by remember { mutableStateOf(false) }

    // Dismiss search dropdown on back press
    BackHandler(enabled = isSearchFocused) {
        isSearchFocused = false
        focusManager.clearFocus()
    }

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
                        text = "Search address, landmark, haven...",
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
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 4.dp),
                                strokeWidth = 2.dp,
                                color = PrimaryActionBlue
                            )
                        }

                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = SlateLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else if (hasActiveRoute) {
                            IconButton(
                                onClick = { viewModel.clearActiveRoute() },
                                modifier = Modifier.testTag("search_bar_clear_route_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Route and Return to Plain Map",
                                    tint = SlateMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Disabled voice preview entry point
                        IconButton(
                            onClick = {
                                viewModel.startLiveVoiceSession()
                            },
                            modifier = Modifier.testTag("search_bar_voice_mic_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice companion preview unavailable",
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
                            },
                            modifier = Modifier.testTag("search_bar_my_location_button")
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
                            },
                            modifier = Modifier.testTag("search_bar_tune_options_button")
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
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            viewModel.searchForLocation(searchQuery)
                            isSearchFocused = false
                            focusManager.clearFocus()
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = PureWhiteCard,
                    unfocusedContainerColor = PureWhiteCard
                )
            )
        }

        // Suggestions & Location Results Dropdown
        AnimatedVisibility(
            visible = isSearchFocused,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(PureWhiteCard)
                    .border(1.dp, BorderCanvas, RoundedCornerShape(16.dp))
                    .padding(vertical = 8.dp)
                    .testTag("search_suggestions_dropdown")
            ) {
                Column {
                    // Category Filter Chips Inside Search
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CategoryPill(
                            label = "All",
                            isSelected = searchCategoryFilter == "ALL",
                            onClick = { viewModel.setSearchCategoryFilter("ALL") }
                        )
                        CategoryPill(
                            label = "★ Safe Havens",
                            isSelected = searchCategoryFilter == "HAVEN",
                            badgeColor = VerifiedGreen,
                            onClick = { viewModel.setSearchCategoryFilter("HAVEN") }
                        )
                        CategoryPill(
                            label = "Transit",
                            isSelected = searchCategoryFilter == "TRANSIT",
                            onClick = { viewModel.setSearchCategoryFilter("TRANSIT") }
                        )
                        CategoryPill(
                            label = "Parks",
                            isSelected = searchCategoryFilter == "PARK",
                            onClick = { viewModel.setSearchCategoryFilter("PARK") }
                        )
                        CategoryPill(
                            label = "Medical / 24h",
                            isSelected = searchCategoryFilter == "MEDICAL",
                            onClick = { viewModel.setSearchCategoryFilter("MEDICAL") }
                        )
                        CategoryPill(
                            label = "Cafes & Stores",
                            isSelected = searchCategoryFilter == "STORE",
                            onClick = { viewModel.setSearchCategoryFilter("STORE") }
                        )
                        CategoryPill(
                            label = "Civic & Community",
                            isSelected = searchCategoryFilter == "CIVIC",
                            onClick = { viewModel.setSearchCategoryFilter("CIVIC") }
                        )
                        CategoryPill(
                            label = "Landmarks",
                            isSelected = searchCategoryFilter == "LANDMARK",
                            onClick = { viewModel.setSearchCategoryFilter("LANDMARK") }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BorderCanvas)
                    )

                    // Results content with bounded height for scrollability
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp)
                    ) {
                        // 1. Recent Searches (when query is empty and recents exist)
                        if (searchQuery.isBlank() && recentSearches.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = SlateMuted,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "RECENT SEARCHES",
                                            style = MonospaceDataSm.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                            color = SlateMuted
                                        )
                                    }

                                    Text(
                                        text = "Clear All",
                                        style = Typography.labelSmall.copy(color = PrimaryActionBlue, fontWeight = FontWeight.SemiBold),
                                        modifier = Modifier
                                            .clickable { viewModel.clearRecentSearches() }
                                            .padding(4.dp)
                                            .testTag("clear_recent_searches_button")
                                    )
                                }
                            }

                            items(recentSearches, key = { "recent_${it.id}" }) { place ->
                                RecentSearchItem(
                                    place = place,
                                    onClick = {
                                        viewModel.selectSearchPlace(place)
                                        isSearchFocused = false
                                        focusManager.clearFocus()
                                    },
                                    onDelete = {
                                        viewModel.removeRecentSearch(place.id)
                                    }
                                )
                            }

                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .height(1.dp)
                                        .background(BorderCanvas)
                                )
                            }
                        }

                        // 2. Header for Suggestions or Search Results
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (searchQuery.isBlank()) {
                                        "VERIFIED PEDESTRIAN DESTINATIONS"
                                    } else {
                                        "SEARCH RESULTS (${searchResults.size})"
                                    },
                                    style = MonospaceDataSm.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = SlateMuted
                                )
                                Text(
                                    text = if (isSearching) "Searching Geocoder..." else "SF Smart Infrastructure",
                                    style = MonospaceDataSm.copy(fontSize = 10.sp),
                                    color = if (isSearching) PrimaryActionBlue else VerifiedGreen
                                )
                            }
                        }

                        // 3. Search Results list
                        if (searchResults.isNotEmpty()) {
                            items(searchResults, key = { it.id }) { place ->
                                SearchPlaceItem(
                                    place = place,
                                    onClick = {
                                        viewModel.selectSearchPlace(place)
                                        isSearchFocused = false
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        } else if (!isSearching) {
                            // Empty state: offer to geocode address
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "No saved places match \"$searchQuery\"",
                                        style = Typography.bodyMedium,
                                        color = SlateMuted
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MistBlue)
                                            .border(1.dp, PrimaryActionBlue.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                            .clickable {
                                                viewModel.searchForLocation(searchQuery)
                                                isSearchFocused = false
                                                focusManager.clearFocus()
                                            }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                            .testTag("search_with_geocoder_fallback_button")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Navigation,
                                                contentDescription = null,
                                                tint = PrimaryActionBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Search address: \"$searchQuery\"",
                                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = PrimaryActionBlue
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

        // Quick Category Destination Chips below Search (Visible when not focused)
        if (!isSearchFocused) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickSearchChip(
                    label = "Voice preview",
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
                    label = "Bi-Rite Haven",
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
                    label = "24/7 Pharmacy",
                    icon = Icons.Default.LocalHospital,
                    onClick = {
                        viewModel.selectSearchPlace(MapDataDefaults.searchSuggestions[4])
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
            .padding(horizontal = 14.dp, vertical = 9.dp)
            .testTag("search_place_item_${place.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val (icon, bg, tint) = when {
            place.isSafeHaven || place.category == "HAVEN" -> Triple(Icons.Default.Storefront, SoftSage, VerifiedGreen)
            place.category == "TRANSIT" -> Triple(Icons.Default.DirectionsSubway, MistBlue, PrimaryActionBlue)
            place.category == "PARK" -> Triple(Icons.Default.Park, SoftSage, VerifiedGreen)
            place.category == "MEDICAL" -> Triple(Icons.Default.LocalHospital, Color(0xFFFFDAD6), EmergencyRose)
            place.category == "STORE" -> Triple(Icons.Default.LocalCafe, MistBlue, DeepSlateText)
            place.category == "CIVIC" -> Triple(Icons.Default.Shield, SoftSage, VerifiedGreen)
            place.category == "LANDMARK" -> Triple(Icons.Default.Place, MutedButter, DeepSlateText)
            else -> Triple(Icons.Default.Navigation, MistBlue, PrimaryActionBlue)
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(19.dp)
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
                            text = "SAFE HAVEN",
                            style = MonospaceDataSm.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = VerifiedGreen
                        )
                    }
                }
            }
            Text(
                text = place.subtitle.ifBlank { place.address },
                style = Typography.bodySmall,
                color = SlateMuted
            )
            if (place.safetyBadge.isNotBlank() && !place.isSafeHaven) {
                Text(
                    text = place.safetyBadge,
                    style = MonospaceDataSm.copy(fontSize = 9.sp),
                    color = VerifiedGreen
                )
            }
        }
    }
}

@Composable
private fun RecentSearchItem(
    place: SearchPlace,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .testTag("recent_search_item_${place.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MistBlue.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = SlateMuted,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = place.title,
                style = Typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                color = DeepSlateText
            )
            Text(
                text = place.subtitle.ifBlank { place.address },
                style = Typography.bodySmall,
                color = SlateMuted,
                maxLines = 1
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .size(28.dp)
                .testTag("delete_recent_search_${place.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove recent search",
                tint = SlateLight,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun CategoryPill(
    label: String,
    isSelected: Boolean,
    badgeColor: Color? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) PrimaryActionBlue else MistBlue.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("search_category_pill_$label")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (badgeColor != null && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                )
            }
            Text(
                text = label,
                style = Typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                color = if (isSelected) PureWhiteCard else DeepSlateText
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
