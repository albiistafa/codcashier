package dev.codcow.kasirku.middleware

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.codcow.kasirku.core.data.model.addToCart.CartItem
import dev.codcow.kasirku.features.auth.ui.screens.LoginScreen
import dev.codcow.kasirku.features.beranda.ui.screens.BerandaPegawai
import dev.codcow.kasirku.features.berandaAdmin.ui.screens.beranda
import dev.codcow.kasirku.features.deposit.ui.screens.CreateDeposit
import dev.codcow.kasirku.features.deposit.ui.screens.DepositManager
import dev.codcow.kasirku.features.deposit.ui.screens.EditDeposit
import dev.codcow.kasirku.features.kategoriManager.ui.screens.CreateKategori
import dev.codcow.kasirku.features.kategoriManager.ui.screens.KategoriManager
import dev.codcow.kasirku.features.kategoriManager.ui.screens.EditKategori
import dev.codcow.kasirku.features.menuManager.ui.screens.BuatMenuScreen
import dev.codcow.kasirku.features.menuManager.ui.screens.EditMenuScreen
import dev.codcow.kasirku.features.menuManager.ui.screens.MenuAdmin
import dev.codcow.kasirku.kategoriManager.ui.screens.SubKategoriManager
import dev.codcow.kasirku.features.menuManager.ui.screens.MenuManager
import dev.codcow.kasirku.features.menuManager.ui.screens.MenuPegawai
import dev.codcow.kasirku.features.pegawai.ui.screens.CreatePegawai
import dev.codcow.kasirku.features.pegawai.ui.screens.PegawaiManager
import dev.codcow.kasirku.features.splash.ui.screens.Splash
import dev.codcow.kasirku.features.subKategoriManager.ui.screens.CreateSubKategori
import dev.codcow.kasirku.features.subKategoriManager.ui.screens.EditSubKategori
import dev.codcow.kasirku.features.transaksi.ui.screens.CreatePengeluaran
import dev.codcow.kasirku.features.transaksi.ui.screens.DetailPembayaran
import dev.codcow.kasirku.features.transaksi.ui.screens.DetailPesananScreen
import dev.codcow.kasirku.features.transaksi.ui.screens.DetailPesananScreenSelesai
import dev.codcow.kasirku.features.transaksi.ui.screens.RekapScreen
import dev.codcow.kasirku.features.transaksi.ui.screens.TransaksiManager
import dev.codcow.kasirku.features.transaksi.ui.screens.TransaksiScreen
import dev.codcow.kasirku.features.warung.ui.screens.EditWarung
import dev.codcow.kasirku.features.transaksi.ui.screens.DetailPesananScreenUpdate
import dev.codcow.kasirku.features.transaksi.ui.screens.PaymentUpdateScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object HomePegawai : Screen("homePegawai")

    object Pegawai : Screen("pegawai")
    object CreatePegawai : Screen("create_pegawai")

    object Menu : Screen("menu")
    object MenuAdmin : Screen("menuadmin")

    object MenuManager : Screen("menuManager")
    object CreateMenu : Screen("create_menu")

    object TransaksiManager : Screen("transaksiManager")

    object EditWarung : Screen("editWarung")

    object Kategori : Screen("kategori")
    object CreateKategori : Screen("create_kategori")
    object EditKategori : Screen("edit_kategori/{kategoriId}")

    object SubKategori : Screen("subkategori")
    object CreateSubKategori : Screen("create_subkategori")
    object EditSubKategori : Screen("edit_subKategori/{subKategoriId}")

    object Deposit : Screen("deposit")
    object CreateDeposit : Screen("create_deposit")
    object EditDeposit : Screen("edit_deposit/{depositId}") {
        fun createRoute(depositId: Int): String {
            return "edit_deposit/$depositId"
        }
    }

    object Rekap : Screen("rekap")
    object Transaksi : Screen("transaksi")
    object DetailSelesai : Screen("detail_selesai/{transactionId}") {
        fun createRoute(transactionId: Int): String {
            return "detail_selesai/$transactionId"
        }
    }
    object PaymentUpdate : Screen("paymentupdate/{transactionId}") {
        fun createRoute(transactionId: Int): String {
            return "paymentupdate/$transactionId"
        }
    }

    object CreatePengeluaran : Screen("createPengeluaran")

    object Detail : Screen("detail/{transactionId}") {
        fun createRoute(transactionId: Int): String {
            return "detail/$transactionId"
        }
    }

    // Add this new route for cart details (no transaction ID needed)
    object CartDetail : Screen("cart_detail")

    object DetailPembayaran : Screen("detail_pembayaran/{transactionId}") {
        fun createRoute(transactionId: Int): String {
            return "detail_pembayaran/$transactionId"
        }
    }
}

@Composable
fun AppNavGraph(startDestination: String = Screen.Splash.route) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Splash.route) {
            val context = LocalContext.current
            Splash(navController = navController,
                viewModel = hiltViewModel(),
                context = context)
        }
        composable(Screen.Login.route) {
            val context = LocalContext.current
            LoginScreen(
                viewModel = hiltViewModel(), navController = navController,
                context = context
            )
        }
        composable(Screen.Home.route) {
            beranda(
                navController = navController,
                onNavigateToPegawai = { navController.navigate(Screen.Pegawai.route) },
                onNavigateToMenu = { navController.navigate(Screen.MenuManager.route) },
                onNavigateToKategori = { navController.navigate(Screen.Kategori.route) },
                onNavigateToSubKategori = { navController.navigate(Screen.SubKategori.route) },
                onNavigateToDeposit = { navController.navigate(Screen.Deposit.route) },
                onNavigateToTransaksi = { navController.navigate(Screen.TransaksiManager.route) },
                onNavigateToWarung = { navController.navigate(Screen.EditWarung.route)}
            )
        }
        composable(Screen.HomePegawai.route){
            BerandaPegawai(
                navController = navController
            )
        }
        composable(Screen.Pegawai.route) {
            PegawaiManager(
                navController = navController
            )
        }
        composable(Screen.TransaksiManager.route) {
            TransaksiManager(
                navController = navController
            )
        }
        composable(Screen.CreatePegawai.route) {
            CreatePegawai(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable (Screen.EditWarung.route) {
            EditWarung(
                navController = navController
            )
        }
        composable(Screen.Menu.route) {
            MenuPegawai(
                navController = navController
            )
        }
        composable(Screen.MenuAdmin.route) {
            MenuAdmin(
                navController = navController
            )
        }
        composable(Screen.MenuManager.route) {
            MenuManager(
                navController = navController
            )
        }
        composable(Screen.CartDetail.route) {
            DetailPesananScreen(
                navController = navController
            )
        }
        composable (Screen.Transaksi.route) {
            TransaksiScreen(
                navController = navController,
                onNavigateToDetail = { transactionId ->
                    navController.navigate(Screen.DetailSelesai.createRoute(transactionId))
                },
                onNavigateToPayment = { transactionId ->
                    navController.navigate(Screen.PaymentUpdate.createRoute(transactionId))
                }
            )
        }
        composable(
            route = Screen.DetailSelesai.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getInt("transactionId")
            transactionId?.let {
                DetailPesananScreenSelesai(
                    navController = navController,
                    transactionId = it,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        composable(
            route = Screen.PaymentUpdate.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getInt("transactionId")
            transactionId?.let {
                PaymentUpdateScreen(
                    navController = navController,
                    transactionId = it
                )
            }
        }
        composable(Screen.Rekap.route) {
            RekapScreen(
                navController = navController
            )
        }
//        composable(
//            route = Screen.DetailSelesai.route,
//            arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
//        ) { backStackEntry ->
//            val transactionId = backStackEntry.arguments?.getInt("transactionId")
//            transactionId?.let {
//                DetailPesananScreenSelesai(
//                    navController = navController,
//                    transactionId = it,
//                    onNavigateBack = { navController.popBackStack() }
//                )
//            }
//        }
        composable(Screen.Kategori.route) {
            KategoriManager(
                navController = navController
            )
        }
        composable(Screen.CreateKategori.route) {
            CreateKategori(
                onNavigateBack = { navController.navigateUp() }
            )
        }
        composable(Screen.CreatePengeluaran.route) {
            CreatePengeluaran(
                onNavigateBack = { navController.navigateUp()}
            )
        }
        composable(Screen.SubKategori.route) {
            SubKategoriManager(
                navController = navController
            )
        }

        composable(Screen.CreateMenu.route) {
            BuatMenuScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable(Screen.Deposit.route) {
            DepositManager(
                navController = navController
            )
        }
        composable(Screen.CreateDeposit.route) {
            CreateDeposit(
                onNavigateBack = {
                    navController.navigateUp()
                },
                navController = navController
            )
        }
        composable(
            route = Screen.EditDeposit.route,
            arguments = listOf(navArgument("depositId") { type = NavType.IntType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getInt("depositId") ?: return@composable
            EditDeposit(
                customerId = customerId,
                onNavigateBack = { navController.navigateUp() },
                navController = navController
            )
        }
        composable(
            route = "edit_menu/{menuId}",
            arguments = listOf(navArgument("menuId") { type = NavType.IntType })
        ) { backStackEntry ->
            val menuId = backStackEntry.arguments?.getInt("menuId") ?: return@composable
            EditMenuScreen(
                navController = navController,
                menuId = menuId
            )
        }
        composable(
            route = Screen.EditKategori.route,
            arguments = listOf(navArgument("kategoriId") { type = NavType.IntType })
        ) { backStackEntry ->
            val kategoriId = backStackEntry.arguments?.getInt("kategoriId") ?: return@composable
            EditKategori(
                navController = navController,
                kategoriId = kategoriId
            )
        }
        composable(Screen.CreateSubKategori.route) {
            CreateSubKategori(
                navController = navController,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable(
            route = Screen.EditSubKategori.route,
            arguments = listOf(navArgument("subKategoriId") { type = NavType.IntType })
        ) { backStackEntry ->
            val subKategoriId =
                backStackEntry.arguments?.getInt("subKategoriId") ?: return@composable
            EditSubKategori(
                navController = navController,
                subKategoriId = subKategoriId,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable(
            route = Screen.DetailPembayaran.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getInt("transactionId")
            transactionId?.let {
                DetailPembayaran(
                    navController = navController,
                    transactionId = it,
                    onNavigateBack = { navController.popBackStack() } // Optional: Handle back navigation
                )
            }
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getInt("transactionId")
            transactionId?.let {
                DetailPesananScreenUpdate(
                    navController = navController,
                    transactionId = it,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
