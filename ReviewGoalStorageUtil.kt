package com.neonloop.sangpumtok.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReviewGoalStorageUtil {

    private const val PREF_NAME = "sangpumtok_review_goal_storage"
    private const val KEY_GOAL_COUNT = "today_goal_count"
    private const val KEY_GOAL_DATE = "today_goal_date"

    fun saveTodayGoal(
        context: Context,
        goalCount: Int
    ) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        prefs.edit()
            .putInt(KEY_GOAL_COUNT, goalCount)
            .putString(KEY_GOAL_DATE, getTodayText())
            .apply()
    }

    fun getTodayGoal(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val savedDate = prefs.getString(KEY_GOAL_DATE, "") ?: ""
        val todayText = getTodayText()

        if (savedDate != todayText) {
            return 0
        }

        return prefs.getInt(KEY_GOAL_COUNT, 0)
    }

    fun clearTodayGoal(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        prefs.edit()
            .remove(KEY_GOAL_COUNT)
            .remove(KEY_GOAL_DATE)
            .apply()
    }

    private fun getTodayText(): String {
        val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
        return formatter.format(Date())
    }
}