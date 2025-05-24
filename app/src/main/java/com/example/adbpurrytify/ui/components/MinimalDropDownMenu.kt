package com.example.adbpurrytify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.adbpurrytify.ui.utils.DynamicColorExtractor

@Composable
fun MinimalDropdownMenu(
    onShareViaUrl: () -> Unit,
    onShareViaQr: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { isExpanded = !isExpanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Share via URL") },
                onClick = {
                    onShareViaUrl()
                    isExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Share via QR") },
                onClick = {
                    onShareViaQr()
                    isExpanded = false
                }
            )
        }
    }
}