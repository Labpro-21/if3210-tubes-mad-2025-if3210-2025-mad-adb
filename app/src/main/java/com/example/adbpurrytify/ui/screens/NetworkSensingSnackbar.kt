package com.example.adbpurrytify.ui.screens

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.adbpurrytify.api.ConnectionStateMonitor
import com.example.adbpurrytify.api.ConnectionStateMonitor.OnNetworkAvailableCallbacks
import kotlinx.coroutines.launch

@Composable
fun NetworkSensingSnackbar(
    context: Context,
    snackbarHostState: SnackbarHostState
) {
    var isNetworkAvailable by rememberSaveable { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val networkMonitor = remember {
        ConnectionStateMonitor(context, object: OnNetworkAvailableCallbacks {
            override fun onPositive() {
                isNetworkAvailable = true
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
            }

            override fun onNegative() {
                isNetworkAvailable = false
                coroutineScope.launch {
                    if (snackbarHostState.currentSnackbarData == null) {
                        snackbarHostState.showSnackbar(
                            message = "No network connection",
                            duration = SnackbarDuration.Indefinite
                        )
                    }
                }
            }
        })
    }

    LaunchedEffect(true) {
        networkMonitor.enable()
    }

    DisposableEffect(true) {
        onDispose {
            networkMonitor.disable()
        }
    }
}

// Although this is a composable, no preview is made, need device context for connection status