package com.example.vkr_healthy_nutrition.ui.stats

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.vkr_healthy_nutrition.R
import com.example.vkr_healthy_nutrition.ThemeManager
import com.example.vkr_healthy_nutrition.data.WaterViewModel
import com.example.vkr_healthy_nutrition.data.WaterIntakeViewModelFactory
import com.example.vkr_healthy_nutrition.data.repository.WaterRepository
import com.example.vkr_healthy_nutrition.data.WaterUiState
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.text.SimpleDateFormat
import java.util.*

abstract class BaseStatsFragment : Fragment(), OnChartValueSelectedListener {
    protected lateinit var chart: BarChart
    protected var recommendedDailyIntake = 2000f
    protected lateinit var viewModel: WaterViewModel
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stats_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chart = view.findViewById(R.id.chart)
        setupViewModel()
        setupChart()
        observeData()
    }

    private fun setupViewModel() {
        val repository = WaterRepository(requireContext())
        val factory = WaterIntakeViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory).get(WaterViewModel::class.java)
    }

    private fun setupChart() {
        chart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(false)
            legend.isEnabled = true
            setOnChartValueSelectedListener(this@BaseStatsFragment)
            
            animateY(1000)
            
            minOffset = 10f
            setExtraOffsets(10f, 10f, 10f, 10f)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = getXAxisFormatter()
                textColor = ThemeManager.getPrimaryColor(requireContext())
                textSize = 10f
                yOffset = 5f
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()} мл"
                    }
                }
                textColor = ThemeManager.getPrimaryColor(requireContext())
                textSize = 10f
                
                val recommendedLine = LimitLine(getRecommendedValue(), "Рекомендуемый объем").apply {
                    lineWidth = 1f
                    lineColor = ThemeManager.getSecondaryColor(requireContext())
                    textColor = ThemeManager.getSecondaryColor(requireContext())
                    textSize = 10f
                    labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                }
                addLimitLine(recommendedLine)
            }

            axisRight.isEnabled = false
            
            marker = WaterMarkerView(context, R.layout.marker_view)
        }
    }

    protected open fun observeData() {
        viewModel.todayWaterState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WaterUiState.Success -> {
                    val entries = state.records.mapIndexed { index, record ->
                        BarEntry(index.toFloat(), record.amount.toFloat())
                    }
                    updateChartData(entries)
                    recommendedDailyIntake = state.goal.toFloat()
                    updateRecommendedLine()
                }
                is WaterUiState.Error -> {
                    // Можно добавить обработку ошибок, например показать сообщение
                }
                else -> {
                    // Обработка других состояний если необходимо
                }
            }
        }
    }

    protected fun updateRecommendedLine() {
        chart.axisLeft.removeLimitLine(chart.axisLeft.limitLines.firstOrNull())
        val recommendedLine = LimitLine(recommendedDailyIntake, "Рекомендуемый объем").apply {
            lineWidth = 1f
            lineColor = ThemeManager.getSecondaryColor(requireContext())
            textColor = ThemeManager.getSecondaryColor(requireContext())
            textSize = 10f
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        }
        chart.axisLeft.addLimitLine(recommendedLine)
        chart.invalidate()
    }

    protected fun updateChartData(entries: List<BarEntry>) {
        val dataSet = BarDataSet(entries, "Потребление воды").apply {
            color = ThemeManager.getPrimaryColor(requireContext())
            valueTextColor = ThemeManager.getPrimaryColor(requireContext())
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()} мл"
                }
            }
            valueTextSize = 9f
            setDrawValues(true)
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.5f
        }

        chart.data = barData
        chart.invalidate()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadTodayWaterData()
    }

    protected open fun getRecommendedValue(): Float {
        return recommendedDailyIntake
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        // Реализация при необходимости
    }

    override fun onNothingSelected() {
        // Реализация при необходимости
    }

    abstract fun getXAxisFormatter(): ValueFormatter
} 