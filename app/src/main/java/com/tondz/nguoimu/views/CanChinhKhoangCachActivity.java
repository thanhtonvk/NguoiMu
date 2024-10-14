package com.tondz.nguoimu.views;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.tondz.nguoimu.NguoiMuSDK;
import com.tondz.nguoimu.R;
import com.tondz.nguoimu.database.DBContext;
import com.tondz.nguoimu.utils.CalDistance;
import com.tondz.nguoimu.utils.Common;

import java.util.List;

public class CanChinhKhoangCachActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    NguoiMuSDK yolov8Ncnn = new NguoiMuSDK();
    private SurfaceView cameraView;
    private static final int REQUEST_CAMERA = 510;
    TextView tvKhoangCach, tvKichThuoc;
    private int facing = 1;
    List<String> stringList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_can_chinh_khoang_cach);
        reload();
        init();
        getObject();
        onClick();

    }

    private void onClick() {
        findViewById(R.id.btnCong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!stringList.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Update 5", Toast.LENGTH_SHORT).show();
                    updateDistance(stringList.get(0), 5);
                }
            }
        });
        findViewById(R.id.btnTru).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!stringList.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Update -5", Toast.LENGTH_SHORT).show();
                    updateDistance(stringList.get(0), -5);
                }
            }
        });
        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                String widthInImages = Common.convertArrayToString(CalDistance.widthInImages);

                editor.putString("widthInImages", widthInImages);
                editor.apply();
                finish();
            }
        });
    }

    private void getObject() {
        new Thread(() -> {
            while (true) {
                List<String> temp = yolov8Ncnn.getListResult();
                if (!temp.isEmpty()) {
                    stringList = temp;
                    showDistance(stringList.get(0));
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void showDistance(String text) {
        String[] arr = text.split(" ");
        int label = Integer.parseInt(arr[0]);
        double x = Double.parseDouble(arr[1]);
        double y = Double.parseDouble(arr[2]);
        double w = Double.parseDouble(arr[3]);
        double h = Double.parseDouble(arr[4]);
        double focalLength = CalDistance.calculateFocalLength(CalDistance.knownDistances[label], CalDistance.knownWidths[label],
                CalDistance.widthInImages[label]);
        double distance = CalDistance.calculateDistance(CalDistance.knownWidths[label], focalLength, w);
        tvKhoangCach.setText(String.format("Khoảng cách thực %,.2fm", distance));
        tvKichThuoc.setText(String.format("Kích thước vật: %,.2f", CalDistance.widthInImages[label]));
    }

    @SuppressLint("SetTextI18n")
    private void updateDistance(String text, double value) {
        String[] arr = text.split(" ");
        int label = Integer.parseInt(arr[0]);
        CalDistance.widthInImages[label] += value;
    }

    private void init() {
        cameraView = findViewById(R.id.cameraview);
        cameraView.getHolder().addCallback(this);
        tvKhoangCach = findViewById(R.id.tvKc);
        tvKichThuoc = findViewById(R.id.tvKichthuoc);
    }

    private void reload() {
        boolean ret_init = yolov8Ncnn.loadModel(getAssets(), 1, 0, 1,0);
        if (!ret_init) {
            Log.e("DoDuongActivity", "yolov8ncnn loadModel failed");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }

        yolov8Ncnn.openCamera(facing);
    }

    @Override
    public void onPause() {
        super.onPause();
        yolov8Ncnn.closeCamera();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        yolov8Ncnn.setOutputWindow(holder.getSurface());
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }
}