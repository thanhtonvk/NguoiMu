package com.tondz.nguoimu.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.tondz.nguoimu.NguoiMuSDK;
import com.tondz.nguoimu.R;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;

import java.util.ArrayList;
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
    GenerativeModel gm;
    GenerativeModelFutures model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        checkPermissions();
        reload();
        onClick();
        initModel();
        generateText("Xin chào các bạn");
    }

    private void initModel() {
        gm = new GenerativeModel(/* modelName */ "gemini-1.5-flash", getResources().getString(R.string.api_key));
        model = GenerativeModelFutures.from(gm);
    }

    public static List<String> splitParagraph(String paragraph, int maxWords) {
        List<String> result = new ArrayList<>();

        // Tách đoạn văn thành các câu cơ bản dựa vào dấu câu
        String[] sentences = paragraph.split("(?<=[.!?])\\s+");

        for (String sentence : sentences) {
            String[] words = sentence.split("\\s+");
            StringBuilder chunk = new StringBuilder();
            int wordCount = 0;

            for (String word : words) {
                if (wordCount < maxWords) {
                    chunk.append(word).append(" ");
                    wordCount++;
                } else {
                    result.add(chunk.toString().trim());
                    chunk = new StringBuilder(word).append(" ");
                    wordCount = 1;
                }
            }

            if (chunk.length() > 0) {
                result.add(chunk.toString().trim());
            }
        }

        return result;
    }

    private void generateText(String value) {
        List<String> words = splitParagraph(value, 10);
        for (String word : words
        ) {
            Content content = new Content.Builder()
                    .addText(word)
                    .build();
            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        String resultText = result.getText();
                        Parser parser = Parser.builder().build();
                        Node document = parser.parse(resultText);
                        TextContentRenderer renderer = TextContentRenderer.builder().build();
                        String plainText = renderer.render(document);

                        Log.d("OCR", word);
                        Log.d("EDIT", plainText);

                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                    }
                }, this.getMainExecutor());
            }
        }

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
                            generateText("Sửa lỗi chính tả: " + stringBuilder.toString());
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
        boolean ret_init = yolov8Ncnn.loadModel(getAssets(), 0, 0, 0, 0, 0);
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }
}