package com.example.adbpurrytify.ui.components

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothAudio
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import com.example.adbpurrytify.ui.screens.globalPlayer
import java.lang.Thread.sleep

//import kotlinx.coroutines.withContext


@androidx.annotation.OptIn(UnstableApi::class)
fun onDeviceSelected(device: AudioDeviceInfo?, context: Context) {
    if (globalPlayer.player == null) {
        Log.d("GLOBAL PLAYER", "BELUM DI INSTANTIATE")
        return
    }

    if (device == null) {
        globalPlayer.player?.setPreferredAudioDevice(null)
        globalPlayer.userSelectedAudioDeviceId = -1
        return
    }

    val audioManager = ContextCompat.getSystemService(context, AudioManager::class.java)
    val audioDevices = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS)?.toList().orEmpty()

    val isDeviceStillConnected = device == null || audioDevices.any { it.id == device.id }

    if (isDeviceStillConnected) {
        globalPlayer.player?.pause()
        globalPlayer.player?.setPreferredAudioDevice(device)
        sleep(250L) // ya
        globalPlayer.player?.play()
        globalPlayer.userSelectedAudioDeviceId = device!!.id
    } else {
        Toast.makeText(
            context,
            "Banh udah banh...",
            Toast.LENGTH_SHORT
        ).show()
        globalPlayer.player?.setPreferredAudioDevice(null)
        globalPlayer.userSelectedAudioDeviceId = -1
    }
}


@Composable
fun AudioDeviceSelect()
{

    val appContext = LocalContext.current.applicationContext
    val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

    var showDialog by remember { mutableStateOf(false) }

    val filteredDevices = audioDevices.filter { device ->
        when (device.type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> true
            else -> false
        }
    }

    fun getDisplayName(device: AudioDeviceInfo): String {
        return when (device.type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Phone Speaker"
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired Headphones"
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth Device"
            else -> "Other Device"
        }
    }
    fun getDeviceIcon(device: AudioDeviceInfo?): ImageVector {
        if (device == null) return Icons.Default.DeviceUnknown
        return when (device.type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> Icons.Default.Speaker
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> Icons.Default.Headset
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> Icons.Default.BluetoothAudio
            else -> Icons.Default.DeviceUnknown
        }
    }
    val selectedDevice = audioDevices.find { it.id == globalPlayer.userSelectedAudioDeviceId }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(4.dp)
    ) {
        IconButton(
            onClick = { showDialog = true },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = getDeviceIcon(selectedDevice),
                contentDescription = "Select Audio Device",
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = "Using: ${selectedDevice?.let { getDisplayName(it) } ?: "Default Audio Device"}",
            fontSize = 12.sp, // slightly increased for legibility
            modifier = Modifier.padding(start = 8.dp)
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Audio Device") },
            text = {
                Column {
                    filteredDevices.forEach { device ->
                        val isSelected = device.id == selectedDevice?.id
                        Text(
                            text = getDisplayName(device) + if (isSelected) " (current)" else "",
                            color = if (isSelected) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDeviceSelected(device, appContext)
                                    showDialog = false
                                }
                                .padding(8.dp)
                        )
                    }
                    val isDefault = selectedDevice == null
                    Text(
                        text = "Default" + if (isDefault) " (current)" else "",
                        color = if (isDefault) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDeviceSelected(null, appContext)
                                showDialog = false
                            }
                            .padding(8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

}

