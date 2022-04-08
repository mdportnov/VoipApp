package ru.mephi.voip.ui.navigation

import android.Manifest
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import ru.mephi.voip.ui.BottomNavItem
import ru.mephi.voip.ui.caller.CallerScreen
import ru.mephi.voip.ui.catalog.CatalogScreen
import ru.mephi.voip.ui.profile.ProfileScreen

const val CALLER_NUMBER_KEY = "caller_number"
const val CALLER_NAME_KEY = "caller_name"

@OptIn(
    ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun NavigationGraph(navController: NavHostController, starDest: String) {
    val isPermissionGranted = rememberMultiplePermissionsState(
        listOf(Manifest.permission.USE_SIP, Manifest.permission.RECORD_AUDIO)
    )

    NavHost(
        navController = navController, startDestination = starDest
    ) {
        composable(
            route = BottomNavItem.Caller.screen_route.plus("?$CALLER_NUMBER_KEY={$CALLER_NUMBER_KEY}&$CALLER_NAME_KEY={$CALLER_NAME_KEY}"),
//            arguments = listOf(
//                navArgument(CALLER_NUMBER_KEY) {
//                    NavType.StringType
//                    nullable = true
//                },
//                navArgument(CALLER_NAME_KEY) {
//                    NavType.StringType
//                    nullable = true
//                })
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
            route = BottomNavItem.Catalog.screen_route
        ) {
            CatalogScreen(navController)
        }
        composable(route = BottomNavItem.Profile.screen_route) {
            ProfileScreen(navController)
        }
    }
}