package ru.mephi.voip.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.get
import ru.mephi.shared.data.sip.PhoneStatus
import ru.mephi.voip.data.PhoneManager

@Composable
fun SipStatusActionButton(
    phoneManager: PhoneManager = get()
) {
    val currentAccount by phoneManager.currentAccount.collectAsState()
    if (currentAccount.login.isNotEmpty()) {
        val phoneStatus by phoneManager.phoneStatus.collectAsState()
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) { }
            Icon(
                imageVector = when (phoneStatus) {
                    PhoneStatus.UNREGISTERED -> Icons.Outlined.PhoneDisabled
                    PhoneStatus.CONNECTING,
                    PhoneStatus.STARTING_UP,
                    PhoneStatus.SHUTTING_DOWN -> Icons.Outlined.Sync
                    PhoneStatus.RESTARTING -> Icons.Outlined.RestartAlt
                    PhoneStatus.NO_CONNECTION -> Icons.Outlined.WifiOff
                    PhoneStatus.REGISTRATION_FAILED,
                    PhoneStatus.RECONNECTING -> Icons.Outlined.ErrorOutline
                    PhoneStatus.REGISTERED -> Icons.Outlined.Done
                },
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
                contentDescription = null
            )
        }
    }
}