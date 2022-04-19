package ru.mephi.voip.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.profile.ProfileViewModel
import ru.mephi.voip.utils.ColorAccent
import ru.mephi.voip.utils.ColorGray
import ru.mephi.voip.utils.ColorGreen
import ru.mephi.voip.utils.ColorPrimary


@Composable
fun AccountStatusWidget(
    modifier: Modifier = Modifier,
    accountStatusRepository: AccountStatusRepository,
    scaffoldState: ScaffoldState
) {
    val accountStatus by accountStatusRepository.status.collectAsState()
    val isSipEnabled by accountStatusRepository.isSipEnabled.collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val viewModel: ProfileViewModel by inject()

    IconButton(modifier = modifier.background(ColorPrimary, CircleShape), onClick = {
        if (accountStatus == AccountStatus.REGISTERED) {
            scope.launch {
                scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                scaffoldState.snackbarHostState.showSnackbar(accountStatus.status)
            }
        } else {
            if (isSipEnabled) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.retryRegistration()
            } else {
                scope.launch {
                    scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                    scaffoldState.snackbarHostState.showSnackbar("Включите SIP")
                }
            }
        }
    }) {
        Icon(
            when (accountStatus) {
                AccountStatus.REGISTERED, AccountStatus.NO_CONNECTION -> Icons.Filled.CheckCircle
                AccountStatus.UNREGISTERED, AccountStatus.REGISTRATION_FAILED, AccountStatus.CHANGING, AccountStatus.LOADING -> Icons.Filled.Refresh
            },
            "Статус",
            tint = when (accountStatus) {
                AccountStatus.REGISTERED -> ColorGreen
                AccountStatus.UNREGISTERED, AccountStatus.REGISTRATION_FAILED -> ColorAccent
                AccountStatus.NO_CONNECTION, AccountStatus.CHANGING, AccountStatus.LOADING -> ColorGray
            },
        )
    }
}