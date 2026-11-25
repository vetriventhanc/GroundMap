package com.groundmap.android.ui.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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

private val AssetBackground = Color(0xFF0B1024)
private val AssetPanel = Color(0xFF171D38)
private val AssetCard = Color(0xFF1C2443)
private val AssetPurple = Color(0xFF8B5CF6)
private val AssetMuted = Color(0xFFA8B2D1)
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
    "new",
    "good",
    "fair",
    "poor",
    "damaged"
)

private val AssetSortOptions = listOf(
    "Name (A–Z)",
    "Name (Z–A)",
    "Asset Code",
    "Condition",
    "Status"
)

@OptIn(ExperimentalMaterial3Api::class)
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
    var selectedSort by remember { mutableStateOf("Name (A–Z)") }
    var sortMenuExpanded by remember { mutableStateOf(false) }

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
                        asset.serialNumber?.contains(
                            searchQuery,
                            ignoreCase = true
                        ) == true

            val matchesStatus =
                selectedStatus == "All" ||
                        asset.status.equals(selectedStatus, ignoreCase = true)

            val matchesCondition =
                selectedCondition == "All" ||
                        asset.condition.equals(selectedCondition, ignoreCase = true)

            matchesSearch && matchesStatus && matchesCondition
        }
    }

    val sortedAssets = remember(filteredAssets, selectedSort) {
        when (selectedSort) {
            "Name (Z–A)" ->
                filteredAssets.sortedByDescending { it.name.lowercase() }

            "Asset Code" ->
                filteredAssets.sortedBy { it.assetCode.lowercase() }

            "Condition" ->
                filteredAssets.sortedBy { it.condition.lowercase() }

            "Status" ->
                filteredAssets.sortedBy { it.status.lowercase() }

            else ->
                filteredAssets.sortedBy { it.name.lowercase() }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AssetBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GROUNDMAP",
                    color = AssetPurple,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = "Asset Registry",
                    color = AssetWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Browse and manage your assets",
                    color = AssetMuted,
                    fontSize = 12.sp
                )
            }

            TextButton(onClick = onRefresh) {
                Text("Refresh ↻", color = AssetPurple)
            }
        }

        Spacer(Modifier.height(16.dp))

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
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AssetWhite,
                unfocusedTextColor = AssetWhite,
                focusedBorderColor = AssetPurple,
                unfocusedBorderColor = Color(0xFF343D62),
                cursorColor = AssetPurple,
                focusedContainerColor = AssetPanel,
                unfocusedContainerColor = AssetPanel
            )
        )

        Spacer(Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = AssetPanel)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = "CONDITION",
                        color = AssetMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(Modifier.height(7.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AssetConditions.forEach { condition ->
                            RegistryFilterChip(
                                label = if (condition == "All") "All"
                                else condition.replaceFirstChar { it.uppercase() },
                                selected = selectedCondition == condition,
                                onClick = { selectedCondition = condition }
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF303858))

                Column {
                    Text(
                        text = "STATUS",
                        color = AssetMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(Modifier.height(7.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AssetStatuses.forEach { status ->
                            val label = when (status) {
                                "All" -> "All"
                                "active" -> "Active"
                                "maintenance" -> "Maintenance"
                                "inactive" -> "Inactive"
                                else -> "Retired"
                            }

                            RegistryFilterChip(
                                label = label,
                                selected = selectedStatus == status,
                                onClick = { selectedStatus = status }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${filteredAssets.size} assets",
                    color = AssetWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Matching your filters",
                    color = AssetMuted,
                    fontSize = 11.sp
                )
            }

            ExposedDropdownMenuBox(
                expanded = sortMenuExpanded,
                onExpandedChange = { sortMenuExpanded = it }
            ) {
                TextButton(
                    onClick = { sortMenuExpanded = true },
                    modifier = Modifier.menuAnchor()
                ) {
                    Text(
                        text = "Sort",
                        color = AssetPurple,
                        fontSize = 12.sp
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = sortMenuExpanded
                    )
                }

                ExposedDropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    AssetSortOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedSort = option
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AssetPurple)
                }
            }

            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Unable to load assets",
                        color = Color(0xFFFF8295),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        error,
                        color = AssetMuted,
                        fontSize = 12.sp
                    )

                    TextButton(onClick = onRefresh) {
                        Text("Try again", color = AssetPurple)
                    }
                }
            }

            filteredAssets.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("▦", color = AssetPurple, fontSize = 42.sp)

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = if (assets.isEmpty()) {
                                "No assets registered yet"
                            } else {
                                "No matching assets found"
                            },
                            color = AssetWhite,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            "Try changing your search or filters.",
                            color = AssetMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    sortedAssets.forEach { asset ->
                        AssetCardItem(
                            asset = asset,
                            onClick = { onAssetSelected(asset) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegistryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                maxLines = 1
            )
        },
        shape = RoundedCornerShape(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AssetPurple,
            selectedLabelColor = Color.White,
            containerColor = AssetCard,
            labelColor = AssetMuted
        )
    )
}

@Composable
private fun AssetCardItem(
    asset: AssetData,
    onClick: () -> Unit
) {
    val statusColor = when (asset.status.lowercase()) {
        "active" -> Color(0xFF65D6B0)
        "maintenance" -> Color(0xFFFFC66D)
        "inactive" -> Color(0xFFA8B2D1)
        "retired" -> Color(0xFFFF8295)
        else -> AssetMuted
    }

    val conditionColor = when (asset.condition.lowercase()) {
        "new", "good" -> Color(0xFF65D6B0)
        "fair" -> Color(0xFFFFC66D)
        "poor", "damaged" -> Color(0xFFFF8295)
        else -> AssetMuted
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AssetPanel)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF29234F)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "▦",
                            color = AssetPurple,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = asset.name,
                        color = AssetWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = asset.assetCode,
                        color = AssetMuted,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = asset.status.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 6.dp
                        ),
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF303858))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "CATEGORY",
                        color = AssetMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        asset.category,
                        color = AssetWhite,
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "CONDITION",
                        color = AssetMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(Modifier.height(4.dp))

                    Surface(
                        color = conditionColor.copy(alpha = 0.13f),
                        shape = RoundedCornerShape(7.dp)
                    ) {
                        Text(
                            text = asset.condition.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(
                                horizontal = 9.dp,
                                vertical = 5.dp
                            ),
                            color = conditionColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    "View details  →",
                    color = AssetPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}