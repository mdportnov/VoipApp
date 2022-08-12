@file:OptIn(ExperimentalAnimationApi::class)

package ru.mephi.voip.ui.login

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ru.mephi.voip.ui.MasterActivity
import ru.mephi.voip.ui.login.screens.AskPermissionsScreen
import ru.mephi.voip.ui.login.screens.CredentialsInputScreen
import ru.mephi.voip.ui.login.screens.HelloScreen

@Composable
internal fun LoginScreen(
    skipInit: () -> Unit,
    isInit: Boolean
) {
    rememberSystemUiController().setSystemBarsColor(MaterialTheme.colorScheme.background)

    val navController = rememberAnimatedNavController()

    LoginNavCtl(
        navController = navController,
        skipInit = skipInit,
        isInit = isInit
    )
}

@Composable
private fun LoginNavCtl(
    navController: NavHostController,
    skipInit: () -> Unit,
    isInit: Boolean
) {
    val activity = LocalContext.current as MasterActivity
    AnimatedNavHost(
        navController = navController,
        startDestination = (
                if (isInit) {
                    Screens.HelloScreen
                } else {
                    if (activity.isPermissionsGranted) Screens.CredentialsInputScreen else Screens.AskPermissionsScreen
                }).route
    ) {
        composable(
            route = Screens.HelloScreen.route
        ) {
            HelloScreen(
                skipInit = skipInit,
                goNext = { navController.navigate(route = Screens.AskPermissionsScreen.route) }
            )
        }
        composable(
            route = Screens.AskPermissionsScreen.route
        ) {
            AskPermissionsScreen(
                goBack = { navController.popBackStack() },
                goNext = { navController.navigate(route = Screens.AskPermissionsScreen.route) }
            )
        }
        composable(
            route = Screens.CredentialsInputScreen.route
        ) {
            CredentialsInputScreen(
                skipInit = skipInit,
                goBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun Background() {
    Surface() {
        
    }
}