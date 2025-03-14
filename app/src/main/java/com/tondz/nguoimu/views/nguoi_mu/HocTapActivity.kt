package com.tondz.nguoimu.views.nguoi_mu

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tondz.nguoimu.Common
import com.tondz.nguoimu.R
import java.util.Collections
import java.util.Locale
import java.util.Random
import java.util.UUID


class HocTapActivity : AppCompatActivity() {
    private var tv_CauHoi: TextView? = null
    private var btn_da1: Button? = null
    private var btn_da2: Button? = null
    private var btn_da3: Button? = null
    private var btn_da4: Button? = null
    private var btn_trove: Button? = null
    private var random: Random? = null
    private var idxHocTap = -1
    private var dung = 0
    private var sai = 0
    private var entry = 0
    private var textToSpeech: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hoc_tap)

        initViews()
        initTextToSpeech()
        shuffleQuestions()
        setupClickListeners()
        nextQuestion()
    }

    private fun initViews() {
        tv_CauHoi = findViewById(R.id.tv_cauhoi)
        btn_da1 = findViewById(R.id.btn_da1)
        btn_da2 = findViewById(R.id.btn_da2)
        btn_da3 = findViewById(R.id.btn_da3)
        btn_da4 = findViewById(R.id.btn_da4)
        btn_trove = findViewById(R.id.btn_trove)
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

    private fun shuffleQuestions() {
        random = Random()
        Collections.shuffle(Common.cauHoiArrayList)
    }

    private fun setupClickListeners() {
        btn_da1!!.setOnClickListener { view: View? ->
            handleAnswer(
                btn_da1!!.text.toString()
            )
        }
        btn_da2!!.setOnClickListener { view: View? ->
            handleAnswer(
                btn_da2!!.text.toString()
            )
        }
        btn_da3!!.setOnClickListener { view: View? ->
            handleAnswer(
                btn_da3!!.text.toString()
            )
        }
        btn_da4!!.setOnClickListener { view: View? ->
            handleAnswer(
                btn_da4!!.text.toString()
            )
        }
        btn_trove!!.setOnClickListener { view: View? -> finish() }
        findViewById<View>(R.id.btnReplay).setOnClickListener { view: View? -> repeatQuestion() }
        findViewById<View>(R.id.btnMic).setOnClickListener { view: View? -> startVoiceRecognition() }
    }

    private fun repeatQuestion() {
        val noiDung = currentQuestionContent
        speak(noiDung)
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

    private fun handleAnswer(selectedAnswer: String) {
        if (Common.CAU_HOI != null && checkAnswer(selectedAnswer)) {
            dung++
            speak("Chúc mừng bạn đã trả lời đúng")
            delayAndProceed({ this.nextQuestion() }, 3000)
        } else {
            entry++
            if (entry == 1) {
                speak("Câu trả lời chưa chính xác, bạn hãy chọn lại.")
            } else if (entry == 2) {
                sai++
                val noiDung =
                    "Câu trả lời chưa chính xác. Đáp án đúng là " + Common.CAU_HOI.dapan + ". giải thích. " + Common.CAU_HOI.giaithich
                entry = 0
                val utteranceId = UUID.randomUUID().toString()
                textToSpeech!!.speak(noiDung, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

                textToSpeech!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String) {
                    }

                    override fun onDone(utteranceId: String) {
                        runOnUiThread { nextQuestion() }
                    }

                    override fun onError(utteranceId: String) {
                        runOnUiThread {
                            Toast.makeText(
                                this@HocTapActivity,
                                "Lỗi khi phát âm",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
            }
        }
    }

    private fun checkAnswer(selectedAnswer: String): Boolean {
        return selectedAnswer == Common.CAU_HOI.dapan
    }

    private fun nextQuestion() {
        if (++idxHocTap >= Common.cauHoiArrayList.size) {
            showResultDialog()
        } else {
            Common.CAU_HOI = Common.cauHoiArrayList[idxHocTap]
            updateUIForQuestion()
        }
    }

    private fun updateUIForQuestion() {
        tv_CauHoi?.setText(Common.CAU_HOI.getCauhoi())
        btn_da1!!.text = Common.CAU_HOI.a
        btn_da2!!.text = Common.CAU_HOI.b
        btn_da3!!.text = Common.CAU_HOI.c
        btn_da4!!.text = Common.CAU_HOI.d
        speak(currentQuestionContent)
    }

    private val currentQuestionContent: String
        get() = (("Câu hỏi số " + (idxHocTap + 1) + ", " + Common.CAU_HOI.getCauhoi()
                ).toString() + ". Đáp án một, " + Common.CAU_HOI.a
                + ". Đáp án hai, " + Common.CAU_HOI.b
                + ". Đáp án ba, " + Common.CAU_HOI.c
                + ". Đáp án bốn, " + Common.CAU_HOI.d)

    private fun showResultDialog() {
        val diem = dung.toFloat() / (dung + sai) * 10
        val noiDung = ("Bạn đã hoàn thành bài học. Trả lời đúng " + dung + " câu hỏi. "
                + "Trả lời sai " + sai + " câu hỏi. Điểm, " + diem + " điểm.")
        val utteranceId = UUID.randomUUID().toString()
        textToSpeech!!.speak(noiDung, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

        textToSpeech!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
            }

            override fun onDone(utteranceId: String) {
                runOnUiThread { finish() }
            }

            override fun onError(utteranceId: String) {
                runOnUiThread {
                    Toast.makeText(
                        this@HocTapActivity,
                        "Lỗi khi phát âm",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
        AlertDialog.Builder(this)
            .setTitle("Hoàn thành bài kiểm tra")
            .setMessage(noiDung)
            .setPositiveButton(
                "Thoát"
            ) { dialog: DialogInterface?, which: Int -> finish() }
            .create()
            .show()
    }

    private fun speak(text: String) {
        if (textToSpeech != null) {
            textToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun delayAndProceed(action: Runnable, delayMillis: Int) {
        Handler(Looper.getMainLooper()).postDelayed(action, delayMillis.toLong())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MIC && resultCode == RESULT_OK && data != null) {
            val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (results != null && !results.isEmpty()) {
                val spokenText = results[0].lowercase()
                if (spokenText.contains("đọc")) {
                    repeatQuestion()
                } else {
                    processSpokenAnswer(spokenText)
                }
            }
        }
    }

    private fun processSpokenAnswer(spokenText: String) {
        if (spokenText.contains("1") || spokenText.contains("một")) {
            btn_da1!!.performClick()
        } else if (spokenText.contains("2") || spokenText.contains("hai")) {
            btn_da2!!.performClick()
        } else if (spokenText.contains("3") || spokenText.contains("ba")) {
            btn_da3!!.performClick()
        } else if (spokenText.contains("4") || spokenText.contains("bốn")) {
            btn_da4!!.performClick()
        }
    }

    override fun onDestroy() {
        if (textToSpeech != null) {
            textToSpeech!!.shutdown()
        }
        super.onDestroy()
    }

    companion object {
        private const val REQUEST_MIC = 123
    }
}


