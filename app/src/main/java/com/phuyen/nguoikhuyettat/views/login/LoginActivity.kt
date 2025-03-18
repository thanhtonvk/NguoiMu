package com.phuyen.nguoikhuyettat.views.login

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.phuyen.nguoikhuyettat.ManHinhChinhActivity
import com.phuyen.nguoikhuyettat.databinding.ActivityLoginBinding
import com.phuyen.nguoikhuyettat.views.register.RegisterActivity

class LoginActivity : AppCompatActivity() {
    var binding: ActivityLoginBinding? = null
    var auth: FirebaseAuth? = null
    var database: FirebaseDatabase? = null
    var reference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        reference = database!!.reference
        onClick()
        loadAccount()
    }


    private fun onClick() {
        binding!!.btnRegister.setOnClickListener { view: View? ->
            startActivity(
                Intent(
                    this,
                    RegisterActivity::class.java
                )
            )
        }
        binding!!.btnLogin.setOnClickListener { v: View? ->
            login()
        }
        binding!!.btnForgotPassword.setOnClickListener { v: View? ->
            forgotPassword()
        }
    }

    private fun forgotPassword() {
        val email = binding!!.edtEmail.text.toString()
        if (email.isEmpty()) {
            Toast.makeText(
                applicationContext,
                " Thông tin Email không được bỏ trong ",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            auth!!.sendPasswordResetEmail(email).addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Thông báo")
                    builder.setMessage("Yêu cầu quên mật khẩu đã được gửi về mail của bạn")
                    builder.setPositiveButton(
                        "OK"
                    ) { dialog: DialogInterface, which: Int ->
                        dialog.dismiss()
                    }
                    val alertDialog = builder.create()
                    alertDialog.show()
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Thông báo")
                    builder.setMessage("Có lỗi xảy ra, hãy thử lại")
                    builder.setPositiveButton(
                        "OK"
                    ) { dialog: DialogInterface, which: Int ->
                        dialog.dismiss()
                    }
                    val alertDialog = builder.create()
                    alertDialog.show()
                }
            }.addOnFailureListener { command: Exception? ->
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Thông báo")
                builder.setMessage("Có lỗi xảy ra, hãy thử lại")
                builder.setPositiveButton(
                    "OK"
                ) { dialog: DialogInterface, which: Int ->
                    dialog.dismiss()
                }
                val alertDialog = builder.create()
                alertDialog.show()
            }
        }
    }

    private fun login() {
        val email = binding!!.edtEmail.text.toString()
        val password = binding!!.edtPassword.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                applicationContext,
                " Thông tin không được bỏ trong ",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val dialog = ProgressDialog(this@LoginActivity)
            dialog.setTitle("Đang đăng nhập")
            dialog.show()
            auth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task: Task<AuthResult?> ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            applicationContext,
                            "Đăng nhập thành công",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        saveAccount(email, password)
                        startActivity(
                            Intent(
                                applicationContext,
                                ManHinhChinhActivity::class.java
                            )
                        )
                        finish()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Tài khoản hoặc mật khẩu không chinh xác",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    dialog.dismiss()
                }.addOnFailureListener { command: Exception? ->
                    Toast.makeText(applicationContext, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
        }
    }

    private fun saveAccount(email: String, password: String) {
        val prefs = getSharedPreferences("Account", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("email", email)
        editor.putString("password", password)
        editor.apply()
    }

    private fun loadAccount() {
        val prefs = getSharedPreferences("Account", MODE_PRIVATE)
        val email = prefs.getString("email", "")!!
        val password = prefs.getString("password", "")!!
        binding!!.edtEmail.setText(email)
        binding!!.edtPassword.setText(password)
        login()
    }
}