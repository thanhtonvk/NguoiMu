package com.tondz.nguoimu.views.nguoi_mu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.tondz.nguoimu.NguoiMuSDK;
import com.tondz.nguoimu.R;
import com.tondz.nguoimu.adapters.NguoiThanAdapter;
import com.tondz.nguoimu.database.DBContext;
import com.tondz.nguoimu.models.NguoiThan;
import com.tondz.nguoimu.utils.BitmapUtils;
import com.tondz.nguoimu.utils.FileUtils;

public class ThemNguoiThanActivity extends AppCompatActivity {
    DBContext dbContext;
    EditText edtName;
    public static ImageView imgAvatar;
    AppCompatButton btnThem;
    RecyclerView rcvNguoiThan;
    NguoiThanAdapter nguoiThanAdapter;
    public static String embedding = "";
    public static Bitmap bitmap = null;

    private static final int SELECT_IMAGE = 456;
    NguoiMuSDK nguoiMuSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_nguoi_than);
        dbContext = new DBContext(ThemNguoiThanActivity.this);
        init();
        onClick();
        reload();

    }

    private void onClick() {
        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), DetectFaceActivity.class));
            }
        });
        findViewById(R.id.btnThem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!embedding.isEmpty() && bitmap != null && !edtName.getText().toString().isEmpty()) {
                    dbContext.add(new NguoiThan(edtName.getText().toString(), BitmapUtils.getBytes(bitmap), embedding));
                    nguoiThanAdapter = new NguoiThanAdapter(dbContext.getNguoiThans(), ThemNguoiThanActivity.this);
                    rcvNguoiThan.setAdapter(nguoiThanAdapter);
                } else {
                    Toast.makeText(getApplicationContext(), "Empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.openGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            String imagePath = FileUtils.getPath(getApplicationContext(), selectedImageUri);
            bitmap = nguoiMuSDK.getFaceAlignFromPath(imagePath);
            imgAvatar.setImageBitmap(bitmap);
            embedding = nguoiMuSDK.getEmbeddingFromPath(imagePath);
            Log.e("IMAGE_PATH", imagePath);
            Log.e("EMBEDDING", embedding);
        }
    }

    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_IMAGE);
    }

    private void reload() {
        boolean ret_init = nguoiMuSDK.loadModel(getAssets(), 0, 1, 0, 0, 0,0);
        if (!ret_init) {
            Log.e("NhanDienNguoiThanActivity", "yolov8ncnn loadModel failed");
        } else {
            Log.e("CHECK RELOAD", "yolov8ncnn loadModel ok");
        }
    }

    private void init() {
        edtName = findViewById(R.id.edtName);
        imgAvatar = findViewById(R.id.imgView);
        btnThem = findViewById(R.id.btnThem);
        rcvNguoiThan = findViewById(R.id.rcvView);
        nguoiThanAdapter = new NguoiThanAdapter(dbContext.getNguoiThans(), ThemNguoiThanActivity.this);
        rcvNguoiThan.setAdapter(nguoiThanAdapter);
        nguoiMuSDK = new NguoiMuSDK();

    }
}