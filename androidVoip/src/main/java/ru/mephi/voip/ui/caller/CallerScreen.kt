package ru.mephi.voip.ui.caller

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
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
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.call.CallActivity
import ru.mephi.voip.ui.caller.list.CallRecordsList
import ru.mephi.voip.ui.caller.list.NumberHistoryList
import ru.mephi.voip.ui.caller.numpad.NumPad
import ru.mephi.voip.utils.ColorGray
import ru.mephi.voip.utils.ColorGreen
import ru.mephi.voip.utils.ColorRed
import timber.log.Timber

@Composable
fun AccountStatusWidget(accountStatusRepository: AccountStatusRepository, modifier: Modifier) {
    val status = accountStatusRepository.status.collectAsState()

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Text(text = status.value.status, modifier = Modifier.padding(end = 5.dp))
        Canvas(modifier = Modifier
            .padding(end = 5.dp)
            .size(15.dp),
            onDraw = {
                drawCircle(
                    color = when (status.value) {
                        AccountStatus.REGISTERED -> ColorGreen
                        AccountStatus.NO_CONNECTION, AccountStatus.CHANGING, AccountStatus.LOADING -> ColorGray
                        AccountStatus.UNREGISTERED, AccountStatus.REGISTRATION_FAILED -> ColorRed
                    }
                )
            })
    }
}

@Composable
fun CallerScreen(
    isPermissionGranted: Boolean,
    navController: NavController,
    args: CallerFragmentArgs,
) {
    val accountStatusRepository: AccountStatusRepository by inject()
    val viewModel: CallerViewModel by inject()

    var inputState by remember { mutableStateOf("") }
    var isNumPadStateUp by remember { mutableStateOf(false) }
    var painter by remember { mutableStateOf(R.drawable.logo_mephi) }
    val callerNumber by remember { mutableStateOf(args.callerNumber) }
    val snackBarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    BackHandler {
        navController.popBackStack()
    }

    if (!callerNumber.isNullOrEmpty()) {
        if (!inputState.contains(callerNumber!!) && !callerNumber!!.contains(inputState) || inputState.isEmpty())
            inputState = args.callerNumber!!
        painter = R.drawable.ic_baseline_arrow_back_24
        isNumPadStateUp = true
    } else
        painter = R.drawable.logo_mephi

    Column {
        TopAppBar(
            backgroundColor = Color.White,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart),
                ) {
                    Image(
                        painter = painterResource(id = painter),
                        contentDescription = "лого",
                    )
                }

                Text(
                    text = "Звонки", style = TextStyle(color = Color.Black, fontSize = 20.sp),
                    modifier = Modifier.align(Alignment.Center)
                )
                AccountStatusWidget(
                    accountStatusRepository,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }

        val scope = rememberCoroutineScope()

        Box(modifier = Modifier.fillMaxSize()) {
            val (selectedRecord, setSelectedRecord) = remember {
                mutableStateOf<CallRecord?>(null)
            }
            Column(modifier = Modifier.fillMaxSize()) {
                StatusBar()
                CallRecordsList(setSelectedRecord) { deletedRecord ->
                    scope.launch {
                        snackBarHostState.currentSnackbarData?.dismiss()
                        val snackBarResult = snackBarHostState.showSnackbar(
                            "Запись ${deletedRecord.sipNumber} удалена",
                            actionLabel = "Вернуть"
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
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                NumPad(
                    11, inputState, isNumPadStateUp,
                    onLimitExceeded = {
                        scope.launch {
                            snackBarHostState.showSnackbar("Превышен размер номера")
                        }
                    },
                    onNumPadStateChange = {
                        if (!args.callerNumber.isNullOrEmpty()) {
                            navController.popBackStack()
                        } else
                            isNumPadStateUp = !isNumPadStateUp
                    },
                    onInputStateChanged = {
                        inputState = it
                    }
                )
                args.callerName?.let {
                    AnimatedVisibility(
                        visible = inputState == args.callerNumber,
                        enter = slideInVertically() + expandVertically()
                                + fadeIn(initialAlpha = 0.3f),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        OutlinedButton(
                            onClick = {}, modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                colorResource(id = R.color.colorGreen)
                            ),
                            elevation = ButtonDefaults.elevation()
                        ) {
                            Text(text = it, color = Color.White)
                        }
                    }
                }
                FloatingActionButton(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.End), onClick = {
                        if (isPermissionGranted)
                            if (isNumPadStateUp) {
                                if (inputState.length <= 3) {
                                    scope.launch {
                                        snackBarHostState.showSnackbar("Слишком короткий номер")
                                    }
                                    return@FloatingActionButton
                                }
                                if (accountStatusRepository.status.value == AccountStatus.REGISTERED) {
                                    CallActivity.create(
                                        context,
                                        inputState,
                                        false
                                    )
                                } else {
//                                showSnackBar(
//                                    binding.root,
//                                    "Нет активного аккаунта для совершения звонка"
//                                )
                                }
                            } else {
                                isNumPadStateUp = !isNumPadStateUp
                            }
                    }, backgroundColor = colorResource(id = R.color.colorGreen)
                ) {
                    if (isNumPadStateUp)
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "",
                            tint = colorResource(
                                id = if (isPermissionGranted)
                                    R.color.colorPrimary else R.color.colorGray
                            )
                        ) else
                        Icon(
                            painterResource(id = R.drawable.ic_baseline_dialpad_24),
                            contentDescription = "",
                            tint = colorResource(
                                id = if (isPermissionGranted)
                                    R.color.colorPrimary else R.color.colorGray
                            )
                        )
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visibleState = MutableTransitionState(selectedRecord != null),
                enter = slideInVertically(),
                exit = slideOutVertically()
            ) {
                NumberHistoryList(
                    callRecord = selectedRecord!!,
                    callsHistory = viewModel.getAllCallsBySipNumber(selectedRecord.sipNumber)
                        .executeAsList(),
                    setSelectedRecord
                )
            }
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )
        }
    }
}