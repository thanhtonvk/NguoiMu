package com.phuyen.nguoikhuyettat.views.cam_diec

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.GridView
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.phuyen.nguoikhuyettat.R
import java.util.Locale

class NguoiBinhThuongActivity : AppCompatActivity() {
    var videoView: VideoView? = null
    var keywordToVideoMap: LinkedHashMap<String, Int> = LinkedHashMap()
    var gridView: GridView? = null
    var data: MutableList<String> = ArrayList()
    var editText: EditText? = null
    var textToSpeech: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nguoi_binh_thuong)
        videoView = findViewById(R.id.videoView)
        gridView = findViewById(R.id.gridView)
        editText = findViewById(R.id.edtText)
        createCauDict()
        loadGridView()
        onClick()
        textToSpeech = TextToSpeech(
            applicationContext
        ) { i ->
            if (i != TextToSpeech.ERROR) {
                textToSpeech!!.setLanguage(Locale.forLanguageTag("vi-VN"))
            }
        }
    }

    private fun readContent(content: String) {
        textToSpeech!!.speak(content, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun onClick() {
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
                    this@NguoiBinhThuongActivity,
                    "Thiết bị không hỗ trợ tính năng này",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        findViewById<View>(R.id.btnOk).setOnClickListener {
            val text = editText!!.text.toString()
            if (text.isEmpty()) {
                Toast.makeText(
                    this@NguoiBinhThuongActivity,
                    "Vui lòng nhập từ khóa",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()
                videoQueue = extractKeywords(text)
                currentIndex = 0
                playNextVideo()
            }
        }
    }

    private fun playNextVideo() {
        if (currentIndex < videoQueue!!.size) {
            val videoResId = videoQueue!![currentIndex]
            val videoUri = Uri.parse("android.resource://$packageName/$videoResId")

            // Thiết lập MediaController
            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoView)

            // Gắn MediaController và đường dẫn cho VideoView
            videoView!!.setMediaController(mediaController)
            videoView!!.setVideoURI(videoUri)

            videoView!!.setOnCompletionListener {
                currentIndex++
                playNextVideo()
            }
            videoView!!.start()
        }
    }

    private var videoQueue: List<Int>? = null
    private var currentIndex = 0

    private fun extractKeywords(text: String): List<Int> {
        var text = text
        val queue: MutableList<Int> = ArrayList()
        for (keyword in keywordToVideoMap.keys) {
            if (text.contains(keyword)) {
                queue.add(keywordToVideoMap[keyword]!!)
                text = text.replace(keyword, "") // Tránh lặp lại từ khóa
            }
        }
        return queue
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_MIC && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (result != null && !result.isEmpty()) {
                val text = result[0].lowercase(Locale.getDefault())
                videoQueue = extractKeywords(text)
                currentIndex = 0
                playNextVideo()
            }
        }
    }

    fun loadGridView() {
        data.clear()
        for (key in keywordToVideoMap.keys
        ) {
            data.add(key)
            Log.d("TAG", "loadGridView: $key")
        }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,  // Layout mặc định chỉ chứa TextView
            data
        )

        // Gắn Adapter vào GridView
        gridView!!.adapter = adapter
        gridView!!.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                val key = data[position]
                val videoId = keywordToVideoMap[key]!!
                playVideo(videoId)
            }
    }

    fun createCauDict() {
        keywordToVideoMap.clear()
        keywordToVideoMap["biển đông"] = R.raw.bien_dong
        keywordToVideoMap["biểu đồ"] = R.raw.bieu_do
        keywordToVideoMap["bom đạn"] = R.raw.bom_dan
        keywordToVideoMap["boong tàu"] = R.raw.boong_tau
        keywordToVideoMap["buồn nôn"] = R.raw.buon_non
        keywordToVideoMap["bài thơ"] = R.raw.bai_tho
        keywordToVideoMap["bàn chải đánh răng"] = R.raw.ban_chai_danh_rang
        keywordToVideoMap["bàn ghế"] = R.raw.ban_ghe
        keywordToVideoMap["bãi tắm"] = R.raw.bai_tam
        keywordToVideoMap["bãi đổ xe"] = R.raw.bai_do_xe
        keywordToVideoMap["bóng bay"] = R.raw.bong_bay
        keywordToVideoMap["bỏ phiếu"] = R.raw.bo_phieu
        keywordToVideoMap["bộ truyện"] = R.raw.bo_truyen
        keywordToVideoMap["bức tranh"] = R.raw.buc_tranh
        keywordToVideoMap["bực mình"] = R.raw.buc_minh
        keywordToVideoMap["cao thấp"] = R.raw.cao_thap
        keywordToVideoMap["cao tầng"] = R.raw.cao_tang
        keywordToVideoMap["chai nước"] = R.raw.chai_nuoc
        keywordToVideoMap["che chở"] = R.raw.che_cho
        keywordToVideoMap["chuyên cần"] = R.raw.chuyen_can
        keywordToVideoMap["chuyển động"] = R.raw.chuyen_dong
        keywordToVideoMap["chú rể"] = R.raw.chu_re
        keywordToVideoMap["chậm chạp"] = R.raw.cham_chap
        keywordToVideoMap["chật chội"] = R.raw.chat_choi
        keywordToVideoMap["chặt chẽ"] = R.raw.chat_che
        keywordToVideoMap["con sông"] = R.raw.con_song
        keywordToVideoMap["cánh buồm"] = R.raw.canh_buom
        keywordToVideoMap["câu cá"] = R.raw.cau_ca
        keywordToVideoMap["cây trồng"] = R.raw.cay_trong
        keywordToVideoMap["cô dâu"] = R.raw.co_dau
        keywordToVideoMap["công nhận"] = R.raw.cong_nhan
        keywordToVideoMap["căng thẳng"] = R.raw.cang_thang
        keywordToVideoMap["cảm cúm"] = R.raw.cam_cum
        keywordToVideoMap["cấp cứu"] = R.raw.cap_cuu
        keywordToVideoMap["cất giữ"] = R.raw.cat_giu
        keywordToVideoMap["cất tiếng nói"] = R.raw.cat_tieng_noi
        keywordToVideoMap["cầu thủ"] = R.raw.cau_thu
        keywordToVideoMap["cắm trại"] = R.raw.cam_trai
        keywordToVideoMap["cố gắng"] = R.raw.co_gang
        keywordToVideoMap["dự án"] = R.raw.du_an
        keywordToVideoMap["giao tiếp"] = R.raw.giao_tiep
        keywordToVideoMap["giàu có"] = R.raw.giau_co
        keywordToVideoMap["giám khảo"] = R.raw.giam_khao
        keywordToVideoMap["giám đốc"] = R.raw.giam_doc
        keywordToVideoMap["giấy nháp"] = R.raw.giay_nhap
        keywordToVideoMap["giấy tờ"] = R.raw.giay_to
        keywordToVideoMap["giọng ca"] = R.raw.giong_ca
        keywordToVideoMap["giọng nói"] = R.raw.giong_noi
        keywordToVideoMap["giới hạn"] = R.raw.gioi_han
        keywordToVideoMap["giới thiệu"] = R.raw.gioi_thieu
        keywordToVideoMap["góp ý"] = R.raw.gop_y
        keywordToVideoMap["gù lưng"] = R.raw.gu_lung
        keywordToVideoMap["gợi ý"] = R.raw.goi_y
        keywordToVideoMap["hi sinh"] = R.raw.hi_sinh
        keywordToVideoMap["hiệu thuốc"] = R.raw.hieu_thuoc
        keywordToVideoMap["hoang tưởng"] = R.raw.hoang_tuong
        keywordToVideoMap["hoạt hình"] = R.raw.hoat_hinh
        keywordToVideoMap["huân chương"] = R.raw.huan_chuong
        keywordToVideoMap["hài lòng"] = R.raw.hai_long
        keywordToVideoMap["hành lang"] = R.raw.hanh_lang
        keywordToVideoMap["hòa bình"] = R.raw.hoa_binh
        keywordToVideoMap["hòn đá"] = R.raw.hon_da
        keywordToVideoMap["hút thuốc lá"] = R.raw.hut_thuoc_la
        keywordToVideoMap["hướng dẫn"] = R.raw.huong_dan
        keywordToVideoMap["học bổng"] = R.raw.hoc_bong
        keywordToVideoMap["học giỏi"] = R.raw.hoc_gioi
        keywordToVideoMap["học kém"] = R.raw.hoc_kem
        keywordToVideoMap["học trung bình"] = R.raw.hoc_trung_binh
        keywordToVideoMap["học trò"] = R.raw.hoc_tro
        keywordToVideoMap["học tập"] = R.raw.hoc_tap
        keywordToVideoMap["hỏi han"] = R.raw.hoi_han
        keywordToVideoMap["hồ nước"] = R.raw.ho_nuoc
        keywordToVideoMap["hộp sữa"] = R.raw.hop_sua
        keywordToVideoMap["khiêm tốn"] = R.raw.khiem_ton
        keywordToVideoMap["khoe"] = R.raw.khoe
        keywordToVideoMap["khuyến khích"] = R.raw.khuyen_khich
        keywordToVideoMap["khám phá"] = R.raw.kham_pha
        keywordToVideoMap["khó khăn"] = R.raw.kho_khan
        keywordToVideoMap["khổng lồ"] = R.raw.khong_lo
        keywordToVideoMap["kinh ngạc"] = R.raw.kinh_ngac
        keywordToVideoMap["kêu gọi"] = R.raw.keu_goi
        keywordToVideoMap["kể chuyện"] = R.raw.ke_chuyen
        keywordToVideoMap["kỉ niệm"] = R.raw.ki_niem
        keywordToVideoMap["lang thang"] = R.raw.lang_thang
        keywordToVideoMap["lau chùi"] = R.raw.lau_chui
        keywordToVideoMap["lau nhà"] = R.raw.lau_nha
        keywordToVideoMap["leo trèo"] = R.raw.leo_treo
        keywordToVideoMap["luyện tập"] = R.raw.luyen_tap
        keywordToVideoMap["làm bài tập"] = R.raw.lam_bai_tap
        keywordToVideoMap["lãnh đạo"] = R.raw.lanh_dao
        keywordToVideoMap["lên xe"] = R.raw.len_xe
        keywordToVideoMap["lưu ý"] = R.raw.luu_y
        keywordToVideoMap["lạc quan"] = R.raw.lac_quan
        keywordToVideoMap["lắp ráp"] = R.raw.lap_rap
        keywordToVideoMap["mất ngủ"] = R.raw.mat_ngu
        keywordToVideoMap["mắng"] = R.raw.mang
        keywordToVideoMap["mặc"] = R.raw.mac
        keywordToVideoMap["mệt mỏi"] = R.raw.met_moi
        keywordToVideoMap["nấu nướng"] = R.raw.nau_nuong
        keywordToVideoMap["rửa tay"] = R.raw.rua_tay
        keywordToVideoMap["trông nhà"] = R.raw.trong_nha
        keywordToVideoMap["trả thù"] = R.raw.tra_thu
        keywordToVideoMap["yêu cầu"] = R.raw.yeu_cau
        keywordToVideoMap["yêu mến"] = R.raw.yeu_men
        keywordToVideoMap["áo"] = R.raw.ao
        keywordToVideoMap["đau bụng"] = R.raw.dau_bung
        keywordToVideoMap["đau răng"] = R.raw.dau_rang
        keywordToVideoMap["đi dạo"] = R.raw.di_dao
        keywordToVideoMap["đi lạc"] = R.raw.di_lac
        keywordToVideoMap["đèn ông sao"] = R.raw.den_ong_sao
        keywordToVideoMap["địa chỉ"] = R.raw.dia_chi
        keywordToVideoMap["ảo thuật"] = R.raw.ao_thuat
    }


    fun playVideo(source: Int) {
        // Đặt đường dẫn video
        val videoUri = Uri.parse("android.resource://$packageName/$source")

        // Thiết lập MediaController
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)

        // Gắn MediaController và đường dẫn cho VideoView
        videoView!!.setMediaController(mediaController)
        videoView!!.setVideoURI(videoUri)


        // Bắt đầu phát video
        videoView!!.start()
    }

    companion object {
        private const val REQUEST_MIC = 12345
    }
}