//// TimelineUtils.kt
//package com.apmw.freshbrand.utils
//
//import com.apmw.freshbrand.model.DeliveryTimelineEntry
//import com.apmw.freshbrand.model.OrderStatus
//
////object TimelineUtils {
////
////    /**
////     * Creates an initial timeline entry for existing orders that don't have timeline data
////     */
////    fun createInitialTimelineEntry(
////        currentStatus: OrderStatus,
////        orderDate: Long
////    ): List<DeliveryTimelineEntry> {
////        val entries = mutableListOf<DeliveryTimelineEntry>()
////
////        // Add order creation entry
////        entries.add(
////            DeliveryTimelineEntry(
////                status = "PENDING",
////                timestamp = orderDate,
////                notes = "Order placed"
////            )
////        )
////
////        // If current status is different from PENDING, add current status entry
////        if (currentStatus != OrderStatus.PENDING) {
////            entries.add(
////                DeliveryTimelineEntry(
////                    status = currentStatus.name,
////                    timestamp = orderDate, // Since we don't know when status changed, use order date
////                    notes = "Status from existing data"
////                )
////            )
////        }
////
////        return entries
////    }
////
////    /**
////     * Migrates existing orders to include timeline data
////     * Call this once to initialize timeline for existing orders in your database
////     */
////    suspend fun migrateExistingOrdersTimeline(orderRepository: com.apmw.freshbrand.model.repository.OrderRepository) {
////        // This is a utility function that you can call once to migrate existing orders
////        // Implementation would depend on how you want to handle the migration
////        // For example, you might add a migration method to your repository
////    }
////}