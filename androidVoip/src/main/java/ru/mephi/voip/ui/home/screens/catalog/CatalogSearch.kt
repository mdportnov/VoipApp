@file:OptIn(ExperimentalComposeUiApi::class)

package ru.mephi.voip.ui.home.screens.catalog

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import ru.mephi.voip.ui.theme.md_theme_light_background

@Composable
internal fun SearchTopBar(
    switchSearchVisibility: (isEnter: Boolean) -> Unit,
    runSearch: (searchStr: String, searchType: SearchType) -> Unit,
    cVM: CatalogViewModel = get()
) {
    val activity = LocalContext.current as MasterActivity
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(true) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    val searchStr = cVM.currentSearchStr.collectAsState()
    val searchType = cVM.currentSearchType.collectAsState()
    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(onClick = { switchSearchVisibility(false) }) {
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
                if (searchStr.value.isEmpty()) {
                    Text(
                        text = getSearchHint(searchType.value),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge.let {
                            it.copy(color = it.color.copy(alpha = 0.75f))
                        }
                    )
                }
                BasicTextField(
                    value = searchStr.value,
                    onValueChange = {
                        cVM.currentSearchStr.value = it
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                            runSearch(searchStr.value, searchType.value)
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
                cVM.currentSearchType.value = when (searchType.value) {
                    SearchType.SEARCH_UNIT -> SearchType.SEARCH_USER
                    SearchType.SEARCH_USER -> SearchType.SEARCH_UNIT
                }
                if (searchStr.value.isNotEmpty()) {
                    Toast.makeText(activity, getSearchHint(searchType.value), Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(
                    imageVector = when (searchType.value) {
                        SearchType.SEARCH_USER -> Icons.Default.Person
                        SearchType.SEARCH_UNIT -> Icons.Default.Group
                    },
                    contentDescription = null
                )
            }
        }
    )
    BackHandler(enabled = true) {
        switchSearchVisibility(false)
    }
}

@Composable
internal fun SearchHistory(
    cVM: CatalogViewModel = get(),
    runSearch: (searchStr: String, searchType: SearchType) -> Unit
) {
    val searchHistory = cVM.selectedSearchHistory.collectAsState()
    Column {
        Divider(
            color = CommonColor(),
            thickness = 0.8.dp,
            modifier = Modifier.fillMaxWidth()
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(color = md_theme_light_background)
        ) {
            itemsIndexed(items = searchHistory.value) { i, item ->
                if (i < 5) {
                    SearchHistoryItem(
                        searchStr = item,
                        applyStr = { searchStr ->
                            cVM.currentSearchStr.value = searchStr
                        },
                        runSearch = { searchStr ->
                            runSearch(searchStr, cVM.currentSearchType.value)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchHistoryItem(
    searchStr: String,
    applyStr: (searchStr: String) -> Unit,
    runSearch: (searchStr: String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clickable { runSearch(searchStr) }
    ) {
        IconButton(onClick = { runSearch(searchStr) }) {
            Icon(imageVector = Icons.Default.History, contentDescription = null)
        }
        Text(
            text = searchStr,
            modifier = Modifier
                .wrapContentHeight()
                .width((LocalConfiguration.current.screenWidthDp - 128).dp),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = { applyStr(searchStr) }) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null)
        }
    }
}

private fun getSearchHint(searchType: SearchType): String {
    return when (searchType) {
        SearchType.SEARCH_USER -> "Поиск пользователей"
        SearchType.SEARCH_UNIT -> "Поиск подразделений"
    }
}

