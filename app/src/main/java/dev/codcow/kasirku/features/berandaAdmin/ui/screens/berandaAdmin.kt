package dev.codcow.kasirku.features.berandaAdmin.ui.screens

import CustomBottomNavigation
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.codcow.kasirku.features.auth.ui.screens.LoginViewModel
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.components.MenuScreen
import dev.codcow.kasirku.ui.components.UserRole
import dev.codcow.kasirku.ui.components.WelcomeTopBar
import dev.codcow.kasirku.ui.theme.AppTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun beranda(
    onNavigateToPegawai: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToKategori: () -> Unit,
    onNavigateToSubKategori: () -> Unit,
    onNavigateToDeposit: () -> Unit,
    onNavigateToTransaksi: () -> Unit,
    onNavigateToWarung: () -> Unit,
    navController: NavController,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }

    // Get the current context
    val context = LocalContext.current

    // Get the user role from shared preferences via LoginViewModel
    val userRole = remember {
        loginViewModel.getUserRole(context) ?: ""
    }

    // Convert the role string to UserRole enum
    val role = when (userRole.lowercase()) {
        "admin" -> UserRole.ADMIN
        else -> UserRole.EMPLOYEE
    }

    // Set title based on role
    val roleTitle = if (role == UserRole.ADMIN) "Admin Menu" else "Pegawai Menu"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.onSurface)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp)
        ) {
            WelcomeTopBar(navController = navController)

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                roleTitle,
                style = AppTheme.typography.paragraph1Bold
            )

            MenuScreen(
                onMenuSelected = { menu ->
                    when (menu) {
                        "Pegawai" -> onNavigateToPegawai()
                        "Menu Manager" -> onNavigateToMenu()
                        "Kategori" -> onNavigateToKategori()
                        "Sub-kategori" -> onNavigateToSubKategori()
                        "Deposit" -> onNavigateToDeposit()
                        "Transaksi Manager" -> onNavigateToTransaksi()
                        "Warung" -> onNavigateToWarung()
                    }
                },
                onLogoutSelected = {
                    loginViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true } // Optional: Hapus semua backstack
                    }
                },
                userRole = role
            )
        }

    }

    Column (modifier = Modifier
        .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ){
        CustomBottomNavigation(
            currentRoute = currentRoute(navController = navController),
            onNavigateToHome = {
                // Navigasi ke home dengan menghapus backstack
                navController.navigate(Screen.Menu.route) {
                    popUpTo(Screen.Menu.route) { inclusive = true }
                }
            },
            onNavigateToRekap = {
                navController.navigate(Screen.Rekap.route) {
                    popUpTo(Screen.Rekap.route) { inclusive = true }
                }
            },
            onNavigateToTransaksi = {
                navController.navigate(Screen.Transaksi.route) {
                    popUpTo(Screen.Transaksi.route) { inclusive = true}
                }
            }
        )
    }
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}