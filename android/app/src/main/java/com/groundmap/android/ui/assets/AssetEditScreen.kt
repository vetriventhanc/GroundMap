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

data class AssetEditFormData(
    val name: String,
    val category: String,
    val manufacturer: String?,
    val modelNumber: String?,
    val serialNumber: String?,
    val purchaseDate: String?,
    val notes: String?
)

private val EditBackground = Color(0xFF0B1024)
private val EditCard = Color(0xFF171D38)
private val EditField = Color(0xFF202747)
private val EditWhite = Color(0xFFF8FAFC)
private val EditMuted = Color(0xFFA8B2D1)
private val EditAccent = Color(0xFF8B5CF6)
private val EditError = Color(0xFFFF8295)

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

    var validationError by remember(asset.id) { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EditBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(
            onClick = onBack,
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
        ) {
            Text("← Back to Details", color = EditAccent, fontSize = 13.sp)
        }

        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(
                "ASSET MANAGEMENT",
                color = EditAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.8.sp
            )

            Text(
                "Edit Asset",
                color = EditWhite,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Asset code: ${asset.assetCode}",
                color = EditMuted,
                fontSize = 12.sp
            )
        }

        EditSection(title = "Asset Information") {
            EditInput("Asset Name *", name) {
                name = it
                validationError = null
            }

            EditInput("Category *", category) {
                category = it
                validationError = null
            }
        }

        EditSection(title = "Equipment Information") {
            EditInput("Manufacturer", manufacturer) {
                manufacturer = it
            }

            EditInput("Model Number", modelNumber) {
                modelNumber = it
            }

            EditInput("Serial Number", serialNumber) {
                serialNumber = it
            }
        }

        EditSection(title = "Purchase Information") {
            EditInput("Purchase Date (YYYY-MM-DD)", purchaseDate) {
                purchaseDate = it
                validationError = null
            }
        }

        EditSection(title = "Additional Information") {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                colors = editFieldColors(),
                shape = RoundedCornerShape(14.dp)
            )
        }

        if (validationError != null) {
            EditNotice(
                message = validationError!!,
                isError = true
            )
        }

        if (error != null) {
            EditNotice(
                message = error,
                isError = true
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
                            !purchaseDate.matches(Regex("""\d{4}-\d{2}-\d{2}""")) ->
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
                .height(52.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EditAccent,
                disabledContainerColor = Color(0xFF2B334D)
            )
        ) {
            if (saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = EditWhite,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Save Changes",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun EditSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = EditCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Text(
                title,
                color = EditAccent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            content()
        }
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
        shape = RoundedCornerShape(14.dp),
        colors = editFieldColors()
    )
}

@Composable
private fun EditNotice(
    message: String,
    isError: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isError) EditError.copy(alpha = 0.12f) else EditCard,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            message,
            modifier = Modifier.padding(12.dp),
            color = if (isError) EditError else EditMuted,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun editFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = EditWhite,
        unfocusedTextColor = EditWhite,
        focusedBorderColor = EditAccent,
        unfocusedBorderColor = Color(0xFF3A456B),
        focusedLabelColor = EditAccent,
        unfocusedLabelColor = EditMuted,
        cursorColor = EditAccent,
        focusedContainerColor = EditField,
        unfocusedContainerColor = EditField
    )