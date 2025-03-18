package com.phuyen.nguoikhuyettat.views.cam_diec

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.phuyen.nguoikhuyettat.Common
import com.phuyen.nguoikhuyettat.NguoiMuSDK
import com.phuyen.nguoikhuyettat.databinding.ActivityGhepTuBinding
import java.util.Locale

class GhepTuActivity : AppCompatActivity(), SurfaceHolder.Callback {
    var binding: ActivityGhepTuBinding? = null
    var nguoiMuSDK: NguoiMuSDK = NguoiMuSDK()
    private var facing = 0
    private var canPlaySound = true
    var handler: Handler? = null
    var runnable: Runnable? = null
    var mediaPlayer: MediaPlayer? = null
    var speak: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGhepTuBinding.inflate(layoutInflater)
        setContentView(binding!!.root)


        init()
        onClick()
        reload()
        handler = Handler()
        runnable = Runnable {
            canPlaySound = true
            finalString = ""
            binding!!.tvNoiDung.text = ""
        }
        `object`
    }

    var finalString: String = ""
    private val mainHandler = Handler(Looper.getMainLooper())
    private val backgroundHandler = Handler(Looper.getMainLooper())
    private var isRunning = true
    private var isSpeaking = false // Đánh dấu đang đọc
    var clearFinalStringRunnable: Runnable = Runnable {
        finalString = ""
        binding!!.tvNoiDung.text = ""
        canPlaySound = true // Cho phép đọc tiếp
        isSpeaking = false
    }

    private val `object`: Unit
        get() {
            Thread {
                while (isRunning) {
                    try {
                        // Nếu đang đọc thì không nhận thêm cử chỉ mới
                        if (isSpeaking) {
                            Thread.sleep(1000)
                            continue
                        }

                        val dataDeaf = nguoiMuSDK.chuCai

                        if (dataDeaf.isEmpty()) {
                            Thread.sleep(10)
                            continue
                        }

                        val deaf = getDeaf(dataDeaf)
                        val cuChi = getSource("", deaf)
                        if (cuChi.isEmpty()) {
                            Thread.sleep(10)
                            continue
                        }
                        mainHandler.post { addToFinalString(cuChi) }
                        // Xóa sau 10 giây nếu không có hành động mới
                        backgroundHandler.removeCallbacks(clearFinalStringRunnable)
                        backgroundHandler.postDelayed(clearFinalStringRunnable, 10000)

                        Thread.sleep(10)
                    } catch (e: InterruptedException) {
                        isRunning = false
                    }
                }
            }.start()
        }

    private fun addToFinalString(cuChi: String) {
        finalString += cuChi
        binding!!.tvNoiDung.text = finalString
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                speak!!.speak(finalString, TextToSpeech.QUEUE_FLUSH, null, null)
                try {
                    Thread.sleep(1000)
                    binding!!.tvNoiDung.text = ""
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }
        }
        return super.onTouchEvent(event)
    }


    private fun getSource(emotion: String, deaf: Int): String {
        return Common.classNames[deaf]
    }

    private fun onClick() {
        binding!!.tvNoiDung.setOnClickListener {
            finalString = ""
            binding!!.tvNoiDung.text = ""
        }
        binding!!.btnChangeCamera.setOnClickListener {
            val new_facing = 1 - facing
            nguoiMuSDK.closeCamera()
            nguoiMuSDK.openCamera(new_facing)
            facing = new_facing
        }
    }

    private fun getDeaf(scoreDeaf: String): Int {
        val arr = scoreDeaf.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return arr[0].toInt()
    }

    private fun getEmotion(scoreEmotion: String): String {
        val arr = scoreEmotion.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var maxScore = 0f
        var maxIdx = 0
        for (i in arr.indices) {
            val score = arr[i].toFloat()
            if (score > maxScore) {
                maxScore = score
                maxIdx = i
            }
        }
        return Common.emotionClasses[maxIdx]
    }


    private fun init() {
        if (speak != null) {
            speak!!.stop() // Dừng đọc nếu đang nói
            speak!!.shutdown() // Giải phóng tài nguyên cũ
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding!!.cameraview.holder.setFormat(PixelFormat.RGBA_8888)
        binding!!.cameraview.holder.addCallback(this)

        Common.classNames = arrayOf(
            "a",
            "ă",
            "â",
            "b",
            "c",
            "d",
            "đ",
            "e",
            "ê",
            "g",
            "h",
            "i",
            "k",
            "l",
            "m",
            "n",
            "o",
            "ô",
            "ơ",
            "p",
            "q",
            "r",
            "s",
            "t",
            "u",
            "ú",
            "v",
            "x",
            "y",
            "dấu chấm",
            "dấu hỏi",
            "dấu huyền",
            "dấu ngã",
            "dấu sắc"
        )
        if (speak != null) {
            speak!!.stop() // Dừng đọc nếu đang nói
            speak!!.shutdown() // Giải phóng tài nguyên cũ
        }
        speak = TextToSpeech(applicationContext) { i ->
            if (i != TextToSpeech.ERROR) {
                speak!!.setLanguage(Locale.forLanguageTag("vi-VN"))
            }
        }
    }

    private fun reload() {
        val ret_init = nguoiMuSDK.loadModel(assets, 0, 0, 0, 0, 0, 1)
        if (!ret_init) {
            Log.e("NhanDienNguoiThanActivity", "yolov8ncnn loadModel failed")
        } else {
            Log.e("NhanDienNguoiThanActivity", "yolov8ncnn loadModel ok")
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        nguoiMuSDK.setOutputWindow(holder.surface)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    override fun onResume() {
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
        nguoiMuSDK.openCamera(facing)
    }

    override fun onPause() {
        super.onPause()
        if (speak != null) {
            speak!!.stop() // Dừng đọc nếu đang nói
            speak!!.shutdown() // Giải phóng tài nguyên cũ
        }
        nguoiMuSDK.closeCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        nguoiMuSDK.closeCamera()
        if (speak != null) {
            speak!!.stop() // Dừng đọc nếu đang nói
            speak!!.shutdown() // Giải phóng tài nguyên cũ
        }
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    companion object {
        private const val REQUEST_CAMERA = 1345
    }
}