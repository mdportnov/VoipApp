package ru.mephi.voip.ui.catalog

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.koin.androidx.compose.inject
import ru.mephi.voip.ui.catalog.list.Breadcrumbs
import ru.mephi.voip.ui.catalog.list.CatalogList
import ru.mephi.voip.ui.catalog.search.SearchRecordsList
import ru.mephi.voip.ui.catalog.search.SearchTopAppBar
import ru.mephi.voip.utils.ColorAccent
import ru.mephi.voip.utils.rememberFlowWithLifecycle


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CatalogScreen(navController: NavController) {
    val viewModel: CatalogViewModel by inject()
    val items by viewModel.catalogStack.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isProgressBarVisible by viewModel.isProgressBarVisible.collectAsState()
    val activity = LocalContext.current as? Activity
    val searchHistoryModelState by rememberFlowWithLifecycle(viewModel.searchHistoryModelState)
        .collectAsState(initial = HistorySearchModelState.Empty)

    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(true) {
        viewModel.snackBarEvents.collect {
            when (it) {
                is CatalogViewModel.Event.ShowSnackBar -> {
                    scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                    scaffoldState.snackbarHostState.showSnackbar(it.text)
                }
                is CatalogViewModel.Event.DismissSnackBar -> {
                    scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                }
            }
        }
    }

    BackHandler {
        viewModel.goBack()
        if (viewModel.catalogStack.value.isEmpty())
            activity?.finish()
    }

    Scaffold(
        scaffoldState = scaffoldState, topBar = { SearchTopAppBar() },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = { viewModel.onRefresh() },
            ) {
                Column {
                    Breadcrumbs(items)
                    CatalogList(items, navController)
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isProgressBarVisible,
                modifier = Modifier.align(Alignment.Center)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(100.dp), color = ColorAccent,
                    strokeWidth = 10.dp
                )
            }

            if (searchHistoryModelState.historyRecords.isNotEmpty()) {
                Card(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(5.dp)
                ) {
                    Column {
                        Text(
                            "История:",
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 25.sp
                        )
                        SearchRecordsList(searchHistoryModelState) {
                            viewModel.onSearchTextChanged(it.name)
                            viewModel.performSearch(it.name)
                        }
                    }
                }
            }
        }
    }
}