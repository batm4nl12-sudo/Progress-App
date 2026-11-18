package com.progress.app
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = getSharedPreferences("ProgressPrefs", MODE_PRIVATE)
        val etEventName = findViewById<EditText>(R.id.etEventName)
        val etTargetDate = findViewById<EditText>(R.id.etTargetDate)
        val btnSave = findViewById<Button>(R.id.btnSave)
        etEventName.setText(prefs.getString("eventName", "Hedef"))
        etTargetDate.setText(prefs.getString("targetDate", "2026-12-31"))
        btnSave.setOnClickListener {
            prefs.edit()
                .putString("eventName", etEventName.text.toString())
                .putString("targetDate", etTargetDate.text.toString())
                .apply()
            val ids = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(ComponentName(this, ProgressWidget::class.java))
            if (ids.isNotEmpty()) {
                sendBroadcast(Intent(this, ProgressWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                })
            }
            Toast.makeText(this, "Kaydedildi ve Widget Güncellendi", Toast.LENGTH_SHORT).show()
        }
    }
}
