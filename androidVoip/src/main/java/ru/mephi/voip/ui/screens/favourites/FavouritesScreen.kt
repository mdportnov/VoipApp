@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package ru.mephi.voip.ui.screens.favourites

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.koin.androidx.compose.get
import ru.mephi.voip.R
import ru.mephi.voip.entities.status.FavouritesStatus
import ru.mephi.voip.ui.common.scaffold.DialerFAB
import ru.mephi.voip.ui.common.scaffold.TopBarWithStatus
import ru.mephi.voip.ui.screens.favourites.variants.FavouritesEmpty
import ru.mephi.voip.ui.screens.favourites.variants.FavouritesExists
import ru.mephi.voip.ui.screens.favourites.variants.FavouritesLoading
import ru.mephi.voip.vm.FavouritesViewModel

@Composable
internal fun FavouritesScreen(
    openDialPad: () -> Unit
) {
    Scaffold(
        topBar = { TopBarWithStatus(title = stringResource(R.string.bottom_bar_title_favourites)) },
        floatingActionButton = { DialerFAB(openDialPad = openDialPad) },
        contentWindowInsets = MutableWindowInsets()
    ) {
        FavouritesContent(paddingValues = it)
    }
}

@Composable
private  fun FavouritesContent(
    paddingValues: PaddingValues,
    favouritesVM: FavouritesViewModel = get()
) {
    Box(modifier = Modifier.padding(paddingValues)) {
        val favouritesStatus by favouritesVM.favouritesStatus.collectAsState()
        when(favouritesStatus) {
            FavouritesStatus.EMPTY -> FavouritesEmpty()
            FavouritesStatus.EXISTS -> FavouritesExists(favouritesVM = favouritesVM)
            FavouritesStatus.LOADING -> FavouritesLoading()
        }
    }
}