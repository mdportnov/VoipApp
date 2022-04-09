package ru.mephi.voip.ui.navigation

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.koin.androidx.compose.inject
import ru.mephi.voip.ui.catalog.NewCatalogViewModel
import ru.mephi.voip.utils.ColorAccent

@Composable
fun MyBottomNavigation(navController: NavController) {
    val items = listOf(BottomNavItem.Caller, BottomNavItem.Catalog, BottomNavItem.Profile)
    val viewModel: NewCatalogViewModel by inject()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomNavigation(
        backgroundColor = Color.White,
        contentColor = Color.Black
    ) {
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(text = item.title, fontSize = 10.sp) },
                selectedContentColor = ColorAccent,
                unselectedContentColor = Color.Black.copy(0.4f),
                alwaysShowLabel = true,
                selected = if (item.screen_route == BottomNavItem.Caller.screen_route)
                    currentRoute == item.screen_route.plus("?$CALLER_NUMBER_KEY={$CALLER_NUMBER_KEY}&$CALLER_NAME_KEY={$CALLER_NAME_KEY}")
                else currentRoute == item.screen_route,
                onClick = {
                    if (item == BottomNavItem.Catalog) {
                        if (viewModel.catalogStack.value.size > 1) {
                            viewModel.goToStartPage()
                        }
                    }

                    navController.navigate(item.screen_route) {
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