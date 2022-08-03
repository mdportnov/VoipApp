package ru.mephi.voip.ui.home.screens.catalog.items

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.get
import org.koin.androidx.compose.inject
import ru.mephi.shared.Stack
import ru.mephi.shared.data.model.UnitM
import ru.mephi.voip.ui.home.screens.catalog.CatalogViewModel
import ru.mephi.shared.vm.DetailedInfoViewModel

@Composable
internal fun CatalogList(
    items: Stack<UnitM>,
    openDetailedInfo: () -> Unit
) {
    val diVM: DetailedInfoViewModel = get()
    val viewModel: CatalogViewModel by inject()
    val catalogPageState by viewModel.catalogStateFlow.collectAsState()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {

    }
}

