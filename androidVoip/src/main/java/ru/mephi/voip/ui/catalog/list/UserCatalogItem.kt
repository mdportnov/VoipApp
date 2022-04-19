package ru.mephi.voip.ui.catalog.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DialerSip
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import ru.mephi.shared.data.model.Appointment
import ru.mephi.voip.R
import ru.mephi.voip.ui.catalog.CatalogViewModel
import ru.mephi.voip.utils.ColorAccent
import ru.mephi.voip.utils.ColorGray
import ru.mephi.voip.utils.ColorGreen
import ru.mephi.voip.utils.getImageUrl


@Composable
fun UserCatalogItem(
    modifier: Modifier,
    record: Appointment,
    viewModel: CatalogViewModel,
    navController: NavController
) {
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current).data(data = record.line?.let { getImageUrl(it) })
            .apply(block = fun ImageRequest.Builder.() {
                crossfade(200)
                transformations(RoundedCornersTransformation(15f))
                crossfade(true)
                diskCachePolicy(CachePolicy.ENABLED)
                networkCachePolicy(CachePolicy.ENABLED)
                memoryCachePolicy(CachePolicy.ENABLED)
                placeholder(R.drawable.nophoto)
                error(R.drawable.nophoto)
            }).build()
    )

    Row(modifier = modifier) {
        Image(
            painter = painter,
            modifier = Modifier
                .width(80.dp)
                .aspectRatio(0.8f)
                .padding(end = 10.dp),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = record.fio, fontWeight = FontWeight.Bold, style = TextStyle(fontSize = 16.sp)
            )

            record.appointment?.let {
                Text(
                    text = "Должность: ",
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontSize = 14.sp, color = ColorGray)
                )
                if (record.positions == null) {
                    Text(
                        text = it,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(fontSize = 14.sp),
                        maxLines = 10,
                        modifier = Modifier.width(250.dp)
                    )
                } else {
                    record.positions!!.forEach { info ->
                        Column(modifier = Modifier.clickable {
                            viewModel.goNext(info.unitCodeStr)

                        }) {
                            Text(
                                text = info.unitName,
                                fontWeight = FontWeight.Medium,
                                style = TextStyle(fontSize = 14.sp, color = ColorAccent),
                                maxLines = 3,
                                modifier = Modifier.width(250.dp)
                            )
                            Text(
                                text = info.appointmentName,
                                fontWeight = FontWeight.Light,
                                style = TextStyle(fontSize = 14.sp, color = ColorGray),
                                maxLines = 1,
                                modifier = Modifier.width(250.dp)
                            )
                        }
                    }
                }
            }

            record.line?.let {
                RowWithIcon(Modifier.padding(vertical = 2.dp),
                    icon = Icons.Default.DialerSip,
                    color = ColorGreen,
                    title = "SIP: ",
                    onClick = {
                        navController.navigate("caller?caller_number=${record.line}&caller_name=${record.fullName}")
                    }) {
                    Text(
                        text = it, style = TextStyle(fontSize = 14.sp, color = ColorGray)
                    )
                }
            }
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