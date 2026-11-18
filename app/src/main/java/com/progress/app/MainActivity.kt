package com.progress.app
import android.app.DatePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var etName: EditText
    private lateinit var preview: ImageView
    private lateinit var chipsRow: LinearLayout
    private lateinit var sbRadius: SeekBar
    private lateinit var sbThick: SeekBar
    private lateinit var swPercent: Switch
    private lateinit var swDays: Switch
    private lateinit var spTheme: Spinner
    private lateinit var btnStart: Button
    private lateinit var btnEnd: Button
    private lateinit var radiusVal: TextView
    private lateinit var thickVal: TextView
    private var palette = 0; private var radius = 22; private var thick = 10; private var light = false
    private var start: LocalDate = LocalDate.of(2026, 1, 1)
    private var end: LocalDate = LocalDate.of(2027, 1, 1)
    private val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("tr"))
    private val chipViews = ArrayList<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = getSharedPreferences("ProgressPrefs", MODE_PRIVATE)
        etName = findViewById(R.id.etName); preview = findViewById(R.id.preview)
        chipsRow = findViewById(R.id.chips); sbRadius = findViewById(R.id.sbRadius); sbThick = findViewById(R.id.sbThick)
        swPercent = findViewById(R.id.swPercent); swDays = findViewById(R.id.swDays); spTheme = findViewById(R.id.spTheme)
        btnStart = findViewById(R.id.btnStart); btnEnd = findViewById(R.id.btnEnd)
        radiusVal = findViewById(R.id.radiusVal); thickVal = findViewById(R.id.thickVal)
        etName.setText(prefs.getString("name", "Yılbaşı 2027"))
        start = WidgetRenderer.parse(prefs.getString("start", null), "2026-01-01")
        end = WidgetRenderer.parse(prefs.getString("end", null), "2027-01-01")
        palette = prefs.getInt("palette", 0); radius = prefs.getInt("radius", 22); thick = prefs.getInt("thick", 10)
        light = prefs.getBoolean("light", false)
        swPercent.isChecked = prefs.getBoolean("showPercent", true)
        swDays.isChecked = prefs.getBoolean("showDays", true)
        sbRadius.progress = radius - 8; sbThick.progress = thick - 6
        spTheme.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayOf("Koyu", "Açık")).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spTheme.setSelection(if (light) 1 else 0)
        for (i in WidgetRenderer.PALETTES.indices) {
            val v = View(this)
            val lp = LinearLayout.LayoutParams(dp(42), dp(42)); lp.marginEnd = dp(10)
            v.layoutParams = lp
            v.setOnClickListener { palette = i; refresh() }
            chipsRow.addView(v); chipViews.add(v)
        }
        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) { refresh() }
        })
        sbRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, u: Boolean) { radius = 8 + p; refresh() }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        sbThick.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, u: Boolean) { thick = 6 + p; refresh() }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        swPercent.setOnCheckedChangeListener { _, _ -> refresh() }
        swDays.setOnCheckedChangeListener { _, _ -> refresh() }
        spTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) { light = pos == 1; refresh() }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        btnStart.setOnClickListener { pick(true) }
        btnEnd.setOnClickListener { pick(false) }
        findViewById<Button>(R.id.btnReset).setOnClickListener { reset() }
        findViewById<Button>(R.id.btnSave).setOnClickListener { save() }
        refresh()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun pick(isStart: Boolean) {
        val cur = if (isStart) start else end
        DatePickerDialog(this, { _, y, m, d ->
            if (isStart) start = LocalDate.of(y, m + 1, d) else end = LocalDate.of(y, m + 1, d)
            refresh()
        }, cur.year, cur.monthValue - 1, cur.dayOfMonth).show()
    }

    private fun style() = WidgetRenderer.Style(etName.text.toString(), start, end, palette, radius, thick,
        swPercent.isChecked, swDays.isChecked, light)

    private fun refresh() {
        for (i in chipViews.indices) {
            val g = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, WidgetRenderer.PALETTES[i])
            g.shape = GradientDrawable.OVAL
            if (i == palette) g.setStroke(dp(3), 0xFFEAF2FA.toInt()) else g.setStroke(0, 0)
            chipViews[i].background = g
        }
        radiusVal.text = "$radius dp"; thickVal.text = "$thick dp"
        btnStart.text = start.format(fmt); btnEnd.text = end.format(fmt)
        val wDp = (resources.displayMetrics.widthPixels / resources.displayMetrics.density).toInt() - 40
        preview.setImageBitmap(WidgetRenderer.render(this, style(), wDp, 170))
    }

    private fun reset() {
        palette = 0; radius = 22; thick = 10; light = false
        start = LocalDate.of(2026, 1, 1); end = LocalDate.of(2027, 1, 1)
        etName.setText("Yılbaşı 2027"); sbRadius.progress = radius - 8; sbThick.progress = thick - 6
        swPercent.isChecked = true; swDays.isChecked = true; spTheme.setSelection(0)
        refresh()
    }

    private fun save() {
        if (!end.isAfter(start)) {
            Toast.makeText(this, "Bitiş tarihi başlangıçtan sonra olmalı", Toast.LENGTH_LONG).show(); return
        }
        prefs.edit().putString("name", etName.text.toString())
            .putString("start", start.toString()).putString("end", end.toString())
            .putInt("palette", palette).putInt("radius", radius).putInt("thick", thick)
            .putBoolean("showPercent", swPercent.isChecked).putBoolean("showDays", swDays.isChecked)
            .putBoolean("light", light).apply()
        val ids = AppWidgetManager.getInstance(this).getAppWidgetIds(ComponentName(this, ProgressWidget::class.java))
        if (ids.isNotEmpty()) sendBroadcast(Intent(this, ProgressWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        })
        Toast.makeText(this, "Kaydedildi — widget güncellendi", Toast.LENGTH_SHORT).show()
    }
}
