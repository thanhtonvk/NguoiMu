package com.phuyen.nguoikhuyettat.views.cam_diec

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.phuyen.nguoikhuyettat.R

class ChonChucNangMuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_chon_chuc_nang_mu)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<View>(R.id.btnCamDiec).setOnClickListener {
            startActivity(
                Intent(applicationContext, CamDiecActivity::class.java)
            )
        }
        findViewById<View>(R.id.btnCau).setOnClickListener {
            startActivity(
                Intent(applicationContext, CamDiecNoiCauActivity::class.java)
            )
        }
        findViewById<View>(R.id.btnBt).setOnClickListener {
            startActivity(
                Intent(
                    applicationContext,
                    NguoiBinhThuongActivity::class.java
                )
            )
        }
        findViewById<View>(R.id.btnGhepTu).setOnClickListener { view: View? ->
            startActivity(
                Intent(
                    applicationContext,
                    GhepTuActivity::class.java
                )
            )
        }
    }
}