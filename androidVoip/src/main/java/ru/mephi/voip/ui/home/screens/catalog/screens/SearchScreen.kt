@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package ru.mephi.voip.ui.home.screens.catalog.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.get
import ru.mephi.shared.vm.CatalogViewModel
import ru.mephi.shared.vm.SearchType
import ru.mephi.voip.ui.MasterActivity
import ru.mephi.voip.ui.common.CommonColor
import ru.mephi.voip.ui.home.screens.catalog.screens.common.items.SearchHistoryItem

@Composable
internal fun SearchScreen(
    runSearch: (String, SearchType) -> Unit,
    exitSearch: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(true) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    var searchStr by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf(SearchType.SEARCH_USER) }

    val applySearchStr = { newStr: String -> searchStr = newStr }
    val applySearchType = { newType: SearchType -> searchType = newType }

    val quitSearch = { focusRequester.freeFocus(); keyboardController?.hide() }

    Scaffold(
        topBar = {
            SearchTopBar(
                runSearch = { str, type ->
                    quitSearch()
                    runSearch(str, type)
                },
                exitSearch = {
                    quitSearch()
                    exitSearch()
                },
                searchStr = searchStr,
                applySearchStr = applySearchStr,
                searchType = searchType,
                applySearchType = applySearchType,
                focusRequester = focusRequester
            )
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            SearchHistory(
                runSearch = { str, type ->
                    quitSearch()
                    runSearch(str, type)
                },
                applySearchStr = applySearchStr,
                searchType = searchType
            )
        }
    }

    BackHandler(true) {
        quitSearch()
        exitSearch()
    }
}

@Composable
private fun SearchTopBar(
    runSearch: (String, SearchType) -> Unit,
    exitSearch: () -> Unit,
    searchStr: String,
    applySearchStr: (String) -> Unit,
    searchType: SearchType,
    applySearchType: (SearchType) -> Unit,
    focusRequester: FocusRequester,
) {
    val activity = LocalContext.current as MasterActivity
    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(onClick = { exitSearch() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
        },
        title = {
            Box(
                modifier = Modifier
                    .width((LocalConfiguration.current.screenWidthDp - 98).dp)
                    .padding(start = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchStr.isEmpty()) {
                    Text(
                        text = getSearchHint(searchType),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.outline)
                    )
                }
                BasicTextField(
                    value = searchStr,
                    onValueChange = { applySearchStr(it) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            runSearch(searchStr, searchType)
                        }
                    ),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            }
        },
        actions = {
            IconButton(onClick = {
                applySearchType(
                    when (searchType) {
                        SearchType.SEARCH_UNIT -> SearchType.SEARCH_USER
                        SearchType.SEARCH_USER -> SearchType.SEARCH_UNIT
                    }
                )
                if (searchStr.isNotEmpty()) {
                    Toast.makeText(activity, getSearchHint(searchType), Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(
                    imageVector = when (searchType) {
                        SearchType.SEARCH_USER -> Icons.Default.Person
                        SearchType.SEARCH_UNIT -> Icons.Default.Group
                    },
                    contentDescription = null
                )
            }
        }
    )
}

@Composable
private fun SearchHistory(
    runSearch: (String, SearchType) -> Unit,
    applySearchStr: (String) -> Unit,
    searchType: SearchType,
    cVM: CatalogViewModel = get()
) {
    val searchHistory = cVM.selectedSearchHistory.collectAsState()
    Column {
        Divider(
            color = CommonColor(),
            thickness = 0.8.dp,
            modifier = Modifier.fillMaxWidth()
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(items = searchHistory.value) { i, item ->
                if (i < 5) {
                    SearchHistoryItem(
                        searchStr = item,
                        searchType = searchType,
                        applySearchStr = applySearchStr,
                        runSearch = runSearch
                    )
                }
            }
        }
    }
}

private fun getSearchHint(searchType: SearchType): String {
    return when (searchType) {
        SearchType.SEARCH_USER -> "Поиск пользователей"
        SearchType.SEARCH_UNIT -> "Поиск подразделений"
    }
}