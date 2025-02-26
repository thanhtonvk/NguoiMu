package com.tondz.nguoimu.views.cam_diec;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
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
        findViewById(R.id.btnCauVaTu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCauDict();
                loadGridView();
            }
        });
        findViewById(R.id.btnBangChuCai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createChuCaiDict();
                loadGridView();
            }
        });
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
                    readContent(text);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MIC && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String text = result.get(0).toLowerCase();
                int videoId = keywordToVideoMap.get(text);
                playVideo(videoId);
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

    void createChuCaiDict() {
        keywordToVideoMap.clear();
        keywordToVideoMap.put("chấm", R.raw.dau_cham);
        keywordToVideoMap.put("hỏi", R.raw.dau_hoi);
        keywordToVideoMap.put("huyền", R.raw.dau_huyen);
        keywordToVideoMap.put("ngã", R.raw.dau_nga);
        keywordToVideoMap.put("sắc", R.raw.dau_sac);
        keywordToVideoMap.put("a", R.raw.chu_a);
        keywordToVideoMap.put("ă", R.raw.chu_aw);
        keywordToVideoMap.put("â", R.raw.chu_aa);
        keywordToVideoMap.put("b", R.raw.chu_b);
        keywordToVideoMap.put("c", R.raw.chu_c);

        keywordToVideoMap.put("d", R.raw.chu_d);
        keywordToVideoMap.put("đ", R.raw.chu_dd);
        keywordToVideoMap.put("e", R.raw.chu_e);
        keywordToVideoMap.put("ê", R.raw.chu_ee);
        keywordToVideoMap.put("g", R.raw.chu_g);
        keywordToVideoMap.put("h", R.raw.chu_h);

        keywordToVideoMap.put("i", R.raw.chu_i);
        keywordToVideoMap.put("k", R.raw.chu_k);
        keywordToVideoMap.put("l", R.raw.chu_l);
        keywordToVideoMap.put("m", R.raw.chu_m);
        keywordToVideoMap.put("n", R.raw.chu_n);

        keywordToVideoMap.put("o", R.raw.chu_o);
        keywordToVideoMap.put("ô", R.raw.chu_oo);
        keywordToVideoMap.put("ơ", R.raw.chu_ow);
        keywordToVideoMap.put("p", R.raw.chu_p);
        keywordToVideoMap.put("q", R.raw.chu_q);
        keywordToVideoMap.put("r", R.raw.chu_r);
        keywordToVideoMap.put("s", R.raw.chu_s);

        keywordToVideoMap.put("t", R.raw.chu_t);
        keywordToVideoMap.put("u", R.raw.chu_u);
        keywordToVideoMap.put("ư", R.raw.chu_uw);
        keywordToVideoMap.put("v", R.raw.chu_v);
        keywordToVideoMap.put("x", R.raw.chu_x);
        keywordToVideoMap.put("y", R.raw.chu_y);

    }

    void createCauDict() {
        keywordToVideoMap.clear();
        keywordToVideoMap.put("xin chào", R.raw.xin_chao_vid);
        keywordToVideoMap.put("cảm ơn", R.raw.cam_on_vid);
        keywordToVideoMap.put("đánh vần ngón tay", R.raw.danh_van_ngon_tay);
        keywordToVideoMap.put("bạn khỏe không", R.raw.ban_khoe_khong);
        keywordToVideoMap.put("bạn thật tuyệt vời", R.raw.ban_that_tuyet_voi);
        keywordToVideoMap.put("hẹn gặp lại", R.raw.hen_gap_lai_vid);
        keywordToVideoMap.put("rất vui được gặp bạn", R.raw.rat_vui_duoc_gap_ban_vid);
        keywordToVideoMap.put("tên tôi là", R.raw.ten_toi_la);
        keywordToVideoMap.put("vỗ tay", R.raw.vo_tay);
        keywordToVideoMap.put("xin lỗi", R.raw.xin_loi_vid);
        keywordToVideoMap.put("bất ngờ", R.raw.bat_ngo);
        keywordToVideoMap.put("buồn", R.raw.buon_vid);
        keywordToVideoMap.put("thất vọng", R.raw.that_vong);
        keywordToVideoMap.put("tức giận", R.raw.tuc_gian);
        keywordToVideoMap.put("vui", R.raw.vui);
        keywordToVideoMap.put("biết", R.raw.biet);
        keywordToVideoMap.put("chấp nhận", R.raw.chap_nhan);
        keywordToVideoMap.put("ghen", R.raw.ghen);
        keywordToVideoMap.put("ghét", R.raw.ghet);
        keywordToVideoMap.put("hiểu", R.raw.hieu);
        keywordToVideoMap.put("hồi hộp", R.raw.hoi_hop);
        keywordToVideoMap.put("không biết", R.raw.khong_biet);
        keywordToVideoMap.put("không hiểu", R.raw.khong_hieu);
        keywordToVideoMap.put("không thích", R.raw.khong_thich_vid);
        keywordToVideoMap.put("mắc cở", R.raw.mac_co);
        keywordToVideoMap.put("nhớ", R.raw.nho);
        keywordToVideoMap.put("sợ", R.raw.so_vid);
        keywordToVideoMap.put("bạn thích không", R.raw.thich_vid);
        keywordToVideoMap.put("thông cảm", R.raw.thong_cam);
        keywordToVideoMap.put("tỉnh cảm", R.raw.tinh_cam);
        keywordToVideoMap.put("tò mò", R.raw.to_mo);
        keywordToVideoMap.put("xấu hổ", R.raw.xau_ho);
        keywordToVideoMap.put("yên tâm", R.raw.yen_tam);
        keywordToVideoMap.put("yêu", R.raw.yeu);
        keywordToVideoMap.put("anh trai", R.raw.anh_trai);
        keywordToVideoMap.put("chị gái", R.raw.chi_gai);
        keywordToVideoMap.put("con", R.raw.con01);
        keywordToVideoMap.put("cha", R.raw.cha);
        keywordToVideoMap.put("mẹ", R.raw.me);
        keywordToVideoMap.put("nhà", R.raw.nha);
        keywordToVideoMap.put("gia đình", R.raw.gia_dinh);
        keywordToVideoMap.put("con trai", R.raw.con_trai);
        keywordToVideoMap.put("con gái", R.raw.con_gai);
        keywordToVideoMap.put("yêu thương", R.raw.yeu_thuong);
        keywordToVideoMap.put("biết ơn", R.raw.biet_on);
        keywordToVideoMap.put("hạnh phúc", R.raw.hanh_phuc);

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