
package com.groundmap.android.ui.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groundmap.android.data.remote.AssetHistoryData
import org.json.JSONObject
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val HistoryBackground = Color(0xFF0B1120)
private val HistoryCard = Color(0xFF151F32)
private val HistoryWhite = Color(0xFFF8FAFC)
private val HistoryMuted = Color(0xFF94A3B8)
private val HistoryAccent = Color(0xFF3B82F6)
private val HistoryGreen = Color(0xFF22C55E)
private val HistoryOrange = Color(0xFFF59E0B)
private val HistoryRed = Color(0xFFEF4444)

@Composable
fun AssetHistoryScreen(
    assetName: String,
    assetCode: String,
    history: List<AssetHistoryData>,
    loading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HistoryBackground)
            .padding(20.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back to Asset", color = HistoryAccent)
        }

        Text(
            "ASSET HISTORY",
            color = HistoryAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            assetName,
            color = HistoryWhite,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            assetCode,
            color = HistoryMuted,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Activity Timeline",
                color = HistoryWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = onRefresh,
                enabled = !loading
            ) {
                Text("Refresh", color = HistoryAccent)
            }
        }

        Spacer(Modifier.height(10.dp))

        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = HistoryAccent)
                        Text("Loading asset history...", color = HistoryMuted)
                    }
                }
            }

            error != null -> {
                HistoryMessage(
                    title = "Unable to load history",
                    message = error,
                    action = "Try Again",
                    onAction = onRefresh
                )
            }

            history.isEmpty() -> {
                HistoryMessage(
                    title = "No history available",
                    message = "Changes to this asset will appear here.",
                    action = "Refresh",
                    onAction = onRefresh
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Text(
                            "${history.size} recorded event(s)",
                            color = HistoryMuted,
                            fontSize = 12.sp
                        )
                    }

                    items(
                        items = history,
                        key = { it.id }
                    ) { event ->
                        HistoryEventCard(event)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEventCard(event: AssetHistoryData) {
    val eventColor = when (event.eventType.lowercase()) {
        "registered" -> HistoryGreen
        "status_changed" -> HistoryOrange
        "condition_changed" -> HistoryRed
        "updated" -> HistoryAccent
        else -> HistoryMuted
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = HistoryCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .background(eventColor, CircleShape)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    eventTitle(event.eventType),
                    color = HistoryWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    event.description,
                    color = HistoryMuted,
                    fontSize = 12.sp
                )

                EventValueChanges(
                    oldValue = event.oldValue,
                    newValue = event.newValue
                )

                Text(
                    formatTimestamp(event.createdAt),
                    color = HistoryAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EventValueChanges(
    oldValue: JSONObject?,
    newValue: JSONObject?
) {
    if (oldValue == null && newValue == null) return

    val keys = linkedSetOf<String>().apply {
        oldValue?.keys()?.forEach { add(it) }
        newValue?.keys()?.forEach { add(it) }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        keys.forEach { key ->
            val oldText = displayValue(oldValue?.opt(key))
            val newText = displayValue(newValue?.opt(key))

            if (oldText != newText) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        key.replace("_", " ")
                            .replaceFirstChar { it.uppercase() },
                        color = HistoryMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (oldText != null) {
                            Text(
                                oldText,
                                color = HistoryRed,
                                fontSize = 12.sp
                            )
                        }

                        if (oldText != null && newText != null) {
                            Text("→", color = HistoryMuted, fontSize = 12.sp)
                        }

                        if (newText != null) {
                            Text(
                                newText,
                                color = HistoryGreen,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun displayValue(value: Any?): String? {
    return when (value) {
        null, JSONObject.NULL -> null
        is JSONObject -> value.toString()
        else -> value.toString()
            .takeIf { it.isNotBlank() }
    }
}

private fun eventTitle(eventType: String): String {
    return when (eventType.lowercase()) {
        "registered" -> "Asset Registered"
        "updated" -> "Asset Information Updated"
        "status_changed" -> "Status Changed"
        "condition_changed" -> "Condition Changed"
        "location_changed" -> "Location Changed"
        else -> eventType
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        val dateTime = OffsetDateTime.parse(timestamp)

        val date = dateTime.format(
            DateTimeFormatter.ofPattern(
                "dd MMM yyyy",
                Locale.ENGLISH
            )
        )

        val time = dateTime.format(
            DateTimeFormatter.ofPattern(
                "hh:mm a",
                Locale.ENGLISH
            )
        )

        "$date • $time"
    } catch (_: Exception) {
        timestamp
    }
}

@Composable
private fun HistoryMessage(
    title: String,
    message: String,
    action: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                title,
                color = HistoryWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                message,
                color = HistoryMuted,
                fontSize = 13.sp
            )

            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HistoryAccent
                )
            ) {
                Text(action)
            }
        }
    }
}
