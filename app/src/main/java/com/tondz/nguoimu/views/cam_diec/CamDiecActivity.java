package com.tondz.nguoimu.views.cam_diec;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tondz.nguoimu.Common;
import com.tondz.nguoimu.NguoiMuSDK;
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
                    String deafScore = dataDeaf;
                    int deaf = getDeaf(deafScore);
                    String cuChi = getSource("emotion", deaf);
                    if (canPlaySound && !cuChi.isEmpty()) {
                        speak.speak(cuChi, TextToSpeech.QUEUE_FLUSH, null, null);
                        canPlaySound = false;
                        handler.postDelayed(runnable, 2000);
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private String getSource(String emotion, int deaf) {
        String source = "";
        if (deaf < 33) {
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
                        "anh trai",
                        "biết",
                        "cảm ơn",
                        "chăm sóc",
                        "chị gái",
                        "con người",
                        "công cộng",
                        "công việc",
                        "giúp đỡ",
                        "giường",
                        "giống nhau",
                        "hẹn gặp lại",
                        "hiểu",
                        "hợp tác",
                        "khám bệnh",
                        "khát nước",
                        "khen",
                        "khỏe",
                        "không thích",
                        "lắng nghe",
                        "lễ phép",
                        "mẹ",
                        "năn nỉ",
                        "nhà",
                        "nhớ",
                        "rất vui được gặp bạn",
                        "sợ",
                        "tạm biệt",
                        "thích",
                        "tò mò",
                        "xin chào",
                        "xin lỗi",
                        "yêu",
                        "a",
                        "ă",
                        "â",
                        "b",
                        "c",
                        "d",
                        "đ",
                        "e",
                        "ê",
                        "g",
                        "h",
                        "i",
                        "k",
                        "l",
                        "m",
                        "n",
                        "o",
                        "ô",
                        "ơ",
                        "p",
                        "q",
                        "r",
                        "s",
                        "t",
                        "u",
                        "ú",
                        "v",
                        "x",
                        "y",
                        "dấu chấm",
                        "dấu hỏi",
                        "dấu huyền",
                        "dấu ngã",
                        "dấu sắc"
                };
                if (speak != null) {
                    speak.stop();  // Dừng đọc nếu đang nói
                    speak.shutdown();  // Giải phóng tài nguyên cũ
                }
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
                Common.classNames = new String[]{
                        "brother",
                        "know",
                        "thank you",
                        "take care",
                        "sister",
                        "human",
                        "public",
                        "job",
                        "help",
                        "bed",
                        "similar",
                        "see you again",
                        "understand",
                        "cooperate",
                        "medical checkup",
                        "thirsty",
                        "praise",
                        "healthy",
                        "dislike",
                        "listen",
                        "polite",
                        "mother",
                        "beg",
                        "house",
                        "miss",
                        "nice to meet you",
                        "afraid",
                        "goodbye",
                        "like",
                        "curious",
                        "hello",
                        "sorry",
                        "love",
                        "a",
                        "ă",
                        "â",
                        "b",
                        "c",
                        "d",
                        "đ",
                        "e",
                        "ê",
                        "g",
                        "h",
                        "i",
                        "k",
                        "l",
                        "m",
                        "n",
                        "o",
                        "ô",
                        "ơ",
                        "p",
                        "q",
                        "r",
                        "s",
                        "t",
                        "u",
                        "ú",
                        "v",
                        "x",
                        "y",
                        "period",
                        "question mark",
                        "grave accent",
                        "tilde",
                        "acute accent"
                };
                if (speak != null) {
                    speak.stop();  // Dừng đọc nếu đang nói
                    speak.shutdown();  // Giải phóng tài nguyên cũ
                }
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
                Common.classNames = new String[]{
                        "哥哥",  // anh trai
                        "知道",  // biết
                        "谢谢",  // cảm ơn
                        "照顾",  // chăm sóc
                        "姐姐",  // chị gái
                        "人类",  // con người
                        "公共",  // công cộng
                        "工作",  // công việc
                        "帮助",  // giúp đỡ
                        "床",  // giường
                        "相似",  // giống nhau
                        "再见",  // hẹn gặp lại
                        "理解",  // hiểu
                        "合作",  // hợp tác
                        "看病",  // khám bệnh
                        "口渴",  // khát nước
                        "称赞",  // khen
                        "健康",  // khỏe
                        "不喜欢",  // không thích
                        "倾听",  // lắng nghe
                        "礼貌",  // lễ phép
                        "妈妈",  // mẹ
                        "恳求",  // năn nỉ
                        "家",  // nhà
                        "想念",  // nhớ
                        "很高兴认识你",  // rất vui được gặp bạn
                        "害怕",  // sợ
                        "再见",  // tạm biệt
                        "喜欢",  // thích
                        "好奇",  // tò mò
                        "你好",  // xin chào
                        "对不起",  // xin lỗi
                        "爱",  // yêu
                        "a",
                        "ă",
                        "â",
                        "b",
                        "c",
                        "d",
                        "đ",
                        "e",
                        "ê",
                        "g",
                        "h",
                        "i",
                        "k",
                        "l",
                        "m",
                        "n",
                        "o",
                        "ô",
                        "ơ",
                        "p",
                        "q",
                        "r",
                        "s",
                        "t",
                        "u",
                        "ú",
                        "v",
                        "x",
                        "y",
                        "句号",  // dấu chấm
                        "问号",  // dấu hỏi
                        "抑音符",  // dấu huyền
                        "波浪号",  // dấu ngã
                        "重音符"   // dấu sắc
                };
                if (speak != null) {
                    speak.stop();  // Dừng đọc nếu đang nói
                    speak.shutdown();  // Giải phóng tài nguyên cũ
                }
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
                "anh trai",
                "biết",
                "cảm ơn",
                "chăm sóc",
                "chị gái",
                "con người",
                "công cộng",
                "công việc",
                "giúp đỡ",
                "giường",
                "giống nhau",
                "hẹn gặp lại",
                "hiểu",
                "hợp tác",
                "khám bệnh",
                "khát nước",
                "khen",
                "khỏe",
                "không thích",
                "lắng nghe",
                "lễ phép",
                "mẹ",
                "năn nỉ",
                "nhà",
                "nhớ",
                "rất vui được gặp bạn",
                "sợ",
                "tạm biệt",
                "thích",
                "tò mò",
                "xin chào",
                "xin lỗi",
                "yêu",
                "a",
                "ă",
                "â",
                "b",
                "c",
                "d",
                "đ",
                "e",
                "ê",
                "g",
                "h",
                "i",
                "k",
                "l",
                "m",
                "n",
                "o",
                "ô",
                "ơ",
                "p",
                "q",
                "r",
                "s",
                "t",
                "u",
                "ú",
                "v",
                "x",
                "y",
                "dấu chấm",
                "dấu hỏi",
                "dấu huyền",
                "dấu ngã",
                "dấu sắc"
        };
        if (speak != null) {
            speak.stop();  // Dừng đọc nếu đang nói
            speak.shutdown();  // Giải phóng tài nguyên cũ
        }
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
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
        nguoiMuSDK.openCamera(facing);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speak != null) {
            speak.stop();  // Dừng đọc nếu đang nói
            speak.shutdown();  // Giải phóng tài nguyên cũ
        }
        nguoiMuSDK.closeCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speak != null) {
            speak.stop();  // Dừng đọc nếu đang nói
            speak.shutdown();  // Giải phóng tài nguyên cũ
        }
        nguoiMuSDK.closeCamera();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}