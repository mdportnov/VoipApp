package ru.mephi.voip.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import ru.mephi.voip.data.PhoneManager
import ru.mephi.voip.data.SettingsRepository
import ru.mephi.voip.vm.SettingsViewModel

class SplashActivity : AppCompatActivity(), KoinComponent {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().let {
            it.setKeepOnScreenCondition { true }
        }

        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenCreated {
//            inject<SettingsRepository>().let {
//
//            }

            inject<PhoneManager>().value
            val settingsVM by inject<SettingsViewModel>()
            while(!settingsVM.isScreenReady()) {
                delay(100L)
            }

            startActivity(Intent(this@SplashActivity, MasterActivity::class.java))
            finish()
        }
    }
}