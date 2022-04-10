package ru.mephi.voip.ui.catalog.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.inject
import ru.mephi.shared.data.model.SearchType
import ru.mephi.voip.R
import ru.mephi.voip.ui.catalog.CatalogViewModel
import ru.mephi.voip.ui.catalog.HistorySearchModelState
import ru.mephi.voip.utils.rememberFlowWithLifecycle

@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchTopAppBar() {
    val viewModel: CatalogViewModel by inject()
    val isSearchFieldVisible by viewModel.isSearchFieldVisible.collectAsState()
    val searchType by viewModel.searchType.collectAsState()
    val searchHistoryModelState by rememberFlowWithLifecycle(viewModel.searchHistoryModelState)
        .collectAsState(initial = HistorySearchModelState.Empty)

    TopAppBar(backgroundColor = Color.White) {
        AnimatedVisibility(
            visible = !isSearchFieldVisible, modifier = Modifier.fillMaxWidth(),
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { }) {
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
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            SearchView(
                searchText = searchHistoryModelState.searchText,
                placeholderText = stringResource(
                    if (searchType == SearchType.UNITS)
                        R.string.search_of_units else R.string.search_of_appointments
                ),
                onSearchTextChanged = {
                    viewModel.onSearchTextChanged(it)
                },
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
                },
                onFocused = { viewModel.retrieveSearchHistory() }
            )
        }
    }
}