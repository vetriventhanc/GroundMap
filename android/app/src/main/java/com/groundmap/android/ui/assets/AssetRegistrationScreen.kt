
package com.groundmap.android.ui.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groundmap.android.data.remote.WorkspaceData

data class AssetFormData(
    val workspaceId: String,
    val roomId: String?,
    val assetCode: String,
    val name: String,
    val category: String,
    val manufacturer: String?,
    val modelNumber: String?,
    val serialNumber: String?,
    val purchaseDate: String?,
    val status: String,
    val condition: String,
    val notes: String?
)

private val FormBackground = Color(0xFF0B1120)
private val FormCard = Color(0xFF151F32)
private val FormWhite = Color(0xFFF8FAFC)
private val FormMuted = Color(0xFF94A3B8)
private val FormAccent = Color(0xFF3B82F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetRegistrationScreen(
    workspaces: List<WorkspaceData>,
    loading: Boolean,
    error: String?,
    saving: Boolean,
    saveMessage: String?,
    onRegister: (AssetFormData) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedWorkspace by remember {
        mutableStateOf<WorkspaceData?>(null)
    }

    var workspaceMenu by remember { mutableStateOf(false) }

    var roomId by remember { mutableStateOf("") }
    var assetCode by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var modelNumber by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var status by remember { mutableStateOf("active") }
    var condition by remember { mutableStateOf("good") }

    var statusMenu by remember { mutableStateOf(false) }
    var conditionMenu by remember { mutableStateOf(false) }

    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(workspaces) {
        if (selectedWorkspace == null) {
            selectedWorkspace = workspaces.firstOrNull()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FormBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        TextButton(onClick = onBack) {
            Text("← Back to Assets", color = FormAccent)
        }

        Text(
            "Register Asset",
            color = FormWhite,
            fontSize = 26.sp
        )

        Text(
            "Add equipment to your workspace inventory.",
            color = FormMuted,
            fontSize = 13.sp
        )

        FormSection("Workspace")

        ExposedDropdownMenuBox(
            expanded = workspaceMenu,
            onExpandedChange = { workspaceMenu = it }
        ) {
            OutlinedTextField(
                value = selectedWorkspace?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Select workspace") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = workspaceMenu
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = formFieldColors()
            )

            ExposedDropdownMenu(
                expanded = workspaceMenu,
                onDismissRequest = { workspaceMenu = false }
            ) {
                workspaces.forEach { workspace ->
                    DropdownMenuItem(
                        text = { Text(workspace.name) },
                        onClick = {
                            selectedWorkspace = workspace
                            workspaceMenu = false
                        }
                    )
                }
            }
        }

        FormSection("Basic Information")

        FormInput(
            "Asset Code *",
            assetCode
        ) { assetCode = it }

        FormInput(
            "Asset Name *",
            name
        ) { name = it }

        FormInput(
            "Category *",
            category
        ) { category = it }

        FormSection("Equipment Information")

        FormInput(
            "Manufacturer",
            manufacturer
        ) { manufacturer = it }

        FormInput(
            "Model Number",
            modelNumber
        ) { modelNumber = it }

        FormInput(
            "Serial Number",
            serialNumber
        ) { serialNumber = it }

        FormSection("Location and Purchase")

        FormInput(
            "Room ID (optional UUID)",
            roomId
        ) { roomId = it }

        FormInput(
            "Purchase Date (YYYY-MM-DD)",
            purchaseDate
        ) { purchaseDate = it }

        FormSection("Asset Status")

        ExposedDropdownMenuBox(
            expanded = statusMenu,
            onExpandedChange = { statusMenu = it }
        ) {
            OutlinedTextField(
                value = status,
                onValueChange = {},
                readOnly = true,
                label = { Text("Status") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = statusMenu
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = formFieldColors()
            )

            ExposedDropdownMenu(
                expanded = statusMenu,
                onDismissRequest = { statusMenu = false }
            ) {
                listOf(
                    "active",
                    "maintenance",
                    "inactive",
                    "retired"
                ).forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            status = it
                            statusMenu = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = conditionMenu,
            onExpandedChange = { conditionMenu = it }
        ) {
            OutlinedTextField(
                value = condition,
                onValueChange = {},
                readOnly = true,
                label = { Text("Condition") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = conditionMenu
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = formFieldColors()
            )

            ExposedDropdownMenu(
                expanded = conditionMenu,
                onDismissRequest = { conditionMenu = false }
            ) {
                listOf(
                    "new",
                    "good",
                    "fair",
                    "poor",
                    "damaged"
                ).forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            condition = it
                            conditionMenu = false
                        }
                    )
                }
            }
        }

        FormSection("Additional Notes")

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = formFieldColors()
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

        if (saveMessage != null) {
            Text(
                saveMessage,
                color = Color(0xFF22C55E),
                fontSize = 13.sp
            )
        }

        Button(
            onClick = {
                validationError = when {
                    selectedWorkspace == null ->
                        "Please select a workspace."

                    assetCode.trim().length !in 2..80 ->
                        "Asset code must contain 2–80 characters."

                    name.trim().length !in 2..150 ->
                        "Asset name must contain 2–150 characters."

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
                    onRegister(
                        AssetFormData(
                            workspaceId = selectedWorkspace!!.id,
                            roomId = roomId.trim().ifBlank { null },
                            assetCode = assetCode.trim(),
                            name = name.trim(),
                            category = category.trim(),
                            manufacturer = manufacturer.trim().ifBlank { null },
                            modelNumber = modelNumber.trim().ifBlank { null },
                            serialNumber = serialNumber.trim().ifBlank { null },
                            purchaseDate = purchaseDate.trim().ifBlank { null },
                            status = status,
                            condition = condition,
                            notes = notes.trim().ifBlank { null }
                        )
                    )
                }
            },
            enabled = !saving && !loading && workspaces.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FormAccent
            )
        ) {
            if (saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text("Register Asset")
            }
        }

        if (loading) {
            CircularProgressIndicator(color = FormAccent)
        }

        if (!loading && workspaces.isEmpty()) {
            Text(
                "No workspaces available. Create a workspace first.",
                color = FormMuted
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FormSection(title: String) {
    Text(
        title,
        color = FormAccent,
        fontSize = 15.sp
    )
}

@Composable
private fun FormInput(
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
        colors = formFieldColors()
    )
}

@Composable
private fun formFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = FormWhite,
        unfocusedTextColor = FormWhite,
        focusedBorderColor = FormAccent,
        unfocusedBorderColor = Color(0xFF334155),
        focusedLabelColor = FormAccent,
        unfocusedLabelColor = FormMuted,
        cursorColor = FormAccent,
        focusedContainerColor = FormCard,
        unfocusedContainerColor = FormCard
    )
