import dev.codcow.kasirku.ui.components.NavIcons
import dev.codcow.kasirku.ui.components.DuoIcon
import dev.codcow.kasirku.ui.components.Duo
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.codcow.kasirku.middleware.Screen
import dev.codcow.kasirku.ui.theme.AppTheme
import dev.codcow.kasirku.R

@Composable
fun CustomBottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val currentRoute = currentRoute(navController)

    // Wide windows get AppNavigationRail from AppNavGraph instead; keep only the inset
    if (isWideWindow()) {
        Spacer(modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        return
    }
    val tabs = navTabs()
    val selected = selectedTab(tabs, currentRoute)
    val line = MaterialTheme.colorScheme.outlineVariant

    // Surface paints behind the gesture bar; only the content is inset
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .drawBehind { drawLine(line, Offset.Zero, Offset(size.width, 0f), 1.dp.toPx()) }
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).selectableGroup(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val active = tab == selected
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .selectable(selected = active, role = Role.Tab) { navController.navigateToTab(tab.route) }
                        .padding(vertical = 10.dp)
                ) {
                    TabIcon(tab, active)
                    Text(
                        text = stringResource(tab.label),
                        style = AppTheme.typography.paragraph2.copy(
                            fontSize = 12.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TabIcon(tab: NavTab, active: Boolean) = DuoIcon(
    tab.icon, 26.dp,
    fill = if (active) AppTheme.colors.money else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
    stroke = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
)

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

private class NavTab(val route: String, val label: Int, val icon: Duo)

private val homeTab = NavTab(Screen.Home.route, R.string.nav_home, NavIcons.home)
private val cashierTab = NavTab(Screen.Menu.route, R.string.nav_cashier, NavIcons.kasir)
private val transaksiTab = NavTab(Screen.Transaksi.route, R.string.nav_transactions, NavIcons.transaksi)
private val rekapTab = NavTab(Screen.Rekap.route, R.string.nav_recap, NavIcons.rekap)

/** Owner gets Beranda; staff land straight in Kasir (decision 19 Okt). */
@Composable
private fun navTabs(): List<NavTab> {
    val context = LocalContext.current
    val isAdmin = remember {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("user_role", null) == "admin"
    }
    return if (isAdmin) listOf(homeTab, cashierTab, transaksiTab, rekapTab) else listOf(cashierTab, transaksiTab, rekapTab)
}

private fun selectedTab(tabs: List<NavTab>, route: String?) =
    tabs.firstOrNull { it.route == route }

/** Material window size class boundary: compact < 600dp uses the bottom bar, wider gets a rail. */
@Composable
fun isWideWindow(): Boolean = LocalConfiguration.current.screenWidthDp >= 600

/** Expanded width class (840dp+): room for the menu grid and the cart side by side. */
@Composable
fun isExpandedWindow(): Boolean = LocalConfiguration.current.screenWidthDp >= 840

/** Tab switch that keeps one copy of each tab on the stack, so Back exits instead of cycling tabs. */
fun NavController.navigateToTab(route: String) {
    val root = if (runCatching { getBackStackEntry(Screen.Home.route) }.isSuccess) Screen.Home.route else Screen.Menu.route
    navigate(route) {
        popUpTo(root) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavigationRail(currentRoute: String?, onSelect: (String) -> Unit) {
    val tabs = navTabs()
    val selected = selectedTab(tabs, currentRoute)
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight()
    ) {
        Spacer(Modifier.weight(1f))
        tabs.forEach { tab ->
            val active = tab == selected
            NavigationRailItem(
                selected = active,
                onClick = { onSelect(tab.route) },
                icon = { TabIcon(tab, active) },
                label = { Text(stringResource(tab.label), style = AppTheme.typography.paragraph2.copy(fontSize = 12.sp)) },
                colors = NavigationRailItemDefaults.colors(
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        Spacer(Modifier.weight(1f))
    }
}
