package ru.mephi.voip.ui.catalog.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject
import ru.mephi.shared.Stack
import ru.mephi.shared.data.model.UnitM
import ru.mephi.voip.ui.catalog.NewCatalogViewModel
import ru.mephi.voip.utils.ColorGray

@Composable
fun CatalogBreadcrumbs(items: Stack<UnitM>) {
    val viewModel: NewCatalogViewModel by inject()
    val catalogPageState by viewModel.catalogStateFlow.collectAsState()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        state = listState
    ) {
        if (catalogPageState > 0) {
            scope.launch {
                listState.animateScrollToItem(index = catalogPageState)
            }
            items(items = items, key = { it.code_str }) { stackItem ->
                RowWithIcon(
                    Modifier.padding(4.dp),
                    icon = Icons.Default.ChevronRight,
                    color = ColorGray,
                    title = stackItem.shortname.uppercase(),
                    onClick = {
                        viewModel.popFromCatalogTill(stackItem)
                    }
                )
            }
        }
    }
}