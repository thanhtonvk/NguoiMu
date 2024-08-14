package com.tondz.nguoimu.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tondz.nguoimu.NguoiMuSDK;
import com.tondz.nguoimu.R;
import com.tondz.nguoimu.database.DBContext;
import com.tondz.nguoimu.models.NguoiThan;
import com.tondz.nguoimu.utils.Common;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    NguoiMuSDK yolov8Ncnn = new NguoiMuSDK();
    private SurfaceView cameraView;
    private static final int REQUEST_CAMERA = 510;
    TextToSpeech textToSpeech;
    ImageView imgView;
    DBContext dbContext;
    TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        reload();
        getObject();


    }

    NguoiThan nguoiThan = null;

    private void getObject() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {

                        nguoiThan = null;
                        List<String> stringList = yolov8Ncnn.getListResult();
                        String stringEmb = yolov8Ncnn.getEmbedding();
                        if (!stringEmb.isEmpty()) {
                            nguoiThan = timNguoi(stringEmb);
                            if (nguoiThan != null) {
                                speakNguoiThan(nguoiThan);
                                tvName.setText(nguoiThan.getTen());
                                Thread.sleep(2000);
                            }
                            Bitmap bitmap = yolov8Ncnn.getFaceAlign();
                            if(bitmap!=null){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        imgView.setImageBitmap(bitmap);

                                    }
                                });
                            }


                        } else {
                            if (!stringList.isEmpty()) {
                                for (String result : stringList
                                ) {
                                    speakObject(result);
                                    Thread.sleep(2000);
                                }
                            }
                        }
                        Thread.sleep(1000);

                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        });
        thread.start();
    }

    private void init() {
        cameraView = findViewById(R.id.cameraview);
        imgView = findViewById(R.id.imgView);
        cameraView.getHolder().addCallback(this);
        dbContext = new DBContext(MainActivity.this);
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

    private void speakObject(String text) {
        String[] arr = text.split(" ");

        int label = Integer.parseInt(arr[0]);
        double x = Double.parseDouble(arr[1]);
        double y = Double.parseDouble(arr[2]);
        double w = Double.parseDouble(arr[3]);
        double h = Double.parseDouble(arr[4]);
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
        Log.e("TAG", "speakObject: " + speaking);
        textToSpeech.speak(speaking, TextToSpeech.QUEUE_FLUSH, null);


    }

    private NguoiThan timNguoi(String embedding) {
        NguoiThan result = null;
        double maxScore = 0;
        for (NguoiThan nguoiThan : dbContext.getNguoiThans()
        ) {
            String[] str_target = embedding.split(",");
            double[] target = new double[512];
            for (int i = 0; i < 512; i++) {
                target[i] = Double.parseDouble(str_target[i]);
            }
            String[] str_source = nguoiThan.getEmbedding().split(",");
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
        Log.e("SEARCH EMB", "timNguoi: " + maxScore);
        return result;
    }

    private void reload() {
        boolean ret_init = yolov8Ncnn.loadModel(getAssets(), 0, 0);
        if (!ret_init) {
            Log.e("MainActivity", "yolov8ncnn loadModel failed");
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

        yolov8Ncnn.openCamera(1);
    }

    @Override
    public void onPause() {
        super.onPause();

        yolov8Ncnn.closeCamera();
    }
}
