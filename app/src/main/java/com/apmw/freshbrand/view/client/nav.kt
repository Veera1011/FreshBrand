package com.apmw.freshbrand.view.client

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ClientBottomNavTab(val title: String, val icon: ImageVector) {
    object Dashboard : ClientBottomNavTab("Dashboard", Icons.Default.Home)
    object Orders : ClientBottomNavTab("Orders", Icons.Default.Receipt)
    object Shop : ClientBottomNavTab("Shop", Icons.Default.Storefront)
}
