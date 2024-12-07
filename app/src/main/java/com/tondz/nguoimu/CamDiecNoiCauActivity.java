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
import com.tondz.nguoimu.databinding.ActivityCamDiecNoiCauBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    Handler handlerClearString;
    Runnable runnableClearString;

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
    List<List<Integer>> noiCau = new ArrayList<>();

    private void initNoiCau() {
        noiCau.add(new ArrayList<>(List.of(7,0,6)));
        noiCau.add(new ArrayList<>(List.of(0, 2, 4)));
        noiCau.add(new ArrayList<>(List.of(8, 2, 4)));
        noiCau.add(new ArrayList<>(List.of(3, 9, 6)));
        noiCau.add(new ArrayList<>(List.of(11, 16, 15)));
        noiCau.add(new ArrayList<>(List.of(12, 16, 15)));
        noiCau.add(new ArrayList<>(List.of(0, 13, 1)));
        noiCau.add(new ArrayList<>(List.of(0, 3, 6)));
        noiCau.add(new ArrayList<>(List.of(9, 3, 1)));
        noiCau.add(new ArrayList<>(List.of(12, 18, 14)));
        noiCau.add(new ArrayList<>(List.of(17, 18, 11)));
        noiCau.add(new ArrayList<>(List.of(0, 3, 1)));
        noiCau.add(new ArrayList<>(List.of(16, 14, 11)));
        noiCau.add(new ArrayList<>(List.of(16, 11, 12)));
        noiCau.add(new ArrayList<>(List.of(16, 12, 14)));
        noiCau.add(new ArrayList<>(List.of(17, 7, 18)));
        noiCau.add(new ArrayList<>(List.of(17, 18, 12)));


    }

    //           0: "cảm ơn",1: "hẹn gặp lại",2: "khỏe", 3: "không thích",4: "rất vui được gặp bạn",5: "sợ", 6:"tạm biệt",
//           7:  "thích",8: "xin chào",9: "xin lỗi", 10:"biết",11: "anh trai", 12:"chị gái", 13:"hiểu",14: "mẹ", 15:"nhà",
//           16:  "nhớ",17: "tò mò",18: "yêu"
    private void getObject() {
        Handler clearHandler = new Handler(); // Tạo handler để xóa finalString
        Runnable clearFinalStringRunnable = new Runnable() {
            @Override
            public void run() {
                noiCauTemp1.clear();
                noiCauTemp2.clear();
                finalString.clear();
                binding.tvNoiDung.setText("");

            }
        };

        new Thread(() -> {
            while (true) {
                String deafEmotionScore = nguoiMuSDK.getDeaf();
                if (!deafEmotionScore.isEmpty()) {
                    String deafScore = deafEmotionScore;
                    int deaf = getDeaf(deafScore);
                    String cuChi = getSource("", deaf);
                    if (!cuChi.isEmpty()) {
                        if (finalString.isEmpty()) {
                            for (List cumTu : noiCau) {
                                if (Integer.valueOf(deaf).equals(cumTu.get(0))) {
                                    if (!finalString.contains(cuChi)) {
                                        noiCauTemp1.add(cumTu);
                                        finalString.add(cuChi);
                                        StringBuilder temp = new StringBuilder();
                                        for (String i : finalString) {
                                            temp.append(i).append(" ");
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                binding.tvNoiDung.setText(temp);
                                            }
                                        });

                                    }
                                }
                            }
                        }
                        if (finalString.size() == 1) {
                            for (List cumTu : noiCauTemp1) {
                                if (Integer.valueOf(deaf).equals(cumTu.get(1))) {
                                    if (!finalString.contains(cuChi)) {
                                        noiCauTemp2.add(cumTu);
                                        finalString.add(cuChi);

                                        StringBuilder temp = new StringBuilder();
                                        for (String i : finalString) {
                                            temp.append(i).append(" ");
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                binding.tvNoiDung.setText(temp);
                                            }
                                        });

                                    }
                                }
                            }
                        }
                        if (finalString.size() == 2) {
                            for (List cumTu : noiCauTemp2) {
                                if (Integer.valueOf(deaf).equals(cumTu.get(2))) {
                                    if (!finalString.contains(cuChi)) {
                                        finalString.add(cuChi);
                                        StringBuilder temp = new StringBuilder();
                                        for (String i : finalString) {
                                            temp.append(i).append(" ");
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {


                                                binding.tvNoiDung.setText(temp);
                                            }
                                        });

                                    }
                                }
                            }
                        }
                        if (finalString.size() == 3) {
                            if (canPlaySound) {
                                StringBuilder result = new StringBuilder();
                                for (String i : finalString) {
                                    result.append(i).append(" ");
                                }
                                speak.speak(result, TextToSpeech.QUEUE_FLUSH, null, null);
                                canPlaySound = false;
                                handler.postDelayed(runnable, 100);
                            }
                        }
                        clearHandler.removeCallbacks(clearFinalStringRunnable); // Loại bỏ tác vụ trước đó
                        clearHandler.postDelayed(clearFinalStringRunnable, 10000); // Đặt lại sau 10 giây
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
        source = Common.classNames[deaf];
        return source;
//        {
//           0: "cảm ơn",1: "hẹn gặp lại",2: "khỏe", 3: "không thích",4: "rất vui được gặp bạn",5: "sợ", 6:"tạm biệt",
//                  7:  "thích",8: "xin chào",9: "xin lỗi", 10:"biết",11: "anh trai", 12:"chị gái", 13:"hiểu",14: "mẹ", 15:"nhà",
//                  16:  "nhớ",17: "tò mò",18: "yêu"
//        };
//        if (emotion.equalsIgnoreCase("sợ") && deaf == 5) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("vui vẻ") && deaf == 4) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("tức giận") && deaf == 3) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("vui vẻ") && deaf == 0) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("vui vẻ") && deaf == 2) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("vui vẻ") && deaf == 7) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("buồn") && deaf == 9) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("tự nhiên") && deaf == 1) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("tự nhiên") && deaf == 8) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("buồn") && deaf == 6) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("buồn") && deaf == 16) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("tự nhiên") && deaf == 13) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("bất ngờ") && deaf == 17) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("vui vẻ") && deaf == 12) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("tự nhiên") && deaf == 11) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("vui vẻ") && deaf == 14) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("tự nhiên") && deaf == 15) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("vui vẻ") && deaf == 18) {
//            source = Common.classNames[deaf];
//        } else if (emotion.equalsIgnoreCase("tự nhiên") && deaf == 10) {
//            source = Common.classNames[deaf];
//        }
//        return source;
    }

    private void onClick() {
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
                Common.classNames = new String[]{"cảm ơn", "hẹn gặp lại", "khỏe", "không thích", "rất vui được gặp bạn", "sợ", "tạm biệt", "thích", "xin chào", "xin lỗi", "biết", "anh trai", "chị gái", "hiểu", "mẹ", "nhà", "nhớ", "tò mò", "yêu"};
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
                Common.classNames = new String[]{"thank you", "see you later", "fine", "don't like", "nice to meet you", "scared", "goodbye", "like", "hello", "sorry", "know", "brother", "sister", "understand", "mother", "home", "miss", "curious", "love"};
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
                Common.classNames = new String[]{"谢谢", "待会儿见", "好吧", "不喜欢", "很高兴见到你", "害怕", "再见", "喜欢", "你好", "对不起", "知道", "哥哥", "姐姐", "理解", "妈妈", "家", "想念", "好奇", "爱"};
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

        Common.classNames = new String[]{"cảm ơn", "hẹn gặp lại", "khỏe", "không thích", "rất vui được gặp bạn", "sợ", "tạm biệt", "thích", "xin chào", "xin lỗi", "biết", "anh trai", "chị gái", "hiểu", "mẹ", "nhà", "nhớ", "tò mò", "yêu"};
        speak = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    speak.setLanguage(Locale.forLanguageTag("vi-VN"));
                }
            }
        });
        initNoiCau();
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
