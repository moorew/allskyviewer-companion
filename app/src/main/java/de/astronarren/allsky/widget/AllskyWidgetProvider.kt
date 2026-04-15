package de.astronarren.allsky.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.*
import de.astronarren.allsky.MainActivity
import de.astronarren.allsky.R
import de.astronarren.allsky.workers.WidgetUpdateWorker

class AllskyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            enqueueUpdate(context, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == REFRESH_ACTION) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                enqueueUpdate(context, appWidgetId)
            }
        }
    }

    private fun enqueueUpdate(context: Context, appWidgetId: Int) {
        // First, set the widget to a "refreshing" state immediately
        val views = RemoteViews(context.packageName, R.layout.widget_allsky)
        
        // Setup Intents (always needed when updating views)
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_image, openAppPendingIntent)

        val refreshIntent = Intent(context, AllskyWidgetProvider::class.java).apply {
            action = REFRESH_ACTION
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)

        views.setTextViewText(R.id.widget_last_update, context.getString(R.string.widget_refreshing))
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views)

        // Enqueue background work
        val workRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .setInputData(workDataOf(AppWidgetManager.EXTRA_APPWIDGET_ID to appWidgetId))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "widget_update_$appWidgetId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    companion object {
        const val REFRESH_ACTION = "de.astronarren.allsky.widget.REFRESH"
    }
}