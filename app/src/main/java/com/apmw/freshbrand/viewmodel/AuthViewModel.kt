package com.apmw.freshbrand.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apmw.freshbrand.model.AuthState
import com.apmw.freshbrand.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.apmw.freshbrand.model.repository.AuthRepository
class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val currentUser = repository.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val userData = repository.getUserData(currentUser.uid)
                    _currentUser.value = userData
                    _authState.value = AuthState.Authenticated
                } catch (e: Exception) {
                    _authState.value = AuthState.Error(e.message ?: "Unknown error")
                } finally {
                    _isLoading.value = false
                }
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading

            val result = repository.signIn(email, password)

            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Sign in failed")
                }
            )

            _isLoading.value = false
        }
    }

    fun signUp(email: String, password: String, name: String, phone: String, companyName: String, gstNumber: String, address: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading

            val userData = User(
                name = name,
                phone = phone,
                companyName = companyName,
                gstNumber = gstNumber,
                address = address
            )

            val result = repository.signUp(email, password, userData)

            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated
                },
                onFailure = { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Sign up failed")
                }
            )

            _isLoading.value = false
        }
    }

    fun signOut() {
        repository.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }
}
