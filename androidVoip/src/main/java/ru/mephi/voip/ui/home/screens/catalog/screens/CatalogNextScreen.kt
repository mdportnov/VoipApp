@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.home.screens.catalog.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults.OutlinedBorderOpacity
import androidx.compose.material.ButtonDefaults.OutlinedBorderSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.vm.CatalogStatus
import ru.mephi.shared.vm.CatalogViewModel
import ru.mephi.shared.vm.StackUnitM
import ru.mephi.voip.ui.home.screens.catalog.screens.common.CatalogList
import ru.mephi.voip.ui.home.screens.catalog.screens.common.CatalogView

@Composable
internal fun CatalogNextScreen(
    codeStr: String,
    openDetailedInfo: (appointment: Appointment) -> Unit,
    goNext: (UnitM) -> Unit,
    goBack: (Int) -> Unit,
    openSearch: () -> Unit,
    cVM: CatalogViewModel = get(),
    breadCrumbsState: LazyListState
) {
    val unitM = cVM.navigateUnitMap[codeStr]?.unitM?.collectAsState()
    val status = cVM.navigateUnitMap[codeStr]?.status?.collectAsState()
    SwipeRefresh(
        state = rememberSwipeRefreshState(false),
        onRefresh = {
            if (unitM != null) {
                cVM.navigateNext(unitM.value)
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                CatalogNextTopBar(
                    title = unitM?.value?.shortname ?: "",
                    goBack = goBack,
                    openSearch = openSearch,
                    breadCrumbsState = breadCrumbsState
                )
            }
        ) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                CatalogView(
                    codeStr = codeStr,
                    status = status?.value ?: CatalogStatus.OK
                ) {
                    CatalogList(
                        unitM = unitM?.value ?: UnitM(code_str = codeStr),
                        openDetailedInfo = openDetailedInfo,
                        goNext = goNext
                    )
                }
            }
        }
    }
    BackHandler(true) { goBack(1) }
}

@Composable
private fun CatalogNextTopBar(
    title: String,
    goBack: (Int) -> Unit,
    openSearch: () -> Unit,
    breadCrumbsState: LazyListState
) {
    Column {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = { goBack(1) }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }
            },
            actions = {
                IconButton(onClick = { openSearch() }) {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            }
        )
        CatalogBreadCrumbs(goBack, breadCrumbsState)
    }
}

@Composable
private fun CatalogBreadCrumbs(
    goBack: (Int) -> Unit,
    breadCrumbsState: LazyListState,
    cVM: CatalogViewModel = get()
) {
    val scope = rememberCoroutineScope()
    val stack = cVM.stack
    LaunchedEffect(true) {
        breadCrumbsState.animateScrollToItem(index = stack.size - 1)
    }
    LazyRow(
        modifier = Modifier.padding(bottom = 4.dp),
        state = breadCrumbsState
    ) {
        item { BreadCrumbsPadding() }
        itemsIndexed(items = stack) { i, item ->
            BreadCrumbsItem(
                onClick = {
                    scope.launch {
                        breadCrumbsState.animateScrollToItem(index = i + 1)
                        goBack(stack.size - 1 - i)
                    }
                },
                icon = when (i) {
                    0 -> Icons.Outlined.Home
                    else -> null
                },
                isStart = i == 0,
                item = item
            )
        }
        item { BreadCrumbsPadding() }
    }
}

@Composable
private fun BreadCrumbsPadding() {
    Divider(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .width(8.dp),
        color = Color.Transparent
    )
}

@Composable
private fun BreadCrumbsItem(
    onClick: () -> Unit,
    icon: ImageVector?,
    isStart: Boolean = false,
    item: StackUnitM
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        if (!isStart) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.padding(end = 2.dp)
            )
        }
        Row(
            modifier = Modifier
                .border(
                    width = OutlinedBorderSize,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = OutlinedBorderOpacity),
                    shape = RoundedCornerShape(5.dp)
                )
                .clip(RoundedCornerShape(5.dp))
                .clickable { onClick() }
                .padding(horizontal = 6.dp)
                .defaultMinSize(minHeight = 32.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            Text(
                text = item.shortname,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
