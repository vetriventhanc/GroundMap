
package com.groundmap.android.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private val ScannerBackground = Color(0xFF0B1120)
private val ScannerCard = Color(0xFF151F32)
private val ScannerAccent = Color(0xFF3B82F6)
private val ScannerMuted = Color(0xFF94A3B8)
private val ScannerWhite = Color(0xFFF8FAFC)
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
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "QR Scanner",
            color = ScannerWhite,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Scan a GroundMap asset QR code",
            color = ScannerMuted,
            fontSize = 14.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    ScannerCard,
                    RoundedCornerShape(20.dp)
                ),
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
                            "Loading asset details...",
                            color = ScannerWhite
                        )
                    }
                }

                !cameraPermissionGranted -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            "Camera permission is required for live scanning.",
                            color = ScannerWhite
                        )

                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    Manifest.permission.CAMERA
                                )
                            }
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

        viewModel.errorMessage?.let { message ->
            Text(
                text = message,
                color = ScannerRed,
                fontSize = 13.sp
            )
        }

        cameraError?.let { message ->
            Text(
                text = message,
                color = ScannerRed,
                fontSize = 13.sp
            )
        }

        imageScanError?.let { message ->
            Text(
                text = message,
                color = ScannerRed,
                fontSize = 13.sp
            )
        }

        Button(
            onClick = {
                imageScanError = null
                imagePickerLauncher.launch("image/*")
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = ScannerAccent
            )
        ) {
            Text("Scan from Image")
        }

        Text(
            text = "Or position the QR code inside the live camera view.",
            color = ScannerMuted,
            fontSize = 12.sp
        )

        if (
            viewModel.errorMessage != null ||
            cameraError != null ||
            imageScanError != null
        ) {
            Button(
                onClick = {
                    viewModel.reset()
                    cameraError = null
                    imageScanError = null
                    cameraSessionKey++
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScannerAccent
                )
            ) {
                Text("Scan Again")
            }
        }

        Spacer(Modifier.height(4.dp))
    }
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
