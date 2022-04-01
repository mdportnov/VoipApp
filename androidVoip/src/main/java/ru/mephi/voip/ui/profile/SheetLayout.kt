package ru.mephi.voip.ui.profile

import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.stringResource
import ru.mephi.voip.R

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun SheetLayout(
    currentScreen: BottomSheetScreen,
    onCloseBottomSheet: () -> Unit,
    scaffoldState: BottomSheetScaffoldState
) {
    BottomSheetWithCloseDialog(
        onCloseBottomSheet, title =
        when (currentScreen) {
            BottomSheetScreen.ScreenAddNewAccount -> stringResource(id = R.string.add_new_account)
            BottomSheetScreen.ScreenChangeAccount -> stringResource(id = R.string.change_account)
        }
    ) {
        when (currentScreen) {
            BottomSheetScreen.ScreenAddNewAccount -> ScreenAddNewAccount(scaffoldState)
            BottomSheetScreen.ScreenChangeAccount -> ScreenChangeAccount(onCloseBottomSheet)
        }
    }
}