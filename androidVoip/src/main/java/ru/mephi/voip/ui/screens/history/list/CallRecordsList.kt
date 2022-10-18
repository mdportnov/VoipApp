package ru.mephi.voip.ui.screens.history.list

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.DismissDirection.EndToStart
import androidx.compose.material.DismissDirection.StartToEnd
import androidx.compose.material.DismissValue.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.inject
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.sip.PhoneStatus
import ru.mephi.shared.vm.CallerViewModel
import ru.mephi.voip.R
import ru.mephi.voip.data.PhoneManager
import ru.mephi.voip.ui.call.CallActivity
import ru.mephi.voip.ui.components.ExpandableCard
import ru.mephi.voip.ui.components.ExpandableContent

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun CallRecordsList(
    setSelectedRecord: (CallRecord?) -> Unit,
    onSnackBarHostStateChanged: (CallRecord) -> Unit,
) {
    val viewModel: CallerViewModel by inject()
    val phoneManager: PhoneManager by inject()
    val items by viewModel.getAllRecordsFlow().collectAsState(initial = emptyList())

    val context = LocalContext.current

    lateinit var deletedRecord: CallRecord

    val expandedCardIds = viewModel.expandedCardIdsList.collectAsState()

    val onDeleteItem: (CallRecord) -> Unit = { record ->
        deletedRecord = record
        viewModel.deleteRecord(deletedRecord)
        onSnackBarHostStateChanged(deletedRecord)
    }

    val onSwipeToCall: (CallRecord) -> Unit = { record ->
        if (phoneManager.phoneStatus.value == PhoneStatus.REGISTERED) {
            CallActivity.create(context, record.sipNumber, false)
        } else {
            Toast.makeText(
                context,
                "Нет активного аккаунта для совершения звонка",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val records = viewModel.getAllCallRecords()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(items = items, key = { it.id!! }) { recordItem ->
                val dismissState = rememberDismissState(
                    confirmStateChange = {
                        if (it == DismissedToEnd)
                            onDeleteItem(recordItem)
                        if (it == DismissedToStart)
                            onSwipeToCall(recordItem)
                        false
                    }
                )
                SwipeToDismiss(
                    state = dismissState,
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .animateItemPlacement(),
                    directions = setOf(StartToEnd, EndToStart),
                    dismissThresholds = { FractionalThreshold(0.2f) },
                    background = {
                        val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                        val color by animateColorAsState(
                            when (dismissState.targetValue) {
                                Default -> Color.LightGray
                                DismissedToEnd -> colorResource(id = R.color.colorAccent)
                                DismissedToStart -> colorResource(id = R.color.colorGreen)
                            }
                        )
                        val alignment = when (direction) {
                            StartToEnd -> Alignment.CenterStart
                            EndToStart -> Alignment.CenterEnd
                        }
                        val icon = when (direction) {
                            StartToEnd -> Icons.Default.Delete
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
                                    records.indexOf(recordItem)
                                )
                            },
                            expanded = expandedCardIds.value.contains(
                                records.indexOf(recordItem)
                            ),
                            content = {
                                CallRecordMain(record = recordItem)
                            },
                            expandableContent = {
                                ExpandableContent(content = {
                                    CallRecordMoreInfo(
                                        record = recordItem,
                                        setSelectedRecord = setSelectedRecord
                                    )
                                })
                            }
                        )
                    },
                )
            }
        }
    }
}