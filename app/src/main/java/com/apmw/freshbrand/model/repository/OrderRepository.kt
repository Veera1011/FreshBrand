// OrderRepository.kt
package com.apmw.freshbrand.model.repository

import com.apmw.freshbrand.model.Order
import com.apmw.freshbrand.model.CartItem
import com.apmw.freshbrand.model.OrderStatus
import com.apmw.freshbrand.model.User
import com.apmw.freshbrand.model.CustomDesigns
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OrderRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val ordersCollection = firestore.collection("orders")
    private val usersCollection = firestore.collection("users")
    private val customDesignsCollection = firestore.collection("custom_designs")

    suspend fun getAllOrders(): Flow<List<Order>> = flow {
        try {
            val snapshot = ordersCollection
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(id = doc.id)
            }
            emit(orders)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun getOrderById(orderId: String): Order? {
        return try {
            val snapshot = ordersCollection.document(orderId).get().await()
            snapshot.toObject(Order::class.java)?.copy(id = snapshot.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Boolean {
        return try {
            // Only update the status, no automatic date setting
            ordersCollection.document(orderId)
                .update("status", status.name)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateDeliveryDate(orderId: String, deliveryDate: Long): Boolean {
        return try {
            ordersCollection.document(orderId)
                .update("deliveryDate", deliveryDate)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteOrder(orderId: String): Boolean {
        return try {
            ordersCollection.document(orderId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            snapshot.toObject(User::class.java)?.copy(id = snapshot.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCustomDesignById(designId: String): CustomDesigns? {
        return try {
            val snapshot = customDesignsCollection.document(designId).get().await()
            snapshot.toObject(CustomDesigns::class.java)?.copy(id = snapshot.id)
        } catch (e: Exception) {
            null
        }
    }
}