package com.apmw.freshbrand.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apmw.freshbrand.model.User
import com.apmw.freshbrand.model.UserUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.apmw.freshbrand.model.repository.UserRepository

class UsersViewModel : ViewModel() {
    private val repository = UserRepository()
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val users = repository.getAllUsers()
                _uiState.value = _uiState.value.copy(
                    users = users,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load users",
                    isLoading = false
                )
            }
        }
    }

    fun updateUser(id: String, user: User) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdatingUser = true, error = null)
            try {
                val updatedUser = user.copy(
                    updatedAt = System.currentTimeMillis()
                )
                val success = repository.updateUser(id, updatedUser)
                if (success) {
                    loadUsers()
                    _uiState.value = _uiState.value.copy(isUpdatingUser = false)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update user",
                        isUpdatingUser = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update user",
                    isUpdatingUser = false
                )
            }
        }
    }

    fun deleteUser(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            try {
                val success = repository.deleteUser(id)
                if (success) {
                    loadUsers()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to delete user"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete user"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}