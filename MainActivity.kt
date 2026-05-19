package com.neonloop.sangpumtok

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.neonloop.sangpumtok.model.CatalogReview
import com.neonloop.sangpumtok.util.ReviewGoalStorageUtil
import com.neonloop.sangpumtok.util.ReviewStorageUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var btnStartReview: Button
    private lateinit var btnCaptureReview: Button
    private lateinit var btnChecklist: Button
    private lateinit var btnReviewTemplate: Button
    private lateinit var btnReviewGoal: Button
    private lateinit var btnReviewMemo: Button
    private lateinit var btnReviewHistory: Button
    private lateinit var btnReviewStats: Button
    private lateinit var btnRiskKeyword: Button
    private lateinit var btnGuide: Button

    private lateinit var btnOpenPendingReviews: Button
    private lateinit var btnOpenRiskReviews: Button
    private lateinit var btnOpenPhotoReviews: Button
    private lateinit var btnCopyTodayReport: Button
    private lateinit var btnShareTodayReport: Button

    private lateinit var tvDashboardTodayCount: TextView
    private lateinit var tvDashboardPendingCount: TextView
    private lateinit var tvDashboardRiskCount: TextView
    private lateinit var tvDashboardTotalCount: TextView
    private lateinit var tvDashboardPhotoCount: TextView
    private lateinit var tvDashboardSummary: TextView

    private lateinit var tvDashboardGoalText: TextView
    private lateinit var tvDashboardGoalDetail: TextView
    private lateinit var progressDashboardGoal: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupClickListeners()
        loadDashboard()
    }

    override fun onResume() {
        super.onResume()
        loadDashboard()
    }

    private fun initViews() {
        btnStartReview = findViewById(R.id.btnStartReview)
        btnCaptureReview = findViewById(R.id.btnCaptureReview)
        btnChecklist = findViewById(R.id.btnChecklist)
        btnReviewTemplate = findViewById(R.id.btnReviewTemplate)
        btnReviewGoal = findViewById(R.id.btnReviewGoal)
        btnReviewMemo = findViewById(R.id.btnReviewMemo)
        btnReviewHistory = findViewById(R.id.btnReviewHistory)
        btnReviewStats = findViewById(R.id.btnReviewStats)
        btnRiskKeyword = findViewById(R.id.btnRiskKeyword)
        btnGuide = findViewById(R.id.btnGuide)

        btnOpenPendingReviews = findViewById(R.id.btnOpenPendingReviews)
        btnOpenRiskReviews = findViewById(R.id.btnOpenRiskReviews)
        btnOpenPhotoReviews = findViewById(R.id.btnOpenPhotoReviews)
        btnCopyTodayReport = findViewById(R.id.btnCopyTodayReport)
        btnShareTodayReport = findViewById(R.id.btnShareTodayReport)

        tvDashboardTodayCount = findViewById(R.id.tvDashboardTodayCount)
        tvDashboardPendingCount = findViewById(R.id.tvDashboardPendingCount)
        tvDashboardRiskCount = findViewById(R.id.tvDashboardRiskCount)
        tvDashboardTotalCount = findViewById(R.id.tvDashboardTotalCount)
        tvDashboardPhotoCount = findViewById(R.id.tvDashboardPhotoCount)
        tvDashboardSummary = findViewById(R.id.tvDashboardSummary)

        tvDashboardGoalText = findViewById(R.id.tvDashboardGoalText)
        tvDashboardGoalDetail = findViewById(R.id.tvDashboardGoalDetail)
        progressDashboardGoal = findViewById(R.id.progressDashboardGoal)
    }

    private fun setupClickListeners() {
        btnStartReview.setOnClickListener {
            startActivity(Intent(this, ProductReviewActivity::class.java))
        }

        btnCaptureReview.setOnClickListener {
            startActivity(Intent(this, CaptureReviewActivity::class.java))
        }

        btnChecklist.setOnClickListener {
            startActivity(Intent(this, ReviewChecklistActivity::class.java))
        }

        btnReviewTemplate.setOnClickListener {
            startActivity(Intent(this, ReviewTemplateActivity::class.java))
        }

        btnReviewGoal.setOnClickListener {
            openGoalSetting()
        }

        btnReviewMemo.setOnClickListener {
            startActivity(Intent(this, ReviewMemoActivity::class.java))
        }

        btnReviewHistory.setOnClickListener {
            openHistoryWithFilter(ReviewHistoryActivity.FILTER_ALL)
        }

        btnReviewStats.setOnClickListener {
            startActivity(Intent(this, ReviewStatsActivity::class.java))
        }

        btnRiskKeyword.setOnClickListener {
            startActivity(Intent(this, RiskKeywordActivity::class.java))
        }

        btnGuide.setOnClickListener {
            startActivity(Intent(this, GuideActivity::class.java))
        }

        btnOpenPendingReviews.setOnClickListener {
            openHistoryWithFilter(ReviewHistoryActivity.FILTER_PENDING)
        }

        btnOpenRiskReviews.setOnClickListener {
            openHistoryWithFilter(ReviewHistoryActivity.FILTER_RISK)
        }

        btnOpenPhotoReviews.setOnClickListener {
            openHistoryWithFilter(ReviewHistoryActivity.FILTER_PHOTO)
        }

        btnCopyTodayReport.setOnClickListener {
            copyTodayReport()
        }

        btnShareTodayReport.setOnClickListener {
            shareTodayReport()
        }

        tvDashboardSummary.setOnClickListener {
            showTodayReportDialog()
        }

        tvDashboardTodayCount.setOnClickListener {
            openHistoryWithFilter(ReviewHistoryActivity.FILTER_TODAY)
        }

        tvDashboardPendingCount.setOnClickListener {
            openHistoryWithFilter(ReviewHistoryActivity.FILTER_PENDING)
        }

        tvDashboardRiskCount.setOnClickListener {
            openHistoryWithFilter(ReviewHistoryActivity.FILTER_RISK)
        }

        tvDashboardPhotoCount.setOnClickListener {
            openHistoryWithFilter(ReviewHistoryActivity.FILTER_PHOTO)
        }

        tvDashboardTotalCount.setOnClickListener {
            openHistoryWithFilter(ReviewHistoryActivity.FILTER_ALL)
        }

        tvDashboardGoalText.setOnClickListener {
            openGoalSetting()
        }

        tvDashboardGoalDetail.setOnClickListener {
            openGoalSetting()
        }

        progressDashboardGoal.setOnClickListener {
            openGoalSetting()
        }
    }

    private fun openHistoryWithFilter(filter: String) {
        val intent = Intent(this, ReviewHistoryActivity::class.java).apply {
            putExtra(
                ReviewHistoryActivity.EXTRA_START_FILTER,
                filter
            )
        }

        startActivity(intent)
    }

    private fun openGoalSetting() {
        startActivity(Intent(this, ReviewGoalActivity::class.java))
    }

    private fun loadDashboard() {
        val reviews = ReviewStorageUtil.loadReviews(this)
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

        val todayGoal = ReviewGoalStorageUtil.getTodayGoal(this)

        tvDashboardTodayCount.text = "${todayCount}건"
        tvDashboardPendingCount.text = "${pendingCount}건"
        tvDashboardRiskCount.text = "${riskCount}건"
        tvDashboardTotalCount.text = "${totalCount}건"
        tvDashboardPhotoCount.text = "${photoCount}건"

        tvDashboardSummary.text = makeDashboardSummary(
            totalCount = totalCount,
            todayCount = todayCount,
            pendingCount = pendingCount,
            riskCount = riskCount,
            photoCount = photoCount,
            todayGoal = todayGoal
        )

        updateGoalProgress(
            todayCount = todayCount,
            todayGoal = todayGoal
        )
    }

    private fun updateGoalProgress(
        todayCount: Int,
        todayGoal: Int
    ) {
        if (todayGoal <= 0) {
            progressDashboardGoal.progress = 0
            tvDashboardGoalText.text = "🎯 오늘 목표가 아직 없어요"
            tvDashboardGoalDetail.text =
                "탭해서 오늘 검수 목표를 설정해보세요. 목표가 있으면 진행률을 바로 확인할 수 있어요."
            return
        }

        val progressPercent = ((todayCount.toDouble() / todayGoal.toDouble()) * 100)
            .toInt()
            .coerceAtMost(100)

        val remainingCount = (todayGoal - todayCount).coerceAtLeast(0)

        progressDashboardGoal.progress = progressPercent

        tvDashboardGoalText.text = when {
            todayCount >= todayGoal -> {
                "🎉 오늘 목표 달성! ${todayCount}/${todayGoal}건 완료 · 100%"
            }

            progressPercent >= 80 -> {
                "🔥 목표 거의 달성! ${todayCount}/${todayGoal}건 완료 · ${progressPercent}%"
            }

            progressPercent >= 50 -> {
                "💪 절반 이상 완료! ${todayCount}/${todayGoal}건 완료 · ${progressPercent}%"
            }

            else -> {
                "🎯 오늘 목표 ${todayGoal}건 중 ${todayCount}건 완료 · ${progressPercent}%"
            }
        }

        tvDashboardGoalDetail.text = when {
            todayCount >= todayGoal -> {
                "완료! 오늘 목표를 채웠어요. 더 검수하면 초과 달성 기록으로 쌓입니다. 탭하면 목표를 다시 조정할 수 있어요."
            }

            remainingCount <= 3 -> {
                "남은 검수 ${remainingCount}건 · 진짜 거의 끝났어요!"
            }

            remainingCount <= 10 -> {
                "남은 검수 ${remainingCount}건 · 조금만 더 하면 목표 달성입니다."
            }

            progressPercent >= 80 -> {
                "남은 검수 ${remainingCount}건 · 마무리 페이스로 가면 됩니다."
            }

            progressPercent >= 50 -> {
                "남은 검수 ${remainingCount}건 · 안정적으로 진행 중이에요."
            }

            else -> {
                "남은 검수 ${remainingCount}건 · 지금부터 페이스를 올려볼 시간입니다."
            }
        }
    }

    private fun makeDashboardSummary(
        totalCount: Int,
        todayCount: Int,
        pendingCount: Int,
        riskCount: Int,
        photoCount: Int,
        todayGoal: Int
    ): String {
        val goalText = when {
            todayGoal <= 0 -> {
                "오늘 목표는 아직 설정되지 않았습니다."
            }

            todayCount >= todayGoal -> {
                "오늘 목표 ${todayGoal}건을 달성했습니다."
            }

            else -> {
                "오늘 목표 ${todayGoal}건까지 ${todayGoal - todayCount}건 남았습니다."
            }
        }

        return when {
            totalCount == 0 -> {
                "아직 저장된 검수 기록이 없습니다. 새 상품 검수나 사진 검수를 시작해보세요. $goalText\n\n요약문을 누르면 오늘 리포트를 미리 볼 수 있어요."
            }

            todayCount == 0 -> {
                "오늘 저장된 검수 기록은 아직 없습니다. 이전 누적 검수 기록은 ${totalCount}건이고, 사진 검수는 ${photoCount}건입니다. $goalText\n\n요약문을 누르면 오늘 리포트를 미리 볼 수 있어요."
            }

            pendingCount > 0 || riskCount > 0 -> {
                "오늘 ${todayCount}건을 검수했습니다. 보류 ${pendingCount}건, 위험 감지 ${riskCount}건, 사진 검수 ${photoCount}건을 다시 확인할 수 있어요. $goalText\n\n요약문을 누르면 오늘 리포트를 미리 볼 수 있어요."
            }

            photoCount > 0 -> {
                "오늘 ${todayCount}건을 검수했습니다. 현재까지 사진 검수는 ${photoCount}건, 전체 저장 기록은 ${totalCount}건입니다. $goalText\n\n요약문을 누르면 오늘 리포트를 미리 볼 수 있어요."
            }

            else -> {
                "오늘 ${todayCount}건을 검수했습니다. 현재까지 누적 저장 기록은 ${totalCount}건입니다. $goalText\n\n요약문을 누르면 오늘 리포트를 미리 볼 수 있어요."
            }
        }
    }

    private fun showTodayReportDialog() {
        val reportText = makeTodayReportText()

        AlertDialog.Builder(this)
            .setTitle("오늘 검수 리포트")
            .setMessage(reportText)
            .setNegativeButton("닫기", null)
            .setNeutralButton("복사") { _, _ ->
                copyTodayReport()
            }
            .setPositiveButton("공유") { _, _ ->
                shareTodayReport()
            }
            .show()
    }

    private fun copyTodayReport() {
        val reportText = makeTodayReportText()

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("상품톡 오늘 검수 리포트", reportText)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "오늘 리포트가 복사되었습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun shareTodayReport() {
        val reportText = makeTodayReportText()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "상품톡 오늘 검수 리포트")
            putExtra(Intent.EXTRA_TEXT, reportText)
        }

        startActivity(Intent.createChooser(intent, "오늘 리포트 공유하기"))
    }

    private fun makeTodayReportText(): String {
        val reviews = ReviewStorageUtil.loadReviews(this)
        val todayText = getTodayText()
        val todayGoal = ReviewGoalStorageUtil.getTodayGoal(this)

        val todayReviews = reviews.filter { review ->
            review.createdAt.startsWith(todayText)
        }

        val todayCount = todayReviews.size

        val todayPendingCount = todayReviews.count { review ->
            isPendingReview(review)
        }

        val todayRiskCount = todayReviews.count { review ->
            isRiskReview(review)
        }

        val todayPhotoCount = todayReviews.count { review ->
            isPhotoReview(review)
        }

        val totalCount = reviews.size

        val goalText = if (todayGoal <= 0) {
            "미설정"
        } else {
            "${todayGoal}건"
        }

        val progressPercent = if (todayGoal <= 0) {
            0
        } else {
            ((todayCount.toDouble() / todayGoal.toDouble()) * 100)
                .toInt()
                .coerceAtMost(100)
        }

        val remainingCount = if (todayGoal <= 0) {
            "-"
        } else {
            "${(todayGoal - todayCount).coerceAtLeast(0)}건"
        }

        val goalStatus = when {
            todayGoal <= 0 -> "목표 미설정"
            todayCount >= todayGoal -> "목표 달성"
            progressPercent >= 80 -> "거의 달성"
            progressPercent >= 50 -> "절반 이상 완료"
            else -> "진행 중"
        }

        return buildString {
            appendLine("[상품톡 오늘 검수 리포트]")
            appendLine()
            appendLine("날짜: $todayText")
            appendLine()
            appendLine("오늘 검수: ${todayCount}건")
            appendLine("오늘 판단 보류: ${todayPendingCount}건")
            appendLine("오늘 위험 감지: ${todayRiskCount}건")
            appendLine("오늘 사진 검수: ${todayPhotoCount}건")
            appendLine("전체 저장: ${totalCount}건")
            appendLine()
            appendLine("오늘 목표: $goalText")
            appendLine("진행률: ${progressPercent}%")
            appendLine("남은 검수: $remainingCount")
            appendLine("목표 상태: $goalStatus")
        }
    }

    private fun getTodayText(): String {
        return SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date())
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
                review.result.contains("판단 보류")
    }

    private fun isPhotoReview(review: CatalogReview): Boolean {
        return review.category.contains("사진 검수") ||
                review.optionName.contains("사진") ||
                review.description.contains("이미지 라벨")
    }
}