package com.groundmap.android.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.groundmap.android.data.remote.AssetData
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.UUID

private val ScannerBackground = Color(0xFF0B1020)
private val ScannerCard = Color(0xFF171D3A)
private val ScannerAccent = Color(0xFF8B5CF6)
private val ScannerMuted = Color(0xFFA5AEC8)
private val ScannerWhite = Color(0xFFF8FAFC)
private val ScannerBorder = Color(0xFF303858)
private val ScannerRed = Color(0xFFEF4444)

@Composable
fun QrScannerScreen(
    onAssetFound: (AssetData) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    val viewModel = remember {
        QrScannerViewModel()
    }

    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var cameraSessionKey by remember {
        mutableIntStateOf(0)
    }

    var cameraError by remember {
        mutableStateOf<String?>(null)
    }

    var imageScanError by remember {
        mutableStateOf<String?>(null)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            cameraPermissionGranted = granted
        }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                imageScanError = null
                cameraError = null

                try {
                    val image = InputImage.fromFilePath(context, uri)

                    val options =
                        BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                            .build()

                    val scanner = BarcodeScanning.getClient(options)

                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            val payload = barcodes
                                .firstOrNull { it.rawValue != null }
                                ?.rawValue

                            val assetId = payload?.let {
                                extractGroundMapAssetId(it)
                            }

                            if (assetId == null) {
                                imageScanError =
                                    "No valid GroundMap asset QR code found."
                            } else {
                                viewModel.findAsset(
                                    assetId = assetId,
                                    onAssetFound = { asset ->
                                        onAssetFound(asset)
                                    }
                                )
                            }
                        }
                        .addOnFailureListener { exception ->
                            imageScanError =
                                exception.message ?: "Could not read the QR image."
                        }
                        .addOnCompleteListener {
                            scanner.close()
                        }
                } catch (exception: Exception) {
                    imageScanError =
                        exception.message ?: "Unable to open the selected image."
                }
            }
        }

    LaunchedEffect(Unit) {
        if (!cameraPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.dispose()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScannerBackground)
            .padding(horizontal = 18.dp)
            .padding(top = 14.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "G R O U N D M A P",
                color = ScannerAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "QR Scanner",
                color = ScannerWhite,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Scan a QR code to open its asset details.",
                color = ScannerMuted,
                fontSize = 13.sp
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = ScannerCard
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = ScannerBorder
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    viewModel.isLoading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = ScannerAccent
                            )

                            Text(
                                text = "Loading asset details...",
                                color = ScannerWhite,
                                fontSize = 14.sp
                            )
                        }
                    }

                    !cameraPermissionGranted -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Camera access is needed for live scanning.",
                                color = ScannerWhite,
                                fontSize = 14.sp
                            )

                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        Manifest.permission.CAMERA
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ScannerAccent
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Grant Camera Permission")
                            }
                        }
                    }

                    else -> {
                        key(cameraSessionKey) {
                            CameraPreview(
                                onQrCodeDetected = { assetId ->
                                    mainHandler.post {
                                        cameraError = null
                                        imageScanError = null

                                        viewModel.findAsset(
                                            assetId = assetId,
                                            onAssetFound = { asset ->
                                                onAssetFound(asset)
                                            }
                                        )
                                    }
                                },
                                onScanError = { message ->
                                    mainHandler.post {
                                        cameraError = message
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "LIVE SCAN",
            color = ScannerAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Position the asset QR code inside the camera view.",
            color = ScannerMuted,
            fontSize = 12.sp
        )

        viewModel.errorMessage?.let { message ->
            ScannerMessage(message)
        }

        cameraError?.let { message ->
            ScannerMessage(message)
        }

        imageScanError?.let { message ->
            ScannerMessage(message)
        }

        Button(
            onClick = {
                imageScanError = null
                imagePickerLauncher.launch("image/*")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !viewModel.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = ScannerAccent,
                disabledContainerColor = ScannerAccent.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Scan from Image",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (
            viewModel.errorMessage != null ||
            cameraError != null ||
            imageScanError != null
        ) {
            OutlinedButton(
                onClick = {
                    viewModel.reset()
                    cameraError = null
                    imageScanError = null
                    cameraSessionKey++
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ScannerAccent
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = ScannerBorder
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Scan Again")
            }
        }
    }
}

@Composable
private fun ScannerMessage(message: String) {
    Text(
        text = message,
        color = ScannerRed,
        fontSize = 12.sp
    )
}

private fun extractGroundMapAssetId(payload: String): String? {
    return try {
        val prefix = "groundmap://asset/"

        if (!payload.startsWith(prefix, ignoreCase = true)) {
            return null
        }

        val id = payload.substring(prefix.length)
            .substringBefore("?")
            .trim()
            .removeSuffix("/")

        UUID.fromString(id).toString()
    } catch (_: IllegalArgumentException) {
        null
    }
}