package com.neonloop.sangpumtok

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neonloop.sangpumtok.model.CatalogReview
import com.neonloop.sangpumtok.util.ReviewStorageUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewHistoryActivity : Activity() {

    private lateinit var tvHistoryTitle: TextView
    private lateinit var tvHistorySummary: TextView

    private lateinit var btnFilterAll: Button
    private lateinit var btnFilterToday: Button
    private lateinit var btnFilterPending: Button
    private lateinit var btnFilterRisk: Button
    private lateinit var btnFilterPhoto: Button
    private lateinit var btnFilterDirect: Button
    private lateinit var btnHistoryBack: Button

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyHistory: TextView

    private val allReviews = mutableListOf<CatalogReview>()
    private val filteredReviews = mutableListOf<CatalogReview>()

    private lateinit var adapter: ReviewHistoryAdapter

    private var currentFilter: String = FILTER_ALL

    companion object {
        const val EXTRA_START_FILTER = "extra_start_filter"

        const val FILTER_ALL = "all"
        const val FILTER_TODAY = "today"
        const val FILTER_PENDING = "pending"
        const val FILTER_RISK = "risk"
        const val FILTER_PHOTO = "photo"
        const val FILTER_DIRECT = "direct"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentFilter = intent.getStringExtra(EXTRA_START_FILTER) ?: FILTER_ALL

        buildLayout()
        setupRecyclerView()
        setupClickListeners()
        loadReviews()
    }

    override fun onResume() {
        super.onResume()
        loadReviews()
    }

    private fun buildLayout() {
        val rootScrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundResource(R.drawable.bg_main_gradient)
            setFillViewport(true)
        }

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }

        tvHistoryTitle = TextView(this).apply {
            text = "저장된 기록"
            textSize = 29f
            setTextColor(0xFF1F1B2E.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val subTitle = TextView(this).apply {
            text = "검수 결과를 필터별로 모아보고, 답변만 빠르게 복사할 수 있어요."
            textSize = 14f
            setTextColor(0xFF6F6780.toInt())
            setPadding(0, dp(4), 0, dp(16))
            setLineSpacing(3f, 1.0f)
        }

        val summaryCard = makeCardLayout()

        val summaryTitle = TextView(this).apply {
            text = "기록 요약"
            textSize = 18f
            setTextColor(0xFF1F1B2E.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        tvHistorySummary = TextView(this).apply {
            text = "검수 기록을 불러오는 중입니다."
            textSize = 14f
            setTextColor(0xFF5E566F.toInt())
            setPadding(0, dp(8), 0, 0)
            setLineSpacing(3f, 1.0f)
        }

        summaryCard.addView(summaryTitle)
        summaryCard.addView(tvHistorySummary)

        val filterCard = makeCardLayout()

        val filterTitle = TextView(this).apply {
            text = "필터"
            textSize = 18f
            setTextColor(0xFF1F1B2E.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val filterLayout1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
        }

        val filterLayout2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, 0)
        }

        btnFilterAll = makeFilterButton("전체")
        btnFilterToday = makeFilterButton("오늘")
        btnFilterPending = makeFilterButton("보류")
        btnFilterRisk = makeFilterButton("위험")
        btnFilterPhoto = makeFilterButton("사진")
        btnFilterDirect = makeFilterButton("직접")

        filterLayout1.addView(btnFilterAll)
        filterLayout1.addView(btnFilterToday)
        filterLayout1.addView(btnFilterPending)

        filterLayout2.addView(btnFilterRisk)
        filterLayout2.addView(btnFilterPhoto)
        filterLayout2.addView(btnFilterDirect)

        filterCard.addView(filterTitle)
        filterCard.addView(filterLayout1)
        filterCard.addView(filterLayout2)

        tvEmptyHistory = TextView(this).apply {
            text = "저장된 검수 기록이 없습니다."
            textSize = 15f
            setTextColor(0xFF777777.toInt())
            gravity = Gravity.CENTER
            visibility = View.GONE
            setPadding(0, dp(34), 0, dp(34))
            setBackgroundResource(R.drawable.bg_white_card)
        }

        recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(560)
            )
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        btnHistoryBack = Button(this).apply {
            text = "뒤로가기"
            textSize = 15f
            setTextColor(0xFF4E3AD8.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            setBackgroundResource(R.drawable.bg_soft_button)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                topMargin = dp(16)
                bottomMargin = dp(24)
            }
        }

        rootLayout.addView(tvHistoryTitle)
        rootLayout.addView(subTitle)
        rootLayout.addView(summaryCard)
        rootLayout.addView(filterCard)
        rootLayout.addView(tvEmptyHistory)
        rootLayout.addView(recyclerView)
        rootLayout.addView(btnHistoryBack)

        rootScrollView.addView(rootLayout)
        setContentView(rootScrollView)
    }

    private fun makeCardLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setBackgroundResource(R.drawable.bg_white_card)
            elevation = dp(4).toFloat()

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(14)
            }
        }
    }

    private fun makeFilterButton(textValue: String): Button {
        return Button(this).apply {
            text = textValue
            textSize = 13f
            setTextColor(0xFF4E3AD8.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            setBackgroundResource(R.drawable.bg_soft_button)

            layoutParams = LinearLayout.LayoutParams(
                0,
                dp(46),
                1f
            ).apply {
                marginStart = dp(4)
                marginEnd = dp(4)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ReviewHistoryAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        btnFilterAll.setOnClickListener {
            applyFilter(FILTER_ALL)
        }

        btnFilterToday.setOnClickListener {
            applyFilter(FILTER_TODAY)
        }

        btnFilterPending.setOnClickListener {
            applyFilter(FILTER_PENDING)
        }

        btnFilterRisk.setOnClickListener {
            applyFilter(FILTER_RISK)
        }

        btnFilterPhoto.setOnClickListener {
            applyFilter(FILTER_PHOTO)
        }

        btnFilterDirect.setOnClickListener {
            applyFilter(FILTER_DIRECT)
        }

        btnHistoryBack.setOnClickListener {
            finish()
        }
    }

    private fun loadReviews() {
        allReviews.clear()

        val loadedReviews = ReviewStorageUtil.loadReviews(this)
            .sortedByDescending { review ->
                review.createdAt
            }

        allReviews.addAll(loadedReviews)

        applyFilter(currentFilter)
    }

    private fun applyFilter(filter: String) {
        currentFilter = filter
        filteredReviews.clear()

        val todayText = getTodayText()

        val targetReviews = when (filter) {
            FILTER_TODAY -> {
                allReviews.filter { review ->
                    review.createdAt.startsWith(todayText)
                }
            }

            FILTER_PENDING -> {
                allReviews.filter { review ->
                    isPendingReview(review)
                }
            }

            FILTER_RISK -> {
                allReviews.filter { review ->
                    isRiskReview(review)
                }
            }

            FILTER_PHOTO -> {
                allReviews.filter { review ->
                    isPhotoReview(review)
                }
            }

            FILTER_DIRECT -> {
                allReviews.filter { review ->
                    isDirectReview(review)
                }
            }

            else -> {
                allReviews
            }
        }

        filteredReviews.addAll(targetReviews)

        updateFilterButtons()
        updateSummary()
        updateEmptyState()

        adapter.notifyDataSetChanged()
    }

    private fun updateFilterButtons() {
        updateFilterButton(btnFilterAll, currentFilter == FILTER_ALL, "전체")
        updateFilterButton(btnFilterToday, currentFilter == FILTER_TODAY, "오늘")
        updateFilterButton(btnFilterPending, currentFilter == FILTER_PENDING, "보류")
        updateFilterButton(btnFilterRisk, currentFilter == FILTER_RISK, "위험")
        updateFilterButton(btnFilterPhoto, currentFilter == FILTER_PHOTO, "사진")
        updateFilterButton(btnFilterDirect, currentFilter == FILTER_DIRECT, "직접")
    }

    private fun updateFilterButton(
        button: Button,
        selected: Boolean,
        label: String
    ) {
        button.text = if (selected) {
            "✓ $label"
        } else {
            label
        }

        if (selected) {
            button.setBackgroundResource(R.drawable.bg_primary_button)
            button.setTextColor(0xFFFFFFFF.toInt())
        } else {
            button.setBackgroundResource(R.drawable.bg_soft_button)
            button.setTextColor(0xFF4E3AD8.toInt())
        }
    }

    private fun updateSummary() {
        val todayText = getTodayText()

        val totalCount = allReviews.size
        val todayCount = allReviews.count { review ->
            review.createdAt.startsWith(todayText)
        }
        val pendingCount = allReviews.count { review ->
            isPendingReview(review)
        }
        val riskCount = allReviews.count { review ->
            isRiskReview(review)
        }
        val photoCount = allReviews.count { review ->
            isPhotoReview(review)
        }
        val directCount = allReviews.count { review ->
            isDirectReview(review)
        }

        val filterName = when (currentFilter) {
            FILTER_TODAY -> "오늘"
            FILTER_PENDING -> "판단 보류"
            FILTER_RISK -> "위험 감지"
            FILTER_PHOTO -> "사진 검수"
            FILTER_DIRECT -> "직접 검수"
            else -> "전체"
        }

        tvHistorySummary.text =
            "현재 필터: $filterName ${filteredReviews.size}건\n" +
                    "전체 ${totalCount}건 · 오늘 ${todayCount}건 · 보류 ${pendingCount}건 · 위험 ${riskCount}건 · 사진 ${photoCount}건 · 직접 ${directCount}건"
    }

    private fun updateEmptyState() {
        if (filteredReviews.isEmpty()) {
            tvEmptyHistory.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE

            tvEmptyHistory.text = when (currentFilter) {
                FILTER_TODAY -> "오늘 저장된 검수 기록이 없습니다."
                FILTER_PENDING -> "판단 보류 기록이 없습니다."
                FILTER_RISK -> "위험 감지 기록이 없습니다."
                FILTER_PHOTO -> "사진 검수 기록이 없습니다."
                FILTER_DIRECT -> "직접 검수 기록이 없습니다."
                else -> "저장된 검수 기록이 없습니다."
            }
        } else {
            tvEmptyHistory.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun showReviewDetailDialog(review: CatalogReview) {
        val detailText = makeReviewDetailText(review)

        AlertDialog.Builder(this)
            .setTitle("검수 기록 상세")
            .setMessage(detailText)
            .setNegativeButton("닫기", null)
            .setNeutralButton("답변 복사") { _, _ ->
                copyText("상품톡 답변", review.result)
                Toast.makeText(this, "답변이 복사되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .setPositiveButton("전체 복사") { _, _ ->
                copyText("상품톡 검수 기록", detailText)
                Toast.makeText(this, "검수 기록 전체가 복사되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun makeReviewDetailText(review: CatalogReview): String {
        return buildString {
            appendLine("상품명")
            appendLine(if (review.productName.isBlank()) "-" else review.productName)
            appendLine()

            appendLine("카테고리")
            appendLine(if (review.category.isBlank()) "-" else review.category)
            appendLine()

            appendLine("문제 유형 / 옵션")
            appendLine(if (review.optionName.isBlank()) "-" else review.optionName)
            appendLine()

            appendLine("답변")
            appendLine(if (review.result.isBlank()) "-" else review.result)
            appendLine()

            appendLine("판단 이유")
            appendLine(if (review.reason.isBlank()) "-" else review.reason)
            appendLine()

            appendLine("화면/OCR 내용")
            appendLine(if (review.description.isBlank()) "-" else review.description)
            appendLine()

            appendLine("메모")
            appendLine(if (review.memo.isBlank()) "-" else review.memo)
            appendLine()

            appendLine("저장 시간")
            appendLine(if (review.createdAt.isBlank()) "-" else review.createdAt)
        }
    }

    private fun copyText(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    private fun isPendingReview(review: CatalogReview): Boolean {
        return review.result.contains("판단 보류") ||
                review.result.contains("보류") ||
                review.reason.contains("판단 보류") ||
                review.reason.contains("보류")
    }

    private fun isRiskReview(review: CatalogReview): Boolean {
        return review.reason.contains("위험 키워드") ||
                review.reason.contains("민감") ||
                review.reason.contains("금지") ||
                review.optionName.contains("위험") ||
                review.result.contains("판단 보류") ||
                review.result.contains("보류")
    }

    private fun isPhotoReview(review: CatalogReview): Boolean {
        return review.category.contains("사진 검수") ||
                review.optionName.contains("사진") ||
                review.description.contains("이미지 라벨")
    }

    private fun isDirectReview(review: CatalogReview): Boolean {
        return !isPhotoReview(review)
    }

    private fun getTodayText(): String {
        return SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date())
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private inner class ReviewHistoryAdapter :
        RecyclerView.Adapter<ReviewHistoryAdapter.ReviewViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ReviewViewHolder {
            val itemLayout = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(14))
                setBackgroundResource(R.drawable.bg_white_card)
                elevation = dp(3).toFloat()

                layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(12)
                }
            }

            val tvItemTitle = TextView(parent.context).apply {
                textSize = 17f
                setTextColor(0xFF1F1B2E.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val tvItemMeta = TextView(parent.context).apply {
                textSize = 12f
                setTextColor(0xFF81768F.toInt())
                setPadding(0, dp(5), 0, dp(8))
            }

            val tvItemResult = TextView(parent.context).apply {
                textSize = 15f
                setTextColor(0xFFD17600.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                setBackgroundResource(R.drawable.bg_answer_box)
                setPadding(dp(12), dp(10), dp(12), dp(10))
            }

            val tvItemReason = TextView(parent.context).apply {
                textSize = 13f
                setTextColor(0xFF5E566F.toInt())
                setPadding(0, dp(10), 0, 0)
                maxLines = 3
                setLineSpacing(2f, 1.0f)
            }

            val tvItemHint = TextView(parent.context).apply {
                text = "탭하면 상세 보기 / 복사"
                textSize = 12f
                setTextColor(0xFF9C92AA.toInt())
                gravity = Gravity.END
                setPadding(0, dp(8), 0, 0)
            }

            itemLayout.addView(tvItemTitle)
            itemLayout.addView(tvItemMeta)
            itemLayout.addView(tvItemResult)
            itemLayout.addView(tvItemReason)
            itemLayout.addView(tvItemHint)

            return ReviewViewHolder(
                itemView = itemLayout,
                tvItemTitle = tvItemTitle,
                tvItemMeta = tvItemMeta,
                tvItemResult = tvItemResult,
                tvItemReason = tvItemReason
            )
        }

        override fun getItemCount(): Int {
            return filteredReviews.size
        }

        override fun onBindViewHolder(
            holder: ReviewViewHolder,
            position: Int
        ) {
            val review = filteredReviews[position]
            holder.bind(review)
        }

        private inner class ReviewViewHolder(
            itemView: View,
            private val tvItemTitle: TextView,
            private val tvItemMeta: TextView,
            private val tvItemResult: TextView,
            private val tvItemReason: TextView
        ) : RecyclerView.ViewHolder(itemView) {

            fun bind(review: CatalogReview) {
                val title = if (review.productName.isBlank()) {
                    "검수 기록"
                } else {
                    review.productName
                }

                val category = if (review.category.isBlank()) {
                    "카테고리 없음"
                } else {
                    review.category
                }

                val option = if (review.optionName.isBlank()) {
                    "유형 없음"
                } else {
                    review.optionName
                }

                val result = if (review.result.isBlank()) {
                    "결과 없음"
                } else {
                    review.result
                }

                val reason = if (review.reason.isBlank()) {
                    "판단 이유가 없습니다."
                } else {
                    review.reason
                }

                tvItemTitle.text = title
                tvItemMeta.text = "${review.createdAt} · $category · $option"
                tvItemResult.text = "답변: $result"
                tvItemReason.text = reason

                itemView.setOnClickListener {
                    showReviewDetailDialog(review)
                }
            }
        }
    }
}