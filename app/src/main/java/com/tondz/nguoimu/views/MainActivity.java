package com.tondz.nguoimu.views;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tondz.nguoimu.NguoiMuSDK;
import com.tondz.nguoimu.R;
import com.tondz.nguoimu.database.DBContext;
import com.tondz.nguoimu.models.NguoiThan;
import com.tondz.nguoimu.utils.CalDistance;
import com.tondz.nguoimu.utils.Common;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnDoDuong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), DoDuongActivity.class));
            }
        });
        findViewById(R.id.btnNhanDienNguoi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), NhanDienNguoiThanActivity.class));
            }
        });
        checkPermissions();
        loadWidthInImages();
    }

    private void loadWidthInImages() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String widthInImages = preferences.getString("widthInImages", "");
        if (!widthInImages.equalsIgnoreCase("")) {
            String[] arr = widthInImages.split(",");
            for (int i = 0; i < arr.length; i++) {
                CalDistance.widthInImages[i] = Double.parseDouble(arr[i]);
            }
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }
}
