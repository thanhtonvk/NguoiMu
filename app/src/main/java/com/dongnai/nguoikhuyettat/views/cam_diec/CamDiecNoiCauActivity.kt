package com.dongnai.nguoikhuyettat.views.cam_diec

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.SurfaceHolder
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dongnai.nguoikhuyettat.Common
import com.dongnai.nguoikhuyettat.NguoiMuSDK
import com.tondz.nguoimu.databinding.ActivityCamDiecNoiCauBinding
import java.util.Locale

class CamDiecNoiCauActivity : AppCompatActivity(), SurfaceHolder.Callback {
    var binding: ActivityCamDiecNoiCauBinding? = null
    var nguoiMuSDK: NguoiMuSDK = NguoiMuSDK()
    private var facing = 0
    private var canPlaySound = true
    var handler: Handler? = null
    var runnable: Runnable? = null
    var mediaPlayer: MediaPlayer? = null
    var speak: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCamDiecNoiCauBinding.inflate(layoutInflater)
        setContentView(binding!!.root)


        init()
        onClick()
        reload()
        handler = Handler()
        runnable = Runnable {
            canPlaySound = true
            noiCauTemp1.clear()
            noiCauTemp2.clear()
            finalString.clear()
            binding!!.tvNoiDung.text = ""
        }
        `object`
    }

    var noiCauTemp1: MutableList<List<Int>> = ArrayList()
    var noiCauTemp2: MutableList<List<Int>> = ArrayList()
    var finalString: MutableList<String> = ArrayList()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val backgroundHandler = Handler(Looper.getMainLooper())
    private var isRunning = true
    private var isSpeaking = false // Đánh dấu đang đọc
    var clearFinalStringRunnable: Runnable = Runnable {
        finalString.clear()
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
                            Thread.sleep(100)
                            continue
                        }

                        val dataDeaf = nguoiMuSDK.deaf
                        if (dataDeaf != null) {
                            if (dataDeaf.isEmpty()) {
                                Thread.sleep(10)
                                continue
                            }
                        }

                        val deaf = dataDeaf?.let { getDeaf(it) }
                        val cuChi = deaf?.let { getSource("", it) }
                        if (cuChi != null) {
                            if (cuChi.isEmpty()) {
                                Thread.sleep(10)
                                continue
                            }
                        }

                        if (!finalString.contains(cuChi)) {
                            mainHandler.post {
                                if (cuChi != null) {
                                    addToFinalString(cuChi)
                                }
                            }
                        }

                        // Xóa sau 10 giây nếu không có hành động mới
                        backgroundHandler.removeCallbacks(clearFinalStringRunnable)
                        backgroundHandler.postDelayed(clearFinalStringRunnable, 5000)

                        Thread.sleep(10)
                    } catch (e: InterruptedException) {
                        isRunning = false
                    }
                }
            }.start()
        }

    private fun addToFinalString(cuChi: String) {
        finalString.add(cuChi)
        binding!!.tvNoiDung.text = java.lang.String.join(" ", finalString)

        // Khi đủ 3 câu, tạm dừng nhận cử chỉ 1 giây rồi đọc
        if (finalString.size == 3 && canPlaySound) {
            isSpeaking = true
            canPlaySound = false

            mainHandler.postDelayed({
                speak!!.speak(
                    java.lang.String.join(" ", finalString),
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
            }, 1000) // Đợi 1 giây trước khi đọc
            speak!!.setOnUtteranceCompletedListener { s: String? ->
                mainHandler.post(clearFinalStringRunnable)
            }
        }
    }


    private fun getSource(emotion: String, deaf: Int): String {
        var source = ""
        if (deaf < 33) {
            source = Common.classNames[deaf]
        }

        return source
    }

    private fun onClick() {
        if (speak != null) {
            speak!!.stop() // Dừng đọc nếu đang nói
            speak!!.shutdown() // Giải phóng tài nguyên cũ
        }
        binding!!.tvNoiDung.setOnClickListener {
            noiCauTemp1.clear()
            noiCauTemp2.clear()
            finalString.clear()
            binding!!.tvNoiDung.text = ""
        }
        binding!!.btnChangeCamera.setOnClickListener {
            val new_facing = 1 - facing
            nguoiMuSDK.closeCamera()
            nguoiMuSDK.openCamera(new_facing)
            facing = new_facing
        }
        binding!!.btnVi.setOnClickListener {
            Common.classNames = arrayOf(
                "anh trai",
                "biết",
                "cảm ơn",
                "chăm sóc",
                "chị gái",
                "con người",
                "công cộng",
                "công việc",
                "giúp đỡ",
                "giường",
                "giống nhau",
                "hẹn gặp lại",
                "hiểu",
                "hợp tác",
                "khám bệnh",
                "khát nước",
                "khen",
                "khỏe",
                "không thích",
                "lắng nghe",
                "lễ phép",
                "mẹ",
                "năn nỉ",
                "nhà",
                "nhớ",
                "rất vui được gặp bạn",
                "sợ",
                "tạm biệt",
                "thích",
                "tò mò",
                "xin chào",
                "xin lỗi",
                "yêu",
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
            speak = TextToSpeech(
                applicationContext
            ) { i ->
                if (i != TextToSpeech.ERROR) {
                    speak!!.setLanguage(Locale.forLanguageTag("vi-VN"))
                }
            }
        }
        binding!!.btnEn.setOnClickListener {
            Common.classNames = arrayOf(
                "brother",
                "know",
                "thank you",
                "take care",
                "sister",
                "human",
                "public",
                "job",
                "help",
                "bed",
                "similar",
                "see you again",
                "understand",
                "cooperate",
                "medical checkup",
                "thirsty",
                "praise",
                "healthy",
                "dislike",
                "listen",
                "polite",
                "mother",
                "beg",
                "house",
                "miss",
                "nice to meet you",
                "afraid",
                "goodbye",
                "like",
                "curious",
                "hello",
                "sorry",
                "love",
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
                "period",
                "question mark",
                "grave accent",
                "tilde",
                "acute accent"
            )
            if (speak != null) {
                speak!!.stop() // Dừng đọc nếu đang nói
                speak!!.shutdown() // Giải phóng tài nguyên cũ
            }
            speak = TextToSpeech(
                applicationContext
            ) { i ->
                if (i != TextToSpeech.ERROR) {
                    speak!!.setLanguage(Locale.ENGLISH)
                }
            }
        }
        binding!!.btnCn.setOnClickListener {
            Common.classNames = arrayOf(
                "哥哥",  // anh trai
                "知道",  // biết
                "谢谢",  // cảm ơn
                "照顾",  // chăm sóc
                "姐姐",  // chị gái
                "人类",  // con người
                "公共",  // công cộng
                "工作",  // công việc
                "帮助",  // giúp đỡ
                "床",  // giường
                "相似",  // giống nhau
                "再见",  // hẹn gặp lại
                "理解",  // hiểu
                "合作",  // hợp tác
                "看病",  // khám bệnh
                "口渴",  // khát nước
                "称赞",  // khen
                "健康",  // khỏe
                "不喜欢",  // không thích
                "倾听",  // lắng nghe
                "礼貌",  // lễ phép
                "妈妈",  // mẹ
                "恳求",  // năn nỉ
                "家",  // nhà
                "想念",  // nhớ
                "很高兴认识你",  // rất vui được gặp bạn
                "害怕",  // sợ
                "再见",  // tạm biệt
                "喜欢",  // thích
                "好奇",  // tò mò
                "你好",  // xin chào
                "对不起",  // xin lỗi
                "爱",  // yêu
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
                "句号",  // dấu chấm
                "问号",  // dấu hỏi
                "抑音符",  // dấu huyền
                "波浪号",  // dấu ngã
                "重音符" // dấu sắc
            )
            if (speak != null) {
                speak!!.stop() // Dừng đọc nếu đang nói
                speak!!.shutdown() // Giải phóng tài nguyên cũ
            }
            speak = TextToSpeech(
                applicationContext
            ) { i ->
                if (i != TextToSpeech.ERROR) {
                    speak!!.setLanguage(Locale.CHINA)
                }
            }
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
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding!!.cameraview.holder.setFormat(PixelFormat.RGBA_8888)
        binding!!.cameraview.holder.addCallback(this)

        Common.classNames = arrayOf(
            "anh trai",
            "biết",
            "cảm ơn",
            "chăm sóc",
            "chị gái",
            "con người",
            "công cộng",
            "công việc",
            "giúp đỡ",
            "giường",
            "giống nhau",
            "hẹn gặp lại",
            "hiểu",
            "hợp tác",
            "khám bệnh",
            "khát nước",
            "khen",
            "khỏe",
            "không thích",
            "lắng nghe",
            "lễ phép",
            "mẹ",
            "năn nỉ",
            "nhà",
            "nhớ",
            "rất vui được gặp bạn",
            "sợ",
            "tạm biệt",
            "thích",
            "tò mò",
            "xin chào",
            "xin lỗi",
            "yêu",
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
        val ret_init = nguoiMuSDK.loadModel(assets, 0, 0, 0, 1, 0, 0)
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
        if (speak != null) {
            speak!!.stop() // Dừng đọc nếu đang nói
            speak!!.shutdown() // Giải phóng tài nguyên cũ
        }
        nguoiMuSDK.closeCamera()
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    companion object {
        private const val REQUEST_CAMERA = 1345
    }
}