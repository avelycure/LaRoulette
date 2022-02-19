package com.avelycure.laroulette

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class Roulette : View {
    private var startAngle = 0F
    private lateinit var paint: Paint
    private var colorDark: Int = Color.BLACK
    private var colorLight: Int = Color.RED
    private var rouletteWidth: Int = 256
    private var rouletteHeight: Int = 256
    private val rouletteRadius: Int = 240
    private val rectF = RectF(0f, 0f, 0f, 0f)

    constructor(context: Context) : super(context) {
        initElements(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initElements(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initElements(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        initElements(attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 256
        val desiredHeight = 256
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        rouletteWidth = if (widthMode == MeasureSpec.EXACTLY) {
            widthSize
        } else if (widthMode == MeasureSpec.AT_MOST) {
            Math.min(desiredWidth, widthSize)
        } else {
            desiredWidth
        }

        rouletteHeight = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            Math.min(desiredHeight, heightSize)
        } else {
            desiredHeight
        }

        rectF.apply {
            left = x + (rouletteWidth - 2 * rouletteRadius) / 2
            right = x + (rouletteWidth + 2 * rouletteRadius) / 2
            top = y + (rouletteHeight - 2 * rouletteRadius) / 2
            bottom = y + (rouletteHeight + 2 * rouletteRadius) / 2
        }

        setMeasuredDimension(rouletteWidth, rouletteHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null)
            return

        paint.color = colorLight
        canvas.drawCircle(
            x + rouletteWidth / 2,
            y + rouletteHeight / 2,
            rouletteRadius.toFloat(),
            paint
        )

        paint.color = colorDark
        for (i in 0..6 step 2)
            canvas.drawArc(rectF, startAngle + i * 45, 45f, true, paint)

    }

    @SuppressLint("CustomViewStyleable")
    private fun initElements(set: AttributeSet?) {
        paint = Paint()

        if (set == null)
            return;
        val ta = context.obtainStyledAttributes(set, R.styleable.LaRoulette)

        colorLight = ta.getColor(R.styleable.LaRoulette_color_light, Color.RED)
        colorDark = ta.getColor(R.styleable.LaRoulette_color_dark, Color.BLACK)

        ta.recycle()
    }

    private var xDown = 0f
    private var yDown = 0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val value = super.onTouchEvent(event)

        if (event == null)
            return false;

        when (event.action) {
            MotionEvent.ACTION_UP -> {
                xDown = 0f
                yDown = 0f
            }
            MotionEvent.ACTION_DOWN -> {
                xDown = event.x
                yDown = event.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val xMove = event.x
                val yMove = event.y

                val circleCenterX = x + rouletteWidth / 2
                val circleCenterY = y + rouletteHeight / 2

                val dx2 = (xDown - circleCenterX) * (xDown - circleCenterX)
                val dy2 = (yDown - circleCenterY) * (yDown - circleCenterY)

                if (dx2 + dy2 < rouletteRadius * rouletteRadius) {
                    //we are inside roulette
                    startAngle = (Math.atan((circleCenterY - yMove) / (circleCenterX - xMove).toDouble()) * 57.28).toFloat()

                    postInvalidate()
                    return true
                }
                return value
            }
        }

        return value
    }

}