package dev.codcow.kasirku.features.beranda.ui.screens

import CustomBottomNavigation
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.codcow.kasirku.features.auth.ui.screens.LoginViewModel
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.components.MenuPegawaiScreen
import dev.codcow.kasirku.ui.components.MenuScreen
import dev.codcow.kasirku.ui.components.WelcomeTopBar
import dev.codcow.kasirku.ui.components.WelcomeTopBarPegawai
import dev.codcow.kasirku.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BerandaPegawai(
    navController: NavController,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(MaterialTheme.colorScheme.background)
        ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp)
        ) {
            WelcomeTopBarPegawai(navController = navController)

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                "Pegawai Menu",
                style = AppTheme.typography.paragraph1Bold
            )

            MenuPegawaiScreen { menu ->
                when (menu) {
                    "Log Out" -> {
                        loginViewModel.logout() // Panggil fungsi logout (kamu bisa sesuaikan ini dengan implementasi sebenarnya)
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) // Hapus seluruh backstack agar tidak bisa kembali
                        }
                    }
                }
            }
        }

    }

    Column (modifier = Modifier
        .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ){
        CustomBottomNavigation(navController = navController)
    }
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

