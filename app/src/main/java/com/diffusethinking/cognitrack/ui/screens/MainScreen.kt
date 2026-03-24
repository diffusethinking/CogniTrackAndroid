package com.diffusethinking.cognitrack.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diffusethinking.cognitrack.ui.ActiveScreen
import com.diffusethinking.cognitrack.ui.MainViewModel
import com.diffusethinking.cognitrack.ui.theme.AppGreen

private data class TabItem(val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem("Home", Icons.Filled.Home),
    TabItem("History", Icons.Filled.Schedule),
    TabItem("Trends", Icons.Filled.QueryStats),
    TabItem("Settings", Icons.Filled.Settings)
)

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val records by viewModel.allRecords.collectAsStateWithLifecycle()
    val recordsAsc by viewModel.allRecordsAscending.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = viewModel.activeScreen,
            animationSpec = tween(250),
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                is ActiveScreen.Onboarding -> {
                    OnboardingScreen(
                        onComplete = { startBaseline ->
                            viewModel.setOnboardingCompleted()
                            if (startBaseline) {
                                viewModel.navigateTo(
                                    ActiveScreen.Test(
                                        isBaseline = true,
                                        isGuestMode = false,
                                        guestName = ""
                                    )
                                )
                            } else {
                                viewModel.navigateTo(ActiveScreen.Tabs)
                            }
                        }
                    )
                }

                is ActiveScreen.Tabs -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 800.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Crossfade(
                                targetState = viewModel.selectedTab,
                                animationSpec = tween(200),
                                label = "tab_transition"
                            ) { tab ->
                                when (tab) {
                                    0 -> HomeScreen(
                                        baselineRT = viewModel.baselineRT,
                                        baselineDate = viewModel.baselineDate,
                                        guestMode = viewModel.guestMode,
                                        guestName = viewModel.guestName,
                                        onGuestModeChange = { viewModel.updateGuestMode(it) },
                                        onGuestNameChange = { viewModel.updateGuestName(it) },
                                        onStartTest = { isBaseline, isGuestMode, guestName ->
                                            viewModel.navigateTo(
                                                ActiveScreen.Test(isBaseline, isGuestMode, guestName)
                                            )
                                        }
                                    )

                                    1 -> HistoryScreen(
                                        records = records,
                                        viewModel = viewModel
                                    )

                                    2 -> TrendsScreen(
                                        allRecords = recordsAsc,
                                        viewModel = viewModel
                                    )

                                    3 -> SettingsScreen(viewModel = viewModel)
                                }
                            }
                        }

                        NavigationBar(
                            containerColor = Color(0xFF1C1C1E),
                            contentColor = Color.White
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                NavigationBarItem(
                                    selected = viewModel.selectedTab == index,
                                    onClick = { viewModel.selectedTab = index },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AppGreen,
                                        selectedTextColor = AppGreen,
                                        unselectedIconColor = Color(0xFF8E8E93),
                                        unselectedTextColor = Color(0xFF8E8E93),
                                        indicatorColor = Color(0xFF2C2C2E)
                                    )
                                )
                            }
                        }
                    }
                }

                is ActiveScreen.Test -> {
                    PVTTestScreen(
                        isBaseline = screen.isBaseline,
                        baselineRT = if (screen.isGuestMode) 0.0 else viewModel.baselineRT,
                        isGuestMode = screen.isGuestMode,
                        guestName = screen.guestName,
                        onComplete = { trials, startDate ->
                            viewModel.navigateTo(
                                ActiveScreen.Results(
                                    trials = trials,
                                    isBaseline = screen.isBaseline,
                                    startDate = startDate,
                                    isGuestMode = screen.isGuestMode,
                                    guestName = screen.guestName
                                )
                            )
                        },
                        onCancel = { viewModel.navigateTo(ActiveScreen.Tabs) }
                    )
                }

                is ActiveScreen.Results -> {
                    ResultsScreen(
                        trials = screen.trials,
                        isBaseline = screen.isBaseline,
                        startDate = screen.startDate,
                        baselineRT = viewModel.baselineRT,
                        isGuestMode = screen.isGuestMode,
                        guestName = screen.guestName,
                        isLive = true,
                        viewModel = viewModel,
                        onDone = { viewModel.navigateTo(ActiveScreen.Tabs) },
                        onRestart = {
                            viewModel.navigateTo(
                                ActiveScreen.Test(
                                    isBaseline = screen.isBaseline,
                                    isGuestMode = screen.isGuestMode,
                                    guestName = screen.guestName
                                )
                            )
                        },
                        onStartBaseline = {
                            viewModel.navigateTo(
                                ActiveScreen.Test(
                                    isBaseline = true,
                                    isGuestMode = false,
                                    guestName = ""
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}
