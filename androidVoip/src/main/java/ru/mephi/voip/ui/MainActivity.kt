package ru.mephi.voip.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import ru.mephi.voip.BuildConfig
import ru.mephi.voip.R
import ru.mephi.voip.call.SipBackgroundService
import ru.mephi.voip.data.network.NetworkSensingBaseActivity
import ru.mephi.voip.databinding.ActivityMainBinding
import ru.mephi.voip.di.viewModels
import ru.mephi.voip.ui.utils.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE
import ru.mephi.voip.ui.utils.PACKAGE_NAME
import ru.mephi.voip.ui.utils.setupWithNavController
import timber.log.Timber

class MainActivity : NetworkSensingBaseActivity(), EasyPermissions.PermissionCallbacks {
    lateinit var binding: ActivityMainBinding

    private val sharedVM: SharedViewModel by viewModel()

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var currentNavController: LiveData<NavController>? = null

    var sipService: SipBackgroundService? = null

    companion object {
        private const val REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Теперь, когда BottomNavigationBar восстановил свое состояние экземпляра.
        // и его selectItemId, мы можем приступить к настройке
        // BottomNavigationBar с навигацией
        setupBottomNavigationBar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadKoinModules(viewModels)

        firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)

        if (!hasPermissions())
            requestPermissions()

//        checkPermissionOverlay()
        val serviceStartIntent = Intent(this, SipBackgroundService::class.java)

        sharedVM.mBinder.observe(this) { binder ->
            sipService = if (binder != null) {
                Timber.d("onChanged: connected to service")
                binder.getService()
            } else {
                Timber.d("onChanged: unbound from service")
                null
            }
        }

        startService(serviceStartIntent)
        bindService(serviceStartIntent, sharedVM.serviceConnection, Context.BIND_AUTO_CREATE)

        if (savedInstanceState == null)
            setupBottomNavigationBar()
    }

    override fun onStop() {
        super.onStop()

        if (sharedVM.mBinder.value != null) {
            unbindService(sharedVM.serviceConnection)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadKoinModules(viewModels)
    }

    private fun setupBottomNavigationBar() {
        val navGraphIds = listOf(
            R.navigation.calls,
            R.navigation.catalog,
            R.navigation.profile
        )

        val navController = binding.bottomNav.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_container,
            intent = intent
        )

        currentNavController = navController
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    fun hasPermissions(): Boolean =
        EasyPermissions.hasPermissions(
            this,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.USE_SIP
        )

    fun requestPermissions() {
        EasyPermissions.requestPermissions(
            this,
            "Это приложение требует разрешение на совершение звонков и использование микрофона",
            REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.USE_SIP
        )
    }

    private fun checkPermissionOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$PACKAGE_NAME")
            )
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this@MainActivity, perms)) {
            SettingsDialog.Builder(this)
                .title("Запрос разрешений для SIP-звонков")
                .rationale("Это приложение требует разрешения на совершение звонков и использование микрофона")
                .negativeButtonText("Закрыть")
                .positiveButtonText("Предоставить")
                .build().show()
        } else
            requestPermissions()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}