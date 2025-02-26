package com.tondz.nguoimu.views.nguoi_mu;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tondz.nguoimu.Common;
import com.tondz.nguoimu.R;

import java.util.ArrayList;
import java.util.Locale;

public class ReviewActivity extends AppCompatActivity {
    TextView tvCauHoi, tvDa1, tvDa2, tvDa3, tvDa4;
    ImageView btnMic, btnReplay, btnPrev, btnNext;
    private TextToSpeech textToSpeech;
    private static final int REQUEST_MIC = 123;
    int idxReview = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        init();
        initTextToSpeech();
        onClick();
        nextQuestion();
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(new Locale("vi", "VN"));
            }
        });
    }

    private String getCurrentQuestionContent() {
        return "Câu hỏi số " + (idxReview + 1) + ", " + Common.CAU_HOI.getCauhoi()
                + ". Đáp án một, " + Common.CAU_HOI.getA()
                + ". Đáp án hai, " + Common.CAU_HOI.getB()
                + ". Đáp án ba, " + Common.CAU_HOI.getC()
                + ". Đáp án bốn, " + Common.CAU_HOI.getD()
                + ". Bạn chọn đáp án " + Common.dapanChon.get(idxReview)
                + ". Đáp án đúng, " + Common.CAU_HOI.getDapan()
                + ". Giải thích, " + Common.CAU_HOI.getGiaithich();

    }

    private void updateUIForQuestion() {
        tvCauHoi.setText(Common.CAU_HOI.getCauhoi());
        tvDa1.setText(Common.CAU_HOI.getA());
        tvDa2.setText(Common.CAU_HOI.getB());
        tvDa3.setText(Common.CAU_HOI.getC());
        tvDa4.setText(Common.CAU_HOI.getD());
        speak(getCurrentQuestionContent());
    }

    private void onClick() {
        btnNext.setOnClickListener(view -> {
            nextQuestion();
        });
        btnPrev.setOnClickListener(view -> {
            prevQuestion();
        });
        btnReplay.setOnClickListener(view -> {
            repeatQuestion();
        });
        btnMic.setOnClickListener(view -> {
            startVoiceRecognition();
        });
        findViewById(R.id.btn_trove).setOnClickListener(view -> {
            finish();
        });
    }

    private void repeatQuestion() {
        String noiDung = getCurrentQuestionContent();
        speak(noiDung);
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
                } else if (spokenText.contains("sau") || spokenText.contains("tiếp")) {
                    nextQuestion();
                } else if (spokenText.contains("trước")) {
                    prevQuestion();
                }
            }
        }
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

    private void prevQuestion() {
        if (--idxReview < 0) {
            idxReview += 1;
            return;
        } else {
            Common.CAU_HOI = Common.cauHoiDaTraLoi.get(idxReview);
            updateUIForQuestion();
        }
    }

    private void nextQuestion() {
        if (++idxReview >= Common.cauHoiArrayList.size()) {
            idxReview -= 1;
        } else {
            Common.CAU_HOI = Common.cauHoiDaTraLoi.get(idxReview);
            updateUIForQuestion();
        }
    }

    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void init() {
        tvCauHoi = findViewById(R.id.tv_cauhoi);
        tvDa1 = findViewById(R.id.btn_da1);
        tvDa2 = findViewById(R.id.btn_da2);
        tvDa3 = findViewById(R.id.btn_da3);
        tvDa4 = findViewById(R.id.btn_da4);
        btnMic = findViewById(R.id.btnMic);
        btnReplay = findViewById(R.id.btnReplay);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

    }
}