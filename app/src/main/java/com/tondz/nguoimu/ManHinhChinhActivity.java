package com.tondz.nguoimu;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.tondz.nguoimu.views.DoDuongActivity;
import com.tondz.nguoimu.views.MainActivity;
import com.tondz.nguoimu.views.NhanDienNguoiThanActivity;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public class ManHinhChinhActivity extends AppCompatActivity {
    int REQUEST_MIC = 123;

    boolean isCamDiec = true;
    boolean isKhiemThi = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_man_hinh_chinh);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (!isCamDiec) {
            findViewById(R.id.btnCamDiec).setVisibility(View.GONE);
        }
        if (!isKhiemThi) {
            findViewById(R.id.btnKhiemThi).setVisibility(View.GONE);
        }

        findViewById(R.id.btnCamDiec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ChonChucNangMuActivity.class));
            }
        });
        findViewById(R.id.btnKhiemThi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
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
                    Toast.makeText(ManHinhChinhActivity.this, "Thiết bị không hỗ trợ tính năng này", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void actionMic(String text) {
        if (text.toLowerCase().contains("khiem thi")) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        if (text.toLowerCase().contains("cam diec")) {
            startActivity(new Intent(getApplicationContext(), ChonChucNangMuActivity.class));
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MIC && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String text = result.get(0);
                text = removeAccent(text);
                actionMic(text);
            }
        }
    }

    public String removeAccent(String s) {
        // Normalize văn bản để chuyển các ký tự có dấu thành dạng ký tự tổ hợp
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);

        // Loại bỏ các dấu bằng cách loại bỏ các ký tự không phải ASCII
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D");
    }

}