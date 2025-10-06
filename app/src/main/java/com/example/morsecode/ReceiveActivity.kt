package com.example.morsecode

import YUVtoRGB
import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import java.util.Locale
import java.util.concurrent.ExecutionException


class ReceiveActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var speakerButton: ImageButton
    private lateinit var modeButton: ImageButton
    private lateinit var chatButton: ImageButton
    private lateinit var cameraPreviewView: PreviewView
    private lateinit var testImageView: ImageView
    private lateinit var cropRect: View
    private lateinit var recMessage: TextView
    private lateinit var overlay: OverlayView;
    private var mode = "flashlight"

    private val PERMISSION_REQUEST_CAMERA = 83854
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private val translator = YUVtoRGB()

    private var durSymbol = 0L
    private var durPause = 0L
    private var prevDetected = false
    private var begSymbol = 0L
    private var begPause = 0L
    private val currLetter = mutableListOf<Char>()
    private var recLetters = ""
    private var missFalse = 0
    private val coder = MorseCoder()

    private lateinit var tts: TextToSpeech
    private var ttsEnable = false

//    private lateinit var detector: Detector



    @ExperimentalGetImage
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive)
        speakerButton = findViewById(R.id.butt_speaker)
        modeButton = findViewById(R.id.butt_mode)
        chatButton = findViewById(R.id.butt_chat)
        cameraPreviewView = findViewById(R.id.cameraPreviewView)
        testImageView = findViewById(R.id.testImageView)
        cropRect = findViewById(R.id.cropRect)
        recMessage = findViewById(R.id.recMessage)
        overlay = findViewById(R.id.overlay)
        tts = TextToSpeech(this, this)

        speakerButton.setOnClickListener { pressSpeaker(it as ImageButton) }
        modeButton.setOnClickListener { pressMode(it as ImageButton) }
        chatButton.setOnClickListener { pressChat() }

        if (ContextCompat.checkSelfPermission(
                this,
                permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(permission.CAMERA), PERMISSION_REQUEST_CAMERA
            )
        } else {
            initializeCamera()
        }

        onBackPressedDispatcher.addCallback(this /* lifecycle owner */) {
            setResult(RESULT_OK, Intent().putExtra("mess", recLetters))
            finish()
        }


//        detector = Detector(baseContext, "model.tflite", "labels.txt", this)
//        detector.setup()

    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val locale = Locale("RU")
            val result = tts.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported!")
            } else {
                ttsEnable = true
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    @ExperimentalGetImage
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeCamera()
        }
    }

    @ExperimentalGetImage
    private fun initializeCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .build()

                preview.setSurfaceProvider(cameraPreviewView.surfaceProvider)


                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(Size(640, 480))
                    .build()
                val cameraSelector =
                    CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()
                imageAnalysis.setAnalyzer(
                    ContextCompat.getMainExecutor(this)
                ) { image ->
                    val img = image.image

                    val bitmap: Bitmap = translator.translateYUV(img!!, this)
                    val fragmentBitmap = Bitmap.createBitmap(bitmap,
                                    cropRect.left + 20 ,
                                    cropRect.top - 70,
                                    cropRect.width, cropRect.height)


                    testImageView.rotation = image.imageInfo.rotationDegrees.toFloat()

                    val (circles, detected) = FrameAnalyzer.analyze(fragmentBitmap, this)
                    // testImageView.setImageBitmap(newBitmap)
                    drawCircles(circles)
                    decodeBlinks(detected)
                    // detector.detect(bitmap)
                    image.close()
                }
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageAnalysis, preview
                )

                camera.cameraControl.setExposureCompensationIndex(-19)
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }


    @SuppressLint("SetTextI18n")
    private fun decodeBlinks(detected: Boolean) {
        if (detected) {
            missFalse = 0
        }

        if (detected && !prevDetected) {
            begSymbol = System.currentTimeMillis()
            durPause = System.currentTimeMillis() - begPause
            prevDetected = true

            if (durPause in 1200..2000) {
                var letter = coder.decode(currLetter)
                if (letter != null) {
                    letter = letter.lowercase().toCharArray()[0]
                    recMessage.text = "${recMessage.text}$letter "
                    speakLetter(letter)
                    recLetters += letter
                }
                currLetter.clear()
            } else if (durPause > 2000) {
                currLetter.clear()
            }
        } else if (!detected && prevDetected) {
            missFalse++
            if (missFalse > 2) {
                durSymbol = System.currentTimeMillis() - begSymbol
                begPause = System.currentTimeMillis()
                prevDetected = false

                if (durSymbol in 200..800) {
                    currLetter.add('.')
                } else if (durSymbol in 800..2000) {
                    currLetter.add('-')
                }
                missFalse = 0
            }
        } else {
            missFalse++
            if (missFalse > 5) {
                val letter = coder.decode(currLetter)
                if (letter != null) {
                    recMessage.text = "${recMessage.text}$letter "
                    speakLetter(letter)
                    recLetters += letter
                }
                currLetter.clear()
            }
        }
    }

    private fun drawCircles(circles: ArrayList<Array<Float>>) {
        overlay.clear()
        overlay.setCircles(circles)
        overlay.draw(Canvas())
    }

    private fun pressSpeaker(button: ImageButton) {
        button.isHovered = !button.isHovered
        if (tts.isSpeaking)
            tts.stop()
    }

    private fun pressMode(button: ImageButton) {
        if (mode == "flashlight") {
            button.setImageResource(R.drawable.rec_act_icon_flashlight_white)
            mode = "eyes"
            Toast.makeText(this, "Режим распознавания морганий глазами", Toast.LENGTH_SHORT).show()
        } else if (mode == "eyes") {
            button.setImageResource(R.drawable.rec_act_icon_eyes_white)
            mode = "flashlight"
            Toast.makeText(this, "Режим распознавания вспышек фонаря", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pressChat() {
        if (tts.isSpeaking)
            tts.stop()
        setResult(RESULT_OK, Intent().putExtra("mess", recLetters))
        finish()
    }


    private fun speakLetter(letter: Char){
        if (ttsEnable and speakerButton.isHovered){
            tts.speak(letter.toString(), TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }
}