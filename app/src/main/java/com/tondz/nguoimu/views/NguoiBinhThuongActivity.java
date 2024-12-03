package com.tondz.nguoimu.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.tondz.nguoimu.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NguoiBinhThuongActivity extends AppCompatActivity {
    private static final int REQUEST_MIC = 12345;
    VideoView videoView;
    HashMap<String, Integer> keywordToVideoMap = new HashMap<>();
    GridView gridView;
    List<String> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nguoi_binh_thuong);
        videoView = findViewById(R.id.videoView);
        gridView = findViewById(R.id.gridView);
        createDict();
        loadGridView();
        onClick();

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
        for (String key : keywordToVideoMap.keySet()
        ) {
            data.add(key);
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

    void createDict() {
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