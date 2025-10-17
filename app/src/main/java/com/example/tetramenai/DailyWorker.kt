// DailyWorker.kt (하나의 파일에 포함)
package com.uselessdev.tetramenai

import android.content.Context
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import java.util.Calendar

class DailyWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("DailyWorker", "새벽 4시 작업 시작")

            // 여기서 MainService 또는 직접 작업 수행
            // 예: OpenAiClient 호출, 파일 처리, DB 갱신 등

            performDailyTask(applicationContext)

            Log.d("DailyWorker", "작업 완료")
            Result.success()
        } catch (e: Exception) {
            Log.e("DailyWorker", "작업 실패", e)
            Result.retry()
        }
    }

    private fun performDailyTask(context: Context) {
        DeepLearnManager().deeplearncycle(context)
    }

    companion object {
        fun schedule(context: Context) {
            val initialDelay = calculateInitialDelay(4) // 4시까지 남은 시간

            val workRequest = PeriodicWorkRequestBuilder<DailyWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daily_task",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }

        private fun calculateInitialDelay(hourOfDay: Int): Long {
            val calendar = Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis() }
            val target = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (target.before(calendar)) target.add(Calendar.DATE, 1)
            return target.timeInMillis - calendar.timeInMillis
        }
    }
}