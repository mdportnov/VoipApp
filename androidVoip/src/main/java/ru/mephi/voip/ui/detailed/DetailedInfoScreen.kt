@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)

package ru.mephi.voip.ui.detailed

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import ru.mephi.voip.ui.common.NoRippleTheme
import ru.mephi.voip.ui.detailed.screens.HistoryScreen
import ru.mephi.voip.ui.detailed.screens.InfoScreen


@Composable
internal fun DetailedInfoScreen(
    onGoBack: () -> Unit
) {
    val navController = rememberAnimatedNavController()
    Scaffold(
        topBar = { DetailedInfoTopBar(onGoBack) },
        bottomBar = { DetailedInfoBottomBar(navController) }
    ) {
        Box(modifier = Modifier.padding(it)) {
            DetailedInfoNavCtl(navController)
        }
    }
}

@Composable
private fun DetailedInfoTopBar(
    onGoBack: () -> Unit
) {
    SmallTopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = { onGoBack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null)
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Default.Star, contentDescription = null)
            }
        }
    )
}

@Composable
private fun DetailedInfoBottomBar(
    navController: NavHostController
) {
    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        NavigationBar {
            val currentBackStack by navController.currentBackStackEntryAsState()
            var current = currentBackStack?.destination?.route
            detailedInfoScreensList.forEach{ item ->
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
private fun DetailedInfoNavCtl(
    navController: NavHostController
) {
    AnimatedNavHost(navController = navController, startDestination = Screens.Info.route) {
        composable(
            route = Screens.Info.route
        ) {
            InfoScreen()
        }
        composable(
            route = Screens.History.route
        ) {
            HistoryScreen()
        }
    }
}

private fun getCurrentRoute(navController: NavController): String {
    return navController.currentBackStackEntry?.destination?.route ?: ""
}
