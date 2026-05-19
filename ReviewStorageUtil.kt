package com.neonloop.sangpumtok.util

import android.content.Context
import com.neonloop.sangpumtok.model.CatalogReview
import org.json.JSONArray
import org.json.JSONObject

object ReviewStorageUtil {

    private const val PREF_NAME = "sangpumtok_review_storage"
    private const val KEY_REVIEWS = "catalog_reviews"

    fun saveReview(context: Context, review: CatalogReview) {
        val reviews = loadReviews(context).toMutableList()

        reviews.add(0, review)

        val limitedReviews = reviews.take(100)

        val jsonArray = JSONArray()

        limitedReviews.forEach {
            val jsonObject = JSONObject().apply {
                put("id", it.id)
                put("productName", it.productName)
                put("category", it.category)
                put("optionName", it.optionName)
                put("description", it.description)
                put("memo", it.memo)
                put("result", it.result)
                put("reason", it.reason)
                put("createdAt", it.createdAt)
            }

            jsonArray.put(jsonObject)
        }

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_REVIEWS, jsonArray.toString())
            .apply()
    }

    fun loadReviews(context: Context): List<CatalogReview> {
        val jsonText = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_REVIEWS, "[]") ?: "[]"

        return try {
            val jsonArray = JSONArray(jsonText)
            val reviews = mutableListOf<CatalogReview>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                val review = CatalogReview(
                    id = obj.optString("id"),
                    productName = obj.optString("productName"),
                    category = obj.optString("category"),
                    optionName = obj.optString("optionName"),
                    description = obj.optString("description"),
                    memo = obj.optString("memo"),
                    result = obj.optString("result"),
                    reason = obj.optString("reason"),
                    createdAt = obj.optString("createdAt")
                )

                reviews.add(review)
            }

            reviews
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deleteReview(context: Context, reviewId: String) {
        val reviews = loadReviews(context)
            .filterNot { it.id == reviewId }

        val jsonArray = JSONArray()

        reviews.forEach {
            val jsonObject = JSONObject().apply {
                put("id", it.id)
                put("productName", it.productName)
                put("category", it.category)
                put("optionName", it.optionName)
                put("description", it.description)
                put("memo", it.memo)
                put("result", it.result)
                put("reason", it.reason)
                put("createdAt", it.createdAt)
            }

            jsonArray.put(jsonObject)
        }

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_REVIEWS, jsonArray.toString())
            .apply()
    }

    fun clearReviews(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_REVIEWS)
            .apply()
    }
}