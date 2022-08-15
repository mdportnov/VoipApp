package ru.mephi.voip.ui.profile.favourites

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DialerSip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import ru.mephi.shared.data.model.FavouriteRecord
import ru.mephi.shared.data.network.GET_PROFILE_PIC_URL_BY_SIP
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.call.CallActivity
import ru.mephi.voip.ui.profile.ProfileViewModel
import ru.mephi.voip.utils.ColorGreen
import ru.mephi.voip.utils.ColorRed

enum class FavouriteContextMenu(val text: String, val icon: ImageVector) {
    DELETE("Удалить", Icons.Outlined.Delete), CALL("Позвонить", Icons.Outlined.DialerSip)
}

@Composable
fun FavouriteItem(
    favouriteRecord: FavouriteRecord, expanded: Boolean,
    changeExpandedState: (Boolean) -> Unit,
    viewModel: ProfileViewModel,
    accountStatusRepository: AccountStatusRepository
) {
    val context = LocalContext.current

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(data = GET_PROFILE_PIC_URL_BY_SIP + favouriteRecord.sipNumber)
            .apply(block = fun ImageRequest.Builder.() {
                crossfade(true)
                diskCachePolicy(CachePolicy.ENABLED)
                memoryCachePolicy(CachePolicy.ENABLED)
            }).build()
    )

    Box(
        modifier = Modifier
            .padding(5.dp)
            .width(100.dp)
            .height(140.dp)
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(CircleShape)
                .size(100.dp)
                .align(Alignment.Center)
                .clickable {
                    changeExpandedState(true)
                }
        )

        Text(
            text = favouriteRecord.sipName,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.White)
                .border(shape = CircleShape, border = BorderStroke(0.dp, Color.Transparent))
        )

        DropdownMenu(
            expanded = expanded,
            offset = DpOffset((-40).dp, (-40).dp),
            onDismissRequest = {
                changeExpandedState(false)
            }) {
            FavouriteContextMenu.values().forEach {
                DropdownMenuItem(onClick = {
                    when (it) {
                        FavouriteContextMenu.DELETE -> {
                            viewModel.deleteFromFavourite(favouriteRecord)
                        }
                        FavouriteContextMenu.CALL -> {
                            if (accountStatusRepository.phoneStatus.value == AccountStatus.REGISTERED && favouriteRecord.sipNumber.isNotEmpty()) {
                                CallActivity.create(context, favouriteRecord.sipNumber, false)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Нет активного аккаунта для совершения звонка",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    changeExpandedState(false)
                }) {
                    Row {
                        Icon(
                            imageVector = it.icon,
                            tint = if (it == FavouriteContextMenu.DELETE) ColorRed else ColorGreen,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text(text = it.text, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}