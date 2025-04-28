package com.example.vkr_healthy_nutrition.ui.stats

import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.example.vkr_healthy_nutrition.data.WaterPeriodState
import com.example.vkr_healthy_nutrition.data.network.WaterRecordResponse
import java.text.SimpleDateFormat
import java.util.*

class MonthStatsFragment : BaseStatsFragment() {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val calendar = Calendar.getInstance()

    override fun getXAxisFormatter(): ValueFormatter {
        return object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt() + 1}"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadMonthWaterData()
    }

    override fun observeData() {
        viewModel.monthWaterState.observe(viewLifecycleOwner) { state ->
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
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dailyTotals = MutableList(daysInMonth) { 0 }
        
        records.forEach { record ->
            try {
                val date = dateFormat.parse(record.record_time)
                date?.let {
                    calendar.time = it
                    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH) - 1 // Преобразуем в индекс 0-based
                    dailyTotals[dayOfMonth] += record.amount
                }
            } catch (e: Exception) {
                // Обработка ошибок парсинга даты
            }
        }
        
        return dailyTotals
    }

    companion object {
        fun newInstance() = MonthStatsFragment()
    }
} 