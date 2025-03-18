package com.dongnai.nguoikhuyettat.views.nguoi_mu

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dongnai.nguoikhuyettat.Common
import com.dongnai.nguoikhuyettat.R
import java.util.Locale

class ReviewActivity : AppCompatActivity() {
    var tvCauHoi: TextView? = null
    var tvDa1: TextView? = null
    var tvDa2: TextView? = null
    var tvDa3: TextView? = null
    var tvDa4: TextView? = null
    var btnMic: ImageView? = null
    var btnReplay: ImageView? = null
    var btnPrev: ImageView? = null
    var btnNext: ImageView? = null
    private var textToSpeech: TextToSpeech? = null
    var idxReview: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)
        init()
        initTextToSpeech()
        onClick()
        nextQuestion()
    }

    private fun initTextToSpeech() {
        if (textToSpeech != null) {
            textToSpeech!!.stop() // Dừng đọc nếu đang nói
            textToSpeech!!.shutdown() // Giải phóng tài nguyên cũ
        }
        textToSpeech = TextToSpeech(this) { status: Int ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech!!.setLanguage(Locale("vi", "VN"))
            }
        }
    }

    private val currentQuestionContent: String
        get() = (("Câu hỏi số " + (idxReview + 1) + ", " + Common.CAU_HOI.getCauhoi()
                ).toString() + ". Đáp án một, " + Common.CAU_HOI.a
                + ". Đáp án hai, " + Common.CAU_HOI.b
                + ". Đáp án ba, " + Common.CAU_HOI.c
                + ". Đáp án bốn, " + Common.CAU_HOI.d
                + ". Bạn chọn đáp án " + Common.dapanChon[idxReview]
                + ". Đáp án đúng, " + Common.CAU_HOI.dapan
                + ". Giải thích, " + Common.CAU_HOI.giaithich)

    private fun updateUIForQuestion() {
        tvCauHoi?.setText(Common.CAU_HOI.getCauhoi())
        tvDa1!!.text = Common.CAU_HOI.a
        tvDa2!!.text = Common.CAU_HOI.b
        tvDa3!!.text = Common.CAU_HOI.c
        tvDa4!!.text = Common.CAU_HOI.d
        speak(currentQuestionContent)
    }

    private fun onClick() {
        btnNext!!.setOnClickListener { view: View? ->
            nextQuestion()
        }
        btnPrev!!.setOnClickListener { view: View? ->
            prevQuestion()
        }
        btnReplay!!.setOnClickListener { view: View? ->
            repeatQuestion()
        }
        btnMic!!.setOnClickListener { view: View? ->
            startVoiceRecognition()
        }
        findViewById<View>(R.id.btn_trove).setOnClickListener { view: View? ->
            finish()
        }
    }

    private fun repeatQuestion() {
        val noiDung = currentQuestionContent
        speak(noiDung)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MIC && resultCode == RESULT_OK && data != null) {
            val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (results != null && !results.isEmpty()) {
                val spokenText = results[0].lowercase()
                if (spokenText.contains("đọc")) {
                    repeatQuestion()
                } else if (spokenText.contains("sau") || spokenText.contains("tiếp")) {
                    nextQuestion()
                } else if (spokenText.contains("trước")) {
                    prevQuestion()
                }
            }
        }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy nói...")
        try {
            startActivityForResult(intent, REQUEST_MIC)
        } catch (e: Exception) {
            Toast.makeText(this, "Thiết bị không hỗ trợ tính năng này", Toast.LENGTH_SHORT).show()
        }
    }

    private fun prevQuestion() {
        if (--idxReview < 0) {
            idxReview += 1
            return
        } else {
            Common.CAU_HOI = Common.cauHoiDaTraLoi[idxReview]
            updateUIForQuestion()
        }
    }

    private fun nextQuestion() {
        if (++idxReview >= Common.cauHoiArrayList.size) {
            idxReview -= 1
        } else {
            Common.CAU_HOI = Common.cauHoiDaTraLoi[idxReview]
            updateUIForQuestion()
        }
    }

    private fun speak(text: String) {
        if (textToSpeech != null) {
            textToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun init() {
        tvCauHoi = findViewById(R.id.tv_cauhoi)
        tvDa1 = findViewById(R.id.btn_da1)
        tvDa2 = findViewById(R.id.btn_da2)
        tvDa3 = findViewById(R.id.btn_da3)
        tvDa4 = findViewById(R.id.btn_da4)
        btnMic = findViewById(R.id.btnMic)
        btnReplay = findViewById(R.id.btnReplay)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
    }

    companion object {
        const val REQUEST_MIC = 123
    }
}