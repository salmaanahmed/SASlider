package com.wisemani.patienttouch.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View


/**
 * Created by salmaanahmed on 21/11/2017.
 */
class TriangleShapeView : View {

    var colorCode = Color.DKGRAY

    constructor(context: Context) : super(context) {
        if (isInEditMode())
            return
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        if (isInEditMode())
            return
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {

        if (isInEditMode())
            return
    }

    constructor(context: Context, color: Int) : super(context) {
        colorCode = color
        if (isInEditMode())
            return
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = getWidth() / 2
        val h = getHeight() / 2

        //Choose what type of triangle you want here
        val path = getInvertedTriangle(w, h)

        path.close()
        val p = Paint()
        p.setColor(colorCode)
        p.setAntiAlias(true)

        canvas.drawPath(path, p)
    }

    private
            /**
             * Return Path for down facing triangle
             */
    fun getInvertedTriangle(w: Int, h: Int): Path {
        val path = Path()
        path.moveTo(0F, 0F)
        path.lineTo(w.toFloat(), 2F * h)
        path.lineTo(2F * w, 0F)
        path.lineTo(0F, 0F)
        return path
    }

    private
            /**
             * Return Path for Up facing triangle
             */
    fun getUpTriangle(w: Int, h: Int): Path {
        val path = Path()
        path.moveTo(0F, 2F * h)
        path.lineTo(w.toFloat(), 0F)
        path.lineTo(2F * w, 2F * h)
        path.lineTo(0F, 2F * h)
        return path
    }

    private
            /**
             * Return Path for Right pointing triangle
             */
    fun getRightTriangle(w: Int, h: Int): Path {
        val path = Path()
        path.moveTo(0F, 0F)
        path.lineTo(2F * w, h.toFloat())
        path.lineTo(0F, 2F * h)
        path.lineTo(0F, 0F)
        return path
    }

    private
            /**
             * Return Path for Left pointing triangle
             */
    fun getLeftTriangle(w: Int, h: Int): Path {
        val path = Path()
        path.moveTo(2F * w, 0F)
        path.lineTo(0F, h.toFloat())
        path.lineTo(2F * w, 2F * h)
        path.lineTo(2F * w, 0F)
        return path
    }
}