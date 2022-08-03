@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.home.screens.catalog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.get
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.vm.CatalogUtils
import ru.mephi.shared.vm.CatalogViewModel
import ru.mephi.shared.vm.SearchType
import ru.mephi.voip.ui.common.CommonColor
import ru.mephi.voip.ui.common.GroupTitle
import ru.mephi.voip.ui.home.screens.catalog.items.UnitCatalogItem
import ru.mephi.voip.ui.home.screens.catalog.items.UserCatalogItem


@Composable
fun CatalogScreen(
    codeStr: String,
    goNext: (nextCodeStr: String, shortname: String) -> Unit,
    goBack: () -> Unit,
    openDetailedInfo: (appointment: Appointment) -> Unit,
    runSearch: (searchStr: String, searchType: SearchType) -> Unit,
    cVM: CatalogViewModel = get()
) {
    val unitM = cVM.navigateUnitMap[codeStr]?.collectAsState() ?: remember { mutableStateOf(UnitM()) }
    var isSearchVisible by remember { mutableStateOf(false) }
    val switchSearchVisibility = { isEnter: Boolean ->
        if (isEnter) {
            cVM.currentSearchStr.value = ""
            cVM.currentSearchType.value = SearchType.SEARCH_USER
        }
        isSearchVisible = !isSearchVisible
    }
    Scaffold(
        topBar = {
            if (codeStr == CatalogUtils.INIT_CODE_STR) {
                CatalogTopBarInit(
                    isSearchVisible = isSearchVisible,
                    runSearch = runSearch,
                    switchSearchVisibility = switchSearchVisibility
                )
            } else {
                CatalogTopBarDefault(
                    title = unitM.value.shortname,
                    goBack = goBack,
                    isSearchVisible = isSearchVisible,
                    runSearch = runSearch,
                    switchSearchVisibility = switchSearchVisibility
                )
            }
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            if (!isSearchVisible) {
                CatalogList(unitM.value, goNext, openDetailedInfo)
            } else {
                SearchHistory(runSearch = runSearch)
            }
        }
    }
    BackHandler(enabled = true) {
        goBack()
    }
}

@Composable
private fun CatalogTopBarInit(
    isSearchVisible: Boolean,
    runSearch: (searchStr: String, searchType: SearchType) -> Unit,
    switchSearchVisibility: (isEnter: Boolean) -> Unit
) {
    if (!isSearchVisible) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .background(color = CommonColor(), shape = RoundedCornerShape(48.dp))
                .clip(RoundedCornerShape(48.dp))
                .height(48.dp)
                .fillMaxWidth()
                .clickable { switchSearchVisibility(true) }
                .padding(start = 4.dp)
        ) {
            IconButton(onClick = { switchSearchVisibility(true) }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            }
            Text(
                text = "Поиск",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .wrapContentHeight(),
                style = MaterialTheme.typography.bodyLarge.let {
                    it.copy(color = it.color.copy(alpha = 0.75f))
                }
            )
        }
    } else {
        SearchTopBar(switchSearchVisibility, runSearch)
    }
}

@Composable
private fun CatalogTopBarDefault(
    title: String,
    goBack: () -> Unit,
    isSearchVisible: Boolean,
    runSearch: (searchStr: String, searchType: SearchType) -> Unit,
    switchSearchVisibility: (isEnter: Boolean) -> Unit
) {
    if (!isSearchVisible) {
        CenterAlignedTopAppBar(
            navigationIcon = {
                IconButton(onClick = { goBack() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }
            },
            title = { Text(text = title.replaceFirstChar(Char::titlecase)) },
            actions = {
                IconButton(onClick = { switchSearchVisibility(true) }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                }
            }
        )
    } else {
        SearchTopBar(switchSearchVisibility, runSearch)
    }
}

@Composable
private fun CatalogList(
    unitM: UnitM,
    goNext: (codeStr: String, shortname: String) -> Unit,
    openDetailedInfo: (appointment: Appointment) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        unitM.appointments.let {
            if (it.isNotEmpty()) {
                item { GroupTitle(title = "Абоненты") }
            }
            itemsIndexed(items = it) { i, appointment ->
                UserCatalogItem(
                    appointment = appointment,
                    openDetailedInfo = openDetailedInfo,
                    isStart = i == 0,
                    isEnd = i + 1 == it.size
                )
            }
        }
        unitM.children.let {
            if (it.isNotEmpty()) {
                item { GroupTitle(title = "Подгруппы") }
            }
            itemsIndexed(items = it) { i, item ->
                UnitCatalogItem(
                    unit = item,
                    goNext = goNext,
                    isStart = i == 0,
                    isEnd = i + 1 == it.size
                )
            }
        }
    }
}
