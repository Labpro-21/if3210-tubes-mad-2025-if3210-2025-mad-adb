package com.example.adbpurrytify.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun shareSong(context: Context, songId: Long) {
    val shareUrl = "purrytify://song/$songId"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareUrl)
    }
    context.startActivity(Intent.createChooser(intent, "Share song via"))
}

fun shareSongQR(context: Context, songId: Long) {
    val shareUrl = "purrytify://song/$songId"
    val dimension = 512
    val qrgEncoder = QRGEncoder(shareUrl, null, QRGContents.Type.TEXT, dimension)
    val bitmap: Bitmap = qrgEncoder.bitmap

    val cachePath = File(context.cacheDir, "qr_images")
    if (!cachePath.exists()) {
        cachePath.mkdirs()
    }

    val file = File(cachePath, "qr_share_${songId}.png")
    FileOutputStream(file).use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    }

    val contentUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
}
