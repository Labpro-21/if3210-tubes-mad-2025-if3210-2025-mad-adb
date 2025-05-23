package com.example.adbpurrytify.data.download

import android.content.Context
import com.example.adbpurrytify.data.model.SongEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.io.File

/**
 * Why no file checking?
 * I delegated this checking to the SongPlayerScreen
 */
suspend fun downloadSong(
    song: SongEntity, context: Context, dispatcher: CoroutineDispatcher = Dispatchers.IO,
    // onProgress: Float => Unit = _ => ()
    onProgress: (Float) -> Unit = {}
): String? {
    return withContext(dispatcher) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(song.audioUri).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response")
                    return@withContext null
                }

                val body = response.body ?: return@withContext null
                val totalBytes = body.contentLength()
                val inputStream = body.byteStream()

                val file = File(context.filesDir, "${song.author + "-" + song.title}.mp3")
                file.outputStream().use { outputStream ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesCopied = 0L
                    var read = inputStream.read(buffer)
                    val eof = -1 // an EOF marker as specified by read method
                    while (read != eof) {
                        outputStream.write(buffer, 0, read)
                        bytesCopied += read
                        if (totalBytes > 0) {
                            val progress = bytesCopied.toFloat() / totalBytes.toFloat()
                            onProgress(progress)
                        }
                        read = inputStream.read(buffer)
                    }
                }
                return@withContext file.path
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}

