@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.home.screens.catalog.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.koin.androidx.compose.get
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.vm.CatalogStatus
import ru.mephi.shared.vm.CatalogViewModel
import ru.mephi.voip.ui.home.screens.catalog.screens.common.CatalogList
import ru.mephi.voip.ui.home.screens.catalog.screens.common.CatalogView

@Composable
internal fun CatalogNextScreen(
    codeStr: String,
    openDetailedInfo: (appointment: Appointment) -> Unit,
    goNext: (UnitM) -> Unit,
    goBack: (String) -> Unit,
    openSearch: () -> Unit,
    cVM: CatalogViewModel = get()
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
                    openSearch = openSearch
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
    BackHandler(true) { goBack("") }
}

@Composable
private fun CatalogNextTopBar(
    title: String,
    goBack: (String) -> Unit,
    openSearch: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = { goBack("") }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            var expanded by remember { mutableStateOf(false) }
            var openHistory by remember { mutableStateOf(false) }
            IconButton(onClick = { expanded =  true }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Поиск") },
                    onClick = {
                        expanded = false
                        openSearch()
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null
                        )
                    })
                DropdownMenuItem(
                    text = { Text("Навигация") },
                    onClick = {
                        expanded = false
                        openHistory = true
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.List,
                            contentDescription = null
                        )
                    })
            }
            if (openHistory) {
                CatalogHistoryMenu(openHistory, { openHistory = false }, goBack)
            }
        }
    )
}

@Composable
private fun CatalogHistoryMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    goBack: (String) -> Unit,
    cVM: CatalogViewModel = get(),
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onDismiss() }
    ) {
        cVM.stack.forEachIndexed { i, item ->
            DropdownMenuItem(
                text = { Text(item.shortname) },
                onClick = {
                    onDismiss()
                    goBack(item.codeStr)
                },
                leadingIcon = {
                    Icon(
                        when {
                            i == 0 -> Icons.Default.Home
                            i + 1 == cVM.stack.size -> Icons.Default.Flag
                            else -> Icons.Default.North
                        },
                        contentDescription = null
                    )
                }
            )
        }
    }
}
