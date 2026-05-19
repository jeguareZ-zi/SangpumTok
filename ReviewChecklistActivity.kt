package com.neonloop.sangpumtok

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewChecklistActivity : Activity() {

    private lateinit var tvChecklistSummary: TextView
    private lateinit var tvChecklistProgress: TextView

    private lateinit var checklistContainer: LinearLayout

    private lateinit var btnCheckAll: Button
    private lateinit var btnUncheckAll: Button
    private lateinit var btnCopyChecklist: Button
    private lateinit var btnChecklistBack: Button

    private val checkBoxMap = mutableMapOf<String, CheckBox>()

    private val checklistItems = listOf(
        ChecklistItem(
            id = "product_name",
            group = "기본 정보",
            title = "상품명 확인",
            description = "상품명에 브랜드, 구성, 수량, 성별, 연령 정보가 과장 없이 들어갔는지 확인"
        ),
        ChecklistItem(
            id = "category",
            group = "기본 정보",
            title = "카테고리 확인",
            description = "상품이 실제로 속해야 하는 카테고리에 배치되어 있는지 확인"
        ),
        ChecklistItem(
            id = "image_match",
            group = "이미지",
            title = "이미지와 상품 정보 일치",
            description = "대표 이미지가 상품명/옵션/구성품과 다르지 않은지 확인"
        ),
        ChecklistItem(
            id = "option_value",
            group = "옵션",
            title = "옵션명 / 옵션값 확인",
            description = "색상, 사이즈, 용량, 수량 등 구매 옵션과 값이 자연스럽게 연결되는지 확인"
        ),
        ChecklistItem(
            id = "same_product",
            group = "동일 상품",
            title = "동일 상품 여부 확인",
            description = "이미지, 상품명, 구성, 옵션이 같은 상품인지 비교"
        ),
        ChecklistItem(
            id = "risk_keyword",
            group = "위험 요소",
            title = "위험/민감 키워드 확인",
            description = "성인, 가품, 불법, 혐오, 정치·역사 민감 요소, 욱일기 유사 패턴 등 확인"
        ),
        ChecklistItem(
            id = "policy_hold",
            group = "위험 요소",
            title = "판단 보류 필요 여부 확인",
            description = "확정하기 애매하거나 정책 기준 재확인이 필요한 경우 보류 처리"
        ),
        ChecklistItem(
            id = "reason_written",
            group = "기록",
            title = "판단 이유 정리",
            description = "왜 승인/수정/보류인지 나중에 봐도 이해되게 기록"
        ),
        ChecklistItem(
            id = "answer_copy",
            group = "기록",
            title = "답변 복사 확인",
            description = "최종 답변만 복사해서 바로 제출 가능한 상태인지 확인"
        ),
        ChecklistItem(
            id = "save_record",
            group = "기록",
            title = "검수 기록 저장",
            description = "상품톡 저장된 기록에 남겨서 오늘 통계와 목표에 반영"
        )
    )

    companion object {
        private const val PREF_NAME = "sangpumtok_checklist_storage"
        private const val KEY_DATE = "checklist_date"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildLayout()
        setupClickListeners()
        loadChecklist()
    }

    override fun onResume() {
        super.onResume()
        loadChecklist()
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
            text = "검수 체크리스트"
            textSize = 29f
            setTextColor(0xFF1F1B2E.toInt())
            setTypeface(null, Typeface.BOLD)
        }

        val subTitle = TextView(this).apply {
            text = "상품 검수 전에 빠뜨리기 쉬운 항목을 오늘 기준으로 체크해요."
            textSize = 14f
            setTextColor(0xFF6F6780.toInt())
            setPadding(0, dp(4), 0, dp(16))
            setLineSpacing(3f, 1.0f)
        }

        val summaryCard = makeCardLayout()

        val summaryTitle = makeSectionTitle("오늘 체크 현황")

        tvChecklistSummary = TextView(this).apply {
            text = "체크리스트를 불러오는 중입니다."
            textSize = 14f
            setTextColor(0xFF5E566F.toInt())
            setPadding(0, dp(8), 0, 0)
            setLineSpacing(3f, 1.0f)
        }

        tvChecklistProgress = TextView(this).apply {
            text = "0%"
            textSize = 28f
            setTextColor(0xFF6C4DFF.toInt())
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(4))
        }

        summaryCard.addView(summaryTitle)
        summaryCard.addView(tvChecklistSummary)
        summaryCard.addView(tvChecklistProgress)

        val checklistCard = makeCardLayout()

        val checklistTitle = makeSectionTitle("체크 항목")

        checklistContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, 0)
        }

        checklistCard.addView(checklistTitle)
        checklistCard.addView(checklistContainer)

        buildChecklistItems()

        val actionCard = makeCardLayout()

        val actionTitle = makeSectionTitle("작업 메뉴")

        val row1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(12), 0, 0)
        }

        btnCheckAll = makeSoftButton("전체 체크")
        btnUncheckAll = makeSoftButton("전체 해제")

        btnCheckAll.layoutParams = LinearLayout.LayoutParams(
            0,
            dp(50),
            1f
        ).apply {
            marginEnd = dp(6)
        }

        btnUncheckAll.layoutParams = LinearLayout.LayoutParams(
            0,
            dp(50),
            1f
        ).apply {
            marginStart = dp(6)
        }

        row1.addView(btnCheckAll)
        row1.addView(btnUncheckAll)

        btnCopyChecklist = makePrimaryButton("오늘 체크 요약 복사")
        btnCopyChecklist.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(52)
        ).apply {
            topMargin = dp(10)
        }

        actionCard.addView(actionTitle)
        actionCard.addView(row1)
        actionCard.addView(btnCopyChecklist)

        btnChecklistBack = makeSoftButton("뒤로가기")
        btnChecklistBack.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(52)
        ).apply {
            topMargin = dp(2)
            bottomMargin = dp(24)
        }

        rootLayout.addView(title)
        rootLayout.addView(subTitle)
        rootLayout.addView(summaryCard)
        rootLayout.addView(checklistCard)
        rootLayout.addView(actionCard)
        rootLayout.addView(btnChecklistBack)

        scrollView.addView(rootLayout)
        setContentView(scrollView)
    }

    private fun buildChecklistItems() {
        checklistContainer.removeAllViews()
        checkBoxMap.clear()

        var currentGroup = ""

        checklistItems.forEach { item ->
            if (item.group != currentGroup) {
                currentGroup = item.group

                val groupTitle = TextView(this).apply {
                    text = currentGroup
                    textSize = 15f
                    setTextColor(0xFF4E3AD8.toInt())
                    setTypeface(null, Typeface.BOLD)
                    setPadding(0, dp(14), 0, dp(6))
                }

                checklistContainer.addView(groupTitle)
            }

            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), dp(10), dp(12), dp(10))
                setBackgroundResource(R.drawable.bg_stat_box)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(8)
                }
            }

            val checkBox = CheckBox(this).apply {
                text = item.title
                textSize = 16f
                setTextColor(0xFF1F1B2E.toInt())
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(4))
            }

            val description = TextView(this).apply {
                text = item.description
                textSize = 13f
                setTextColor(0xFF6F6780.toInt())
                setPadding(dp(34), 0, 0, 0)
                setLineSpacing(2f, 1.0f)
            }

            checkBox.setOnCheckedChangeListener { _, _ ->
                saveChecklist()
                updateSummary()
            }

            itemLayout.addView(checkBox)
            itemLayout.addView(description)

            checklistContainer.addView(itemLayout)

            checkBoxMap[item.id] = checkBox
        }
    }

    private fun setupClickListeners() {
        btnCheckAll.setOnClickListener {
            setAllChecked(true)
        }

        btnUncheckAll.setOnClickListener {
            setAllChecked(false)
        }

        btnCopyChecklist.setOnClickListener {
            copyChecklistSummary()
        }

        btnChecklistBack.setOnClickListener {
            finish()
        }
    }

    private fun loadChecklist() {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedDate = prefs.getString(KEY_DATE, "") ?: ""
        val todayText = getTodayText()

        if (savedDate != todayText) {
            prefs.edit()
                .clear()
                .putString(KEY_DATE, todayText)
                .apply()

            checkBoxMap.values.forEach { checkBox ->
                checkBox.isChecked = false
            }

            updateSummary()
            return
        }

        checklistItems.forEach { item ->
            val checked = prefs.getBoolean(item.id, false)
            checkBoxMap[item.id]?.isChecked = checked
        }

        updateSummary()
    }

    private fun saveChecklist() {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val editor = prefs.edit()
            .putString(KEY_DATE, getTodayText())

        checklistItems.forEach { item ->
            val checked = checkBoxMap[item.id]?.isChecked ?: false
            editor.putBoolean(item.id, checked)
        }

        editor.apply()
    }

    private fun setAllChecked(checked: Boolean) {
        checkBoxMap.values.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = checked
        }

        checklistItems.forEach { item ->
            checkBoxMap[item.id]?.setOnCheckedChangeListener { _, _ ->
                saveChecklist()
                updateSummary()
            }
        }

        saveChecklist()
        updateSummary()

        val message = if (checked) {
            "전체 항목을 체크했습니다."
        } else {
            "전체 항목을 해제했습니다."
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateSummary() {
        val totalCount = checklistItems.size
        val checkedCount = checklistItems.count { item ->
            checkBoxMap[item.id]?.isChecked == true
        }

        val percent = if (totalCount == 0) {
            0
        } else {
            ((checkedCount.toDouble() / totalCount.toDouble()) * 100).toInt()
        }

        tvChecklistSummary.text =
            "오늘 날짜: ${getTodayText()}\n" +
                    "완료 항목: ${checkedCount}/${totalCount}개\n" +
                    "남은 항목: ${(totalCount - checkedCount).coerceAtLeast(0)}개"

        tvChecklistProgress.text = "${percent}%"

        tvChecklistProgress.setTextColor(
            when {
                percent >= 100 -> 0xFF009688.toInt()
                percent >= 70 -> 0xFF6C4DFF.toInt()
                percent >= 40 -> 0xFFFF8A00.toInt()
                else -> 0xFFFF4D6D.toInt()
            }
        )
    }

    private fun copyChecklistSummary() {
        val checkedItems = checklistItems.filter { item ->
            checkBoxMap[item.id]?.isChecked == true
        }

        val uncheckedItems = checklistItems.filter { item ->
            checkBoxMap[item.id]?.isChecked != true
        }

        val text = buildString {
            appendLine("[상품톡 오늘 검수 체크리스트]")
            appendLine()
            appendLine("날짜: ${getTodayText()}")
            appendLine("완료: ${checkedItems.size}/${checklistItems.size}개")
            appendLine()
            appendLine("완료한 항목")
            if (checkedItems.isEmpty()) {
                appendLine("- 없음")
            } else {
                checkedItems.forEach { item ->
                    appendLine("- ${item.title}")
                }
            }
            appendLine()
            appendLine("남은 항목")
            if (uncheckedItems.isEmpty()) {
                appendLine("- 없음")
            } else {
                uncheckedItems.forEach { item ->
                    appendLine("- ${item.title}")
                }
            }
        }

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("상품톡 체크리스트", text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "체크리스트 요약이 복사되었습니다.", Toast.LENGTH_SHORT).show()
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

    data class ChecklistItem(
        val id: String,
        val group: String,
        val title: String,
        val description: String
    )
}