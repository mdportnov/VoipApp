@file:OptIn(ExperimentalAnimationApi::class)

package ru.mephi.voip.ui.home.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import ru.mephi.voip.ui.home.screens.settings.screens.*
import ru.mephi.voip.ui.home.screens.settings.screens.BackgroundWorkScreen
import ru.mephi.voip.ui.home.screens.settings.screens.SettingsHomeScreen
import ru.mephi.voip.ui.home.screens.settings.screens.StartSelectionScreen

@Composable
internal fun SettingsScreen(
    openLogin: () -> Unit
) {
    val navController = rememberAnimatedNavController()
    Box {
        SettingsNavCtl(
            navController = navController,
            openLogin = openLogin
        )
    }
}

@Composable
private fun SettingsNavCtl(
    navController: NavHostController,
    openLogin: () -> Unit
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = Screens.SettingsHomeScreen.route
    ) {
        composable(
            route = Screens.SettingsHomeScreen.route
        ) {
            SettingsHomeScreen(
                settingsNavCtl = navController,
                openLogin = openLogin
            )
        }
        composable(
            route = Screens.BackgroundWorkScreen.route
        ) {
            BackgroundWorkScreen(goBack = { navController.popBackStack() })
        }
        composable(
             route = Screens.StartSelectionScreen.route
        ) {
            StartSelectionScreen(goBack = { navController.popBackStack() })
        }
        composable(
            route = Screens.EnableOverlayScreen.route
        ) {
            EnableOverlayScreen(goBack = { navController.popBackStack() })
        }
        composable(
            route = Screens.ClearDataScreen.route
        ) {
            ClearDataScreen(goBack = { navController.popBackStack()  } )
        }
        composable(
            route = Screens.AboutAppScreen.route
        ) {
            AboutAppScreen(goBack = { navController.popBackStack()  } )
        }
    }
}