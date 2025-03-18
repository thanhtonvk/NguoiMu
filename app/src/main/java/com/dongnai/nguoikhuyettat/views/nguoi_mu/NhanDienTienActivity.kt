package com.dongnai.nguoikhuyettat.views.nguoi_mu

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dongnai.nguoikhuyettat.NguoiMuSDK
import com.dongnai.nguoikhuyettat.R
import com.dongnai.nguoikhuyettat.database.DBContext
import com.dongnai.nguoikhuyettat.utils.Common
import java.util.Locale

class NhanDienTienActivity : AppCompatActivity(), SurfaceHolder.Callback {
    var yolov8Ncnn: NguoiMuSDK = NguoiMuSDK()
    private var cameraView: SurfaceView? = null
    var textToSpeech: TextToSpeech? = null
    var dbContext: DBContext? = null
    var handler: Handler? = null
    var runnable: Runnable? = null
    private var canPlaySound = true
    var btnDoiCamera: Button? = null
    private var facing = 1
    var tvKhoangCach: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nhan_dien_tien)
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

    private val `object`: Unit
        get() {
            Thread {
                while (true) {
                    val moneyList =
                        yolov8Ncnn.listMoneyResult
                    if (!moneyList.isEmpty()) {
                        if (canPlaySound) {
                            for (money in moneyList
                            ) {
                                speakMoney(money)
                                break
                            }
                            canPlaySound = false
                            handler!!.postDelayed(runnable!!, 3000)
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


        cameraView.getHolder().addCallback(this)
        tvKhoangCach = findViewById(R.id.tvKc)

        dbContext = DBContext(this@NhanDienTienActivity)
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

    private fun speakMoney(result: String) {
        Log.e("TAGTOND", "speakMoney: $result")
        val arr = result.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val label = arr[0].toInt()
        val prob = arr[5].toDouble()
        if (prob > 0.9) {
            val money = Common.moneys[label]
            if (!textToSpeech!!.isSpeaking) {
                textToSpeech!!.speak(money, TextToSpeech.QUEUE_FLUSH, null)
            }
        }
    }

    private fun reload() {
        val ret_init = yolov8Ncnn.loadModel(assets, 0, 0, 0, 0, 1, 0)
        if (!ret_init) {
            Log.e("NhanDienTienActivity", "yolov8ncnn loadModel failed")
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
