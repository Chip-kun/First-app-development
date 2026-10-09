package com.example.ui.scanner

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val onBarcodeDetected: (barcodeValue: String, formatLabel: String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()
    private var lastScannedCode: String? = null
    private var lastScannedTimestamp: Long = 0L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue ?: barcode.displayValue
                        if (!rawValue.isNullOrBlank()) {
                            val now = System.currentTimeMillis()
                            // Debounce same barcode within 2 seconds
                            if (rawValue != lastScannedCode || (now - lastScannedTimestamp > 2000L)) {
                                lastScannedCode = rawValue
                                lastScannedTimestamp = now
                                val format = getFormatLabel(barcode.format)
                                onBarcodeDetected(rawValue, format)
                                break
                            }
                        }
                    }
                }
            }
            .addOnFailureListener {
                // Ignore transient frame analysis failures
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun getFormatLabel(format: Int): String {
        return when (format) {
            Barcode.FORMAT_EAN_13 -> "JAN-13 (EAN)"
            Barcode.FORMAT_EAN_8 -> "JAN-8 (EAN)"
            Barcode.FORMAT_UPC_A -> "UPC-A"
            Barcode.FORMAT_UPC_E -> "UPC-E"
            Barcode.FORMAT_QR_CODE -> "QRコード"
            Barcode.FORMAT_CODE_128 -> "Code 128"
            Barcode.FORMAT_CODE_39 -> "Code 39"
            Barcode.FORMAT_CODE_93 -> "Code 93"
            Barcode.FORMAT_CODABAR -> "Codabar (NW-7)"
            Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
            Barcode.FORMAT_ITF -> "ITF (Interleaved 2 of 5)"
            Barcode.FORMAT_PDF417 -> "PDF417"
            Barcode.FORMAT_AZTEC -> "Aztec"
            else -> "バーコード"
        }
    }
}
