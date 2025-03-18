package com.dongnai.nguoikhuyettat.views.nguoi_mu

import android.Manifest
import android.annotation.SuppressLint
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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dongnai.nguoikhuyettat.NguoiMuSDK
import com.dongnai.nguoikhuyettat.R
import com.dongnai.nguoikhuyettat.database.DBContext
import com.dongnai.nguoikhuyettat.utils.CalDistance
import com.dongnai.nguoikhuyettat.utils.Common
import java.util.Locale

class DoDuongActivity : AppCompatActivity(), SurfaceHolder.Callback {
    var yolov8Ncnn: NguoiMuSDK = NguoiMuSDK()
    private var cameraView: SurfaceView? = null
    var textToSpeech: TextToSpeech? = null
    var dbContext: DBContext? = null
    var handler: Handler? = null
    var runnable: Runnable? = null
    private var canPlaySound = true
    private var facing = 1
    var tvKhoangCach: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_do_duong)
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
        findViewById<View>(R.id.btnChangeCamera).setOnClickListener {
            val new_facing = 1 - facing
            yolov8Ncnn.closeCamera()
            yolov8Ncnn.openCamera(new_facing)
            facing = new_facing
        }
        findViewById<View>(R.id.btnCanChinh).setOnClickListener {
            startActivity(
                Intent(
                    applicationContext,
                    CanChinhKhoangCachActivity::class.java
                )
            )
            finish()
        }
    }

    private val `object`: Unit
        get() {
            Thread {
                while (true) {
                    val stringList =
                        yolov8Ncnn.listResult
                    if (!stringList.isEmpty()) {
                        if (canPlaySound) {
                            countObject(stringList)
                            for (result in stringList
                            ) {
                                speakObject(result)
                            }
                            canPlaySound = false
                            handler!!.postDelayed(runnable!!, 5000)
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
        cameraView = findViewById(R.id.cameraview)


        cameraView.getHolder().addCallback(this)
        tvKhoangCach = findViewById(R.id.tvKc)

        dbContext = DBContext(this@DoDuongActivity)
        textToSpeech = TextToSpeech(
            applicationContext
        ) { i ->
            if (i != TextToSpeech.ERROR) {
                textToSpeech!!.setLanguage(Locale.forLanguageTag("vi-VN"))
            }
        }
    }

    private fun countObject(stringObject: List<String>) {
        val labels: MutableList<String> = ArrayList()
        for (result in stringObject
        ) {
            val arr = result.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            labels.add(Common.listObject[arr[0].toInt()])
        }
        val elementCountMap = HashMap<String, Int>()

        // Duyệt qua mảng và đếm số lần xuất hiện của từng phần tử
        for (str in labels) {
            if (elementCountMap.containsKey(str)) {
                elementCountMap[str] = elementCountMap[str]!! + 1
            } else {
                elementCountMap[str] = 1
            }
        }
        for (key in elementCountMap.keys) {
            val num_of_value = elementCountMap[key]!!
            if (num_of_value > 1) {
                val speak = "Có $num_of_value $key"
                textToSpeech!!.speak(speak, TextToSpeech.QUEUE_FLUSH, null)
                try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }
        }
    }


    @SuppressLint("DefaultLocale")
    private fun speakObject(text: String) {
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
        runOnUiThread { tvKhoangCach!!.text = String.format("Khoảng cách %,.2fm", distance) }

        val name = Common.listObject[label]
        val position = Common.xywhToCenter(x, y, w, h)
        val centerX = position[0]
        val centerY = position[1]
        var speaking = ""
        //top
        if (100 < centerX && centerX < 200 && 0 < centerY && centerY < 200) {
            speaking = "$name đang ở trên"
        }
        //right
        if (200 < centerX && centerX < 320 && 200 < centerY && centerY < 400) {
            speaking = "$name đang ở bên phải"
        }
        //bottom
        if (100 < centerX && centerX < 200 && 400 < centerY && centerY < 640) {
            speaking = "$name đang ở dưới"
        }
        //left
        if (0 < centerX && centerX < 100 && 200 < centerY && centerY < 400) {
            speaking = "$name đang ở bên trái"
        }
        //top right
        if (200 < centerX && centerX < 320 && 0 < centerY && centerY < 200) {
            speaking = "$name đang ở trên bên phải"
        }
        // bottom right
        if (200 < centerX && centerX < 320 && 400 < centerY && centerY < 640) {
            speaking = "$name đang ở dưới bên phải"
        }
        //bottom left
        if (0 < centerX && centerX < 100 && 400 < centerY && centerY < 640) {
            speaking = "$name đang ở dưới bên trái"
        }
        //top left
        if (0 < centerX && centerX < 100 && 0 < centerY && centerY < 200) {
            speaking = "$name đang ở trên bên trái"
        }

        if (100 < centerX && centerX < 200 && 200 < centerY && centerY < 400) {
            speaking = "$name đang ở giữa "
        }
        @SuppressLint("DefaultLocale") val valDistance = String.format("%.2f", distance)
        speaking += "$valDistance met"
        if (label == 9) {
            val lightTraffic = yolov8Ncnn.lightTraffic
            if (!lightTraffic.trim { it <= ' ' }.isEmpty()) {
                val resultLight = predictLight(lightTraffic)
                val content = "đèn giao thông đang $resultLight"
                textToSpeech!!.speak(content, TextToSpeech.QUEUE_FLUSH, null)
                try {
                    Thread.sleep(3000)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }
        }

        textToSpeech!!.speak(speaking, TextToSpeech.QUEUE_FLUSH, null)


        try {
            Thread.sleep(3000)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    private fun predictLight(result: String): String {
        val arr = result.trim { it <= ' ' }.split(",".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        var maxIdx = -1
        var maxScore = 0.0
        for (i in arr.indices) {
            val score = arr[i].toDouble()
            if (score > maxScore) {
                maxScore = score
                maxIdx = i
            }
        }
        return Common.lightTraffic[maxIdx]
    }

    private fun reload() {
        val ret_init = yolov8Ncnn.loadModel(assets, 1, 0, 1, 0, 0, 0)
        if (!ret_init) {
            Log.e("DoDuongActivity", "yolov8ncnn loadModel failed")
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
