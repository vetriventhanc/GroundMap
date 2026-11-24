
package com.groundmap.android.ui.assets

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetOperationsScreen(
    asset: AssetData,
    onBack: () -> Unit,
    onViewDetails: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onUpdateCondition: (String) -> Unit,
    onReportFault: () -> Unit,
    onViewHistory: () -> Unit,
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
            .background(OperationsBackground)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                "ASSET OPERATIONS",
                color = OperationsAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                asset.name,
                color = OperationsWhite,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                asset.assetCode,
                color = OperationsMuted,
                fontSize = 14.sp
            )
        }

        // CURRENT ASSET INFORMATION

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
                    "CURRENT ASSET CONDITION",
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
                            "Status",
                            color = OperationsMuted,
                            fontSize = 12.sp
                        )

                        Text(
                            asset.status.replaceFirstChar {
                                it.uppercase()
                            },
                            color = OperationsGreen,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            "Condition",
                            color = OperationsMuted,
                            fontSize = 12.sp
                        )

                        Text(
                            asset.condition.replaceFirstChar {
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

        // STATUS UPDATE

        OperationSection("Update Operational Status") {

            ExposedDropdownMenuBox(
                expanded = statusMenu,
                onExpandedChange = {
                    statusMenu = it
                }
            ) {
                OutlinedTextField(
                    value = selectedStatus.replaceFirstChar {
                        it.uppercase()
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select status") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = statusMenu
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = operationFieldColors()
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

            statusError?.let {
                Text(it, color = OperationsRed, fontSize = 12.sp)
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
                    containerColor = OperationsAccent
                )
            ) {
                if (statusSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = OperationsWhite
                    )
                } else {
                    Text("Save Status")
                }
            }
        }

        // CONDITION UPDATE

        OperationSection("Update Physical Condition") {

            ExposedDropdownMenuBox(
                expanded = conditionMenu,
                onExpandedChange = {
                    conditionMenu = it
                }
            ) {
                OutlinedTextField(
                    value = selectedCondition.replaceFirstChar {
                        it.uppercase()
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select condition") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = conditionMenu
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = operationFieldColors()
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

            conditionError?.let {
                Text(it, color = OperationsRed, fontSize = 12.sp)
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
                    containerColor = OperationsGreen
                )
            ) {
                if (conditionSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = OperationsWhite
                    )
                } else {
                    Text("Save Condition")
                }
            }
        }

        // OTHER OPERATIONS

        Text(
            "Other Operations",
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

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun OperationSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = OperationsCard
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                title,
                color = OperationsWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            content()
        }
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
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                symbol,
                color = color,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    title,
                    color = OperationsWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    description,
                    color = OperationsMuted,
                    fontSize = 11.sp
                )
            }

            Text(
                "›",
                color = color,
                fontSize = 25.sp
            )
        }
    }
}

@Composable
private fun operationFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = OperationsWhite,
        unfocusedTextColor = OperationsWhite,
        focusedBorderColor = OperationsAccent,
        unfocusedBorderColor = Color(0xFF334155),
        focusedLabelColor = OperationsAccent,
        unfocusedLabelColor = OperationsMuted,
        cursorColor = OperationsAccent,
        focusedContainerColor = OperationsCard,
        unfocusedContainerColor = OperationsCard
    )
