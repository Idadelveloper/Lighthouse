package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.NavTab
import com.example.ui.components.BlackoutOverlay
import com.example.ui.components.FakeCallOverlay
import com.example.ui.components.LighthouseBottomBar
import com.example.ui.components.LighthouseTopBar
import com.example.ui.components.ToastBanner
import com.example.ui.screens.ActiveWalkScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.PocketVoiceScreen
import com.example.ui.screens.RouteEvidenceSheet
import com.example.ui.screens.RoutePlannerScreen
import com.example.ui.screens.SafetyScreen
import com.example.ui.theme.LighthouseTheme
import com.example.ui.theme.WarmCloudBackground
import com.example.viewmodel.LighthouseViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LighthouseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LighthouseTheme {
                LighthouseApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LighthouseApp(viewModel: LighthouseViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isPocketMode by viewModel.isPocketVoiceMode.collectAsStateWithLifecycle()
    val isRouteSheetOpen by viewModel.isRouteSheetOpen.collectAsStateWithLifecycle()
    val showEvidenceSheet by viewModel.showEvidenceSheet.collectAsStateWithLifecycle()
    val fakeCallActive by viewModel.fakeCallActive.collectAsStateWithLifecycle()
    val blackoutActive by viewModel.blackoutActive.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    // Handle Back Press
    BackHandler(enabled = isRouteSheetOpen || showEvidenceSheet || fakeCallActive || isPocketMode || currentTab != NavTab.MAP) {
        when {
            isRouteSheetOpen -> viewModel.setRouteSheetVisible(false)
            showEvidenceSheet -> viewModel.setEvidenceSheetVisible(false)
            fakeCallActive -> viewModel.dismissFakeCall()
            isPocketMode -> viewModel.togglePocketMode(false)
            currentTab != NavTab.MAP -> viewModel.selectTab(NavTab.MAP)
        }
    }

    val topSubtitle = when (currentTab) {
        NavTab.MAP -> "Map"
        NavTab.WALK -> "Active Walk Guidance"
        NavTab.COMMUNITY -> "Community"
        NavTab.SAFETY -> "Safety"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = WarmCloudBackground,
            topBar = {
                if (!isPocketMode) {
                    LighthouseTopBar(
                        subtitle = topSubtitle,
                        onNotificationsClick = {
                            viewModel.showToast("No new safety notices in your area.")
                        }
                    )
                }
            },
            bottomBar = {
                if (!isPocketMode) {
                    LighthouseBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { tab ->
                            viewModel.selectTab(tab)
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isPocketMode) {
                    PocketVoiceScreen(viewModel = viewModel)
                } else {
                    when (currentTab) {
                        NavTab.MAP -> RoutePlannerScreen(viewModel = viewModel)
                        NavTab.WALK -> ActiveWalkScreen(viewModel = viewModel)
                        NavTab.COMMUNITY -> CommunityScreen(viewModel = viewModel)
                        NavTab.SAFETY -> SafetyScreen(viewModel = viewModel)
                    }
                }
            }
        }

        // Modal Route Evidence Sheet
        if (showEvidenceSheet) {
            RouteEvidenceSheet(
                viewModel = viewModel,
                onDismiss = { viewModel.setEvidenceSheetVisible(false) }
            )
        }

        // Fake Call Overlay
        FakeCallOverlay(
            isVisible = fakeCallActive,
            onDismiss = { viewModel.dismissFakeCall() }
        )

        // Screen Blackout Overlay
        BlackoutOverlay(
            isActive = blackoutActive,
            onTripleTap = { viewModel.disableBlackout() }
        )

        // Ambient feedback toast
        ToastBanner(
            message = toastMessage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (isPocketMode) 16.dp else 70.dp)
        )
    }
}
