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

private val FormBackground = Color(0xFF0B1020)
private val FormCard = Color(0xFF171D3A)
private val FormWhite = Color(0xFFF8FAFC)
private val FormMuted = Color(0xFFA5AEC8)
private val FormAccent = Color(0xFF8B5CF6)
private val FormBorder = Color(0xFF303858)
private val FormError = Color(0xFFEF4444)
private val FormSuccess = Color(0xFF22C55E)

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
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(
            onClick = onBack,
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
        ) {
            Text(
                text = "← Back to Assets",
                color = FormAccent,
                fontSize = 14.sp
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Register Asset",
                color = FormWhite,
                fontSize = 27.sp
            )

            Text(
                text = "Add equipment to your workspace inventory.",
                color = FormMuted,
                fontSize = 13.sp
            )
        }

        FormCardSection(
            title = "Workspace",
            subtitle = "Choose where this asset belongs."
        ) {
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
        }

        FormCardSection(
            title = "Basic Information",
            subtitle = "Enter the asset’s identifying details."
        ) {
            FormInput("Asset Code *", assetCode) {
                assetCode = it
            }

            FormInput("Asset Name *", name) {
                name = it
            }

            FormInput("Category *", category) {
                category = it
            }
        }

        FormCardSection(
            title = "Equipment Information",
            subtitle = "Optional manufacturer and equipment details."
        ) {
            FormInput("Manufacturer", manufacturer) {
                manufacturer = it
            }

            FormInput("Model Number", modelNumber) {
                modelNumber = it
            }

            FormInput("Serial Number", serialNumber) {
                serialNumber = it
            }
        }

        FormCardSection(
            title = "Location & Purchase",
            subtitle = "Add the room and purchase information, if available."
        ) {
            FormInput("Room ID (optional UUID)", roomId) {
                roomId = it
            }

            FormInput("Purchase Date (YYYY-MM-DD)", purchaseDate) {
                purchaseDate = it
            }
        }

        FormCardSection(
            title = "Asset Status",
            subtitle = "Set the current status and physical condition."
        ) {
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
                    ).forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                status = option
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
                    ).forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                condition = option
                                conditionMenu = false
                            }
                        )
                    }
                }
            }
        }

        FormCardSection(
            title = "Additional Notes",
            subtitle = "Include any useful details about this asset."
        ) {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                colors = formFieldColors()
            )
        }

        if (validationError != null) {
            FormMessage(
                message = validationError.orEmpty(),
                color = FormError
            )
        }

        if (error != null) {
            FormMessage(
                message = error,
                color = FormError
            )
        }

        if (saveMessage != null) {
            FormMessage(
                message = saveMessage,
                color = FormSuccess
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
                            !purchaseDate.matches(Regex("""\d{4}-\d{2}-\d{2}""")) ->
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
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FormAccent,
                disabledContainerColor = FormAccent.copy(alpha = 0.4f)
            )
        ) {
            if (saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(21.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Register Asset",
                    fontSize = 15.sp
                )
            }
        }

        if (loading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = FormAccent,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        if (!loading && workspaces.isEmpty()) {
            FormMessage(
                message = "No workspaces available. Create a workspace first.",
                color = FormMuted
            )
        }
    }
}

@Composable
private fun FormCardSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = FormCard
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = FormBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        color = FormAccent,
                        fontSize = 15.sp
                    )

                    Text(
                        text = subtitle,
                        color = FormMuted,
                        fontSize = 12.sp
                    )
                }

                content()
            }
        )
    }
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
private fun FormMessage(
    message: String,
    color: Color
) {
    Text(
        text = message,
        color = color,
        fontSize = 12.sp
    )
}

@Composable
private fun formFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = FormWhite,
        unfocusedTextColor = FormWhite,
        focusedBorderColor = FormAccent,
        unfocusedBorderColor = FormBorder,
        focusedLabelColor = FormAccent,
        unfocusedLabelColor = FormMuted,
        cursorColor = FormAccent,
        focusedContainerColor = FormBackground,
        unfocusedContainerColor = FormBackground
    )