package com.example.adbpurrytify.ui.utils

import android.content.Context
import android.content.Intent

fun shareSong(context: Context, songId: Long) {
    val shareUrl = "purrytify://song/$songId"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareUrl)
    }
    context.startActivity(Intent.createChooser(intent, "Share song via"))
}
