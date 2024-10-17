package com.tondz.nguoimu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.tondz.nguoimu.views.MainActivity;

public class ChonChucNangMuActivity extends AppCompatActivity {
    Spinner spinner;
    String[] ngonNgus = new String[]{"Tiếng Việt", "Tiếng Anh", "Tiếng Trung"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chon_chuc_nang_mu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        spinner = findViewById(R.id.spinner);
        findViewById(R.id.btnCamDiec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CamDiecActivity.class));
            }
        });
        findViewById(R.id.btnBt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri webpage = Uri.parse("https://google.com");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);

                startActivity(intent);

            }
        });
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ngonNgus);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                Common.ngonNgu = arg2;
                if (Common.ngonNgu == 0) {
                    Common.classNames = new String[]{
                            "cảm ơn", "hẹn gặp lại", "khỏe", "không thích", "rất vui được gặp bạn", "sợ", "tạm biệt",
                            "thích", "xin chào", "xin lỗi", "biết", "anh trai", "chị gái", "hiểu", "mẹ", "nhà",
                            "nhớ", "tò mò", "yêu"
                    };
                }
                if (Common.ngonNgu == 1) {
                    Common.classNames = new String[]{"thank you", "see you later", "fine", "don't like", "nice to meet you", "scared", "goodbye",
                            "like", "hello", "sorry", "know", "brother", "sister", "understand", "mother", "home",
                            "miss", "curious", "love"};
                }
                if (Common.ngonNgu == 2) {
                    Common.classNames = new String[]{"谢谢", "待会儿见", "好吧", "不喜欢", "很高兴见到你", "害怕", "再见",
                            "喜欢", "你好", "对不起", "知道", "哥哥", "姐姐", "理解", "妈妈", "家",
                            "想念", "好奇", "爱"};
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                arg0.setSelection(0);
                Common.ngonNgu = 0;
                Common.classNames = new String[]{
                        "cảm ơn", "hẹn gặp lại", "khỏe", "không thích", "rất vui được gặp bạn", "sợ", "tạm biệt",
                        "thích", "xin chào", "xin lỗi", "biết", "anh trai", "chị gái", "hiểu", "mẹ", "nhà",
                        "nhớ", "tò mò", "yêu"
                };
            }
        });

    }
}