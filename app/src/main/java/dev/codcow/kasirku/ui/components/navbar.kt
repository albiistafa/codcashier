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
    val items = tabLabels()
    val selectedIndex = remember(currentRoute) { selectedTabIndex(currentRoute) }

    // Surface paints behind the gesture bar; only the content is inset
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
                clip = false
            )
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 82.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().selectableGroup(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, label ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .selectable(selected = selectedIndex == index, role = Role.Tab) {
                                navController.navigateToTab(tabRoutes[index])
                            }
                            .heightIn(min = 48.dp)
                            .padding(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = tabIcons[index]),
                            contentDescription = null,
                            tint = if (selectedIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = label,
                            style = AppTheme.typography.paragraph2,
                            color = if (selectedIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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

private val tabIcons = listOf(R.drawable.navbarhome, R.drawable.navbarshop, R.drawable.navbarrekap)
private val tabRoutes = listOf(Screen.Menu.route, Screen.Transaksi.route, Screen.Rekap.route)

@Composable
private fun tabLabels() = listOf(
    stringResource(R.string.nav_home),
    stringResource(R.string.nav_transactions),
    stringResource(R.string.nav_recap)
)

private fun selectedTabIndex(route: String?) =
    tabRoutes.indexOf(if (route == Screen.MenuAdmin.route) Screen.Menu.route else route)

/** Material window size class boundary: compact < 600dp uses the bottom bar, wider gets a rail. */
@Composable
fun isWideWindow(): Boolean = LocalConfiguration.current.screenWidthDp >= 600

/** Expanded width class (840dp+): room for the menu grid and the cart side by side. */
@Composable
fun isExpandedWindow(): Boolean = LocalConfiguration.current.screenWidthDp >= 840

/** Tab switch that keeps one copy of each tab on the stack, so Back exits instead of cycling tabs. */
fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(Screen.Menu.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavigationRail(currentRoute: String?, onSelect: (String) -> Unit) {
    val labels = tabLabels()
    val selectedIndex = selectedTabIndex(currentRoute)
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxHeight()
    ) {
        Spacer(Modifier.weight(1f))
        labels.forEachIndexed { index, label ->
            NavigationRailItem(
                selected = selectedIndex == index,
                onClick = { onSelect(tabRoutes[index]) },
                icon = { Icon(painterResource(tabIcons[index]), contentDescription = null, modifier = Modifier.size(24.dp)) },
                label = { Text(label, style = AppTheme.typography.paragraph2) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        Spacer(Modifier.weight(1f))
    }
}
