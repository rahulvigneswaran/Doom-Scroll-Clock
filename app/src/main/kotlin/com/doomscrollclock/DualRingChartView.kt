package com.doomscrollclock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class DualRingChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val d = context.resources.displayMetrics.density
    private val sp = context.resources.displayMetrics.scaledDensity

    private var todaySeconds: Long = 0L
    private var weeklyMaxSeconds: Long = 1L
    private var levelProgress: Float = 0f
    private var displayValue: String = "0m"
    private var displayUnit: String = "today"

    // Outer ring (pink) — r=52dp, strokeWidth=11dp
    private val outerRadius = 52f * d
    private val outerStroke = 11f * d
    private val outerTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = outerStroke
        strokeCap = Paint.Cap.ROUND
        color = Color.argb((0.08f * 255).toInt(), 255, 255, 255)
    }
    private val outerArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = outerStroke
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#EF5DA8")
    }

    // Inner ring (lavender) — r=36dp, strokeWidth=9dp
    private val innerRadius = 36f * d
    private val innerStroke = 9f * d
    private val innerTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = innerStroke
        strokeCap = Paint.Cap.ROUND
        color = Color.argb((0.06f * 255).toInt(), 255, 255, 255)
    }
    private val innerArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = innerStroke
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#AEAFF7")
    }

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 22f * sp
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb((0.45f * 255).toInt(), 255, 255, 255)
        textSize = 11f * sp
        textAlign = Paint.Align.CENTER
    }

    fun setData(todaySeconds: Long, weeklyMaxSeconds: Long, levelProgress: Float, mode: String) {
        this.todaySeconds = todaySeconds
        this.weeklyMaxSeconds = weeklyMaxSeconds.coerceAtLeast(1L)
        this.levelProgress = levelProgress.coerceIn(0f, 1f)
        if (mode == "distance") {
            val metres = TimerManager.getScrollMetres()
            displayValue = FunFacts.formatDistance(metres)
            displayUnit = "scrolled"
        } else {
            val mins = todaySeconds / 60
            displayValue = if (mins >= 60) "${mins / 60}h ${mins % 60}m" else "${mins}m"
            displayUnit = "today"
        }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = (120 * d).toInt()
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f

        // Outer ring track (full circle)
        val outerRect = RectF(cx - outerRadius, cy - outerRadius, cx + outerRadius, cy + outerRadius)
        canvas.drawArc(outerRect, -90f, 360f, false, outerTrackPaint)

        // Outer ring arc (today vs weekly max)
        val outerSweep = 360f * (todaySeconds.toFloat() / weeklyMaxSeconds).coerceIn(0f, 1f)
        if (outerSweep > 0f) canvas.drawArc(outerRect, -90f, outerSweep, false, outerArcPaint)

        // Inner ring track
        val innerRect = RectF(cx - innerRadius, cy - innerRadius, cx + innerRadius, cy + innerRadius)
        canvas.drawArc(innerRect, -90f, 360f, false, innerTrackPaint)

        // Inner ring arc (level progress)
        val innerSweep = 360f * levelProgress
        if (innerSweep > 0f) canvas.drawArc(innerRect, -90f, innerSweep, false, innerArcPaint)

        // Center text
        val textY = cy - (valuePaint.descent() + valuePaint.ascent()) / 2f
        canvas.drawText(displayValue, cx, textY, valuePaint)
        canvas.drawText(displayUnit, cx, textY + valuePaint.textSize * 1.1f, unitPaint)
    }
}
