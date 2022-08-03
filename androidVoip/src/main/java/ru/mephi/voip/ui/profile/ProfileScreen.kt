package ru.mephi.voip.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject
import ru.mephi.voip.R
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.home.screens.catalog.CatalogViewModel
import ru.mephi.voip.ui.profile.bottomsheet.BottomSheetScreen
import ru.mephi.voip.ui.profile.bottomsheet.BottomSheetShape
import ru.mephi.voip.ui.profile.bottomsheet.SheetLayout
import ru.mephi.voip.ui.profile.favourites.FavouriteContactsBoard
import ru.mephi.voip.utils.ColorGreen

//https://dev.to/davidibrahim/how-to-use-multiple-bottom-sheets-in-android-compose-382p
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun ProfileScreen(openSettings: () -> Unit) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    var currentBottomSheet: BottomSheetScreen? by remember { mutableStateOf(null) }

    val catalogViewModel: CatalogViewModel by inject()
    val profileViewModel: ProfileViewModel by inject()

    val accountRepository: AccountStatusRepository by inject()
    val accountsCount by accountRepository.accountsCount.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current

    if (scaffoldState.bottomSheetState.isCollapsed)
        currentBottomSheet = null

    // to set the current sheet to null when the bottom sheet closes
    if (scaffoldState.bottomSheetState.isCollapsed)
        currentBottomSheet = null

    val keyboardController = LocalSoftwareKeyboardController.current

    val closeSheet: () -> Unit = {
        scope.launch {
            keyboardController?.hide()
            scaffoldState.bottomSheetState.collapse()
        }
    }

    val openSheet: (BottomSheetScreen) -> Unit = {
        scope.launch {
            currentBottomSheet = it
            scaffoldState.bottomSheetState.expand()
        }
    }

    val fillMaxWidthModifier = Modifier.fillMaxWidth()

    BottomSheetScaffold(
        sheetElevation = 20.dp,
        sheetPeekHeight = 0.dp,
        scaffoldState = scaffoldState,
        sheetShape = BottomSheetShape,
        sheetContent = {
            currentBottomSheet?.let { currentSheet ->
                SheetLayout(currentSheet, closeSheet, scaffoldState)
            }
        },
        topBar = { ProfileTopBar(openSettings) },
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            Column(Modifier.padding(horizontal = 20.dp).fillMaxHeight()) {
                ProfileContent(modifier = fillMaxWidthModifier)
                FavouriteContactsBoard(
                    modifier = fillMaxWidthModifier,
                    profileViewModel = profileViewModel,
                    accountStatusRepository = accountRepository
                )
                Column(modifier = fillMaxWidthModifier, verticalArrangement = Arrangement.Bottom) {
                    if (accountsCount > 0) ExtendedFloatingActionButton(icon = {
                        Icon(
                            Icons.Filled.Edit, null, tint = Color.White
                        )
                    },
                        text = {
                            Text(
                                text = stringResource(R.string.change_account) + " (${accountsCount})",
                                color = Color.White
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(16.dp, 16.dp, 0.dp, 0.dp),
                        backgroundColor = ColorGreen,
                        onClick = {
                            openSheet(BottomSheetScreen.ScreenChangeAccount)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        })
                    ExtendedFloatingActionButton(icon = {
                        Icon(
                            Icons.Filled.Add,
                            null,
                            tint = Color.White
                        )
                    },
                        text = {
                            Text(
                                text = stringResource(R.string.add_new_account), color = Color.White
                            )
                        },
                        backgroundColor = ColorGreen,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(16.dp, 8.dp, 0.dp, 16.dp),
                        onClick = {
                            openSheet(BottomSheetScreen.ScreenAddNewAccount)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        })
                }
            }
        }
    }
}