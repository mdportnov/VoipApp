package ru.mephi.voip.ui.caller

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.shared.vm.CallerViewModel
import ru.mephi.voip.R
import ru.mephi.voip.data.PhoneManager
import ru.mephi.voip.ui.MasterActivity
import ru.mephi.voip.ui.call.CallActivity
import ru.mephi.voip.ui.screens.history.list.CallRecordsList
import ru.mephi.voip.ui.screens.history.list.NumberHistoryList
import ru.mephi.voip.ui.components.numpad.NumPad
import timber.log.Timber

@Composable
fun CallerScreen(
    isPermissionGranted: Boolean = false,
    navController: NavController,
    callerNumberArg: String? = null,
    callerNameArg: String? = null
) {
    val phoneManager: PhoneManager by inject()
    val viewModel: CallerViewModel by inject()

    var isNumPadStateUp by remember { mutableStateOf(false) }
    var isBackArrowVisible by remember { mutableStateOf(false) }
    val callerNumber by remember { mutableStateOf(callerNumberArg) }
    val callerName by remember { mutableStateOf(callerNameArg) }

    var inputState by remember { mutableStateOf(if (callerNumber.isNullOrEmpty()) "" else callerNumber!!) }

    val context = LocalContext.current
    val activity = (context as? Activity)

    val scope = rememberCoroutineScope()

    val (selectedRecord, setSelectedRecord) = remember {
        mutableStateOf<CallRecord?>(null)
    }

    if (callerNumber.isNullOrEmpty()) {
        isBackArrowVisible = false
    } else {
        isBackArrowVisible = true
        isNumPadStateUp = true
    }

    fun onNumPadStateChange() {
        if (callerNumber.isNullOrEmpty()) isNumPadStateUp = !isNumPadStateUp
        else navController.popBackStack()
    }

    BackHandler {
        when {
            selectedRecord != null -> setSelectedRecord(null)
            isNumPadStateUp -> onNumPadStateChange()
            else -> activity?.finish()
        }
    }

    val scaffoldState = rememberScaffoldState()

    Scaffold(topBar = {
        TopAppBar(
            backgroundColor = Color.White, modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isBackArrowVisible) IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                } else Image(
                    painter = painterResource(id = R.drawable.logo_mephi),
                    contentDescription = "лого",
                )

                Text(
                    text = "Звонки", style = TextStyle(color = Color.Black, fontSize = 20.sp),
                )
            }
        }
    }, scaffoldState = scaffoldState) {
        Box(modifier = Modifier.padding(it)) { }
        val list = viewModel.getAllCallsBySipNumber(inputState).executeAsList()

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                CallStatusBar()
                CallRecordsList(setSelectedRecord) { deletedRecord ->
                    scope.launch {
                        scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                        val snackBarResult = scaffoldState.snackbarHostState.showSnackbar(
                            "Запись ${deletedRecord.sipNumber} удалена", actionLabel = "Вернуть"
                        )
                        when (snackBarResult) {
                            SnackbarResult.Dismissed -> Timber.d("SnackBar dismissed")
                            SnackbarResult.ActionPerformed -> viewModel.addRecord(
                                deletedRecord
                            )
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom
            ) {
                if (callerName != null) {
                    AnimatedVisibility(
                        visible = inputState == callerNumber && inputState.isNotEmpty(),
                        enter = slideInVertically() + expandVertically() + fadeIn(initialAlpha = 0.3f),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                colorResource(id = R.color.colorGreen)
                            ),
                            elevation = ButtonDefaults.elevation()
                        ) {
                            Text(text = callerName!!, color = Color.White)
                        }
                    }
                } else {
                    AnimatedVisibility(
                        visible = list.map { it.sipNumber }.contains(inputState),
                        enter = slideInVertically() + expandVertically() + fadeIn(initialAlpha = 0.3f),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        val index = list.map { it.sipNumber }.indexOf(inputState)
                        if (index > 0 && !list[index].sipName.isNullOrEmpty()) OutlinedButton(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                colorResource(id = R.color.colorGreen)
                            ),
                            elevation = ButtonDefaults.elevation()
                        ) {
                            Text(text = list[index].sipName!!, color = Color.White)
                        }
                    }
                }

                NumPad(11, inputState, isNumPadStateUp, onLimitExceeded = {
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar("Превышен размер номера")
                    }
                }, onNumPadStateChange = {
                    onNumPadStateChange()
                }, onInputStateChanged = {
                    inputState = it
                })
                FloatingActionButton(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.End), onClick = {
                        if (isPermissionGranted) if (isNumPadStateUp) {
                            if (inputState.length <= 3) {
                                scope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar("Слишком короткий номер")
                                }
                                return@FloatingActionButton
                            }
                            if (phoneManager.phoneStatus.value == AccountStatus.REGISTERED) {
                                CallActivity.create(
                                    context, inputState, false
                                )
                            } else {
                                scope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar("Нет активного аккаунта для совершения звонка")
                                }
                            }
                        } else {
                            isNumPadStateUp = !isNumPadStateUp
                        }
                        else {
                            (context as MasterActivity).checkNonGrantedPermissions()
                        }
                    }, backgroundColor = colorResource(id = R.color.colorGreen)
                ) {
                    if (isNumPadStateUp) Icon(
                        Icons.Default.Call, contentDescription = "", tint = colorResource(
                            id = if (isPermissionGranted) R.color.colorPrimary else R.color.colorGray
                        )
                    ) else Icon(
                        Icons.Default.Dialpad, contentDescription = null, tint = colorResource(
                            id = if (isPermissionGranted) R.color.colorPrimary else R.color.colorGray
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = selectedRecord != null,
                enter = slideInVertically() + expandVertically(),
                exit = slideOutVertically() + shrinkVertically()
            ) {
                NumberHistoryList(
                    selectedRecord = selectedRecord, setSelectedRecord = setSelectedRecord
                )
            }
        }
    }
}