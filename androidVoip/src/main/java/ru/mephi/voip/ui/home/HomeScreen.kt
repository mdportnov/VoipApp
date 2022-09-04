@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class
)

package ru.mephi.voip.ui.home

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject
import ru.mephi.voip.ui.MasterScreens
import ru.mephi.voip.ui.caller.CallerScreen
import ru.mephi.voip.ui.common.NoRippleTheme
import ru.mephi.voip.ui.home.screens.catalog.CatalogScreen
import ru.mephi.voip.ui.home.screens.profile.ProfileScreen
import ru.mephi.voip.ui.settings.PreferenceRepository
import ru.mephi.voip.utils.NavAnimationUtils

@Composable
internal fun HomeScreen(
    masterNavController: NavHostController
) {
    rememberSystemUiController().let {
        it.setStatusBarColor(MaterialTheme.colorScheme.background)
        it.setNavigationBarColor(
            TopAppBarDefaults.smallTopAppBarColors().containerColor(scrollFraction = 1.0f).value
        )
    }

    val navController = rememberAnimatedNavController()
    Scaffold(
        bottomBar = {
            HomeScreenNavBar(navController = navController)
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            HomeScreenNavCtl(
                navController = navController,
                masterNavController = masterNavController
            )
        }
    }
}

@Composable
private fun HomeScreenNavBar(
    navController: NavHostController
) {
    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        NavigationBar {
            val currentBackStack by navController.currentBackStackEntryAsState()
            var current = currentBackStack?.destination?.route
            homeScreensList.forEach{ item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = if (current == item.route) item.selectedIcon else item.icon,
                            contentDescription = stringResource(id = item.title)
                        )
                    },
                    label = { Text(
                        text = stringResource(id = item.title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) },
                    selected = current == item.route,
                    onClick = {
                        current = getCurrentRoute(navController)
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route)
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeScreenNavCtl(
    navController: NavHostController,
    masterNavController: NavHostController
) {
    val preferenceRepository: PreferenceRepository by inject()
    val startScreen by preferenceRepository.startScreen.collectAsState(initial = Screens.Catalog)

    val scope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    AnimatedNavHost(navController = navController, startDestination = startScreen.route) {
        composable(
            route = Screens.Dialer.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) { backStackEntry ->
            val callerNumber = backStackEntry.arguments?.getString("caller_number")
            val callerName = backStackEntry.arguments?.getString("caller_name")
            CallerScreen(
                navController = navController,
                callerNameArg = callerName,
                callerNumberArg = callerNumber,
                isPermissionGranted = true,
                openDialPad = {
                    scope.launch {
                        modalBottomSheetState.show()
                    }
                }
            )
        }
        composable(
            route = Screens.Catalog.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            CatalogScreen()
        }
        composable(
            route = Screens.Settings.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            ProfileScreen(
                openSettings = {
                    masterNavController.navigate(route = MasterScreens.SettingsScreen.route)
                },
                openLogin = {
                    masterNavController.navigate(route = MasterScreens.LoginScreen.route)
                }
            )
        }
    }
//    HomeDialPad(modalBottomSheetState)
}

@Composable
private fun HomeDialPad(sheetState: ModalBottomSheetState) {
    ModalBottomSheetLayout(
        sheetContent = {

        },
        sheetState = sheetState
    ) {

    }
}

private fun getCurrentRoute(navController: NavController): String {
    return navController.currentBackStackEntry?.destination?.route ?: ""
}

