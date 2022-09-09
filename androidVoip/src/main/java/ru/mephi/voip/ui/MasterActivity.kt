@file:OptIn(ExperimentalAnimationApi::class)

package ru.mephi.voip.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.abtollc.sdk.AbtoApplication
import org.abtollc.sdk.AbtoPhone
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import ru.mephi.shared.utils.appContext
import ru.mephi.shared.vm.LogType
import ru.mephi.shared.vm.LoggerViewModel
import ru.mephi.shared.vm.UserNotifierViewModel
import ru.mephi.voip.BuildConfig
import ru.mephi.voip.data.PhoneManager
import ru.mephi.voip.ui.home.HomeScreen
import ru.mephi.voip.ui.login.LoginScreen
import ru.mephi.voip.ui.settings.SettingsScreen
import ru.mephi.voip.ui.theme.MasterTheme
import ru.mephi.voip.utils.NotificationHandler
import timber.log.Timber


class MasterActivity : AppCompatActivity(), KoinComponent {

    private val requiredPermission = listOf(Manifest.permission.USE_SIP, Manifest.permission.RECORD_AUDIO)
    var isPermissionsGranted = false

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val phoneManager: PhoneManager by inject()

    private var isBackgroundWork: Boolean = false
    private var isSipEnabled = false

    companion object {
        var phone: AbtoPhone = (appContext as AbtoApplication).abtoPhone
    }

    override fun onResume() {
        super.onResume()
        phone = (application as AbtoApplication).abtoPhone
        if (!isBackgroundWork) {
            if (isSipEnabled) {
                phoneManager.initPhone()
            }
        }
    }

    private var scaffoldState: ScaffoldState? = null

    init {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val lVM: LoggerViewModel = get()
                lVM.log.collectLatest {
                    if (it.logMessage.isEmpty()) return@collectLatest
                    when(it.logType) {
                        LogType.VERBOSE -> Timber.v(it.logMessage)
                        LogType.DEBUG -> Timber.d(it.logMessage)
                        LogType.INFO -> Timber.i(it.logMessage)
                        LogType.WARNING -> Timber.w(it.logMessage)
                        LogType.ERROR -> Timber.e(it.logMessage)
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val unVM: UserNotifierViewModel = get()
                unVM.notifyMsg.collectLatest {
                    if (it.isNotEmpty()) {
                        Toast.makeText(this@MasterActivity, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requiredPermission.filter { p -> !isPermissionGranted(p) }.let { if (it.isEmpty()) isPermissionsGranted = true }

        setContent {
            scaffoldState = rememberScaffoldState()
            MasterTheme {
                MasterNavCtl()
            }
        }

        lifecycleScope.launch {
            phoneManager.isSipEnabled.collect {
                isSipEnabled = it
            }
        }

        lifecycleScope.launch {
            phoneManager.isBackgroundWork.collect {
                isBackgroundWork = it
            }
        }

        firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)

        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)

        checkNonGrantedPermissions()
    }

    fun checkNonGrantedPermissions(
        permissions: List<String> = requiredPermission
    ): Boolean {
        permissions.filter { p -> !isPermissionGranted(p) }.let {
            if (it.isEmpty()) return true else {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(150)
                    showPermissionsRequestDialog(it)
                }
                return false
            }
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return this.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        checkNonGrantedPermissions(permissions.filter { p ->
            grantResults[permissions.indexOf(p)] != PackageManager.PERMISSION_GRANTED
        })
    }

    private fun showPermissionsRequestDialog(
        permissions: List<String>
    ) {
        MaterialAlertDialogBuilder(this@MasterActivity).also {
            it.setTitle("Необходимые разрешения")
            if (permissions.contains(Manifest.permission.USE_SIP) && permissions.contains(Manifest.permission.RECORD_AUDIO)) {
                it.setMessage("Для полноценной работы приложения необходимо предоставить разрешения на совершения звонков и использования микрофона")
            } else if (permissions.contains(Manifest.permission.USE_SIP)) {
                it.setMessage("Для полноценной работы приложения необходимо предоставить разрешения на совершения звонков")
            } else if (permissions.contains(Manifest.permission.RECORD_AUDIO)) {
                it.setMessage("Для полноценной работы приложения необходимо предоставить разрешения на использования микрофона")
            } else {
                it.setMessage("Для полноценной работы приложения необходимо предоставить разрешения")
                Timber.e("dialog for ${permissions.joinToString(", ")} not implemented yet")
            }
            it.setNeutralButton("Отмена") { _, _ -> onRequestDialogCancellation() }
            it.setOnCancelListener { onRequestDialogCancellation() }
            it.setPositiveButton("Предоставить") { _, _ ->  this.requestPermissions(permissions.toTypedArray(), 0x1)}
        }.show()
    }

    private fun onRequestDialogCancellation() {
        Toast.makeText(this, "¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show()
    }

    @Composable
    private fun MasterNavCtl() {
        val navController = rememberAnimatedNavController()
        AnimatedNavHost(
            navController = navController,
            startDestination = MasterScreens.HomeScreen.route
        ) {
            composable(
                route = MasterScreens.HomeScreen.route
            ) {
                HomeScreen(
                    openLogin = { navController.navigate(route = MasterScreens.LoginScreen.route) },
                    openSettings = { navController.navigate(route = MasterScreens.SettingsScreen.route) }
                )
            }
            composable(
                route = MasterScreens.LoginScreen.route
            ) {
                LoginScreen(goBack = { navController.popBackStack() })
            }
            composable(
                route = MasterScreens.SettingsScreen.route
            ) {
                SettingsScreen(navController)
            }
        }
    }

}
