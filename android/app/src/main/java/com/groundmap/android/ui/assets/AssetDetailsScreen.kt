
package com.groundmap.android.ui.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groundmap.android.data.remote.AssetData

private val DetailBackground = Color(0xFF0B1120)
private val DetailCard = Color(0xFF151F32)
private val DetailWhite = Color(0xFFF8FAFC)
private val DetailMuted = Color(0xFF94A3B8)
private val DetailAccent = Color(0xFF3B82F6)

@Composable
fun AssetDetailsScreen(
    asset: AssetData,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DetailBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back to Assets", color = DetailAccent)
        }

        Text(
            "ASSET DETAILS",
            color = DetailAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            asset.name,
            color = DetailWhite,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            asset.assetCode,
            color = DetailMuted,
            fontSize = 14.sp
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = DetailCard
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Text(
                    "Current Status",
                    color = DetailMuted,
                    fontSize = 12.sp
                )

                DetailValue("Status", asset.status)
                DetailValue("Condition", asset.condition)
            }
        }

        DetailCardSection("Equipment Information") {
            DetailValue("Category", asset.category)
            DetailValue("Manufacturer", asset.manufacturer)
            DetailValue("Model Number", asset.modelNumber)
            DetailValue("Serial Number", asset.serialNumber)
        }

        DetailCardSection("Location Information") {
            DetailValue("Workspace ID", asset.workspaceId)
            DetailValue("Room ID", asset.roomId)
        }

        DetailCardSection("Purchase Information") {
            DetailValue("Purchase Date", asset.purchaseDate)
        }

        DetailCardSection("Additional Information") {
            DetailValue("Notes", asset.notes)
        }

        Button(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DetailAccent
            )
        ) {
            Text("Edit Asset")
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun DetailCardSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = DetailCard
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                title,
                color = DetailAccent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            content()
        }
    }
}

@Composable
private fun DetailValue(
    label: String,
    value: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            color = DetailMuted,
            fontSize = 11.sp
        )

        Text(
            value?.takeIf { it.isNotBlank() } ?: "Not specified",
            color = DetailWhite,
            fontSize = 14.sp
        )
    }
}
