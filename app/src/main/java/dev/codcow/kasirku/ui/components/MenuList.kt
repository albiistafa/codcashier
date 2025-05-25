package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.codcow.kasirku.ui.theme.AppTheme

enum class UserRole {
    ADMIN,
    EMPLOYEE
}

@Composable
fun MenuScreen(
    onMenuSelected: (String) -> Unit,
    onLogoutSelected: () -> Unit,
    userRole: UserRole = UserRole.ADMIN // Default to ADMIN if not specified
) {
    // Define menu items based on user role
    val items = when (userRole) {
        UserRole.ADMIN -> listOf(
            Pair(Icons.Outlined.Groups, "Pegawai"),
            Pair(Icons.Outlined.Storefront, "Menu Manager"),
            Pair(Icons.Outlined.Folder, "Kategori"),
            Pair(Icons.Outlined.FolderCopy, "Sub-kategori"),
            Pair(Icons.Outlined.Person, "Deposit"),
            Pair(Icons.Outlined.Analytics, "Transaksi Manager"),
            Pair(Icons.Outlined.Settings, "Warung"),
            Pair(Icons.Outlined.Logout, "Log Out")
        )
        UserRole.EMPLOYEE -> listOf(
            Pair(Icons.Outlined.Person, "Deposit"),
            Pair(Icons.Outlined.Logout, "Log Out")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        LazyColumn {
            items(count = items.size) { index ->
                val (icon, title) = items[index]
                if (title == "Log Out") {
                    MenuItem(icon, title) { onLogoutSelected() } // Panggil callback logout
                } else {
                    MenuItem(icon, title) { onMenuSelected(title) } // Panggil callback menu lainnya
                }
            }
        }
    }
}

@Composable
fun MenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp)
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = Color.Black
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                style = AppTheme.typography.paragraph1
            )
        }
        Divider(color = Color.Gray.copy(alpha = 0.3f), thickness = 0.5.dp)
    }
}