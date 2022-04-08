package ru.mephi.voip.ui.catalog.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Room
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.mephi.shared.data.model.Appointment
import ru.mephi.voip.R
import ru.mephi.voip.utils.ColorGray
import ru.mephi.voip.utils.ColorRed
import ru.mephi.voip.utils.launchDialer
import ru.mephi.voip.utils.launchMailClientIntent

@Composable
fun UserCatalogMoreInfo(record: Appointment) {
    val context = LocalContext.current

    Column {
        record.lineShown?.let { line ->
            if (line.isNotEmpty())
                RowWithIcon(
                    Modifier.padding(vertical = 2.dp, horizontal = 5.dp),
                    Icons.Default.Call,
                    Color.Blue,
                    "Звонок через телефон",
                    onClick = {
                        context.launchDialer(context.getString(R.string.mephi_number) + "," + line)
                    })
        }

        record.email?.let { email ->
            if (email.isNotEmpty())
                RowWithIcon(
                    Modifier.padding(vertical = 2.dp, horizontal = 5.dp),
                    Icons.Default.Mail,
                    ColorRed,
                    "Email: ",
                    onClick = {
                        context.launchMailClientIntent(email)
                    }) {
                    Text(
                        text = email, style = TextStyle(fontSize = 14.sp, color = ColorGray)
                    )
                }
        }

        record.room?.let { room ->
            RowWithIcon(
                Modifier.padding(vertical = 2.dp, horizontal = 5.dp),
                Icons.Default.Room,
                Color.Black,
                "Помещение:"
            ) {
                Text(text = room, style = TextStyle(fontSize = 14.sp, color = ColorGray))
            }
        }
    }
}
