package com.neonloop.sangpumtok.util

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ReviewMemo(
    val id: String,
    val type: String,
    val content: String,
    val createdAt: String,
    val dateKey: String
)

object ReviewMemoStorageUtil {

    private const val PREF_NAME = "sangpumtok_review_memo_storage"
    private const val KEY_MEMOS = "review_memos"

    fun saveMemo(
        context: Context,
        type: String,
        content: String
    ) {
        val memos = loadAllMemos(context).toMutableList()

        val memo = ReviewMemo(
            id = UUID.randomUUID().toString(),
            type = type,
            content = content,
            createdAt = getCurrentTimeText(),
            dateKey = getTodayText()
        )

        memos.add(0, memo)

        saveAllMemos(context, memos)
    }

    fun loadAllMemos(context: Context): List<ReviewMemo> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val rawJson = prefs.getString(KEY_MEMOS, "[]") ?: "[]"

        return try {
            val jsonArray = JSONArray(rawJson)
            val result = mutableListOf<ReviewMemo>()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.optJSONObject(i) ?: continue

                result.add(
                    ReviewMemo(
                        id = item.optString("id"),
                        type = item.optString("type"),
                        content = item.optString("content"),
                        createdAt = item.optString("createdAt"),
                        dateKey = item.optString("dateKey")
                    )
                )
            }

            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deleteMemo(
        context: Context,
        memoId: String
    ) {
        val memos = loadAllMemos(context)
            .filter { memo ->
                memo.id != memoId
            }

        saveAllMemos(context, memos)
    }

    fun clearAllMemos(context: Context) {
        saveAllMemos(context, emptyList())
    }

    private fun saveAllMemos(
        context: Context,
        memos: List<ReviewMemo>
    ) {
        val jsonArray = JSONArray()

        memos.forEach { memo ->
            val item = JSONObject().apply {
                put("id", memo.id)
                put("type", memo.type)
                put("content", memo.content)
                put("createdAt", memo.createdAt)
                put("dateKey", memo.dateKey)
            }

            jsonArray.put(item)
        }

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        prefs.edit()
            .putString(KEY_MEMOS, jsonArray.toString())
            .apply()
    }

    private fun getCurrentTimeText(): String {
        val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
        return formatter.format(Date())
    }

    private fun getTodayText(): String {
        val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
        return formatter.format(Date())
    }
}