import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.R

@Composable
fun CustomBottomNavigation(
    modifier: Modifier = Modifier,
    currentRoute: String?,
    onNavigateToHome: () -> Unit = {},
    onNavigateToTransaksi: () -> Unit ={},
    onNavigateToRekap: () -> Unit = {}
) {

    val items = listOf("Beranda", "Transaksi", "Rekap")
    val icons = listOf(R.drawable.navbarhome, R.drawable.navbarshop, R.drawable.navbarrekap)
    val routes = listOf(Screen.Menu.route, Screen.Transaksi.route, Screen.Rekap.route)

    val selectedIndex = remember(currentRoute) {
        routes.indexOf(currentRoute)
    }

    // SOLUSI: Tambahkan windowInsetsPadding untuk navigation bars
    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars) // PENTING: Tambahkan ini
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
                    clip = false
                )
                .background(Color(0xFFF3F7FC), shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, label ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(1f)
                            .clickable {
                                when (index) {
                                    0 -> onNavigateToHome()
                                    1 -> onNavigateToTransaksi()
                                    2 -> onNavigateToRekap()
                                }
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = icons[index]),
                            contentDescription = label,
                            tint = if (selectedIndex == index) Color(0xFF5F7C1E) else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = label,
                            style = AppTheme.typography.paragraph2,
                            color = if (selectedIndex == index) Color(0xFF5F7C1E) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}