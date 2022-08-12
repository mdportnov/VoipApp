@file:OptIn(ExperimentalMaterial3Api::class)

package ru.mephi.voip.ui.switch

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.get
import ru.mephi.shared.vm.SavedAccountsViewModel
import ru.mephi.voip.data.AccountStatusRepository

@Composable
internal fun SwitchScreen(
    goBack: () -> Unit,
    accountRepo: AccountStatusRepository = get(),
    saVM: SavedAccountsViewModel = get()
) {
    Scaffold(
        topBar = { SwitchTopBar(goBack) }
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column() {
                saVM.sipList.value.forEach {
                    val tmp = saVM.accountsMap[it]
                    Text(text = tmp?.value?.fio ?: "")
                }
            }
        }
    }
}

@Composable
private fun SwitchTopBar(
    goBack: () -> Unit
) {
    SmallTopAppBar(
        title = { Text(text = "Сменить аккаунт") },
        navigationIcon = {
            IconButton(onClick = { goBack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
        }
    )
}