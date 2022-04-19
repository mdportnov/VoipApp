package ru.mephi.voip.ui.navigation

import android.Manifest
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.koin.androidx.compose.inject
import ru.mephi.voip.ui.caller.CallerScreen
import ru.mephi.voip.ui.catalog.CatalogScreen
import ru.mephi.voip.ui.profile.ProfileScreen
import ru.mephi.voip.ui.settings.PreferenceRepository
import ru.mephi.voip.ui.settings.SettingsScreen

@OptIn(
    ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class,
    ExperimentalPermissionsApi::class, ExperimentalAnimationApi::class
)
@Composable
fun AppNavigation(navController: NavHostController) {
    val isPermissionGranted = rememberMultiplePermissionsState(
        listOf(Manifest.permission.USE_SIP, Manifest.permission.RECORD_AUDIO)
    )

    val preferenceRepository: PreferenceRepository by inject()
    val startRoute by preferenceRepository.startScreen.collectAsState(initial = Screen.Catalog)

    AnimatedNavHost(navController = navController, startDestination = startRoute.route) {
        composable(
            route = Screen.Caller.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(400)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(400)
                )
            }
        ) { backStackEntry ->
            val callerNumber = backStackEntry.arguments?.getString("caller_number")
            val callerName = backStackEntry.arguments?.getString("caller_name")
            CallerScreen(
                navController = navController,
                callerNameArg = callerName,
                callerNumberArg = callerNumber,
                isPermissionGranted = isPermissionGranted.allPermissionsGranted
            )
        }
        composable(
            route = Screen.Catalog.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Left,
                    animationSpec = tween(400)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(400)
                )
            }
        ) {
            CatalogScreen(navController)
        }
        composable(
            route = Screen.Profile.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Left,
                    animationSpec = tween(400)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(400)
                )
            }) {
            ProfileScreen {
                navController.navigate(Screen.Settings.route)
            }
        }
        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentScope.SlideDirection.Left,
                    animationSpec = tween(400)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentScope.SlideDirection.Right,
                    animationSpec = tween(400)
                )
            }) {
            SettingsScreen(navController)
        }
    }
}