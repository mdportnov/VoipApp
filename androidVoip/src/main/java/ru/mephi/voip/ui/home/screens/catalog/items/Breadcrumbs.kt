package ru.mephi.voip.ui.home.screens.catalog.items

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults.OutlinedBorderOpacity
import androidx.compose.material.ButtonDefaults.OutlinedBorderSize
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject
import ru.mephi.shared.Stack
import ru.mephi.shared.data.model.UnitM
import ru.mephi.voip.ui.home.screens.catalog.CatalogViewModel

@Composable
internal fun Breadcrumbs(items: Stack<UnitM>) {
    val viewModel: CatalogViewModel by inject()
    val catalogPageState by viewModel.catalogStateFlow.collectAsState()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        state = listState
    ) {
        if (catalogPageState > 0) {
            scope.launch {
                listState.animateScrollToItem(index = catalogPageState)
            }
            itemsIndexed(items  = items) { i, item ->
                BreadcrumbElement(
                    title = item.shortname.uppercase(),
                    isStart = i == 0,
                    isEnd = i + 1 == items.size
                ) {
                    viewModel.popFromCatalogTill(item)
                }
            }
        }
    }
}

@Composable
private fun BreadcrumbElement(
    title: String,
    isStart: Boolean = false,
    isEnd: Boolean = false,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.padding(
        start = (if (isStart) 6 else 0).dp,
        end = (if (isEnd) 6 else 0).dp
    )) {
        Row(
            modifier = Modifier.wrapContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isStart) {
                Icon(imageVector = Icons.Default.NavigateNext, contentDescription = null)
            }
            Box(
                modifier = Modifier
                    .border(
                        width = OutlinedBorderSize,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = OutlinedBorderOpacity),
                        shape = RoundedCornerShape(5.dp)
                    )
                    .clip(RoundedCornerShape(5.dp))
                    .clickable { onClick() }
                    .padding(start = 10.dp, end = 10.dp)
                    .defaultMinSize(minHeight = 32.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = title,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}