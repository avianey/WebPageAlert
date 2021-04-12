package fr.avianey.webpagealert

import android.content.Context
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.core.content.edit
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.work.*
import com.android.volley.Request
import com.google.common.util.concurrent.ListenableFuture
import fr.avianey.webpagealert.WebPageAlertActivity.Companion.CRAWL_FREQUENCY_DEFAULT
import fr.avianey.webpagealert.WebPageAlertActivity.Companion.CRAWL_FREQUENCY_MAX
import fr.avianey.webpagealert.WebPageAlertActivity.Companion.CRAWL_FREQUENCY_MIN
import fr.avianey.webpagealert.WebPageAlertActivity.Companion.KEY_FREQUENCY
import fr.avianey.webpagealert.WebPageAlertRequest.Companion.LAST_MODIFIED_ERROR
import java.util.*
import java.util.concurrent.TimeUnit


class WebPageAlertWorker(appContext: Context, workerParams: WorkerParameters): ListenableWorker(appContext, workerParams) {

    override fun startWork(): ListenableFuture<Result> {
        val url = getDefaultSharedPreferences(applicationContext)
            .getString(WebPageAlertActivity.KEY_URL, "")!!
        val now = Date().time
        return CallbackToFutureAdapter.getFuture { completer ->
            getDefaultSharedPreferences(applicationContext).edit {
                // will trigger status update asap in activity
                putLong(WebPageAlertActivity.KEY_LAST_CRAWLED, now)
            }
            val stringRequest = WebPageAlertRequest(Request.Method.GET, url,
                { completer.set(Result.success()) },
                {
                    getDefaultSharedPreferences(applicationContext).edit {
                        putLong(WebPageAlertActivity.KEY_LAST_MODIFIED, LAST_MODIFIED_ERROR)
                    }
                    completer.setException(it)
                    completer.set(Result.failure())
                }) { lastModified ->
                getDefaultSharedPreferences(applicationContext).edit {
                    putLong(WebPageAlertActivity.KEY_LAST_MODIFIED, lastModified)
                }
            }
            WebPageAlertInitializer.requestQueue.add(stringRequest)
        }
    }
    
    companion object {

        fun schedule(context: Context) {
            val frequency =
                getDefaultSharedPreferences(context)
                    .getInt(KEY_FREQUENCY, CRAWL_FREQUENCY_DEFAULT)
                    .coerceAtLeast(CRAWL_FREQUENCY_MIN)
                    .coerceAtMost(CRAWL_FREQUENCY_MAX)
                    .toLong()
            if (frequency > 0) {
                val constraints = Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val workRequestBuilder = PeriodicWorkRequest
                    .Builder(WebPageAlertWorker::class.java, frequency, TimeUnit.MINUTES)
                    .addTag(WebPageAlertWorker::class.java.name)
                    .setConstraints(constraints)
                    .setInitialDelay(0, TimeUnit.MINUTES) // schedule in 0 minute
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                val workRequest = workRequestBuilder.build()
                WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(
                        WebPageAlertWorker::class.java.name,
                        ExistingPeriodicWorkPolicy.REPLACE,
                        workRequest
                    )
            } else {
                cancel(context)
            }
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(WebPageAlertWorker::class.java.name)
        }
        
    }
    
}