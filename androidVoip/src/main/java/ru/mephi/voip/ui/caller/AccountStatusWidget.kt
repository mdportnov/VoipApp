package ru.mephi.voip.ui.caller

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.utils.ColorGray
import ru.mephi.voip.utils.ColorGreen
import ru.mephi.voip.utils.ColorRed

@Composable
fun AccountStatusWidget(accountStatusRepository: AccountStatusRepository, modifier: Modifier) {
    val status by accountStatusRepository.status.collectAsState()

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        if (status != AccountStatus.UNREGISTERED)
            Text(text = status.status, modifier = Modifier.padding(end = 5.dp))
        Canvas(modifier = Modifier
            .padding(end = 5.dp)
            .size(15.dp),
            onDraw = {
                drawCircle(
                    color = when (status) {
                        AccountStatus.REGISTERED -> ColorGreen
                        AccountStatus.NO_CONNECTION, AccountStatus.CHANGING, AccountStatus.LOADING -> ColorGray
                        AccountStatus.UNREGISTERED, AccountStatus.REGISTRATION_FAILED -> ColorRed
                    }
                )
            })
    }
}