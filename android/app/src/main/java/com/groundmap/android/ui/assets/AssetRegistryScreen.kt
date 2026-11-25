
package com.groundmap.android.ui.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

private val AssetBackground = Color(0xFF0B1120)
private val AssetCard = Color(0xFF151F32)
private val AssetAccent = Color(0xFF3B82F6)
private val AssetMuted = Color(0xFF94A3B8)
private val AssetWhite = Color(0xFFF8FAFC)

private val AssetStatuses = listOf(
    "All",
    "active",
    "maintenance",
    "inactive",
    "retired"
)

private val AssetConditions = listOf(
    "All",
    "excellent",
    "good",
    "fair",
    "poor",
    "damaged"
)

@Composable
fun AssetRegistryScreen(
    assets: List<AssetData>,
    loading: Boolean,
    error: String?,
    onAssetSelected: (AssetData) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("All") }
    var selectedCondition by remember { mutableStateOf("All") }

    val filteredAssets = remember(
        assets,
        searchQuery,
        selectedStatus,
        selectedCondition
    ) {
        assets.filter { asset ->
            val matchesSearch =
                searchQuery.isBlank() ||
                        asset.name.contains(searchQuery, ignoreCase = true) ||
                        asset.assetCode.contains(searchQuery, ignoreCase = true) ||
                        (asset.serialNumber?.contains(
                            searchQuery,
                            ignoreCase = true
                        ) == true)

            val matchesStatus =
                selectedStatus == "All" ||
                        asset.status.equals(selectedStatus, ignoreCase = true)

            val matchesCondition =
                selectedCondition == "All" ||
                        asset.condition.equals(selectedCondition, ignoreCase = true)

            matchesSearch && matchesStatus && matchesCondition
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AssetBackground)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "GROUNDMAP",
                    color = AssetAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.height(7.dp))

                Text(
                    "Asset Registry",
                    color = AssetWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(onClick = onRefresh) {
                Text("Refresh ↻", color = AssetAccent)
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            "FILTER BY CONDITION",
            color = AssetMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AssetConditions.forEach { condition ->
                val selected = selectedCondition == condition

                FilterChip(
                    selected = selected,
                    onClick = { selectedCondition = condition },
                    label = {
                        Text(
                            if (condition == "All") "All"
                            else condition.replaceFirstChar { it.uppercase() },
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AssetAccent,
                        selectedLabelColor = Color.White,
                        containerColor = AssetCard,
                        labelColor = AssetMuted
                    )
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    "Search name, asset code or serial...",
                    color = AssetMuted,
                    fontSize = 12.sp
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AssetWhite,
                unfocusedTextColor = AssetWhite,
                focusedBorderColor = AssetAccent,
                unfocusedBorderColor = Color(0xFF334155),
                cursorColor = AssetAccent,
                focusedContainerColor = AssetCard,
                unfocusedContainerColor = AssetCard
            )
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "FILTER BY STATUS",
            color = AssetMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AssetStatuses.forEach { status ->

                val selected = selectedStatus == status

                FilterChip(
                    selected = selected,
                    onClick = { selectedStatus = status },
                    label = {
                        Text(
                            when (status) {
                                "All" -> "All"
                                "active" -> "Active"
                                "maintenance" -> "Repair"
                                "inactive" -> "Inactive"
                                else -> "Retired"
                            },
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AssetAccent,
                        selectedLabelColor = Color.White,
                        containerColor = AssetCard,
                        labelColor = AssetMuted
                    )
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            "${filteredAssets.size} asset(s) found",
            color = AssetMuted,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(12.dp))

        when {
            loading -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AssetAccent)
                }
            }

            error != null -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Unable to load assets",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        error,
                        color = AssetMuted,
                        fontSize = 12.sp
                    )

                    TextButton(onClick = onRefresh) {
                        Text("Try again")
                    }
                }
            }

            filteredAssets.isEmpty() -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("▦", color = AssetAccent, fontSize = 42.sp)

                        Spacer(Modifier.height(10.dp))

                        Text(
                            if (assets.isEmpty()) {
                                "No assets registered yet"
                            } else {
                                "No matching assets found"
                            },
                            color = AssetWhite,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            "Try changing your search or filter.",
                            color = AssetMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(
                        items = filteredAssets,
                        key = { it.id }
                    ) { asset ->

                        AssetCardItem(
                            asset = asset,
                            onClick = {
                                onAssetSelected(asset)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssetCardItem(
    asset: AssetData,
    onClick: () -> Unit
) {
    val statusColor = when (asset.status.lowercase()) {
        "active" -> Color(0xFF22C55E)
        "maintenance" -> Color(0xFFF59E0B)
        "inactive" -> Color(0xFF94A3B8)
        "retired" -> Color(0xFFEF4444)
        else -> AssetMuted
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AssetCard
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        asset.name,
                        color = AssetWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        asset.assetCode,
                        color = AssetAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        asset.status.uppercase(),
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 6.dp
                        ),
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF28354A))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "CATEGORY",
                        color = AssetMuted,
                        fontSize = 9.sp
                    )

                    Text(
                        asset.category,
                        color = AssetWhite,
                        fontSize = 12.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "CONDITION",
                        color = AssetMuted,
                        fontSize = 9.sp
                    )

                    Text(
                        asset.condition.replaceFirstChar {
                            it.uppercase()
                        },
                        color = if (asset.condition == "damaged") {
                            Color(0xFFEF4444)
                        } else {
                            AssetWhite
                        },
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                "View asset details  →",
                color = AssetAccent,
                fontSize = 11.sp
            )
        }
    }
}
