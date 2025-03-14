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
        keywordToVideoMap.put("a", R.raw.chu_a);

        keywordToVideoMap.put("b", R.raw.chu_b);
        keywordToVideoMap.put("c", R.raw.chu_c);

        keywordToVideoMap.put("d", R.raw.chu_d);

        keywordToVideoMap.put("e", R.raw.chu_e);

        keywordToVideoMap.put("g", R.raw.chu_g);
        keywordToVideoMap.put("h", R.raw.chu_h);

        keywordToVideoMap.put("i", R.raw.chu_i);
        keywordToVideoMap.put("k", R.raw.chu_k);
        keywordToVideoMap.put("l", R.raw.chu_l);
        keywordToVideoMap.put("m", R.raw.chu_m);
        keywordToVideoMap.put("n", R.raw.chu_n);

        keywordToVideoMap.put("o", R.raw.chu_o);

        keywordToVideoMap.put("p", R.raw.chu_p);
        keywordToVideoMap.put("q", R.raw.chu_q);
        keywordToVideoMap.put("r", R.raw.chu_r);
        keywordToVideoMap.put("s", R.raw.chu_s);

        keywordToVideoMap.put("t", R.raw.chu_t);
        keywordToVideoMap.put("u", R.raw.chu_u);

        keywordToVideoMap.put("v", R.raw.chu_v);
        keywordToVideoMap.put("x", R.raw.chu_x);
        keywordToVideoMap.put("y", R.raw.chu_y);

    }

    void createCauDict() {
        keywordToVideoMap.clear();
        keywordToVideoMap.put("ăn", R.raw.an);
        keywordToVideoMap.put("anh trai", R.raw.anh_trai);
        keywordToVideoMap.put("bánh mì", R.raw.banh_mi);
        keywordToVideoMap.put("bệnh viện", R.raw.benh_vien);
        keywordToVideoMap.put("biết", R.raw.biet);
        keywordToVideoMap.put("bún", R.raw.bun);
        keywordToVideoMap.put("cảm ơn", R.raw.cam_on);
        keywordToVideoMap.put("chị gái", R.raw.chi_gai);
        keywordToVideoMap.put("đi", R.raw.di);
        keywordToVideoMap.put("đi vệ sinh", R.raw.di_ve_sinh);
        keywordToVideoMap.put("đọc sách", R.raw.doc_sach);
        keywordToVideoMap.put("đồng ý", R.raw.dong_y);
        keywordToVideoMap.put("hẹn gặp lại", R.raw.hen_gap_lai);
        keywordToVideoMap.put("hiểu", R.raw.hieu);
        keywordToVideoMap.put("khỏe", R.raw.khoe);
        keywordToVideoMap.put("không thích", R.raw.khong_thich);
        keywordToVideoMap.put("mẹ", R.raw.me);
        keywordToVideoMap.put("nhà", R.raw.nha);
        keywordToVideoMap.put("nhớ", R.raw.nho);
        keywordToVideoMap.put("rất vui được gặp bạn", R.raw.rat_vui_duoc_gap_ban);
        keywordToVideoMap.put("siêu thị", R.raw.sieu_thi);
        keywordToVideoMap.put("sợ", R.raw.so);
        keywordToVideoMap.put("tạm biệt", R.raw.tam_biet);
        keywordToVideoMap.put("thích", R.raw.thich);
        keywordToVideoMap.put("tò mò", R.raw.to_mo);
        keywordToVideoMap.put("uống", R.raw.uong);
        keywordToVideoMap.put("xe mô tô", R.raw.xe_mo_to);
        keywordToVideoMap.put("xin chào", R.raw.xin_chao);
        keywordToVideoMap.put("xin lỗi", R.raw.xin_loi);
        keywordToVideoMap.put("yêu", R.raw.yeu);


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