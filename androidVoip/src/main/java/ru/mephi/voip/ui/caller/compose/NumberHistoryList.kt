package ru.mephi.voip.ui.caller.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.request.CachePolicy
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.model.CallStatus
import ru.mephi.shared.data.network.KtorClientBuilder
import ru.mephi.voip.R
import ru.mephi.voip.utils.durationStringFromMillis
import ru.mephi.voip.utils.stringFromDate

@Composable
@Preview
fun NumberHistoryListPreview() = NumberHistoryList(
    callRecord = CallRecord(
        1,
        "09024",
        "Портнов М.Д.",
        CallStatus.INCOMING,
        1648133547,
        10000
    ),
    listOf(
        CallRecord(
            1,
            "09024",
            "Портнов М.Д.",
            CallStatus.INCOMING,
            1648133547,
            10000
        ), CallRecord(
            1,
            "09024",
            "Портнов М.Д.",
            CallStatus.INCOMING,
            1648133547,
            10000
        ), CallRecord(
            1,
            "09024",
            "Портнов М.Д.",
            CallStatus.INCOMING,
            1648133547,
            10000
        )
    )
)

@OptIn(ExperimentalCoilApi::class)
@Composable
fun NumberHistoryList(callRecord: CallRecord, callsHistory: List<CallRecord>) {
    val painter = rememberImagePainter(
        data = KtorClientBuilder.PHOTO_REQUEST_URL_BY_PHONE + callRecord.sipNumber,
        builder = {
            crossfade(true)
            diskCachePolicy(CachePolicy.ENABLED)
        }
    )

    Column() {
        Image(
            painter = painter,
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(CircleShape)
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
        )

        Text(
            text = callRecord.sipNumber,
            style = TextStyle(color = Color.White, fontSize = 30.sp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        callRecord.sipName?.let {
            Text(
                text = it, style = TextStyle(color = Color.White),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(items = callsHistory) { callItem ->
                CallItem(callRecord = callItem)
            }
        }
    }

}

@Composable
@Preview
fun CallItemPreview() =
    CallItem(
        callRecord = CallRecord(
            1,
            "09024",
            "Портнов М.Д.",
            CallStatus.INCOMING,
            1648133547,
            10000
        )
    )

@Composable
fun ImageCallStatus(status: CallStatus) {
    Image(
        painterResource(
            when (status) {
                CallStatus.INCOMING ->
                    R.drawable.ic_baseline_call_received_24
                CallStatus.OUTCOMING ->
                    R.drawable.ic_baseline_call_made_24
                CallStatus.MISSED ->
                    R.drawable.ic_baseline_call_missed_24
                CallStatus.DECLINED_FROM_SIDE ->
                    R.drawable.ic_baseline_call_declined_24
                CallStatus.DECLINED_FROM_YOU ->
                    R.drawable.ic_baseline_call_declined_from_side_24
                else -> R.drawable.ic_baseline_error_24
            }
        ),
        contentDescription = "",
        contentScale = ContentScale.Crop,
        modifier = Modifier.padding(end = 10.dp)
    )
}

@Composable
fun CallItem(callRecord: CallRecord) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ImageCallStatus(callRecord.status)

        Column(horizontalAlignment = Alignment.Start) {
            Text(text = callRecord.time.stringFromDate(), style = TextStyle(color = Color.White))
            Text(
                text = callRecord.status.text + " , " + callRecord.time.durationStringFromMillis(),
                style = TextStyle(color = Color.Gray)
            )
        }
    }
}