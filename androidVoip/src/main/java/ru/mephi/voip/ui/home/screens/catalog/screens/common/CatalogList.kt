package ru.mephi.voip.ui.home.screens.catalog.screens.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.model.UnitM
import ru.mephi.voip.ui.common.GroupTitle
import ru.mephi.voip.ui.home.screens.catalog.screens.common.items.UnitCatalogItem
import ru.mephi.voip.ui.home.screens.catalog.screens.common.items.UserCatalogItem


@Composable
internal fun CatalogList(
    unitM: UnitM,
    openDetailedInfo: (appointment: Appointment) -> Unit,
    goNext: (UnitM) -> Unit
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
                if (unitM.appointments.isNotEmpty()) {
                    item { CatalogListDivider() }
                }
                item { GroupTitle(title = "Подгруппы") }
            }
            itemsIndexed(items = it) { i, item ->
                UnitCatalogItem(
                    unitM = item,
                    goNext = goNext,
                    isStart = i == 0,
                    isEnd = i + 1 == it.size
                )
            }
        }
        item { CatalogListDivider() }
    }
}


@Composable
private fun CatalogListDivider() {
    Divider(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
    )
}