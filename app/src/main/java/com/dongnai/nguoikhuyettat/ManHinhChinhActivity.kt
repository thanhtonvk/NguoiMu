package com.dongnai.nguoikhuyettat

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dongnai.nguoikhuyettat.views.MainActivity
import com.dongnai.nguoikhuyettat.views.cam_diec.ChonChucNangMuActivity
import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern


class ManHinhChinhActivity : AppCompatActivity() {
    var REQUEST_MIC: Int = 123

    var isCamDiec: Boolean = true
    var isKhiemThi: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_man_hinh_chinh)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!isCamDiec) {
            findViewById<View>(R.id.btnCamDiec).visibility = View.GONE
        }
        if (!isKhiemThi) {
            findViewById<View>(R.id.btnKhiemThi).visibility = View.GONE
        }

        findViewById<View>(R.id.btnCamDiec).setOnClickListener {
            startActivity(
                Intent(applicationContext, ChonChucNangMuActivity::class.java)
            )
        }
        findViewById<View>(R.id.btnKhiemThi).setOnClickListener {
            startActivity(
                Intent(applicationContext, MainActivity::class.java)
            )
        }
        findViewById<View>(R.id.btnMic).setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
            try {
                startActivityForResult(intent, REQUEST_MIC)
            } catch (e: Exception) {
                Toast.makeText(
                    this@ManHinhChinhActivity,
                    "Thiết bị không hỗ trợ tính năng này",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun actionMic(text: String) {
        if (text.lowercase(Locale.getDefault()).contains("khiem thi")) {
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }
        if (text.lowercase(Locale.getDefault()).contains("cam diec")) {
            startActivity(Intent(applicationContext, ChonChucNangMuActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MIC && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (result != null && !result.isEmpty()) {
                var text = result[0]
                text = removeAccent(text)
                actionMic(text)
            }
        }
    }

    fun removeAccent(s: String?): String {
        // Normalize văn bản để chuyển các ký tự có dấu thành dạng ký tự tổ hợp
        val temp = Normalizer.normalize(s, Normalizer.Form.NFD)

        // Loại bỏ các dấu bằng cách loại bỏ các ký tự không phải ASCII
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(temp).replaceAll("").replace("đ".toRegex(), "d")
            .replace("Đ".toRegex(), "D")
    }
}