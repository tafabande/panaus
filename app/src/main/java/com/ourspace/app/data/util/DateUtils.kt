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
            formatDateTime(date.time)
        } catch (e: Exception) {
            ""
        }
    }

    fun formatDateTime(timestamp: Long): String {
        return try {
            val date = Date(timestamp)
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

    fun getDaysInMonth(year: Int, month: Int): List<Date> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        val days = mutableListOf<Date>()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..daysInMonth) {
            calendar.set(year, month, i)
            days.add(calendar.time)
        }
        return days
    }

    fun formatToIsoDate(year: Int, month: Int, day: Int): String {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
    }

    fun formatToDisplayTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        return SimpleDateFormat("hh:mm a", Locale.US).format(calendar.time)
    }

    fun formatRelativeTime(isoString: String): String {
        return try {
            val parser = SimpleDateFormat(ISO_FORMAT, Locale.US)
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(isoString) ?: return ""
            formatRelativeTime(date.time)
        } catch (e: Exception) {
            ""
        }
    }

    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60000 -> "just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            else -> "${diff / 86400000}d ago"
        }
    }
}
