package com.tondz.nguoimu.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class DoDuongActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final int REQUEST_MIC = 1235;
    NguoiMuSDK yolov8Ncnn = new NguoiMuSDK();
    private SurfaceView cameraView;
    private static final int REQUEST_CAMERA = 510;
    TextToSpeech textToSpeech;
    DBContext dbContext;
    Handler handler;
    Runnable runnable;
    boolean objectCanPlaySound = true;
    boolean personCanPlaySound = true;
    ImageView btnDoiCamera;
    private int facing = 0;
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
                if (!objectCanPlaySound) {
                    objectCanPlaySound = true;
                }
                if (!personCanPlaySound) {
                    personCanPlaySound = true;
                }

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
        findViewById(R.id.btnCanChinh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CanChinhKhoangCachActivity.class));
                finish();
            }
        });
        findViewById(R.id.btnThem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ThemNguoiThanActivity.class));
            }
        });

    }

    NguoiThan nguoiThan = null;

    private void getObject() {
        new Thread(() -> {
            while (true) {
                List<String> stringList = yolov8Ncnn.getListResult();
                if (!stringList.isEmpty()) {
                    if (objectCanPlaySound) {
                        countObject(stringList);
                        for (String result : stringList
                        ) {
                            speakObject(result);
                            break;
                        }
                        objectCanPlaySound = false;
                        handler.postDelayed(runnable, 5000);
                    }

                }

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
                        if (personCanPlaySound) {
                            speakNguoiThan(nguoiThan);
                            tvName.setText(nguoiThan.getTen());
                            personCanPlaySound = false;
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Intent intent
                    = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

            try {
                startActivityForResult(intent, REQUEST_MIC);
            } catch (Exception e) {
                Toast.makeText(DoDuongActivity.this, "Thiết bị không hỗ trợ tính năng này", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            textToSpeech.speak("nhận diện", TextToSpeech.QUEUE_FLUSH, null);
            startActivity(new Intent(getApplicationContext(), DoDuongActivity.class));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void actionMic(String text) {
        if (text.toLowerCase().contains("doi")) {
            int new_facing = 1 - facing;
            yolov8Ncnn.closeCamera();
            yolov8Ncnn.openCamera(new_facing);
            facing = new_facing;
        }
        if (text.toLowerCase().contains("chinh")) {
            startActivity(new Intent(getApplicationContext(), CanChinhKhoangCachActivity.class));
            finish();
        }
        if (text.toLowerCase().contains("them")) {
            startActivity(new Intent(getApplicationContext(), ThemNguoiThanActivity.class));
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MIC && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String text = result.get(0);
                text = removeAccent(text);
                actionMic(text);
            }
        }
    }

    public String removeAccent(String s) {
        // Normalize văn bản để chuyển các ký tự có dấu thành dạng ký tự tổ hợp
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);

        // Loại bỏ các dấu bằng cách loại bỏ các ký tự không phải ASCII
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D");
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

    ImageView imgView;
    TextView tvName;

    private void init() {
        btnDoiCamera = findViewById(R.id.btnChangeCamera);
        cameraView = findViewById(R.id.cameraview);


        cameraView.getHolder().addCallback(this);
        tvKhoangCach = findViewById(R.id.tvKc);
        imgView = findViewById(R.id.imgView);
        tvName = findViewById(R.id.tvName);
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
        tvKhoangCach.post(new Runnable() {
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
        boolean ret_init = yolov8Ncnn.loadModel(getAssets(), 1, 1, 1);
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
