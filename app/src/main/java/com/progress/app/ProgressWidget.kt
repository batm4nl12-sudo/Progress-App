package com.progress.app
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews

class ProgressWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        for (id in ids) draw(context, mgr, id)
    }
    override fun onAppWidgetOptionsChanged(context: Context, mgr: AppWidgetManager, id: Int, newOptions: Bundle) {
        draw(context, mgr, id)
    }
    private fun draw(context: Context, mgr: AppWidgetManager, id: Int) {
        val p = context.getSharedPreferences("ProgressPrefs", Context.MODE_PRIVATE)
        val s = WidgetRenderer.Style(
            p.getString("name", "Yılbaşı 2027") ?: "Yılbaşı 2027",
            WidgetRenderer.parse(p.getString("start", null), "2026-01-01"),
            WidgetRenderer.parse(p.getString("end", null), "2027-01-01"),
            p.getInt("palette", 0), p.getInt("radius", 22), p.getInt("thick", 10),
            p.getBoolean("showPercent", true), p.getBoolean("showDays", true), p.getBoolean("light", false))
        val o = mgr.getAppWidgetOptions(id)
        val w = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
        val h = o.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 120)
        val views = RemoteViews(context.packageName, R.layout.progress_widget)
        views.setImageViewBitmap(R.id.widget_image, WidgetRenderer.render(context, s, w, h))
        val pi = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_image, pi)
        mgr.updateAppWidget(id, views)
    }
}
