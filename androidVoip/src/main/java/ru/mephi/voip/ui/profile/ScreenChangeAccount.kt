package ru.mephi.voip.ui.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.inject
import ru.mephi.shared.data.model.Account

@SuppressLint("MutableCollectionMutableState")
@Composable
fun ScreenChangeAccount(onCloseBottomSheet: () -> Unit) {
    val viewModel by inject<ProfileViewModel>()
    val lifecycleOwner = LocalLifecycleOwner.current
    val mList: MutableList<Account> by remember { mutableStateOf(viewModel.accountRepository.getAccountsList()) }

    viewModel.accountRepository.accountList.observe(lifecycleOwner) {
        mList.apply {
            clear()
            addAll(viewModel.accountRepository.getAccountsList())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.White, shape = RectangleShape)
    ) {
        LazyColumn {
            items(items = mList) { acc ->
                AccountItem(acc, onCloseBottomSheet)
            }
        }
    }
}