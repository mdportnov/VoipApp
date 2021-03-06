package ru.mephi.voip.ui.catalog.list

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.DismissDirection.EndToStart
import androidx.compose.material.DismissDirection.StartToEnd
import androidx.compose.material.DismissValue.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject
import ru.mephi.shared.Stack
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.model.CatalogItem
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.R
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.call.CallActivity
import ru.mephi.voip.ui.catalog.CatalogViewModel
import ru.mephi.voip.ui.components.ExpandableCard
import ru.mephi.voip.ui.components.ExpandableContent

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CatalogList(items: Stack<UnitM>, navController: NavController) {
    val viewModel: CatalogViewModel by inject()
    val accountStatusRepository: AccountStatusRepository by inject()
    val catalogPageState by viewModel.catalogStateFlow.collectAsState()

    val context = LocalContext.current

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val currentItems = mutableListOf<CatalogItem>()

    val onSwipeToCall: (Appointment) -> Unit = { record ->
        if (accountStatusRepository.status.value == AccountStatus.REGISTERED && !record.line.isNullOrEmpty()) {
            CallActivity.create(context, record.line!!, false)
        } else {
            Toast.makeText(
                context,
                "?????? ?????????????????? ???????????????? ?????? ???????????????????? ????????????",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val onSwipeToFavourite: (Appointment) -> Unit = { record ->
        Toast.makeText(context, viewModel.addToFavourites(record).text, Toast.LENGTH_SHORT).show()
    }

    val expandedCardIds = viewModel.expandedCardIdsList.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (catalogPageState > 0) {
            currentItems.clear()
            items[catalogPageState - 1].appointments?.let {
                currentItems.addAll(it)
            }

            items[catalogPageState - 1].children?.let {
                currentItems.addAll(it)
            }
        }
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            if (catalogPageState > 0) {
                scope.launch {
                    listState.animateScrollToItem(index = 0)
                }
            }
            items(items = currentItems, key = { it.toString() }) { recordItem ->
                when (recordItem) {
                    is Appointment -> {
                        val dismissState = rememberDismissState(
                            confirmStateChange = {
                                if (it == DismissedToEnd)
                                    onSwipeToFavourite(recordItem)
                                if (it == DismissedToStart)
                                    onSwipeToCall(recordItem)
                                false
                            }
                        )
                        SwipeToDismiss(
                            state = dismissState,
                            modifier = Modifier.padding(vertical = 2.dp),
                            directions = setOf(EndToStart, StartToEnd),
                            dismissThresholds = { FractionalThreshold(0.4f) },
                            background = {
                                val direction =
                                    dismissState.dismissDirection ?: return@SwipeToDismiss
                                val color by animateColorAsState(
                                    when (dismissState.targetValue) {
                                        Default -> Color.LightGray
                                        DismissedToStart -> colorResource(id = R.color.colorGreen)
                                        DismissedToEnd -> Color.Yellow
                                    }
                                )
                                val alignment = when (direction) {
                                    StartToEnd -> Alignment.CenterStart
                                    EndToStart -> Alignment.CenterEnd
                                }
                                val icon = when (direction) {
                                    StartToEnd -> Icons.Outlined.Star
                                    EndToStart -> Icons.Default.Call
                                }
                                val scale by animateFloatAsState(if (dismissState.targetValue == Default) 0.75f else 1f)
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = alignment
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = "Localized description",
                                        modifier = Modifier.scale(scale)
                                    )
                                }
                            },
                            dismissContent = {
                                ExpandableCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onCardArrowClick = {
                                        viewModel.onCardArrowClicked(
                                            currentItems.indexOf(recordItem)
                                        )
                                    },
                                    expanded = expandedCardIds.value.contains(
                                        currentItems.indexOf(recordItem)
                                    ),
                                    content = {
                                        UserCatalogItem(
                                            modifier = it,
                                            record = recordItem,
                                            viewModel = viewModel,
                                            navController = navController
                                        )
                                    },
                                    expandableContent = {
                                        ExpandableContent(content = {
                                            UserCatalogMoreInfo(record = recordItem)
                                        })
                                    }
                                )
                            },
                        )
                    }
                    is UnitM -> {
                        Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f))
                        UnitCatalogItem(record = recordItem, viewModel = viewModel)
                    }
                }
            }
        }
    }
}