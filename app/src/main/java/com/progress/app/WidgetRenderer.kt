package com.progress.app
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object WidgetRenderer {
    data class Style(val name: String, val start: LocalDate, val end: LocalDate, val palette: Int,
        val radiusDp: Int, val thickDp: Int, val showPercent: Boolean, val showDays: Boolean, val light: Boolean)

    val PALETTES = arrayOf(
        intArrayOf(0xFFFFB020.toInt(), 0xFFFF3D81.toInt()),
        intArrayOf(0xFF22D3EE.toInt(), 0xFF3B82F6.toInt()),
        intArrayOf(0xFFA3E635.toInt(), 0xFF10B981.toInt()),
        intArrayOf(0xFFFF5C5C.toInt(), 0xFFFFB020.toInt()),
        intArrayOf(0xFFFF3D81.toInt(), 0xFF8B5CF6.toInt()),
        intArrayOf(0xFF7DD3FC.toInt(), 0xFF2DD4BF.toInt()))

    fun parse(s: String?, def: String): LocalDate =
        try { LocalDate.parse(s) } catch (e: Exception) { LocalDate.parse(def) }

    fun daysLeft(s: Style): Long = ChronoUnit.DAYS.between(LocalDate.now(), s.end)

    fun percent(s: Style): Int {
        val total = ChronoUnit.DAYS.between(s.start, s.end)
        if (total <= 0) return 100
        val done = ChronoUnit.DAYS.between(s.start, LocalDate.now())
        return ((done * 100) / total).toInt().coerceIn(0, 100)
    }

    fun render(ctx: Context, s: Style, wDp: Int, hDp: Int): Bitmap {
        val d = ctx.resources.displayMetrics.density
        val w = (wDp * d).toInt().coerceIn(200, 1400)
        val h = (hDp * d).toInt().coerceIn(120, 900)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val ink = if (s.light) 0xFF171A21.toInt() else 0xFFEAF2FA.toInt()
        val muted = if (s.light) 0xFF5C6470.toInt() else 0xFF93A9C4.toInt()
        val surface = if (s.light) 0xFFF7F3EC.toInt() else 0xFF151A23.toInt()
        val border = if (s.light) 0xFFE2D9CC.toInt() else 0xFF2A3444.toInt()
        val track = if (s.light) 0xFFE7DFD2.toInt() else 0xFF232C3B.toInt()
        val acc = PALETTES[s.palette.coerceIn(0, PALETTES.size - 1)]
        val r = s.radiusDp * d
        val pad = 18 * d
        val bg = Paint(Paint.ANTI_ALIAS_FLAG)
        bg.color = surface; bg.style = Paint.Style.FILL
        c.drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), r, r, bg)
        bg.style = Paint.Style.STROKE; bg.strokeWidth = 1.5f * d; bg.color = border
        c.drawRoundRect(RectF(0.75f * d, 0.75f * d, w - 0.75f * d, h - 0.75f * d), r, r, bg)
        val tp = TextPaint(Paint.ANTI_ALIAS_FLAG)
        val pct = percent(s); val left = daysLeft(s)
        tp.typeface = Typeface.DEFAULT_BOLD; tp.textSize = 19 * d; tp.color = ink
        val nameMax = w - pad * 2 - (if (s.showPercent) 74 * d else 0f)
        val name = TextUtils.ellipsize(s.name.ifEmpty { "Hedef" }, tp, nameMax, TextUtils.TruncateAt.END).toString()
        c.drawText(name, pad, pad + 19 * d, tp)
        if (s.showPercent) {
            val chipW = 64 * d; val chipH = 28 * d
            val cx = w - pad - chipW; val cy = pad + 2 * d
            val cp = Paint(Paint.ANTI_ALIAS_FLAG)
            cp.color = (acc[0] and 0x00FFFFFF) or 0x33000000
            c.drawRoundRect(RectF(cx, cy, cx + chipW, cy + chipH), chipH / 2, chipH / 2, cp)
            tp.textSize = 14 * d; tp.color = acc[0]; tp.textAlign = Paint.Align.CENTER
            c.drawText("%$pct", cx + chipW / 2, cy + chipH / 2 + 5 * d, tp)
            tp.textAlign = Paint.Align.LEFT
        }
        val th = s.thickDp * d
        val by = h * 0.46f - th / 2
        val tr = Paint(Paint.ANTI_ALIAS_FLAG); tr.color = track
        c.drawRoundRect(RectF(pad, by, w - pad, by + th), th / 2, th / 2, tr)
        if (pct > 0) {
            val fw = maxOf(pad + (w - 2 * pad) * pct / 100f, pad + th)
            val gp = Paint(Paint.ANTI_ALIAS_FLAG)
            gp.shader = LinearGradient(pad, 0f, w - pad, 0f, acc[0], acc[1], Shader.TileMode.CLAMP)
            c.drawRoundRect(RectF(pad, by, fw, by + th), th / 2, th / 2, gp)
        }
        val baseY = h - pad - 4 * d
        if (s.showDays) {
            tp.typeface = Typeface.DEFAULT_BOLD; tp.textSize = 30 * d; tp.color = ink
            val label = if (left > 0) "$left" else "0"
            c.drawText(label, pad, baseY, tp)
            val nw = tp.measureText(label)
            tp.textSize = 14 * d; tp.color = muted; tp.typeface = Typeface.DEFAULT
            c.drawText(if (left > 0) " gün kaldı" else " tamamlandı", pad + nw, baseY, tp)
        }
        val fmt = DateTimeFormatter.ofPattern("dd MMM", Locale("tr"))
        tp.textSize = 12 * d; tp.color = muted; tp.typeface = Typeface.DEFAULT
        tp.textAlign = Paint.Align.RIGHT
        c.drawText("${s.start.format(fmt)} → ${s.end.format(fmt)}", w - pad, baseY, tp)
        tp.textAlign = Paint.Align.LEFT
        return bmp
    }
}
