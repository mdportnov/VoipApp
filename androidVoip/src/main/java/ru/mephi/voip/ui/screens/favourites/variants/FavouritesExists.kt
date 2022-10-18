@file:OptIn(ExperimentalFoundationApi::class)

package ru.mephi.voip.ui.screens.favourites.variants

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.get
import ru.mephi.voip.ui.screens.favourites.cards.FavouriteCard
import ru.mephi.voip.vm.FavouritesViewModel

@Composable
internal fun FavouritesExists(
    favouritesVM: FavouritesViewModel = get()
) {
    CompositionLocalProvider(
        LocalOverscrollConfiguration provides null
    ) {
        val favouritesList by favouritesVM.favouritesList.collectAsState()
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 112.dp),
            contentPadding = PaddingValues(top = 6.dp, bottom = 82.dp, start = 6.dp, end = 6.dp)
        ) {
            items(favouritesList.size) { i ->
                FavouriteCard(favourite = favouritesList[i])
            }
        }
    }
}