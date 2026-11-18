
package com.groundmap.android.ui.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
private val DetailGreen = Color(0xFF22C55E)
private val DetailRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailsScreen(
    asset: AssetData,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onViewHistory: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onUpdateCondition: (String) -> Unit,
    statusSaving: Boolean,
    conditionSaving: Boolean,
    statusError: String?,
    conditionError: String?,
    modifier: Modifier = Modifier
) {
    var selectedStatus by remember(asset.id, asset.status) {
        mutableStateOf(asset.status)
    }

    var selectedCondition by remember(asset.id, asset.condition) {
        mutableStateOf(asset.condition)
    }

    var statusMenu by remember { mutableStateOf(false) }
    var conditionMenu by remember { mutableStateOf(false) }

    val statuses = listOf(
        "active",
        "maintenance",
        "inactive",
        "retired"
    )

    val conditions = listOf(
        "excellent",
        "good",
        "fair",
        "poor",
        "damaged"
    )

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

        // STATUS MANAGEMENT

        DetailCardSection("Operational Status") {

            ExposedDropdownMenuBox(
                expanded = statusMenu,
                onExpandedChange = { statusMenu = it }
            ) {
                OutlinedTextField(
                    value = selectedStatus,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Asset status") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = statusMenu
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = detailFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = statusMenu,
                    onDismissRequest = {
                        statusMenu = false
                    }
                ) {
                    statuses.forEach { status ->
                        DropdownMenuItem(
                            text = {
                                Text(status.replaceFirstChar {
                                    it.uppercase()
                                })
                            },
                            onClick = {
                                selectedStatus = status
                                statusMenu = false
                            }
                        )
                    }
                }
            }

            if (statusError != null) {
                Text(
                    statusError,
                    color = DetailRed,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = {
                    onUpdateStatus(selectedStatus)
                },
                enabled = !statusSaving &&
                        !conditionSaving &&
                        selectedStatus != asset.status,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DetailAccent
                )
            ) {
                if (statusSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = DetailWhite
                    )
                } else {
                    Text("Save Status")
                }
            }
        }

        // CONDITION MANAGEMENT

        DetailCardSection("Physical Condition") {

            ExposedDropdownMenuBox(
                expanded = conditionMenu,
                onExpandedChange = {
                    conditionMenu = it
                }
            ) {
                OutlinedTextField(
                    value = selectedCondition,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Asset condition") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = conditionMenu
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = detailFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = conditionMenu,
                    onDismissRequest = {
                        conditionMenu = false
                    }
                ) {
                    conditions.forEach { condition ->
                        DropdownMenuItem(
                            text = {
                                Text(condition.replaceFirstChar {
                                    it.uppercase()
                                })
                            },
                            onClick = {
                                selectedCondition = condition
                                conditionMenu = false
                            }
                        )
                    }
                }
            }

            if (conditionError != null) {
                Text(
                    conditionError,
                    color = DetailRed,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = {
                    onUpdateCondition(selectedCondition)
                },
                enabled = !conditionSaving &&
                        !statusSaving &&
                        selectedCondition != asset.condition,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DetailGreen
                )
            ) {
                if (conditionSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = DetailWhite
                    )
                } else {
                    Text("Save Condition")
                }
            }
        }

        // EQUIPMENT INFORMATION

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

        OutlinedButton(
            onClick = onViewHistory,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = DetailAccent
            )
        ) {
            Text("View Asset History")
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
            Text("Edit Asset Information")
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

@Composable
private fun detailFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = DetailWhite,
        unfocusedTextColor = DetailWhite,
        focusedBorderColor = DetailAccent,
        unfocusedBorderColor = Color(0xFF334155),
        focusedLabelColor = DetailAccent,
        unfocusedLabelColor = DetailMuted,
        cursorColor = DetailAccent,
        focusedContainerColor = DetailCard,
        unfocusedContainerColor = DetailCard
    )
