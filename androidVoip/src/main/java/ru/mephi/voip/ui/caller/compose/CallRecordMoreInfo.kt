package ru.mephi.voip.ui.caller.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DialerSip
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.voip.R
import ru.mephi.voip.utils.durationStringFromMillis

@Composable
fun CallRecordMoreInfo(record: CallRecord, setSelectedRecord: (CallRecord?) -> Unit) {
    Column(modifier = Modifier.padding(8.dp)) {
        Row {
            Text(
                "Статус:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 10.dp)
            )
            Text(record.status.text, modifier = Modifier.padding(end = 10.dp))
            Text(record.duration.durationStringFromMillis())
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Номер:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(record.sipNumber, modifier = Modifier.padding(end = 10.dp))
            IconButton(onClick = {
                setSelectedRecord(record)
            }) {
                Icon(
                    Icons.Default.Info,
                    tint = Color.LightGray,
                    contentDescription = "Подробнее",
                    modifier = Modifier.padding(end = 10.dp)
                )
            }
            Icon(
                Icons.Default.DialerSip,
                tint = colorResource(id = R.color.colorGreen),
                contentDescription = "Позвонить",
            )
        }
    }
}