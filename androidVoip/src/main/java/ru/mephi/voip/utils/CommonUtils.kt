package ru.mephi.voip.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import org.abtollc.sdk.AbtoPhone
import timber.log.Timber

var PACKAGE_NAME: String? = null
var ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 2323

fun String.isLetters(): Boolean {
    return this.matches("^[a-zA-Zа-яА-Я ]*$".toRegex())
}

fun AbtoPhone.getCurrentUserNumber() = when {
    config.accountsCount == 0 -> null
    config.getAccount(currentAccountId).active ->
        config.getAccount(currentAccountId)?.sipUserName
    else -> null
}

fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if (capabilities != null) {
        when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                Timber.tag("Internet").i("NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                Timber.tag("Internet").i("NetworkCapabilities.TRANSPORT_WIFI")
                return true
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                Timber.tag("Internet").i("NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
    }
    return false
}