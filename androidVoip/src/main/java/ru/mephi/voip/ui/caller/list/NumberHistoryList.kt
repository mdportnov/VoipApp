package ru.mephi.voip.ui.caller.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
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
@Preview(showBackground = true, showSystemUi = true)
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
    ),
    {}
)

@OptIn(ExperimentalCoilApi::class)
@Composable
fun NumberHistoryList(
    callRecord: CallRecord,
    callsHistory: List<CallRecord>,
    setSelectedRecord: (CallRecord?) -> Unit
) {
    val painter = rememberImagePainter(
        data = KtorClientBuilder.PHOTO_REQUEST_URL_BY_PHONE + callRecord.sipNumber,
        builder = {
            crossfade(true)
            diskCachePolicy(CachePolicy.ENABLED)
        }
    )
    Card(
        modifier = Modifier
            .background(Color.White)
            .fillMaxWidth()
            .padding(5.dp), elevation = 10.dp,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Spacer(modifier = Modifier.width(50.dp))
                Image(
                    painter = painter,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(100.dp)
                )

                IconButton(onClick = {
                    setSelectedRecord(null)
                }, modifier = Modifier.padding(end = 10.dp)) {
                    Icon(
                        Icons.Sharp.Close,
                        tint = Color.LightGray,
                        contentDescription = "Закрыть"
                    )
                }
            }

            Text(
                text = callRecord.sipNumber,
                style = TextStyle(color = Color.Black, fontSize = 30.sp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            callRecord.sipName?.let {
                if (it != callRecord.sipNumber)
                    Text(
                        text = it, style = TextStyle(color = Color.Black, fontSize = 30.sp),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(items = callsHistory) { callItem ->
                    CallItem(callRecord = callItem)
                }
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

        Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
            Text(
                text = callRecord.time.stringFromDate(),
                style = TextStyle(color = colorResource(id = R.color.colorAccent))
            )
            Text(
                text = callRecord.status.text + ", " + callRecord.duration.durationStringFromMillis(),
                style = TextStyle(color = Color.Gray)
            )
        }
    }
}