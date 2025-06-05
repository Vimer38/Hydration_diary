package com.example.vkr_healthy_nutrition.ui.stats


import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.vkr_healthy_nutrition.ui.viewmodel.WaterIntakeViewModel
import com.example.vkr_healthy_nutrition.data.local.entities.WaterIntakeEntity
import androidx.lifecycle.lifecycleScope
import com.example.vkr_healthy_nutrition.R
import com.example.vkr_healthy_nutrition.ui.viewmodel.WaterIntakeState
import kotlinx.coroutines.launch
import androidx.fragment.app.Fragment

class WaterStatsFragment : Fragment() {

    private lateinit var waterIntakeViewModel: WaterIntakeViewModel

    private lateinit var statsTextView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация ViewModel
        waterIntakeViewModel = ViewModelProvider(this)[WaterIntakeViewModel::class.java]

        // Инициализация UI элементов (если они есть в макете)
        statsTextView = view.findViewById(R.id.stats_text_view)

        val startTime = System.currentTimeMillis()
        val endTime = System.currentTimeMillis()
        // Загружаем статистику за период
        waterIntakeViewModel.loadStatsForPeriod(startTime, endTime)

        // Наблюдаем за состоянием StateFlow в ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            waterIntakeViewModel.state.collect { state ->
                when (state) {
                    is WaterIntakeState.Loading -> {
                        println("Загрузка...")
                    }
                    is WaterIntakeState.Success -> {
                        // Обновляем UI с полученными данными
                        updateStatsUI(state.records)
                    }
                    is WaterIntakeState.Error -> {
                        println("Ошибка: ${state.message}")
                    }
                    is WaterIntakeState.Idle -> {
                    }
                }
            }
        }

    }


    private fun updateStatsUI(waterIntakeList: List<WaterIntakeEntity>) {
        val totalAmount = waterIntakeList.sumOf { it.amount }
        println("Статистика получена: $waterIntakeList")
    }

}