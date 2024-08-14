package com.tondz.nguoimu.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tondz.nguoimu.R;
import com.tondz.nguoimu.adapters.NguoiThanAdapter;
import com.tondz.nguoimu.database.DBContext;
import com.tondz.nguoimu.models.NguoiThan;
import com.tondz.nguoimu.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.List;

public class ThemNguoiThanActivity extends AppCompatActivity {
    DBContext dbContext;
    EditText edtName;
    public static ImageView imgAvatar;
    AppCompatButton btnThem;
    RecyclerView rcvNguoiThan;
    NguoiThanAdapter nguoiThanAdapter;
    public static String embedding = "";
    public static Bitmap bitmap = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_nguoi_than);
        dbContext = new DBContext(ThemNguoiThanActivity.this);
        init();
        onClick();

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
                }
            }
        });
    }

    private void init() {
        edtName = findViewById(R.id.edtName);
        imgAvatar = findViewById(R.id.imgView);
        btnThem = findViewById(R.id.btnThem);
        rcvNguoiThan = findViewById(R.id.rcvView);
        nguoiThanAdapter = new NguoiThanAdapter(dbContext.getNguoiThans(), ThemNguoiThanActivity.this);
        rcvNguoiThan.setAdapter(nguoiThanAdapter);

    }
}