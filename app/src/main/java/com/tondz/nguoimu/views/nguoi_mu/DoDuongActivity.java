package com.tondz.nguoimu.views;

import android.Manifest;
import android.annotation.SuppressLint;
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
import com.tondz.nguoimu.utils.CalDistance;
import com.tondz.nguoimu.utils.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DoDuongActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    NguoiMuSDK yolov8Ncnn = new NguoiMuSDK();
    private SurfaceView cameraView;
    private static final int REQUEST_CAMERA = 510;
    TextToSpeech textToSpeech;
    DBContext dbContext;
    Handler handler;
    Runnable runnable;
    private boolean canPlaySound = true;
    private int facing = 1;
    TextView tvKhoangCach;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_duong);
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
        findViewById(R.id.btnChangeCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int new_facing = 1 - facing;
                yolov8Ncnn.closeCamera();
                yolov8Ncnn.openCamera(new_facing);
                facing = new_facing;
            }
        });
        findViewById(R.id.btnCanChinh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CanChinhKhoangCachActivity.class));
                finish();
            }
        });

    }

    private void getObject() {
        new Thread(() -> {
            while (true) {
                List<String> stringList = yolov8Ncnn.getListResult();
                if (!stringList.isEmpty()) {
                    if (canPlaySound) {
                        countObject(stringList);
                        for (String result : stringList
                        ) {
                            speakObject(result);
                        }
                        canPlaySound = false;
                        handler.postDelayed(runnable, 5000);
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
        cameraView = findViewById(R.id.cameraview);


        cameraView.getHolder().addCallback(this);
        tvKhoangCach = findViewById(R.id.tvKc);

        dbContext = new DBContext(DoDuongActivity.this);
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


    @SuppressLint("DefaultLocale")
    private void speakObject(String text) {
        String[] arr = text.split(" ");
        int label = Integer.parseInt(arr[0]);
        double x = Double.parseDouble(arr[1]);
        double y = Double.parseDouble(arr[2]);
        double w = Double.parseDouble(arr[3]);
        double h = Double.parseDouble(arr[4]);
        double focalLength = CalDistance.calculateFocalLength(CalDistance.knownDistances[label], CalDistance.knownWidths[label],
                CalDistance.widthInImages[label]);
        double distance = CalDistance.calculateDistance(CalDistance.knownWidths[label], focalLength, w);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvKhoangCach.setText(String.format("Khoảng cách %,.2fm", distance));
            }
        });

        String name = Common.listObject[label];
        double[] position = Common.xywhToCenter(x, y, w, h);
        double centerX = position[0];
        double centerY = position[1];
        String speaking = "";
        //top
        if (100 < centerX && centerX < 200 && 0 < centerY && centerY < 200) {
            speaking = name + " " + "đang ở trên";
        }
        //right
        if (200 < centerX && centerX < 320 && 200 < centerY && centerY < 400) {
            speaking = name + " " + "đang ở bên phải";
        }
        //bottom
        if (100 < centerX && centerX < 200 && 400 < centerY && centerY < 640) {
            speaking = name + " " + "đang ở dưới";
        }
        //left
        if (0 < centerX && centerX < 100 && 200 < centerY && centerY < 400) {
            speaking = name + " " + "đang ở bên trái";
        }
        //top right
        if (200 < centerX && centerX < 320 && 0 < centerY && centerY < 200) {
            speaking = name + " " + "đang ở trên bên phải";
        }
        // bottom right
        if (200 < centerX && centerX < 320 && 400 < centerY && centerY < 640) {
            speaking = name + " " + "đang ở dưới bên phải";
        }
        //bottom left
        if (0 < centerX && centerX < 100 && 400 < centerY && centerY < 640) {
            speaking = name + " " + "đang ở dưới bên trái";
        }
        //top left
        if (0 < centerX && centerX < 100 && 0 < centerY && centerY < 200) {
            speaking = name + " " + "đang ở trên bên trái";
        }

        if (100 < centerX && centerX < 200 && 200 < centerY && centerY < 400) {
            speaking = name + " " + "đang ở giữa ";
        }
        @SuppressLint("DefaultLocale") String valDistance = String.format("%.2f", distance);
        speaking += valDistance + " met";
        if (label == 9) {
            String lightTraffic = yolov8Ncnn.getLightTraffic();
            if (!lightTraffic.trim().isEmpty()) {
                String resultLight = predictLight(lightTraffic);
                String content = "đèn giao thông đang " + resultLight;
                textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        textToSpeech.speak(speaking, TextToSpeech.QUEUE_FLUSH, null);


        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private String predictLight(String result) {
        String arr[] = result.trim().split(",");
        int maxIdx = -1;
        double maxScore = 0;
        for (int i = 0; i < arr.length; i++) {
            double score = Double.parseDouble(arr[i]);
            if (score > maxScore) {
                maxScore = score;
                maxIdx = i;
            }
        }
        return Common.lightTraffic[maxIdx];

    }

    private void reload() {
        boolean ret_init = yolov8Ncnn.loadModel(getAssets(), 1, 0, 1, 0, 0);
        if (!ret_init) {
            Log.e("DoDuongActivity", "yolov8ncnn loadModel failed");
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
