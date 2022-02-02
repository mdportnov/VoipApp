package ru.mephi.voip.data.network

import android.annotation.SuppressLint
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import ru.mephi.voip.R
import ru.mephi.voip.ui.utils.CustomSnackBar

@SuppressLint("Registered")
open class NetworkSensingBaseActivity : AppCompatActivity(),
    ConnectionStateMonitor.OnNetworkAvailableCallbacks {
    private var snackBar: CustomSnackBar? = null
    private var connectionStateMonitor: ConnectionStateMonitor? = null
    private lateinit var parentView: View

    override fun onResume() {
        super.onResume()
        parentView = findViewById(R.id.main_container)

        if (snackBar == null)
            snackBar = CustomSnackBar(
                parentView,
                getString(R.string.connection_lost),
                Snackbar.LENGTH_INDEFINITE
            )

        if (connectionStateMonitor == null)
            connectionStateMonitor = ConnectionStateMonitor(this, this)
        connectionStateMonitor?.enable()        //Register

        // Recheck network status manually whenever activity resumes
        if (connectionStateMonitor?.hasNetworkConnection() == false) onNegative()
        else onPositive()
    }

    override fun onPause() {
        snackBar?.dismiss()
        snackBar = null
        connectionStateMonitor?.disable()
        connectionStateMonitor = null
        super.onPause()
    }

    override fun onPositive() {
        runOnUiThread {
            snackBar?.dismiss()
        }
    }

    override fun onNegative() {
        runOnUiThread {
            snackBar?.show()
        }
    }
}