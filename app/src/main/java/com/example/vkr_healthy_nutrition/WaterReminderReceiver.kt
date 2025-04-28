package com.example.vkr_healthy_nutrition

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WaterReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationActivity.showStaticWaterNotification(context)
    }
}