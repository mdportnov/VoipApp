@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.home.screens.catalog.items

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.koin.androidx.compose.inject
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.R
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.MasterActivity
import ru.mephi.voip.ui.call.CallActivity
import ru.mephi.voip.ui.home.screens.catalog.CatalogViewModel
import ru.mephi.voip.utils.getImageUrl


@Composable
internal fun UserCatalogItem(
    appointment: Appointment,
    openDetailedInfo: (appointment: Appointment) -> Unit,
    isStart: Boolean = false,
    isEnd: Boolean = false
) {
    val activity = LocalContext.current as MasterActivity
    val viewModel: CatalogViewModel by inject()
    val accountStatusRepository: AccountStatusRepository by inject()
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
            .padding(top = 0.5.dp, bottom = 0.5.dp)
            .clip(cardShape)
            .clickable { openDetailedInfo(appointment) }
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = 1.5.dp, top = 1.5.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier
                    .padding(start = 6.dp, end = 10.dp, top = 4.dp, bottom = 4.dp)
                    .size(42.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                model = getImageUrl(appointment.line),
                error = painterResource(id = R.drawable.ic_dummy_avatar),
                placeholder = painterResource(id = R.drawable.ic_dummy_avatar),
                contentDescription = null
            )
            Column(
                modifier = Modifier
                    .width(
                        (LocalConfiguration.current.screenWidthDp - 158).dp
                    )
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = appointment.fio,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = MaterialTheme.typography.labelLarge.toSpanStyle()) {
                            append("Номер: ")
                        }
                        withStyle(style = MaterialTheme.typography.bodyMedium.toSpanStyle()) {
                            append(appointment.line)
                        }
                    },
                    maxLines = 1,
                    overflow  = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.wrapContentSize()
            ) {
                UserActionButton(Icons.Default.Star) {
                    Toast.makeText(activity, viewModel.addToFavourites(appointment).text, Toast.LENGTH_SHORT).show()
                }
                UserActionButton(Icons.Default.Call) {
                    if (accountStatusRepository.status.value == AccountStatus.REGISTERED && appointment.line.isNotEmpty()) {
                        CallActivity.create(activity, appointment.line, false)
                    } else {
                        Toast.makeText(activity, R.string.no_active_account_call, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
private fun UserActionButton(
    imageVector: ImageVector,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(2.dp)
    ) {
        Box( modifier = Modifier
            .size(36.dp)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                shape = CircleShape
            )) { }
        IconButton(
            onClick = onClick,
            modifier = Modifier.then(Modifier.size(26.dp))
        ) {
            Icon(
                imageVector = imageVector,
                modifier = Modifier.size(26.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                contentDescription = null
            )
        }
    }
}
