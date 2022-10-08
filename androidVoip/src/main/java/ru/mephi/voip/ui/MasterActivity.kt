@file:OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)

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
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
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
import ru.mephi.shared.vm.*
import ru.mephi.voip.BuildConfig
import ru.mephi.voip.data.PhoneManager
import ru.mephi.voip.ui.caller.CallerScreen
import ru.mephi.voip.ui.screens.catalog.catalogNavCtl
import ru.mephi.voip.data.PreferenceRepository
import ru.mephi.voip.ui.screens.favourites.FavouritesScreen
import ru.mephi.voip.ui.screens.settings.SettingsScreen
import ru.mephi.voip.ui.theme.MasterTheme
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
                MasterScreen()
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
    private fun MasterScreen() {
        val navController = rememberAnimatedNavController()
        Scaffold(
            bottomBar = {
                MasterScreenBottomBar(navController = navController)
            }
        ) {
            Box(modifier = Modifier.padding(it)) {
                MasterNavCtl(navController)
            }
        }
    }

    @Composable
    private fun MasterScreenBottomBar(
        navController: NavHostController
    ) {
        NavigationBar {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            masterScreensList.forEach { item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = when (currentDestination?.hierarchy?.any { it.route == item.route }) {
                                true -> item.selectedIcon
                                false -> item.icon
                                else -> item.icon
                            },
                            contentDescription = stringResource(id = item.title)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(id = item.title),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun MasterNavCtl(
        navController: NavHostController,
        preferenceRepository: PreferenceRepository = get(),
        catalogVM: CatalogViewModel = get(),
        detailedInfoVM: DetailedInfoViewModel = get()
    ) {
        val startScreen by preferenceRepository.startScreen.collectAsState(initial = MasterScreens.Catalog)

        AnimatedNavHost(navController = navController, startDestination = startScreen.route) {
            composable(
                route = MasterScreens.History.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }
            ) {
                CallerScreen(
                    navController = navController,
                    isPermissionGranted = true
                )
            }
            composable(
                route = MasterScreens.Settings.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }
            ) {
                SettingsScreen()
            }
            composable(
                route = MasterScreens.Favourites.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }
            ) {
                FavouritesScreen()
            }
            catalogNavCtl(
                navController = navController,
                catalogVM = catalogVM,
                detailedInfoVM = detailedInfoVM
            )
        }
    }
}
