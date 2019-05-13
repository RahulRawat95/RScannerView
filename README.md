Introduction
============

Android library projects that provides easy to use and extensible Barcode Scanner view based on Firebase Vision Barcode and Fotoapparat alongwith overlay that looks like WhatsApp scanner.

Screenshots
===========
<img src="https://raw.githubusercontent.com/RahulRawat95/RScannerView/master/screenshots/screenshot1.gif" width="266">

Installation
------------

Add the following dependency to your project level build.gradle file.

```
repositories {
   maven { url 'https://jitpack.io' }
}
```

And Add the following dependency to your build.gradle file.

```
implementation 'com.google.firebase:firebase-ml-vision:19.0.3'
implementation 'com.github.RahulRawat95:RScannerView:1.0.0-beta'
```

Simple Usage
------------

In Java

```java
public class MainActivity extends AppCompatActivity implements ResultHandler {

    private RScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scannerView = findViewById(R.id.scanner_view);
        scannerView.setResultHandler(this);
        scannerView.setProcessingDelay(1000);
        scannerView.setBarcodeFormats(FirebaseVisionBarcode.FORMAT_AZTEC, FirebaseVisionBarcode.FORMAT_DATA_MATRIX);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scannerView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stop();
    }

    @Override
    public void handleResult(FirebaseVisionBarcode firebaseVisionBarcode) {

    }

    @Override
    public void handleResults(List<? extends FirebaseVisionBarcode> list) {

    }
}

```

In Kotlin

```kotlin
class MainActivity : AppCompatActivity(), ResultHandler {
    override fun handleResults(barcodes: List<FirebaseVisionBarcode>) {

    }

    override fun handleResult(barcode: FirebaseVisionBarcode) {
        Log.d("dexter", barcode.rawValue)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scanner_view.setResultHandler(this)
        scanner_view.processingDelay = 1000
        scanner_view.setBarcodeFormats(FirebaseVisionBarcode.FORMAT_AZTEC, FirebaseVisionBarcode.FORMAT_DATA_MATRIX)
    }

    override fun onResume() {
        super.onResume()
        scanner_view.start()
    }

    override fun onPause() {
        super.onPause()
        scanner_view.stop()
    }
}

```

activity_main.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.rr.scannerview.RScannerView
    android:id="@+id/scanner_view"
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>

```

Methods Overview
--------------


Interesting methods on the RScannerView include:

```java
// Set callback to deliver the scanned barcode:
void setResultHandler(com.rr.scannerview.ResultHandler);

// Specify interested barcode formats:
//only allowed values are those specified in FirebaseVisionBarcode class
//i.e. FirebaseVisionBarcode.FORMAT_AZTEC, FirebaseVisionBarcode.FORMAT_DATA_MATRIX etc.
//if not called or called with null or zero elements the scanner will scan for all Barcode Formats
void setBarcodeFormats(int[]);

// start scanning:
//call in onResume
void start();

// stop scanning:
//call in onPause
void stop();

// pauses the camera and the processing of barcodes
void pauseCamera();

// resumes the paused camera and the processing of barcodes
void resumeCamera();
```

Supported Formats:

```java
FirebaseVisionBarcode.FORMAT_ALL_FORMATS
FirebaseVisionBarcode.FORMAT_AZTEC
FirebaseVisionBarcode.FORMAT_CODABAR
FirebaseVisionBarcode.FORMAT_CODE_128
FirebaseVisionBarcode.FORMAT_CODE_39
FirebaseVisionBarcode.FORMAT_CODE_93
FirebaseVisionBarcode.FORMAT_DATA_MATRIX
FirebaseVisionBarcode.FORMAT_EAN_13
FirebaseVisionBarcode.FORMAT_EAN_8
FirebaseVisionBarcode.FORMAT_ITF
FirebaseVisionBarcode.FORMAT_PDF417
FirebaseVisionBarcode.FORMAT_QR_CODE
FirebaseVisionBarcode.FORMAT_UNKNOWN
FirebaseVisionBarcode.FORMAT_UPC_A
FirebaseVisionBarcode.FORMAT_UPC_E
```

Credits
=======

Almost all of the code for this library project is based on:

1. Fotoapparat: https://github.com/RedApparat/Fotoapparat
2. Firebase Vision Barcode: https://firebase.google.com/docs/reference/android/com/google/firebase/ml/vision/barcode/FirebaseVisionBarcodeDetector

Contributors
============

https://github.com/RahulRawat95/RScannerView/graphs/contributors

License
=======
Apache License, Version 2.0