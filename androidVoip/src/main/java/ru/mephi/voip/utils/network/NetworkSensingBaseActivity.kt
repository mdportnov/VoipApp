package ru.mephi.voip.utils.network

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import ru.mephi.voip.R
import ru.mephi.voip.utils.CustomSnackBar

open class NetworkSensingBaseActivity : AppCompatActivity() {
    private var snackBar: CustomSnackBar? = null
    private lateinit var parentView: View
    private lateinit var networkStatusHelper: NetworkStatusHelper

    override fun onResume() {
        super.onResume()
        parentView = findViewById(R.id.main_container)

        networkStatusHelper = NetworkStatusHelper(this)
        networkStatusHelper.observe(this) {
            when (it) {
                NetworkStatus.Available ->
                    runOnUiThread {
                        snackBar?.dismiss()
                    }
                NetworkStatus.Unavailable -> runOnUiThread {
                    snackBar?.show()
                }
            }
        }

        if (snackBar == null)
            snackBar = CustomSnackBar(
                parentView,
                getString(R.string.connection_lost),
                Snackbar.LENGTH_INDEFINITE
            )
    }

    override fun onPause() {
        super.onPause()
        snackBar?.dismiss()
        snackBar = null
    }
}