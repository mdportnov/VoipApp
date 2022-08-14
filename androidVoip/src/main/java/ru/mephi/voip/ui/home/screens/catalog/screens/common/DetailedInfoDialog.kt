@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package ru.mephi.voip.ui.home.screens.catalog.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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
import ru.mephi.voip.ui.common.GroupTitle
import ru.mephi.voip.ui.common.OnBadResult
import ru.mephi.voip.utils.getImageUrl
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
                    SmallTopAppBar(
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            modifier = Modifier
                .size(124.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            model = ImageRequest.Builder(LocalContext.current)
                .data(getImageUrl(appointment.lineShown))
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
            modifier = Modifier.padding(4.dp),
            maxLines = 1,
            overflow  = TextOverflow.Ellipsis
        )
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            if (appointment.line.isNotEmpty()) {
                DetailedInfoDivider()
                PhoneCard(phone = appointment.line, onCallClick = onCallClick)
            }
            if (appointment.email.isNotEmpty()) {
                DetailedInfoDivider()
                MailCard(email = appointment.email)
            }
            if (appointment.positions.isNotEmpty()) {
                DetailedInfoDivider()
                GroupTitle(title = "Места работы")
                appointment.positions.forEach { pos ->
                    PositionCard(pos, goNext)
                }
            }
            DetailedInfoDivider()
        }
    }
}

@Composable
private fun PhoneCard(
    phone: String,
    onCallClick: (String) -> Unit
) {
    val width = LocalConfiguration.current.screenWidthDp
    val shape = RoundedCornerShape(8.dp)
    GroupTitle(title = "Телефон")
    Card(
        shape = shape,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .clip(shape)
            .clickable { onCallClick(phone) },
        elevation = CardDefaults.elevatedCardElevation(),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = "Номер: $phone",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width((width - 76).dp)
            )
            IconButton(onClick = { onCallClick(phone) }) {
                Icon(imageVector = Icons.Default.Phone, contentDescription = null)
            }
        }
    }
}

@Composable
private fun MailCard(
    email: String
) {
    val context = LocalContext.current
    val width = LocalConfiguration.current.screenWidthDp
    val shape = RoundedCornerShape(8.dp)
    GroupTitle(title = "Email")
    Card(
        shape = shape,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .clip(shape)
            .clickable {
                context.launchMailClientIntent(email)
            },
        elevation = CardDefaults.elevatedCardElevation(),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = email,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width((width - 76).dp)
            )
            IconButton(onClick = { context.launchMailClientIntent(email) }) {
                Icon(imageVector = Icons.Default.Mail, contentDescription = null)
            }
        }
    }
}

@Composable
private fun PositionCard(
    pos: PositionInfo,
    goNext: (UnitM) -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    val width = LocalConfiguration.current.screenWidthDp
    Card(
        shape = shape,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
            .clip(shape)
            .clickable {
                goNext(UnitM(code_str = pos.unitCodeStr))
            },
        elevation = CardDefaults.elevatedCardElevation(),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(2.dp)
        ) {
            Column(modifier = Modifier.width((width - 76).dp)) {
                Text(text = buildAnnotatedString {
                    withStyle(MaterialTheme.typography.titleMedium.toSpanStyle()) {
                        append("Место: ${pos.unitName}\nДолжность: ${pos.appointmentName}")
                        if (pos.room.isNotEmpty()) {
                            append("\nПомещение: ${pos.room}")
                        }
                    }
                })
            }
            IconButton(onClick = { goNext(UnitM(code_str = pos.unitCodeStr)) }) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
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
            .height(12.dp)
    )
}

private fun getRealUsername(i: Appointment): String {
    return when {
        i.firstname.isNotEmpty() && i.lastname.isNotEmpty() -> "${i.lastname} ${i.firstname}"
        i.fullName.isNotEmpty() -> i.fullName
        i.fio.isNotEmpty() -> i.fio
        else -> i.line
    }
}


