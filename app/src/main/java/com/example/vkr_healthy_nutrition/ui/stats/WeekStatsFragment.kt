package com.example.vkr_healthy_nutrition.ui.stats

import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.example.vkr_healthy_nutrition.data.WaterPeriodState
import com.example.vkr_healthy_nutrition.data.network.WaterRecordResponse
import java.text.SimpleDateFormat
import java.util.*

class WeekStatsFragment : BaseStatsFragment() {
    private val dayNames = arrayOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val calendar = Calendar.getInstance()

    override fun getXAxisFormatter(): ValueFormatter {
        return object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return dayNames[value.toInt()]
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadWeekWaterData()
    }

    override fun observeData() {
        viewModel.weekWaterState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WaterPeriodState.Success -> {
                    val dailyTotals = calculateDailyTotals(state.records)
                    val entries = dailyTotals.mapIndexed { index, total ->
                        BarEntry(index.toFloat(), total.toFloat())
                    }
                    updateChartData(entries)
                    recommendedDailyIntake = state.goal.toFloat()
                    updateRecommendedLine()
                }
                is WaterPeriodState.Error -> {
                    // Можно добавить обработку ошибок
                }
                else -> {}
            }
        }
    }

    private fun calculateDailyTotals(records: List<WaterRecordResponse>): List<Int> {
        val dailyTotals = MutableList(7) { 0 }
        
        records.forEach { record ->
            try {
                val date = dateFormat.parse(record.record_time)
                date?.let {
                    calendar.time = it
                    val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Преобразуем в индекс 0-6, где 0 - понедельник
                    dailyTotals[dayOfWeek] += record.amount
                }
            } catch (e: Exception) {
                // Обработка ошибок парсинга даты
            }
        }
        
        return dailyTotals
    }

    companion object {
        fun newInstance() = WeekStatsFragment()
    }
} 