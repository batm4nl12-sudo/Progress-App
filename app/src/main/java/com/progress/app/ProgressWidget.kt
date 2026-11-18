package com.progress.app
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

class ProgressWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences("ProgressPrefs", Context.MODE_PRIVATE)
        val eventName = prefs.getString("eventName", "Hedef") ?: "Hedef"
        val targetDateString = prefs.getString("targetDate", "2024-12-31") ?: "2024-12-31"
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance().time
        val targetDate = formatter.parse(targetDateString) ?: today
        val totalDays = ((targetDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
        val remainingDays = max(0, totalDays)
        val progress = if (totalDays > 0) min(100, max(0, 100 - (totalDays * 100 / 365))) else 100

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.progress_widget).apply {
                setTextViewText(R.id.widget_event_name, eventName)
                setTextViewText(R.id.widget_days_left, "$remainingDays Gün Kaldı")
                setProgressBar(R.id.widget_progress_bar, 100, progress, false)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
