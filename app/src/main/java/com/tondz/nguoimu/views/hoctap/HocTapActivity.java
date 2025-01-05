package com.tondz.nguoimu.views.hoctap;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tondz.nguoimu.Common;
import com.tondz.nguoimu.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class HocTapActivity extends AppCompatActivity {
    private TextView tv_CauHoi;
    private Button btn_da1, btn_da2, btn_da3, btn_da4, btn_trove;
    private Random random;
    private int idxHocTap = -1;
    private int dung = 0, sai = 0, entry = 0;
    private TextToSpeech textToSpeech;
    private static final int REQUEST_MIC = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hoc_tap);

        initViews();
        initTextToSpeech();
        shuffleQuestions();
        setupClickListeners();
        nextQuestion();
    }

    private void initViews() {
        tv_CauHoi = findViewById(R.id.tv_cauhoi);
        btn_da1 = findViewById(R.id.btn_da1);
        btn_da2 = findViewById(R.id.btn_da2);
        btn_da3 = findViewById(R.id.btn_da3);
        btn_da4 = findViewById(R.id.btn_da4);
        btn_trove = findViewById(R.id.btn_trove);
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(new Locale("vi", "VN"));
            }
        });
    }

    private void shuffleQuestions() {
        random = new Random();
        Collections.shuffle(Common.cauHoiArrayList);
    }

    private void setupClickListeners() {
        btn_da1.setOnClickListener(view -> handleAnswer(btn_da1.getText().toString()));
        btn_da2.setOnClickListener(view -> handleAnswer(btn_da2.getText().toString()));
        btn_da3.setOnClickListener(view -> handleAnswer(btn_da3.getText().toString()));
        btn_da4.setOnClickListener(view -> handleAnswer(btn_da4.getText().toString()));
        btn_trove.setOnClickListener(view -> finish());
        findViewById(R.id.btnReplay).setOnClickListener(view -> repeatQuestion());
        findViewById(R.id.btnMic).setOnClickListener(view -> startVoiceRecognition());
    }

    private void repeatQuestion() {
        String noiDung = getCurrentQuestionContent();
        speak(noiDung);
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy nói...");
        try {
            startActivityForResult(intent, REQUEST_MIC);
        } catch (Exception e) {
            Toast.makeText(this, "Thiết bị không hỗ trợ tính năng này", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleAnswer(String selectedAnswer) {
        if (Common.CAU_HOI != null && checkAnswer(selectedAnswer)) {
            dung++;
            speak("Chúc mừng bạn đã trả lời đúng");
            delayAndProceed(this::nextQuestion, 3000);
        } else {
            entry++;
            if (entry == 1) {
                speak("Câu trả lời chưa chính xác, bạn hãy chọn lại.");
            } else if (entry == 2) {
                sai++;
                String noiDung = "Câu trả lời chưa chính xác. Đáp án đúng là " + Common.CAU_HOI.getDapan() + ". giải thích. " + Common.CAU_HOI.getGiaithich();
                entry = 0;
                String utteranceId = UUID.randomUUID().toString();
                textToSpeech.speak(noiDung, TextToSpeech.QUEUE_FLUSH, null, utteranceId);

                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                nextQuestion();
                            }
                        });

                    }

                    @Override
                    public void onError(String utteranceId) {
                        runOnUiThread(() -> Toast.makeText(HocTapActivity.this, "Lỗi khi phát âm", Toast.LENGTH_SHORT).show());
                    }
                });

            }
        }
    }

    private boolean checkAnswer(String selectedAnswer) {
        return selectedAnswer.equals(Common.CAU_HOI.getDapan());
    }

    private void nextQuestion() {
        if (++idxHocTap >= Common.cauHoiArrayList.size()) {
            showResultDialog();
        } else {
            Common.CAU_HOI = Common.cauHoiArrayList.get(idxHocTap);
            updateUIForQuestion();
        }
    }

    private void updateUIForQuestion() {
        tv_CauHoi.setText(Common.CAU_HOI.getCauhoi());
        btn_da1.setText(Common.CAU_HOI.getA());
        btn_da2.setText(Common.CAU_HOI.getB());
        btn_da3.setText(Common.CAU_HOI.getC());
        btn_da4.setText(Common.CAU_HOI.getD());
        speak(getCurrentQuestionContent());
    }

    private String getCurrentQuestionContent() {
        return "Câu hỏi số " + (idxHocTap + 1) + ", " + Common.CAU_HOI.getCauhoi()
                + ". Đáp án một, " + Common.CAU_HOI.getA()
                + ". Đáp án hai, " + Common.CAU_HOI.getB()
                + ". Đáp án ba, " + Common.CAU_HOI.getC()
                + ". Đáp án bốn, " + Common.CAU_HOI.getD();
    }

    private void showResultDialog() {
        float diem = (float) dung / (dung + sai) * 10;
        String noiDung = "Bạn đã hoàn thành bài học. Trả lời đúng " + dung + " câu hỏi. "
                + "Trả lời sai " + sai + " câu hỏi. Điểm, " + diem + " điểm.";
        String utteranceId = UUID.randomUUID().toString();
        textToSpeech.speak(noiDung, TextToSpeech.QUEUE_FLUSH, null, utteranceId);

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });

            }

            @Override
            public void onError(String utteranceId) {
                runOnUiThread(() -> Toast.makeText(HocTapActivity.this, "Lỗi khi phát âm", Toast.LENGTH_SHORT).show());
            }
        });
        new AlertDialog.Builder(this)
                .setTitle("Hoàn thành bài kiểm tra")
                .setMessage(noiDung)
                .setPositiveButton("Thoát", (dialog, which) -> finish())
                .create()
                .show();


    }

    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void delayAndProceed(Runnable action, int delayMillis) {
        new Handler(Looper.getMainLooper()).postDelayed(action, delayMillis);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MIC && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0).toLowerCase(Locale.ROOT);
                if (spokenText.contains("đọc")) {
                    repeatQuestion();
                } else {
                    processSpokenAnswer(spokenText);
                }
            }
        }
    }

    private void processSpokenAnswer(String spokenText) {
        if (spokenText.contains("1") || spokenText.contains("một")) {
            btn_da1.performClick();
        } else if (spokenText.contains("2") || spokenText.contains("hai")) {
            btn_da2.performClick();
        } else if (spokenText.contains("3") || spokenText.contains("ba")) {
            btn_da3.performClick();
        } else if (spokenText.contains("4") || spokenText.contains("bốn")) {
            btn_da4.performClick();
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}


