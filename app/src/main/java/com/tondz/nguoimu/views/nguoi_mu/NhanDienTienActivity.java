package com.tondz.nguoimu.views.nguoi_mu;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tondz.nguoimu.NguoiMuSDK;
import com.tondz.nguoimu.R;
import com.tondz.nguoimu.database.DBContext;
import com.tondz.nguoimu.utils.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class NhanDienTienActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    NguoiMuSDK yolov8Ncnn = new NguoiMuSDK();
    private SurfaceView cameraView;
    private static final int REQUEST_CAMERA = 510;
    TextToSpeech textToSpeech;
    DBContext dbContext;
    Handler handler;
    Runnable runnable;
    private boolean canPlaySound = true;
    Button btnDoiCamera;
    private int facing = 1;
    TextView tvKhoangCach;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nhan_dien_tien);
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

    private void getObject() {
        new Thread(() -> {
            while (true) {
                List<String> moneyList = yolov8Ncnn.getListMoneyResult();
                if (!moneyList.isEmpty()) {
                    if (canPlaySound) {
                        for (String money : moneyList
                        ) {
                            speakMoney(money);
                            break;
                        }
                        canPlaySound = false;
                        handler.postDelayed(runnable, 3000);
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


        cameraView.getHolder().addCallback(this);
        tvKhoangCach = findViewById(R.id.tvKc);

        dbContext = new DBContext(NhanDienTienActivity.this);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.forLanguageTag("vi-VN"));
                }
            }
        });

    }

    private void countObject(List<String> stringObject) {
        List<String> labels = new ArrayList<>();
        for (String result : stringObject
        ) {
            String[] arr = result.split(" ");
            labels.add(Common.listObject[Integer.parseInt(arr[0])]);
        }
        HashMap<String, Integer> elementCountMap = new HashMap<>();

        // Duyệt qua mảng và đếm số lần xuất hiện của từng phần tử
        for (String str : labels) {
            if (elementCountMap.containsKey(str)) {
                elementCountMap.put(str, elementCountMap.get(str) + 1);
            } else {
                elementCountMap.put(str, 1);
            }
        }
        for (String key : elementCountMap.keySet()) {
            int num_of_value = elementCountMap.get(key);
            if (num_of_value > 1) {
                String speak = "Có " + num_of_value + " " + key;
                textToSpeech.speak(speak, TextToSpeech.QUEUE_FLUSH, null);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    private void speakMoney(String result) {
        Log.e("TAGTOND", "speakMoney: " + result);
        String[] arr = result.split(" ");
        int label = Integer.parseInt(arr[0]);
        double prob = Double.parseDouble(arr[5]);
        if (prob > 0.9) {
            String money = Common.moneys[label];
            if (!textToSpeech.isSpeaking()) {
                textToSpeech.speak(money, TextToSpeech.QUEUE_FLUSH, null);
            }
        }

    }

    private void reload() {
        boolean ret_init = yolov8Ncnn.loadModel(getAssets(), 0, 0, 0, 0, 1);
        if (!ret_init) {
            Log.e("NhanDienTienActivity", "yolov8ncnn loadModel failed");
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
