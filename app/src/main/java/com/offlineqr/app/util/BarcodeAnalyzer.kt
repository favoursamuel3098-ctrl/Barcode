package com.offlineqr.app.util

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer
import java.util.EnumMap
import java.util.concurrent.atomic.AtomicBoolean

class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java)
        hints[DecodeHintType.POSSIBLE_FORMATS] = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.CODE_128,
            BarcodeFormat.CODE_39,
            BarcodeFormat.CODE_93,
            BarcodeFormat.EAN_13,
            BarcodeFormat.EAN_8,
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E,
            BarcodeFormat.DATA_MATRIX,
            BarcodeFormat.AZTEC,
            BarcodeFormat.PDF_417,
            BarcodeFormat.ITF,
            BarcodeFormat.CODABAR
        )
        hints[DecodeHintType.TRY_HARDER] = true
        setHints(hints)
    }

    private val isProcessing = AtomicBoolean(false)
    private var lastResult: String? = null
    private var lastResultTime = 0L

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        if (isProcessing.get()) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null || imageProxy.format != ImageFormat.YUV_420_888) {
            imageProxy.close()
            return
        }

        isProcessing.set(true)

        try {
            val buffer = mediaImage.planes[0].buffer
            val data = buffer.toByteArray()
            val width = imageProxy.width
            val height = imageProxy.height

            val source = PlanarYUVLuminanceSource(
                data, width, height, 0, 0, width, height, false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decodeWithState(binaryBitmap)

            val text = result.text
            val now = System.currentTimeMillis()
            if (text != lastResult || now - lastResultTime > 2000) {
                lastResult = text
                lastResultTime = now
                onBarcodeDetected(text)
            }
        } catch (_: Exception) {
            // No barcode found
        } finally {
            reader.reset()
            isProcessing.set(false)
            imageProxy.close()
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }
}
