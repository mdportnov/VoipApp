package ru.mephi.voip.ui.navigation

import android.Manifest
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import ru.mephi.voip.ui.caller.CallerScreen
import ru.mephi.voip.ui.catalog.CatalogScreen
import ru.mephi.voip.ui.profile.ProfileScreen

const val CALLER_NUMBER_KEY = "caller_number"
const val CALLER_NAME_KEY = "caller_name"

@OptIn(
    ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class,
    ExperimentalPermissionsApi::class, ExperimentalAnimationApi::class
)
@Composable
fun NavigationGraph(navController: NavHostController, starDest: String) {
    val isPermissionGranted = rememberMultiplePermissionsState(
        listOf(Manifest.permission.USE_SIP, Manifest.permission.RECORD_AUDIO)
    )

    AnimatedNavHost(
        navController = navController, startDestination = starDest
    ) {
        composable(
            route = BottomNavItem.Caller.screen_route.plus("?$CALLER_NUMBER_KEY={$CALLER_NUMBER_KEY}&$CALLER_NAME_KEY={$CALLER_NAME_KEY}"),
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
            val callerNumber = backStackEntry.arguments?.getString(CALLER_NUMBER_KEY)
            val callerName = backStackEntry.arguments?.getString(CALLER_NAME_KEY)
            CallerScreen(
                navController = navController,
                callerNameArg = callerName,
                callerNumberArg = callerNumber,
                isPermissionGranted = isPermissionGranted.allPermissionsGranted
            )
        }
        composable(
            route = BottomNavItem.Catalog.screen_route,
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
            route = BottomNavItem.Profile.screen_route,
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
            ProfileScreen()
        }
    }
}