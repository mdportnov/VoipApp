package ru.mephi.voip.utils

import android.R.attr.action
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import timber.log.Timber


class AudioUtils(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun setBluetoothMode(enabled: Boolean): Boolean {
        return when {
            !isBluetoothDeviceReady() -> false
            enabled -> {
                audioManager.isSpeakerphoneOn = false
                audioManager.startBluetoothSco()
                audioManager.isBluetoothScoOn = true
                true
            }
            else -> {
                setDefaultAudioMode()
                false
            }
        }
    }

    fun setSpeakerMode(enabled: Boolean): Boolean {
        return if (enabled) {
            audioManager.stopBluetoothSco()
            audioManager.isBluetoothScoOn = false
            audioManager.isSpeakerphoneOn = true
            true
        } else {
            setDefaultAudioMode()
            false
        }
    }

    fun setDefaultAudioMode() {
        audioManager.stopBluetoothSco()
        audioManager.isBluetoothScoOn = false
        audioManager.isSpeakerphoneOn = false
    }

    fun isBluetoothDeviceReady(
        expectedReady: Boolean? = null
    ): Boolean {
        Timber.e("BluetoothReceiver: $expectedReady")
        var isReady = false
        for (i in 0..10) {
            isReady = with(audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
                any { it?.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP }
            }
            Timber.e("BluetoothReceiver: isReady=$isReady, i=$i")
            when {
                expectedReady == null -> return isReady
                isReady != expectedReady -> { runBlocking { delay(250) } }
                else -> return isReady
            }
        }
        return isReady
    }
}

class BluetoothReceiver(
    private val context: Context,
    private val audioUtils: AudioUtils
) : BroadcastReceiver() {
    private var mIsListening = false

    private lateinit var setBluetoothState: (Boolean) -> Unit

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> sendBluetoothState(true)
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> sendBluetoothState(false)
        }
    }

    private fun sendBluetoothState(
        expectedReady: Boolean? = null
    ) {
        if (::setBluetoothState.isInitialized) {
            setBluetoothState(audioUtils.isBluetoothDeviceReady(expectedReady))
        }
    }

    fun enable(updateValue: (Boolean) -> Unit) {
        if (mIsListening) return
        Timber.d("BluetoothReceiver: enabling!")
        setBluetoothState = updateValue
        sendBluetoothState(null)
        context.registerReceiver(this, BluetoothUtils.intentFilter)
        mIsListening = true
    }

    fun disable() {
        if (!mIsListening) return
        Timber.d("BluetoothReceiver: disabling!")
        context.unregisterReceiver(this)
        mIsListening = false
    }

}

private object BluetoothUtils {
    val intentFilter by lazy {
        IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
    }
}