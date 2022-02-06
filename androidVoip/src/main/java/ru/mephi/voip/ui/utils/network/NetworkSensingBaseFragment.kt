package ru.mephi.voip.ui.utils.network

import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import ru.mephi.voip.R
import ru.mephi.voip.ui.utils.CustomSnackBar

@SuppressLint("Registered")
open class NetworkSensingBaseFragment : Fragment(),
    ConnectionStateMonitor.OnNetworkAvailableCallbacks {

    private var snackBar: CustomSnackBar? = null
    private var connectionStateMonitor: ConnectionStateMonitor? = null
    private lateinit var parentView: View

    override fun onResume() {
        super.onResume()
        parentView = requireActivity().findViewById(R.id.main_container)
        if (snackBar == null)
            snackBar = CustomSnackBar(
                parentView,
                getString(R.string.connection_lost),
                Snackbar.LENGTH_INDEFINITE
            )

        if (connectionStateMonitor == null)
            connectionStateMonitor = ConnectionStateMonitor(requireActivity(), this)

        connectionStateMonitor?.enable()  //Register

        // Recheck network status manually whenever activity resumes
        if (connectionStateMonitor?.hasNetworkConnection() == false) onNegative()
        else onPositive()
    }

    override fun onPause() {
        snackBar = null
        connectionStateMonitor?.disable() //Unregister
        connectionStateMonitor = null
        super.onPause()
    }

    override fun onPositive() {
        requireActivity().runOnUiThread {
            snackBar?.dismiss()
        }
    }

    override fun onNegative() {
        requireActivity().runOnUiThread {
            snackBar?.show()
        }
    }
}