package com.tondz.nguoimu.views.exam;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tondz.nguoimu.Common;
import com.tondz.nguoimu.ManHinhChinhActivity;
import com.tondz.nguoimu.R;
import com.tondz.nguoimu.models.CauHoi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class CauHoiActivity extends AppCompatActivity {
    TextView tv_cauhoi;
    Button btn_da1, btn_da2, btn_da3, btn_da4, btn_trove;
    Random random;
    int idxCauHoi = -1;
    List<Boolean> traLois = new ArrayList<>();
    List<Integer> chonDapAns = new ArrayList<>();
    TextToSpeech textToSpeech;
    int REQUEST_MIC = 123;
    int dung = 0, sai = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cau_hoi);
        traLois.clear();
        chonDapAns.clear();
        random = new Random();
        init();
        Collections.shuffle(Common.cauHoiArrayList);
        onClick();
        nextCauHoi();
    }

    private void speak(String noiDung) {
        textToSpeech.stop();
        textToSpeech.speak(noiDung, TextToSpeech.QUEUE_FLUSH, null);
    }


    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hoàn thành bài kiểm tra");
        float diem = (float) dung / (dung + sai) * 10;

        builder.setMessage("Trả lời đúng: " + dung + " câu hỏi\nTrả lời sai: " + sai + " câu hỏi\nĐiểm: " + diem + " điểm");
        String noiDung = "Bạn đã hoàn thành bài kiểm tra Trả lời đúng " + dung + " câu hỏi Trả lời sai: " + sai + " câu hỏi Điểm: " + diem + " điểm";
        speak(noiDung);
        // Add Yes button
        builder.setPositiveButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Close the dialog
                finish();
            }
        });

//        builder.setNegativeButton("Xem lại đáp án", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void nextCauHoi() {
        if (idxCauHoi == Common.cauHoiArrayList.size() - 1) {
            showAlertDialog();
            return;
        } else {
            idxCauHoi += 1;
            Common.CAU_HOI = Common.cauHoiArrayList.get(idxCauHoi);
            tv_cauhoi.setText(Common.CAU_HOI.getCauhoi());
            btn_da1.setText(Common.CAU_HOI.getA());
            btn_da2.setText(Common.CAU_HOI.getB());
            btn_da3.setText(Common.CAU_HOI.getC());
            btn_da4.setText(Common.CAU_HOI.getD());
            String noiDung = "Câu hỏi " + Common.CAU_HOI.getCauhoi() + ", Đáp án 1 " + Common.CAU_HOI.getA() + ", Đáp án 2 " + Common.CAU_HOI.getB() + ", Đáp án 3 " + Common.CAU_HOI.getC() + ", Đáp án 4 " + Common.CAU_HOI.getD();
            Log.d("TAG", "nextCauHoi: " + noiDung);
            speak(noiDung);
        }
    }

    private void chonDapAn(String noiDung) {
        if (noiDung.contains("1") || noiDung.contains("một") || noiDung.contains("đáp án 1")) {
            btn_da1.performClick();
        } else if (noiDung.contains("2") || noiDung.contains("hai") || noiDung.contains("đáp án 2")) {
            btn_da2.performClick();
        } else if (noiDung.contains("3") || noiDung.contains("ba") || noiDung.contains("đáp án 3")) {
            btn_da2.performClick();
        } else if (noiDung.contains("4") || noiDung.contains("bốn") || noiDung.contains("đáp án 4")) {
            btn_da2.performClick();
        }
    }

    private void onClick() {
        findViewById(R.id.btnReplay).setOnClickListener(view -> {
            String noiDung = "Câu hỏi: " + Common.CAU_HOI.getCauhoi() + " Đáp án 1: " + Common.CAU_HOI.getA() + " Đáp án 2: " + Common.CAU_HOI.getB() + " Đáp án 3: " + Common.CAU_HOI.getC() + " Đáp án 4: " + Common.CAU_HOI.getD();
            speak(noiDung);
        });
        btn_trove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btn_da1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkDapAn(btn_da1.getText().toString(), Common.CAU_HOI)) {
                    traLois.add(true);
                    dung += 1;
                } else {
                    sai += 1;
                    traLois.add(false);
                }
                nextCauHoi();
            }
        });
        btn_da2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkDapAn(btn_da2.getText().toString(), Common.CAU_HOI)) {
                    dung += 1;
                    traLois.add(true);
                } else {
                    sai += 1;
                    traLois.add(false);

                }
                nextCauHoi();
            }
        });
        btn_da3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkDapAn(btn_da3.getText().toString(), Common.CAU_HOI)) {
                    dung += 1;
                    traLois.add(true);
                } else {
                    sai += 1;
                    traLois.add(false);

                }
                nextCauHoi();
            }
        });
        btn_da4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkDapAn(btn_da4.getText().toString(), Common.CAU_HOI)) {
                    dung += 1;
                    traLois.add(true);
                } else {
                    sai += 1;
                    traLois.add(false);
                }
                nextCauHoi();
            }
        });
        findViewById(R.id.btnMic).setOnClickListener(view -> {
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
                Toast.makeText(CauHoiActivity.this, "Thiết bị không hỗ trợ tính năng này", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MIC && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String text = result.get(0);
                chonDapAn(text);
            }
        }
    }


    private void init() {
        tv_cauhoi = findViewById(R.id.tv_cauhoi);
        btn_da1 = findViewById(R.id.btn_da1);
        btn_da2 = findViewById(R.id.btn_da2);
        btn_da3 = findViewById(R.id.btn_da3);
        btn_da4 = findViewById(R.id.btn_da4);
        btn_trove = findViewById(R.id.btn_trove);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.forLanguageTag("vi-VN"));
                }
            }
        });
    }

    private boolean checkDapAn(String dapanchon, CauHoi cauHoi) {
        if (dapanchon.equalsIgnoreCase(cauHoi.getDapan())) return true;
        else return false;
    }

}