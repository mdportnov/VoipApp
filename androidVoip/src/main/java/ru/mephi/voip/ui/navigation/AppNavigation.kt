package ru.mephi.voip.ui.navigation

import android.Manifest
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.IntOffset
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
                when (initialState.destination.route) {
                    Screen.Catalog.route,
                    Screen.Profile.route,
                    Screen.Settings.route -> {
                        slideIntoContainer(NavAnimationUtils.SLIDE_RIGHT, NavAnimationUtils.ANIMATION)
                    }
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Screen.Catalog.route,
                    Screen.Profile.route,
                    Screen.Settings.route -> {
                        slideOutOfContainer(NavAnimationUtils.SLIDE_LEFT, NavAnimationUtils.ANIMATION)
                    }
                    else -> null
                }
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
                when (initialState.destination.route) {
                    Screen.Caller.route -> {
                        slideIntoContainer(NavAnimationUtils.SLIDE_LEFT, NavAnimationUtils.ANIMATION)
                    }
                    Screen.Profile.route,
                    Screen.Settings.route -> {
                        slideIntoContainer(NavAnimationUtils.SLIDE_RIGHT, NavAnimationUtils.ANIMATION)
                    }
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Screen.Caller.route -> {
                        slideOutOfContainer(NavAnimationUtils.SLIDE_RIGHT, NavAnimationUtils.ANIMATION)
                    }
                    Screen.Profile.route,
                    Screen.Settings.route -> {
                        slideOutOfContainer(NavAnimationUtils.SLIDE_LEFT, NavAnimationUtils.ANIMATION)
                    }
                    else -> null
                }
            }
        ) {
            CatalogScreen(navController)
        }
        composable(
            route = Screen.Profile.route,
            enterTransition = {
                when (initialState.destination.route) {
                    Screen.Caller.route,
                    Screen.Catalog.route -> {
                        slideIntoContainer(NavAnimationUtils.SLIDE_LEFT, NavAnimationUtils.ANIMATION)
                    }
                    Screen.Settings.route -> {
                        slideIntoContainer(NavAnimationUtils.SLIDE_RIGHT, NavAnimationUtils.ANIMATION)
                    }
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Screen.Caller.route,
                    Screen.Catalog.route -> {
                        slideOutOfContainer(NavAnimationUtils.SLIDE_RIGHT, NavAnimationUtils.ANIMATION)
                    }
                    Screen.Settings.route -> {
                        slideOutOfContainer(NavAnimationUtils.SLIDE_LEFT, NavAnimationUtils.ANIMATION)
                    }
                    else -> null
                }
            }
        ) {
            ProfileScreen {
                navController.navigate(Screen.Settings.route)
            }
        }
        composable(
            route = Screen.Settings.route,
            enterTransition = {
                when (initialState.destination.route) {
                    Screen.Caller.route,
                    Screen.Catalog.route,
                    Screen.Profile.route -> {
                        slideIntoContainer(NavAnimationUtils.SLIDE_LEFT, NavAnimationUtils.ANIMATION)
                    }
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Screen.Caller.route,
                    Screen.Catalog.route,
                    Screen.Profile.route -> {
                        slideOutOfContainer(NavAnimationUtils.SLIDE_RIGHT, NavAnimationUtils.ANIMATION)
                    }
                    else -> null
                }
            }
        ) {
            SettingsScreen(navController)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
object NavAnimationUtils {
    val SLIDE_RIGHT = AnimatedContentScope.SlideDirection.Right
    val SLIDE_LEFT = AnimatedContentScope.SlideDirection.Left
    val ANIMATION: FiniteAnimationSpec<IntOffset> = tween(400)
}