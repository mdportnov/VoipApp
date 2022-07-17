package ru.mephi.voip.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import ru.mephi.voip.ui.navigation.AppNavigation
import ru.mephi.voip.ui.navigation.HomeBottomNavigation
import ru.mephi.voip.ui.navigation.Screen

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val navController = rememberAnimatedNavController()
    val mainScreen = listOf(Screen.Caller.route, Screen.Catalog.route, Screen.Profile.route)
    val showBottomBar =
        navController.currentBackStackEntryAsState().value?.destination?.route in mainScreen

    Scaffold(
        bottomBar = {
            AnimatedVisibility(visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                content = {
                    HomeBottomNavigation(navController = navController)
                })
        }
    ) { innerPadding ->
        val paddingBottom = if (showBottomBar) innerPadding else PaddingValues(0.dp)

        Box(modifier = Modifier.padding(paddingBottom)) {
            AppNavigation(navController = navController)
        }
    }
}