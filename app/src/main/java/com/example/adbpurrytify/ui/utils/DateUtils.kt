package com.example.adbpurrytify.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val MONTH_YEAR_FORMAT = "MM-yyyy"
    private val formatter = SimpleDateFormat(MONTH_YEAR_FORMAT, Locale.getDefault())

    fun getCurrentMonthKey(): String {
        val calendar = Calendar.getInstance()
        return formatter.format(calendar.time)
    }

    fun formatMonthKey(year: Int, month: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(year, month - 1, 1)
        }
        return formatter.format(calendar.time)
    }

    fun parseMonthKey(monthKey: String): Pair<Int, Int>? {
        return try {
            val parts = monthKey.split("-")
            if (parts.size == 2) {
                Pair(parts[0].toInt(), parts[1].toInt()) // month, year
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun formatMonthForDisplay(monthYear: String): String {
        return try {
            val parts = monthYear.split("-")
            if (parts.size == 2) {
                val month = parts[0].toInt()
                val year = parts[1]
                val monthNames = listOf(
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
                )
                "${monthNames[month - 1]} $year"
            } else {
                monthYear
            }
        } catch (e: Exception) {
            monthYear
        }
    }

    fun formatMonthForDisplayShort(monthYear: String): String {
        return try {
            val parts = monthYear.split("-")
            if (parts.size == 2) {
                val month = parts[0].toInt()
                val year = parts[1]
                val monthNames = listOf(
                    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
                )
                "${monthNames[month - 1]} $year"
            } else {
                monthYear
            }
        } catch (e: Exception) {
            monthYear
        }
    }
}