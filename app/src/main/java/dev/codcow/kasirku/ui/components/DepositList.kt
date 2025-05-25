package dev.codcow.kasirku.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.codcow.kasirku.core.data.model.deposit.DataDeposit
import dev.codcow.kasirku.ui.theme.AppTheme

@Composable
fun DepositItem(
    deposit: DataDeposit,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onItemClick: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable{onItemClick(deposit.customer_id)}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column (
                modifier = Modifier.weight(1f)
            ){
                Text(
                    text = deposit.customer_name ,
                    style = AppTheme.typography.paragraph1Semibold,
                    color = Color.Black
                )

                Text(
                    text = deposit.phone_number,
                    style = AppTheme.typography.paragraph2,
                    color = Color.Black
                )
            }




            // Tombol Edit
            IconButton(onClick = { onEditClick(deposit.customer_id)}) {
                Icon(
                    imageVector = Icons.Default.AddCircleOutline,
                    contentDescription = "Edit",
                    tint = AppTheme.colors.surface
                )
            }

            // Tombol Delete
            IconButton(onClick = { onDeleteClick(deposit.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}

