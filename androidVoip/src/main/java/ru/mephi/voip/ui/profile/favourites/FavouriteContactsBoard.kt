package ru.mephi.voip.ui.profile.favourites

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mephi.voip.R
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.profile.ProfileViewModel


@Composable
fun FavouriteContactsBoard(
    modifier: Modifier,
    profileViewModel: ProfileViewModel,
    accountStatusRepository: AccountStatusRepository
) {
    val items by profileViewModel.recordsFlow.collectAsState(initial = emptyList())
    val itemIdUp by profileViewModel.expandedMenuId.collectAsState()

    if (items.isNotEmpty())
        Text(
            text = "Избранные контакты",
            style = TextStyle(color = Color.Black, fontWeight = FontWeight.SemiBold),
            fontSize = with(LocalDensity.current) {
                (dimensionResource(id = R.dimen.profile_text_size).value.sp / fontScale)
            },
        )

    LazyVerticalGrid(
        modifier = modifier
            .height(300.dp)
            .padding(top = 10.dp),
        columns = GridCells.Adaptive(100.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        itemsIndexed(items) { index, item ->
            FavouriteItem(
                favouriteRecord = item,
                expanded = itemIdUp == index,
                changeExpandedState = { isExpanded ->
                    if (isExpanded)
                        profileViewModel.onFavouriteClicked(index)
                    else
                        profileViewModel.onFavouriteClicked(-1)
                },
                viewModel = profileViewModel,
                accountStatusRepository = accountStatusRepository
            )
        }
    }
}