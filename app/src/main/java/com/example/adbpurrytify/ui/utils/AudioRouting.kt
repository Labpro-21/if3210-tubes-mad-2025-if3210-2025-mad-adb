package com.example.adbpurrytify.ui.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import kotlinx.coroutines.flow.MutableStateFlow

class HeadsetStateProvider(
    private val context: Context,
    private val audioManager: AudioManager
) {
    // The current state of wired headies; true means enabled
    val isHeadsetPlugged = MutableStateFlow(getHeadsetState())
    // Create BroadcastReceiver to track the headset connection and disconnection events
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == AudioManager.ACTION_HEADSET_PLUG) {
                when (intent.getIntExtra("state", -1)) {
                    // 0 -- the headset is offline, 1 -- the headset is online
                    0 -> isHeadsetPlugged.value = false
                    1 -> isHeadsetPlugged.value = true
                }
            }
        }
    }
    init {
        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        // РRegister our BroadcastReceiver


        context.registerReceiver(receiver, filter)
    }
    // The method to receive a current headset state. It's used to initialize the starting point.
    fun getHeadsetState(): Boolean {
        val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        return audioDevices.any {
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                    || it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
        }
    }
}