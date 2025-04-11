package com.example.adbpurrytify.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.model.Song
import android.view.View
import androidx.compose.ui.platform.LocalContext
import com.example.adbpurrytify.data.model.SongEntity
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
    val contentResolver = context.contentResolver
    val returnCursor = contentResolver.query(uri, null, null, null, null) ?: return null

    val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    returnCursor.moveToFirst()
    val name = returnCursor.getString(nameIndex)
    returnCursor.close()

    val inputStream = contentResolver.openInputStream(uri) ?: return null
    val file = File(context.filesDir, name)
    val outputStream = FileOutputStream(file)

    try {
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
    } catch (e: Exception) {
        Log.e("copyUri", "Error copying file: ${e.message}")
        return null
    } finally {
        inputStream.close()
        outputStream.close()
    }

    return file.absolutePath
}


class SongAdapter(private val songs: List<SongEntity>, context: Context) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private val context = context
    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songArt: ImageView = itemView.findViewById(R.id.song_image)
        val title: TextView = itemView.findViewById(R.id.song_title)
        val author: TextView = itemView.findViewById(R.id.song_author)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song_row, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {

        val song = songs[position]
        val copiedPath = copyUriToInternalStorage(context, Uri.parse(song.artUri))

        val file = File(copiedPath)
        if (file.exists())
            holder.songArt.setImageURI(Uri.fromFile(file))
        else
            holder.songArt.setImageResource(R.drawable.song_art_placeholder)

        holder.title.text = song.title
        holder.author.text = song.author
    }

    override fun getItemCount(): Int = songs.size
}
