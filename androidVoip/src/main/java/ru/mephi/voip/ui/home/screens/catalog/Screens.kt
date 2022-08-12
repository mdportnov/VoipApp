package ru.mephi.voip.ui.home.screens.catalog

sealed class Screens(
    val route: String
) {
    object CatalogHomeScreen: Screens("catalog_home_screen")
    object CatalogNextScreen: Screens("catalog_next_screen/{codeStr}")
    object CatalogSearchScreen: Screens("catalog_search_screen")
    object SearchScreen: Screens("search_screen")
}