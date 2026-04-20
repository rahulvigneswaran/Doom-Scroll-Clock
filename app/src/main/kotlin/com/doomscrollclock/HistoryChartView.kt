package com.doomscrollclock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class HistoryChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    data class Bar(val label: String, val seconds: Long, val isToday: Boolean)

    private val d = resources.displayMetrics.density
    private val sp = resources.displayMetrics.scaledDensity
    private var bars: List<Bar> = emptyList()

    private val ROW_H = 34 * d
    private val BAR_H = 10 * d
    private val LABEL_W = 44 * d
    private val VALUE_W = 60 * d
    private val RADIUS = BAR_H / 2f
    private val ROW_GAP = 10 * d

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorSurfaceVariant)
    }
    private val pastPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorBarPast)
    }
    private val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorBarToday)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorOnSurfaceVariant)
        textSize = 11 * sp
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorOnSurfaceVariant)
        textSize = 11 * sp
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.RIGHT
    }
    private val todayLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorPrimary)
        textSize = 11 * sp
        typeface = Typeface.DEFAULT_BOLD
    }
    private val todayValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorPrimary)
        textSize = 11 * sp
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.RIGHT
    }

    fun setData(data: List<Bar>) {
        bars = data
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val n = bars.size.coerceAtLeast(1)
        val h = (n * ROW_H + (n - 1) * ROW_GAP).toInt()
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        if (bars.isEmpty()) return
        val maxSecs = bars.maxOfOrNull { it.seconds }?.takeIf { it > 0L } ?: 1L
        val barStart = LABEL_W
        val barEnd = width - VALUE_W - 4 * d
        val span = barEnd - barStart

        bars.forEachIndexed { i, bar ->
            val rowY = i * (ROW_H + ROW_GAP)
            val barTop = rowY + (ROW_H - BAR_H) / 2f
            val barBot = barTop + BAR_H
            val textY = rowY + ROW_H / 2f + labelPaint.textSize * 0.37f

            canvas.drawRoundRect(RectF(barStart, barTop, barEnd, barBot), RADIUS, RADIUS, trackPaint)

            val fill = span * (bar.seconds.toFloat() / maxSecs)
            if (fill >= RADIUS * 2) {
                canvas.drawRoundRect(
                    RectF(barStart, barTop, barStart + fill, barBot),
                    RADIUS, RADIUS,
                    if (bar.isToday) todayPaint else pastPaint
                )
            }

            canvas.drawText(
                bar.label, 0f, textY,
                if (bar.isToday) todayLabelPaint else labelPaint
            )
            val t = if (bar.seconds > 0L) TimerManager.formatSeconds(bar.seconds) else "—"
            canvas.drawText(
                t, width.toFloat(), textY,
                if (bar.isToday) todayValuePaint else valuePaint
            )
        }
    }
}
