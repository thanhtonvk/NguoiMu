package com.tondz.nguoimu.views.cam_diec;

import static android.widget.Toast.LENGTH_LONG;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tondz.nguoimu.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class NguoiBinhThuongActivity extends AppCompatActivity {
    private static final int REQUEST_MIC = 12345;
    VideoView videoView;
    LinkedHashMap<String, Integer> keywordToVideoMap = new LinkedHashMap<>();
    GridView gridView;
    List<String> data = new ArrayList<>();
    EditText editText;
    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nguoi_binh_thuong);
        videoView = findViewById(R.id.videoView);
        gridView = findViewById(R.id.gridView);
        editText = findViewById(R.id.edtText);
        createCauDict();
        loadGridView();
        onClick();
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.forLanguageTag("vi-VN"));
                }
            }
        });

    }

    private void readContent(String content) {
        textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    void onClick() {
        findViewById(R.id.btnMic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent
                        = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                        Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

                try {
                    startActivityForResult(intent, REQUEST_MIC);
                } catch (Exception e) {
                    Toast.makeText(NguoiBinhThuongActivity.this, "Thiết bị không hỗ trợ tính năng này", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                if (text.isEmpty()) {
                    Toast.makeText(NguoiBinhThuongActivity.this, "Vui lòng nhập từ khóa", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), text, LENGTH_LONG).show();
                    videoQueue = extractKeywords(text);
                    currentIndex = 0;
                    playNextVideo();
                }
            }
        });
    }

    private void playNextVideo() {
        if (currentIndex < videoQueue.size()) {
            int videoResId = videoQueue.get(currentIndex);
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + videoResId);

            // Thiết lập MediaController
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);

            // Gắn MediaController và đường dẫn cho VideoView
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(videoUri);

            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    currentIndex++;
                    playNextVideo();
                }
            });
            videoView.start();
        }
    }

    private List<Integer> videoQueue;
    private int currentIndex = 0;

    private List<Integer> extractKeywords(String text) {
        List<Integer> queue = new ArrayList<>();
        for (String keyword : keywordToVideoMap.keySet()) {
            if (text.contains(keyword)) {
                queue.add(keywordToVideoMap.get(keyword));
                text = text.replace(keyword, ""); // Tránh lặp lại từ khóa
            }
        }
        return queue;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MIC && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String text = result.get(0).toLowerCase();
                videoQueue = extractKeywords(text);
                currentIndex = 0;
                playNextVideo();
            }
        }
    }

    void loadGridView() {
        data.clear();
        for (String key : keywordToVideoMap.keySet()
        ) {
            data.add(key);
            Log.d("TAG", "loadGridView: " + key);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1, // Layout mặc định chỉ chứa TextView
                data
        );

        // Gắn Adapter vào GridView
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String key = data.get(position);
                int videoId = keywordToVideoMap.get(key);
                playVideo(videoId);
            }
        });
    }

    void createCauDict() {
        keywordToVideoMap.clear();
        keywordToVideoMap.put("biển đông", R.raw.bien_dong);
        keywordToVideoMap.put("biểu đồ", R.raw.bieu_do);
        keywordToVideoMap.put("bom đạn", R.raw.bom_dan);
        keywordToVideoMap.put("boong tàu", R.raw.boong_tau);
        keywordToVideoMap.put("buồn nôn", R.raw.buon_non);
        keywordToVideoMap.put("bài thơ", R.raw.bai_tho);
        keywordToVideoMap.put("bàn chải đánh răng", R.raw.ban_chai_danh_rang);
        keywordToVideoMap.put("bàn ghế", R.raw.ban_ghe);
        keywordToVideoMap.put("bãi tắm", R.raw.bai_tam);
        keywordToVideoMap.put("bãi đổ xe", R.raw.bai_do_xe);
        keywordToVideoMap.put("bóng bay", R.raw.bong_bay);
        keywordToVideoMap.put("bỏ phiếu", R.raw.bo_phieu);
        keywordToVideoMap.put("bộ truyện", R.raw.bo_truyen);
        keywordToVideoMap.put("bức tranh", R.raw.buc_tranh);
        keywordToVideoMap.put("bực mình", R.raw.buc_minh);
        keywordToVideoMap.put("cao thấp", R.raw.cao_thap);
        keywordToVideoMap.put("cao tầng", R.raw.cao_tang);
        keywordToVideoMap.put("chai nước", R.raw.chai_nuoc);
        keywordToVideoMap.put("che chở", R.raw.che_cho);
        keywordToVideoMap.put("chuyên cần", R.raw.chuyen_can);
        keywordToVideoMap.put("chuyển động", R.raw.chuyen_dong);
        keywordToVideoMap.put("chú rể", R.raw.chu_re);
        keywordToVideoMap.put("chậm chạp", R.raw.cham_chap);
        keywordToVideoMap.put("chật chội", R.raw.chat_choi);
        keywordToVideoMap.put("chặt chẽ", R.raw.chat_che);
        keywordToVideoMap.put("con sông", R.raw.con_song);
        keywordToVideoMap.put("cánh buồm", R.raw.canh_buom);
        keywordToVideoMap.put("câu cá", R.raw.cau_ca);
        keywordToVideoMap.put("cây trồng", R.raw.cay_trong);
        keywordToVideoMap.put("cô dâu", R.raw.co_dau);
        keywordToVideoMap.put("công nhận", R.raw.cong_nhan);
        keywordToVideoMap.put("căng thẳng", R.raw.cang_thang);
        keywordToVideoMap.put("cảm cúm", R.raw.cam_cum);
        keywordToVideoMap.put("cấp cứu", R.raw.cap_cuu);
        keywordToVideoMap.put("cất giữ", R.raw.cat_giu);
        keywordToVideoMap.put("cất tiếng nói", R.raw.cat_tieng_noi);
        keywordToVideoMap.put("cầu thủ", R.raw.cau_thu);
        keywordToVideoMap.put("cắm trại", R.raw.cam_trai);
        keywordToVideoMap.put("cố gắng", R.raw.co_gang);
        keywordToVideoMap.put("dự án", R.raw.du_an);
        keywordToVideoMap.put("giao tiếp", R.raw.giao_tiep);
        keywordToVideoMap.put("giàu có", R.raw.giau_co);
        keywordToVideoMap.put("giám khảo", R.raw.giam_khao);
        keywordToVideoMap.put("giám đốc", R.raw.giam_doc);
        keywordToVideoMap.put("giấy nháp", R.raw.giay_nhap);
        keywordToVideoMap.put("giấy tờ", R.raw.giay_to);
        keywordToVideoMap.put("giọng ca", R.raw.giong_ca);
        keywordToVideoMap.put("giọng nói", R.raw.giong_noi);
        keywordToVideoMap.put("giới hạn", R.raw.gioi_han);
        keywordToVideoMap.put("giới thiệu", R.raw.gioi_thieu);
        keywordToVideoMap.put("góp ý", R.raw.gop_y);
        keywordToVideoMap.put("gù lưng", R.raw.gu_lung);
        keywordToVideoMap.put("gợi ý", R.raw.goi_y);
        keywordToVideoMap.put("hi sinh", R.raw.hi_sinh);
        keywordToVideoMap.put("hiệu thuốc", R.raw.hieu_thuoc);
        keywordToVideoMap.put("hoang tưởng", R.raw.hoang_tuong);
        keywordToVideoMap.put("hoạt hình", R.raw.hoat_hinh);
        keywordToVideoMap.put("huân chương", R.raw.huan_chuong);
        keywordToVideoMap.put("hài lòng", R.raw.hai_long);
        keywordToVideoMap.put("hành lang", R.raw.hanh_lang);
        keywordToVideoMap.put("hòa bình", R.raw.hoa_binh);
        keywordToVideoMap.put("hòn đá", R.raw.hon_da);
        keywordToVideoMap.put("hút thuốc lá", R.raw.hut_thuoc_la);
        keywordToVideoMap.put("hướng dẫn", R.raw.huong_dan);
        keywordToVideoMap.put("học bổng", R.raw.hoc_bong);
        keywordToVideoMap.put("học giỏi", R.raw.hoc_gioi);
        keywordToVideoMap.put("học kém", R.raw.hoc_kem);
        keywordToVideoMap.put("học trung bình", R.raw.hoc_trung_binh);
        keywordToVideoMap.put("học trò", R.raw.hoc_tro);
        keywordToVideoMap.put("học tập", R.raw.hoc_tap);
        keywordToVideoMap.put("hỏi han", R.raw.hoi_han);
        keywordToVideoMap.put("hồ nước", R.raw.ho_nuoc);
        keywordToVideoMap.put("hộp sữa", R.raw.hop_sua);
        keywordToVideoMap.put("khiêm tốn", R.raw.khiem_ton);
        keywordToVideoMap.put("khoe", R.raw.khoe);
        keywordToVideoMap.put("khuyến khích", R.raw.khuyen_khich);
        keywordToVideoMap.put("khám phá", R.raw.kham_pha);
        keywordToVideoMap.put("khó khăn", R.raw.kho_khan);
        keywordToVideoMap.put("khổng lồ", R.raw.khong_lo);
        keywordToVideoMap.put("kinh ngạc", R.raw.kinh_ngac);
        keywordToVideoMap.put("kêu gọi", R.raw.keu_goi);
        keywordToVideoMap.put("kể chuyện", R.raw.ke_chuyen);
        keywordToVideoMap.put("kỉ niệm", R.raw.ki_niem);
        keywordToVideoMap.put("lang thang", R.raw.lang_thang);
        keywordToVideoMap.put("lau chùi", R.raw.lau_chui);
        keywordToVideoMap.put("lau nhà", R.raw.lau_nha);
        keywordToVideoMap.put("leo trèo", R.raw.leo_treo);
        keywordToVideoMap.put("luyện tập", R.raw.luyen_tap);
        keywordToVideoMap.put("làm bài tập", R.raw.lam_bai_tap);
        keywordToVideoMap.put("lãnh đạo", R.raw.lanh_dao);
        keywordToVideoMap.put("lên xe", R.raw.len_xe);
        keywordToVideoMap.put("lưu ý", R.raw.luu_y);
        keywordToVideoMap.put("lạc quan", R.raw.lac_quan);
        keywordToVideoMap.put("lắp ráp", R.raw.lap_rap);
        keywordToVideoMap.put("mất ngủ", R.raw.mat_ngu);
        keywordToVideoMap.put("mắng", R.raw.mang);
        keywordToVideoMap.put("mặc", R.raw.mac);
        keywordToVideoMap.put("mệt mỏi", R.raw.met_moi);
        keywordToVideoMap.put("nấu nướng", R.raw.nau_nuong);
        keywordToVideoMap.put("rửa tay", R.raw.rua_tay);
        keywordToVideoMap.put("trông nhà", R.raw.trong_nha);
        keywordToVideoMap.put("trả thù", R.raw.tra_thu);
        keywordToVideoMap.put("yêu cầu", R.raw.yeu_cau);
        keywordToVideoMap.put("yêu mến", R.raw.yeu_men);
        keywordToVideoMap.put("áo", R.raw.ao);
        keywordToVideoMap.put("đau bụng", R.raw.dau_bung);
        keywordToVideoMap.put("đau răng", R.raw.dau_rang);
        keywordToVideoMap.put("đi dạo", R.raw.di_dao);
        keywordToVideoMap.put("đi lạc", R.raw.di_lac);
        keywordToVideoMap.put("đèn ông sao", R.raw.den_ong_sao);
        keywordToVideoMap.put("địa chỉ", R.raw.dia_chi);
        keywordToVideoMap.put("ảo thuật", R.raw.ao_thuat);

    }


    void playVideo(int source) {
        // Đặt đường dẫn video
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + source);

        // Thiết lập MediaController
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        // Gắn MediaController và đường dẫn cho VideoView
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(videoUri);


        // Bắt đầu phát video
        videoView.start();
    }
}