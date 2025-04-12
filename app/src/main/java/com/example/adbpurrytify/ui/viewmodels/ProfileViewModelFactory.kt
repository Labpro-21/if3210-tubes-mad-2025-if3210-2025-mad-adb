package com.example.adbpurrytify.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.adbpurrytify.api.ApiService
import com.example.adbpurrytify.data.local.SongDao

class ProfileViewModelFactory(
    private val apiService: ApiService,
    private val songDao: SongDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(apiService, songDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
