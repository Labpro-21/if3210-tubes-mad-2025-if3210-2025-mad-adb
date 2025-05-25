package com.example.adbpurrytify.data.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.adbpurrytify.data.model.SoundCapsule
import com.example.adbpurrytify.ui.viewmodels.TopArtistsData
import com.example.adbpurrytify.ui.viewmodels.TopSongsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportManager @Inject constructor() {

    suspend fun exportSoundCapsuleAsCSV(
        context: Context,
        soundCapsule: SoundCapsule,
        topArtistsData: TopArtistsData?,
        topSongsData: TopSongsData?
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val fileName = "sound_capsule_${soundCapsule.month}.csv"
            val csvContent = buildString {
                // Header
                append("Sound Capsule Report - ${soundCapsule.displayMonth}\n")
                append("Generated on: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n\n")

                // Summary
                append("SUMMARY\n")
                append("Time Listened,${soundCapsule.timeListened} minutes\n")
                append("Top Artist,${soundCapsule.topArtist?.name ?: "N/A"}\n")
                append("Top Song,${soundCapsule.topSong?.title ?: "N/A"}\n")
                append("Day Streak,${soundCapsule.dayStreak?.streakDays ?: 0} days\n\n")

                // Top Artists
                if (topArtistsData != null && topArtistsData.artists.isNotEmpty()) {
                    append("TOP ARTISTS\n")
                    append("Rank,Artist Name,Minutes Listened,Songs Count\n")
                    topArtistsData.artists.forEach { artist ->
                        append("${artist.rank},\"${artist.name}\",${artist.minutesListened},${artist.songsCount}\n")
                    }
                    append("\n")
                }

                // Top Songs
                if (topSongsData != null && topSongsData.songs.isNotEmpty()) {
                    append("TOP SONGS\n")
                    append("Rank,Song Title,Artist,Minutes Listened,Play Count\n")
                    topSongsData.songs.forEach { song ->
                        append("${song.rank},\"${song.title}\",\"${song.artist}\",${song.minutesListened},${song.playsCount}\n")
                    }
                }
            }

            val uri = saveFileToDownloads(context, fileName, csvContent.toByteArray(), "text/csv")
            showDownloadNotification(context, fileName, "CSV")

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportSoundCapsuleAsPDF(
        context: Context,
        soundCapsule: SoundCapsule,
        topArtistsData: TopArtistsData?,
        topSongsData: TopSongsData?
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val fileName = "sound_capsule_${soundCapsule.month}.pdf"

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply {
                isAntiAlias = true
                textSize = 12f
                color = android.graphics.Color.BLACK
            }

            val titlePaint = Paint().apply {
                isAntiAlias = true
                textSize = 18f
                color = android.graphics.Color.BLACK
                isFakeBoldText = true
            }

            val headerPaint = Paint().apply {
                isAntiAlias = true
                textSize = 14f
                color = android.graphics.Color.BLACK
                isFakeBoldText = true
            }

            var yPosition = 50f
            val margin = 50f
            val lineHeight = 20f

            // Title
            canvas.drawText("Sound Capsule Report", margin, yPosition, titlePaint)
            yPosition += lineHeight * 1.5f

            canvas.drawText(soundCapsule.displayMonth, margin, yPosition, headerPaint)
            yPosition += lineHeight * 2f

            // Summary section
            canvas.drawText("SUMMARY", margin, yPosition, headerPaint)
            yPosition += lineHeight * 1.5f

            canvas.drawText("Time Listened: ${soundCapsule.timeListened} minutes", margin, yPosition, paint)
            yPosition += lineHeight

            canvas.drawText("Top Artist: ${soundCapsule.topArtist?.name ?: "N/A"}", margin, yPosition, paint)
            yPosition += lineHeight

            canvas.drawText("Top Song: ${soundCapsule.topSong?.title ?: "N/A"}", margin, yPosition, paint)
            yPosition += lineHeight

            canvas.drawText("Day Streak: ${soundCapsule.dayStreak?.streakDays ?: 0} days", margin, yPosition, paint)
            yPosition += lineHeight * 2f

            // Top Artists section
            if (topArtistsData != null && topArtistsData.artists.isNotEmpty()) {
                canvas.drawText("TOP ARTISTS", margin, yPosition, headerPaint)
                yPosition += lineHeight * 1.5f

                topArtistsData.artists.take(10).forEach { artist ->
                    val text = "${artist.rank}. ${artist.name} - ${artist.minutesListened} min (${artist.songsCount} songs)"
                    canvas.drawText(text, margin, yPosition, paint)
                    yPosition += lineHeight
                }
                yPosition += lineHeight
            }

            // Top Songs section
            if (topSongsData != null && topSongsData.songs.isNotEmpty()) {
                canvas.drawText("TOP SONGS", margin, yPosition, headerPaint)
                yPosition += lineHeight * 1.5f

                topSongsData.songs.take(10).forEach { song ->
                    val text = "${song.rank}. ${song.title} by ${song.artist} - ${song.minutesListened} min (${song.playsCount} plays)"
                    canvas.drawText(text, margin, yPosition, paint)
                    yPosition += lineHeight
                }
            }

            // Footer
            val footerY = 800f
            canvas.drawText(
                "Generated on ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}",
                margin, footerY, paint
            )

            pdfDocument.finishPage(page)

            // Convert to byte array
            val tempFile = File(context.cacheDir, "temp_$fileName")
            FileOutputStream(tempFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            val pdfBytes = tempFile.readBytes()
            tempFile.delete()

            val uri = saveFileToDownloads(context, fileName, pdfBytes, "application/pdf")
            showDownloadNotification(context, fileName, "PDF")

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveFileToDownloads(
        context: Context,
        fileName: String,
        data: ByteArray,
        mimeType: String
    ): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ - Use MediaStore API
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Purrytify")
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IOException("Failed to create new MediaStore record")

            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(data)
            } ?: throw IOException("Failed to open output stream")

            uri
        } else {
            // Android 9 and below - Use legacy external storage
            val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Purrytify")
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val file = File(downloadsDir, fileName)
            file.writeBytes(data)

            // Create URI for sharing
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }
    }

    private fun showDownloadNotification(context: Context, fileName: String, format: String) {
        // Show toast notification
        Toast.makeText(
            context,
            "✓ $format file downloaded to Downloads/Purrytify/$fileName",
            Toast.LENGTH_LONG
        ).show()

        // You can also add system notification here if needed
        // This would require notification permission in newer Android versions
    }

    suspend fun shareFile(context: Context, uri: Uri, mimeType: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Share Sound Capsule")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            throw IOException("Failed to share file: ${e.message}")
        }
    }
}

// Extension functions for easy usage
suspend fun SoundCapsule.exportAsCSV(
    context: Context,
    exportManager: ExportManager,
    topArtistsData: TopArtistsData? = null,
    topSongsData: TopSongsData? = null
): Result<Uri> {
    return exportManager.exportSoundCapsuleAsCSV(context, this, topArtistsData, topSongsData)
}

suspend fun SoundCapsule.exportAsPDF(
    context: Context,
    exportManager: ExportManager,
    topArtistsData: TopArtistsData? = null,
    topSongsData: TopSongsData? = null
): Result<Uri> {
    return exportManager.exportSoundCapsuleAsPDF(context, this, topArtistsData, topSongsData)
}