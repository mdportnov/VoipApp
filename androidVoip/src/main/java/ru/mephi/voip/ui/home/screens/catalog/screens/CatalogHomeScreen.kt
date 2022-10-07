@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.home.screens.catalog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.koin.androidx.compose.get
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.vm.CatalogStatus
import ru.mephi.shared.vm.CatalogUtils
import ru.mephi.shared.vm.CatalogViewModel
import ru.mephi.voip.ui.common.CommonColor
import ru.mephi.voip.ui.home.screens.catalog.screens.common.CatalogList
import ru.mephi.voip.ui.home.screens.catalog.screens.common.CatalogView

@Composable
internal fun CatalogHomeScreen(
    openSearch: () -> Unit,
    openDetailedInfo: (appointment: Appointment) -> Unit,
    goNext: (UnitM) -> Unit,
    cVM: CatalogViewModel = get()
) {
    LaunchedEffect(true) {
        cVM.goHome()
    }
    val unitM = cVM.navigateUnitMap[CatalogUtils.INIT_CODE_STR]?.unitM?.collectAsState()
    val status = cVM.navigateUnitMap[CatalogUtils.INIT_CODE_STR]?.status?.collectAsState()
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
            topBar = { CatalogHomeTopBar(openSearch) },
        ) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                CatalogView(
                    codeStr = CatalogUtils.INIT_CODE_STR,
                    status = status?.value ?: CatalogStatus.OK
                ) {
                    CatalogList(
                        unitM = unitM?.value ?: UnitM(code_str = CatalogUtils.INIT_CODE_STR),
                        openDetailedInfo = openDetailedInfo,
                        goNext = goNext
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogHomeTopBar(
    openSearch: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 5.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(48.dp))
            .height(48.dp)
            .fillMaxWidth()
            .clickable { openSearch() }
            .padding(start = 4.dp)
    ) {
        Row {
            IconButton(onClick = { openSearch() }) {
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
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.outline)
            )
        }
    }
}