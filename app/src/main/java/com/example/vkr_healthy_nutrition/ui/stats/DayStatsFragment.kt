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
import java.util.concurrent.TimeUnit

class DayStatsFragment : Fragment() {
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
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis
        viewModel.loadStatsForPeriod(startOfDay, endOfDay)
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
                        statsChart.invalidate()
                    }
                    is WaterIntakeState.Loading -> {
                        println("Загрузка...")
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
        println("Обновление графика с данными: $waterIntakeList")

        if (waterIntakeList.isEmpty()) {
            statsChart.setNoDataText("Нет данных за сегодня")
            statsChart.invalidate()
            return
        }

        val entries = waterIntakeList.groupBy { record ->
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = record.timestamp
            calendar.get(java.util.Calendar.HOUR_OF_DAY) // Группируем по часу
        }.map { (hour, recordsInHour) ->
            val totalAmountInHour = recordsInHour.sumOf { it.amount }
            BarEntry(hour.toFloat(), totalAmountInHour.toFloat())
        }.sortedBy { it.x }

        val dataSet = BarDataSet(entries, "Объем воды (мл)")
        dataSet.color = android.graphics.Color.BLUE

        val barData = BarData(dataSet)
        barData.barWidth = 0.8f // Увеличиваем ширину столбцов для отображения по часам

        statsChart.data = barData
        statsChart.description.isEnabled = false
        statsChart.legend.isEnabled = true

        // Настраиваем ось X для отображения времени (часов)
        val xAxis = statsChart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f // Шаг в 1 час
        xAxis.setDrawGridLines(false) // Отключаем сетку по оси X
        xAxis.textColor = android.graphics.Color.BLACK // Цвет текста оси X
        xAxis.axisLineColor = android.graphics.Color.BLACK // Цвет линии оси X
        xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString() + " ч" // Отображаем только час
            }
        }
        // Убираем принудительное количество меток
        // xAxis.setLabelCount(entries.size, true)

        // Настраиваем ось Y
        val yAxis = statsChart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.granularity = 100f // Шаг в 100 мл
        yAxis.textColor = android.graphics.Color.BLACK // Цвет текста оси Y
        yAxis.axisLineColor = android.graphics.Color.BLACK // Цвет линии оси Y
        yAxis.setDrawGridLines(false) // Отключаем сетку по оси Y

        statsChart.axisRight.isEnabled = false // Отключаем правую ось Y
        statsChart.setDrawGridBackground(false) // Отключаем рисование фона сетки
        statsChart.setDrawBarShadow(false)
        statsChart.setDrawValueAboveBar(true)

        statsChart.invalidate()
    }
} 