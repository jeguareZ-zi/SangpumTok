package com.neonloop.sangpumtok

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.neonloop.sangpumtok.model.CatalogReview
import com.neonloop.sangpumtok.util.CaptureJudgeUtil
import com.neonloop.sangpumtok.util.ReviewStorageUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ProductReviewActivity : Activity() {

    private lateinit var etProductName: EditText
    private lateinit var etCategory: EditText
    private lateinit var etOptionName: EditText
    private lateinit var etDescription: EditText
    private lateinit var etMemo: EditText

    private lateinit var rgQuestionType: RadioGroup
    private lateinit var rbCategory: RadioButton
    private lateinit var rbOption: RadioButton
    private lateinit var rbSameProduct: RadioButton
    private lateinit var rbRisk: RadioButton
    private lateinit var rbEtc: RadioButton

    private lateinit var tvAnswerOnly: TextView
    private lateinit var tvResultTitle: TextView
    private lateinit var tvResultReason: TextView
    private lateinit var tvDetectedChoices: TextView

    private lateinit var btnAnalyze: Button
    private lateinit var btnCopyAnswer: Button
    private lateinit var btnSaveReview: Button
    private lateinit var btnClearInput: Button
    private lateinit var btnOpenHistory: Button
    private lateinit var btnBack: Button

    private var latestAnswerOnly: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildLayout()
        setupClickListeners()
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
            text = "새 상품 검수"
            textSize = 29f
            setTextColor(0xFF1F1B2E.toInt())
            setTypeface(null, Typeface.BOLD)
        }

        val subTitle = TextView(this).apply {
            text = "상품명, 카테고리, 옵션, 설명을 입력하고 정답 후보를 빠르게 판단해요."
            textSize = 14f
            setTextColor(0xFF6F6780.toInt())
            setPadding(0, dp(4), 0, dp(16))
            setLineSpacing(3f, 1.0f)
        }

        val inputCard = makeCardLayout()
        val inputTitle = makeSectionTitle("상품 정보 입력")

        etProductName = makeInput(
            hintText = "상품명 예: 여성 니트 가디건 / 아동용 티셔츠",
            heightDp = 56,
            singleLine = true
        )

        etCategory = makeInput(
            hintText = "카테고리 예: 패션의류 > 여성의류 > 가디건",
            heightDp = 56,
            singleLine = true
        )

        etOptionName = makeInput(
            hintText = "옵션/선택지 예: 색상=블랙 / 사이즈=M / 선택지 1,2,3",
            heightDp = 56,
            singleLine = true
        )

        etDescription = makeInput(
            hintText = "상품 설명, 화면에 보이는 문구, 판단해야 할 문제 내용을 적어주세요.",
            heightDp = 150,
            singleLine = false
        )

        etMemo = makeInput(
            hintText = "추가 메모 예: 이미지에 민감 문양 있음 / 선택지는 A,B,C / 한국 정서상 민감",
            heightDp = 110,
            singleLine = false
        )

        inputCard.addView(inputTitle)
        inputCard.addView(makeSmallLabel("상품명"))
        inputCard.addView(etProductName)
        inputCard.addView(makeSmallLabel("카테고리"))
        inputCard.addView(etCategory)
        inputCard.addView(makeSmallLabel("옵션 / 선택지"))
        inputCard.addView(etOptionName)
        inputCard.addView(makeSmallLabel("상품 설명 / 문제 내용"))
        inputCard.addView(etDescription)
        inputCard.addView(makeSmallLabel("추가 메모"))
        inputCard.addView(etMemo)

        val typeCard = makeCardLayout()
        val typeTitle = makeSectionTitle("문제 유형")

        rgQuestionType = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            setPadding(0, dp(8), 0, 0)
        }

        rbCategory = makeRadioButton("카테고리 판단")
        rbOption = makeRadioButton("옵션/옵션값 판단")
        rbSameProduct = makeRadioButton("동일 상품 판단")
        rbRisk = makeRadioButton("위험/민감 상품 판단")
        rbEtc = makeRadioButton("기타 판단")

        rgQuestionType.addView(rbCategory)
        rgQuestionType.addView(rbOption)
        rgQuestionType.addView(rbSameProduct)
        rgQuestionType.addView(rbRisk)
        rgQuestionType.addView(rbEtc)
        rgQuestionType.check(rbCategory.id)

        typeCard.addView(typeTitle)
        typeCard.addView(rgQuestionType)

        val resultCard = makeCardLayout()
        val resultTitle = makeSectionTitle("판단 결과")

        tvDetectedChoices = TextView(this).apply {
            text = "감지된 선택지: -"
            textSize = 13f
            setTextColor(0xFF5E566F.toInt())
            setBackgroundResource(R.drawable.bg_stat_box)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        }

        tvAnswerOnly = TextView(this).apply {
            text = "답변: -"
            textSize = 18f
            setTextColor(0xFFD17600.toInt())
            setTypeface(null, Typeface.BOLD)
            setBackgroundResource(R.drawable.bg_answer_box)
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
        }

        tvResultTitle = TextView(this).apply {
            text = "아직 판단 전입니다."
            textSize = 16f
            setTextColor(0xFF1F1B2E.toInt())
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(12), 0, 0)
        }

        tvResultReason = TextView(this).apply {
            text = "상품 정보를 입력한 뒤 정답 후보 판단 버튼을 눌러주세요."
            textSize = 14f
            setTextColor(0xFF5E566F.toInt())
            setPadding(0, dp(8), 0, 0)
            setLineSpacing(3f, 1.0f)
        }

        btnAnalyze = makePrimaryButton("정답 후보 판단")
        btnAnalyze.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(54)
        ).apply {
            topMargin = dp(16)
        }

        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(10), 0, 0)
        }

        btnCopyAnswer = makeSoftButton("답변 복사")
        btnSaveReview = makeSoftButton("결과 저장")

        btnCopyAnswer.layoutParams = LinearLayout.LayoutParams(
            0,
            dp(50),
            1f
        ).apply {
            marginEnd = dp(6)
        }

        btnSaveReview.layoutParams = LinearLayout.LayoutParams(
            0,
            dp(50),
            1f
        ).apply {
            marginStart = dp(6)
        }

        actionRow.addView(btnCopyAnswer)
        actionRow.addView(btnSaveReview)

        resultCard.addView(resultTitle)
        resultCard.addView(tvDetectedChoices)
        resultCard.addView(tvAnswerOnly)
        resultCard.addView(tvResultTitle)
        resultCard.addView(tvResultReason)
        resultCard.addView(btnAnalyze)
        resultCard.addView(actionRow)

        val bottomCard = makeCardLayout()
        val bottomTitle = makeSectionTitle("작업 메뉴")

        btnClearInput = makeSoftButton("입력 내용 지우기")
        btnOpenHistory = makeSoftButton("저장된 기록 보기")

        btnClearInput.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(50)
        ).apply {
            topMargin = dp(12)
        }

        btnOpenHistory.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(50)
        ).apply {
            topMargin = dp(8)
        }

        bottomCard.addView(bottomTitle)
        bottomCard.addView(btnClearInput)
        bottomCard.addView(btnOpenHistory)

        btnBack = makeSoftButton("뒤로가기")
        btnBack.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(52)
        ).apply {
            topMargin = dp(2)
            bottomMargin = dp(24)
        }

        rootLayout.addView(title)
        rootLayout.addView(subTitle)
        rootLayout.addView(inputCard)
        rootLayout.addView(typeCard)
        rootLayout.addView(resultCard)
        rootLayout.addView(bottomCard)
        rootLayout.addView(btnBack)

        scrollView.addView(rootLayout)
        setContentView(scrollView)
    }

    private fun setupClickListeners() {
        btnAnalyze.setOnClickListener {
            analyzeProduct()
        }

        btnCopyAnswer.setOnClickListener {
            copyAnswerOnly()
        }

        btnSaveReview.setOnClickListener {
            saveReview()
        }

        btnClearInput.setOnClickListener {
            clearInputs()
        }

        btnOpenHistory.setOnClickListener {
            val intent = Intent(this, ReviewHistoryActivity::class.java).apply {
                putExtra(
                    ReviewHistoryActivity.EXTRA_START_FILTER,
                    ReviewHistoryActivity.FILTER_DIRECT
                )
            }
            startActivity(intent)
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun analyzeProduct() {
        val productName = etProductName.text.toString().trim()
        val category = etCategory.text.toString().trim()
        val optionName = etOptionName.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val memo = etMemo.text.toString().trim()

        if (
            productName.isBlank() &&
            category.isBlank() &&
            optionName.isBlank() &&
            description.isBlank() &&
            memo.isBlank()
        ) {
            Toast.makeText(this, "검수할 상품 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val combinedText = makeCombinedText(
            productName = productName,
            category = category,
            optionName = optionName,
            description = description,
            memo = memo
        )

        updateDetectedChoices(combinedText)
        autoDetectTypeIfNeeded(combinedText)

        if (hasRiskKeyword(combinedText)) {
            latestAnswerOnly = "판단 보류"
            tvAnswerOnly.text = "답변: 판단 보류"
            tvResultTitle.text = "위험/민감 키워드 감지"
            tvResultReason.text =
                "상품 정보 안에 위험/민감 가능성이 있는 키워드가 포함되어 있습니다.\n\n바로 승인하지 말고 정책 기준에 맞춰 재확인하는 것이 좋습니다."
            rgQuestionType.check(rbRisk.id)
            return
        }

        val questionType = getSelectedQuestionType()

        val result = CaptureJudgeUtil.judge(
            questionType = questionType,
            visibleText = combinedText,
            memo = memo
        )

        latestAnswerOnly = result.answerOnly
        tvAnswerOnly.text = "답변: ${result.answerOnly}"
        tvResultTitle.text = result.title
        tvResultReason.text = result.reason
    }

    private fun saveReview() {
        val productName = etProductName.text.toString().trim()
        val category = etCategory.text.toString().trim()
        val optionName = etOptionName.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val memo = etMemo.text.toString().trim()

        if (latestAnswerOnly.isBlank()) {
            Toast.makeText(this, "먼저 정답 후보 판단을 해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val safeProductName = if (productName.isBlank()) {
            "직접 검수 결과"
        } else {
            productName
        }

        val safeCategory = if (category.isBlank()) {
            "직접 검수"
        } else {
            category
        }

        val review = CatalogReview(
            id = UUID.randomUUID().toString(),
            productName = safeProductName,
            category = safeCategory,
            optionName = getSelectedQuestionType(),
            description = makeCombinedText(
                productName = productName,
                category = category,
                optionName = optionName,
                description = description,
                memo = memo
            ),
            memo = memo,
            result = latestAnswerOnly,
            reason = tvResultReason.text.toString(),
            createdAt = getCurrentTimeText()
        )

        ReviewStorageUtil.saveReview(this, review)

        Toast.makeText(this, "검수 결과가 저장되었습니다.", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, ReviewHistoryActivity::class.java).apply {
            putExtra(
                ReviewHistoryActivity.EXTRA_START_FILTER,
                ReviewHistoryActivity.FILTER_DIRECT
            )
        }
        startActivity(intent)
    }

    private fun copyAnswerOnly() {
        if (latestAnswerOnly.isBlank()) {
            Toast.makeText(this, "먼저 정답 후보 판단을 해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("상품톡 답변", latestAnswerOnly)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "답변이 복사되었습니다: $latestAnswerOnly", Toast.LENGTH_SHORT).show()
    }

    private fun clearInputs() {
        etProductName.setText("")
        etCategory.setText("")
        etOptionName.setText("")
        etDescription.setText("")
        etMemo.setText("")
        rgQuestionType.check(rbCategory.id)

        latestAnswerOnly = ""
        tvDetectedChoices.text = "감지된 선택지: -"
        tvAnswerOnly.text = "답변: -"
        tvResultTitle.text = "아직 판단 전입니다."
        tvResultReason.text = "상품 정보를 입력한 뒤 정답 후보 판단 버튼을 눌러주세요."

        Toast.makeText(this, "입력 내용을 지웠습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun autoDetectTypeIfNeeded(text: String) {
        val detectedId = when {
            containsAny(text, listOf("카테고리", "속하는", "분류", "어디에 해당", "카테고리를 선택")) -> {
                rbCategory.id
            }

            containsAny(text, listOf("옵션", "옵션값", "구매 옵션", "값을 선택", "색상", "사이즈", "용량", "수량")) -> {
                rbOption.id
            }

            containsAny(text, listOf("동일", "같은 상품", "같습니까", "일치", "동일 상품")) -> {
                rbSameProduct.id
            }

            containsAny(text, listOf("위험", "보류", "민감", "금지", "선정", "가품", "짝퉁", "욱일기", "나치")) -> {
                rbRisk.id
            }

            else -> {
                rgQuestionType.checkedRadioButtonId
            }
        }

        if (detectedId != -1) {
            rgQuestionType.check(detectedId)
        }
    }

    private fun updateDetectedChoices(text: String) {
        val choices = CaptureJudgeUtil.extractChoices(text)

        tvDetectedChoices.text = if (choices.isEmpty()) {
            "감지된 선택지: -"
        } else {
            "감지된 선택지: ${choices.joinToString(" / ")}"
        }
    }

    private fun makeCombinedText(
        productName: String,
        category: String,
        optionName: String,
        description: String,
        memo: String
    ): String {
        return buildString {
            if (productName.isNotBlank()) appendLine("상품명: $productName")
            if (category.isNotBlank()) appendLine("카테고리: $category")
            if (optionName.isNotBlank()) appendLine("옵션/선택지: $optionName")
            if (description.isNotBlank()) appendLine("설명: $description")
            if (memo.isNotBlank()) appendLine("메모: $memo")
        }.trim()
    }

    private fun getSelectedQuestionType(): String {
        return when (rgQuestionType.checkedRadioButtonId) {
            rbOption.id -> "옵션/옵션값 판단"
            rbSameProduct.id -> "동일 상품 판단"
            rbRisk.id -> "위험/민감 상품 판단"
            rbEtc.id -> "기타 판단"
            else -> "카테고리 판단"
        }
    }

    private fun hasRiskKeyword(text: String): Boolean {
        return containsAny(
            text = text,
            keywords = listOf(
                "위험",
                "민감",
                "금지",
                "선정",
                "성인",
                "가품",
                "짝퉁",
                "욱일기",
                "나치",
                "마약",
                "도박",
                "불법",
                "혐오",
                "폭력"
            )
        )
    }

    private fun containsAny(
        text: String,
        keywords: List<String>
    ): Boolean {
        return keywords.any { keyword ->
            keyword.isNotBlank() && text.contains(keyword, ignoreCase = true)
        }
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

    private fun makeSmallLabel(textValue: String): TextView {
        return TextView(this).apply {
            text = textValue
            textSize = 13f
            setTextColor(0xFF6F6780.toInt())
            setPadding(0, dp(12), 0, dp(6))
            setTypeface(null, Typeface.BOLD)
        }
    }

    private fun makeInput(
        hintText: String,
        heightDp: Int,
        singleLine: Boolean
    ): EditText {
        return EditText(this).apply {
            hint = hintText
            textSize = 14f
            setTextColor(0xFF1F1B2E.toInt())
            setHintTextColor(0xFFA39BAF.toInt())
            setBackgroundResource(R.drawable.bg_input_box)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            gravity = if (singleLine) {
                Gravity.CENTER_VERTICAL or Gravity.START
            } else {
                Gravity.TOP or Gravity.START
            }
            isSingleLine = singleLine
            minLines = if (singleLine) 1 else 4

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(heightDp)
            )
        }
    }

    private fun makeRadioButton(textValue: String): RadioButton {
        return RadioButton(this).apply {
            id = View.generateViewId()
            text = textValue
            textSize = 15f
            setTextColor(0xFF2E2942.toInt())
            setPadding(0, dp(4), 0, dp(4))
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

    private fun getCurrentTimeText(): String {
        val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
        return formatter.format(Date())
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}