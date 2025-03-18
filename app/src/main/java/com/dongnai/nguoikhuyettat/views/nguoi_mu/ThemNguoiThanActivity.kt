package com.dongnai.nguoikhuyettat.views.nguoi_mu

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.dongnai.nguoikhuyettat.NguoiMuSDK
import com.dongnai.nguoikhuyettat.R
import com.dongnai.nguoikhuyettat.adapters.NguoiThanAdapter
import com.dongnai.nguoikhuyettat.database.DBContext
import com.dongnai.nguoikhuyettat.models.NguoiThan
import com.dongnai.nguoikhuyettat.utils.BitmapUtils
import com.dongnai.nguoikhuyettat.utils.FileUtils

class ThemNguoiThanActivity : AppCompatActivity() {
    var dbContext: DBContext? = null
    var edtName: EditText? = null
    var btnThem: AppCompatButton? = null
    var rcvNguoiThan: RecyclerView? = null
    var nguoiThanAdapter: NguoiThanAdapter? = null
    var nguoiMuSDK: NguoiMuSDK? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_them_nguoi_than)
        dbContext = DBContext(this@ThemNguoiThanActivity)
        init()
        onClick()
        reload()
    }

    private fun onClick() {
        imgAvatar!!.setOnClickListener {
            startActivity(
                Intent(applicationContext, DetectFaceActivity::class.java)
            )
        }
        findViewById<View>(R.id.btnThem).setOnClickListener {
            if (!embedding.isEmpty() && bitmap != null && !edtName!!.text.toString()
                    .isEmpty()
            ) {
                dbContext!!.add(
                    NguoiThan(
                        edtName!!.text.toString(),
                        BitmapUtils.getBytes(bitmap),
                        embedding
                    )
                )
                nguoiThanAdapter = NguoiThanAdapter(
                    dbContext!!.nguoiThans,
                    this@ThemNguoiThanActivity
                )
                rcvNguoiThan!!.adapter = nguoiThanAdapter
            } else {
                Toast.makeText(applicationContext, "Empty", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<View>(R.id.openGallery).setOnClickListener { openGallery() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data

            val imagePath = FileUtils.getPath(
                applicationContext, selectedImageUri
            )
            bitmap = nguoiMuSDK!!.getFaceAlignFromPath(imagePath)
            imgAvatar!!.setImageBitmap(bitmap)
            embedding = nguoiMuSDK!!.getEmbeddingFromPath(imagePath)
            Log.e("IMAGE_PATH", imagePath!!)
            Log.e("EMBEDDING", embedding)
        }
    }

    fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/*")
        startActivityForResult(intent, SELECT_IMAGE)
    }

    private fun reload() {
        val ret_init = nguoiMuSDK!!.loadModel(assets, 0, 1, 0, 0, 0, 0)
        if (!ret_init) {
            Log.e("NhanDienNguoiThanActivity", "yolov8ncnn loadModel failed")
        } else {
            Log.e("CHECK RELOAD", "yolov8ncnn loadModel ok")
        }
    }

    private fun init() {
        edtName = findViewById(R.id.edtName)
        imgAvatar = findViewById(R.id.imgView)
        btnThem = findViewById(R.id.btnThem)
        rcvNguoiThan = findViewById(R.id.rcvView)
        nguoiThanAdapter = NguoiThanAdapter(dbContext!!.nguoiThans, this@ThemNguoiThanActivity)
        rcvNguoiThan.setAdapter(nguoiThanAdapter)
        nguoiMuSDK = NguoiMuSDK()
    }

    companion object {
        var imgAvatar: ImageView? = null
        var embedding: String = ""
        var bitmap: Bitmap? = null

        private const val SELECT_IMAGE = 456
    }
}