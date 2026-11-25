package com.groundmap.android.ui.assets

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FaultBackground = Color(0xFF0B1023)
private val FaultCard = Color(0xFF171F3B)
private val FaultAccent = Color(0xFF9B5CFF)
private val FaultWhite = Color(0xFFF8FAFC)
private val FaultMuted = Color(0xFF94A3B8)
private val FaultRed = Color(0xFFEF4444)
private val FaultGreen = Color(0xFF22C55E)
private val FaultOrange = Color(0xFFF59E0B)

data class FaultReportUiData(
    val id: String,
    val assetId: String,
    val title: String,
    val description: String,
    val severity: String,
    val status: String,
    val createdAt: String,
    val resolutionNotes: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaultReportsScreen(
    assetName: String,
    assetCode: String,
    reports: List<FaultReportUiData>,
    loading: Boolean,
    saving: Boolean,
    error: String?,
    successMessage: String?,
    onSubmit: (String, String, String) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onUpdateReport: (String, String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("medium") }
    var severityMenu by remember { mutableStateOf(false) }

    val severities = listOf("low", "medium", "high", "critical")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FaultBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = FaultAccent
            )
        ) {
            Text(
                "← Back to Asset Operations",
                fontSize = 12.sp
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                "FAULT REPORTING",
                color = FaultAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                assetName,
                color = FaultWhite,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Asset code: $assetCode",
                color = FaultMuted,
                fontSize = 13.sp
            )
        }

        FaultSection(title = "Submit a Fault Report") {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Fault title", fontSize = 12.sp) },
                placeholder = {
                    Text(
                        "e.g. Cooling system malfunction",
                        fontSize = 12.sp
                    )
                },
                textStyle = TextStyle(fontSize = 12.sp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = faultFieldColors()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description", fontSize = 12.sp) },
                placeholder = {
                    Text(
                        "Describe the problem and what happened...",
                        fontSize = 12.sp
                    )
                },
                textStyle = TextStyle(fontSize = 12.sp),
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
                colors = faultFieldColors()
            )

            ExposedDropdownMenuBox(
                expanded = severityMenu,
                onExpandedChange = { severityMenu = it }
            ) {
                OutlinedTextField(
                    value = severity.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Severity", fontSize = 12.sp) },
                    textStyle = TextStyle(fontSize = 12.sp),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = severityMenu
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = faultFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = severityMenu,
                    onDismissRequest = { severityMenu = false }
                ) {
                    severities.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    option.replaceFirstChar { it.uppercase() },
                                    fontSize = 12.sp
                                )
                            },
                            onClick = {
                                severity = option
                                severityMenu = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    onSubmit(
                        title.trim(),
                        description.trim(),
                        severity
                    )
                },
                enabled = !saving &&
                        title.trim().length >= 3 &&
                        description.trim().length >= 5,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FaultAccent
                )
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = FaultWhite,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Submitting...", fontSize = 12.sp)
                } else {
                    Text("Submit Fault Report", fontSize = 12.sp)
                }
            }

            error?.let {
                Text(
                    it,
                    color = FaultRed,
                    fontSize = 12.sp
                )
            }

            successMessage?.let {
                Text(
                    it,
                    color = FaultGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Previous Fault Reports",
                color = FaultWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = onRefresh,
                enabled = !loading
            ) {
                Text(
                    "Refresh",
                    color = FaultAccent,
                    fontSize = 12.sp
                )
            }
        }

        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FaultAccent)
                }
            }

            reports.isEmpty() -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = FaultCard
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "No fault reports recorded for this asset.",
                        color = FaultMuted,
                        modifier = Modifier.padding(18.dp),
                        fontSize = 12.sp
                    )
                }
            }

            else -> {
                reports.forEach { report ->
                    FaultReportCard(
                        report = report,
                        saving = saving,
                        onUpdateReport = onUpdateReport
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FaultSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FaultCard
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Text(
                title,
                color = FaultWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FaultReportCard(
    report: FaultReportUiData,
    saving: Boolean,
    onUpdateReport: (String, String, String?) -> Unit
) {
    val severityColor = when (report.severity.lowercase()) {
        "critical" -> FaultRed
        "high" -> FaultOrange
        "medium" -> FaultAccent
        else -> FaultGreen
    }

    var selectedStatus by remember(report.id, report.status) {
        mutableStateOf(report.status.lowercase())
    }
    var resolutionNotes by remember(report.id, report.resolutionNotes) {
        mutableStateOf(report.resolutionNotes.orEmpty())
    }
    var statusMenuExpanded by remember { mutableStateOf(false) }

    val statuses = listOf("open", "in_progress", "resolved")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = FaultCard
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(
                report.title,
                color = FaultWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                report.description,
                color = FaultMuted,
                fontSize = 12.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FaultTag(
                    report.severity.uppercase(),
                    severityColor
                )

                FaultTag(
                    report.status.replace("_", " ").uppercase(),
                    FaultAccent
                )
            }

            Text(
                "Reported: ${report.createdAt}",
                color = FaultMuted,
                fontSize = 10.sp
            )

            Divider(color = Color(0xFF334155))

            Text(
                "Manage Report",
                color = FaultWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            ExposedDropdownMenuBox(
                expanded = statusMenuExpanded,
                onExpandedChange = { statusMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedStatus
                        .replace("_", " ")
                        .replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status", fontSize = 12.sp) },
                    textStyle = TextStyle(fontSize = 12.sp),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = statusMenuExpanded
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = faultFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = statusMenuExpanded,
                    onDismissRequest = { statusMenuExpanded = false }
                ) {
                    statuses.forEach { status ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    status.replace("_", " ")
                                        .replaceFirstChar { it.uppercase() },
                                    fontSize = 12.sp
                                )
                            },
                            onClick = {
                                selectedStatus = status
                                statusMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = resolutionNotes,
                onValueChange = { resolutionNotes = it },
                label = { Text("Resolution notes", fontSize = 12.sp) },
                placeholder = {
                    Text(
                        "Describe the work completed or next steps...",
                        fontSize = 12.sp
                    )
                },
                textStyle = TextStyle(fontSize = 12.sp),
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = faultFieldColors()
            )

            Button(
                onClick = {
                    onUpdateReport(
                        report.id,
                        selectedStatus,
                        resolutionNotes.trim().ifBlank { null }
                    )
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FaultAccent
                )
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = FaultWhite,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Saving...", fontSize = 12.sp)
                } else {
                    Text("Save Update", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun FaultTag(
    text: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(7.dp)
    ) {
        Text(
            text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = 9.dp,
                vertical = 5.dp
            )
        )
    }
}

@Composable
private fun faultFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = FaultWhite,
        unfocusedTextColor = FaultWhite,
        focusedBorderColor = FaultAccent,
        unfocusedBorderColor = Color(0xFF334155),
        focusedLabelColor = FaultAccent,
        unfocusedLabelColor = FaultMuted,
        cursorColor = FaultAccent,
        focusedContainerColor = FaultCard,
        unfocusedContainerColor = FaultCard
    )