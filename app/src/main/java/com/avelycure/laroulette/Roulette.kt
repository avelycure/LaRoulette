package com.avelycure.laroulette

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class Roulette : View {
    private lateinit var paint: Paint
    private var colorDark: Int = Color.BLACK
    private var colorLight: Int = Color.RED
    private var rouletteWidth: Int = 256
    private var rouletteHeight: Int = 256
    private val rouletteRadius: Int = 240

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

        setMeasuredDimension(rouletteWidth, rouletteHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null)
            return

        canvas.drawCircle(
            y + rouletteHeight / 2,
            x + rouletteWidth / 2,
            rouletteRadius.toFloat(),
            paint
        )
    }

    @SuppressLint("CustomViewStyleable")
    private fun initElements(set: AttributeSet?) {
        paint = Paint()

        if (set == null)
            return;
        val ta = context.obtainStyledAttributes(set, R.styleable.LaRoulette)

        colorLight = ta.getColor(R.styleable.LaRoulette_color_light, Color.RED)
        colorDark = ta.getColor(R.styleable.LaRoulette_color_dark, Color.BLACK)

        paint.color = colorLight

        ta.recycle()
    }

}