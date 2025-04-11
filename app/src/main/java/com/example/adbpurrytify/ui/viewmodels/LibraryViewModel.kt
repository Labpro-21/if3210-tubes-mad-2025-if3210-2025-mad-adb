package com.example.adbpurrytify.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.adbpurrytify.data.model.Song


class LibraryViewModel : ViewModel() {
    // Hardcoded list of songs for the library
    val songs = listOf(
        Song(
            id = 1,
            title = "Starboy",
            author = "The Weeknd, Daft Punk",
            coverUrl = "https://i.scdn.co/image/ab67616d0000b273a048415db06a5b6fa7ec4e1a",
            audioUrl = "https://example.com/audio/starboy.mp3"
        ),
        Song(
            id = 2,
            title = "Here Comes The Sun - Remastered",
            author = "The Beatles",
            coverUrl = "https://i.scdn.co/image/ab67616d0000b273dc30583ba717007b00cceb25",
            audioUrl = "https://example.com/audio/herecomesthesun.mp3"
        ),
        Song(
            id = 3,
            title = "Midnight Pretenders",
            author = "Tomoko Aran",
            coverUrl = "https://i.scdn.co/image/ab67616d0000b2731df02badb7b01be1d614c63b",
            audioUrl = "https://example.com/audio/midnightpretenders.mp3"
        ),
        Song(
            id = 4,
            title = "Violent Crimes",
            author = "Kanye West",
            coverUrl = "https://i.scdn.co/image/ab67616d0000b273231fd0b2207747f75b67f867",
            audioUrl = "https://example.com/audio/violentcrimes.mp3"
        ),
        Song(
            id = 5,
            title = "DENIAL IS A RIVER",
            author = "Doechii",
            coverUrl = "https://i.scdn.co/image/ab67616d0000b2731fc4e25f4ce8d0e4d7b95a17",
            audioUrl = "https://example.com/audio/denialisariver.mp3"
        ),
        Song(
            id = 6,
            title = "Doomsday",
            author = "MF DOOM, Pebbles The Invisible Girl",
            coverUrl = "https://i.scdn.co/image/ab67616d0000b273cb9b6e56d6a3f37c551ef8e3",
            audioUrl = "https://example.com/audio/doomsday.mp3"
        )
    )

    // Currently playing song (matches what's shown in the screenshot)
    val currentlyPlayingSong = songs[0]
}