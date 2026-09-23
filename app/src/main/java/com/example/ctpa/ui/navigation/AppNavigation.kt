package com.example.ctpa.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ctpa.domain.model.Worker
import com.example.ctpa.ui.screens.admin.AdminDashboardScreen
import com.example.ctpa.ui.screens.login.LoginScreen
import com.example.ctpa.ui.screens.worker.WorkerDashboardScreen

object Routes {
    const val LOGIN = "login"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val WORKER_DASHBOARD = "worker_dashboard/{workerId}"

    fun workerDashboard(workerId: String) = "worker_dashboard/$workerId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // Pantalla de Login
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToAdmin = {
                    navController.navigate(Routes.ADMIN_DASHBOARD)
                },
                onNavigateToWorker = { worker: Worker ->
                    navController.navigate(Routes.workerDashboard(worker.docId))
                }
            )
        }

        // Pantalla Admin (Placeholder por ahora)
        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Pantalla Worker (Placeholder por ahora)
        composable(Routes.WORKER_DASHBOARD) { backStackEntry ->
            val workerId = backStackEntry.arguments?.getString("workerId") ?: ""
            WorkerDashboardScreen(
                workerId = workerId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}