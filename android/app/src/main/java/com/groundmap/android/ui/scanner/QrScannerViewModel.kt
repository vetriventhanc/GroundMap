
package com.groundmap.android.ui.scanner

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.groundmap.android.data.remote.AssetData
import com.groundmap.android.data.remote.GroundMapApi
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class QrScannerViewModel {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val executor: ExecutorService =
        Executors.newSingleThreadExecutor()

    private val mainHandler = Handler(Looper.getMainLooper())

    private val isRequestRunning = AtomicBoolean(false)

    fun findAsset(
        assetId: String,
        onAssetFound: (AssetData) -> Unit
    ) {
        if (!isRequestRunning.compareAndSet(false, true)) {
            return
        }

        isLoading = true
        errorMessage = null

        executor.execute {
            try {
                val asset = GroundMapApi.loadAsset(assetId)

                mainHandler.post {
                    isLoading = false
                    isRequestRunning.set(false)
                    onAssetFound(asset)
                }
            } catch (exception: Exception) {
                mainHandler.post {
                    isLoading = false
                    isRequestRunning.set(false)
                    errorMessage = exception.message
                        ?: "Unable to load the scanned asset."
                }
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun reset() {
        errorMessage = null
        isLoading = false
        isRequestRunning.set(false)
    }

    fun dispose() {
        executor.shutdownNow()
    }
}
