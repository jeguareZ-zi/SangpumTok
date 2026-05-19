package com.neonloop.sangpumtok

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.neonloop.sangpumtok.util.ReviewGoalStorageUtil
import com.neonloop.sangpumtok.util.ReviewStorageUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewGoalActivity : Activity() {

    private lateinit var tvGoalSummary: TextView
    private lateinit var tvGoalProgressText: TextView
    private lateinit var tvGoalAdvice: TextView
    private lateinit var progressGoal: ProgressBar

    private lateinit var etGoalCount: EditText

    private lateinit var btnGoal50: Button
    private lateinit var btnGoal100: Button
    private lateinit var btnGoal200: Button
    private lateinit var btnGoal500: Button

    private lateinit var btnSaveGoal: Button
    private lateinit var btnClearGoal: Button
    private lateinit var btnGoalBack: Button

    private var todayCount: Int = 0
    private var todayGoal: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildLayout()
        setupClickListeners()
        loadGoalStatus()
    }

    override fun onResume() {
        super.onResume()
        loadGoalStatus()
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

        val title = TextView(this).apply {
            text = "오늘 목표"
            textSize = 29f
            setTextColor(0xFF1F1B2E.toInt())
            setTypeface(null, Typeface.BOLD)
        }

        val subTitle = TextView(this).apply {
            text = "오늘 검수 목표를 설정하고, 진행률을 확인해요."
            textSize = 14f
            setTextColor(0xFF6F6780.toInt())
            setPadding(0, dp(4), 0, dp(16))
            setLineSpacing(3f, 1.0f)
        }

        val statusCard = makeCardLayout()

        val statusTitle = makeSectionTitle("목표 진행 현황")

        tvGoalSummary = TextView(this).apply {
            text = "목표 정보를 불러오는 중입니다."
            textSize = 14f
            setTextColor(0xFF5E566F.toInt())
            setPadding(0, dp(8), 0, 0)
            setLineSpacing(3f, 1.0f)
        }

        progressGoal = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(12)
            ).apply {
                topMargin = dp(14)
            }
        }

        tvGoalProgressText = TextView(this).apply {
            text = "진행률 0%"
            textSize = 18f
            setTextColor(0xFF6C4DFF.toInt())
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
        }

        tvGoalAdvice = TextView(this).apply {
            text = "목표를 설정하면 메인 화면에서도 진행률을 볼 수 있어요."
            textSize = 14f
            setTextColor(0xFF6F6780.toInt())
            setPadding(0, dp(10), 0, 0)
            setLineSpacing(3f, 1.0f)
        }

        statusCard.addView(statusTitle)
        statusCard.addView(tvGoalSummary)
        statusCard.addView(progressGoal)
        statusCard.addView(tvGoalProgressText)
        statusCard.addView(tvGoalAdvice)

        val inputCard = makeCardLayout()

        val inputTitle = makeSectionTitle("목표 설정")

        etGoalCount = EditText(this).apply {
            hint = "예: 100"
            textSize = 16f
            setTextColor(0xFF1F1B2E.toInt())
            setHintTextColor(0xFFA39BAF.toInt())
            inputType = InputType.TYPE_CLASS_NUMBER
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_input_box)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
            ).apply {
                topMargin = dp(12)
            }
        }

        val quickTitle = TextView(this).apply {
            text = "빠른 목표 선택"
            textSize = 14f
            setTextColor(0xFF6F6780.toInt())
            setPadding(0, dp(14), 0, dp(8))
        }

        val quickRow1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val quickRow2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8), 0, 0)
        }

        btnGoal50 = makeQuickButton("50건")
        btnGoal100 = makeQuickButton("100건")
        btnGoal200 = makeQuickButton("200건")
        btnGoal500 = makeQuickButton("500건")

        quickRow1.addView(btnGoal50)
        quickRow1.addView(btnGoal100)
        quickRow2.addView(btnGoal200)
        quickRow2.addView(btnGoal500)

        btnSaveGoal = makePrimaryButton("오늘 목표 저장")
        btnClearGoal = makeSoftButton("오늘 목표 초기화")

        btnSaveGoal.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(52)
        ).apply {
            topMargin = dp(14)
        }

        btnClearGoal.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(52)
        ).apply {
            topMargin = dp(8)
        }

        inputCard.addView(inputTitle)
        inputCard.addView(etGoalCount)
        inputCard.addView(quickTitle)
        inputCard.addView(quickRow1)
        inputCard.addView(quickRow2)
        inputCard.addView(btnSaveGoal)
        inputCard.addView(btnClearGoal)

        val guideCard = makeCardLayout()

        val guideTitle = makeSectionTitle("사용 팁")

        val guideText = TextView(this).apply {
            text =
                "• 목표는 오늘 날짜 기준으로 저장됩니다.\n" +
                        "• 날짜가 바뀌면 목표는 자동으로 0건으로 처리돼요.\n" +
                        "• 메인 화면에서 오늘 검수 건수와 목표 진행률을 바로 확인할 수 있어요.\n" +
                        "• 검수 기록을 저장하면 진행률이 자동으로 올라갑니다."
            textSize = 14f
            setTextColor(0xFF5E566F.toInt())
            setPadding(0, dp(8), 0, 0)
            setLineSpacing(4f, 1.0f)
        }

        guideCard.addView(guideTitle)
        guideCard.addView(guideText)

        btnGoalBack = makeSoftButton("뒤로가기")
        btnGoalBack.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(52)
        ).apply {
            topMargin = dp(2)
            bottomMargin = dp(24)
        }

        rootLayout.addView(title)
        rootLayout.addView(subTitle)
        rootLayout.addView(statusCard)
        rootLayout.addView(inputCard)
        rootLayout.addView(guideCard)
        rootLayout.addView(btnGoalBack)

        scrollView.addView(rootLayout)
        setContentView(scrollView)
    }

    private fun setupClickListeners() {
        btnGoal50.setOnClickListener {
            etGoalCount.setText("50")
        }

        btnGoal100.setOnClickListener {
            etGoalCount.setText("100")
        }

        btnGoal200.setOnClickListener {
            etGoalCount.setText("200")
        }

        btnGoal500.setOnClickListener {
            etGoalCount.setText("500")
        }

        btnSaveGoal.setOnClickListener {
            saveGoal()
        }

        btnClearGoal.setOnClickListener {
            showClearGoalDialog()
        }

        btnGoalBack.setOnClickListener {
            finish()
        }
    }

    private fun loadGoalStatus() {
        val reviews = ReviewStorageUtil.loadReviews(this)
        val todayText = getTodayText()

        todayCount = reviews.count { review ->
            review.createdAt.startsWith(todayText)
        }

        todayGoal = ReviewGoalStorageUtil.getTodayGoal(this)

        updateGoalViews()
    }

    private fun updateGoalViews() {
        if (todayGoal <= 0) {
            progressGoal.progress = 0
            tvGoalProgressText.text = "진행률 0%"
            tvGoalSummary.text =
                "오늘 저장된 검수 기록: ${todayCount}건\n오늘 목표: 미설정"
            tvGoalAdvice.text =
                "아직 오늘 목표가 설정되지 않았어요. 목표를 설정하면 메인 화면에서도 진행률이 표시됩니다."
            etGoalCount.setText("")
            return
        }

        val progressPercent = ((todayCount.toDouble() / todayGoal.toDouble()) * 100)
            .toInt()
            .coerceAtMost(100)

        val remainingCount = (todayGoal - todayCount).coerceAtLeast(0)

        progressGoal.progress = progressPercent
        tvGoalProgressText.text = "${progressPercent}%"

        tvGoalSummary.text =
            "오늘 저장된 검수 기록: ${todayCount}건\n오늘 목표: ${todayGoal}건\n남은 검수: ${remainingCount}건"

        tvGoalAdvice.text = when {
            todayCount >= todayGoal -> {
                "🎉 오늘 목표 달성! 더 저장하면 초과 달성 기록으로 쌓입니다."
            }

            remainingCount <= 5 -> {
                "거의 끝났어요. ${remainingCount}건만 더 하면 오늘 목표 달성입니다."
            }

            progressPercent >= 80 -> {
                "마무리 페이스입니다. 남은 ${remainingCount}건만 처리하면 돼요."
            }

            progressPercent >= 50 -> {
                "절반 이상 완료했어요. 지금 페이스 괜찮습니다."
            }

            else -> {
                "천천히 쌓아가도 됩니다. 오늘 목표까지 ${remainingCount}건 남았어요."
            }
        }

        etGoalCount.setText(todayGoal.toString())
    }

    private fun saveGoal() {
        val goalText = etGoalCount.text.toString().trim()

        if (goalText.isBlank()) {
            Toast.makeText(this, "오늘 목표 건수를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val goalCount = goalText.toIntOrNull()

        if (goalCount == null || goalCount <= 0) {
            Toast.makeText(this, "1건 이상의 숫자로 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        ReviewGoalStorageUtil.saveTodayGoal(
            context = this,
            goalCount = goalCount
        )

        Toast.makeText(this, "오늘 목표가 ${goalCount}건으로 저장되었습니다.", Toast.LENGTH_SHORT).show()

        loadGoalStatus()
    }

    private fun showClearGoalDialog() {
        if (todayGoal <= 0) {
            Toast.makeText(this, "초기화할 오늘 목표가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("오늘 목표 초기화")
            .setMessage("오늘 설정한 검수 목표를 초기화할까요?")
            .setNegativeButton("취소", null)
            .setPositiveButton("초기화") { _, _ ->
                ReviewGoalStorageUtil.clearTodayGoal(this)
                Toast.makeText(this, "오늘 목표가 초기화되었습니다.", Toast.LENGTH_SHORT).show()
                loadGoalStatus()
            }
            .show()
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

    private fun makeQuickButton(textValue: String): Button {
        return Button(this).apply {
            text = textValue
            textSize = 15f
            setTextColor(0xFF4E3AD8.toInt())
            setTypeface(null, Typeface.BOLD)
            setBackgroundResource(R.drawable.bg_soft_button)

            layoutParams = LinearLayout.LayoutParams(
                0,
                dp(48),
                1f
            ).apply {
                marginStart = dp(4)
                marginEnd = dp(4)
            }
        }
    }

    private fun makePrimaryButton(textValue: String): Button {
        return Button(this).apply {
            text = textValue
            textSize = 15f
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(null, Typeface.BOLD)
            setBackgroundResource(R.drawable.bg_primary_button)
        }
    }

    private fun makeSoftButton(textValue: String): Button {
        return Button(this).apply {
            text = textValue
            textSize = 15f
            setTextColor(0xFF4E3AD8.toInt())
            setTypeface(null, Typeface.BOLD)
            setBackgroundResource(R.drawable.bg_soft_button)
        }
    }

    private fun getTodayText(): String {
        val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
        return formatter.format(Date())
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}