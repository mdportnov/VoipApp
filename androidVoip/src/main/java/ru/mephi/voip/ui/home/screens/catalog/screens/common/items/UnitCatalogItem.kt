@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.home.screens.catalog.screens.common.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ru.mephi.shared.data.model.UnitM

@Composable
internal fun UnitCatalogItem(
    unitM: UnitM,
    goNext: (UnitM) -> Unit,
    isStart: Boolean = false,
    isEnd: Boolean = false
) {
    val cardShape = RoundedCornerShape(
        topStart = (if (isStart) 8 else 0).dp,
        topEnd = (if (isStart) 8 else 0).dp,
        bottomStart = (if (isEnd) 8 else 0).dp,
        bottomEnd = (if (isEnd) 8 else 0).dp
    )
    Card(
        shape = cardShape,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(top = 0.5.dp, bottom = 0.5.dp)
            .clip(cardShape)
            .clickable { goNext(unitM) },
        elevation = CardDefaults.elevatedCardElevation(),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Text(
            text = unitM.fullname,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
        )
    }
}
