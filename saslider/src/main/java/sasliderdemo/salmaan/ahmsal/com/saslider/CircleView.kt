package com.wisemani.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

/**
 * Created by salmaanahmed on 19/12/2017.
 */
class CircleView (
        context: Context,
        private val size: Int,
        color: Int
) : View(context) {

    private val paint: Paint = Paint()

    init {
        paint.color = color
    }

    override fun onDraw(canvas: Canvas) {
        var radius: Int = if (size == -1) {
            if (measuredHeight < measuredWidth) (measuredHeight/2) else (measuredWidth/2)
        } else {
            size
        }
        canvas.drawCircle((measuredWidth/2).toFloat(), (measuredHeight/2).toFloat(), radius.toFloat(), paint)
    }
}