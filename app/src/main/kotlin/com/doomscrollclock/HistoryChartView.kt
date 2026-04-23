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
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class Bar(val label: String, val seconds: Long, val isToday: Boolean)

    private var bars: List<Bar> = emptyList()

    private val d: Float
    private val sp: Float
    private val rowH: Float
    private val barH: Float
    private val labelW: Float
    private val valueW: Float
    private val radius: Float
    private val rowGap: Float

    private val trackPaint: Paint
    private val pastPaint: Paint
    private val todayPaint: Paint
    private val labelPaint: Paint
    private val valuePaint: Paint
    private val todayLabelPaint: Paint
    private val todayValuePaint: Paint

    init {
        val dm = context.resources.displayMetrics
        d = dm.density
        sp = dm.scaledDensity

        rowH = 34f * d
        barH = 10f * d
        labelW = 44f * d
        valueW = 60f * d
        radius = barH / 2f
        rowGap = 10f * d

        trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.colorSurfaceVariant)
        }
        pastPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.colorBarPast)
        }
        todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.colorBarToday)
        }
        labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.colorOnSurfaceVariant)
            textSize = 11f * sp
        }
        valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.colorOnSurfaceVariant)
            textSize = 11f * sp
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.RIGHT
        }
        todayLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.colorPrimary)
            textSize = 11f * sp
            typeface = Typeface.DEFAULT_BOLD
        }
        todayValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.colorPrimary)
            textSize = 11f * sp
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
    }

    fun setData(data: List<Bar>) {
        bars = data
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val n = bars.size.coerceAtLeast(1)
        val h = (n.toFloat() * rowH + (n - 1).toFloat() * rowGap).toInt()
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        if (bars.isEmpty()) return
        val maxSecs = bars.maxOfOrNull { it.seconds }?.takeIf { it > 0L }?.toFloat() ?: 1f
        val barStart = labelW
        val barEnd = width.toFloat() - valueW - 4f * d
        val span = barEnd - barStart

        bars.forEachIndexed { i, bar ->
            val rowY = i.toFloat() * (rowH + rowGap)
            val barTop = rowY + (rowH - barH) / 2f
            val barBot = barTop + barH
            val textY = rowY + rowH / 2f + labelPaint.textSize * 0.37f

            canvas.drawRoundRect(RectF(barStart, barTop, barEnd, barBot), radius, radius, trackPaint)

            val fill = span * (bar.seconds.toFloat() / maxSecs)
            if (fill >= radius * 2f) {
                canvas.drawRoundRect(
                    RectF(barStart, barTop, barStart + fill, barBot),
                    radius, radius,
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
