package com.apmw.freshbrand.view.admin




import androidx.compose.material3.*
import androidx.compose.runtime.*


data class SalesData(
    val period: String,
    val revenue: Double,
    val orders: Int,
    val growth: Double
)

data class ProductPerformance(
    val name: String,
    val sold: Int,
    val revenue: Double,
    val growth: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen() {

}