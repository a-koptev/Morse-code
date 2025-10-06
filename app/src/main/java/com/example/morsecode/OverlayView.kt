package com.example.morsecode

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View


class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var circlePaint = Paint()

    private var circles = ArrayList<Array<Float>>()

    init {
        initPaints()
    }

    fun clear() {
        circlePaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        circlePaint.color = Color.RED
        circlePaint.strokeWidth = 3F
        circlePaint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (circle in circles) {
            canvas.drawCircle(circle[1],circle[0], circle[2], circlePaint)
        }
    }


    fun setCircles(newCircles: ArrayList<Array<Float>>) {
        circles = newCircles
    }

}