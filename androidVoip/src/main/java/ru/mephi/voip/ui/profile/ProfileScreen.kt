package ru.mephi.voip.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

//https://dev.to/davidibrahim/how-to-use-multiple-bottom-sheets-in-android-compose-382p
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun ProfileScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    var currentBottomSheet: BottomSheetScreen? by remember { mutableStateOf(null) }

    if (scaffoldState.bottomSheetState.isCollapsed)
        currentBottomSheet = null

    // to set the current sheet to null when the bottom sheet closes
    if (scaffoldState.bottomSheetState.isCollapsed)
        currentBottomSheet = null

    val closeSheet: () -> Unit = {
        scope.launch {
            scaffoldState.bottomSheetState.collapse()
        }
    }

    val openSheet: (BottomSheetScreen) -> Unit = {
        scope.launch {
            currentBottomSheet = it
            scaffoldState.bottomSheetState.expand()
        }
    }

    BottomSheetScaffold(
        sheetElevation = 20.dp,
        sheetPeekHeight = 0.dp, scaffoldState = scaffoldState,
        sheetShape = BottomSheetShape,
        sheetContent = {
            currentBottomSheet?.let { currentSheet ->
                SheetLayout(currentSheet, closeSheet, scaffoldState)
            }
        }) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            MainContent(openSheet, navController)
        }
    }
}