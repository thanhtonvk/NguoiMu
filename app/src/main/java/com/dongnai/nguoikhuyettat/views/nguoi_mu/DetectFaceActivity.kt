package com.dongnai.nguoikhuyettat.views.nguoi_mu

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dongnai.nguoikhuyettat.NguoiMuSDK
import com.dongnai.nguoikhuyettat.R

class DetectFaceActivity : AppCompatActivity(), SurfaceHolder.Callback {
    var yolov8Ncnn: NguoiMuSDK = NguoiMuSDK()
    private var cameraView: SurfaceView? = null
    private var facing = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detect_face)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        init()
        reload()
        click()
    }


    private fun click() {
        findViewById<View>(R.id.btnCapture).setOnClickListener {
            val stringEmb = yolov8Ncnn.embedding
            if (!stringEmb.isEmpty()) {
                ThemNguoiThanActivity.Companion.embedding = stringEmb
                ThemNguoiThanActivity.Companion.bitmap = yolov8Ncnn.faceAlign
                ThemNguoiThanActivity.Companion.imgAvatar!!.setImageBitmap(ThemNguoiThanActivity.Companion.bitmap)
                onBackPressed()
            }
        }
        findViewById<View>(R.id.btnChangeCamera).setOnClickListener {
            val new_facing = 1 - facing
            yolov8Ncnn.closeCamera()
            yolov8Ncnn.openCamera(new_facing)
            facing = new_facing
        }
    }

    private fun init() {
        cameraView = findViewById(R.id.cameraview)

        cameraView.getHolder().addCallback(this)
    }


    private fun reload() {
        val ret_init = yolov8Ncnn.loadModel(assets, 0, 1, 0, 0, 0, 0)
        if (!ret_init) {
            Log.e("MainActivity", "yolov8ncnn loadModel failed")
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        yolov8Ncnn.setOutputWindow(holder.surface)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
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

    companion object {
        private const val REQUEST_CAMERA = 510
    }
}