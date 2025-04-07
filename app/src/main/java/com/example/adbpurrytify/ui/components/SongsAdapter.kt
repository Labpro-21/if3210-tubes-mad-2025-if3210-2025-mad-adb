package com.example.adbpurrytify.ui.components

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.model.Song
import android.view.View
import java.io.File

class SongAdapter(private val songs: List<Song>) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

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
        val coverURI = Uri.parse(song.coverUrl)
        val file = File(coverURI.path ?: "")

        if (file.exists()) {
            holder.songArt.setImageURI(coverURI)
        } else {
            holder.songArt.setImageResource(R.drawable.song_art_placeholder)
        }
        holder.title.text = song.title
        holder.author.text = song.author
    }

    override fun getItemCount(): Int = songs.size
}
