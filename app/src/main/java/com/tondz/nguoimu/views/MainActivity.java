package com.tondz.nguoimu.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tondz.nguoimu.Common;
import com.tondz.nguoimu.NguoiMuSDK;
import com.tondz.nguoimu.R;
import com.tondz.nguoimu.database.DBContext;
import com.tondz.nguoimu.models.CauHoi;
import com.tondz.nguoimu.models.NguoiThan;
import com.tondz.nguoimu.utils.CalDistance;
import com.tondz.nguoimu.views.nguoi_mu.CauHoiActivity;
import com.tondz.nguoimu.views.nguoi_mu.DoDuongActivity;
import com.tondz.nguoimu.views.nguoi_mu.HocTapActivity;
import com.tondz.nguoimu.views.nguoi_mu.NhanDienNguoiThanActivity;
import com.tondz.nguoimu.views.nguoi_mu.NhanDienTienActivity;
import com.tondz.nguoimu.views.nguoi_mu.OcrActivity;


import java.sql.Time;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    boolean isPass = true;
    TextToSpeech textToSpeech;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        requestPermissionsIfNecessary();
        loadWidthInImages();
        init();
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.forLanguageTag("vi-VN"));
                }
            }
        });
        findViewById(R.id.btnDoDuong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPass) {
                    textToSpeech.speak("Mở dò đường", TextToSpeech.QUEUE_FLUSH, null);
                    startActivity(new Intent(getApplicationContext(), DoDuongActivity.class));
                }

            }
        });
        findViewById(R.id.btnHoctap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.speak("Học tập", TextToSpeech.QUEUE_FLUSH, null);
                database.getReference("CauHoi").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Common.cauHoiArrayList.clear();
                        Log.d("TAG", "onDataChange: " + snapshot.toString());
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()
                        ) {
                            CauHoi cauHoi = dataSnapshot.getValue(CauHoi.class);
                            Common.cauHoiArrayList.add(cauHoi);
                        }
                        startActivity(new Intent(getApplicationContext(), HocTapActivity.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
        findViewById(R.id.btnNhanDienTien).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.speak("Mở nhận diện tiền", TextToSpeech.QUEUE_FLUSH, null);
                startActivity(new Intent(getApplicationContext(), NhanDienTienActivity.class));
            }
        });
        findViewById(R.id.btnNhanDienNguoi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPass) {
                    textToSpeech.speak("Mở nhận diện người thân", TextToSpeech.QUEUE_FLUSH, null);
                    startActivity(new Intent(getApplicationContext(), NhanDienNguoiThanActivity.class));
                }

            }
        });
        findViewById(R.id.btnGoiDien).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.speak("Quay số", TextToSpeech.QUEUE_FLUSH, null);
                speechQuaySo();
            }
        });
        findViewById(R.id.btnGoiDanhBa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                textToSpeech.speak("Danh bạ", TextToSpeech.QUEUE_FLUSH, null);
                speechDanhBa();
            }
        });

        findViewById(R.id.btnMic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    Toast.makeText(MainActivity.this, "Thiết bị không hỗ trợ tính năng này", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.btnDocChu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), OcrActivity.class));
                textToSpeech.speak("Mở đọc chữ", TextToSpeech.QUEUE_FLUSH, null);
            }
        });
        findViewById(R.id.btnDinhVi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });
        findViewById(R.id.btnThiOnline).setOnClickListener(view -> {
            textToSpeech.speak("Làm bài thi", TextToSpeech.QUEUE_FLUSH, null);
            database.getReference("CauHoi").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Common.cauHoiArrayList.clear();
                    Log.d("TAG", "onDataChange: " + snapshot.toString());
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()
                    ) {
                        CauHoi cauHoi = dataSnapshot.getValue(CauHoi.class);
                        Common.cauHoiArrayList.add(cauHoi);
                    }
                    startActivity(new Intent(MainActivity.this, CauHoiActivity.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        });
        getId();
    }

    Button btnDinhvi;
    String lastFiveDigits;

    private void getId() {
        @SuppressLint("HardwareIds") String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (deviceId != null && deviceId.length() >= 5) {
            lastFiveDigits = deviceId.substring(deviceId.length() - 5);
            btnDinhvi.setText("Định vị \nID: " + lastFiveDigits);

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
        btnDinhvi = findViewById(R.id.btnDinhVi);
    }

    private FusedLocationProviderClient fusedLocationClient;

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
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
    private static final int REQUEST_CODE_PERMISSION = 12000;

    private void actionMic(String text) {
        if (text.toLowerCase().contains("do duong")) {
            textToSpeech.speak("Mở chức năng dò đường", TextToSpeech.QUEUE_FLUSH, null);
            startActivity(new Intent(getApplicationContext(), DoDuongActivity.class));
        }
        if (text.toLowerCase().contains("nhan dien nguoi")) {
            textToSpeech.speak("Mở chức năng nhận diện người thân", TextToSpeech.QUEUE_FLUSH, null);
            startActivity(new Intent(getApplicationContext(), NhanDienNguoiThanActivity.class));
        }
        if (text.toLowerCase().contains("quay so")) {
            textToSpeech.speak("Mở chức năng quay số", TextToSpeech.QUEUE_FLUSH, null);
            speechQuaySo();
        }
        if (text.toLowerCase().contains("danh ba")) {
            textToSpeech.speak("Mở danh bạ", TextToSpeech.QUEUE_FLUSH, null);
            speechDanhBa();
        }
        if (text.toLowerCase().contains("chu")) {
            textToSpeech.speak("Mở chức năng đọc chữ ", TextToSpeech.QUEUE_FLUSH, null);
            startActivity(new Intent(getApplicationContext(), OcrActivity.class));
        }
        if (text.toLowerCase().contains("dinh vi")) {
            textToSpeech.speak("Mở chức năng định vị", TextToSpeech.QUEUE_FLUSH, null);
            getCurrentLocation();
        }
        if (text.toLowerCase().contains("tien")) {
            textToSpeech.speak("Mở chức năng nhận diện tiền", TextToSpeech.QUEUE_FLUSH, null);
            startActivity(new Intent(getApplicationContext(), NhanDienTienActivity.class));
        }
        if (text.toLowerCase().contains("thi")) {
            textToSpeech.speak("Làm bài thi", TextToSpeech.QUEUE_FLUSH, null);
            database.getReference("CauHoi").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Common.cauHoiArrayList.clear();
                    Log.d("TAG", "onDataChange: " + snapshot.toString());
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()
                    ) {
                        CauHoi cauHoi = dataSnapshot.getValue(CauHoi.class);
                        Common.cauHoiArrayList.add(cauHoi);
                    }
                    startActivity(new Intent(MainActivity.this, CauHoiActivity.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        if (text.toLowerCase().contains("hoc")) {
            textToSpeech.speak("Mở chức năng học bài", TextToSpeech.QUEUE_FLUSH, null);
            database.getReference("CauHoi").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Common.cauHoiArrayList.clear();
                    Log.d("TAG", "onDataChange: " + snapshot.toString());
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()
                    ) {
                        CauHoi cauHoi = dataSnapshot.getValue(CauHoi.class);
                        Common.cauHoiArrayList.add(cauHoi);
                    }
                    startActivity(new Intent(getApplicationContext(), HocTapActivity.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

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
        if (requestCode == REQUEST_MIC && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String text = result.get(0);
                text = removeAccent(text);
                actionMic(text);
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
