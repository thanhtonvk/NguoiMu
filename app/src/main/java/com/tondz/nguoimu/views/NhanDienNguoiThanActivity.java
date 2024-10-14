package com.tondz.nguoimu.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.tondz.nguoimu.NguoiMuSDK;
import com.tondz.nguoimu.R;
import com.tondz.nguoimu.database.DBContext;
import com.tondz.nguoimu.models.NguoiThan;
import com.tondz.nguoimu.utils.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class NhanDienNguoiThanActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    NguoiMuSDK yolov8Ncnn = new NguoiMuSDK();
    private SurfaceView cameraView;
    private static final int REQUEST_CAMERA = 510;
    TextToSpeech textToSpeech;
    ImageView imgView;
    DBContext dbContext;
    TextView tvName;
    Handler handler;
    Runnable runnable;
    private boolean canPlaySound = true;
    Button btnDoiCamera;
    private int facing = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nhan_dien_nguoi_than);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        checkPermissions();
        reload();
        onClick();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                canPlaySound = true;
            }
        };
        getObject();

    }

    private void onClick() {
        btnDoiCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int new_facing = 1 - facing;
                yolov8Ncnn.closeCamera();
                yolov8Ncnn.openCamera(new_facing);
                facing = new_facing;
            }
        });

    }

    NguoiThan nguoiThan = null;

    private void getObject() {
        new Thread(() -> {
            while (true) {
                nguoiThan = null;
                String stringEmb = yolov8Ncnn.getEmbedding();
                if (!stringEmb.isEmpty()) {
                    nguoiThan = timNguoi(stringEmb);
                    Bitmap bitmap = yolov8Ncnn.getFaceAlign();
                    if (bitmap != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imgView.setImageBitmap(bitmap);
                            }
                        });
                    }
                    if (nguoiThan != null) {
                        if (canPlaySound) {
                            speakNguoiThan(nguoiThan);
                            tvName.setText(nguoiThan.getTen());
                            canPlaySound = false;
                            handler.postDelayed(runnable, 5000);
                        }
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void init() {
        btnDoiCamera = findViewById(R.id.btnChangeCamera);
        cameraView = findViewById(R.id.cameraview);
        imgView = findViewById(R.id.imgView);
        cameraView.getHolder().addCallback(this);
        dbContext = new DBContext(NhanDienNguoiThanActivity.this);
        tvName = findViewById(R.id.tvName);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.forLanguageTag("vi-VN"));
                }
            }
        });
        findViewById(R.id.btnThem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ThemNguoiThanActivity.class));
            }
        });

    }

    private void speakNguoiThan(NguoiThan nguoiThan) {
        textToSpeech.speak(nguoiThan.getTen(), TextToSpeech.QUEUE_FLUSH, null);
    }

    private NguoiThan timNguoi(String embedding) {
        NguoiThan result = null;
        double maxScore = 0;
        for (NguoiThan nguoiThan : dbContext.getNguoiThans()
        ) {
            String[] str_target = embedding.split(",");
            String[] str_source = nguoiThan.getEmbedding().split(",");
            if (str_target.length == 512 && str_source.length == 512) {
                double[] target = new double[512];
                for (int i = 0; i < 512; i++) {
                    target[i] = Double.parseDouble(str_target[i]);
                }

                double[] source = new double[512];
                for (int i = 0; i < 512; i++) {
                    source[i] = Double.parseDouble(str_source[i]);
                }
                double score = Common.cosineSimilarity(target, source);

                if (score > 0.5 && score > maxScore) {
                    maxScore = score;
                    result = nguoiThan;
                }
            }


        }
        return result;
    }

    private void reload() {
        boolean ret_init = yolov8Ncnn.loadModel(getAssets(), 0, 1, 0,0);
        if (!ret_init) {
            Log.e("NhanDienNguoiThanActivity", "yolov8ncnn loadModel failed");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        yolov8Ncnn.setOutputWindow(holder.getSurface());


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
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

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }
}