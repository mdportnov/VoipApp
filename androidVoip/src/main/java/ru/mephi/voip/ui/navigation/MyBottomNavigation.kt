package ru.mephi.voip.ui.navigation

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import org.koin.androidx.compose.inject
import ru.mephi.voip.ui.catalog.CatalogViewModel
import ru.mephi.voip.utils.ColorAccent

@Composable
fun HomeBottomNavigation(navController: NavController) {
    val items = listOf(Screen.Caller, Screen.Catalog, Screen.Profile)
    val viewModel: CatalogViewModel by inject()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentDestination = navBackStackEntry?.destination

    BottomNavigation(
        backgroundColor = Color.White,
        contentColor = Color.Black
    ) {
        items.forEach { item ->
            val selected =
                currentDestination?.hierarchy?.any { it.route == item.route } == true
            val icon = if (selected) item.selectedIcon else item.icon
            BottomNavigationItem(
                icon = { Icon(icon, contentDescription = stringResource(id = item.title)) },
                label = { Text(text = stringResource(id = item.title), fontSize = 10.sp) },
                selectedContentColor = ColorAccent,
                unselectedContentColor = Color.Black.copy(0.4f),
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