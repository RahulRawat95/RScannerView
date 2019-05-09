package com.rr.scannerview

import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

/**
 * Created by Rahul Rawat on 4/6/19.
 */
interface ResultHandler {
    fun handleResults(barcodes: List<FirebaseVisionBarcode>)

    fun handleResult(barcode: FirebaseVisionBarcode)
}