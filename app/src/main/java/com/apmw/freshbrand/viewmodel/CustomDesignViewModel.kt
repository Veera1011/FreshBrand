package com.apmw.freshbrand.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apmw.freshbrand.model.CustomDesigns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CustomDesignViewModel : ViewModel() {
    private val _design = MutableStateFlow(CustomDesigns())
    val design = _design.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _saveStatus = MutableStateFlow<SaveStatus?>(null)
    val saveStatus = _saveStatus.asStateFlow()

    private val _historyLoading = MutableStateFlow(false)
    val historyLoading = _historyLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    sealed class SaveStatus {
        object Success : SaveStatus()
        data class Error(val message: String) : SaveStatus()
    }

    init {
        if (auth.currentUser != null) {
            loadUserDesign()
        }
    }

    fun saveOrUpdateDesign(designName: String, title: String, colorHex: String, logoUrl: String) {
        val userId = auth.currentUser?.uid ?: run {
            _saveStatus.value = SaveStatus.Error("User not authenticated")
            return
        }

        if (designName.isBlank() || title.isBlank()) {
            _saveStatus.value = SaveStatus.Error("Design name and title are required")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _saveStatus.value = null
                _errorMessage.value = null

                val currentTime = System.currentTimeMillis()
                val designData = mapOf(
                    "designName" to designName,
                    "title" to title,
                    "colorHex" to colorHex,
                    "logoUrl" to logoUrl,
                    "userId" to userId,
                    "createdAt" to if (_design.value.id.isEmpty()) currentTime else _design.value.createdAt,
                    "updatedAt" to currentTime
                )

                // Use userId as document ID for one design per user
                firestore.collection("designs")
                    .document(userId)
                    .set(designData)
                    .await()

                // Update local state
                _design.value = CustomDesigns(
                    id = userId,
                    designName = designName,
                    title = title,
                    colorHex = colorHex,
                    logoUrl = logoUrl,
                    userId = userId,
                    createdAt = _design.value.createdAt.takeIf { it > 0 } ?: currentTime
                )

                _saveStatus.value = SaveStatus.Success
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.Error(e.message ?: "Unknown error occurred")
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserDesign() {
        val userId = auth.currentUser?.uid ?: run {
            _errorMessage.value = "User not authenticated"
            return
        }

        viewModelScope.launch {
            try {
                _historyLoading.value = true
                _errorMessage.value = null

                val doc = firestore.collection("designs")
                    .document(userId)
                    .get()
                    .await()

                if (doc.exists()) {
                    val design = CustomDesigns(
                        id = doc.id,
                        designName = doc.getString("designName") ?: "",
                        title = doc.getString("title") ?: "",
                        colorHex = doc.getString("colorHex") ?: "#4CAF50",
                        logoUrl = doc.getString("logoUrl") ?: "",
                        userId = doc.getString("userId") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                    _design.value = design
                } else {
                    // No design exists for this user
                    _design.value = CustomDesigns()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _design.value = CustomDesigns()
            } finally {
                _historyLoading.value = false
            }
        }
    }

    fun prepareForEdit() {
        // The design is already loaded in _design, just let the UI know to switch tabs
        // You can implement tab switching logic in your UI
    }

    fun deleteUserDesign() {
        val userId = auth.currentUser?.uid ?: run {
            _errorMessage.value = "User not authenticated"
            return
        }

        viewModelScope.launch {
            try {
                firestore.collection("designs")
                    .document(userId)
                    .delete()
                    .await()

                _design.value = CustomDesigns()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun clearSaveStatus() {
        _saveStatus.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}