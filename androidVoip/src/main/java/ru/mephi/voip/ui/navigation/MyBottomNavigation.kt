package ru.mephi.voip.ui.navigation

import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import org.koin.androidx.compose.inject
import ru.mephi.voip.ui.catalog.CatalogViewModel

@Composable
fun HomeBottomNavigation(navController: NavController) {
    val items = listOf(Screen.Caller, Screen.Catalog, Screen.Profile)
    val viewModel: CatalogViewModel by inject()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        items.forEach { item ->
            val selected =
                currentDestination?.hierarchy?.any { it.route == item.route } == true
            val icon = if (selected) item.selectedIcon else item.icon
            CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
                NavigationBarItem(
                    icon = { Icon(icon, contentDescription = stringResource(id = item.title)) },
                    label = { Text(text = stringResource(id = item.title)) },
                    alwaysShowLabel = true,
                    selected = currentRoute == item.route,
                    onClick = {
                        if (item == Screen.Catalog) {
                            if (viewModel.catalogStack.value.size > 1) {
                                viewModel.goToStartPage()
                            }
                        }
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { screen_route ->
                                popUpTo(screen_route) {
                                    saveState = true
                                }
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