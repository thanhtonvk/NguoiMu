package com.tondz.nguoimu.views;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.tondz.nguoimu.R;
import com.tondz.nguoimu.views.cam_diec.CamDiecActivity;
import com.tondz.nguoimu.views.cam_diec.ChonChucNangCamDiecActivity;
import com.tondz.nguoimu.views.nguoi_mu.MainActivity;

public class ManHinhChinh extends AppCompatActivity {

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
        findViewById(R.id.btnCamdiec).setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), ChonChucNangCamDiecActivity.class));
        });
        findViewById(R.id.btnNguoiMu).setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        });

    }
}