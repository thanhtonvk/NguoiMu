package com.tondz.nguoimu;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    NguoiMuSDK yolov8Ncnn = new NguoiMuSDK();
    private SurfaceView cameraView;
    private static final int REQUEST_CAMERA = 510;
    TextToSpeech textToSpeech;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        reload();
        getObject();


    }

    private void getObject() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        List<String> stringList = yolov8Ncnn.getListResult();
                        if (stringList.isEmpty()) {
                            Log.e("RESULT_DETECTOR", "EMPTY LIST");
                        } else {
                            for (String result : stringList
                            ) {
                                speakObject(result);
                                Thread.sleep(2000);
                            }
                        }
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
        textToSpeech.speak(speaking, TextToSpeech.QUEUE_FLUSH, null);
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

        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }

        yolov8Ncnn.openCamera(0);
    }

    @Override
    public void onPause() {
        super.onPause();

        yolov8Ncnn.closeCamera();
    }
}
