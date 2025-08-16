package com.example.gpsmapcamera.cameraHelper

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class QRCodeAnalyzer(
    private val context: Context,
    private val onQRCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner: BarcodeScanner
    private val scannedValues = mutableSetOf<String>()
    private var lastScannedTime = 0L
    private val throttleDuration = 2000L // 2 seconds


    init {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        scanner = BarcodeScanning.getClient(options)
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                /*    for (barcode in barcodes) {
                        barcode.rawValue?.let {
                            Log.d("QRCodeAnalyzer", "QR Code: $it")
                            onQRCodeScanned(it)
                        }
                    }*/

                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastScannedTime >= throttleDuration) {        ////Limit detection frequency (throttle)" means to reduce how often QR codes are processed

                        for (barcode in barcodes) {     ///"Ignore repeated values entirely
                            barcode.rawValue?.let { value ->
                                if (!scannedValues.contains(value)) {
                                    scannedValues.add(value)
                                    Log.d("QRCodeAnalyzer", "QR Code: $value")
                                    onQRCodeScanned(value)
                                }
                            }
                        }
                    }

                }
                .addOnFailureListener {
                    Log.e("QRCodeAnalyzer", "QR scan failed: ${it.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    fun stop() {
        scanner.close()
    }

    fun resetDetectedValues() {
        scannedValues.clear()
    }
}
