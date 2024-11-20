package com.tondz.nguoimu.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.tondz.nguoimu.NguoiMuSDK;
import com.tondz.nguoimu.R;
import com.tondz.nguoimu.database.DBContext;
import com.tondz.nguoimu.models.NguoiThan;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MauSacActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    NguoiMuSDK yolov8Ncnn = new NguoiMuSDK();
    private SurfaceView cameraView;
    private static final int REQUEST_CAMERA = 510;
    TextToSpeech textToSpeech;

    Button btnDoiCamera;
    private int facing = 1;
    TextRecognizer recognizer;
    private boolean isSpeaking = false; // Trạng thái đọc nội dung

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mau_sac);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        checkPermissions();
        reload();
        onClick();
    }

    private void init() {
        btnDoiCamera = findViewById(R.id.btnChangeCamera);
        cameraView = findViewById(R.id.cameraview);
        cameraView.getHolder().addCallback(this);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.forLanguageTag("vi-VN"));
                }
            }
        });
    }

    private void handleTouch() {
        if (isSpeaking) {
            textToSpeech.stop();
            isSpeaking = false;
            return;
        }
        Bitmap screenshot = yolov8Ncnn.getImage();
        recognizeTextFromImage(screenshot);
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

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleTouch();
        }
        return super.onTouchEvent(event);
    }

    private void readContent(String content) {
        isSpeaking = true;
        textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    public void recognizeTextFromImage(Bitmap bitmap) {
        processImage(bitmap);
    }


    private void processImage(Bitmap bitmap) {
        Map<String, Integer> detectedColors = detectColors(bitmap);
        displayDetectedColors(detectedColors);
    }

    // Hàm phân tích màu trong toàn bộ ảnh
    private Map<String, Integer> detectColors(Bitmap bitmap) {
        Map<String, Integer> colorCount = new HashMap<>();

        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                int pixel = bitmap.getPixel(x, y);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                String colorName = classifyColorHSV(red, green, blue);
                colorCount.put(colorName, colorCount.getOrDefault(colorName, 0) + 1);
            }
        }

        return colorCount;
    }

    private String getMostFrequentColor(Map<String, Integer> colorCount) {
        String mostFrequentColor = "Unknown";
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : colorCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequentColor = entry.getKey();
            }
        }

        return mostFrequentColor;
    }

    // Phân loại màu bằng HSV
    private String classifyColorHSV(int red, int green, int blue) {
        float[] hsv = new float[3];
        Color.RGBToHSV(red, green, blue, hsv);

        float hue = hsv[0]; // Giá trị Hue
        float saturation = hsv[1]; // Độ bão hòa
        float value = hsv[2]; // Độ sáng

        if (saturation < 0.2 && value > 0.8) {
            return "Trắng";
        } else if (value < 0.2) {
            return "Đen";
        } else if (hue >= 0 && hue <= 30 || hue >= 330 && hue <= 360) {
            return "Đỏ";
        } else if (hue > 30 && hue <= 90) {
            return "Vàng";
        } else if (hue > 90 && hue <= 150) {
            return "Xanh lá";
        } else if (hue > 150 && hue <= 210) {
            return "Xanh lơ";
        } else if (hue > 210 && hue <= 270) {
            return "Xanh nước biển";
        } else if (hue > 270 && hue <= 330) {
            return "Hồng";
        }
        return "Không xác định";
    }

    // Hiển thị kết quả
    private void displayDetectedColors(Map<String, Integer> colorCount) {
        StringBuilder result = new StringBuilder("");
        int count = 0;
        for (Map.Entry<String, Integer> entry : colorCount.entrySet()) {
            result.append(entry.getKey()).append(" ");
        }

        String mostFrequentColor = getMostFrequentColor(colorCount);
        readContent("Màu " + mostFrequentColor);
    }

    private void reload() {
        boolean ret_init = yolov8Ncnn.loadModel(getAssets(), 0, 0, 0);
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