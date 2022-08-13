@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.home.screens.catalog.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.koin.androidx.compose.get
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.vm.CatalogStatus
import ru.mephi.shared.vm.CatalogViewModel
import ru.mephi.voip.ui.home.screens.catalog.screens.common.CatalogList
import ru.mephi.voip.ui.home.screens.catalog.screens.common.CatalogView
import timber.log.Timber

@Composable
internal fun CatalogNextScreen(
    codeStr: String,
    openDetailedInfo: (appointment: Appointment) -> Unit,
    goNext: (UnitM) -> Unit,
    goBack: () -> Unit,
    openSearch: () -> Unit,
    cVM: CatalogViewModel = get()
) {
    val unitM = cVM.navigateUnitMap[codeStr]?.unitM?.collectAsState()
    val status = cVM.navigateUnitMap[codeStr]?.status?.collectAsState()
    Timber.e("${status?.value}")
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
}

@Composable
private fun CatalogNextTopBar(
    title: String,
    goBack: () -> Unit,
    openSearch: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = { goBack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = { openSearch() }) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            }
        }
    )
}