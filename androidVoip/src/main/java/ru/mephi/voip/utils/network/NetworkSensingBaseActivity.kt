package ru.mephi.voip.utils.network

import android.view.View
import androidx.appcompat.app.AppCompatActivity

// https://betterprogramming.pub/how-to-monitor-internet-connection-in-android-using-kotlin-and-livedata-135de9447796
open class NetworkSensingBaseActivity : AppCompatActivity() {
//    private var snackBar: CustomSnackBar? = null
    private lateinit var parentView: View
    private lateinit var networkStatusHelper: NetworkStatusHelper

    override fun onResume() {
        super.onResume()
//        parentView = findViewById(R.id.main_container)

        networkStatusHelper = NetworkStatusHelper(this)
        networkStatusHelper.observe(this) {
            when (it) {
                NetworkStatus.Available -> runOnUiThread {
//                    snackBar?.dismiss()
                }
                NetworkStatus.Unavailable -> runOnUiThread {
//                    snackBar?.show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
//        snackBar?.dismiss()
//        snackBar = null
    }
}