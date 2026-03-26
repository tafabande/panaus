package com.ourspace.app.data.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private const val ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    fun getCurrentIsoTime(): String {
        val format = SimpleDateFormat(ISO_FORMAT, Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(Date())
    }

    fun formatTime(isoString: String): String {
        return try {
            val parser = SimpleDateFormat(ISO_FORMAT, Locale.US)
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(isoString) ?: return ""
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    fun formatDateTime(isoString: String): String {
        return try {
            val parser = SimpleDateFormat(ISO_FORMAT, Locale.US)
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(isoString) ?: return ""
            val formatter = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    fun formatMonthDay(dateString: String): Pair<String, String> {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = parser.parse(dateString) ?: return "M" to "D"
            val monthFormatter = SimpleDateFormat("MMM", Locale.US)
            val dayFormatter = SimpleDateFormat("dd", Locale.US)
            monthFormatter.format(date).uppercase() to dayFormatter.format(date)
        } catch (e: Exception) {
            "M" to "D"
        }
    }

    fun isToday(dateString: String?): Boolean {
        if (dateString.isNullOrBlank()) return false
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = parser.parse(dateString) ?: return false
            
            val today = Calendar.getInstance()
            val target = Calendar.getInstance().apply { time = date }
            
            today.get(Calendar.MONTH) == target.get(Calendar.MONTH) &&
            today.get(Calendar.DAY_OF_MONTH) == target.get(Calendar.DAY_OF_MONTH)
        } catch (e: Exception) {
            false
        }
    }

    fun getCurrentDate(): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return format.format(Date())
    }
}
