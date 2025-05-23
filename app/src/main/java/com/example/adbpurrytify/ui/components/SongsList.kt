package com.example.adbpurrytify.ui.components

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.ui.adapters.SongAdapter

@Composable
fun RecyclerSongsList(
    songs: List<SongEntity>,
    showBorder: Boolean = true,
    onSongClick: (SongEntity) -> Unit
) {
    AndroidView(
        factory = { context ->
            RecyclerView(context).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = SongAdapter(songs) { song ->
                    onSongClick(song)
                }
                // Add tight spacing like Spotify
                addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect,
                        view: View,
                        parent: RecyclerView,
                        state: RecyclerView.State
                    ) {
                        outRect.bottom = context.dpToPx(4) // Tight spacing
                    }
                })
                setPadding(
                    context.dpToPx(16), // left
                    context.dpToPx(8),  // top
                    context.dpToPx(16), // right
                    context.dpToPx(8)   // bottom
                )
                clipToPadding = false
                overScrollMode = View.OVER_SCROLL_NEVER // Remove overscroll effect
            }
        },
        update = { recyclerView ->
            (recyclerView.adapter as? SongAdapter)?.updateSongs(songs)
        },
        modifier = Modifier.fillMaxWidth()
    )
}

// Extension function to convert dp to px
fun Context.dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
}
