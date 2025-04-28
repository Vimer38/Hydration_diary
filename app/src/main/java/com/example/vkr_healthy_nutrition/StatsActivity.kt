package com.example.vkr_healthy_nutrition

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.example.vkr_healthy_nutrition.ui.stats.StatsViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class StatsActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val appBar: Toolbar = findViewById(R.id.toolbar_set)
        setSupportActionBar(appBar)

        supportActionBar?.title = "Статистика"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewPager = findViewById(R.id.stats_view_pager)
        tabLayout = findViewById(R.id.period_tabs)

        // Настройка ViewPager2
        viewPager.adapter = StatsViewPagerAdapter(this)

        // Связывание TabLayout с ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "День"
                1 -> "Неделя"
                2 -> "Месяц"
                else -> null
            }
        }.attach()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Обработка нажатия на кнопку "Назад" в AppBar
        if (item.itemId == android.R.id.home) {
            // Возврат на главную активность
            finish() // Закрываем текущую активность
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}