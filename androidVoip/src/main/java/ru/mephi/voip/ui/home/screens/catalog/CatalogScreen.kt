@file:OptIn(ExperimentalAnimationApi::class)

package ru.mephi.voip.ui.home.screens.catalog


import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import org.koin.androidx.compose.get
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.utils.pop
import ru.mephi.shared.vm.CatalogUtils
import ru.mephi.shared.vm.CatalogViewModel
import ru.mephi.shared.vm.DetailedInfoViewModel
import ru.mephi.voip.ui.home.screens.catalog.screens.CatalogHomeScreen
import ru.mephi.voip.ui.home.screens.catalog.screens.CatalogNextScreen
import ru.mephi.voip.ui.home.screens.catalog.screens.SearchScreen

@Composable
fun CatalogScreen() {
    val navController = rememberAnimatedNavController()
    val breadCrumbsState = rememberLazyListState()
    Box {
        CatalogNavCtl(navController, breadCrumbsState)
    }
}

@Composable
private fun CatalogNavCtl(
    navController: NavHostController,
    breadCrumbsState: LazyListState,
    cVM: CatalogViewModel = get(),
    diVM: DetailedInfoViewModel = get()
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = Screens.CatalogHomeScreen.route
    ) {
        composable(
            route = Screens.CatalogHomeScreen.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            CatalogHomeScreen(
                openSearch = {
                    navController.navigate(route = Screens.SearchScreen.route) {
                        launchSingleTop = true
                    }
                },
                openDetailedInfo = { app ->
                    diVM.loadDetailedInfo(appointment = app)
                },
                goNext = { unitM ->
                    cVM.navigateNext(unitM)
                    goNext(unitM, navController)
                }
            )
        }
        composable(
            route = Screens.CatalogNextScreen.route,
            arguments = listOf(navArgument("codeStr") { type = NavType.StringType }),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) { backStackEntry ->
            val codeStr = backStackEntry.arguments?.getString("codeStr") ?: CatalogUtils.INIT_CODE_STR
            CatalogNextScreen(
                codeStr = codeStr,
                openDetailedInfo = { app ->
                    diVM.loadDetailedInfo(appointment = app)
                },
                goNext = { unitM ->
                    if (unitM.code_str != codeStr) {
                        cVM.navigateNext(unitM)
                        goNext(unitM, navController)
                    }
                },
                goBack = {
                    for (i in 1..it) {
                        cVM.stack.pop()
                        navController.popBackStack()
                    }
                },
                openSearch = {
                    navController.navigate(route = Screens.SearchScreen.route) {
                        launchSingleTop = true
                    }
                },
                breadCrumbsState = breadCrumbsState
            )
        }
        composable(
            route = Screens.CatalogSearchScreen.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            CatalogNextScreen(
                codeStr = "Search",
                openDetailedInfo = { app ->
                    diVM.loadDetailedInfo(appointment = app)
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
                },
                breadCrumbsState = breadCrumbsState
            )
        }
        composable(
            route = Screens.SearchScreen.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
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
