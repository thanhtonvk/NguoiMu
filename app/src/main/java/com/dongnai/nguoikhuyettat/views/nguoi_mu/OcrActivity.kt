package com.dongnai.nguoikhuyettat.views.nguoi_mu

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.java.GenerativeModelFutures
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.Content.Builder.build
import com.google.ai.client.generativeai.type.Content.Builder.text
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.GenerateContentResponse.text
import com.google.android.gms.tasks.OnSuccessListener
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.dongnai.nguoikhuyettat.NguoiMuSDK
import com.dongnai.nguoikhuyettat.R
import org.commonmark.parser.Parser
import org.commonmark.renderer.text.TextContentRenderer
import java.util.Locale

class OcrActivity : AppCompatActivity(), SurfaceHolder.Callback {
    var yolov8Ncnn: NguoiMuSDK = NguoiMuSDK()
    private var cameraView: SurfaceView? = null
    var textToSpeech: TextToSpeech? = null

    var btnDoiCamera: Button? = null
    private var facing = 1
    var recognizer: TextRecognizer? = null
    private var isSpeaking = false // Trạng thái đọc nội dung
    var gm: GenerativeModel? = null
    var model: GenerativeModelFutures? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocr)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        init()
        checkPermissions()
        reload()
        onClick()
        initModel()
        generateText("Xin chào các bạn")
    }

    private fun initModel() {
        gm = GenerativeModel( /* modelName */"gemini-1.5-flash",
            resources.getString(R.string.api_key)
        )
        model = GenerativeModelFutures.from(gm!!)
    }

    private fun generateText(value: String) {
        val words = splitParagraph(value, 10)
        for (word in words
        ) {
            val content: Content = Builder()
                .text(word)
                .build()
            val response = model!!.generateContent(content)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Futures.addCallback(response, object : FutureCallback<GenerateContentResponse> {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onSuccess(result: GenerateContentResponse) {
                        val resultText = result.text
                        val parser = Parser.builder().build()
                        val document = parser.parse(resultText)
                        val renderer = TextContentRenderer.builder().build()
                        val plainText = renderer.render(document)

                        Log.d("OCR", word)
                        Log.d("EDIT", plainText)
                    }

                    override fun onFailure(t: Throwable) {
                        t.printStackTrace()
                    }
                }, this.mainExecutor)
            }
        }
    }

    private fun init() {
        recognizer =
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        btnDoiCamera = findViewById(R.id.btnChangeCamera)
        cameraView = findViewById(R.id.cameraview)
        cameraView.getHolder().addCallback(this)
        textToSpeech = TextToSpeech(
            applicationContext
        ) { i ->
            if (i != TextToSpeech.ERROR) {
                textToSpeech!!.setLanguage(Locale.forLanguageTag("vi-VN"))
            }
        }
    }

    private fun handleTouch() {
        if (isSpeaking) {
            textToSpeech!!.stop()
            isSpeaking = false
            return
        }
        val screenshot = yolov8Ncnn.image
        recognizeTextFromImage(screenshot)
    }

    private fun onClick() {
        btnDoiCamera!!.setOnClickListener {
            val new_facing = 1 - facing
            yolov8Ncnn.closeCamera()
            yolov8Ncnn.openCamera(new_facing)
            facing = new_facing
        }
    }

    override fun onDestroy() {
        if (textToSpeech != null) {
            textToSpeech!!.stop()
            textToSpeech!!.shutdown()
        }
        super.onDestroy()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> handleTouch()
        }
        return super.onTouchEvent(event)
    }

    private fun readContent(content: String) {
        isSpeaking = true
        textToSpeech!!.speak(content, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    var stringBuilder: StringBuilder? = null

    fun recognizeTextFromImage(bitmap: Bitmap) {
        stringBuilder = StringBuilder()

        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer!!.process(image)
            .addOnSuccessListener(OnSuccessListener { visionText ->
                val blocks = visionText.textBlocks
                if (blocks.size == 0) {
                    readContent("không thấy nội dung")
                    return@OnSuccessListener
                } else {
                    for (block in blocks) {
                        val txt = block.text
                        stringBuilder!!.append(txt)
                    }
                    generateText("Sửa lỗi chính tả: " + stringBuilder.toString())
                    readContent(stringBuilder.toString())
                }
            })
            .addOnFailureListener { readContent("có lỗi xảy ra, vui lòng thử lại") }
    }

    private fun reload() {
        val ret_init = yolov8Ncnn.loadModel(assets, 0, 0, 0, 0, 0, 0)
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
        fun splitParagraph(paragraph: String, maxWords: Int): List<String> {
            val result: MutableList<String> = ArrayList()

            // Tách đoạn văn thành các câu cơ bản dựa vào dấu câu
            val sentences =
                paragraph.split("(?<=[.!?])\\s+".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()

            for (sentence in sentences) {
                val words =
                    sentence.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var chunk = StringBuilder()
                var wordCount = 0

                for (word in words) {
                    if (wordCount < maxWords) {
                        chunk.append(word).append(" ")
                        wordCount++
                    } else {
                        result.add(chunk.toString().trim { it <= ' ' })
                        chunk = StringBuilder(word).append(" ")
                        wordCount = 1
                    }
                }

                if (chunk.length > 0) {
                    result.add(chunk.toString().trim { it <= ' ' })
                }
            }

            return result
        }
    }
}