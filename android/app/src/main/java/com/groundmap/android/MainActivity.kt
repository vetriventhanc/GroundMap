
package com.groundmap.android

import com.groundmap.android.ui.assets.AssetDetailsScreen
import com.groundmap.android.ui.assets.AssetEditScreen
import com.groundmap.android.ui.assets.AssetEditFormData
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groundmap.android.data.remote.*
import com.groundmap.android.ui.assets.AssetRegistrationScreen
import com.groundmap.android.ui.assets.AssetRegistryScreen
import com.groundmap.android.ui.assets.AssetFormData
import com.groundmap.android.ui.workspace.WorkspaceSelectorScreen
import java.util.concurrent.atomic.AtomicBoolean

private val Background = Color(0xFF0B1120)
private val CardColor = Color(0xFF151F32)
private val Accent = Color(0xFF3B82F6)
private val Muted = Color(0xFF94A3B8)
private val White = Color(0xFFF8FAFC)
private val Green = Color(0xFF22C55E)
private val Orange = Color(0xFFF59E0B)
private val Red = Color(0xFFEF4444)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GroundMapApp()
        }
    }
}

@Composable
fun GroundMapApp() {

    var selectedTab by remember { mutableIntStateOf(0) }

    var workspaces by remember {
        mutableStateOf<List<WorkspaceData>>(emptyList())
    }

    var selectedWorkspace by remember {
        mutableStateOf<WorkspaceData?>(null)
    }

    var assets by remember {
        mutableStateOf<List<AssetData>>(emptyList())
    }

    var workspaceLoading by remember { mutableStateOf(false) }
    var assetLoading by remember { mutableStateOf(false) }
    var registrationSaving by remember { mutableStateOf(false) }
    var editSaving by remember { mutableStateOf(false) }
    var editError by remember { mutableStateOf<String?>(null) }

    var workspaceError by remember { mutableStateOf<String?>(null) }
    var assetError by remember { mutableStateOf<String?>(null) }
    var registrationError by remember { mutableStateOf<String?>(null) }

    var workspaceRefresh by remember { mutableIntStateOf(0) }
    var assetRefresh by remember { mutableIntStateOf(0) }

    var selectedAsset by remember {
        mutableStateOf<AssetData?>(null)
    }

    // Load workspaces
    DisposableEffect(workspaceRefresh) {
        val cancelled = AtomicBoolean(false)
        workspaceLoading = true
        workspaceError = null

        Thread {
            try {
                val result = GroundMapApi.loadWorkspaces()

                Handler(Looper.getMainLooper()).post {
                    if (!cancelled.get()) {
                        workspaces = result

                        val current = selectedWorkspace
                        selectedWorkspace = result.firstOrNull {
                            it.id == current?.id
                        } ?: result.firstOrNull()

                        workspaceLoading = false
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    if (!cancelled.get()) {
                        workspaceError = e.message
                        workspaceLoading = false
                    }
                }
            }
        }.start()

        onDispose {
            cancelled.set(true)
        }
    }

    // Load assets for selected workspace
    DisposableEffect(selectedWorkspace?.id, assetRefresh) {
        val workspaceId = selectedWorkspace?.id
        val cancelled = AtomicBoolean(false)

        if (workspaceId == null) {
            assets = emptyList()
            assetLoading = false
            assetError = null
        } else {
            assetLoading = true
            assetError = null

            Thread {
                try {
                    val result = GroundMapApi.loadAssets(workspaceId)

                    Handler(Looper.getMainLooper()).post {
                        if (!cancelled.get()) {
                            assets = result
                            assetLoading = false
                        }
                    }
                } catch (e: Exception) {
                    Handler(Looper.getMainLooper()).post {
                        if (!cancelled.get()) {
                            assetError = e.message
                            assetLoading = false
                        }
                    }
                }
            }.start()
        }

        onDispose {
            cancelled.set(true)
        }
    }

    // Register asset through FastAPI
    fun registerAsset(form: AssetFormData) {
        registrationSaving = true
        registrationError = null

        Thread {
            try {
                GroundMapApi.createAsset(
                    workspaceId = form.workspaceId,
                    roomId = form.roomId,
                    assetCode = form.assetCode,
                    name = form.name,
                    category = form.category,
                    manufacturer = form.manufacturer,
                    modelNumber = form.modelNumber,
                    serialNumber = form.serialNumber,
                    purchaseDate = form.purchaseDate,
                    status = form.status,
                    condition = form.condition,
                    notes = form.notes
                )

                Handler(Looper.getMainLooper()).post {
                    registrationSaving = false
                    registrationError = null

                    // Refresh the selected workspace's asset inventory.
                    assetRefresh++

                    // Return to the asset registry.
                    selectedAsset = null
                    selectedTab = 1
                }

            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    registrationSaving = false
                    registrationError = e.message
                }
            }
        }.start()
    }

    fun updateAsset(form: AssetEditFormData) {
        val assetId = selectedAsset?.id ?: return

        editSaving = true
        editError = null

        Thread {
            try {
                val updatedAsset = GroundMapApi.updateAsset(
                    assetId = assetId,
                    name = form.name,
                    category = form.category,
                    manufacturer = form.manufacturer,
                    modelNumber = form.modelNumber,
                    serialNumber = form.serialNumber,
                    purchaseDate = form.purchaseDate,
                    notes = form.notes
                )

                Handler(Looper.getMainLooper()).post {
                    selectedAsset = updatedAsset
                    editSaving = false
                    editError = null
                    assetRefresh++
                    selectedTab = 6
                }

            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    editSaving = false
                    editError = e.message ?: "Unable to update asset."
                }
            }
        }.start()
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            background = Background,
            surface = CardColor,
            onBackground = White,
            onSurface = White
        )
    ) {
        Scaffold(
            containerColor = Background,

            floatingActionButton = {
                if (selectedTab == 1) {
                    FloatingActionButton(
                        onClick = {
                            registrationError = null
                            selectedTab = 5
                        },
                        containerColor = Accent,
                        contentColor = White
                    ) {
                        Text("+", fontSize = 28.sp)
                    }
                }
            },

            bottomBar = {
                NavigationBar(containerColor = CardColor) {
                    val items = listOf(
                        "Home" to "⌂",
                        "Assets" to "▦",
                        "Scan" to "▣",
                        "Activity" to "◷",
                        "Workspace" to "W"
                    )

                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                selectedAsset = null
                            },
                            icon = {
                                Text(item.second, fontSize = 21.sp)
                            },
                            label = {
                                Text(item.first, fontSize = 10.sp)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Accent,
                                selectedTextColor = Accent,
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted,
                                indicatorColor = Color(0xFF253653)
                            )
                        )
                    }
                }
            }
        ) { padding ->

            when (selectedTab) {

                // HOME
                0 -> DashboardScreen(
                    workspaceName = selectedWorkspace?.name
                        ?: "Loading workspace...",
                    assets = assets,
                    loading = assetLoading,
                    error = assetError,
                    modifier = Modifier.padding(padding),
                    onRefresh = {
                        workspaceRefresh++
                        assetRefresh++
                    },
                    onNavigate = { selectedTab = it }
                )

                // ASSET REGISTRY
                1 -> {
                    AssetRegistryScreen(
                        assets = assets,
                        loading = assetLoading,
                        error = assetError,
                        onAssetSelected = {
                            selectedAsset = it
                            selectedTab = 6
                        },
                        onRefresh = {
                            assetRefresh++
                        },
                        modifier = Modifier.padding(padding)
                    )
                }

                // QR SCANNER PLACEHOLDER
                2 -> SimpleScreen(
                    "QR Scanner",
                    "Camera-based QR scanning is coming next.",
                    "▣",
                    Modifier.padding(padding)
                )

                // ACTIVITY PLACEHOLDER
                3 -> SimpleScreen(
                    "Activity",
                    "Asset event history will appear here.",
                    "◷",
                    Modifier.padding(padding)
                )

                // WORKSPACE SELECTOR
                4 -> WorkspaceSelectorScreen(
                    workspaces = workspaces,
                    selectedWorkspaceId = selectedWorkspace?.id,
                    loading = workspaceLoading,
                    error = workspaceError,
                    onWorkspaceSelected = { workspace ->
                        selectedWorkspace = workspace
                        selectedAsset = null
                        selectedTab = 0
                    },
                    onRefresh = {
                        workspaceRefresh++
                    },
                    modifier = Modifier.padding(padding)
                )

                // ASSET REGISTRATION
                5 -> AssetRegistrationScreen(
                    workspaces = workspaces,
                    loading = workspaceLoading,
                    error = registrationError,
                    saving = registrationSaving,
                    saveMessage = null,
                    onRegister = { form ->
                        registerAsset(form)
                    },
                    onBack = {
                        selectedTab = 1
                        registrationError = null
                    },
                    modifier = Modifier.padding(padding)
                )

                // ASSET DETAILS
                6 -> {
                    val asset = selectedAsset

                    if (asset != null) {
                        AssetDetailsScreen(
                            asset = asset,
                            onBack = {
                                selectedTab = 1
                            },
                            onEdit = {
                                editError = null
                                selectedTab = 7
                            },
                            modifier = Modifier.padding(padding)
                        )
                    } else {
                        SimpleScreen(
                            title = "Asset not selected",
                            description = "Select an asset from the registry.",
                            symbol = "!",
                            modifier = Modifier.padding(padding)
                        )
                    }
                }

                // EDIT ASSET
                7 -> {
                    val asset = selectedAsset

                    if (asset != null) {
                        AssetEditScreen(
                            asset = asset,
                            saving = editSaving,
                            error = editError,
                            onSave = { form ->
                                updateAsset(form)
                            },
                            onBack = {
                                editError = null
                                selectedTab = 6
                            },
                            modifier = Modifier.padding(padding)
                        )
                    } else {
                        SimpleScreen(
                            title = "Asset not selected",
                            description = "Return to the asset registry.",
                            symbol = "!",
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    workspaceName: String,
    assets: List<AssetData>,
    loading: Boolean,
    error: String?,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    onNavigate: (Int) -> Unit
) {
    val operational = assets.count {
        it.status == "active"
    }

    val maintenance = assets.count {
        it.status == "maintenance"
    }

    val damaged = assets.count {
        it.condition == "damaged"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text(
                    "GROUNDMAP",
                    color = Accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(7.dp))

                Text(
                    "Good day!",
                    color = White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Your physical world, organized.",
                    color = Muted,
                    fontSize = 13.sp
                )
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF172B49)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "CURRENT WORKSPACE",
                        color = Color(0xFF93C5FD),
                        fontSize = 10.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        workspaceName,
                        color = White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Connected to GroundMap API",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )

                    TextButton(
                        onClick = { onNavigate(4) }
                    ) {
                        Text("Switch workspace")
                    }
                }
            }
        }

        item {
            SectionTitle("Asset Overview")

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    "Total Assets",
                    assets.size.toString(),
                    "▦",
                    Accent,
                    Modifier.weight(1f)
                )

                StatCard(
                    "Operational",
                    operational.toString(),
                    "✓",
                    Green,
                    Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    "Maintenance",
                    maintenance.toString(),
                    "⚙",
                    Orange,
                    Modifier.weight(1f)
                )

                StatCard(
                    "Damaged",
                    damaged.toString(),
                    "!",
                    Red,
                    Modifier.weight(1f)
                )
            }
        }

        item {
            when {
                loading -> Text(
                    "Synchronizing...",
                    color = Muted
                )

                error != null -> Text(
                    "API error: $error",
                    color = Red,
                    fontSize = 12.sp
                )

                else -> Text(
                    "Live data from GroundMap API",
                    color = Green,
                    fontSize = 12.sp
                )
            }

            TextButton(onClick = onRefresh) {
                Text("Refresh dashboard")
            }
        }

        item {
            SectionTitle("Quick Actions")

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard(
                    "View Assets",
                    "Browse equipment",
                    "▦",
                    Modifier.weight(1f)
                ) {
                    onNavigate(1)
                }

                ActionCard(
                    "Register Asset",
                    "Add new equipment",
                    "+",
                    Modifier.weight(1f)
                ) {
                    onNavigate(5)
                }
            }
        }

        item {
            SectionTitle("Recent Activity")

            Spacer(Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CardColor
                )
            ) {
                Text(
                    "Activity history will appear here.",
                    color = Muted,
                    modifier = Modifier.padding(18.dp)
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        color = White,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun StatCard(
    title: String,
    value: String,
    symbol: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardColor
        )
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(symbol, color = color, fontSize = 22.sp)

            Text(
                value,
                color = White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                title,
                color = Muted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    description: String,
    symbol: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                symbol,
                color = Accent,
                fontSize = 25.sp
            )

            Text(
                title,
                color = White,
                fontWeight = FontWeight.Bold
            )

            Text(
                description,
                color = Muted,
                fontSize = 11.sp
            )

            TextButton(onClick = onClick) {
                Text("Open", color = Accent)
            }
        }
    }
}

@Composable
fun DetailLine(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            label,
            color = Muted,
            fontSize = 10.sp
        )

        Text(
            value,
            color = White,
            fontSize = 13.sp
        )
    }
}

@Composable
fun SimpleScreen(
    title: String,
    description: String,
    symbol: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            symbol,
            color = Accent,
            fontSize = 54.sp
        )

        Spacer(Modifier.height(18.dp))

        Text(
            title,
            color = White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            description,
            color = Muted,
            fontSize = 14.sp
        )
    }
}
