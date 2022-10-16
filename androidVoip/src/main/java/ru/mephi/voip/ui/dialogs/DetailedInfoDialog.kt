@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class
)

package ru.mephi.voip.ui.dialogs

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import org.koin.androidx.compose.get
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.model.PositionInfo
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.vm.DetailedInfoStatus
import ru.mephi.shared.vm.DetailedInfoViewModel
import ru.mephi.voip.R
import ru.mephi.voip.ui.common.exceptions.OnBadResult
import ru.mephi.voip.ui.screens.catalog.screens.common.OnLoading
import ru.mephi.voip.ui.screens.catalog.screens.common.OnNetworkFailure
import ru.mephi.voip.utils.getImageUrl
import ru.mephi.voip.utils.getImageUrlByGuid
import ru.mephi.voip.utils.launchMailClientIntent

@Composable
internal fun DetailedInfoDialog(
    onDismiss: () -> Unit,
    diVM: DetailedInfoViewModel = get(),
    onCallClick: (String) -> Unit,
    goNext: (UnitM) -> Unit,
    onStar: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
        ) {
            val appointment = diVM.detailedInfo.collectAsState()
            Scaffold(
                modifier = Modifier
                    .padding(top = 34.dp, bottom = 42.dp, start = 10.dp, end = 10.dp)
                    .background(color = Color.Transparent)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = false) { },
                topBar = {
                    TopAppBar(
                        title = { },
                        navigationIcon = {
                            IconButton(onClick = { onDismiss() }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                            }
                        },
                        actions = {
                            if (appointment.value.line.isNotEmpty()) {
                                IconButton(onClick = { onStar() }) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    )
                }
            ) {
                Surface(
                    modifier = Modifier.padding(it)
                ) {
                    val status = diVM.status.collectAsState()
                    DetailedInfoView(status.value) {
                        DetailedInfoContent(
                            appointment = appointment.value,
                            goNext = goNext,
                            onCallClick = onCallClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailedInfoView(
    status: DetailedInfoStatus,
    content: @Composable () -> Unit
) {
    when (status) {
        DetailedInfoStatus.OK -> {
            content()
        }
        DetailedInfoStatus.LOADING -> {
            OnLoading()
        }
        DetailedInfoStatus.BAD_RESULT -> {
            OnBadResult()
        }
        DetailedInfoStatus.NETWORK_FAILURE -> {
            OnNetworkFailure()
        }
    }
}

@Composable
private fun DetailedInfoContent(
    appointment: Appointment,
    goNext: (UnitM) -> Unit,
    onCallClick: (String) -> Unit
) {
    val context = LocalContext.current
    CompositionLocalProvider(
        LocalOverscrollConfiguration provides null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(124.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                model = ImageRequest.Builder(LocalContext.current)
                    .data(
                        if (appointment.EmpGUID.isNotEmpty()) {
                            getImageUrlByGuid(appointment.EmpGUID)
                        } else {
                            getImageUrl(appointment.line)
                        }
                    )
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                error = painterResource(id = R.drawable.ic_dummy_avatar),
                placeholder = painterResource(id = R.drawable.ic_dummy_avatar),
                contentDescription = null
            )
            Text(
                text = getRealUsername(appointment),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(top = 4.dp, start = 6.dp, end = 6.dp)
            ) {
                if (appointment.line.isNotEmpty()) {
                    DetailedInfoDivider()
                    DetailedInfoTitle(title = "Телефон")
                    DetailedInfoCard(
                        text = "Номер: ${appointment.line}",
                        icon = Icons.Default.Phone,
                        onClick = { onCallClick(appointment.line) }
                    )
                }
                if (appointment.email.isNotEmpty()) {
                    DetailedInfoDivider()
                    DetailedInfoTitle(title = "Почта")
                    DetailedInfoCard(
                        text = "Email: ${appointment.email}",
                        icon = Icons.Default.Mail,
                        onClick = { context.launchMailClientIntent(appointment.email) }
                    )
                }
                if (appointment.positions.isNotEmpty()) {
                    DetailedInfoDivider()
                    DetailedInfoTitle(title = "Места работы")
                    appointment.positions.let {
                        it.forEachIndexed { i, pos ->
                            DetailedInfoCard(
                                text = getPositionString(pos),
                                icon = Icons.Default.Search,
                                onClick = {
                                    goNext(
                                        UnitM(
                                            code_str = pos.unitCodeStr,
                                            shortname = pos.unitShortname
                                        )
                                    )
                                },
                                isStart = i == 0,
                                isEnd = i == it.size - 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailedInfoTitle(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun DetailedInfoCard(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isStart: Boolean = true,
    isEnd: Boolean = true
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
            .padding(bottom = 4.dp)
            .clip(cardShape)
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(2.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
            IconButton(onClick = { onClick() }) {
                Icon(imageVector = icon, contentDescription = null)
            }
        }
    }
}

@Composable
private fun DetailedInfoDivider() {
    Divider(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
    )
}

private fun getRealUsername(i: Appointment): String {
    return when {
        i.fullName.isNotEmpty() -> i.fullName
        i.firstname.isNotEmpty() && i.lastname.isNotEmpty() -> "${i.lastname} ${i.firstname}"
        i.fio.isNotEmpty() -> i.fio
        else -> i.line
    }
}

private fun getPositionString(pos: PositionInfo): String {
    var ret = ""
    if (pos.unitName.isNotEmpty()) ret += "Место: ${pos.unitName}\n"
    if (pos.appointmentName.isNotEmpty()) ret += "Должность: ${pos.appointmentName}\n"
    if (pos.room.isNotEmpty()) ret += "Помещение: ${pos.room}"
    return ret.trim()
}


