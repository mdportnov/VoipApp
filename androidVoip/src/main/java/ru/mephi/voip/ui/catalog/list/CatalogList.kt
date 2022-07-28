package ru.mephi.voip.ui.catalog.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject
import ru.mephi.shared.Stack
import ru.mephi.shared.data.model.UnitM
import ru.mephi.voip.ui.catalog.CatalogViewModel

@Composable
internal fun CatalogList(items: Stack<UnitM>, navController: NavController) {
    val viewModel: CatalogViewModel by inject()
    val catalogPageState by viewModel.catalogStateFlow.collectAsState()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            if (catalogPageState == 0) return@LazyColumn
            if (catalogPageState > 0) {
                scope.launch {
                    listState.animateScrollToItem(index = 0)
                }
            }
            items[catalogPageState - 1].appointments?.let {
                if (it.isNotEmpty()) {
                    item { GroupTitle(title = "Абоненты") }
                }
                itemsIndexed(items = it) { i, item ->
                    UserCatalogItem(
                        record = item,
                        navController = navController,
                        isStart = i == 0,
                        isEnd = i + 1 == it.size
                    )
                }
            }
            items[catalogPageState - 1].children?.let {
                if (it.isNotEmpty()) {
                    item { GroupTitle(title = "Подгруппы") }
                }
                itemsIndexed(items = it) { i, item ->
                    UnitCatalogItem(
                        record = item,
                        viewModel = viewModel,
                        isStart = i == 0,
                        isEnd = i + 1 == it.size
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupTitle(
    title: String
) {
    Text(
        modifier = Modifier.padding(top = 2.dp, bottom = 6.dp, start = 8.dp),
        text = title,
        style = MaterialTheme.typography.titleMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}