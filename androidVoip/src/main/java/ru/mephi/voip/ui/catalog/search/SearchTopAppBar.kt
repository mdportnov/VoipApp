package ru.mephi.voip.ui.catalog.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.koin.androidx.compose.inject
import ru.mephi.voip.R
import ru.mephi.voip.ui.catalog.HistorySearchModelState
import ru.mephi.voip.ui.catalog.NewCatalogViewModel
import ru.mephi.voip.utils.rememberFlowWithLifecycle

@Composable
fun SearchTopAppBar(
    navController: NavController
) {
    val viewModel: NewCatalogViewModel by inject()
    val isSearchFieldVisible by viewModel.isSearchFieldVisible.collectAsState()
    val searchType by viewModel.searchType.collectAsState()
    val searchHistoryModelState by rememberFlowWithLifecycle(viewModel.searchHistoryModelState)
        .collectAsState(initial = HistorySearchModelState.Empty)

    TopAppBar(backgroundColor = Color.White) {
        AnimatedVisibility(visible = !isSearchFieldVisible, modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_mephi),
                        contentDescription = "лого",
                    )
                }
                Text(
                    text = "Каталог", style = TextStyle(color = Color.Black, fontSize = 20.sp),
                )
                IconButton(
                    modifier = Modifier,
                    onClick = { viewModel.isSearchFieldVisible.value = true }) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Поиск"
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = isSearchFieldVisible, modifier = Modifier.fillMaxWidth(),
            enter = expandHorizontally(),
            exit = shrinkHorizontally()
        ) {
            SearchBarUI(
                searchText = searchHistoryModelState.searchText,
                onSearchTextChanged = { viewModel.onSearchTextChanged(it) },
                onClearClick = {
                    viewModel.onClearClick()
                },
                searchType = searchType,
                onChangeSearchType = {
                    viewModel.changeSearchType()
                },
                onSubmit = {
                    viewModel.performSearch(searchHistoryModelState.searchText)
                    viewModel.onClearClick()
                }
            )
        }
    }
}