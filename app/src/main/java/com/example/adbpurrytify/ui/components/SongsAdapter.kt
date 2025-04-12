package com.example.adbpurrytify.ui.components

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import coil3.request.target
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.model.SongEntity

class SongAdapter(
    private var songs: List<SongEntity>,
    private val context: Context,
    private val onSongClick: (SongEntity) -> Unit = {}
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songImage: ImageView = view.findViewById(R.id.song_image)
        val songTitle: TextView = view.findViewById(R.id.song_title)
        val songAuthor: TextView = view.findViewById(R.id.song_author)

        // Set up click listener in the view holder
        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSongClick(songs[position])
                }
            }
        }
    }

    fun updateData(newSongs: List<SongEntity>) {
        this.songs = newSongs
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song_row, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.songTitle.text = song.title
        holder.songAuthor.text = song.author

        // If artUri is a valid URL or file path
        if (song.artUri.isNotEmpty()) {
            val imageLoader = ImageLoader.Builder(context)
                .crossfade(true)
                .build()

            val request = ImageRequest.Builder(context)
                .data(song.artUri)
                .target(holder.songImage)
                .placeholder(R.drawable.song_art_placeholder)
                .error(R.drawable.song_art_placeholder)
                .build()

            imageLoader.enqueue(request)
        } else {
            // Use default image
            holder.songImage.setImageResource(R.drawable.song_art_placeholder)
        }
    }

    override fun getItemCount() = songs.size
}
