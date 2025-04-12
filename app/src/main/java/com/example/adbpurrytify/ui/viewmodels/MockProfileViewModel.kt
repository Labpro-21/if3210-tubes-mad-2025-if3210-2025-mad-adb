package com.example.adbpurrytify.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.adbpurrytify.data.model.User
import com.example.adbpurrytify.data.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileViewModel.ProfileUiState>(
        ProfileViewModel.ProfileUiState.Success(
            User(
                id = 0,
                userName = "My Username",
                email = "test@email.com",
                image = "pathToImage",
                location = "Indonesia",
                createdAt = "10/10/2010",
                updatedAt = "10/10/2010"
            ),
            UserStats(
                0, 1, 2
            )
        )
    )
    val uiState: StateFlow<ProfileViewModel.ProfileUiState> = _uiState
}
