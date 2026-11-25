package com.groundmap.android.ui.assets

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groundmap.android.data.remote.AssetData
import com.groundmap.android.data.remote.RoomLocationData

private val DetailBackground = Color(0xFF0B1024)
private val DetailCard = Color(0xFF171D38)
private val DetailCardInner = Color(0xFF202747)
private val DetailWhite = Color(0xFFF8FAFC)
private val DetailMuted = Color(0xFFA8B2D1)
private val DetailAccent = Color(0xFF8B5CF6)
private val DetailGreen = Color(0xFF65D6B0)
private val DetailAmber = Color(0xFFFFC66D)
private val DetailRed = Color(0xFFFF8295)

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
    qrImageBytes: ByteArray?,
    qrLoading: Boolean,
    qrError: String?,
    onRefreshQr: () -> Unit,
    rooms: List<RoomLocationData>,
    locationSaving: Boolean,
    locationError: String?,
    onUpdateLocation: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatus by remember(asset.id, asset.status) {
        mutableStateOf(asset.status)
    }
    var selectedCondition by remember(asset.id, asset.condition) {
        mutableStateOf(asset.condition)
    }
    var selectedRoomId by remember(asset.id, asset.roomId) {
        mutableStateOf(asset.roomId)
    }

    var statusMenu by remember { mutableStateOf(false) }
    var conditionMenu by remember { mutableStateOf(false) }
    var roomMenu by remember { mutableStateOf(false) }

    val statuses = listOf("active", "maintenance", "inactive", "retired")
    val conditions = listOf("excellent", "good", "fair", "poor", "damaged")

    val qrBitmap = remember(qrImageBytes) {
        qrImageBytes?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    }

    val statusColor = when (asset.status.lowercase()) {
        "active" -> DetailGreen
        "maintenance" -> DetailAmber
        "inactive", "retired" -> DetailRed
        else -> DetailMuted
    }

    val conditionColor = when (asset.condition.lowercase()) {
        "excellent", "good", "new" -> DetailGreen
        "fair" -> DetailAmber
        "poor", "damaged" -> DetailRed
        else -> DetailMuted
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DetailBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(
            onClick = onBack,
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
        ) {
            Text("← Back to Assets", color = DetailAccent, fontSize = 13.sp)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "ASSET DETAILS",
                color = DetailAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.8.sp
            )

            Text(
                asset.name,
                color = DetailWhite,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                asset.assetCode,
                color = DetailMuted,
                fontSize = 13.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DetailTag(
                    text = asset.status.replaceFirstChar { it.uppercase() },
                    color = statusColor
                )
                DetailTag(
                    text = asset.condition.replaceFirstChar { it.uppercase() },
                    color = conditionColor
                )
            }
        }

        // QR CODE
        DetailCardSection(
            title = "Asset QR Code",
            subtitle = "Scan this code to identify the asset."
        ) {
            when {
                qrLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DetailAccent)
                    }
                }

                qrError != null -> {
                    Text(qrError, color = DetailRed, fontSize = 12.sp)
                    OutlinedButton(
                        onClick = onRefreshQr,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Retry QR Code")
                    }
                }

                qrBitmap != null -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White
                        ) {
                            Image(
                                bitmap = qrBitmap,
                                contentDescription = "QR code for ${asset.assetCode}",
                                modifier = Modifier
                                    .size(210.dp)
                                    .padding(10.dp)
                            )
                        }
                    }

                    Text(
                        asset.assetCode,
                        color = DetailWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = onRefreshQr,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DetailMuted
                        )
                    ) {
                        Text("Refresh QR Code")
                    }
                }

                else -> {
                    Text(
                        "QR image is unavailable.",
                        color = DetailMuted,
                        fontSize = 12.sp
                    )
                    OutlinedButton(
                        onClick = onRefreshQr,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Load QR Code")
                    }
                }
            }
        }

        // OPERATIONAL STATUS
        DetailCardSection(
            title = "Operational Status",
            subtitle = "Update the current operating state."
        ) {
            ExposedDropdownMenuBox(
                expanded = statusMenu,
                onExpandedChange = { statusMenu = it }
            ) {
                OutlinedTextField(
                    value = selectedStatus.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Asset status") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenu)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(14.dp),
                    colors = detailFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = statusMenu,
                    onDismissRequest = { statusMenu = false }
                ) {
                    statuses.forEach { status ->
                        DropdownMenuItem(
                            text = {
                                Text(status.replaceFirstChar { it.uppercase() })
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
                Text(statusError, color = DetailRed, fontSize = 12.sp)
            }

            Button(
                onClick = { onUpdateStatus(selectedStatus) },
                enabled = !statusSaving &&
                        !conditionSaving &&
                        selectedStatus != asset.status,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DetailAccent,
                    disabledContainerColor = Color(0xFF2B334D)
                )
            ) {
                if (statusSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = DetailWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Status")
                }
            }
        }

        // PHYSICAL CONDITION
        DetailCardSection(
            title = "Physical Condition",
            subtitle = "Record the asset's current condition."
        ) {
            ExposedDropdownMenuBox(
                expanded = conditionMenu,
                onExpandedChange = { conditionMenu = it }
            ) {
                OutlinedTextField(
                    value = selectedCondition.replaceFirstChar { it.uppercase() },
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
                    shape = RoundedCornerShape(14.dp),
                    colors = detailFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = conditionMenu,
                    onDismissRequest = { conditionMenu = false }
                ) {
                    conditions.forEach { condition ->
                        DropdownMenuItem(
                            text = {
                                Text(condition.replaceFirstChar { it.uppercase() })
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
                Text(conditionError, color = DetailRed, fontSize = 12.sp)
            }

            Button(
                onClick = { onUpdateCondition(selectedCondition) },
                enabled = !conditionSaving &&
                        !statusSaving &&
                        selectedCondition != asset.condition,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DetailAccent,
                    disabledContainerColor = Color(0xFF2B334D)
                )
            ) {
                if (conditionSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = DetailWhite,
                        strokeWidth = 2.dp
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

        // LOCATION INFORMATION
        DetailCardSection(
            title = "Location Information",
            subtitle = "Assign this asset to a room in the workspace."
        ) {
            DetailValue("Workspace ID", asset.workspaceId)

            val selectedRoom = rooms.firstOrNull { it.id == selectedRoomId }

            ExposedDropdownMenuBox(
                expanded = roomMenu,
                onExpandedChange = { roomMenu = it }
            ) {
                OutlinedTextField(
                    value = selectedRoom?.name ?: "Unassigned",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Room") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomMenu)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(14.dp),
                    colors = detailFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = roomMenu,
                    onDismissRequest = { roomMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Unassigned") },
                        onClick = {
                            selectedRoomId = null
                            roomMenu = false
                        }
                    )

                    rooms.forEach { room ->
                        DropdownMenuItem(
                            text = { Text(room.name) },
                            onClick = {
                                selectedRoomId = room.id
                                roomMenu = false
                            }
                        )
                    }
                }
            }

            if (locationError != null) {
                Text(locationError, color = DetailRed, fontSize = 12.sp)
            }

            Button(
                onClick = { onUpdateLocation(selectedRoomId) },
                enabled = !locationSaving && selectedRoomId != asset.roomId,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DetailAccent,
                    disabledContainerColor = Color(0xFF2B334D)
                )
            ) {
                if (locationSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = DetailWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Location")
                }
            }
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
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
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
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DetailAccent
            )
        ) {
            Text("Edit Asset Information")
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DetailCardSection(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DetailCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    title,
                    color = DetailAccent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                if (subtitle != null) {
                    Text(
                        subtitle,
                        color = DetailMuted,
                        fontSize = 11.sp
                    )
                }
            }

            content()
        }
    }
}

@Composable
private fun DetailTag(
    text: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.13f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DetailValue(
    label: String,
    value: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            label,
            color = DetailMuted,
            fontSize = 11.sp
        )

        Text(
            value?.takeIf { it.isNotBlank() } ?: "Not specified",
            color = DetailWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun detailFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = DetailWhite,
        unfocusedTextColor = DetailWhite,
        focusedBorderColor = DetailAccent,
        unfocusedBorderColor = Color(0xFF3A456B),
        focusedLabelColor = DetailAccent,
        unfocusedLabelColor = DetailMuted,
        cursorColor = DetailAccent,
        focusedContainerColor = DetailCardInner,
        unfocusedContainerColor = DetailCardInner
    )