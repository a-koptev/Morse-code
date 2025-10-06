package com.example.morsecode

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.method.KeyListener
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


class MainActivity : AppCompatActivity() {
    private lateinit var messageView: RecyclerView
    private lateinit var messageViewAdapter: MessageAdapter
    private lateinit var inputMessField: EditText
    private lateinit var welcomePlate: LinearLayout
    private lateinit var flashlight: Flashlight
    private lateinit var mainHandler: Handler
    private lateinit var defaultKeyListener: KeyListener
    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        window.navigationBarColor = getColor(R.color.basicBlue)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        messageView = findViewById(R.id.messageView)
        inputMessField = findViewById(R.id.inputMessage)
        welcomePlate = findViewById(R.id.welcome_plate)
        messageViewAdapter = MessageAdapter()

        flashlight = Flashlight(this)
        mainHandler = Handler(Looper.getMainLooper())
        defaultKeyListener = inputMessField.keyListener

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            addRecMess(it.data?.getStringExtra("mess").toString())
        }

        buttonsFlip()
        messageViewInit()
        setMainFieldView("Welcome Plate")
    }


    private fun buttonsFlip() {
        val outButton: ImageButton = findViewById(R.id.outButton)
        val innerButton: ImageButton = findViewById(R.id.innerButton)

        outButton.setOnClickListener { goToReceive() }

        inputMessField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (inputMessField.text.isBlank()) {
                    outButton.setImageResource(R.drawable.main_act_icon_camera_blue)
                    innerButton.visibility = View.INVISIBLE
                    outButton.setOnClickListener { goToReceive() }

                } else if (inputMessField.keyListener != null){
                    outButton.setImageResource(R.drawable.main_act_icon_send)
                    innerButton.visibility = View.VISIBLE
                    outButton.setOnClickListener { startBlink(it as ImageButton) }
                    innerButton.setOnClickListener { goToReceive() }
                }
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }


    private fun startBlink(outButton: ImageButton) {
        val encoder = MorseCoder()

        outButton.setImageResource(R.drawable.main_act_icon_stop_translation)
        outButton.setOnClickListener { stopBlink() }

        inputMessField.keyListener = null
        inputMessField.setTextColor(getColor(R.color.gray))

        val encodeInpText: List<List<List<Int>>> = encoder.encode(inputMessField.text.toString())

        val tr = Thread {
            var idx = 0
            flashlight.offStopThread()
            for (word in encodeInpText) {
                for (char in word) {
                    mainHandler.post { setCharColor(idx) }
                    if (flashlight.flashChar(char) == "stop") {
                        return@Thread
                    }
                    idx++
                }
                idx++
                Thread.sleep(200)
            }
            mainHandler.post {
                messageViewAdapter.addNewMessage(
                    Message(inputMessField.text.toString().trim(), "send"))
                inputMessField.keyListener = defaultKeyListener
                inputMessField.text.clear()
                setMainFieldView("Message View")
            }
        }
        tr.start()

    }

    private fun stopBlink(){
        val outButton: ImageButton = findViewById(R.id.outButton)
        flashlight.stopBlinking()

        mainHandler.post {
            inputMessField.keyListener = defaultKeyListener
            inputMessField.setTextColor(getColor(R.color.white))
            if (inputMessField.text.isBlank()){
                outButton.setImageResource(R.drawable.main_act_icon_camera_blue)
                outButton.setOnClickListener { goToReceive() }
            } else {
                outButton.setImageResource(R.drawable.main_act_icon_send)
                outButton.setOnClickListener { startBlink(outButton) }
            }
        }
    }

    private fun setCharColor(idx: Int) {
        val text = inputMessField.text.toString()
        val spannableText = SpannableString(text)

        val fcs = ForegroundColorSpan(getColor(R.color.white))
        spannableText.setSpan(fcs, 0, idx + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        inputMessField.setText(spannableText)
        inputMessField.setSelection(idx + 1)
    }

    private fun messageViewInit() {
        messageView.layoutManager = LinearLayoutManager(this)
        messageView.adapter = messageViewAdapter
    }

    private fun setMainFieldView(viewName: String) {
        if (viewName == "Welcome Plate") {
            welcomePlate.visibility = View.VISIBLE
            messageView.visibility = View.GONE
        } else if (viewName == "Message View") {
            welcomePlate.visibility = View.GONE
            messageView.visibility = View.VISIBLE
        }
    }

    private fun goToReceive(){
        stopBlink()
        launcher.launch(Intent(this, ReceiveActivity::class.java))
    }

    private fun addRecMess(mess: String){
        if (mess.isBlank())
            return
        setMainFieldView("Message View")
        messageViewAdapter.addNewMessage(
            Message(mess, "receive")
        )
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }


    fun convertToGrayScale(input: Mat): Mat {
        val gray = Mat()
        Imgproc.cvtColor(input, gray, Imgproc.COLOR_BGR2GRAY)
        return gray
    }
}