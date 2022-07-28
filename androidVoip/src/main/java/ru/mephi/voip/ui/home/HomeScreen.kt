@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class
)

package ru.mephi.voip.ui.home

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import org.koin.androidx.compose.inject
import ru.mephi.voip.ui.caller.CallerScreen
import ru.mephi.voip.ui.catalog.CatalogScreen
import ru.mephi.voip.ui.common.NoRippleTheme
import ru.mephi.voip.ui.profile.ProfileScreen
import ru.mephi.voip.ui.settings.PreferenceRepository
import ru.mephi.voip.ui.settings.SettingsScreen

private val homeScreensList = Screens::class.sealedSubclasses.mapNotNull { it.objectInstance }

@Composable
internal fun HomeScreen(
    masterNavController: NavHostController
) {
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
                            launchSingleTop = true
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

    AnimatedNavHost(navController = navController, startDestination = startScreen.route) {
        composable(
            route = Screens.Dialer.route,
            enterTransition = {
                when (initialState.destination.route) {
                    Screens.Catalog.route,
                    Screens.Profile.route,
                    Screens.Settings.route -> {
                        slideIntoContainer(NavAnimationUtils.SLIDE_RIGHT, NavAnimationUtils.ANIMATION)
                    }
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Screens.Catalog.route,
                    Screens.Profile.route,
                    Screens.Settings.route -> {
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
                isPermissionGranted = true
            )
        }
        composable(
            route = Screens.Catalog.route,
            enterTransition = {
                when (initialState.destination.route) {
                    Screens.Dialer.route -> {
                        slideIntoContainer(NavAnimationUtils.SLIDE_LEFT, NavAnimationUtils.ANIMATION)
                    }
                    Screens.Profile.route,
                    Screens.Settings.route -> {
                        slideIntoContainer(NavAnimationUtils.SLIDE_RIGHT, NavAnimationUtils.ANIMATION)
                    }
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Screens.Dialer.route -> {
                        slideOutOfContainer(NavAnimationUtils.SLIDE_RIGHT, NavAnimationUtils.ANIMATION)
                    }
                    Screens.Profile.route,
                    Screens.Settings.route -> {
                        slideOutOfContainer(NavAnimationUtils.SLIDE_LEFT, NavAnimationUtils.ANIMATION)
                    }
                    else -> null
                }
            }
        ) {
            CatalogScreen(navController)
        }
        composable(
            route = Screens.Profile.route,
            enterTransition = {
                when (initialState.destination.route) {
                    Screens.Dialer.route,
                    Screens.Catalog.route -> {
                        slideIntoContainer(NavAnimationUtils.SLIDE_LEFT, NavAnimationUtils.ANIMATION)
                    }
                    Screens.Settings.route -> {
                        slideIntoContainer(NavAnimationUtils.SLIDE_RIGHT, NavAnimationUtils.ANIMATION)
                    }
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Screens.Dialer.route,
                    Screens.Catalog.route -> {
                        slideOutOfContainer(NavAnimationUtils.SLIDE_RIGHT, NavAnimationUtils.ANIMATION)
                    }
                    Screens.Settings.route -> {
                        slideOutOfContainer(NavAnimationUtils.SLIDE_LEFT, NavAnimationUtils.ANIMATION)
                    }
                    else -> null
                }
            }
        ) {
            ProfileScreen {
                navController.navigate(Screens.Settings.route)
            }
        }
        composable(
            route = Screens.Settings.route,
            enterTransition = {
                when (initialState.destination.route) {
                    Screens.Dialer.route,
                    Screens.Catalog.route,
                    Screens.Profile.route -> {
                        slideIntoContainer(NavAnimationUtils.SLIDE_LEFT, NavAnimationUtils.ANIMATION)
                    }
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Screens.Dialer.route,
                    Screens.Catalog.route,
                    Screens.Profile.route -> {
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

private fun getCurrentRoute(navController: NavController): String {
    return navController.currentBackStackEntry?.destination?.route ?: ""
}


object NavAnimationUtils {
    val SLIDE_RIGHT = AnimatedContentScope.SlideDirection.Right
    val SLIDE_LEFT = AnimatedContentScope.SlideDirection.Left
    val ANIMATION: FiniteAnimationSpec<IntOffset> = tween(400)
}
