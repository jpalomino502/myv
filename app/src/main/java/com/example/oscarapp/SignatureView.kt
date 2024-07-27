package com.example.oscarapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ScrollView
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.StringBuilderWriter
import java.io.PrintWriter

class SignatureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private val path = android.graphics.Path()
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private val paintWidth = 5f
    private var parentScrollView: ScrollView? = null
    private val lineHeight = 10f // Define the height for the signature line

    init {
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        paint.strokeWidth = paintWidth
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
        // Draw the signature line at the bottom
        val lineY = height - lineHeight
        canvas.drawLine(0f, lineY, width.toFloat(), lineY, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(event.x, event.y)
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                path.quadTo(lastTouchX, lastTouchY, event.x, event.y)
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_UP -> {
                path.lineTo(lastTouchX, lastTouchY)
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        invalidate()
        return true
    }

    fun clear() {
        path.reset()
        invalidate()
    }

    fun getSignatureBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }

    fun toSvg(): String {
        val stringWriter = StringBuilderWriter()
        val writer = PrintWriter(stringWriter)

        writer.println("""<?xml version="1.0" encoding="UTF-8"?>""")
        writer.println("""<svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height">""")

        val bitmap = getSignatureBitmap()
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = pixels[y * width + x]
                if (Color.alpha(pixel) > 0) { // Only draw non-transparent pixels
                    val color = Color.rgb(Color.red(pixel), Color.green(pixel), Color.blue(pixel))
                    writer.println("""<rect x="$x" y="$y" width="1" height="1" fill="rgb($color)" />""")
                }
            }
        }

        writer.println("""</svg>""")
        writer.close()
        return stringWriter.toString()
    }
}
