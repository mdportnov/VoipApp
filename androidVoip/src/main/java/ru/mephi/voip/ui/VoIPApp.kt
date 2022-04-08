package ru.mephi.voip.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import ru.mephi.voip.ui.navigation.NavigationGraph

@Composable
fun VoIPApp() {
    val navController = rememberNavController()
    Scaffold(bottomBar = {
        MyBottomNavigation(navController = navController)
    }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavigationGraph(
                navController = navController,
                starDest = BottomNavItem.Catalog.screen_route
            )
        }
    }
}