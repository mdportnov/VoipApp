package ru.mephi.voip.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.Fragment

fun Context.toast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Fragment.toast(message: CharSequence?) =
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

fun Context.launchMailClientIntent(email: String) {
    val emailIntent = Intent(Intent.ACTION_SEND)
    emailIntent.data = Uri.parse("mailto:$email")
    startActivity(
        Intent.createChooser(emailIntent, "Отправить сообщение по почте через...")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

fun Context.launchDialer(number: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(number)))
    startActivity(intent)
}