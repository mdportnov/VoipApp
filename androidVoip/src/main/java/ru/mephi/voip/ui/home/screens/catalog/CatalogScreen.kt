@file:OptIn(ExperimentalAnimationApi::class)

package ru.mephi.voip.ui.home.screens.catalog

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.utils.pop
import ru.mephi.shared.vm.CatalogUtils
import ru.mephi.shared.vm.CatalogViewModel
import ru.mephi.shared.vm.DetailedInfoViewModel
import ru.mephi.voip.ui.home.Screens.Catalog
import ru.mephi.voip.ui.home.screens.catalog.screens.CatalogHomeScreen
import ru.mephi.voip.ui.home.screens.catalog.screens.CatalogNextScreen
import ru.mephi.voip.ui.home.screens.catalog.screens.SearchScreen


internal fun NavGraphBuilder.catalogNavCtl(
    navController: NavHostController,
    catalogVM: CatalogViewModel,
    detailedInfoVM: DetailedInfoViewModel
) {
    navigation(
        route = Catalog.route,
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
                    detailedInfoVM.loadDetailedInfo(appointment = app)
                },
                goNext = { unitM ->
                    catalogVM.navigateNext(unitM)
                    goNext(unitM, navController)
                }
            )
        }
        composable(
            route = Screens.CatalogNextScreen.route,
            arguments = listOf(navArgument("codeStr") { type = NavType.StringType }),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            val codeStr = it.arguments?.getString("codeStr") ?: CatalogUtils.INIT_CODE_STR
            CatalogNextScreen(
                codeStr = codeStr,
                openDetailedInfo = { app ->
                    detailedInfoVM.loadDetailedInfo(appointment = app)
                },
                goNext = { unitM ->
                    if (unitM.code_str != codeStr) {
                        catalogVM.navigateNext(unitM)
                        goNext(unitM, navController)
                    }
                },
                goBack = { count ->
                    for (i in 1..count) {
                        catalogVM.stack.pop()
                        navController.popBackStack()
                    }
                },
                openSearch = {
                    navController.navigate(route = Screens.SearchScreen.route) {
                        launchSingleTop = true
                    }
                }
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
                    detailedInfoVM.loadDetailedInfo(appointment = app)
                },
                goNext = { unitM ->
                    catalogVM.navigateNext(unitM)
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
            route = Screens.SearchScreen.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            SearchScreen(
                runSearch = { str, type ->
                    catalogVM.runSearch(str, type)
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
    navController.navigate(
        route = Screens.CatalogNextScreen.route.replace(
            oldValue = "{codeStr}",
            newValue = unitM.code_str
        )
    )
}
