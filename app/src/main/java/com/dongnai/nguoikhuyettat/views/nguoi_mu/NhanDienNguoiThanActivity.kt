package com.dongnai.nguoikhuyettat.views.nguoi_mu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dongnai.nguoikhuyettat.NguoiMuSDK
import com.dongnai.nguoikhuyettat.R
import com.dongnai.nguoikhuyettat.database.DBContext
import com.dongnai.nguoikhuyettat.models.NguoiThan
import com.dongnai.nguoikhuyettat.utils.Common
import java.util.Locale

class NhanDienNguoiThanActivity : AppCompatActivity(), SurfaceHolder.Callback {
    var yolov8Ncnn: NguoiMuSDK = NguoiMuSDK()
    private var cameraView: SurfaceView? = null
    var textToSpeech: TextToSpeech? = null
    var imgView: ImageView? = null
    var dbContext: DBContext? = null
    var tvName: TextView? = null
    var handler: Handler? = null
    var runnable: Runnable? = null
    private var canPlaySound = true
    var btnDoiCamera: Button? = null
    private var facing = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nhan_dien_nguoi_than)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        init()
        checkPermissions()
        reload()
        onClick()
        handler = Handler()
        runnable = Runnable { canPlaySound = true }
        `object`
    }

    private fun onClick() {
        btnDoiCamera!!.setOnClickListener {
            val new_facing = 1 - facing
            yolov8Ncnn.closeCamera()
            yolov8Ncnn.openCamera(new_facing)
            facing = new_facing
        }
    }

    var nguoiThan: NguoiThan? = null

    private val `object`: Unit
        get() {
            Thread {
                while (true) {
                    nguoiThan = null
                    val stringEmb = yolov8Ncnn.embedding
                    if (!stringEmb.isEmpty()) {
                        nguoiThan = timNguoi(stringEmb)
                        val bitmap = yolov8Ncnn.faceAlign
                        if (bitmap != null) {
                            runOnUiThread { imgView!!.setImageBitmap(bitmap) }
                        }
                        if (nguoiThan != null) {
                            if (canPlaySound) {
                                speakNguoiThan(nguoiThan!!)
                                runOnUiThread { tvName!!.text = nguoiThan!!.ten }

                                canPlaySound = false
                                handler!!.postDelayed(runnable!!, 5000)
                            }
                        }
                    }
                    try {
                        Thread.sleep(100)
                    } catch (e: InterruptedException) {
                        throw RuntimeException(e)
                    }
                }
            }.start()
        }

    private fun init() {
        btnDoiCamera = findViewById(R.id.btnChangeCamera)
        cameraView = findViewById(R.id.cameraview)
        imgView = findViewById(R.id.imgView)
        cameraView.getHolder().addCallback(this)
        dbContext = DBContext(this@NhanDienNguoiThanActivity)
        tvName = findViewById(R.id.tvName)
        textToSpeech = TextToSpeech(
            applicationContext
        ) { i ->
            if (i != TextToSpeech.ERROR) {
                textToSpeech!!.setLanguage(Locale.forLanguageTag("vi-VN"))
            }
        }
        findViewById<View>(R.id.btnThem).setOnClickListener {
            startActivity(
                Intent(applicationContext, ThemNguoiThanActivity::class.java)
            )
        }
    }

    private fun speakNguoiThan(nguoiThan: NguoiThan) {
        textToSpeech!!.speak(nguoiThan.ten, TextToSpeech.QUEUE_FLUSH, null)
    }

    private fun timNguoi(embedding: String): NguoiThan? {
        var result: NguoiThan? = null
        var maxScore = 0.0
        for (nguoiThan in dbContext!!.nguoiThans
        ) {
            val str_target =
                embedding.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val str_source = nguoiThan.embedding.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            if (str_target.size == 512 && str_source.size == 512) {
                val target = DoubleArray(512)
                for (i in 0..511) {
                    target[i] = str_target[i].toDouble()
                }

                val source = DoubleArray(512)
                for (i in 0..511) {
                    source[i] = str_source[i].toDouble()
                }
                val score = Common.cosineSimilarity(target, source)

                if (score > 0.5 && score > maxScore) {
                    maxScore = score
                    result = nguoiThan
                }
            }
        }
        return result
    }

    private fun reload() {
        val ret_init = yolov8Ncnn.loadModel(assets, 0, 1, 0, 0, 0, 0)
        if (!ret_init) {
            Log.e("NhanDienNguoiThanActivity", "yolov8ncnn loadModel failed")
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

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100
            )
        }
    }

    companion object {
        private const val REQUEST_CAMERA = 510
    }
}