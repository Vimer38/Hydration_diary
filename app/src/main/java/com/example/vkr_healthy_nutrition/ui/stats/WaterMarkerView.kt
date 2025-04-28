package com.example.vkr_healthy_nutrition.ui.stats

import android.content.Context
import android.widget.TextView
import com.example.vkr_healthy_nutrition.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class WaterMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val tvContent: TextView = findViewById(R.id.tvContent)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            tvContent.text = "${it.y.toInt()} мл"
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }
} 