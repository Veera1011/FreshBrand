package com.apmw.freshbrand.model.repository

// ProductRepository.kt

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import android.net.Uri
import com.apmw.freshbrand.model.Product
import com.apmw.freshbrand.model.ProductStatus
import com.apmw.freshbrand.model.User
import java.util.*

class ProductRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val productsCollection = firestore.collection("products")
    private val storageRef = storage.reference.child("product_images")

    suspend fun getAllProducts(): List<Product> {
        return try {
            val snapshot = productsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                document.toObject(Product::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getProductById(productId: String): Product? {
        return try {
            val document = productsCollection.document(productId).get().await()
            document.toObject(Product::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addProduct(product: Product): String {
        return try {
            val documentRef = productsCollection.add(product).await()
            documentRef.id
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateProduct(productId: String, product: Product): Boolean {
        return try {
            val updatedProduct = product.copy(
                updatedAt = System.currentTimeMillis()
            )
            productsCollection.document(productId)
                .set(updatedProduct)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteProduct(productId: String): Boolean {
        return try {
            productsCollection.document(productId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateStock(productId: String, newStock: Int): Boolean {
        return try {
            val updates = mapOf(
                "availableStock" to newStock,
                "updatedAt" to System.currentTimeMillis(),
                "status" to if (newStock > 0) ProductStatus.AVAILABLE else ProductStatus.OUT_OF_STOCK
            )
            productsCollection.document(productId).update(updates).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun uploadProductImage(imageUri: Uri): String {
        return try {
            val imageRef = storageRef.child("${UUID.randomUUID()}.jpg")
            val uploadTask = imageRef.putFile(imageUri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteProductImage(imageUrl: String): Boolean {
        return try {
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}



class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = usersCollection
                .orderBy("createdDate", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                document.toObject(User::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getUserById(id: String): User? {
        return try {
            val document = usersCollection.document(id).get().await()
            document.toObject(User::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUser(id: String, user: User): Boolean {
        return try {
            // Create update map with only the fields to update
            val updateMap = mapOf(
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "userType" to user.userType,
                "gstNumber" to user.gstNumber,
                "companyName" to user.companyName,
                "address" to user.address,
                "status" to user.status,
                "updatedAt" to System.currentTimeMillis()
            )

            // Use update() instead of set() to modify existing document
            usersCollection.document(id)
                .update(updateMap)
                .await()
            true
        } catch (e: Exception) {
            // If document doesn't exist, create it with set()
            try {
                val updatedUser = user.copy(
                    updatedAt = System.currentTimeMillis()
                )
                usersCollection.document(id)
                    .set(updatedUser)
                    .await()
                true
            } catch (setException: Exception) {
                false
            }
        }
    }

    suspend fun deleteUser(id: String): Boolean {
        return try {
            usersCollection.document(id).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}

