package com.tondz.nguoimu.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.util.List;
import java.util.Locale;

public class OcrActivity extends AppCompatActivity implements SurfaceHolder.Callback {
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
        setContentView(R.layout.activity_ocr);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        checkPermissions();
        reload();
        onClick();
    }

    private void init() {
        recognizer =
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
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

    StringBuilder stringBuilder;

    public void recognizeTextFromImage(Bitmap bitmap) {
        stringBuilder = new StringBuilder();

        InputImage image = InputImage.fromBitmap(bitmap, 0);
        recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        List<Text.TextBlock> blocks = visionText.getTextBlocks();
                        if (blocks.size() == 0) {
                            readContent("không thấy nội dung");
                            return;
                        } else {
                            for (Text.TextBlock block : blocks) {
                                String txt = block.getText();
                                stringBuilder.append(txt);
                            }
                            readContent(stringBuilder.toString());
                        }


                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                readContent("có lỗi xảy ra, vui lòng thử lại");
                            }
                        });
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