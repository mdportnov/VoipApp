package ru.mephi.voip.ui.caller.compose

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.DismissDirection.EndToStart
import androidx.compose.material.DismissDirection.StartToEnd
import androidx.compose.material.DismissValue.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DialerSip
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.request.CachePolicy
import coil.transform.RoundedCornersTransformation
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.network.KtorClientBuilder
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.shared.vm.CallerViewModel
import ru.mephi.voip.R
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.call.CallActivity
import ru.mephi.voip.utils.durationStringFromMillis
import ru.mephi.voip.utils.stringFromDate
import timber.log.Timber


@OptIn(ExperimentalMaterialApi::class, ExperimentalCoilApi::class, ExperimentalFoundationApi::class)
@Composable
fun CallRecordsList() {
    val viewModel: CallerViewModel by inject()
    val accountStatusRepository: AccountStatusRepository by inject()
    val items by viewModel.getAllRecordsFlow().collectAsState(initial = emptyList())

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    lateinit var deletedRecord: CallRecord

    val expandedCardIds = viewModel.expandedCardIdsList.collectAsState()

    val onDeleteItem: (CallRecord) -> Unit = { record ->
        scope.launch {
            snackBarHostState.currentSnackbarData?.dismiss()
            deletedRecord = record
            viewModel.deleteRecord(deletedRecord)
            val snackBarResult = snackBarHostState.showSnackbar(
                "Запись ${deletedRecord.sipNumber} удалена",
                actionLabel = "Вернуть"
            )
            when (snackBarResult) {
                SnackbarResult.Dismissed -> Timber.d("SnackBar dismissed")
                SnackbarResult.ActionPerformed -> viewModel.addRecord(
                    deletedRecord
                )
            }
        }
    }

    val onSwipeToCall: (CallRecord) -> Unit = { record ->
        if (accountStatusRepository.status.value == AccountStatus.REGISTERED) {
            CallActivity.create(context, record.sipNumber, false)
        } else {
            Toast.makeText(
                context,
                "Нет активного аккаунта для совершения звонка",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Box {
        LazyColumn {
            items(items = items, key = { it.id!! }) { recordItem ->
                val dismissState = rememberDismissState(
                    confirmStateChange = {
                        if (it == DismissedToEnd)
                            onDeleteItem(recordItem)
                        else
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
                    dismissThresholds = { direction ->
                        FractionalThreshold(if (direction == StartToEnd) 0.4f else 0.2f)
                    },
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
                            onCardArrowClick = {
                                viewModel.onCardArrowClicked(
                                    viewModel.getAllCallRecords().indexOf(recordItem)
                                )
                            },
                            expanded = expandedCardIds.value.contains(
                                viewModel.getAllCallRecords().indexOf(recordItem)
                            ),
                            content = { CallRecordMain(record = recordItem) },
                            expandableContent = {
                                ExpandableContent(content = {
                                    CallRecordMoreInfo(
                                        record = recordItem
                                    )
                                })
                            }
                        )
                    },
                )
            }
        }
        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun CallRecordMain(record: CallRecord) {
    Column {
        Row {
            Image(
                painter = rememberImagePainter(
                    data = KtorClientBuilder.PHOTO_REQUEST_URL_BY_PHONE + record.sipNumber,
                    builder = {
                        placeholder(R.drawable.nophoto)
                        crossfade(true)
                        diskCachePolicy(CachePolicy.ENABLED)
                        memoryCachePolicy(CachePolicy.ENABLED)
                        transformations(RoundedCornersTransformation(15f))
                        error(R.drawable.nophoto)
                    }
                ),
                modifier = Modifier
                    .width(50.dp)
                    .aspectRatio(0.8f)
                    .padding(end = 10.dp),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
            Column {
                Text(
                    record.sipNumber,
                    style = TextStyle(color = Color.Gray, fontSize = 25.sp)
                )
                Row() {
                    ImageCallStatus(record.status)
                    Text(
                        record.time.stringFromDate(),
                        style = TextStyle(color = colorResource(id = R.color.colorAccent))
                    )
                }
            }
        }
    }
}

@Composable
fun CallRecordMoreInfo(record: CallRecord) {
    Column(modifier = Modifier.padding(8.dp)) {
        Row {
            Text(
                "Статус:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 10.dp)
            )
            Text(record.status.text, modifier = Modifier.padding(end = 10.dp))
            Text(record.duration.durationStringFromMillis())
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Номер:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(record.sipNumber, modifier = Modifier.padding(end = 10.dp))
            Icon(
                Icons.Default.Info,
                tint = Color.LightGray,
                contentDescription = "Подробнее",
                modifier = Modifier.padding(end = 10.dp)
            )
            Icon(
                Icons.Default.DialerSip,
                tint = colorResource(id = R.color.colorGreen),
                contentDescription = "Позвонить",
            )
        }
    }
}