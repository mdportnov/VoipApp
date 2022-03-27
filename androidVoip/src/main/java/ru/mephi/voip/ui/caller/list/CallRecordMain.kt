package ru.mephi.voip.ui.caller.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import coil.request.CachePolicy
import coil.transform.RoundedCornersTransformation
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.network.KtorClientBuilder
import ru.mephi.voip.R
import ru.mephi.voip.utils.stringFromDate

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
                    if (record.sipName.isNullOrEmpty()) record.sipNumber else record.sipName!!,
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

