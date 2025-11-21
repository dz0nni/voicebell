package com.voicebell.clock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.voicebell.clock.service.TimerService

/**
 * BroadcastReceiver for handling timer notification actions.
 * Required for Android 12+ to handle notification actions without background service restrictions.
 */
class TimerActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "TimerActionReceiver"
        const val ACTION_FINISH_TIMER = "com.voicebell.clock.timer.ACTION_FINISH_TIMER"
        const val EXTRA_TIMER_ID = "timer_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received action: ${intent.action}")

        when (intent.action) {
            ACTION_FINISH_TIMER -> {
                val timerId = intent.getLongExtra(EXTRA_TIMER_ID, -1L)
                if (timerId != -1L) {
                    Log.d(TAG, "Finishing timer: $timerId")

                    // Forward to TimerService
                    val serviceIntent = Intent(context, TimerService::class.java).apply {
                        action = TimerService.ACTION_FINISH
                        putExtra(TimerService.EXTRA_TIMER_ID, timerId)
                    }

                    try {
                        context.startService(serviceIntent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to start TimerService", e)
                    }
                } else {
                    Log.w(TAG, "Invalid timer ID received")
                }
            }
        }
    }
}
