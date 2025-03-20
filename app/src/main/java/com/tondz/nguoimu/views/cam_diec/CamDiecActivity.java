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
                String emotionScore = nguoiMuSDK.getEmotion();
                if (!dataDeaf.isEmpty()) {
                    String deafScore = dataDeaf;
                    int deaf = getDeaf(deafScore);
                    String deafValue = Common.classNames[deaf];
                    String emotion = getEmotion(emotionScore);
                    String cuChi = Common.getMatchingGesture(deafValue, emotion);
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
                        "cám ơn", "hẹn gặp lại", "khỏe", "không thích", "rất vui được gặp bạn", "sợ", "tạm biệt", "thích",
                        "xin chào", "xin lỗi", "biết", "anh trai", "chị gái", "hiểu", "mẹ", "nhà", "nhớ", "tò mò", "yêu",
                        "chữ A", "chữ B", "chữ C", "chữ D", "chữ E", "chữ G", "chữ H", "chữ I", "chữ K", "chữ L", "chữ M",
                        "chữ N", "chữ O", "chữ P", "chữ Q", "chữ R", "chữ S", "chữ T", "chữ U", "chữ V", "chữ X", "chữ Y",
                        "dấu chấm", "dấu hỏi", "dấu huyền", "dấu sắc", "dấu ngã", "dấu mũ", "dấu mũ ngược", "chăm sóc",
                        "con người", "công cộng", "công việc", "giống nhau", "giường", "giúp đỡ", "hợp tác", "khám bệnh",
                        "khát nước", "khen", "lắng nghe", "lễ phép", "năn nỉ"
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
                        "thank you", "see you again", "healthy", "don't like", "nice to meet you", "afraid", "goodbye", "like",
                        "hello", "sorry", "know", "older brother", "older sister", "understand", "mother", "home", "miss", "curious", "love",
                        "letter A", "letter B", "letter C", "letter D", "letter E", "letter G", "letter H", "letter I", "letter K", "letter L", "letter M",
                        "letter N", "letter O", "letter P", "letter Q", "letter R", "letter S", "letter T", "letter U", "letter V", "letter X", "letter Y",
                        "period", "question mark", "grave accent", "acute accent", "tilde", "circumflex", "inverted circumflex", "take care",
                        "human", "public", "work", "similar", "bed", "help", "cooperate", "medical checkup",
                        "thirsty", "praise", "listen", "polite", "plead"
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
                        "谢谢", "再见", "健康", "不喜欢", "很高兴见到你", "害怕", "告别", "喜欢",
                        "你好", "对不起", "知道", "哥哥", "姐姐", "理解", "妈妈", "家", "想念", "好奇", "爱",
                        "字母A", "字母B", "字母C", "字母D", "字母E", "字母G", "字母H", "字母I", "字母K", "字母L", "字母M",
                        "字母N", "字母O", "字母P", "字母Q", "字母R", "字母S", "字母T", "字母U", "字母V", "字母X", "字母Y",
                        "句号", "问号", "沉音", "升调", "波浪音", "帽子音", "倒帽子音", "照顾",
                        "人类", "公共", "工作", "相似", "床", "帮助", "合作", "体检",
                        "口渴", "称赞", "倾听", "礼貌", "恳求"
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
                "cám ơn", "hẹn gặp lại", "khỏe", "không thích", "rất vui được gặp bạn", "sợ", "tạm biệt", "thích",
                "xin chào", "xin lỗi", "biết", "anh trai", "chị gái", "hiểu", "mẹ", "nhà", "nhớ", "tò mò", "yêu",
                "chữ A", "chữ B", "chữ C", "chữ D", "chữ E", "chữ G", "chữ H", "chữ I", "chữ K", "chữ L", "chữ M",
                "chữ N", "chữ O", "chữ P", "chữ Q", "chữ R", "chữ S", "chữ T", "chữ U", "chữ V", "chữ X", "chữ Y",
                "dấu chấm", "dấu hỏi", "dấu huyền", "dấu sắc", "dấu ngã", "dấu mũ", "dấu mũ ngược", "chăm sóc",
                "con người", "công cộng", "công việc", "giống nhau", "giường", "giúp đỡ", "hợp tác", "khám bệnh",
                "khát nước", "khen", "lắng nghe", "lễ phép", "năn nỉ"
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
        boolean ret_init = nguoiMuSDK.loadModel(getAssets(), 0, 0, 0, 1, 0, 0);
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