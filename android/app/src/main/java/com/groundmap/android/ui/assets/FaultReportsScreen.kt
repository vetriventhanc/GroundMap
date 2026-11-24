
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

private val FaultBackground = Color(0xFF0B1120)
private val FaultCard = Color(0xFF151F32)
private val FaultAccent = Color(0xFF3B82F6)
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
    val createdAt: String
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
            Text("← Back to Asset Operations")
        }

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                "FAULT REPORTING",
                color = FaultRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                assetName,
                color = FaultWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Asset code: $assetCode",
                color = FaultMuted,
                fontSize = 13.sp
            )
        }

        // NEW FAULT REPORT FORM

        FaultSection(title = "Submit a Fault Report") {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Fault title") },
                placeholder = { Text("e.g. Cooling system malfunction") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = faultFieldColors()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = {
                    Text("Describe the problem and what happened...")
                },
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
                    label = { Text("Severity") },
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
                                Text(option.replaceFirstChar { it.uppercase() })
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
                    containerColor = FaultRed
                )
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = FaultWhite,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Submitting...")
                } else {
                    Text("Submit Fault Report")
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

        // EXISTING FAULT REPORTS

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
                Text("Refresh", color = FaultAccent)
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
                        fontSize = 13.sp
                    )
                }
            }

            else -> {
                reports.forEach { report ->
                    FaultReportCard(report)
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

@Composable
private fun FaultReportCard(report: FaultReportUiData) {
    val severityColor = when (report.severity.lowercase()) {
        "critical" -> FaultRed
        "high" -> FaultOrange
        "medium" -> FaultAccent
        else -> FaultGreen
    }

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
                fontSize = 13.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FaultTag(
                    report.severity.uppercase(),
                    severityColor
                )

                FaultTag(
                    report.status.uppercase(),
                    FaultAccent
                )
            }

            Text(
                "Reported: ${report.createdAt}",
                color = FaultMuted,
                fontSize = 10.sp
            )
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
