@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.catalog.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.mephi.shared.appContext
import ru.mephi.shared.data.model.UnitM
import ru.mephi.voip.ui.catalog.CatalogViewModel
import ru.mephi.voip.utils.isOnline

@Composable
internal fun UnitCatalogItem(
    record: UnitM,
    viewModel: CatalogViewModel,
    isStart: Boolean = false,
    isEnd: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(
            topStart = (if (isStart) 8 else 0).dp,
            topEnd = (if (isStart) 8 else 0).dp,
            bottomStart = (if (isEnd) 8 else 0).dp,
            bottomEnd = (if (isEnd) 8 else 0).dp
        ),
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 0.5.dp, bottom = 0.5.dp)
            .clickable {
                if (isOnline(appContext)) {
                    viewModel.goNext(record.code_str)
                } else {
                    if (viewModel.isExistsInDatabase(record.code_str)) {
                        viewModel.goNext(record.code_str)
                    }
                }
            }
    ) {
        Text(
            text = record.fullname,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
        )
    }
}
