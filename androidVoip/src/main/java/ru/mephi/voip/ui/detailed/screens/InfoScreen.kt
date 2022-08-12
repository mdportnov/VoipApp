@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.detailed.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import org.koin.androidx.compose.get
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.model.PositionInfo
import ru.mephi.shared.vm.DetailedInfoViewModel
import ru.mephi.voip.R
import ru.mephi.voip.ui.common.GroupTitle
import ru.mephi.voip.utils.getImageUrl
import timber.log.Timber

@Composable
internal fun InfoScreen(
    diVM: DetailedInfoViewModel = get()
) {
    val detailedInfo = diVM.detailedInfo.collectAsState(Appointment())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 6.dp, end = 6.dp)
    ) {
        BasicInfo(
            username = detailedInfo.value.let { i ->
                when {
                    i.firstname.isNotEmpty() && i.lastname.isNotEmpty() -> "${i.lastname} ${i.firstname}"
                    i.fullName.isNotEmpty() -> i.fullName
                    i.fio.isNotEmpty() -> i.fio
                    else -> i.line
                }
            },
            SIP = detailedInfo.value.line
        )
        if (detailedInfo.value.positions.isNotEmpty()) {
            GroupTitle(title = "Должности")
            detailedInfo.value.positions.forEach { pos ->
                AppointmentCard(positionInfo = pos)
            }
        }
    }
}

@Composable
private fun BasicInfo(
    username: String,
    SIP: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            modifier = Modifier
                .size(124.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            model = ImageRequest.Builder(LocalContext.current)
                .data(getImageUrl(SIP))
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build(),
            error = painterResource(id = R.drawable.ic_dummy_avatar),
            placeholder = painterResource(id = R.drawable.ic_dummy_avatar),
            contentDescription = null
        )
        Text(
            text = username,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(4.dp),
            maxLines = 1,
            overflow  = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AppointmentCard(
    positionInfo: PositionInfo
) {
    val shape = RoundedCornerShape(6.dp)
    Card(
        shape = shape,
        modifier = Modifier
            .clip(shape)
            .defaultMinSize(minHeight = 42.dp)
            .fillMaxWidth()
    ) {
        Text(text = positionInfo.appointmentName)
    }
}

private fun trimAppointmentString(
    appointment: String
): String {
    appointment.trim().let {
        if (it.endsWith(",")) {
            it.dropLast(1)
        }
        it.replaceFirstChar(Char::titlecase)
        return it
    }
}