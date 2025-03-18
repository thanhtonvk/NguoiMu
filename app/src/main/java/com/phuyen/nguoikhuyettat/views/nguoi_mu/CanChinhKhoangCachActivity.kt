package com.phuyen.nguoikhuyettat.views.nguoi_mu

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.phuyen.nguoikhuyettat.NguoiMuSDK
import com.phuyen.nguoikhuyettat.R
import com.phuyen.nguoikhuyettat.utils.CalDistance
import com.phuyen.nguoikhuyettat.utils.Common


class CanChinhKhoangCachActivity : AppCompatActivity(), SurfaceHolder.Callback {
    var yolov8Ncnn: NguoiMuSDK = NguoiMuSDK()
    private var cameraView: SurfaceView? = null
    var tvKhoangCach: TextView? = null
    var tvKichThuoc: TextView? = null
    private val facing = 1
    var stringList: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_can_chinh_khoang_cach)
        reload()
        init()
        `object`
        onClick()
    }

    private fun onClick() {
        findViewById<View>(R.id.btnCong).setOnClickListener {
            if (!stringList!!.isEmpty()) {
                Toast.makeText(applicationContext, "Update 5", Toast.LENGTH_SHORT).show()
                updateDistance(stringList!![0], 5.0)
            }
        }
        findViewById<View>(R.id.btnTru).setOnClickListener {
            if (!stringList!!.isEmpty()) {
                Toast.makeText(applicationContext, "Update -5", Toast.LENGTH_SHORT).show()
                updateDistance(stringList!![0], -5.0)
            }
        }
        findViewById<View>(R.id.btnSave).setOnClickListener {
            val preferences = PreferenceManager.getDefaultSharedPreferences(
                applicationContext
            )
            val editor = preferences.edit()
            val widthInImages =
                Common.convertArrayToString(CalDistance.widthInImages)

            editor.putString("widthInImages", widthInImages)
            editor.apply()
            finish()
        }
    }

    private val `object`: Unit
        get() {
            Thread {
                while (true) {
                    val temp = yolov8Ncnn.listResult
                    if (!temp.isEmpty()) {
                        stringList = temp
                        showDistance(stringList!![0])
                    }
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        throw RuntimeException(e)
                    }
                }
            }.start()
        }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun showDistance(text: String) {
        val arr = text.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val label = arr[0].toInt()
        val x = arr[1].toDouble()
        val y = arr[2].toDouble()
        val w = arr[3].toDouble()
        val h = arr[4].toDouble()
        val focalLength = CalDistance.calculateFocalLength(
            CalDistance.knownDistances[label], CalDistance.knownWidths[label],
            CalDistance.widthInImages[label]
        )
        val distance = CalDistance.calculateDistance(CalDistance.knownWidths[label], focalLength, w)
        tvKhoangCach!!.text = String.format("Khoảng cách thực %,.2fm", distance)
        tvKichThuoc!!.text =
            String.format("Kích thước vật: %,.2f", CalDistance.widthInImages[label])
    }

    @SuppressLint("SetTextI18n")
    private fun updateDistance(text: String, value: Double) {
        val arr = text.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val label = arr[0].toInt()
        CalDistance.widthInImages[label] += value
    }

    private fun init() {
        cameraView = findViewById(R.id.cameraview)
        cameraView.getHolder().addCallback(this)
        tvKhoangCach = findViewById(R.id.tvKc)
        tvKichThuoc = findViewById(R.id.tvKichthuoc)
    }

    private fun reload() {
        val ret_init = yolov8Ncnn.loadModel(assets, 1, 0, 1, 0, 0, 0)
        if (!ret_init) {
            Log.e("DoDuongActivity", "yolov8ncnn loadModel failed")
        }
    }

    public override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA
            )
        }

        yolov8Ncnn.openCamera(facing)
    }

    public override fun onPause() {
        super.onPause()
        yolov8Ncnn.closeCamera()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        yolov8Ncnn.setOutputWindow(holder.surface)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    companion object {
        private const val REQUEST_CAMERA = 510
    }
}