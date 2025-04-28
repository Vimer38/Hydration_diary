package com.example.vkr_healthy_nutrition.ui.stats

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class TimeAxisFormatter(private val records: List<String>) : ValueFormatter() {
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun getFormattedValue(value: Float): String {
        val index = value.toInt()
        if (index >= 0 && index < records.size) {
            try {
                val date = isoFormat.parse(records[index])
                return date?.let { timeFormat.format(it) } ?: ""
            } catch (e: Exception) {
                return ""
            }
        }
        return ""
    }
} 