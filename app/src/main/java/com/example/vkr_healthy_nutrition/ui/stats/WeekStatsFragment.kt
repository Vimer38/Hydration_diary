package com.example.vkr_healthy_nutrition.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.vkr_healthy_nutrition.HealthyNutritionApp
import com.example.vkr_healthy_nutrition.R
import com.example.vkr_healthy_nutrition.data.local.WaterIntakeEntity
import com.example.vkr_healthy_nutrition.ui.viewmodel.WaterIntakeViewModel
import com.example.vkr_healthy_nutrition.ui.viewmodel.WaterIntakeViewModelFactory
import com.example.vkr_healthy_nutrition.ui.viewmodel.WaterIntakeState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.Calendar
import java.util.concurrent.TimeUnit

class WeekStatsFragment : Fragment() {
    private val viewModel: WaterIntakeViewModel by activityViewModels {
        (requireActivity().application as HealthyNutritionApp).waterIntakeViewModelFactory
    }

    private lateinit var statsChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stats_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        statsChart = view.findViewById(R.id.stats_text_view)
        statsChart.setBackgroundColor(android.graphics.Color.WHITE)
        observeViewModel()
        loadWeekStats()
    }

    private fun loadWeekStats() {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val endOfDay = calendar.timeInMillis
        calendar.add(java.util.Calendar.DAY_OF_MONTH, -6)
        val startOfWeek = calendar.timeInMillis
        viewModel.loadStatsForPeriod(startOfWeek, endOfDay)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is WaterIntakeState.Success -> {
                        updateStatsUI(state.records)
                    }
                    is WaterIntakeState.Error -> {
                        println("Ошибка: ${state.message}")
                        statsChart.setNoDataText("Ошибка: ${state.message}")
                        statsChart.invalidate()
                    }
                    is WaterIntakeState.Loading -> {
                        println("Загрузка...")
                        statsChart.setNoDataText("Загрузка...")
                        statsChart.invalidate()
                    }
                    is WaterIntakeState.Idle -> {
                        statsChart.setNoDataText("Нет данных")
                        statsChart.invalidate()
                    }
                }
            }
        }
    }

    private fun updateStatsUI(waterIntakeList: List<WaterIntakeEntity>) {
        println("Обновление графика с данными за неделю: $waterIntakeList")

        if (waterIntakeList.isEmpty()) {
            statsChart.setNoDataText("Нет данных за неделю")
            statsChart.invalidate()
            return
        }

        val entriesByDay = waterIntakeList.groupBy { record: WaterIntakeEntity ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = record.timestamp
            calendar.get(Calendar.DAY_OF_WEEK)
        }.mapValues { (_, recordsInDay: List<WaterIntakeEntity>) ->
            recordsInDay.sumOf { it.amount }
        }

        val entries = (1..7).map { dayOfWeek ->
            val totalAmount = entriesByDay[getDayOfWeekConstant(dayOfWeek)] ?: 0
            BarEntry(getDayOfWeekIndex(dayOfWeek).toFloat(), totalAmount.toFloat())
        }.sortedBy { it.x }

        val dataSet = BarDataSet(entries, "Объем воды (мл)")
        dataSet.color = android.graphics.Color.BLUE

        val barData = BarData(dataSet)
        barData.barWidth = 0.8f

        statsChart.data = barData

        statsChart.description.isEnabled = false
        statsChart.legend.isEnabled = true

        val xAxis = statsChart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.textColor = android.graphics.Color.BLACK
        xAxis.axisLineColor = android.graphics.Color.BLACK

        val weekdays = arrayOf("Вс", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб")
        xAxis.valueFormatter = IndexAxisValueFormatter(weekdays)
        xAxis.setLabelCount(weekdays.size, false)

        val yAxis = statsChart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.granularity = 100f
        yAxis.textColor = android.graphics.Color.BLACK
        yAxis.axisLineColor = android.graphics.Color.BLACK
        yAxis.setDrawGridLines(false)

        statsChart.axisRight.isEnabled = false
        statsChart.setDrawGridBackground(false)
        statsChart.setDrawBarShadow(false)
        statsChart.setDrawValueAboveBar(true)

        statsChart.invalidate()
    }

    private fun getDayOfWeekConstant(dayOfWeek: Int): Int {
        return when (dayOfWeek) {
            1 -> Calendar.SUNDAY
            2 -> Calendar.MONDAY
            3 -> Calendar.TUESDAY
            4 -> Calendar.WEDNESDAY
            5 -> Calendar.THURSDAY
            6 -> Calendar.FRIDAY
            7 -> Calendar.SATURDAY
            else -> throw IllegalArgumentException("Invalid day of week: $dayOfWeek")
        }
    }

    private fun getDayOfWeekIndex(dayOfWeek: Int): Int {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> 0
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> throw IllegalArgumentException("Invalid day of week constant: $dayOfWeek")
        }
    }
} 