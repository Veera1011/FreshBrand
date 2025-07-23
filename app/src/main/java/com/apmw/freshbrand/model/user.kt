// Updated User.kt
package com.apmw.freshbrand.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val userType: UserType = UserType.CLIENT,
    val gstNumber: String = "",
    val companyName: String = "",
    val address: String = "",
    val status: UserStatus = UserStatus.ACTIVE,
  //  val emailVerified: Boolean = false, // Added email verification field
    val createdDate: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class UserType {
    ADMIN, CLIENT
}

enum class UserStatus {
    ACTIVE, INACTIVE
}

// For UI state management
data class UserUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingUser: Boolean = false,
    val isUpdatingUser: Boolean = false,
    val selectedUser: User? = null
)