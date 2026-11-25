package com.groundmap.android.ui.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

private val HistoryBackground = Color(0xFF0B1024)
private val HistoryCard = Color(0xFF171D38)
private val HistoryWhite = Color(0xFFF8FAFC)
private val HistoryMuted = Color(0xFFA8B2D1)
private val HistoryAccent = Color(0xFF8B5CF6)
private val HistoryGreen = Color(0xFF65D6B0)
private val HistoryOrange = Color(0xFFFFC66D)
private val HistoryRed = Color(0xFFFF8295)

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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(
            onClick = onBack,
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
        ) {
            Text("← Back to Asset", color = HistoryAccent, fontSize = 13.sp)
        }

        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(
                "ASSET HISTORY",
                color = HistoryAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.8.sp
            )

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
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = HistoryCard)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Activity Timeline",
                        color = HistoryWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${history.size} recorded event(s)",
                        color = HistoryMuted,
                        fontSize = 11.sp
                    )
                }

                TextButton(
                    onClick = onRefresh,
                    enabled = !loading
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = HistoryAccent,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Refresh ↻", color = HistoryAccent)
                    }
                }
            }
        }

        when {
            loading -> {
                HistoryMessage(
                    title = "Loading history",
                    message = "Fetching the latest asset activity.",
                    action = null,
                    onAction = onRefresh
                )
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    history.forEach { event ->
                        HistoryEventCard(event)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = HistoryCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(eventColor, CircleShape)
                )

                Text(
                    eventTitle(event.eventType),
                    color = HistoryWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                event.description,
                color = HistoryMuted,
                fontSize = 12.sp
            )

            EventValueChanges(
                oldValue = event.oldValue,
                newValue = event.newValue
            )

            Surface(
                color = Color(0xFF222A4A),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    formatTimestamp(event.createdAt),
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 7.dp
                    ),
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

    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        keys.forEach { key ->
            val oldText = displayValue(oldValue?.opt(key))
            val newText = displayValue(newValue?.opt(key))

            if (oldText != newText) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        key.replace("_", " ")
                            .replaceFirstChar { it.uppercase() },
                        color = HistoryMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
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
        else -> value.toString().takeIf { it.isNotBlank() }
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
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
        )

        val time = dateTime.format(
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
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
    action: String?,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = HistoryCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                title,
                color = HistoryWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                message,
                color = HistoryMuted,
                fontSize = 12.sp
            )

            if (action != null) {
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
}