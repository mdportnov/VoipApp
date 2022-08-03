@file:OptIn(ExperimentalAnimationApi::class)

package ru.mephi.voip.ui.home.screens.catalog

import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import org.koin.androidx.compose.get
import ru.mephi.shared.vm.CatalogUtils
import ru.mephi.shared.vm.CatalogViewModel
import ru.mephi.shared.vm.DetailedInfoViewModel
import ru.mephi.voip.ui.MasterActivity

@Composable
fun MasterCatalogScreen(
    cVM: CatalogViewModel = get(),
    diVM: DetailedInfoViewModel = get(),
    openDetailedInfo: () -> Unit
) {
    val navController = rememberAnimatedNavController()
    CatalogNavCtl(navController, cVM, diVM, openDetailedInfo)
}

@Composable
private fun CatalogNavCtl(
    navController: NavHostController,
    cVM: CatalogViewModel,
    diVM: DetailedInfoViewModel,
    openDetailedInfo: () -> Unit
) {
    val activity = LocalContext.current as MasterActivity
    AnimatedNavHost(
        navController = navController,
        startDestination = CatalogScreenUtils.DESTINATION_NAME
    ) {
        composable(
            route = CatalogScreenUtils.DESTINATION_NAME,
            arguments = listOf(navArgument("codeStr") { type = NavType.StringType })
        ) { backStackEntry ->
            val codeStr = backStackEntry.arguments?.getString("codeStr") ?: CatalogUtils.INIT_CODE_STR
            CatalogScreen(
                codeStr = codeStr,
                goNext = { nextCodeStr, shortname ->
                    cVM.navigateNext(nextCodeStr, shortname)
                    navController.navigate(
                        CatalogScreenUtils.DESTINATION_NAME.replace(
                            oldValue = "{codeStr}",
                            newValue = nextCodeStr
                        )
                    )
                },
                goBack = {
                    navController.popBackStack()
                    cVM.navigateBack()
                },
                openDetailedInfo = { appointment ->
                    diVM.loadDetailedInfo(appointment = appointment)
                    openDetailedInfo()
                },
                runSearch = { searchStr, searchType ->
                    if (searchStr.length <= 3) {
                        Toast.makeText(activity, "Поисковый запрос слишком короткий!", Toast.LENGTH_SHORT).show()
                    } else {
                        cVM.runSearch(searchStr, searchType)
                        navController.navigate(
                            CatalogScreenUtils.DESTINATION_NAME.replace(
                                oldValue = "{codeStr}",
                                newValue = CatalogUtils.getCodeStrBySearch(searchStr, searchType)
                            )
                        ) {
                            popUpTo(
                                route = CatalogScreenUtils.DESTINATION_NAME.replace(
                                    oldValue = "{codeStr}",
                                    newValue = CatalogUtils.INIT_CODE_STR
                                )
                            ) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}

object CatalogScreenUtils {
    const val DESTINATION_NAME = "catalog/{codeStr}"
}
