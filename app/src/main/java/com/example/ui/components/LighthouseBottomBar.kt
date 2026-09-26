package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NavTab
import com.example.ui.theme.BorderCanvas
import com.example.ui.theme.EmergencyRose
import com.example.ui.theme.PrimaryActionBlue
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.SlateLight
import com.example.ui.theme.Typography

@Composable
fun LighthouseBottomBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp)
            .background(PureWhiteCard)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                title = "Map",
                icon = Icons.Default.Place,
                isSelected = currentTab == NavTab.MAP,
                onClick = { onTabSelected(NavTab.MAP) },
                testTag = "tab_map"
            )
            BottomNavItem(
                title = "Walk",
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                isSelected = currentTab == NavTab.WALK,
                onClick = { onTabSelected(NavTab.WALK) },
                testTag = "tab_walk"
            )
            BottomNavItem(
                title = "Community",
                icon = Icons.Default.Shield,
                isSelected = currentTab == NavTab.COMMUNITY,
                onClick = { onTabSelected(NavTab.COMMUNITY) },
                testTag = "tab_community"
            )
            BottomNavItem(
                title = "Safety",
                icon = Icons.Default.VerifiedUser,
                isSelected = currentTab == NavTab.SAFETY,
                onClick = { onTabSelected(NavTab.SAFETY) },
                accentColor = EmergencyRose,
                testTag = "tab_safety"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    accentColor: androidx.compose.ui.graphics.Color? = null
) {
    val activeColor = accentColor ?: PrimaryActionBlue
    val inactiveColor = SlateLight
    val currentColor = if (isSelected) activeColor else inactiveColor

    Column(
        modifier = Modifier
            .size(width = 72.dp, height = 56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 28.dp),
                onClick = onClick
            )
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = currentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            style = Typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = currentColor
        )
    }
}
