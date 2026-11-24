
package com.groundmap.android.ui.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groundmap.android.data.remote.AssetData

private val OperationsBackground = Color(0xFF0B1120)
private val OperationsCard = Color(0xFF151F32)
private val OperationsAccent = Color(0xFF3B82F6)
private val OperationsGreen = Color(0xFF22C55E)
private val OperationsOrange = Color(0xFFF59E0B)
private val OperationsRed = Color(0xFFEF4444)
private val OperationsWhite = Color(0xFFF8FAFC)
private val OperationsMuted = Color(0xFF94A3B8)

@Composable
fun AssetOperationsScreen(
    asset: AssetData,
    onBack: () -> Unit,
    onViewDetails: () -> Unit,
    onUpdateStatus: () -> Unit,
    onUpdateCondition: () -> Unit,
    onReportFault: () -> Unit,
    onViewHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OperationsBackground)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        OutlinedButton(
            onClick = onBack,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = OperationsAccent
            )
        ) {
            Text("← Back to Scanner")
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "ASSET OPERATIONS",
                color = OperationsAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = asset.name,
                color = OperationsWhite,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = asset.assetCode,
                color = OperationsMuted,
                fontSize = 14.sp
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = OperationsCard
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "CURRENT ASSET CONDITION",
                    color = OperationsMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Status",
                            color = OperationsMuted,
                            fontSize = 12.sp
                        )

                        Text(
                            text = asset.status.replaceFirstChar {
                                it.uppercase()
                            },
                            color = OperationsGreen,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = "Condition",
                            color = OperationsMuted,
                            fontSize = 12.sp
                        )

                        Text(
                            text = asset.condition.replaceFirstChar {
                                it.uppercase()
                            },
                            color = OperationsOrange,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Text(
            text = "Choose an operation",
            color = OperationsWhite,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        OperationButton(
            title = "Asset Details",
            description = "View complete asset information",
            symbol = "▣",
            color = OperationsAccent,
            onClick = onViewDetails
        )

        OperationButton(
            title = "Update Status",
            description = "Active, maintenance, inactive or retired",
            symbol = "↻",
            color = OperationsAccent,
            onClick = onUpdateStatus
        )

        OperationButton(
            title = "Update Condition",
            description = "Excellent, good, fair, poor or damaged",
            symbol = "♥",
            color = OperationsGreen,
            onClick = onUpdateCondition
        )

        OperationButton(
            title = "Report Fault",
            description = "Report a problem with this asset",
            symbol = "!",
            color = OperationsRed,
            onClick = onReportFault
        )

        OperationButton(
            title = "Asset History",
            description = "View previous asset activities",
            symbol = "◷",
            color = OperationsOrange,
            onClick = onViewHistory
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun OperationButton(
    title: String,
    description: String,
    symbol: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = OperationsCard
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = symbol,
                color = color,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    color = OperationsWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    color = OperationsMuted,
                    fontSize = 11.sp
                )
            }

            Text(
                text = "›",
                color = color,
                fontSize = 25.sp
            )
        }
    }
}
