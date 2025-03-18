package com.phuyen.nguoikhuyettat.views.nguoi_mu

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.phuyen.nguoikhuyettat.R
import java.util.Arrays
import java.util.Locale

class DocChuActivity : AppCompatActivity() {
    private var textureView: TextureView? = null
    private var cameraDevice: CameraDevice? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    var recognizer: TextRecognizer? = null
    var textToSpeech: TextToSpeech? = null
    private var isSpeaking = false // Trạng thái đọc nội dung

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doc_chu)

        textureView = findViewById(R.id.textureView)

        textureView.setSurfaceTextureListener(surfaceTextureListener)
        init()
    }

    private fun init() {
        recognizer =
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        textToSpeech = TextToSpeech(
            applicationContext
        ) { i ->
            if (i != TextToSpeech.ERROR) {
                textToSpeech!!.setLanguage(Locale.forLanguageTag("vi-VN"))
            }
        }
    }

    private val surfaceTextureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        }
    }

    private fun openCamera() {
        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0]
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val imageSize = map!!.getOutputSizes(ImageFormat.JPEG)[0]

            imageReader =
                ImageReader.newInstance(imageSize.width, imageSize.height, ImageFormat.JPEG, 1)
            imageReader!!.setOnImageAvailableListener(imageAvailableListener, null)

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            cameraManager.openCamera(cameraId, stateCallback, null)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to open camera: " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            startCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            cameraDevice = null
        }
    }

    private fun startCameraPreview() {
        try {
            val texture = textureView!!.surfaceTexture
            texture!!.setDefaultBufferSize(textureView!!.width, textureView!!.height)
            val surface = Surface(texture)

            previewRequestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder!!.addTarget(surface)

            cameraDevice!!.createCaptureSession(
                Arrays.asList(surface, imageReader!!.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        cameraCaptureSession = session
                        try {
                            cameraCaptureSession!!.setRepeatingRequest(
                                previewRequestBuilder!!.build(),
                                null,
                                null
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Toast.makeText(
                            this@DocChuActivity,
                            "Preview Configuration failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val imageAvailableListener =
        OnImageAvailableListener { reader: ImageReader ->
            reader.acquireNextImage().use { image ->
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer[bytes]

                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (isSpeaking) {
                    textToSpeech!!.stop()
                    isSpeaking = false
                    return@OnImageAvailableListener
                }
                recognizeTextFromImage(bitmap)
            }
        }
    var stringBuilder: StringBuilder? = null

    private fun readContent(content: String) {
        isSpeaking = true
        textToSpeech!!.speak(content, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun recognizeTextFromImage(bitmap: Bitmap) {
        stringBuilder = StringBuilder()

        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer!!.process(image)
            .addOnSuccessListener(OnSuccessListener { visionText ->
                val blocks = visionText.textBlocks
                if (blocks.isEmpty()) {
                    readContent("không thấy nội dung")
                    return@OnSuccessListener
                } else {
                    for (block in blocks) {
                        val txt = block.text
                        stringBuilder!!.append(txt)
                    }
                    readContent(stringBuilder.toString())
                }
            })
            .addOnFailureListener { readContent("có lỗi xảy ra, vui lòng thử lại") }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            captureImage()
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun captureImage() {
        try {
            val captureBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(imageReader!!.surface)
            cameraCaptureSession!!.capture(captureBuilder.build(), null, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (cameraDevice != null) {
            cameraDevice!!.close()
            cameraDevice = null
        }
    }
}