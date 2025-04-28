package com.example.vkr_healthy_nutrition.ui.stats

import com.github.mikephil.charting.formatter.ValueFormatter
import com.example.vkr_healthy_nutrition.data.WaterUiState

class WaterStatsFragment : BaseStatsFragment() {
    private var recordTimes = listOf<String>()

    override fun getXAxisFormatter(): ValueFormatter {
        return TimeAxisFormatter(recordTimes)
    }

    override fun observeData() {
        super.observeData()
        viewModel.todayWaterState.observe(viewLifecycleOwner) { state ->
            if (state is WaterUiState.Success) {
                recordTimes = state.records.map { it.record_time }
                chart.xAxis.valueFormatter = getXAxisFormatter()
                chart.invalidate()
            }
        }
    }
} 