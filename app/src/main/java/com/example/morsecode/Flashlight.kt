package com.example.morsecode

import android.content.Context
import android.hardware.camera2.CameraManager

class Flashlight(context: Context) {
    private val camManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var stopThread: Boolean = false


    fun flashChar(delays: List<Int>): String {
        for (delay in delays) {
            if (stopThread) {
                return "stop"
            }
            flashOn()
            Thread.sleep(delay.toLong())
            flashOff()
            Thread.sleep(200)
        }
        Thread.sleep(200)
        return "good"
    }

    fun stopBlinking() {
        stopThread = true
    }

    private fun flashOn() {
        camManager.setTorchMode(camManager.cameraIdList[0], true)
    }

    private fun flashOff() {
        camManager.setTorchMode(camManager.cameraIdList[0], false)
    }

    fun offStopThread(){
        stopThread = false
    }
}