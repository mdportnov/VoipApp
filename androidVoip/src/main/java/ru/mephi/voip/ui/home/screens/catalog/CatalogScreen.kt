@file:OptIn(ExperimentalAnimationApi::class)

package ru.mephi.voip.ui.home.screens.catalog


import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import org.koin.androidx.compose.get
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.vm.CatalogUtils
import ru.mephi.shared.vm.CatalogViewModel
import ru.mephi.shared.vm.DetailedInfoViewModel
import ru.mephi.voip.ui.home.screens.catalog.screens.CatalogHomeScreen
import ru.mephi.voip.ui.home.screens.catalog.screens.CatalogNextScreen
import ru.mephi.voip.ui.home.screens.catalog.screens.SearchScreen

@Composable
fun CatalogScreen(
    openDetailedInfo: () -> Unit,
) {
    val navController = rememberAnimatedNavController()
    Box {
        CatalogNavCtl(navController, openDetailedInfo)
    }
}

@Composable
private fun CatalogNavCtl(
    navController: NavHostController,
    openDetailedInfo: () -> Unit,
    cVM: CatalogViewModel = get(),
    diVM: DetailedInfoViewModel = get()
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = Screens.CatalogHomeScreen.route
    ) {
        composable(
            route = Screens.CatalogHomeScreen.route
        ) {
            CatalogHomeScreen(
                openSearch = {
                    navController.navigate(route = Screens.SearchScreen.route) {
                        launchSingleTop = true
                    }
                },
                openDetailedInfo = { app ->
                    diVM.loadDetailedInfo(appointment = app)
                    openDetailedInfo()
                },
                goNext = { unitM ->
                    cVM.navigateNext(unitM)
                    goNext(unitM, navController)
                }
            )
        }
        composable(
            route = Screens.CatalogNextScreen.route,
            arguments = listOf(navArgument("codeStr") { type = NavType.StringType })
        ) { backStackEntry ->
            val codeStr = backStackEntry.arguments?.getString("codeStr") ?: CatalogUtils.INIT_CODE_STR
            CatalogNextScreen(
                codeStr = codeStr,
                openDetailedInfo = { app ->
                    diVM.loadDetailedInfo(appointment = app)
                    openDetailedInfo()
                },
                goNext = { unitM ->
                    cVM.navigateNext(unitM)
                    goNext(unitM, navController)
                },
                goBack = { navController.popBackStack() },
                openSearch = {
                    navController.navigate(route = Screens.SearchScreen.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = Screens.CatalogSearchScreen.route
        ) {
            CatalogNextScreen(
                codeStr = "Search",
                openDetailedInfo = { app ->
                    diVM.loadDetailedInfo(appointment = app)
                    openDetailedInfo()
                },
                goNext = { unitM ->
                    cVM.navigateNext(unitM)
                    goNext(unitM, navController)
                },
                goBack = { navController.popBackStack() },
                openSearch = {
                    navController.navigate(route = Screens.SearchScreen.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = Screens.SearchScreen.route
        ) {
            SearchScreen(
                runSearch = { str, type ->
                    cVM.runSearch(str, type)
                    navController.navigate(route = Screens.CatalogSearchScreen.route) {
                        launchSingleTop = true
                        popUpTo(route = Screens.CatalogHomeScreen.route)
                    }
                },
                exitSearch = { navController.popBackStack() }
            )
        }
    }
}

private fun goNext(
    unitM: UnitM,
    navController: NavHostController,
) {
    navController.navigate(route = Screens.CatalogNextScreen.route.replace(
        oldValue = "{codeStr}",
        newValue = unitM.code_str
    ))
}
