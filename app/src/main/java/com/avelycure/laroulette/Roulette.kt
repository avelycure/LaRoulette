package com.avelycure.laroulette

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.avelycure.laroulette.RouletteConstants.ACCELERATION
import com.avelycure.laroulette.RouletteConstants.CIRCLE_PROPORTION
import com.avelycure.laroulette.RouletteConstants.DESIRED_HEIGHT
import com.avelycure.laroulette.RouletteConstants.DESIRED_WIDTH
import com.avelycure.laroulette.RouletteConstants.MAX_ROULETTE_SPEED
import com.avelycure.laroulette.RouletteConstants.MIN_SPEED
import com.avelycure.laroulette.RouletteConstants.PHI
import com.avelycure.laroulette.RouletteConstants.ROULETTE_RADIUS
import com.avelycure.laroulette.RouletteConstants.R_TO_D
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.*

//todo when film is chosen show text to user that he can see
//подборку этих фильмов и стрелочку влево
// добавить коллбек, чтобы когда рулетка докрутилась можно было бы выполнить команду пользователя
class Roulette : View {

    //ui
    private var colorDark: Int = 0
    private var colorLight: Int = 0
    private var textColor: Int = 0
    private val rectF = RectF(0f, 0f, 0f, 0f)
    private val rectText = RectF(0f, 0f, 0f, 0f)
    private val pointer = Path()

    //text
    private lateinit var textPath: Path
    private lateinit var data: List<String>
    private var textAngle = 0f

    //size
    private var rouletteWidth: Int = 256
    private var rouletteHeight: Int = 256
    private val rouletteRadius: Int = 240

    //coordinates
    private var start = 0F
    private var startAngle = 0F
    private var sweepAngle = 0F
    private var circleCenterX = 0f
    private var circleCenterY = 0f

    //touch
    private var x1 = 0f
    private var y1 = 0f
    private var t1 = 0L

    private var x2 = 0f
    private var y2 = 0f
    private var t2 = 0L

    //utils
    private lateinit var paint: Paint
    private lateinit var mHandler: Handler
    private var accelerationThread: Thread? = null
    private var x1WasLastModified: Boolean = false

    // acceleration animation
    private var e = ACCELERATION

    //angle speed
    private var w: Float = 0f

    //time of the beginning of acceleration
    private var startTime = 0L

    //delta time
    private var dt = 0f

    //delta radius vector
    private var dr = 0f

    //start angle speed
    private var w0 = 0f

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

    fun setData(
        data: List<String>,
        darkColor: Int = Color.BLACK,
        lightColor: Int = Color.RED,
        textColor: Int = Color.GRAY
    ) {
        this.data = data
        this.textColor = textColor
        textAngle = 360f / data.size
        colorDark = darkColor
        colorLight = lightColor
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = DESIRED_WIDTH
        val desiredHeight = DESIRED_HEIGHT
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        rouletteWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredWidth, widthSize)
            else -> desiredWidth
        }

        rouletteHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        rectF.apply {
            left = x + (rouletteWidth - 2 * rouletteRadius) / 2f
            right = x + (rouletteWidth + 2 * rouletteRadius) / 2f
            top = y + (rouletteHeight - 2 * rouletteRadius) / 2f
            bottom = y + (rouletteHeight + 2 * rouletteRadius) / 2f
        }

        rectText.apply {
            left = x + (rouletteWidth - 2 * rouletteRadius * CIRCLE_PROPORTION) / 2
            right = x + (rouletteWidth + 2 * rouletteRadius * CIRCLE_PROPORTION) / 2f
            top = y + (rouletteHeight - 2 * rouletteRadius * CIRCLE_PROPORTION) / 2f
            bottom = y + (rouletteHeight + 2 * rouletteRadius * CIRCLE_PROPORTION) / 2f
        }

        circleCenterX = x + rouletteWidth / 2f
        circleCenterY = y + rouletteHeight / 2f

        setMeasuredDimension(rouletteWidth, rouletteHeight)

        pointer.moveTo(x + rouletteWidth / 2f, y + rouletteHeight / 2f + 10)
        pointer.lineTo(x + rouletteWidth / 2f - 120, y + rouletteHeight / 2f)
        pointer.lineTo(x + rouletteWidth / 2f, y + rouletteHeight / 2f - 10)
        pointer.lineTo(x + rouletteWidth / 2f, y + rouletteHeight / 2f + 10)
        pointer.close()
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null)
            return

        //draw background
        paint.color = colorLight
        canvas.drawCircle(
            x + rouletteWidth / 2f,
            y + rouletteHeight / 2f,
            rouletteRadius.toFloat(),
            paint
        )

        //draw sectors
        paint.color = colorDark
        for (i in data.indices step 2)
            canvas.drawArc(rectF, start + sweepAngle + i * textAngle, textAngle, true, paint)

        //draw text
        paint.color = textColor
        paint.textSize = 30f
        for (i in data.indices) {
            textPath.reset()
            textPath.moveTo(
                circleCenterX,
                circleCenterY
            )
            paint.textAlign = Paint.Align.CENTER
            textPath.addArc(rectText, start + sweepAngle + i * textAngle, textAngle)
            canvas.drawTextOnPath(data[i], textPath, 0f, 0f, paint)
        }

        paint.color = Color.GRAY
        canvas.drawCircle(
            x + rouletteWidth / 2f,
            y + rouletteHeight / 2f,
            rouletteRadius * 0.1f,
            paint
        )

        canvas.drawPath(pointer, paint)

        paint.color = colorDark
        canvas.drawCircle(
            x + rouletteWidth / 2f,
            y + rouletteHeight / 2f,
            rouletteRadius * 0.05f,
            paint
        )

    }

    @SuppressLint("CustomViewStyleable")
    private fun initElements(set: AttributeSet?) {
        if (set == null)
            return

        paint = Paint()

        val ta = context.obtainStyledAttributes(set, R.styleable.LaRoulette)

        colorLight = ta.getColor(R.styleable.LaRoulette_color_light, Color.RED)
        colorDark = ta.getColor(R.styleable.LaRoulette_color_dark, Color.BLACK)

        textPath = Path()
        ta.recycle()

        mHandler = object : Handler(context.mainLooper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val bundle = msg.data
                val phi = bundle.getFloat(PHI)
                sweepAngle = phi
                postInvalidate()
            }
        }
    }

    private fun setX1X2Properly(event: MotionEvent) {
        if (x1WasLastModified) {
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
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val value = super.onTouchEvent(event)

        if (event == null)
            return false

        val dx2 = (event.x - circleCenterX) * (event.x - circleCenterX)
        val dy2 = (event.y - circleCenterY) * (event.y - circleCenterY)

        when (event.action) {
            MotionEvent.ACTION_UP -> {
                setX1X2Properly(event)

                dt = (t2 - t1) / 1000f
                dr = sqrt(((x2 - x1) * (x2 - x1)) + (y2 - y1) * (y2 - y1)) / 100f
                e = ACCELERATION * chooseSpeedDirection()

                val v0 = (dr / dt) * chooseSpeedDirection() * 0.8f
                val w0New = v0 / ROULETTE_RADIUS

                synchronized(w) {
                    if (accelerationThread == null)
                        w0 = w0New
                    else {
                        if (abs(w0New + w0) < MAX_ROULETTE_SPEED)
                            w0 += w0New
                        else
                            w0 = MAX_ROULETTE_SPEED * chooseSpeedDirection()
                    }
                    w = w0
                    startTime = Calendar.getInstance().timeInMillis
                }

                if (accelerationThread == null) {
                    accelerationThread = thread {
                        var phi = 0f
                        var t = 0f

                        while (abs(w) > MIN_SPEED) {
                            if (accelerationThread?.isInterrupted == true)
                                break
                            t = (Calendar.getInstance().timeInMillis - startTime) / 1000f

                            synchronized(w) {
                                w = w0 - e * t
                            }

                            phi = w0 * t - e * t * t / 2f

                            mHandler.sendMessage(Message().apply {
                                data = Bundle().apply {
                                    putFloat(PHI, phi)
                                }
                            })

                            try {
                                Thread.sleep(2)
                            } catch (e: InterruptedException) {
                                break
                            }
                        }
                        //start += sweepAngle
                        //sweepAngle = 0f

                        accelerationThread = null
                        postInvalidate()
                    }
                }
                x1WasLastModified = false
                x1 = 0f
                y1 = 0f
                t1 = 0L
                x2 = 0f
                y2 = 0f
                t2 = 0L
                return true
            }
            MotionEvent.ACTION_DOWN -> {
                if (dx2 + dy2 < rouletteRadius * rouletteRadius) {
                    start += sweepAngle
                    sweepAngle = 0f
                    startAngle = countAngle(event.x, event.y)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (dx2 + dy2 < rouletteRadius * rouletteRadius) {
                    //we are inside roulette
                    if (x1WasLastModified) {
                        x2 = event.x
                        y2 = event.y
                        t2 = event.eventTime
                    } else {
                        x1 = event.x
                        y1 = event.y
                        t1 = event.eventTime
                    }
                    x1WasLastModified = !x1WasLastModified
                    //if check is true -> 1 point is last modified(the end of the vector)
                    //if check is false -> 2 point is last modified(the end of the vector)

                    //if event is just pressing in one point then stop animation
                    if (sqrt(((x2 - x1) * (x2 - x1)) + (y2 - y1) * (y2 - y1)) < 20)
                        accelerationThread?.interrupt()

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
            if (y2 < y1)
                if (abs(atan((y2 - y1) / (x2 - x1))) * R_TO_D > 45)
                    return -1
        } else if (x2 > circleCenterX && y2 > circleCenterY && x1 > circleCenterX && y1 > circleCenterY) {
            //in this domain we catch all right movements
            if (x2 > x1)
                if (abs(atan((y2 - y1) / (x2 - x1))) * R_TO_D < 45)
                    return -1
        } else if (x2 < circleCenterX && y2 > circleCenterY && x1 < circleCenterX && y1 > circleCenterY) {
            //in this domain we catch all down movements
            if (y2 > y1)
                if (atan((y2 - y1) / (x2 - x1)) * R_TO_D > 45)
                    return -1
        } else if (x2 < circleCenterX && y2 < circleCenterY && x1 < circleCenterX && y1 < circleCenterY) {
            //in this domain we catch all left movements
            if (x2 < x1)
                if (abs(atan((y2 - y1) / (x2 - x1))) * R_TO_D < 45)
                    return -1
        } else if (x2 < circleCenterX && y2 < circleCenterY && x1 > circleCenterX && y1 < circleCenterY)
            return -1
        else if (x2 > circleCenterX && y2 > circleCenterY && x1 < circleCenterX && y1 > circleCenterY)
            return -1
        else if (x2 > circleCenterX && y2 < circleCenterY && x1 > circleCenterX && y1 > circleCenterY)
            return -1
        else if (x2 < circleCenterX && y2 > circleCenterY && x1 < circleCenterX && y1 < circleCenterY)
            return -1
        return 1
    }

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