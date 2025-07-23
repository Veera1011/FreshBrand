package com.apmw.freshbrand.model.repository


import com.apmw.freshbrand.model.AuthState
import com.apmw.freshbrand.model.User
import com.apmw.freshbrand.model.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val adminEmail = "eveera5603@gmail.com"
    private val adminPassword = "Veera32@35"

    fun getCurrentUser() = auth.currentUser

    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user

            if (user != null) {
                val userData = getUserData(user.uid)
                Result.success(userData)
            } else {
                Result.failure(Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, userData: User): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user

            if (user != null) {
                val newUser = userData.copy(
                    id = user.uid,
                    email = email,
                    userType = UserType.CLIENT
                )

                firestore.collection("users")
                    .document(user.uid)
                    .set(newUser)
                    .await()

                Result.success(newUser)
            } else {
                Result.failure(Exception("User creation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

     suspend fun getUserData(uid: String): User {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            if (document.exists()) {
                document.toObject(User::class.java) ?: User()
            } else {
                // Create user data if doesn't exist
                val currentUser = auth.currentUser
                val userData = User(
                    id = uid,
                    email = currentUser?.email ?: "",
                  //  userType = if (currentUser?.email == adminEmail) UserType.ADMIN else UserType.CL
                    userType = UserType.CLIENT
                )

                firestore.collection("users").document(uid).set(userData).await()
                userData
            }
        } catch (e: Exception) {
            User()
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getAuthState(): Flow<AuthState> = flow {
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                // User is signed in
            } else {
                // User is signed out
            }
        }
    }
}
