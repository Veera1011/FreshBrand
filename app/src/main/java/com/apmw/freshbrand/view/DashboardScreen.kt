package com.apmw.freshbrand.view.dashboard


import androidx.compose.runtime.Composable
import com.apmw.freshbrand.model.User
import com.apmw.freshbrand.model.UserType
import com.apmw.freshbrand.view.admin.AdminDashboardScreen
import com.apmw.freshbrand.viewmodel.AuthViewModel

@Composable
fun DashboardScreen(
    user: User,
    viewModel: AuthViewModel
) {
    if (user.userType == UserType.ADMIN) {
        AdminDashboardScreen(user, viewModel)
    } else {
        ClientDashboardScreen(user, viewModel)
    }
}
