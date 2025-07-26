package com.apmw.freshbrand.model

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object AwaitingOTPVerification : AuthState() // New state for OTP verification
    data class Error(val message: String) : AuthState()
}