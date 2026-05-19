package com.neonloop.sangpumtok.util

import android.content.Context
import org.json.JSONArray

object RiskKeywordStorageUtil {

    private const val PREF_NAME = "sangpumtok_risk_keyword_storage"
    private const val KEY_CUSTOM_KEYWORDS = "custom_risk_keywords"

    private val defaultKeywords = listOf(
        "욱일기",
        "나치",
        "하켄크로이츠",
        "선정적",
        "성인용품",
        "가품",
        "짝퉁",
        "도박",
        "마약",
        "대마",
        "총기",
        "흉기",
        "폭발물",
        "혐오",
        "차별",
        "불법",
        "위험물",
        "민감",
        "금지",
        "보류"
    )

    fun getDefaultKeywords(): List<String> {
        return defaultKeywords
    }

    fun loadCustomKeywords(context: Context): List<String> {
        val jsonText = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CUSTOM_KEYWORDS, "[]") ?: "[]"

        return try {
            val jsonArray = JSONArray(jsonText)
            val result = mutableListOf<String>()

            for (i in 0 until jsonArray.length()) {
                val keyword = jsonArray.optString(i).trim()

                if (keyword.isNotBlank()) {
                    result.add(keyword)
                }
            }

            result.distinct()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadAllKeywords(context: Context): List<String> {
        return (defaultKeywords + loadCustomKeywords(context))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    fun addKeyword(context: Context, keyword: String): Boolean {
        val cleanKeyword = keyword.trim()

        if (cleanKeyword.isBlank()) {
            return false
        }

        val allKeywords = loadAllKeywords(context)

        if (allKeywords.any { it.equals(cleanKeyword, ignoreCase = true) }) {
            return false
        }

        val customKeywords = loadCustomKeywords(context).toMutableList()
        customKeywords.add(0, cleanKeyword)

        saveCustomKeywords(context, customKeywords)

        return true
    }

    fun deleteCustomKeyword(context: Context, keyword: String) {
        val customKeywords = loadCustomKeywords(context)
            .filterNot { it.equals(keyword, ignoreCase = true) }

        saveCustomKeywords(context, customKeywords)
    }

    fun clearCustomKeywords(context: Context) {
        saveCustomKeywords(context, emptyList())
    }

    fun makeCopyText(context: Context): String {
        val defaultText = defaultKeywords.joinToString(", ")
        val customText = loadCustomKeywords(context).joinToString(", ")

        return buildString {
            appendLine("[기본 위험 키워드]")
            appendLine(defaultText)

            appendLine()
            appendLine("[내가 추가한 위험 키워드]")

            if (customText.isBlank()) {
                appendLine("추가한 키워드 없음")
            } else {
                appendLine(customText)
            }
        }
    }

    private fun saveCustomKeywords(context: Context, keywords: List<String>) {
        val jsonArray = JSONArray()

        keywords
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .forEach { keyword ->
                jsonArray.put(keyword)
            }

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CUSTOM_KEYWORDS, jsonArray.toString())
            .apply()
    }
}