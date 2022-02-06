package ru.mephi.voip.ui.utils

import android.app.Activity
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

fun showSnackBar(rootView: View, text: String) {
    val snackBar = CustomSnackBar(rootView, text, Snackbar.LENGTH_SHORT)
    snackBar.show()
}

fun Activity.hideKeyboard() {
    val view = currentFocus
    if (view != null) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(
            view.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}

fun Context.toast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Fragment.toast(message: CharSequence?) =
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

fun Context.launchMailClientIntent(email: String) {
    val emailIntent = Intent(Intent.ACTION_SEND)
    emailIntent.data = Uri.parse("mailto:$email")
    startActivity(Intent.createChooser(emailIntent, "Отправить сообщение по почте через..."))
}

fun Context.launchDialer(number: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(number)))
    startActivity(intent)
}