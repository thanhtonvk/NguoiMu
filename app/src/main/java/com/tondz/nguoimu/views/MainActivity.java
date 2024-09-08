package com.tondz.nguoimu.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tondz.nguoimu.NguoiMuSDK;
import com.tondz.nguoimu.R;
import com.tondz.nguoimu.database.DBContext;
import com.tondz.nguoimu.models.NguoiThan;
import com.tondz.nguoimu.utils.CalDistance;
import com.tondz.nguoimu.utils.Common;

import java.sql.Time;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    boolean isPass = false;
    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String PASSWORD = "12345";
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference();

        reference.child("pass").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue().toString().equals(PASSWORD)) {
                    isPass = true;
                } else {
                    Toast.makeText(getApplicationContext(), "Đã hết thời gian dùng thử", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                finish();
            }
        });
        findViewById(R.id.btnDoDuong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPass) {
                    startActivity(new Intent(getApplicationContext(), DoDuongActivity.class));
                }

            }
        });
        findViewById(R.id.btnNhanDienNguoi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPass) {
                    startActivity(new Intent(getApplicationContext(), NhanDienNguoiThanActivity.class));
                }

            }
        });
        findViewById(R.id.btnGoiDien).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechQuaySo();
            }
        });
        findViewById(R.id.btnGoiDanhBa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechDanhBa();
            }
        });
        checkPermissions();
        loadWidthInImages();
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.forLanguageTag("vi-VN"));
                }
            }
        });
    }

    private static final int REQUEST_QUAY_SO = 1000;
    private static final int REQUEST_DANH_BA = 1001;
    private static final int REQUEST_CODE_PERMISSION = 12000;

    private void speechDanhBa() {
        Intent intent
                = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

        try {
            startActivityForResult(intent, REQUEST_DANH_BA);
        } catch (Exception e) {
            Toast.makeText(this, "Thiết bị không hỗ trợ tính năng này", Toast.LENGTH_SHORT).show();
        }
    }

    private void speechQuaySo() {
        Intent intent
                = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

        try {
            startActivityForResult(intent, REQUEST_QUAY_SO);
        } catch (Exception e) {
            Toast.makeText(this, "Thiết bị không hỗ trợ tính năng này", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_QUAY_SO && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String sdt = result.get(0);
                sdt = keepOnlyDigits(sdt).trim();
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + sdt));
                startActivity(intent);

            }
        }
        if (requestCode == REQUEST_DANH_BA && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String text = result.get(0);
                findContacts(text);

            }
        }
    }


    @SuppressLint("Range")
    public void findContacts(String text) {
        ContentResolver contentResolver = getContentResolver();
        boolean ok = false;
        // Truy vấn tới danh sách liên lạc
        Cursor cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                // Lấy ID của danh bạ
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                // Lấy tên của danh bạ
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                // Kiểm tra xem danh bạ có số điện thoại không
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    // Truy vấn tới số điện thoại của danh bạ
                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);

                    while (phoneCursor.moveToNext()) {
                        // Lấy số điện thoại
                        String phoneNumber = phoneCursor.getString(
                                phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        text = removeAccent(text).trim().toLowerCase();
                        phoneNumber = keepOnlyDigits(phoneNumber).trim();
                        name = removeAccent(name).trim().toLowerCase();

                        if (name.contains(text)) {
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setData(Uri.parse("tel:" + phoneNumber));
                            startActivity(intent);
                            ok = true;
                            Log.d("Contact", "Name: " + name + ", Phone Number: " + phoneNumber);
                            break;
                        }

                    }
                    phoneCursor.close();
                }
            }
            if (!ok) {
                Toast.makeText(this, "Không tìm thấy danh bạ", Toast.LENGTH_SHORT).show();
                textToSpeech.speak("Không tìm thấy người liên lạc trong danh bạ, hãy thử lại", TextToSpeech.QUEUE_FLUSH, null);
            }
            cursor.close();
        }
    }

    public String keepOnlyDigits(String input) {
        // Biểu thức chính quy để giữ lại chỉ các chữ số (0-9)
        return input.replaceAll("[^\\d]", "");
    }

    public String removeAccent(String s) {
        // Normalize văn bản để chuyển các ký tự có dấu thành dạng ký tự tổ hợp
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);

        // Loại bỏ các dấu bằng cách loại bỏ các ký tự không phải ASCII
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D");
    }

    private void loadWidthInImages() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String widthInImages = preferences.getString("widthInImages", "");
        if (!widthInImages.equalsIgnoreCase("")) {
            String[] arr = widthInImages.split(",");
            for (int i = 0; i < arr.length; i++) {
                CalDistance.widthInImages[i] = Double.parseDouble(arr[i]);
            }
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 100);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
        }


    }
}
