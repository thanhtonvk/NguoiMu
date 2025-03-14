package com.tondz.nguoimu.views.nguoi_mu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tondz.nguoimu.R;
import com.tondz.nguoimu.utils.CalDistance;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import android.provider.Settings;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class MainActivity extends AppCompatActivity {
    TextToSpeech textToSpeech;
    FirebaseDatabase database;
    DatabaseReference reference;
    Button btnId;
    String lastFiveDigits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissionsIfNecessary();
        loadWidthInImages();
        init();
        onClick();
        getId();
    }

    private void getId() {


        @SuppressLint("HardwareIds") String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (deviceId != null && deviceId.length() >= 5) {
            lastFiveDigits = deviceId.substring(deviceId.length() - 5);
            btnId.setText("ID: " + lastFiveDigits);

        } else {
            System.out.println("Device ID không hợp lệ hoặc quá ngắn.");
        }

    }

    private void init() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.forLanguageTag("vi-VN"));
                }
            }
        });
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("NhanViTri");
        btnId = findViewById(R.id.btnId);
    }

    private void onClick() {
        findViewById(R.id.btnMauSach).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MauSacActivity.class));
            }
        });
        findViewById(R.id.btnDoDuong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                    textToSpeech.speak("Nhận diện thông minh", TextToSpeech.QUEUE_FLUSH, null);
//                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                        startActivity(new Intent(getApplicationContext(), DoDuongActivity.class));
//                    }, 3000);
                startActivity(new Intent(getApplicationContext(), DoDuongActivity.class));


            }
        });
        findViewById(R.id.btnGoiDien).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                textToSpeech.speak("Quay số", TextToSpeech.QUEUE_FLUSH, null);
//                new Handler(Looper.getMainLooper()).postDelayed(() -> {
//
//                }, 3000);
                speechQuaySo();
            }
        });
        findViewById(R.id.btnGoiDanhBa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechDanhBa();
//                textToSpeech.speak("Gọi trong danh bạ", TextToSpeech.QUEUE_FLUSH, null);
//                new Handler(Looper.getMainLooper()).postDelayed(() -> {
//
//                }, 3000);
            }
        });

        findViewById(R.id.btnMic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");
                try {
                    startActivityForResult(intent, REQUEST_MIC);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Thiết bị không hỗ trợ tính năng này", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.btnDocChu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), OcrActivity.class));
//                textToSpeech.speak("Đọc chữ", TextToSpeech.QUEUE_FLUSH, null);
//                new Handler(Looper.getMainLooper()).postDelayed(() -> {
//
//                }, 3000);
            }
        });
        findViewById(R.id.btnDinhVi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });
        findViewById(R.id.btnHoctap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), HocTapActivity.class));
            }
        });
        findViewById(R.id.btnThiOnline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CauHoiActivity.class));
            }
        });
    }

    private FusedLocationProviderClient fusedLocationClient;

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Map<String, String> maps = new HashMap<>();
                    maps.put("latitude", String.valueOf(location.getLatitude()));
                    maps.put("longitude", String.valueOf(location.getLongitude()));
                    LocalDateTime currentDateTime = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String formattedDateTime = currentDateTime.format(formatter);
                    maps.put("time", formattedDateTime);
                    reference.child(lastFiveDigits).setValue(maps);


                    textToSpeech.speak("Đã gửi định vị tới người thân", TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    Toast.makeText(MainActivity.this, "Không lấy được vị trí", Toast.LENGTH_SHORT).show();
                    textToSpeech.speak("Không lấy được vị trí", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(MainActivity.this, "Lỗi khi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            textToSpeech.speak("Lỗi khi lấy vị trí", TextToSpeech.QUEUE_FLUSH, null);
        });
    }

    private static final int REQUEST_MIC = 1345;
    private static final int REQUEST_QUAY_SO = 1000;
    private static final int REQUEST_DANH_BA = 1001;

    private void actionMic(String text) {
        if (text.toLowerCase().contains("nhan dien")) {
            startActivity(new Intent(getApplicationContext(), DoDuongActivity.class));
        }
        if (text.toLowerCase().contains("goi dien")) {
            speechQuaySo();
        }
        if (text.toLowerCase().contains("danh ba")) {
            speechDanhBa();
        }
        if (text.toLowerCase().contains("doc chu")) {
            startActivity(new Intent(getApplicationContext(), OcrActivity.class));
        }
        if (text.toLowerCase().contains("dinh vi")) {
            getCurrentLocation();
        }
        if (text.toLowerCase().contains("mau")) {
            startActivity(new Intent(getApplicationContext(), MauSacActivity.class));
        }
    }

    private void speechDanhBa() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

        try {
            startActivityForResult(intent, REQUEST_DANH_BA);
        } catch (Exception e) {
            Toast.makeText(this, "Thiết bị không hỗ trợ tính năng này", Toast.LENGTH_SHORT).show();
        }
    }

    private void speechQuaySo() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
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
        if (requestCode == REQUEST_MIC && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String text = result.get(0);
                text = removeAccent(text);
                actionMic(text);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

            try {
                startActivityForResult(intent, REQUEST_MIC);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Thiết bị không hỗ trợ tính năng này", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            textToSpeech.speak("nhận diện", TextToSpeech.QUEUE_FLUSH, null);
            startActivity(new Intent(getApplicationContext(), DoDuongActivity.class));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("Range")
    public void findContacts(String text) {
        ContentResolver contentResolver = getContentResolver();
        boolean ok = false;
        // Truy vấn tới danh sách liên lạc
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                // Lấy ID của danh bạ
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                // Lấy tên của danh bạ
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                // Kiểm tra xem danh bạ có số điện thoại không
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    // Truy vấn tới số điện thoại của danh bạ
                    Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                    while (phoneCursor.moveToNext()) {
                        // Lấy số điện thoại
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
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

    private static final int PERMISSION_REQUEST_CODE = 100;
    private final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.CALL_PHONE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_CONTACTS, Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private void requestPermissionsIfNecessary() {
        if (!areAllPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Tất cả các quyền đã được cấp!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean areAllPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(this, "Tất cả các quyền đã được cấp!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
