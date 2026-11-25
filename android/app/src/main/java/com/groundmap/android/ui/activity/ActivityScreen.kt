package com.groundmap.android.ui.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groundmap.android.data.remote.AssetHistoryData

data class ActivityFeedItem(
    val assetName: String,
    val assetCode: String,
    val event: AssetHistoryData
)

private val ActivityBackground = Color(0xFF0B1024)
private val ActivityCard = Color(0xFF171D38)
private val ActivityAccent = Color(0xFF8B5CF6)
private val ActivityMuted = Color(0xFFA8B2D1)
private val ActivityWhite = Color(0xFFF8FAFC)

@Composable
fun ActivityScreen(
    items: List<ActivityFeedItem>,
    loading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Activity",
                    color = ActivityWhite,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Recent events across workspace assets",
                    color = ActivityMuted,
                    fontSize = 13.sp
                )
            }

            Button(onClick = onRefresh) {
                Text(
                    text = "Refresh",
                    fontSize = 12.sp
                )
            }
        }

        when {
            loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = ActivityAccent)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Loading activity…",
                        color = ActivityMuted,
                        fontSize = 12.sp
                    )
                }
            }

            error != null -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = ActivityCard
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Couldn’t load activity",
                            color = ActivityWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = error,
                            color = ActivityMuted,
                            fontSize = 12.sp
                        )

                        Button(onClick = onRefresh) {
                            Text(
                                text = "Try again",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            items.isEmpty() -> {
                Text(
                    text = "No asset activity found yet.",
                    color = ActivityMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = items,
                        key = { "${it.event.assetId}_${it.event.id}" }
                    ) { item ->
                        ActivityEventCard(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityEventCard(item: ActivityFeedItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ActivityCard
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = item.event.eventType
                    .replace('_', ' ')
                    .replaceFirstChar { it.uppercase() },
                color = ActivityAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = item.event.description,
                color = ActivityWhite,
                fontSize = 13.sp
            )

            Text(
                text = listOf(item.assetName, item.assetCode)
                    .filter { it.isNotBlank() }
                    .joinToString(" • "),
                color = ActivityMuted,
                fontSize = 11.sp
            )

            Text(
                text = item.event.createdAt,
                color = ActivityMuted,
                fontSize = 10.sp
            )
        }
    }
}