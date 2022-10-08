@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package ru.mephi.voip.ui.screens.catalog.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.get
import ru.mephi.shared.vm.CatalogViewModel
import ru.mephi.shared.vm.SearchType
import ru.mephi.voip.ui.MasterActivity
import ru.mephi.voip.ui.common.CommonColor
import ru.mephi.voip.ui.screens.catalog.screens.common.items.SearchHistoryItem

@Composable
internal fun SearchScreen(
    runSearch: (String, SearchType) -> Unit,
    exitSearch: () -> Unit,
    cVM: CatalogViewModel = get()
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(true) {
        cVM.updateSearchParams("", SearchType.SEARCH_USER)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    var searchStr by remember { mutableStateOf(TextFieldValue("")) }
    var searchType by remember { mutableStateOf(SearchType.SEARCH_USER) }

    val applySearchStr = { newStr: TextFieldValue ->
        searchStr = newStr; cVM.updateSearchParams(newStr.text, searchType)
    }
    val applySearchType = { newType: SearchType ->
        searchType = newType; cVM.updateSearchParams(searchStr.text, newType)
    }

    val quitSearch = { focusRequester.freeFocus(); keyboardController?.hide() }

    val context = LocalContext.current
    val notify = { Toast.makeText(context, "Поисковвый запрос слишком короткий",  Toast.LENGTH_SHORT).show() }

    Scaffold(
        topBar = {
            SearchTopBar(
                runSearch = { str, type ->
                    if (str.length <= 3) {
                        notify()
                    } else {
                        quitSearch()
                        runSearch(str, type)
                    }
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
                    if (str.length <= 3) {
                        notify()
                    } else {
                        quitSearch()
                        runSearch(str, type)
                    }
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
    searchStr: TextFieldValue,
    applySearchStr: (TextFieldValue) -> Unit,
    searchType: SearchType,
    applySearchType: (SearchType) -> Unit,
    focusRequester: FocusRequester,
) {
    val activity = LocalContext.current as MasterActivity
    SmallTopAppBar(
        navigationIcon = {
            IconButton(onClick = { exitSearch() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
        },
        title = {
            Box(
                modifier = Modifier.padding(start = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchStr.text.isEmpty()) {
                    Text(
                        text = when (searchType) {
                            SearchType.SEARCH_USER -> "Поиск пользователей"
                            SearchType.SEARCH_UNIT -> "Поиск подразделений"
                        },
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
                            runSearch(searchStr.text, searchType)
                        }
                    ),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
                )
            }
        },
        actions = {
            val onSearchTypeClick = {
                val msgHelper = when(searchType) {
                    SearchType.SEARCH_USER -> "Поиск подразделений"
                    SearchType.SEARCH_UNIT -> "Поиск пользователей"
                }
                applySearchType(
                    when (searchType) {
                        SearchType.SEARCH_UNIT -> SearchType.SEARCH_USER
                        SearchType.SEARCH_USER -> SearchType.SEARCH_UNIT
                    }
                )
                if (searchStr.text.isNotEmpty()) {
                    Toast.makeText(activity, msgHelper, Toast.LENGTH_SHORT).show()
                }
            }
            SearchTypeButton(
                isSelected = searchType == SearchType.SEARCH_USER,
                onClick = onSearchTypeClick,
                icon = if (searchType == SearchType.SEARCH_USER) Icons.Filled.Person else Icons.Outlined.Person
            )
            SearchTypeButton(
                isSelected = searchType == SearchType.SEARCH_UNIT,
                onClick = onSearchTypeClick,
                icon = if (searchType == SearchType.SEARCH_UNIT) Icons.Filled.Group else Icons.Outlined.Group
            )
        }
    )
}

@Composable
private fun SearchTypeButton(
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector
) {
    Box(modifier = Modifier.wrapContentSize()) {
        if (isSelected) {
            Surface(
                modifier = Modifier.align(Alignment.Center).size(40.dp),
                shape = CircleShape,
                tonalElevation = 4.dp
            ) { }
        }
        IconButton(
            onClick = { if (!isSelected) onClick() },
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        }
    }
}


@Composable
private fun SearchHistory(
    runSearch: (String, SearchType) -> Unit,
    applySearchStr: (TextFieldValue) -> Unit,
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
