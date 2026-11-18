
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groundmap.android.data.remote.AssetData

data class AssetEditFormData(
    val name: String,
    val category: String,
    val manufacturer: String?,
    val modelNumber: String?,
    val serialNumber: String?,
    val purchaseDate: String?,
    val notes: String?
)

private val EditBackground = Color(0xFF0B1120)
private val EditCard = Color(0xFF151F32)
private val EditWhite = Color(0xFFF8FAFC)
private val EditMuted = Color(0xFF94A3B8)
private val EditAccent = Color(0xFF3B82F6)

@Composable
fun AssetEditScreen(
    asset: AssetData,
    saving: Boolean,
    error: String?,
    onSave: (AssetEditFormData) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember(asset.id) { mutableStateOf(asset.name) }
    var category by remember(asset.id) { mutableStateOf(asset.category) }
    var manufacturer by remember(asset.id) {
        mutableStateOf(asset.manufacturer ?: "")
    }
    var modelNumber by remember(asset.id) {
        mutableStateOf(asset.modelNumber ?: "")
    }
    var serialNumber by remember(asset.id) {
        mutableStateOf(asset.serialNumber ?: "")
    }
    var purchaseDate by remember(asset.id) {
        mutableStateOf(asset.purchaseDate ?: "")
    }
    var notes by remember(asset.id) {
        mutableStateOf(asset.notes ?: "")
    }

    var validationError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EditBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back to Details", color = EditAccent)
        }

        Text(
            "Edit Asset",
            color = EditWhite,
            fontSize = 26.sp
        )

        Text(
            "Asset code: ${asset.assetCode}",
            color = EditMuted,
            fontSize = 13.sp
        )

        Text(
            "Asset Information",
            color = EditAccent,
            fontSize = 16.sp
        )

        EditInput("Asset Name *", name) { name = it }

        EditInput("Category *", category) { category = it }

        Text(
            "Equipment Information",
            color = EditAccent,
            fontSize = 16.sp
        )

        EditInput("Manufacturer", manufacturer) {
            manufacturer = it
        }

        EditInput("Model Number", modelNumber) {
            modelNumber = it
        }

        EditInput("Serial Number", serialNumber) {
            serialNumber = it
        }

        Text(
            "Purchase Information",
            color = EditAccent,
            fontSize = 16.sp
        )

        EditInput("Purchase Date (YYYY-MM-DD)", purchaseDate) {
            purchaseDate = it
        }

        Text(
            "Additional Information",
            color = EditAccent,
            fontSize = 16.sp
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = editFieldColors()
        )

        if (validationError != null) {
            Text(
                validationError!!,
                color = Color(0xFFEF4444),
                fontSize = 12.sp
            )
        }

        if (error != null) {
            Text(
                error,
                color = Color(0xFFEF4444),
                fontSize = 12.sp
            )
        }

        Button(
            onClick = {
                validationError = when {
                    name.trim().length !in 2..150 ->
                        "Name must contain 2–150 characters."

                    category.trim().length !in 2..100 ->
                        "Category must contain 2–100 characters."

                    purchaseDate.isNotBlank() &&
                            !purchaseDate.matches(
                                Regex("""\d{4}-\d{2}-\d{2}""")
                            ) ->
                        "Purchase date must use YYYY-MM-DD."

                    else -> null
                }

                if (validationError == null) {
                    onSave(
                        AssetEditFormData(
                            name = name.trim(),
                            category = category.trim(),
                            manufacturer = manufacturer.trim().ifBlank { null },
                            modelNumber = modelNumber.trim().ifBlank { null },
                            serialNumber = serialNumber.trim().ifBlank { null },
                            purchaseDate = purchaseDate.trim().ifBlank { null },
                            notes = notes.trim().ifBlank { null }
                        )
                    )
                }
            },
            enabled = !saving,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EditAccent
            )
        ) {
            if (saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text("Save Changes")
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun EditInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = editFieldColors()
    )
}

@Composable
private fun editFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = EditWhite,
        unfocusedTextColor = EditWhite,
        focusedBorderColor = EditAccent,
        unfocusedBorderColor = Color(0xFF334155),
        focusedLabelColor = EditAccent,
        unfocusedLabelColor = EditMuted,
        cursorColor = EditAccent,
        focusedContainerColor = EditCard,
        unfocusedContainerColor = EditCard
    )
