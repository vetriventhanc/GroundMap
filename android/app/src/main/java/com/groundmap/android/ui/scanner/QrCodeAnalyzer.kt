
package com.groundmap.android.ui.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

class QrCodeAnalyzer(
    private val onQrCodeDetected: (String) -> Unit,
    private val onScanError: (String) -> Unit = {}
) : ImageAnalysis.Analyzer {

    private val isProcessing = AtomicBoolean(false)
    private val hasScanned = AtomicBoolean(false)

    private val scannerOptions =
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

    private val scanner =
        BarcodeScanning.getClient(scannerOptions)

    override fun analyze(imageProxy: ImageProxy) {

        if (hasScanned.get() || !isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image

        if (mediaImage == null) {
            isProcessing.set(false)
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(image)
            .addOnSuccessListener { barcodes ->

                val rawValue = barcodes
                    .firstOrNull { it.rawValue != null }
                    ?.rawValue

                if (rawValue != null) {
                    val assetId = extractAssetId(rawValue)

                    if (assetId != null &&
                        hasScanned.compareAndSet(false, true)
                    ) {
                        onQrCodeDetected(assetId)
                    } else if (assetId == null) {
                        onScanError("Invalid GroundMap QR code.")
                    }
                }
            }
            .addOnFailureListener { exception ->
                onScanError(
                    exception.message ?: "QR scanning failed."
                )
            }
            .addOnCompleteListener {
                isProcessing.set(false)
                imageProxy.close()
            }
    }

    private fun extractAssetId(payload: String): String? {
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

    fun resetScan() {
        hasScanned.set(false)
    }

    fun close() {
        scanner.close()
    }
}
