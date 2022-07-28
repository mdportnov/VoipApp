@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.catalog.list

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import org.koin.androidx.compose.inject
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.R
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.call.CallActivity
import ru.mephi.voip.ui.catalog.CatalogViewModel
import ru.mephi.voip.utils.getImageUrl


@Composable
internal fun UserCatalogItem(
    record: Appointment,
    navController: NavController,
    isStart: Boolean = false,
    isEnd: Boolean = false
) {
    val context = LocalContext.current
    val viewModel: CatalogViewModel by inject()
    val accountStatusRepository: AccountStatusRepository by inject()
    Card(
        shape = RoundedCornerShape(
            topStart = (if (isStart) 8 else 0).dp,
            topEnd = (if (isStart) 8 else 0).dp,
            bottomStart = (if (isEnd) 8 else 0).dp,
            bottomEnd = (if (isEnd) 8 else 0).dp
        ),
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp)
            .wrapContentHeight()
            .padding(top = 0.5.dp, bottom = 0.5.dp)
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
                model = record.line?.let { getImageUrl(it) },
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
                    text = record.fio,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                record.line?.let {
                    Text(
                        buildAnnotatedString {
                            withStyle(style = MaterialTheme.typography.labelLarge.toSpanStyle()) {
                                append("Номер: ")
                            }
                            withStyle(style = MaterialTheme.typography.bodyMedium.toSpanStyle()) {
                                append(it)
                            }
                        }
                    )
                }
            }
            Row(
                modifier = Modifier.wrapContentSize()
            ) {
                UserActionButton(Icons.Default.Star) {
                    Toast.makeText(context, viewModel.addToFavourites(record).text, Toast.LENGTH_SHORT).show()
                }
                UserActionButton(Icons.Default.Call) {
                    if (accountStatusRepository.status.value == AccountStatus.REGISTERED && !record.line.isNullOrEmpty()) {
                        CallActivity.create(context, record.line!!, false)
                    } else {
                        Toast.makeText(context, R.string.no_active_account_call, Toast.LENGTH_SHORT).show()
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

@Composable
fun RowWithIcon(
    modifier: Modifier,
    icon: ImageVector,
    color: Color,
    title: String,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    Row(horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true), // You can also change the color and radius of the ripple
                onClick = {})
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            }) {
        Icon(
            icon, tint = color, contentDescription = title, modifier = Modifier.padding(end = 5.dp)
        )

        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            style = TextStyle(fontSize = 14.sp),
            modifier = Modifier.padding(end = 5.dp)
        )
        content()
    }
}