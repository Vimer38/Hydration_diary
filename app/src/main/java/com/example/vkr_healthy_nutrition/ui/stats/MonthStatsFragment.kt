package com.example.vkr_healthy_nutrition.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.vkr_healthy_nutrition.core.HealthyNutritionApp
import com.example.vkr_healthy_nutrition.R
import com.example.vkr_healthy_nutrition.data.local.entities.WaterIntakeEntity
import com.example.vkr_healthy_nutrition.ui.viewmodel.WaterIntakeViewModel
import com.example.vkr_healthy_nutrition.ui.viewmodel.WaterIntakeState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

class MonthStatsFragment : Fragment() {
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
        loadMonthStats()
    }

    private fun loadMonthStats() {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        val endOfMonthCalendar = java.util.Calendar.getInstance()
        endOfMonthCalendar.add(java.util.Calendar.MONTH, 1)
        endOfMonthCalendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        endOfMonthCalendar.add(java.util.Calendar.MILLISECOND, -1)
        val endOfMonth = endOfMonthCalendar.timeInMillis

        viewModel.loadStatsForPeriod(startOfMonth, endOfMonth)
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
        println("Обновление графика с данными за месяц: $waterIntakeList")

        if (waterIntakeList.isEmpty()) {
            statsChart.setNoDataText("Нет данных за месяц")
            statsChart.invalidate()
            return
        }

        // Группируем записи по месяцу и году для получения информации о периоде
        val entriesByMonth = waterIntakeList.groupBy { record: WaterIntakeEntity ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = record.timestamp
            // Используем формат "yyyy-MM" для группировки по месяцу и году
            SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
        }.mapValues { (_, recordsInMonth: List<WaterIntakeEntity>) ->
            recordsInMonth.sumOf { it.amount } // Суммируем объем за месяц
        }

        // Поскольку мы в "месячном" фрагменте, ожидается одна группа (текущий месяц)
        val monthEntry = entriesByMonth.entries.firstOrNull()

        val entries = if (monthEntry != null) {
            listOf(BarEntry(0f, monthEntry.value.toFloat())) // Одна запись для текущего месяца
        } else {
            emptyList()
        }

        val dataSet = BarDataSet(entries, "Объем воды (мл)")
        dataSet.color = android.graphics.Color.BLUE

        val barData = BarData(dataSet)
        barData.barWidth = 0.8f

        statsChart.data = barData

        statsChart.description.isEnabled = false
        statsChart.legend.isEnabled = true

        // Настраиваем ось X для отображения месяца и года
        val xAxis = statsChart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f // Шаг в 1 (одна метка для месяца)
        xAxis.setDrawGridLines(false) // Отключаем сетку по оси X
        xAxis.textColor = android.graphics.Color.BLACK // Цвет текста оси X
        xAxis.axisLineColor = android.graphics.Color.BLACK // Цвет линии оси X

        // Создаем форматтер для отображения месяца и года
        val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val monthYearLabel = if (monthEntry != null) {
            monthEntry.key // Используем ключ группы (yyyy-MM)
        } else {
            "Нет данных"
        }

        xAxis.valueFormatter = object : IndexAxisValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                 // Для одной записи всегда возвращаем метку месяца/года
                 return monthYearLabel
            }
        }
        xAxis.setLabelCount(1, true) // Отображаем только одну метку

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
} 