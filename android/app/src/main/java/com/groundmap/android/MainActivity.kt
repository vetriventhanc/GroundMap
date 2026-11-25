
package com.groundmap.android

import com.groundmap.android.ui.assets.AssetOperationsScreen
import com.groundmap.android.ui.assets.FaultReportsScreen
import com.groundmap.android.ui.assets.FaultReportUiData
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
import com.groundmap.android.ui.scanner.QrScannerScreen
import com.groundmap.android.data.remote.*
import com.groundmap.android.ui.assets.AssetRegistrationScreen
import com.groundmap.android.ui.assets.AssetRegistryScreen
import com.groundmap.android.ui.assets.AssetFormData
import com.groundmap.android.ui.assets.AssetHistoryScreen
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

    var previousOperationsTab by remember {
        mutableIntStateOf(2)
    }

    var workspaces by remember {
        mutableStateOf<List<WorkspaceData>>(emptyList())
    }

    var selectedWorkspace by remember {
        mutableStateOf<WorkspaceData?>(null)
    }

    var assets by remember {
        mutableStateOf<List<AssetData>>(emptyList())
    }

    var locationRooms by remember {
        mutableStateOf<List<RoomLocationData>>(emptyList())
    }
    var locationSaving by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }

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

    var statusSaving by remember { mutableStateOf(false) }
    var conditionSaving by remember { mutableStateOf(false) }

    var statusError by remember { mutableStateOf<String?>(null) }
    var conditionError by remember { mutableStateOf<String?>(null) }

    var assetHistory by remember {
        mutableStateOf<List<AssetHistoryData>>(emptyList())
    }

    var historyLoading by remember { mutableStateOf(false) }
    var historyError by remember { mutableStateOf<String?>(null) }
    var historyRefresh by remember { mutableIntStateOf(0) }

    // FAULT REPORT STATES
    var faultReports by remember {
        mutableStateOf<List<FaultReportData>>(emptyList())
    }

    var faultReportLoading by remember { mutableStateOf(false) }
    var faultReportSaving by remember { mutableStateOf(false) }
    var faultReportError by remember { mutableStateOf<String?>(null) }
    var faultReportSuccess by remember { mutableStateOf<String?>(null) }
    var faultReportRefresh by remember { mutableIntStateOf(0) }

    var assetQrImage by remember {
        mutableStateOf<ByteArray?>(null)
    }

    var qrLoading by remember { mutableStateOf(false) }
    var qrError by remember { mutableStateOf<String?>(null) }
    var qrRefresh by remember { mutableIntStateOf(0) }

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

    DisposableEffect(selectedWorkspace?.id) {
        val workspaceId = selectedWorkspace?.id
        val cancelled = AtomicBoolean(false)

        if (workspaceId == null) {
            locationRooms = emptyList()
        } else {
            Thread {
                try {
                    val tree = GroundMapApi.loadLocationTree(workspaceId)

                    val rooms = tree.sites.flatMap { site ->
                        site.buildings.flatMap { building ->
                            building.floors.flatMap { floor ->
                                floor.rooms
                            }
                        }
                    }

                    Handler(Looper.getMainLooper()).post {
                        if (!cancelled.get()) {
                            locationRooms = rooms
                        }
                    }
                } catch (e: Exception) {
                    Handler(Looper.getMainLooper()).post {
                        if (!cancelled.get()) {
                            locationRooms = emptyList()
                        }
                    }
                }
            }.start()
        }

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

    fun updateAssetStatus(status: String) {
        val assetId = selectedAsset?.id ?: return

        statusSaving = true
        statusError = null

        Thread {
            try {
                val updatedAsset = GroundMapApi.updateAssetStatus(
                    assetId = assetId,
                    status = status
                )

                Handler(Looper.getMainLooper()).post {
                    selectedAsset = updatedAsset
                    statusSaving = false
                    assetRefresh++
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    statusSaving = false
                    statusError = e.message ?: "Unable to update status."
                }
            }
        }.start()
    }

    fun updateAssetCondition(condition: String) {
        val assetId = selectedAsset?.id ?: return

        conditionSaving = true
        conditionError = null

        Thread {
            try {
                val updatedAsset = GroundMapApi.updateAssetCondition(
                    assetId = assetId,
                    condition = condition
                )

                Handler(Looper.getMainLooper()).post {
                    selectedAsset = updatedAsset
                    conditionSaving = false
                    assetRefresh++
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    conditionSaving = false
                    conditionError = e.message ?: "Unable to update condition."
                }
            }
        }.start()
    }

    fun updateAssetLocation(roomId: String?) {
        val assetId = selectedAsset?.id ?: return

        locationSaving = true
        locationError = null

        Thread {
            try {
                val updatedAsset = GroundMapApi.updateAssetLocation(
                    assetId = assetId,
                    roomId = roomId
                )

                Handler(Looper.getMainLooper()).post {
                    selectedAsset = updatedAsset
                    locationSaving = false
                    locationError = null
                    assetRefresh++
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    locationSaving = false
                    locationError = e.message ?: "Unable to update asset location."
                }
            }
        }.start()
    }

    fun loadAssetHistory() {
        val assetId = selectedAsset?.id ?: return

        historyLoading = true
        historyError = null

        Thread {
            try {
                val result = GroundMapApi.loadAssetHistory(assetId)

                Handler(Looper.getMainLooper()).post {
                    assetHistory = result
                    historyLoading = false
                    historyError = null
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    historyLoading = false
                    historyError = e.message ?: "Unable to load asset history."
                }
            }
        }.start()
    }


    fun loadFaultReports() {
        val assetId = selectedAsset?.id ?: return

        faultReportLoading = true
        faultReportError = null

        Thread {
            try {
                val result = GroundMapApi.loadFaultReports(assetId)

                Handler(Looper.getMainLooper()).post {
                    faultReports = result
                    faultReportLoading = false
                    faultReportError = null
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    faultReportLoading = false
                    faultReportError =
                        e.message ?: "Unable to load fault reports."
                }
            }
        }.start()
    }

    fun submitFaultReport(
        title: String,
        description: String,
        severity: String
    ) {
        val assetId = selectedAsset?.id ?: return

        faultReportSaving = true
        faultReportError = null
        faultReportSuccess = null

        Thread {
            try {
                GroundMapApi.createFaultReport(
                    assetId = assetId,
                    title = title,
                    description = description,
                    severity = severity
                )

                Handler(Looper.getMainLooper()).post {
                    faultReportSaving = false
                    faultReportSuccess = "Fault report submitted successfully."
                    faultReportRefresh++
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    faultReportSaving = false
                    faultReportError =
                        e.message ?: "Unable to submit fault report."
                }
            }
        }.start()
    }

    fun updateFaultReport(
        reportId: String,
        status: String,
        resolutionNotes: String?
    ) {
        val assetId = selectedAsset?.id ?: return

        faultReportSaving = true
        faultReportError = null
        faultReportSuccess = null

        Thread {
            try {
                GroundMapApi.updateFaultReport(
                    assetId = assetId,
                    reportId = reportId,
                    status = status,
                    resolutionNotes = resolutionNotes
                )

                Handler(Looper.getMainLooper()).post {
                    faultReportSaving = false
                    faultReportSuccess = "Fault report updated successfully."
                    faultReportRefresh++
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    faultReportSaving = false
                    faultReportError =
                        e.message ?: "Unable to update fault report."
                }
            }
        }.start()
    }


    fun loadAssetQr() {
        val assetId = selectedAsset?.id ?: return

        qrLoading = true
        qrError = null

        Thread {
            try {
                val result = GroundMapApi.loadAssetQr(assetId)

                Handler(Looper.getMainLooper()).post {
                    assetQrImage = result
                    qrLoading = false
                    qrError = null
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    assetQrImage = null
                    qrLoading = false
                    qrError = e.message ?: "Unable to load QR code."
                }
            }
        }.start()
    }

    LaunchedEffect(selectedTab, selectedAsset?.id, historyRefresh) {
        if (selectedTab == 8 && selectedAsset != null) {
            loadAssetHistory()
        }
    }

    LaunchedEffect(selectedTab, selectedAsset?.id, faultReportRefresh) {
        if (selectedTab == 10 && selectedAsset != null) {
            loadFaultReports()
        }
    }

    LaunchedEffect(selectedTab, selectedAsset?.id, qrRefresh) {
        if (selectedTab == 6 && selectedAsset != null) {
            assetQrImage = null
            loadAssetQr()
        }
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

                // QR SCANNER
                2 -> QrScannerScreen(
                    onAssetFound = { asset ->
                        selectedAsset = asset
                        previousOperationsTab = 2
                        selectedTab = 9
                    },
                    modifier = Modifier.padding(padding)
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
                            onViewHistory = {
                                assetHistory = emptyList()
                                historyError = null
                                selectedTab = 8
                            },
                            onUpdateStatus = { status ->
                                updateAssetStatus(status)
                            },
                            onUpdateCondition = { condition ->
                                updateAssetCondition(condition)
                            },
                            qrImageBytes = assetQrImage,
                            qrLoading = qrLoading,
                            qrError = qrError,
                            onRefreshQr = {
                                qrRefresh++
                            },
                            statusSaving = statusSaving,
                            conditionSaving = conditionSaving,
                            statusError = statusError,
                            conditionError = conditionError,
                            rooms = locationRooms,
                            locationSaving = locationSaving,
                            locationError = locationError,
                            onUpdateLocation = { roomId ->
                                updateAssetLocation(roomId)
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

                // ASSET HISTORY
                8 -> {
                    val asset = selectedAsset

                    if (asset != null) {
                        AssetHistoryScreen(
                            assetName = asset.name,
                            assetCode = asset.assetCode,
                            history = assetHistory,
                            loading = historyLoading,
                            error = historyError,
                            onBack = {
                                selectedTab = 6
                            },
                            onRefresh = {
                                historyRefresh++
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

                // ASSET OPERATIONS
                9 -> {
                    val asset = selectedAsset

                    if (asset != null) {
                        AssetOperationsScreen(
                            asset = asset,
                            onBack = {
                                selectedTab = previousOperationsTab
                            },
                            onViewDetails = {
                                selectedTab = 6
                            },
                            onUpdateStatus = { status ->
                                updateAssetStatus(status)
                            },
                            onUpdateCondition = { condition ->
                                updateAssetCondition(condition)
                            },
                            onReportFault = {
                                faultReportError = null
                                faultReportSuccess = null
                                selectedTab = 10
                            },
                            onViewHistory = {
                                assetHistory = emptyList()
                                historyError = null
                                selectedTab = 8
                            },
                            statusSaving = statusSaving,
                            conditionSaving = conditionSaving,
                            statusError = statusError,
                            conditionError = conditionError,
                            modifier = Modifier.padding(padding)
                        )
                    } else {
                        SimpleScreen(
                            title = "Asset not selected",
                            description = "Scan an asset QR code first.",
                            symbol = "!",
                            modifier = Modifier.padding(padding)
                        )
                    }
                }


                // FAULT REPORTS
                10 -> {
                    val asset = selectedAsset

                    if (asset != null) {
                        FaultReportsScreen(
                            assetName = asset.name,
                            assetCode = asset.assetCode,
                            reports = faultReports.map { report ->
                                FaultReportUiData(
                                    id = report.id,
                                    assetId = report.assetId,
                                    title = report.title,
                                    description = report.description,
                                    severity = report.severity,
                                    status = report.status,
                                    resolutionNotes = report.resolutionNotes,
                                    createdAt = report.createdAt
                                )
                            },
                            loading = faultReportLoading,
                            saving = faultReportSaving,
                            error = faultReportError,
                            successMessage = faultReportSuccess,
                            onSubmit = { title, description, severity ->
                                submitFaultReport(
                                    title,
                                    description,
                                    severity
                                )
                            },
                            onUpdateReport = { reportId, status, notes ->
                                updateFaultReport(reportId, status, notes)
                            },
                            onRefresh = {
                                faultReportRefresh++
                            },
                            onBack = {
                                faultReportError = null
                                faultReportSuccess = null
                                selectedTab = 9
                            },
                            modifier = Modifier.padding(padding)
                        )
                    } else {
                        SimpleScreen(
                            title = "Asset not selected",
                            description = "Scan an asset QR code first.",
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
    val operational = assets.count { it.status.equals("active", ignoreCase = true) }
    val maintenance = assets.count { it.status.equals("maintenance", ignoreCase = true) }
    val damaged = assets.count { it.condition.equals("damaged", ignoreCase = true) }

    val categoryCounts = assets
        .groupBy { it.category.ifBlank { "Uncategorized" } }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

    val maxCategoryCount = categoryCounts.maxOfOrNull { it.second } ?: 1

    val pageBackground = Color(0xFF0B1024)
    val panelColor = Color(0xFF171D38)
    val purple = Color(0xFF8B5CF6)
    val lightText = Color(0xFFF8FAFC)
    val mutedText = Color(0xFFA8B2D1)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(pageBackground),
        contentPadding = PaddingValues(
            start = 18.dp,
            top = 20.dp,
            end = 18.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column {
                Text(
                    "GroundMap",
                    color = lightText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(18.dp))

                Text(
                    "Asset Dashboard",
                    color = lightText,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    "Real-time facility insights",
                    color = mutedText,
                    fontSize = 13.sp
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = panelColor)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "CURRENT WORKSPACE",
                        color = purple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        workspaceName,
                        color = lightText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    TextButton(onClick = { onNavigate(4) }) {
                        Text("Switch workspace  →", color = purple)
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardMetricCard(
                        title = "Total Assets",
                        value = assets.size.toString(),
                        symbol = "▦",
                        tint = Color(0xFF9B8CFF),
                        modifier = Modifier.weight(1f)
                    )

                    DashboardMetricCard(
                        title = "Active",
                        value = operational.toString(),
                        symbol = "✓",
                        tint = Color(0xFF65D6B0),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardMetricCard(
                        title = "Maintenance",
                        value = maintenance.toString(),
                        symbol = "⚙",
                        tint = Color(0xFFFFC66D),
                        modifier = Modifier.weight(1f)
                    )

                    DashboardMetricCard(
                        title = "Damaged",
                        value = damaged.toString(),
                        symbol = "!",
                        tint = Color(0xFFFF8295),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = panelColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Assets by Category",
                        color = lightText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(14.dp))

                    if (categoryCounts.isEmpty()) {
                        Text(
                            "Category information will appear when assets are available.",
                            color = mutedText,
                            fontSize = 12.sp
                        )
                    } else {
                        categoryCounts.forEach { (category, count) ->
                            Column(
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        category,
                                        color = mutedText,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        count.toString(),
                                        color = lightText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(Modifier.height(6.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .background(
                                            Color(0xFF292F4D),
                                            RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(
                                                (count.toFloat() / maxCategoryCount)
                                                    .coerceIn(0.05f, 1f)
                                            )
                                            .fillMaxHeight()
                                            .background(
                                                purple,
                                                RoundedCornerShape(8.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = panelColor)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        when {
                            loading -> "Synchronizing with GroundMap…"
                            error != null -> "Dashboard connection issue"
                            else -> "Dashboard is up to date"
                        },
                        color = when {
                            loading -> mutedText
                            error != null -> Color(0xFFFF8295)
                            else -> Color(0xFF65D6B0)
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (error != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "API error: $error",
                            color = Color(0xFFFF8295),
                            fontSize = 12.sp
                        )
                    }

                    TextButton(onClick = onRefresh) {
                        Text("Refresh data", color = purple)
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Quick Actions",
                    color = lightText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onNavigate(1) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF29234F)
                        )
                    ) {
                        Text("View Assets")
                    }

                    Button(
                        onClick = { onNavigate(5) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3D286A)
                        )
                    ) {
                        Text("Register Asset")
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    symbol: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF171D38)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                symbol,
                color = tint,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                value,
                color = Color(0xFFF8FAFC),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(3.dp))

            Text(
                title,
                color = Color(0xFFA8B2D1),
                fontSize = 11.sp
            )
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
