package com.dongnai.nguoikhuyettat.views.register

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.dongnai.nguoikhuyettat.R
import com.dongnai.nguoikhuyettat.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    var binding: ActivityRegisterBinding? = null
    var auth: FirebaseAuth? = null
    var edtEmail: TextInputEditText? = null
    var edtFullname: TextInputEditText? = null
    var edtPassword: TextInputEditText? = null
    var edtConfirmPassword: TextInputEditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        init()
        onClick()
        auth = FirebaseAuth.getInstance()
    }

    private fun init() {
        edtEmail = findViewById(R.id.edtEmail)
        edtFullname = findViewById(R.id.edtFullName)
        edtPassword = findViewById(R.id.edtPassword)
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword)
    }

    private fun onClick() {
        findViewById<View>(R.id.btnRegister).setOnClickListener { register() }
    }

    private fun register() {
        val email = edtEmail!!.text.toString()
        val fullName = edtFullname!!.text.toString()
        val password = edtPassword!!.text.toString()
        val confirmPassword = edtConfirmPassword!!.text.toString()
        if (email.isEmpty() || fullName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(applicationContext, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT)
                .show()
        } else {
            if (password == confirmPassword) {
                if (password.length >= 6) {
                    val dialog = ProgressDialog(this@RegisterActivity)
                    dialog.setMessage("Đang đăng ký...")
                    dialog.show()
                    auth!!.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task: Task<AuthResult?> ->
                            Log.e(
                                "TAG",
                                "register: $task"
                            )
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    applicationContext,
                                    "Đăng ký thành công",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dialog.dismiss()
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "Đăng ký thất bại, tài khoản đã tồn tại ",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dialog.dismiss()
                            }
                        }.addOnFailureListener { command: Exception? ->
                            Toast.makeText(
                                applicationContext,
                                "Đăng ký thất bại do lỗi kết nối ",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        applicationContext,
                        " Mật khẩu phải >6 kí tự",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(applicationContext, " Mật khẩu không khớp", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}