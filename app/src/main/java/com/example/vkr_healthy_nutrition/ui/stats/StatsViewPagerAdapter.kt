package com.example.vkr_healthy_nutrition.ui.stats

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class StatsViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DayStatsFragment()
            1 -> WeekStatsFragment()
            2 -> MonthStatsFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
} 