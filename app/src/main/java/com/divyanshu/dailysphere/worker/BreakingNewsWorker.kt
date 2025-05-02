package com.divyanshu.dailysphere.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.divyanshu.dailysphere.BuildConfig
import com.divyanshu.dailysphere.R
import com.divyanshu.dailysphere.WebViewActivity
import com.divyanshu.dailysphere.network.RetrofitInstance

class BreakingNewsWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "BreakingNewsWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Work started: Fetching breaking news")

        return try {
            val response = RetrofitInstance.api.getBreakingNews(
                apiKey = BuildConfig.NEWS_API_KEY,
                language = "en"
            )
            Log.d(TAG, "API response received")

            if (response.isSuccessful) {
                Log.d(TAG, "Response successful")

                val articles = response.body()?.results
                Log.d(TAG, "Articles fetched: ${articles?.size}")

                if (!articles.isNullOrEmpty()) {
                    val limitedArticles = articles.take(5)
                    for ((index, article) in limitedArticles.withIndex()) {
                        Log.d(TAG, "Showing notification for article: ${article.title}")
                        showNotification(
                            notificationId = 1001 + index,
                            article.title,
                            article.description ?: "",
                            article.link ?: ""
                        )
                    }
                }
                Result.success()
            } else {
                Log.e(TAG, "Response failed: ${response.code()} - ${response.message()}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API call", e)
            Result.retry()
        }
    }

    private fun showNotification(
        notificationId: Int,
        title: String,
        message: String,
        link: String
    ) {
        val channelId = "breaking_news_channel"
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.d(TAG, "Preparing notification")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "Breaking News", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, WebViewActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("URL", link)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Breaking News")
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Notification shown")
    }
}