package ru.mephi.voip.ui.profile.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.inject
import ru.mephi.voip.ui.profile.ProfileViewModel

@Composable
fun ScreenChangeAccount(onCloseBottomSheet: () -> Unit) {
    val viewModel by inject<ProfileViewModel>()
    val accountList = viewModel.accountList.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.White, shape = RectangleShape)
    ) {
        LazyColumn {
            items(items = accountList.value) { acc ->
                AccountItem(acc, onCloseBottomSheet)
            }
        }
    }
}