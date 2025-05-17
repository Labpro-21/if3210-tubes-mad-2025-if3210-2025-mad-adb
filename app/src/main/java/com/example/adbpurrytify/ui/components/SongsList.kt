package com.example.adbpurrytify.ui.components

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.ui.theme.BLACK_BACKGROUND


@Composable
fun RecyclerSongsList(
    songs: List<SongEntity>,
    showBorder: Boolean,
    onSongClick: (SongEntity) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .clip(RectangleShape)
            .background(BLACK_BACKGROUND)
    ) {
        AndroidView(
            factory = { context ->
                RecyclerView(context).apply {
                    layoutManager = LinearLayoutManager(context)
                    // Pass onSongClick during initial creation
                    adapter = SongAdapter(songs, context, onSongClick)
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    if (showBorder) {
                        setBackgroundColor(android.graphics.Color.RED)
                    }
                    setPadding(0, 0, 0, 0)
                    clipToPadding = true
                    clipChildren = true
                }
            },
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .background(BLACK_BACKGROUND),
            update = { recyclerView ->
                // *** FIX: Pass onSongClick when updating the adapter too! ***
                // Also, ensure the adapter instance is updated correctly if possible
                // See improvement suggestion below
                (recyclerView.adapter as? SongAdapter)?.let { adapter ->
                    // More efficient: Update data in existing adapter
                    adapter.updateData(songs)
                } ?: run {
                    // Fallback: Create new adapter if it's null or wrong type
                    recyclerView.adapter = SongAdapter(songs, recyclerView.context, onSongClick)
                }
            },
        )
    }
}



