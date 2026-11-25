
package com.groundmap.android.ui.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groundmap.android.data.remote.WorkspaceData

private val WorkspaceBackground = Color(0xFF0B1024)
private val WorkspaceCard = Color(0xFF171D38)
private val WorkspaceAccent = Color(0xFF8B5CF6)
private val WorkspaceMuted = Color(0xFFA8B2D1)
private val WorkspaceWhite = Color(0xFFF8FAFC)

@Composable
fun WorkspaceSelectorScreen(
    workspaces: List<WorkspaceData>,
    selectedWorkspaceId: String?,
    loading: Boolean,
    error: String?,
    onWorkspaceSelected: (WorkspaceData) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WorkspaceBackground)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "WORKSPACES",
                    color = WorkspaceAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Select workspace",
                    color = WorkspaceWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(onClick = onRefresh) {
                Text("Refresh", color = WorkspaceAccent)
            }
        }

        Spacer(Modifier.height(20.dp))

        when {
            loading -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = WorkspaceAccent)
                }
            }

            error != null -> {
                ErrorMessage(
                    message = error,
                    onRetry = onRefresh
                )
            }

            workspaces.isEmpty() -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No workspaces found.",
                        color = WorkspaceMuted
                    )
                }
            }

            else -> {
                Text(
                    "${workspaces.size} workspace(s) available",
                    color = WorkspaceMuted,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(14.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = workspaces,
                        key = { it.id }
                    ) { workspace ->

                        val selected =
                            workspace.id == selectedWorkspaceId

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onWorkspaceSelected(workspace)
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = WorkspaceCard
                            ),
                            border = if (selected) {
                                BorderStroke(1.dp, WorkspaceAccent)
                            } else null
                        ) {
                            Row(
                                modifier = Modifier.padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(
                                            Color(0xFF29234F),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "W",
                                        color = WorkspaceAccent,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(Modifier.width(14.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        workspace.name,
                                        color = WorkspaceWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )

                                    Spacer(Modifier.height(5.dp))

                                    Text(
                                        workspace.description
                                            ?: "No description available",
                                        color = WorkspaceMuted,
                                        fontSize = 12.sp
                                    )
                                }

                                if (selected) {
                                    Text(
                                        "✓",
                                        color = Color(0xFF22C55E),
                                        fontSize = 20.sp
                                    )
                                } else {
                                    Text(
                                        "›",
                                        color = WorkspaceMuted,
                                        fontSize = 24.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Unable to load workspaces",
            color = Color(0xFFEF4444),
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            message,
            color = WorkspaceMuted,
            fontSize = 12.sp
        )

        TextButton(onClick = onRetry) {
            Text("Try again")
        }
    }
}
