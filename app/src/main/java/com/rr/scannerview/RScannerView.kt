package com.rr.scannerview

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.RectF
import android.os.AsyncTask
import android.os.Handler
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.preview.Frame
import io.fotoapparat.preview.FrameProcessor
import io.fotoapparat.selector.*
import kotlinx.android.synthetic.main.view_r_scanner.view.*

/**
 * Created by Rahul Rawat on 5/9/19.
 */
class RScannerView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.view_r_scanner, this)
    }

    private var camera: Fotoapparat? = null
    private var resultHandler: ResultHandler? = null
    private var isProcessing = false

    private var isFrontCameraScanning = false

    private val firebaseBarcodeOptions by lazy {
        if (scannableBarcodeFormats == null || scannableBarcodeFormats?.size == 0) {
            FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
                .build()
        } else if (scannableBarcodeFormats!!.size == 1) {
            FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(scannableBarcodeFormats!![0])
                .build()
        } else {
            FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(
                    scannableBarcodeFormats!![0],
                    *(scannableBarcodeFormats!!.copyOfRange(1, scannableBarcodeFormats!!.size))
                )
                .build()
        }
    }

    private val detector: FirebaseVisionBarcodeDetector by lazy {
        FirebaseVision.getInstance()
            .getVisionBarcodeDetector(firebaseBarcodeOptions)
    }

    private var scannableBarcodeFormats: IntArray? = null

    private val cameraConfiguration = CameraConfiguration
        .builder()
        .photoResolution(
            standardRatio(
                highestResolution()
            )
        )
        .focusMode(
            firstAvailable(
                continuousFocusPicture(),
                autoFocus(),
                fixed()
            )
        )
        .flash(
            off()
        )
        .previewFpsRange(highestFps())
        .sensorSensitivity(highestSensorSensitivity())
        .frameProcessor(object : FrameProcessor {
            override fun process(frame: Frame) {
                if (resultHandler == null) {
                    return
                }
                if (isProcessing) {
                    return
                }
                isProcessing = true

                val metadata = FirebaseVisionImageMetadata.Builder()
                    .setWidth(frame.size.width)
                    .setHeight(frame.size.height)
                    .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                    .setRotation(frame.rotation / 90)
                    .build()

                val image = FirebaseVisionImage.fromByteArray(frame.image, metadata)

                try {
                    detector.detectInImage(image)
                        .addOnSuccessListener { barcodes ->
                            if (barcodes.size > 0) {
                                (object : AsyncTask<Void, Void, Unit>() {
                                    var barcode: FirebaseVisionBarcode? = null
                                    override fun doInBackground(vararg params: Void?) {
                                        val bitmap = image.bitmapForDebugging
                                        for (barcode in barcodes) {
                                            if (barcode.boundingBox?.convertToRectF()?.hasPoints(
                                                    bitmap.width.toFloat(),
                                                    bitmap.height.toFloat()
                                                ) == true
                                            ) {
                                                this.barcode = barcode
                                                break
                                            }
                                        }
                                    }

                                    override fun onPostExecute(result: Unit?) {
                                        if (barcode != null) {
                                            resultHandler?.handleResult(barcode!!)
                                        }
                                        isProcessing = false
                                    }
                                }).execute()
                            } else {
                                isProcessing = false
                            }
                        }
                        .addOnFailureListener {
                            isProcessing = false
                        }
                    //view_finder?.addBarCodes(null)
                } catch (e: Exception) {
                    isProcessing = false
                }
            }

        })
        .build()

    /**
     * Method that starts the camera and scanning process depending on what camera was being used before
     */
    fun start() {
        if (isFrontCameraScanning) {
            startFrontCamera()
        } else {
            startBackCamera()
        }
    }

    /**
     * Method that starts scanning with the Back Camera
     */
    fun startBackCamera() {
        if (cameraPermissionGranted()) {
            camera?.stop()
            initBackCamera()
        } else {
            showToast(context)
        }
    }

    /**
     * Method that starts scanning with the Front Camera
     */
    fun startFrontCamera() {
        if (!hasFrontCamera(context)) {
            showToast(context, "No Front Facing Camera detected. Falling back to Back Camera")
            startBackCamera()
            return
        }
        if (cameraPermissionGranted()) {
            camera?.stop()
            initFrontCamera()
        } else {
            showToast(context)
        }
    }

    /**
     * Method that switches the camera being used to scan i.e. Front to Back and Back to Front
     */
    fun switchCamera() {
        if (isFrontCameraScanning) {
            startBackCamera()
        } else {
            startFrontCamera()
        }
    }

    /**
     * Method that stops the camera and scanning process
     */
    fun stop() {
        isProcessing = false
        camera?.stop()
        camera = null
    }

    /**
     * Method that pauses the camera and processing of frames together
     */
    fun pauseCamera() {
        camera?.stop()
    }

    /**
     * Method that resumes the camera and processing of frames together
     */
    fun resumeCamera() {
        isProcessing = false
        camera?.start()
    }

    private fun initBackCamera() =
        Fotoapparat(
            context = context,
            view = camera_view,
            scaleType = ScaleType.CenterCrop,
            lensPosition = back(),
            cameraConfiguration = cameraConfiguration
        ).run {
            camera = this
            isFrontCameraScanning = false
            camera?.start()
        }

    private fun initFrontCamera() =
        Fotoapparat(
            context = context,
            view = camera_view,
            scaleType = ScaleType.CenterCrop,
            lensPosition = front(),
            cameraConfiguration = cameraConfiguration
        ).run {
            camera = this
            isFrontCameraScanning = true
            camera?.start()
        }

    private fun cameraPermissionGranted() = isPermissionGranted(context, android.Manifest.permission.CAMERA)

    /**
     * Method that sets where the results will be passed
     *
     * @param resultHandler The callback where results will be passed
     *
     * @return Unit
     */
    fun setResultHandler(resultHandler: ResultHandler) {
        this@RScannerView.resultHandler = resultHandler
    }

    /**
     * @param formats The formats that are scannable and detectable by FirebaseVision
     * if null or 0 arguments are passed all formats will be scanned and detected
     *
     * @return Unit
     */
    fun setBarcodeFormats(vararg formats: Int) {
        scannableBarcodeFormats = formats
    }

    companion object {
        private var toast: Toast? = null

        private fun isPermissionGranted(context: Context, permission: String): Boolean {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                return true
            }
            return false
        }

        private fun Rect.convertToRectF(): RectF {
            val rectF = RectF()
            rectF.left = left.toFloat()
            rectF.right = right.toFloat()
            rectF.top = top.toFloat()
            rectF.bottom = bottom.toFloat()
            return rectF
        }

        private fun RectF.hasPoints(width: Float, height: Float) =
            contains((width / 1.99).toFloat(), (height / 1.99).toFloat())
                    &&
                    contains((width / 2), (height / 1.99).toFloat())
                    &&
                    contains((width / 2.01).toFloat(), (height / 1.99).toFloat())
                    &&
                    contains((width / 1.99).toFloat(), (height / 2))
                    &&
                    contains((width / 2), (height / 2))
                    &&
                    contains((width / 2.01).toFloat(), (height / 2))
                    &&
                    contains((width / 1.99).toFloat(), (height / 2.01).toFloat())
                    &&
                    contains((width / 2), (height / 2.01).toFloat())
                    &&
                    contains((width / 2.01).toFloat(), (height / 2.01).toFloat())

        private fun showToast(context: Context, message: String = "Please give Camera Permission First") {
            if (toast != null) {
                toast?.cancel()
            }
            toast = Toast.makeText(context, message, Toast.LENGTH_LONG).apply {
                setGravity(Gravity.CENTER, 0, 0)
            }
            toast?.show()
        }

        private fun hasFrontCamera(context: Context) = when {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) -> true
            else -> false
        }
    }
}