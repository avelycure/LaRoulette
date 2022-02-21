package com.avelycure.laroulette

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationSet
import android.view.animation.RotateAnimation
import java.lang.Math.*
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.min

class Roulette : View {

    private var start = 0F
    private var startAngle = 0F
    private var sweepAngle = 0F

    private var colorDark: Int = Color.BLACK
    private var colorLight: Int = Color.RED

    private var rouletteWidth: Int = 256
    private var rouletteHeight: Int = 256
    private val rouletteRadius: Int = 240

    private lateinit var paint: Paint
    private val rectF = RectF(0f, 0f, 0f, 0f)
    private val rectText = RectF(0f, 0f, 0f, 0f)
    private lateinit var data: List<String>

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
            min(desiredWidth, widthSize)
        } else {
            desiredWidth
        }

        rouletteHeight = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            min(desiredHeight, heightSize)
        } else {
            desiredHeight
        }

        rectF.apply {
            left = x + (rouletteWidth - 2 * rouletteRadius) / 2
            right = x + (rouletteWidth + 2 * rouletteRadius) / 2
            top = y + (rouletteHeight - 2 * rouletteRadius) / 2
            bottom = y + (rouletteHeight + 2 * rouletteRadius) / 2
        }

        rectText.apply {
            left = x + (rouletteWidth - 2 * rouletteRadius / 1.21f) / 2
            right = x + (rouletteWidth + 2 * rouletteRadius / 1.21f) / 2
            top = y + (rouletteHeight - 2 * rouletteRadius / 1.21f) / 2
            bottom = y + (rouletteHeight + 2 * rouletteRadius / 1.21f) / 2
        }

        circleCenterX = x + rouletteWidth / 2
        circleCenterY = y + rouletteHeight / 2

        setMeasuredDimension(rouletteWidth, rouletteHeight)
    }

    private lateinit var path: Path

    //todo add functionality to transform decart coordinates to polar
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
        for (i in 0 until data.size step 2)
            canvas.drawArc(rectF, start + sweepAngle + i * 45, 45f, true, paint)

        paint.color = Color.BLACK
        paint.textSize = 30f
        for (i in data.indices) {
            path.reset()
            path.moveTo(
                circleCenterX,
                circleCenterY
            )
//            path.lineTo(
//                (rouletteRadius * cos(start + sweepAngle + i * 45.0)).toFloat(),
//                (rouletteRadius * sin(start + sweepAngle + i * 45.0)).toFloat()
//            )
            path.addArc(rectText, start + sweepAngle + i * 45, 45f)
            canvas.drawTextOnPath(data.get(i), path, 10f, 10f, paint)
        }

    }

    @SuppressLint("CustomViewStyleable")
    private fun initElements(set: AttributeSet?) {
        paint = Paint()

        if (set == null)
            return;
        val ta = context.obtainStyledAttributes(set, R.styleable.LaRoulette)

        colorLight = ta.getColor(R.styleable.LaRoulette_color_light, Color.RED)
        colorDark = ta.getColor(R.styleable.LaRoulette_color_dark, Color.BLACK)

        data = listOf("Action", "Drama", "Comedy", "Horror", "TvShow", "Cartoon", "War", "History")
        path = Path()
        ta.recycle()
    }

    private var circleCenterX = 0f
    private var circleCenterY = 0f

    private var x1 = 0f
    private var y1 = 0f
    private var t1 = 0L

    private var x2 = 0f
    private var y2 = 0f
    private var t2 = 0L

    private var check: Boolean = false

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val value = super.onTouchEvent(event)

        if (event == null)
            return false;

        val dx2 = (event.x - circleCenterX) * (event.x - circleCenterX)
        val dy2 = (event.y - circleCenterY) * (event.y - circleCenterY)

        when (event.action) {
            MotionEvent.ACTION_UP -> {
                //change this
                if (x1.toInt() != 0) {

                    //set x2 to the end, x1 to the start of the vector
                    if (check) {
                        var temp = x2
                        x2 = x1
                        x1 = temp

                        temp = y2
                        y2 = y1
                        y1 = temp

                        val temp2 = t2
                        t2 = t1
                        t1 = temp2
                    }
                    x2 = event.x
                    y2 = event.y
                    t2 = event.eventTime

                    val animationSet = AnimationSet(false).apply {
                        interpolator = AccelerateInterpolator()
                    }
                    val r = sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1).toDouble())
                    //to prevent dividing on zero
                    if (t2 == t1)
                        t2 += 1
                    val v = r / abs(t2 - t1)
                    //dependency from angle is nedded in physic sense
                    var animationDegrees: Float = abs(v * sweepAngle).toFloat()

                    //if conter clockwize then negative
                    //if clockwize then positive
                    //may be abs for atan???
                    //check for bugs later
                    if (x2 > circleCenterX && y2 < circleCenterY && x1 > circleCenterX && y1 < circleCenterY) {
                        //in this domain we catch all up movements
                        if (y2 < y1) {
                            if (abs(atan((y2 - y1) / (x2 - x1))) * R_TO_D > 45)
                                animationDegrees *= -1
                        }
                        Log.d("mytag", "1")
                    } else if (x2 > circleCenterX && y2 > circleCenterY && x1 > circleCenterX && y1 > circleCenterY) {
                        //in this domain we catch all right movements
                        if (x2 > x1) {
                            if (abs(atan((y2 - y1) / (x2 - x1))) * R_TO_D < 45)
                                animationDegrees *= -1
                        }
                        Log.d("mytag", "2")
                    } else if (x2 < circleCenterX && y2 > circleCenterY && x1 < circleCenterX && y1 > circleCenterY) {
                        //in this domain we catch all down movements
                        if (y2 > y1) {
                            if (atan((y2 - y1) / (x2 - x1)) * R_TO_D > 45)
                                animationDegrees *= -1
                        }
                        Log.d("mytag", "3")
                    } else if (x2 < circleCenterX && y2 < circleCenterY && x1 < circleCenterX && y1 < circleCenterY) {
                        //in this domain we catch all left movements
                        if (x2 < x1) {
                            if (abs(atan((y2 - y1) / (x2 - x1))) * R_TO_D < 45)
                                animationDegrees *= -1
                        }
                        Log.d("mytag", "4")
                    } else if (x2 < circleCenterX && y2 < circleCenterY && x1 > circleCenterX && y1 < circleCenterY) {
                        animationDegrees *= -1
                        Log.d("mytag", "5")
                    } else if (x2 > circleCenterX && y2 > circleCenterY && x1 < circleCenterX && y1 > circleCenterY) {
                        animationDegrees *= -1
                        Log.d("mytag", "6")
                    } else if (x2 > circleCenterX && y2 < circleCenterY && x1 > circleCenterX && y1 > circleCenterY) {
                        animationDegrees *= -1
                        Log.d("mytag", "7")
                    } else if (x2 < circleCenterX && y2 > circleCenterY && x1 < circleCenterX && y1 < circleCenterY) {
                        animationDegrees *= -1
                        Log.d("mytag", "8")
                    }
                    Log.d(
                        "mytag",
                        "x1: ${x1}, x2: ${x2}, y1: ${y1}, y2: ${y2}, cx:${circleCenterX}, , cy:${circleCenterY}"
                    )

                    animationSet.addAnimation(
                        RotateAnimation(
                            0f,//50f,//start + sweepAngle,
                            animationDegrees,//150f,//start + sweepAngle + animationDegrees,
                            circleCenterX,
                            circleCenterY
                        ).apply {
                            duration = 1000
                        }
                    )
                    startAnimation(animationSet)
                    animationSet.fillAfter = true

                    start += sweepAngle + animationDegrees
                    //start += sweepAngle
                    sweepAngle = 0f

                    while (start > 360 || start < 0) {
                        if (start < 0)
                            start += 360
                        if (start > 360)
                            start -= 360
                    }
                    check = false
                    x1 = 0f
                    y1 = 0f
                    t1 = 0L
                    x2 = 0f
                    y2 = 0f
                    t2 = 0L

                    //needed to make animation changes
                    postInvalidate()
                    //Log.d("mytag", "start: ${start}, AD: ${animationDegrees}, sweep: ${sweepAngle}")
                    return true
                }
            }
            MotionEvent.ACTION_DOWN -> {
                if (dx2 + dy2 < rouletteRadius * rouletteRadius) {
                    //clearAnimation()
                    startAngle = countAngle(event.x, event.y)
                    //postInvalidate()
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (dx2 + dy2 < rouletteRadius * rouletteRadius) {
                    //we are inside roulette
                    if (check) {
                        x2 = event.x
                        y2 = event.y
                        t2 = event.eventTime
                    } else {
                        x1 = event.x
                        y1 = event.y
                        t1 = event.eventTime
                    }
                    check = !check
                    //if check is true -> 1 point is last modified(the end of the vector)
                    //if check is false -> 2 point is last modified(the end of the vector)

                    sweepAngle = countAngle(event.x, event.y) - startAngle

                    postInvalidate()
                    return true
                }
            }
        }
        return value
    }

    val R_TO_D = 57.2958
    private fun countAngle(x1: Float, y1: Float): Float {
        var result = 0f
        if (x1 > circleCenterX && y1 > circleCenterY)
            result =
                ((atan((y1 - circleCenterY) / (x1 - circleCenterX).toDouble())) * R_TO_D).toFloat()
        if (x1 < circleCenterX && y1 > circleCenterY)
            result =
                ((Math.PI / 2.0 + abs(atan((x1 - circleCenterX) / (y1 - circleCenterY).toDouble()))) * R_TO_D).toFloat()
        if (x1 < circleCenterX && y1 < circleCenterY)
            result =
                ((Math.PI + abs(atan((y1 - circleCenterY) / (x1 - circleCenterX).toDouble()))) * R_TO_D).toFloat()
        if (x1 > circleCenterX && y1 < circleCenterY)
            result =
                ((Math.PI * (3.0 / 2.0) + abs(atan((x1 - circleCenterX) / (y1 - circleCenterY).toDouble()))) * R_TO_D).toFloat()
        return result
    }

}