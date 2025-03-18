package com.phuyen.nguoikhuyettat.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.ContactsContract
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.phuyen.nguoikhuyettat.Common
import com.phuyen.nguoikhuyettat.R
import com.phuyen.nguoikhuyettat.models.CauHoi
import com.phuyen.nguoikhuyettat.utils.CalDistance
import com.phuyen.nguoikhuyettat.views.nguoi_mu.CauHoiActivity
import com.phuyen.nguoikhuyettat.views.nguoi_mu.DoDuongActivity
import com.phuyen.nguoikhuyettat.views.nguoi_mu.HocTapActivity
import com.phuyen.nguoikhuyettat.views.nguoi_mu.NhanDienNguoiThanActivity
import com.phuyen.nguoikhuyettat.views.nguoi_mu.NhanDienTienActivity
import com.phuyen.nguoikhuyettat.views.nguoi_mu.OcrActivity
import java.text.Normalizer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    var isPass: Boolean = true
    var textToSpeech: TextToSpeech? = null
    var database: FirebaseDatabase? = null
    var reference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        database = FirebaseDatabase.getInstance()
        reference = database!!.reference
        requestPermissionsIfNecessary()
        loadWidthInImages()
        init()
        textToSpeech = TextToSpeech(
            applicationContext
        ) { i ->
            if (i != TextToSpeech.ERROR) {
                textToSpeech!!.setLanguage(Locale.forLanguageTag("vi-VN"))
            }
        }
        findViewById<View>(R.id.btnDoDuong).setOnClickListener {
            if (isPass) {
                textToSpeech!!.speak("Mở dò đường", TextToSpeech.QUEUE_FLUSH, null)
                startActivity(
                    Intent(
                        applicationContext,
                        DoDuongActivity::class.java
                    )
                )
            }
        }
        findViewById<View>(R.id.btnHoctap).setOnClickListener {
            textToSpeech!!.speak("Học tập", TextToSpeech.QUEUE_FLUSH, null)
            database!!.getReference("CauHoi").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Common.cauHoiArrayList = ArrayList();
                    Log.d("TAG", "onDataChange: $snapshot")
                    for (dataSnapshot in snapshot.children
                    ) {
                        val cauHoi = dataSnapshot.getValue(CauHoi::class.java)
                        if (cauHoi != null) {
                            (Common.cauHoiArrayList as ArrayList<CauHoi>).add(cauHoi)
                        }
                    }
                    startActivity(
                        Intent(
                            applicationContext,
                            HocTapActivity::class.java
                        )
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
        findViewById<View>(R.id.btnNhanDienTien).setOnClickListener {
            textToSpeech!!.speak("Mở nhận diện tiền", TextToSpeech.QUEUE_FLUSH, null)
            startActivity(
                Intent(
                    applicationContext,
                    NhanDienTienActivity::class.java
                )
            )
        }
        findViewById<View>(R.id.btnNhanDienNguoi).setOnClickListener {
            if (isPass) {
                textToSpeech!!.speak("Mở nhận diện người thân", TextToSpeech.QUEUE_FLUSH, null)
                startActivity(
                    Intent(
                        applicationContext,
                        NhanDienNguoiThanActivity::class.java
                    )
                )
            }
        }
        findViewById<View>(R.id.btnGoiDien).setOnClickListener {
            textToSpeech!!.speak("Quay số", TextToSpeech.QUEUE_FLUSH, null)
            speechQuaySo()
        }
        findViewById<View>(R.id.btnGoiDanhBa).setOnClickListener {
            textToSpeech!!.speak("Danh bạ", TextToSpeech.QUEUE_FLUSH, null)
            speechDanhBa()
        }

        findViewById<View>(R.id.btnMic).setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
            try {
                startActivityForResult(intent, REQUEST_MIC)
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Thiết bị không hỗ trợ tính năng này",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        findViewById<View>(R.id.btnDocChu).setOnClickListener {
            startActivity(Intent(applicationContext, OcrActivity::class.java))
            textToSpeech!!.speak("Mở đọc chữ", TextToSpeech.QUEUE_FLUSH, null)
        }
        findViewById<View>(R.id.btnDinhVi).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                currentLocation
            }
        })
        findViewById<View>(R.id.btnThiOnline).setOnClickListener { view: View? ->
            textToSpeech!!.speak("Làm bài thi", TextToSpeech.QUEUE_FLUSH, null)
            database!!.getReference("CauHoi").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Common.cauHoiArrayList  = ArrayList();
                    Log.d("TAG", "onDataChange: $snapshot")
                    for (dataSnapshot in snapshot.children
                    ) {
                        val cauHoi = dataSnapshot.getValue(CauHoi::class.java)
                        if (cauHoi != null) {
                            (Common.cauHoiArrayList as ArrayList<CauHoi>).add(cauHoi)
                        }
                    }
                    startActivity(
                        Intent(
                            this@MainActivity,
                            CauHoiActivity::class.java
                        )
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
        id
    }

    var btnDinhvi: Button? = null
    var lastFiveDigits: String? = null

    private val id: Unit
        get() {
            @SuppressLint("HardwareIds") val deviceId =
                Settings.Secure.getString(
                    contentResolver,
                    Settings.Secure.ANDROID_ID
                )
            if (deviceId != null && deviceId.length >= 5) {
                lastFiveDigits = deviceId.substring(deviceId.length - 5)
                btnDinhvi!!.text = "Định vị \nID: $lastFiveDigits"
            } else {
                println("Device ID không hợp lệ hoặc quá ngắn.")
            }
        }

    private fun init() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        textToSpeech = TextToSpeech(
            applicationContext
        ) { i ->
            if (i != TextToSpeech.ERROR) {
                textToSpeech!!.setLanguage(Locale.forLanguageTag("vi-VN"))
            }
        }
        database = FirebaseDatabase.getInstance()
        reference = database!!.getReference("NhanViTri")
        btnDinhvi = findViewById(R.id.btnDinhVi)
    }

    private var fusedLocationClient: FusedLocationProviderClient? = null

    private val currentLocation: Unit
        @RequiresApi(Build.VERSION_CODES.O)
        get() {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient!!.lastLocation.addOnSuccessListener(
                this
            ) { location ->
                if (location != null) {
                    val maps: MutableMap<String, String> =
                        HashMap()
                    maps["latitude"] = location.latitude.toString()
                    maps["longitude"] = location.longitude.toString()
                    val currentDateTime =
                        LocalDateTime.now()
                    val formatter =
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val formattedDateTime = currentDateTime.format(formatter)
                    maps["time"] = formattedDateTime
                    reference!!.child(lastFiveDigits!!).setValue(maps)


                    textToSpeech!!.speak(
                        "Đã gửi định vị tới người thân",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Không lấy được vị trí",
                        Toast.LENGTH_SHORT
                    ).show()
                    textToSpeech!!.speak(
                        "Không lấy được vị trí",
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                }
            }.addOnFailureListener { e: Exception ->
                Toast.makeText(
                    this@MainActivity,
                    "Lỗi khi lấy vị trí: " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
                textToSpeech!!.speak("Lỗi khi lấy vị trí", TextToSpeech.QUEUE_FLUSH, null)
            }
        }

    private fun actionMic(text: String) {
        if (text.lowercase(Locale.getDefault()).contains("do duong")) {
            textToSpeech!!.speak("Mở chức năng dò đường", TextToSpeech.QUEUE_FLUSH, null)
            startActivity(Intent(applicationContext, DoDuongActivity::class.java))
        }
        if (text.lowercase(Locale.getDefault()).contains("nhan dien nguoi")) {
            textToSpeech!!.speak(
                "Mở chức năng nhận diện người thân",
                TextToSpeech.QUEUE_FLUSH,
                null
            )
            startActivity(Intent(applicationContext, NhanDienNguoiThanActivity::class.java))
        }
        if (text.lowercase(Locale.getDefault()).contains("quay so")) {
            textToSpeech!!.speak("Mở chức năng quay số", TextToSpeech.QUEUE_FLUSH, null)
            speechQuaySo()
        }
        if (text.lowercase(Locale.getDefault()).contains("danh ba")) {
            textToSpeech!!.speak("Mở danh bạ", TextToSpeech.QUEUE_FLUSH, null)
            speechDanhBa()
        }
        if (text.lowercase(Locale.getDefault()).contains("chu")) {
            textToSpeech!!.speak("Mở chức năng đọc chữ ", TextToSpeech.QUEUE_FLUSH, null)
            startActivity(Intent(applicationContext, OcrActivity::class.java))
        }
        if (text.lowercase(Locale.getDefault()).contains("dinh vi")) {
            textToSpeech!!.speak("Mở chức năng định vị", TextToSpeech.QUEUE_FLUSH, null)
            currentLocation
        }
        if (text.lowercase(Locale.getDefault()).contains("tien")) {
            textToSpeech!!.speak("Mở chức năng nhận diện tiền", TextToSpeech.QUEUE_FLUSH, null)
            startActivity(Intent(applicationContext, NhanDienTienActivity::class.java))
        }
        if (text.lowercase(Locale.getDefault()).contains("thi")) {
            textToSpeech!!.speak("Làm bài thi", TextToSpeech.QUEUE_FLUSH, null)
            database!!.getReference("CauHoi").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Common.cauHoiArrayList.clear()
                    Log.d("TAG", "onDataChange: $snapshot")
                    for (dataSnapshot in snapshot.children
                    ) {
                        val cauHoi = dataSnapshot.getValue(CauHoi::class.java)
                        Common.cauHoiArrayList.add(cauHoi)
                    }
                    startActivity(
                        Intent(
                            this@MainActivity,
                            CauHoiActivity::class.java
                        )
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
        if (text.lowercase(Locale.getDefault()).contains("hoc")) {
            textToSpeech!!.speak("Mở chức năng học bài", TextToSpeech.QUEUE_FLUSH, null)
            database!!.getReference("CauHoi").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Common.cauHoiArrayList.clear()
                    Log.d("TAG", "onDataChange: $snapshot")
                    for (dataSnapshot in snapshot.children
                    ) {
                        val cauHoi = dataSnapshot.getValue(CauHoi::class.java)
                        Common.cauHoiArrayList.add(cauHoi)
                    }
                    startActivity(Intent(applicationContext, HocTapActivity::class.java))
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    private fun speechDanhBa() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

        try {
            startActivityForResult(intent, REQUEST_DANH_BA)
        } catch (e: Exception) {
            Toast.makeText(this, "Thiết bị không hỗ trợ tính năng này", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speechQuaySo() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

        try {
            startActivityForResult(intent, REQUEST_QUAY_SO)
        } catch (e: Exception) {
            Toast.makeText(this, "Thiết bị không hỗ trợ tính năng này", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_QUAY_SO && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (result != null && !result.isEmpty()) {
                var sdt = result[0]
                sdt = keepOnlyDigits(sdt).trim { it <= ' ' }
                val intent = Intent(Intent.ACTION_CALL)
                intent.setData(Uri.parse("tel:$sdt"))
                startActivity(intent)
            }
        }
        if (requestCode == REQUEST_DANH_BA && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (result != null && !result.isEmpty()) {
                val text = result[0]
                findContacts(text)
            }
        }
        if (requestCode == REQUEST_MIC && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (result != null && !result.isEmpty()) {
                var text = result[0]
                text = removeAccent(text)
                actionMic(text)
            }
        }
    }


    @SuppressLint("Range")
    fun findContacts(text: String?) {
        var text = text
        val contentResolver = contentResolver
        var ok = false
        // Truy vấn tới danh sách liên lạc
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )

        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                // Lấy ID của danh bạ
                @SuppressLint("Range") val id =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))

                // Lấy tên của danh bạ
                @SuppressLint("Range") var name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                // Kiểm tra xem danh bạ có số điện thoại không
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    // Truy vấn tới số điện thoại của danh bạ
                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id), null
                    )

                    while (phoneCursor!!.moveToNext()) {
                        // Lấy số điện thoại
                        var phoneNumber = phoneCursor.getString(
                            phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        )
                        text = removeAccent(text).trim { it <= ' ' }.lowercase(Locale.getDefault())
                        phoneNumber = keepOnlyDigits(phoneNumber).trim { it <= ' ' }
                        name = removeAccent(name).trim { it <= ' ' }.lowercase(Locale.getDefault())

                        if (name.contains(text)) {
                            val intent = Intent(Intent.ACTION_CALL)
                            intent.setData(Uri.parse("tel:$phoneNumber"))
                            startActivity(intent)
                            ok = true
                            Log.d("Contact", "Name: $name, Phone Number: $phoneNumber")
                            break
                        }
                    }
                    phoneCursor.close()
                }
            }
            if (!ok) {
                Toast.makeText(this, "Không tìm thấy danh bạ", Toast.LENGTH_SHORT).show()
                textToSpeech!!.speak(
                    "Không tìm thấy người liên lạc trong danh bạ, hãy thử lại",
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }
            cursor.close()
        }
    }

    fun keepOnlyDigits(input: String): String {
        // Biểu thức chính quy để giữ lại chỉ các chữ số (0-9)
        return input.replace("[^\\d]".toRegex(), "")
    }

    fun removeAccent(s: String?): String {
        // Normalize văn bản để chuyển các ký tự có dấu thành dạng ký tự tổ hợp
        val temp = Normalizer.normalize(s, Normalizer.Form.NFD)

        // Loại bỏ các dấu bằng cách loại bỏ các ký tự không phải ASCII
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(temp).replaceAll("").replace("đ".toRegex(), "d")
            .replace("Đ".toRegex(), "D")
    }

    private fun loadWidthInImages() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val widthInImages = preferences.getString("widthInImages", "")!!
        if (!widthInImages.equals("", ignoreCase = true)) {
            val arr =
                widthInImages.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in arr.indices) {
                CalDistance.widthInImages[i] = arr[i].toDouble()
            }
        }
    }


    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private fun requestPermissionsIfNecessary() {
        if (!areAllPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
        } else {
            Toast.makeText(this, "Tất cả các quyền đã được cấp!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun areAllPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            var allGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false
                    break
                }
            }

            if (allGranted) {
                Toast.makeText(this, "Tất cả các quyền đã được cấp!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_MIC = 1345
        private const val REQUEST_QUAY_SO = 1000
        private const val REQUEST_DANH_BA = 1001
        private const val REQUEST_CODE_PERMISSION = 12000

        private const val PERMISSION_REQUEST_CODE = 100
    }
}
