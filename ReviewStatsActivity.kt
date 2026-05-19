package com.neonloop.sangpumtok

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.neonloop.sangpumtok.model.CatalogReview
import com.neonloop.sangpumtok.util.ReviewStorageUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewStatsActivity : Activity() {

    private lateinit var tvStatsTitle: TextView
    private lateinit var tvStatsSummary: TextView

    private lateinit var tvTotalCount: TextView
    private lateinit var tvTodayCount: TextView
    private lateinit var tvPendingCount: TextView
    private lateinit var tvRiskCount: TextView
    private lateinit var tvPhotoCount: TextView
    private lateinit var tvDirectCount: TextView

    private lateinit var tvTodayRate: TextView
    private lateinit var tvRiskRate: TextView
    private lateinit var tvPendingRate: TextView
    private lateinit var tvPhotoRate: TextView

    private lateinit var tvLatestReview: TextView

    private lateinit var btnOpenAll: Button
    private lateinit var btnOpenToday: Button
    private lateinit var btnOpenPending: Button
    private lateinit var btnOpenRisk: Button
    private lateinit var btnOpenPhoto: Button
    private lateinit var btnOpenDirect: Button
    private lateinit var btnStatsBack: Button

    private var reviews: List<CatalogReview> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildLayout()
        setupClickListeners()
        loadStats()
    }

    override fun onResume() {
        super.onResume()
        loadStats()
    }

    private fun buildLayout() {
        val scrollView = ScrollView(this).apply {
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

        tvStatsTitle = TextView(this).apply {
            text = "검수 통계"
            textSize = 29f
            setTextColor(0xFF1F1B2E.toInt())
            setTypeface(null, Typeface.BOLD)
        }

        val subTitle = TextView(this).apply {
            text = "오늘 작업량, 보류율, 위험 감지 비율을 한눈에 확인해요."
            textSize = 14f
            setTextColor(0xFF6F6780.toInt())
            setPadding(0, dp(4), 0, dp(16))
            setLineSpacing(3f, 1.0f)
        }

        val summaryCard = makeCardLayout()

        val summaryTitle = makeSectionTitle("오늘의 요약")
        tvStatsSummary = TextView(this).apply {
            text = "저장된 검수 기록을 기준으로 통계를 계산합니다."
            textSize = 14f
            setTextColor(0xFF5E566F.toInt())
            setPadding(0, dp(8), 0, 0)
            setLineSpacing(3f, 1.0f)
        }

        summaryCard.addView(summaryTitle)
        summaryCard.addView(tvStatsSummary)

        val countCard = makeCardLayout()

        val countTitle = makeSectionTitle("검수 기록")
        countCard.addView(countTitle)

        val countGrid1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(10), 0, 0)
        }

        val countGrid2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8), 0, 0)
        }

        tvTotalCount = makeStatValueText("#1F1B2E")
        tvTodayCount = makeStatValueText("#6C4DFF")
        tvPendingCount = makeStatValueText("#FF8A00")
        tvRiskCount = makeStatValueText("#FF4D6D")
        tvPhotoCount = makeStatValueText("#0097A7")
        tvDirectCount = makeStatValueText("#4E3AD8")

        countGrid1.addView(makeStatBox("전체", tvTotalCount))
        countGrid1.addView(makeStatBox("오늘", tvTodayCount))
        countGrid1.addView(makeStatBox("보류", tvPendingCount))

        countGrid2.addView(makeStatBox("위험", tvRiskCount))
        countGrid2.addView(makeStatBox("사진", tvPhotoCount))
        countGrid2.addView(makeStatBox("직접", tvDirectCount))

        countCard.addView(countGrid1)
        countCard.addView(countGrid2)

        val rateCard = makeCardLayout()

        val rateTitle = makeSectionTitle("비율 분석")
        tvTodayRate = makeValueText()
        tvRiskRate = makeValueText()
        tvPendingRate = makeValueText()
        tvPhotoRate = makeValueText()

        rateCard.addView(rateTitle)
        rateCard.addView(makeLabelValueRow("오늘 검수 비율", tvTodayRate))
        rateCard.addView(makeLabelValueRow("위험 감지 비율", tvRiskRate))
        rateCard.addView(makeLabelValueRow("판단 보류 비율", tvPendingRate))
        rateCard.addView(makeLabelValueRow("사진 검수 비율", tvPhotoRate))

        val latestCard = makeCardLayout()

        val latestTitle = makeSectionTitle("최근 검수 기록")
        tvLatestReview = TextView(this).apply {
            text = "최근 검수 기록이 없습니다."
            textSize = 14f
            setTextColor(0xFF5E566F.toInt())
            setPadding(0, dp(10), 0, 0)
            setLineSpacing(3f, 1.0f)
        }

        latestCard.addView(latestTitle)
        latestCard.addView(tvLatestReview)

        val buttonCard = makeCardLayout()

        val buttonTitle = makeSectionTitle("기록 바로 보기")

        btnOpenAll = makeFullButton("전체 기록 보기")
        btnOpenToday = makeFullButton("오늘 기록 보기")
        btnOpenPending = makeFullButton("보류 기록 보기")
        btnOpenRisk = makeFullButton("위험 기록 보기")
        btnOpenPhoto = makeFullButton("사진 검수 기록 보기")
        btnOpenDirect = makeFullButton("직접 검수 기록 보기")

        buttonCard.addView(buttonTitle)
        buttonCard.addView(btnOpenAll)
        buttonCard.addView(btnOpenToday)
        buttonCard.addView(btnOpenPending)
        buttonCard.addView(btnOpenRisk)
        buttonCard.addView(btnOpenPhoto)
        buttonCard.addView(btnOpenDirect)

        btnStatsBack = Button(this).apply {
            text = "뒤로가기"
            textSize = 15f
            setTextColor(0xFF4E3AD8.toInt())
            setTypeface(null, Typeface.BOLD)
            setBackgroundResource(R.drawable.bg_soft_button)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                topMargin = dp(16)
                bottomMargin = dp(24)
            }
        }

        rootLayout.addView(tvStatsTitle)
        rootLayout.addView(subTitle)
        rootLayout.addView(summaryCard)
        rootLayout.addView(countCard)
        rootLayout.addView(rateCard)
        rootLayout.addView(latestCard)
        rootLayout.addView(buttonCard)
        rootLayout.addView(btnStatsBack)

        scrollView.addView(rootLayout)
        setContentView(scrollView)
    }

    private fun setupClickListeners() {
        btnOpenAll.setOnClickListener {
            openHistory(ReviewHistoryActivity.FILTER_ALL)
        }

        btnOpenToday.setOnClickListener {
            openHistory(ReviewHistoryActivity.FILTER_TODAY)
        }

        btnOpenPending.setOnClickListener {
            openHistory(ReviewHistoryActivity.FILTER_PENDING)
        }

        btnOpenRisk.setOnClickListener {
            openHistory(ReviewHistoryActivity.FILTER_RISK)
        }

        btnOpenPhoto.setOnClickListener {
            openHistory(ReviewHistoryActivity.FILTER_PHOTO)
        }

        btnOpenDirect.setOnClickListener {
            openHistory(ReviewHistoryActivity.FILTER_DIRECT)
        }

        btnStatsBack.setOnClickListener {
            finish()
        }
    }

    private fun loadStats() {
        reviews = ReviewStorageUtil.loadReviews(this)
            .sortedByDescending { review ->
                review.createdAt
            }

        val todayText = getTodayText()

        val totalCount = reviews.size

        val todayCount = reviews.count { review ->
            review.createdAt.startsWith(todayText)
        }

        val pendingCount = reviews.count { review ->
            isPendingReview(review)
        }

        val riskCount = reviews.count { review ->
            isRiskReview(review)
        }

        val photoCount = reviews.count { review ->
            isPhotoReview(review)
        }

        val directCount = reviews.count { review ->
            isDirectReview(review)
        }

        tvTotalCount.text = "${totalCount}건"
        tvTodayCount.text = "${todayCount}건"
        tvPendingCount.text = "${pendingCount}건"
        tvRiskCount.text = "${riskCount}건"
        tvPhotoCount.text = "${photoCount}건"
        tvDirectCount.text = "${directCount}건"

        tvTodayRate.text = makePercentText(todayCount, totalCount)
        tvRiskRate.text = makePercentText(riskCount, totalCount)
        tvPendingRate.text = makePercentText(pendingCount, totalCount)
        tvPhotoRate.text = makePercentText(photoCount, totalCount)

        tvStatsSummary.text = makeSummaryText(
            totalCount = totalCount,
            todayCount = todayCount,
            pendingCount = pendingCount,
            riskCount = riskCount,
            photoCount = photoCount,
            directCount = directCount
        )

        tvLatestReview.text = makeLatestReviewText()
    }

    private fun makeSummaryText(
        totalCount: Int,
        todayCount: Int,
        pendingCount: Int,
        riskCount: Int,
        photoCount: Int,
        directCount: Int
    ): String {
        return when {
            totalCount == 0 -> {
                "아직 저장된 검수 기록이 없습니다.\n상품 검수나 사진 검수를 먼저 저장해보세요."
            }

            pendingCount > 0 || riskCount > 0 -> {
                "전체 ${totalCount}건 중 오늘 ${todayCount}건을 검수했습니다.\n보류 ${pendingCount}건, 위험 감지 ${riskCount}건이 있어 재확인이 필요할 수 있어요."
            }

            photoCount > 0 -> {
                "전체 ${totalCount}건 중 사진 검수 ${photoCount}건, 직접 검수 ${directCount}건이 저장되어 있습니다."
            }

            else -> {
                "전체 ${totalCount}건 중 오늘 ${todayCount}건을 검수했습니다.\n현재까지 저장된 기록이 정상적으로 누적되고 있어요."
            }
        }
    }

    private fun makeLatestReviewText(): String {
        val latestReview = reviews.firstOrNull()

        if (latestReview == null) {
            return "최근 검수 기록이 없습니다."
        }

        val productName = if (latestReview.productName.isBlank()) {
            "상품명 없음"
        } else {
            latestReview.productName
        }

        val category = if (latestReview.category.isBlank()) {
            "카테고리 없음"
        } else {
            latestReview.category
        }

        val optionName = if (latestReview.optionName.isBlank()) {
            "유형 없음"
        } else {
            latestReview.optionName
        }

        val result = if (latestReview.result.isBlank()) {
            "결과 없음"
        } else {
            latestReview.result
        }

        return buildString {
            appendLine("상품명: $productName")
            appendLine("분류: $category")
            appendLine("유형: $optionName")
            appendLine("답변: $result")
            appendLine("저장 시간: ${latestReview.createdAt}")
        }
    }

    private fun makePercentText(
        count: Int,
        total: Int
    ): String {
        if (total <= 0) {
            return "0%"
        }

        val percent = (count.toDouble() / total.toDouble()) * 100.0

        return String.format(
            Locale.KOREA,
            "%.1f%%",
            percent
        )
    }

    private fun openHistory(filter: String) {
        val intent = Intent(this, ReviewHistoryActivity::class.java).apply {
            putExtra(
                ReviewHistoryActivity.EXTRA_START_FILTER,
                filter
            )
        }

        startActivity(intent)
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

    private fun makeSectionTitle(textValue: String): TextView {
        return TextView(this).apply {
            text = textValue
            textSize = 18f
            setTextColor(0xFF1F1B2E.toInt())
            setTypeface(null, Typeface.BOLD)
        }
    }

    private fun makeValueText(): TextView {
        return TextView(this).apply {
            text = "-"
            textSize = 15f
            setTextColor(0xFF1F1B2E.toInt())
            gravity = Gravity.END
            setTypeface(null, Typeface.BOLD)
        }
    }

    private fun makeStatValueText(colorHex: String): TextView {
        return TextView(this).apply {
            text = "0건"
            textSize = 21f
            setTextColor(android.graphics.Color.parseColor(colorHex))
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }
    }

    private fun makeStatBox(
        label: String,
        valueTextView: TextView
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(10), dp(8), dp(10))
            setBackgroundResource(R.drawable.bg_stat_box)

            layoutParams = LinearLayout.LayoutParams(
                0,
                dp(86),
                1f
            ).apply {
                marginStart = dp(4)
                marginEnd = dp(4)
            }

            val labelTextView = TextView(this@ReviewStatsActivity).apply {
                text = label
                textSize = 13f
                setTextColor(0xFF6F6780.toInt())
                gravity = Gravity.CENTER
            }

            addView(labelTextView)
            addView(valueTextView)
        }
    }

    private fun makeLabelValueRow(
        label: String,
        valueTextView: TextView
    ): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }

        val labelTextView = TextView(this).apply {
            text = label
            textSize = 15f
            setTextColor(0xFF5E566F.toInt())
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        valueTextView.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )

        row.addView(labelTextView)
        row.addView(valueTextView)

        return row
    }

    private fun makeFullButton(textValue: String): Button {
        return Button(this).apply {
            text = textValue
            textSize = 15f
            setTextColor(0xFF4E3AD8.toInt())
            setTypeface(null, Typeface.BOLD)
            setBackgroundResource(R.drawable.bg_soft_button)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(50)
            ).apply {
                topMargin = dp(8)
            }
        }
    }

    private fun getTodayText(): String {
        return SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date())
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}