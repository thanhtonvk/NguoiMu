package com.tondz.nguoimu;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.tondz.nguoimu.databinding.ActivityCamDiecBinding;

import java.util.Locale;

public class CamDiecActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final int REQUEST_CAMERA = 1345;
    ActivityCamDiecBinding binding;
    NguoiMuSDK nguoiMuSDK = new NguoiMuSDK();
    private int facing = 0;
    private boolean canPlaySound = true;
    Handler handler;
    Runnable runnable;
    MediaPlayer mediaPlayer;
    TextToSpeech speak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCamDiecBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        init();
        onClick();
        reload();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                canPlaySound = true;
            }
        };
        getObject();
    }

    private void getObject() {
        new Thread(() -> {
            while (true) {
                String dataDeaf = nguoiMuSDK.getDeaf();
                if (!dataDeaf.isEmpty()) {


                    String deafScore = dataDeaf.split("#")[0];
                    String emotion = getEmotion(dataDeaf.split("#")[1]);


                    int deaf = getDeaf(deafScore);
                    String cuChi = getSource(emotion, deaf);
                    if (canPlaySound && !cuChi.isEmpty()) {
                        speak.speak(cuChi, TextToSpeech.QUEUE_FLUSH, null, null);
                        canPlaySound = false;
                        handler.postDelayed(runnable, 100);
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

    private String getSource(String emotion, int deaf) {
        String source = "";
////        {
////           0: "cảm ơn",1: "hẹn gặp lại",2: "khỏe", 3: "không thích",4: "rất vui được gặp bạn",5: "sợ", 6:"tạm biệt",
////                  7:  "thích",8: "xin chào",9: "xin lỗi", 10:"biết",11: "anh trai", 12:"chị gái", 13:"hiểu",14: "mẹ", 15:"nhà",
////                  16:  "nhớ",17: "tò mò",18: "yêu"
////        };
        if (emotion.equalsIgnoreCase("sợ") && deaf == 5) {
            source = Common.classNames[deaf];
        } else if (emotion.equalsIgnoreCase("vui vẻ") && deaf == 4) {
            source = Common.classNames[deaf];
        } else if (emotion.equalsIgnoreCase("vui vẻ") && deaf == 6) {
            source = Common.classNames[deaf];
        } else if (emotion.equalsIgnoreCase("tức giận") && deaf == 3) {
            source = Common.classNames[deaf];
        } else if (emotion.equalsIgnoreCase("vui vẻ") && deaf == 0) {
            source = Common.classNames[deaf];
        } else if (emotion.equalsIgnoreCase("vui vẻ") && deaf == 2) {
            source = Common.classNames[deaf];
        } else if (emotion.equalsIgnoreCase("vui vẻ") && deaf == 7) {
            source = Common.classNames[deaf];
        } else if (emotion.equalsIgnoreCase("buồn") && deaf == 9) {
            source = Common.classNames[deaf];
        } else if (emotion.equalsIgnoreCase("tự nhiên") && deaf == 1) {
            source = Common.classNames[deaf];
        } else if (emotion.equalsIgnoreCase("tự nhiên") && deaf == 8) {
            source = Common.classNames[deaf];
        } else if (emotion.equalsIgnoreCase("buồn") && deaf == 6) {
            source = Common.classNames[deaf];
        }
        return source;
    }

    private void onClick() {
        binding.btnChangeCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int new_facing = 1 - facing;
                nguoiMuSDK.closeCamera();
                nguoiMuSDK.openCamera(new_facing);
                facing = new_facing;
            }
        });
        binding.btnVi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.classNames = new String[]{
                        "cảm ơn", "hẹn gặp lại", "khỏe", "không thích", "rất vui được gặp bạn", "sợ", "tạm biệt",
                        "thích", "xin chào", "xin lỗi", "biết", "anh trai", "chị gái", "hiểu", "mẹ", "nhà",
                        "nhớ", "tò mò", "yêu"
                };
                speak = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if (i != TextToSpeech.ERROR) {
                            speak.setLanguage(Locale.forLanguageTag("vi-VN"));
                        }
                    }
                });
            }
        });
        binding.btnEn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.classNames = new String[]{"thank you", "see you later", "fine", "don't like", "nice to meet you", "scared", "goodbye",
                        "like", "hello", "sorry", "know", "brother", "sister", "understand", "mother", "home",
                        "miss", "curious", "love"};
                speak = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if (i != TextToSpeech.ERROR) {
                            speak.setLanguage(Locale.ENGLISH);
                        }
                    }
                });
            }
        });
        binding.btnCn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.classNames = new String[]{"谢谢", "待会儿见", "好吧", "不喜欢", "很高兴见到你", "害怕", "再见",
                        "喜欢", "你好", "对不起", "知道", "哥哥", "姐姐", "理解", "妈妈", "家",
                        "想念", "好奇", "爱"};
                speak = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if (i != TextToSpeech.ERROR) {
                            speak.setLanguage(Locale.CHINA);
                        }
                    }
                });
            }
        });

    }

    private int getDeaf(String scoreDeaf) {
        String[] arr = scoreDeaf.split(" ");
        return Integer.parseInt(arr[0]);
    }

    private String getEmotion(String scoreEmotion) {
        String[] arr = scoreEmotion.split(",");
        float maxScore = 0;
        int maxIdx = 0;
        for (int i = 0; i < arr.length; i++) {
            float score = Float.parseFloat(arr[i]);
            if (score > maxScore) {
                maxScore = score;
                maxIdx = i;
            }
        }
        return Common.emotionClasses[maxIdx];
    }

    private void init() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding.cameraview.getHolder().setFormat(PixelFormat.RGBA_8888);
        binding.cameraview.getHolder().addCallback(this);
        Common.classNames = new String[]{
                "cảm ơn", "hẹn gặp lại", "khỏe", "không thích", "rất vui được gặp bạn", "sợ", "tạm biệt",
                "thích", "xin chào", "xin lỗi", "biết", "anh trai", "chị gái", "hiểu", "mẹ", "nhà",
                "nhớ", "tò mò", "yêu"
        };
        speak = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    speak.setLanguage(Locale.forLanguageTag("vi-VN"));
                }
            }
        });
    }

    private void reload() {
        boolean ret_init = nguoiMuSDK.loadModel(getAssets(), 0, 0, 0, 1, 0);
        if (!ret_init) {
            Log.e("NhanDienNguoiThanActivity", "yolov8ncnn loadModel failed");
        } else {
            Log.e("NhanDienNguoiThanActivity", "yolov8ncnn loadModel ok");
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        nguoiMuSDK.setOutputWindow(holder.getSurface());
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
        nguoiMuSDK.openCamera(facing);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nguoiMuSDK.closeCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nguoiMuSDK.closeCamera();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
