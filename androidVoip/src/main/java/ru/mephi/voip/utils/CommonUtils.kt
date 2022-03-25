package ru.mephi.voip.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import org.abtollc.sdk.AbtoPhone
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

var PACKAGE_NAME: String? = null
var ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 2323

fun String.isLetters(): Boolean {
    return this.matches("^[a-zA-Zа-яА-Я ]*$".toRegex())
}


fun Long.stringFromDate(): String {
    val dtfCustom: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
    val dateTime = LocalDateTime.ofEpochSecond(this, 0, ZoneOffset.of("+03:00"))
    return dateTime.format(dtfCustom)
}

fun Long.durationStringFromMillis() =
    if (TimeUnit.MILLISECONDS.toHours(this) != 0L) String.format(
        "%d ч %02d мин %02d сек",
        TimeUnit.MILLISECONDS.toHours(this),
        TimeUnit.MILLISECONDS.toMinutes(this) - TimeUnit.HOURS.toMinutes(
            TimeUnit.MILLISECONDS.toHours(this)
        ),
        TimeUnit.MILLISECONDS.toSeconds(this) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this))
    ) else
        String.format(
            "%02d мин %02d сек",
            TimeUnit.MILLISECONDS.toMinutes(this) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(this)
            ),
            TimeUnit.MILLISECONDS.toSeconds(this) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this))
        )

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