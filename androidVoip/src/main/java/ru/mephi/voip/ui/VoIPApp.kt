package ru.mephi.voip.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import ru.mephi.voip.ui.navigation.BottomNavItem
import ru.mephi.voip.ui.navigation.MyBottomNavigation
import ru.mephi.voip.ui.navigation.NavigationGraph

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VoIPApp() {
    val navController = rememberAnimatedNavController()
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