package com.avelycure.laroulette

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.lang.Exception
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.min
import kotlin.math.sqrt

class NewRoulette : View {

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

    private lateinit var h: Handler

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

        rouletteWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> {
                widthSize
            }
            MeasureSpec.AT_MOST -> {
                min(desiredWidth, widthSize)
            }
            else -> {
                desiredWidth
            }
        }

        rouletteHeight = if (heightMode == View.MeasureSpec.EXACTLY) {
            heightSize
        } else if (heightMode == View.MeasureSpec.AT_MOST) {
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
        h = object : Handler(context.mainLooper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val bundle = msg.data
                val phi = bundle.getFloat("phi")
                sweepAngle = phi
                postInvalidate()
                //Log.d("mytag", "Got from handler $startAngle")
            }
        }
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

    var thread: Thread? = null

    val MIN_SPEED = 1f

    var startTime = 0L

    var dt = 0f
    var dr = 0f

    val r = 0.03f
    var w0 = 0f
    var v0 = 0f

    var e = 0f
    var w: Float = 0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val value = super.onTouchEvent(event)

        if (event == null)
            return false;

        val dx2 = (event.x - circleCenterX) * (event.x - circleCenterX)
        val dy2 = (event.y - circleCenterY) * (event.y - circleCenterY)

        when (event.action) {
            MotionEvent.ACTION_UP -> {
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

                dt = (t2 - t1) / 1000f
                dr = sqrt(((x2 - x1) * (x2 - x1)) + (y2 - y1) * (y2 - y1)) / 100f

                v0 = (dr / dt) * chooseSpeedDirection() * 0.8f
                // radius in meters

                // accelerate m / s^2
                e = 100f * chooseSpeedDirection()

                // angle speed in 1/s
                val w0New = v0 / r

                synchronized(w) {
                    if (thread == null) {
                        w0 = w0New
                    } else {
                        w0 += w0New
                    }
                    w = w0
                    startTime = Calendar.getInstance().timeInMillis
                }

                if (thread == null) {
                    thread = thread {
                        // angle radians
                        var phi = 0f

                        var t = 0f

                        while (abs(w) > MIN_SPEED) {
                            if (thread?.isInterrupted == true) {
                                break
                            }

                            t = (Calendar.getInstance().timeInMillis - startTime) / 1000f

                            synchronized(w) {
                                w = w0 - e * t
                            }

                            phi = w0 * t - e * t * t / 2f

                            h.sendMessage(Message().apply {
                                data = Bundle().apply {
                                    putFloat("phi", phi)
                                }
                            })
                            try {
                                Thread.sleep(2)
                            } catch (e: InterruptedException) {
                                break
                            }
                        }
                        thread = null
                    }
                }

                return true
            }
            MotionEvent.ACTION_DOWN -> {
                if (dx2 + dy2 < rouletteRadius * rouletteRadius) {
                    startAngle = countAngle(event.x, event.y)
                    //stoppedRoullete = false
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

                    if (sqrt(((x2 - x1) * (x2 - x1)) + (y2 - y1) * (y2 - y1)) < 20)
                        thread?.interrupt()

                    sweepAngle = countAngle(event.x, event.y) - startAngle

                    postInvalidate()
                    return true
                }
            }
        }
        return value
    }

    // + -> clockwise
    // - -> counterclockwise
    private fun chooseSpeedDirection(): Int {
        if (x2 > circleCenterX && y2 < circleCenterY && x1 > circleCenterX && y1 < circleCenterY) {
            //in this domain we catch all up movements
            if (y2 < y1) {
                if (abs(atan((y2 - y1) / (x2 - x1))) * R_TO_D > 45)
                    return -1
            }
            Log.d("mytag", "1")
        } else if (x2 > circleCenterX && y2 > circleCenterY && x1 > circleCenterX && y1 > circleCenterY) {
            //in this domain we catch all right movements
            if (x2 > x1) {
                if (abs(atan((y2 - y1) / (x2 - x1))) * R_TO_D < 45)
                    return -1
            }
            Log.d("mytag", "2")
        } else if (x2 < circleCenterX && y2 > circleCenterY && x1 < circleCenterX && y1 > circleCenterY) {
            //in this domain we catch all down movements
            if (y2 > y1) {
                if (atan((y2 - y1) / (x2 - x1)) * R_TO_D > 45)
                    return -1
            }
            Log.d("mytag", "3")
        } else if (x2 < circleCenterX && y2 < circleCenterY && x1 < circleCenterX && y1 < circleCenterY) {
            //in this domain we catch all left movements
            if (x2 < x1) {
                if (abs(atan((y2 - y1) / (x2 - x1))) * R_TO_D < 45)
                    return -1
            }
            Log.d("mytag", "4")
        } else if (x2 < circleCenterX && y2 < circleCenterY && x1 > circleCenterX && y1 < circleCenterY) {
            Log.d("mytag", "5")
            return -1
        } else if (x2 > circleCenterX && y2 > circleCenterY && x1 < circleCenterX && y1 > circleCenterY) {
            Log.d("mytag", "6")
            return -1
        } else if (x2 > circleCenterX && y2 < circleCenterY && x1 > circleCenterX && y1 > circleCenterY) {
            Log.d("mytag", "7")
            return -1
        } else if (x2 < circleCenterX && y2 > circleCenterY && x1 < circleCenterX && y1 < circleCenterY) {
            Log.d("mytag", "8")
            return -1
        }
        return 1
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