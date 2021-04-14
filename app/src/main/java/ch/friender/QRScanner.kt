package ch.friender

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException

class ScannedBarcodeActivity : AppCompatActivity() {
    var surfaceView: SurfaceView? = null
    var txtBarcodeValue: TextView? = null
    private lateinit var barcodeDetector: BarcodeDetector
    private var cameraSource: CameraSource? = null
    var btnAction: Button? = null
    var intentData = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanned_barcode)
        initViews()
    }

    private fun initViews() {
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue)
        surfaceView = findViewById(R.id.surfaceView)
        btnAction = findViewById(R.id.btnAction)
        btnAction?.setOnClickListener(View.OnClickListener {
            if (intentData.isNotEmpty()) {

            }
        })
    }

    private fun initialiseDetectorsAndSources() {
        Toast.makeText(applicationContext, "Barcode scanner started", Toast.LENGTH_SHORT).show()
        barcodeDetector = BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build()
        cameraSource = CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build()
        surfaceView!!.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(this@ScannedBarcodeActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource?.start(surfaceView!!.holder)
                    } else {
                        ActivityCompat.requestPermissions(this@ScannedBarcodeActivity, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource?.stop()
            }
        })
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode?> {
            override fun release() {
                Toast.makeText(applicationContext, "barcode scanner has been stopped", Toast.LENGTH_SHORT).show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode?>) {
                val barcodes: SparseArray<Barcode?> = detections.detectedItems
                if (barcodes.size() != 0) {
                    txtBarcodeValue!!.post {
                            txtBarcodeValue!!.removeCallbacks(null)
                            intentData = barcodes.valueAt(0)!!.rawValue
                            txtBarcodeValue!!.text = intentData
                            btnAction!!.text = "ADD FRIEND"
                    }
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        cameraSource!!.release()
    }

    override fun onResume() {
        super.onResume()
        initialiseDetectorsAndSources()
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 201
    }
}