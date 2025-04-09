package com.example.adbpurrytify.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.adbpurrytify.data.local.SongDao
import com.example.adbpurrytify.data.model.SongEntity
import kotlinx.coroutines.launch

class SongViewModel(private val songDao: SongDao) : ViewModel() {
    val allSongs: LiveData<List<SongEntity>> = songDao.getAllSongs().asLiveData()
    fun insert(song: SongEntity) = viewModelScope.launch {
        songDao.insert(song)
    }
}