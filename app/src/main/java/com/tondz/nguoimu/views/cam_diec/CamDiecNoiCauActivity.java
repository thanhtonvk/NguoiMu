package com.tondz.nguoimu.views.cam_diec;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.tondz.nguoimu.databinding.ActivityCamDiecNoiCauBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CamDiecNoiCauActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final int REQUEST_CAMERA = 1345;
    ActivityCamDiecNoiCauBinding binding;
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
        binding = ActivityCamDiecNoiCauBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        init();
        onClick();
        reload();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                canPlaySound = true;
                noiCauTemp1.clear();
                noiCauTemp2.clear();
                finalString.clear();
                binding.tvNoiDung.setText("");
            }
        };
        getObject();
    }

    List<List<Integer>> noiCauTemp1 = new ArrayList<>();
    List<List<Integer>> noiCauTemp2 = new ArrayList<>();
    List<String> finalString = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Handler backgroundHandler = new Handler(Looper.getMainLooper());
    private boolean isRunning = true;
    private boolean isSpeaking = false; // Đánh dấu đang đọc
    Runnable clearFinalStringRunnable = () -> {
        finalString.clear();
        binding.tvNoiDung.setText("");
        canPlaySound = true; // Cho phép đọc tiếp
        isSpeaking = false;
    };

    private void getObject() {


        new Thread(() -> {
            while (isRunning) {
                try {
                    // Nếu đang đọc thì không nhận thêm cử chỉ mới
                    if (isSpeaking) {
                        Thread.sleep(100);
                        continue;
                    }

                    String dataDeaf = nguoiMuSDK.getDeaf();
                    if (dataDeaf.isEmpty()) {
                        Thread.sleep(10);
                        continue;
                    }

                    int deaf = getDeaf(dataDeaf);
                    String cuChi = getSource("", deaf);
                    if (cuChi.isEmpty()) {
                        Thread.sleep(10);
                        continue;
                    }

                    if (!finalString.contains(cuChi)) {
                        mainHandler.post(() -> addToFinalString(cuChi));
                    }

                    // Xóa sau 10 giây nếu không có hành động mới
                    backgroundHandler.removeCallbacks(clearFinalStringRunnable);
                    backgroundHandler.postDelayed(clearFinalStringRunnable, 5000);

                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    isRunning = false;
                }
            }
        }).start();
    }

    private void addToFinalString(String cuChi) {
        finalString.add(cuChi);
        binding.tvNoiDung.setText(String.join(" ", finalString));

        // Khi đủ 3 câu, tạm dừng nhận cử chỉ 1 giây rồi đọc
        if (finalString.size() == 3 && canPlaySound) {
            isSpeaking = true;
            canPlaySound = false;

            mainHandler.postDelayed(() -> {
                speak.speak(String.join(" ", finalString), TextToSpeech.QUEUE_FLUSH, null, null);
            }, 1000); // Đợi 1 giây trước khi đọc
            speak.setOnUtteranceCompletedListener(s -> {
                mainHandler.post(clearFinalStringRunnable);
            });

        }
    }


    private String getSource(String emotion, int deaf) {
        String source = "";
        if (deaf < 33) {
            source = Common.classNames[deaf];
        }

        return source;
    }

    private void onClick() {
        if (speak != null) {
            speak.stop();  // Dừng đọc nếu đang nói
            speak.shutdown();  // Giải phóng tài nguyên cũ
        }
        binding.tvNoiDung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noiCauTemp1.clear();
                noiCauTemp2.clear();
                finalString.clear();
                binding.tvNoiDung.setText("");
            }
        });
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
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
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