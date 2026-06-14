package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.*
import com.example.ui.theme.AuraThemeLayout
import com.example.viewmodel.ActiveScreen
import com.example.viewmodel.SocialViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SocialViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeColor by viewModel.currentTheme.collectAsStateWithLifecycle()
            val activeScreen by viewModel.activeScreen.collectAsStateWithLifecycle()
            val activeChatRoomId by viewModel.activeChatRoomId.collectAsStateWithLifecycle()
            val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

            AuraThemeLayout(theme = themeColor) {
                if (!isLoggedIn) {
                    AuthScreen(viewModel = viewModel)
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            // Only show bottom navigation bar when no active specific, single conversation is expanded.
                            if (activeChatRoomId == null) {
                                AuraBottomNavigation(
                                    activeScreen = activeScreen,
                                    onScreenSelected = { viewModel.selectScreen(it) }
                                )
                            }
                        },
                        contentWindowInsets = WindowInsets.safeDrawing
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(innerPadding)
                        ) {
                            when (activeScreen) {
                                ActiveScreen.FEED -> HomeScreen(viewModel = viewModel)
                                ActiveScreen.MESSENGER -> ChatScreen(viewModel = viewModel)
                                ActiveScreen.CREATOR_HUB -> CreatorHubScreen(viewModel = viewModel)
                                ActiveScreen.PROFILE -> ProfileScreen(viewModel = viewModel)
                                ActiveScreen.MODERATION -> ModerationScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuraBottomNavigation(
    activeScreen: ActiveScreen,
    onScreenSelected: (ActiveScreen) -> Unit
) {
    NavigationBar(
        modifier = Modifier.testTag("aura_bottom_navbar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val screens = remember {
            listOf(
                Triple(ActiveScreen.FEED, "Flow Feed", Icons.Filled.AllInclusive to Icons.Outlined.AllInclusive),
                Triple(ActiveScreen.MESSENGER, "Secure Chat", Icons.Filled.Send to Icons.Outlined.Send),
                Triple(ActiveScreen.CREATOR_HUB, "Creator", Icons.Filled.Campaign to Icons.Outlined.Campaign),
                Triple(ActiveScreen.PROFILE, "Profile", Icons.Filled.AccountCircle to Icons.Outlined.AccountCircle),
                Triple(ActiveScreen.MODERATION, "Safety", Icons.Filled.Security to Icons.Outlined.Security)
            )
        }

        screens.forEach { item ->
            val isSelected = activeScreen == item.first
            val iconSelected = if (isSelected) item.third.first else item.third.second
            val tag = when (item.first) {
                ActiveScreen.FEED -> "nav_feed_btn"
                ActiveScreen.MESSENGER -> "nav_chats_btn"
                ActiveScreen.CREATOR_HUB -> "nav_creator_btn"
                ActiveScreen.PROFILE -> "nav_profile_btn"
                ActiveScreen.MODERATION -> "nav_safety_btn"
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(item.first) },
                icon = { Icon(iconSelected, contentDescription = item.second) },
                label = { Text(item.second, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag(tag),
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
            )
        }
    }
}
