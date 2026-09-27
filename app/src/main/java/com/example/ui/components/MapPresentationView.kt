package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DayNightMode
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

@Composable
fun MapPresentationView(
    mode: DayNightMode,
    isActiveWalk: Boolean = false,
    onMapClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isActiveWalk) 210.dp else 260.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, MistBlue, RoundedCornerShape(18.dp))
            .clickable(onClick = onMapClick)
            .testTag("map_presentation_view")
    ) {
        // Base realistic Google Maps asset
        val mapRes = when {
            isActiveWalk -> R.drawable.map_walk_sf
            mode == DayNightMode.NIGHT -> R.drawable.map_night_sf
            else -> R.drawable.map_day_sf
        }

        Image(
            painter = painterResource(id = mapRes),
            contentDescription = "Google Maps View: 16th St BART to Mission Dolores Park",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Scrim gradient for text contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (mode == DayNightMode.NIGHT) {
                            listOf(Color(0x5516252D), Color(0x3316252D), Color(0x8816252D))
                        } else {
                            listOf(Color(0x3320343F), Color.Transparent, Color(0x5520343F))
                        }
                    )
                )
        )

        if (mode == DayNightMode.DAY && !isActiveWalk) {
            // Day Shade layer: Tree canopy corridor representation
            Box(
                modifier = Modifier
                    .padding(top = 40.dp, start = 48.dp)
                    .size(width = 170.dp, height = 110.dp)
                    .rotate(6f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(VerifiedGreen.copy(alpha = 0.18f))
                    .border(1.dp, VerifiedGreen.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PureWhiteCard.copy(alpha = 0.92f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "West Canopy Zone",
                        style = MonospaceDataSm,
                        color = VerifiedGreen
                    )
                }
            }

            // Map Pins
            // Origin: 16th St BART Pin
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 18.dp, start = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(6.dp))
                        .clip(RoundedCornerShape(6.dp))
                        .background(PrimaryActionBlue)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsSubway,
                        contentDescription = null,
                        tint = PureWhiteCard,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "16th St BART",
                        style = MonospaceDataSm,
                        color = PureWhiteCard
                    )
                }
            }

            // Destination: Dolores Park Hill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 44.dp, end = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(6.dp))
                        .clip(RoundedCornerShape(6.dp))
                        .background(VerifiedGreen)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Park,
                        contentDescription = null,
                        tint = PureWhiteCard,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Dolores Park Hill",
                        style = MonospaceDataSm,
                        color = PureWhiteCard
                    )
                }
            }

            // Bi-Rite Storefront Badge
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 32.dp, top = 60.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PureWhiteCard.copy(alpha = 0.95f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(VerifiedGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Bi-Rite Storefront",
                        style = MonospaceDataSm,
                        color = DeepSlateText
                    )
                }
            }

            // Guerrero Sidewalk Gap alert
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 46.dp, end = 18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFFFDAD6))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Construction,
                        contentDescription = null,
                        tint = EmergencyRose,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Guerrero Gap",
                        style = MonospaceDataSm,
                        color = EmergencyRose
                    )
                }
            }

            // Shade Legend Pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp, start = 12.dp, end = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PureWhiteCard.copy(alpha = 0.94f))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MistBlue)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Shade data unavailable",
                            style = MonospaceDataSm,
                            color = DeepSlateText
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Status: UNKNOWN",
                            style = MonospaceDataSm,
                            color = SlateMuted
                        )
                    }
                }
            }
        }

        if (mode == DayNightMode.NIGHT && !isActiveWalk) {
            // Night Mode Pins & Overlay
            // 311 Outage Callout
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 12.dp, start = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PureWhiteCard.copy(alpha = 0.95f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = EmergencyRose,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "311 Outage: 17th St (3h ago)",
                        style = MonospaceDataSm,
                        color = EmergencyRose
                    )
                }
            }

            // Safe Haven Pin
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PureWhiteCard.copy(alpha = 0.95f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(VerifiedGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Bi-Rite Storefront",
                        style = MonospaceDataSm,
                        color = DeepSlateText
                    )
                }
            }

            // Mapped Corridor Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 36.dp, start = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(DeepSlateDark.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = SoftSage,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Valencia mapped pedestrian path",
                        style = MonospaceDataSm,
                        color = PureWhiteCard
                    )
                }
            }

            // Night Map Legend Pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp, start = 12.dp, end = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PureWhiteCard.copy(alpha = 0.94f))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(StreetlightLitYellow)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mapped (SFPUC)",
                            style = MonospaceDataSm,
                            color = DeepSlateText
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "◐",
                            style = MonospaceDataSm,
                            color = SlateLight
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mixed",
                            style = MonospaceDataSm,
                            color = SlateMuted
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmergencyRose)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SF 311 Outage",
                            style = MonospaceDataSm,
                            color = EmergencyRose
                        )
                    }
                }
            }
        }

        if (isActiveWalk) {
            // Live Rerouting Pill Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp, start = 12.dp, end = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PureWhiteCard.copy(alpha = 0.95f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.AltRoute,
                        contentDescription = null,
                        tint = VerifiedGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Valencia Corridor waypoint path mapped to Mission Dolores Park.",
                        style = Typography.bodySmall,
                        color = DeepSlateText
                    )
                }
            }

            // Pulsing live pedestrian beacon dot
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(PrimaryActionBlue.copy(alpha = 0.35f))
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(PrimaryActionBlue)
                        .border(2.dp, PureWhiteCard, CircleShape)
                )
            }

            // Telemetry & Recenter
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp, start = 12.dp, end = 12.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PureWhiteCard.copy(alpha = 0.92f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "CORRIDOR: VALENCIA STREET",
                            style = MonospaceDataSm,
                            color = DeepSlateText
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(PureWhiteCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Recenter",
                            tint = PrimaryActionBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
