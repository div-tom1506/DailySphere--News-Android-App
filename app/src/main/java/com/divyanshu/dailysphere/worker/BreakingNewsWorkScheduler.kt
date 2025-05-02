package com.divyanshu.dailysphere.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object BreakingNewsWorkScheduler {

    fun scheduleBreakingNewsWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<BreakingNewsWorker>(
            1, TimeUnit.HOURS // Run every hour
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "BreakingNewsWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    // For testing: Trigger once immediately
    fun triggerOneTimeBreakingNewsWorker(context: Context) {
        val request = OneTimeWorkRequestBuilder<BreakingNewsWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
