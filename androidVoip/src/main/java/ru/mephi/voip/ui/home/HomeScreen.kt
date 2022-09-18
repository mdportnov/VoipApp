@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)

package ru.mephi.voip.ui.home

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.koin.androidx.compose.get
import org.koin.androidx.compose.inject
import ru.mephi.shared.vm.CatalogViewModel
import ru.mephi.shared.vm.DetailedInfoViewModel
import ru.mephi.voip.ui.caller.CallerScreen
import ru.mephi.voip.ui.common.NoRippleTheme
import ru.mephi.voip.ui.home.screens.catalog.catalogNavCtl
import ru.mephi.voip.ui.home.screens.profile.ProfileScreen
import ru.mephi.voip.ui.settings.PreferenceRepository

@Composable
internal fun HomeScreen(
    openLogin: () -> Unit,
    openSettings: () -> Unit
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
                openLogin = openLogin,
                openSettings = openSettings
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
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            homeScreensList.forEach{ item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = when (currentDestination?.hierarchy?.any { it.route == item.route }) {
                                true -> item.selectedIcon
                                false -> item.icon
                                else -> item.icon
                            },
                            contentDescription = stringResource(id = item.title)
                        )
                    },
                    label = { Text(
                        text = stringResource(id = item.title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) },
                    selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
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
    openLogin: () -> Unit,
    openSettings: () -> Unit,
    catalogVM: CatalogViewModel = get(),
    detailedInfoVM: DetailedInfoViewModel = get()
) {
    val preferenceRepository: PreferenceRepository by inject()
    val startScreen by preferenceRepository.startScreen.collectAsState(initial = Screens.Catalog)

    AnimatedNavHost(navController = navController, startDestination = startScreen.route) {
        composable(
            route = Screens.History.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            CallerScreen(
                navController = navController,
                isPermissionGranted = true
            )
        }
        composable(
            route = Screens.Settings.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            ProfileScreen(
                openLogin = openLogin,
                openSettings = openSettings
            )
        }
        catalogNavCtl(
            navController = navController,
            catalogVM = catalogVM,
            detailedInfoVM = detailedInfoVM
        )
    }
}
