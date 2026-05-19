package com.neonloop.sangpumtok

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.neonloop.sangpumtok.model.CatalogReview
import com.neonloop.sangpumtok.util.CaptureJudgeUtil
import com.neonloop.sangpumtok.util.ReviewStorageUtil
import com.neonloop.sangpumtok.util.RiskKeywordStorageUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CaptureReviewActivity : Activity() {

    private lateinit var btnTakePhoto: Button
    private lateinit var btnSelectImage: Button
    private lateinit var btnRunOcr: Button
    private lateinit var btnQuickAnalyze: Button
    private lateinit var btnAutoDetectType: Button
    private lateinit var btnAnalyzeCapture: Button
    private lateinit var btnCopyAnswer: Button
    private lateinit var btnSaveCaptureResult: Button
    private lateinit var btnCaptureBack: Button

    private lateinit var ivCapturePreview: ImageView
    private lateinit var rgQuestionType: RadioGroup

    private lateinit var etVisibleText: EditText
    private lateinit var etCaptureMemo: EditText

    private lateinit var tvDetectedChoices: TextView
    private lateinit var tvAnswerOnly: TextView
    private lateinit var tvCaptureResultTitle: TextView
    private lateinit var tvCaptureResultReason: TextView

    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null
    private var latestAnswerOnly: String = ""

    companion object {
        private const val REQUEST_SELECT_IMAGE = 1001
        private const val REQUEST_TAKE_PHOTO = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture_review)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnRunOcr = findViewById(R.id.btnRunOcr)
        btnQuickAnalyze = findViewById(R.id.btnQuickAnalyze)
        btnAutoDetectType = findViewById(R.id.btnAutoDetectType)
        btnAnalyzeCapture = findViewById(R.id.btnAnalyzeCapture)
        btnCopyAnswer = findViewById(R.id.btnCopyAnswer)
        btnSaveCaptureResult = findViewById(R.id.btnSaveCaptureResult)
        btnCaptureBack = findViewById(R.id.btnCaptureBack)

        ivCapturePreview = findViewById(R.id.ivCapturePreview)
        rgQuestionType = findViewById(R.id.rgQuestionType)

        etVisibleText = findViewById(R.id.etVisibleText)
        etCaptureMemo = findViewById(R.id.etCaptureMemo)

        tvDetectedChoices = findViewById(R.id.tvDetectedChoices)
        tvAnswerOnly = findViewById(R.id.tvAnswerOnly)
        tvCaptureResultTitle = findViewById(R.id.tvCaptureResultTitle)
        tvCaptureResultReason = findViewById(R.id.tvCaptureResultReason)

        rgQuestionType.check(R.id.rbCategory)
    }

    private fun setupClickListeners() {
        btnTakePhoto.setOnClickListener {
            openCamera()
        }

        btnSelectImage.setOnClickListener {
            openImagePicker()
        }

        btnRunOcr.setOnClickListener {
            runOcr(autoAnalyzeAfterOcr = false)
        }

        btnQuickAnalyze.setOnClickListener {
            runOcr(autoAnalyzeAfterOcr = true)
        }

        btnAutoDetectType.setOnClickListener {
            autoDetectQuestionType(showToast = true)
        }

        btnAnalyzeCapture.setOnClickListener {
            analyzeCapture()
        }

        btnCopyAnswer.setOnClickListener {
            copyAnswerOnly()
        }

        btnSaveCaptureResult.setOnClickListener {
            saveCaptureResult()
        }

        btnCaptureBack.setOnClickListener {
            finish()
        }
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            val photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )

            cameraImageUri = photoUri

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            startActivityForResult(intent, REQUEST_TAKE_PHOTO)

        } catch (e: Exception) {
            Toast.makeText(this, "카메라 실행 실패: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "sangpumtok_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        )
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        startActivityForResult(
            Intent.createChooser(intent, "이미지 선택"),
            REQUEST_SELECT_IMAGE
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) {
            return
        }

        when (requestCode) {
            REQUEST_SELECT_IMAGE -> {
                val uri = data?.data

                if (uri != null) {
                    applySelectedImage(uri)
                    Toast.makeText(this, "이미지가 선택되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "이미지를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            REQUEST_TAKE_PHOTO -> {
                val uri = cameraImageUri

                if (uri != null) {
                    applySelectedImage(uri)
                    Toast.makeText(this, "사진 촬영 완료! 자동 판단을 시작합니다.", Toast.LENGTH_SHORT).show()

                    ivCapturePreview.postDelayed({
                        runOcr(autoAnalyzeAfterOcr = true)
                    }, 500)

                } else {
                    Toast.makeText(this, "촬영한 사진을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun applySelectedImage(uri: Uri) {
        selectedImageUri = uri
        ivCapturePreview.setImageURI(uri)

        etVisibleText.setText("")
        tvDetectedChoices.text = "감지된 선택지: -"
        latestAnswerOnly = ""
        tvAnswerOnly.text = "답변: -"
        tvCaptureResultTitle.text = "아직 판단 전입니다."
        tvCaptureResultReason.text = "OCR로 글자를 읽거나 직접 입력한 뒤 판단 버튼을 눌러주세요."
    }

    private fun runOcr(autoAnalyzeAfterOcr: Boolean) {
        val uri = selectedImageUri

        if (uri == null) {
            Toast.makeText(this, "먼저 사진을 촬영하거나 이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        setOcrButtonsEnabled(false)

        if (autoAnalyzeAfterOcr) {
            btnQuickAnalyze.text = "읽고 판단하는 중..."
            tvCaptureResultTitle.text = "자동 판단 중입니다..."
            tvCaptureResultReason.text = "OCR로 글자를 읽고 문제 유형과 선택지를 분석하고 있어요."
        } else {
            btnRunOcr.text = "글자 읽는 중..."
            tvCaptureResultTitle.text = "OCR 처리 중입니다..."
            tvCaptureResultReason.text = "사진 속 글자를 읽고 있어요."
        }

        try {
            val image = InputImage.fromFilePath(this, uri)

            val recognizer = TextRecognition.getClient(
                KoreanTextRecognizerOptions.Builder().build()
            )

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val recognizedText = visionText.text.trim()

                    if (recognizedText.isBlank()) {
                        Toast.makeText(this, "글자는 없지만 이미지 단독 검수를 시작합니다.", Toast.LENGTH_SHORT).show()
                        etVisibleText.setText("")
                        tvDetectedChoices.text = "감지된 선택지: -"
                        runImageOnlyAnalyze()
                        return@addOnSuccessListener
                    }

                    etVisibleText.setText(recognizedText)
                    updateDetectedChoices()
                    autoDetectQuestionType(showToast = false)

                    if (autoAnalyzeAfterOcr) {
                        analyzeCapture()
                        Toast.makeText(this, "OCR + 자동 판단 완료!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "OCR 글자 읽기 완료!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    tvCaptureResultTitle.text = "OCR 실패"
                    tvCaptureResultReason.text = "OCR 처리 중 오류가 발생했습니다.\n${e.message}"
                    Toast.makeText(this, "OCR 실패: ${e.message}", Toast.LENGTH_LONG).show()
                }
                .addOnCompleteListener {
                    setOcrButtonsEnabled(true)
                    btnRunOcr.text = "OCR로 글자 읽기"
                    btnQuickAnalyze.text = "OCR 후 바로 정답 판단"
                }

        } catch (e: Exception) {
            setOcrButtonsEnabled(true)
            btnRunOcr.text = "OCR로 글자 읽기"
            btnQuickAnalyze.text = "OCR 후 바로 정답 판단"
            tvCaptureResultTitle.text = "이미지 처리 실패"
            tvCaptureResultReason.text = "이미지를 처리하지 못했습니다.\n${e.message}"
            Toast.makeText(this, "이미지 처리 실패: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun runImageOnlyAnalyze() {
        val uri = selectedImageUri

        if (uri == null) {
            Toast.makeText(this, "먼저 이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        tvCaptureResultTitle.text = "이미지 단독 검수 중입니다..."
        tvCaptureResultReason.text = "사진 속 사물과 장면을 분석하고 있어요."

        try {
            val image = InputImage.fromFilePath(this, uri)

            val options = ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.45f)
                .build()

            val labeler = ImageLabeling.getClient(options)

            labeler.process(image)
                .addOnSuccessListener { labels ->
                    if (labels.isEmpty()) {
                        latestAnswerOnly = "판단 보류"
                        tvAnswerOnly.text = "답변: 판단 보류"
                        tvCaptureResultTitle.text = "이미지 판단 보류"
                        tvCaptureResultReason.text =
                            "이미지에서 명확한 사물 라벨을 찾지 못했습니다.\n\n텍스트가 없는 이미지는 상품명, 선택지, 카테고리 정보가 부족할 수 있으므로 직접 메모를 추가한 뒤 다시 판단해주세요."
                        return@addOnSuccessListener
                    }

                    val labelText = labels.joinToString("\n") { label ->
                        "${label.text} (${(label.confidence * 100).toInt()}%)"
                    }

                    etVisibleText.setText("이미지 라벨:\n$labelText")

                    val riskKeywords = findMatchedRiskKeywords(
                        text = "$labelText\n${etCaptureMemo.text.toString().trim()}"
                    )

                    if (riskKeywords.isNotEmpty()) {
                        showRiskKeywordResult(riskKeywords)
                        Toast.makeText(this, "위험 키워드가 감지되었습니다.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val result = CaptureJudgeUtil.judgeImageOnly(
                        imageLabelText = labelText,
                        memo = etCaptureMemo.text.toString().trim()
                    )

                    latestAnswerOnly = result.answerOnly
                    tvAnswerOnly.text = "답변: ${result.answerOnly}"
                    tvCaptureResultTitle.text = result.title
                    tvCaptureResultReason.text = result.reason

                    Toast.makeText(this, "이미지 단독 검수 완료!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    latestAnswerOnly = "판단 보류"
                    tvAnswerOnly.text = "답변: 판단 보류"
                    tvCaptureResultTitle.text = "이미지 분석 실패"
                    tvCaptureResultReason.text = "이미지 분석 중 오류가 발생했습니다.\n${e.message}"
                }

        } catch (e: Exception) {
            latestAnswerOnly = "판단 보류"
            tvAnswerOnly.text = "답변: 판단 보류"
            tvCaptureResultTitle.text = "이미지 처리 실패"
            tvCaptureResultReason.text = "이미지를 처리하지 못했습니다.\n${e.message}"
        }
    }

    private fun setOcrButtonsEnabled(enabled: Boolean) {
        btnRunOcr.isEnabled = enabled
        btnQuickAnalyze.isEnabled = enabled
        btnAnalyzeCapture.isEnabled = enabled
        btnAutoDetectType.isEnabled = enabled
        btnTakePhoto.isEnabled = enabled
        btnSelectImage.isEnabled = enabled
        btnSaveCaptureResult.isEnabled = enabled
    }

    private fun autoDetectQuestionType(showToast: Boolean) {
        val visibleText = etVisibleText.text.toString().trim()
        val memo = etCaptureMemo.text.toString().trim()
        val combinedText = "$visibleText\n$memo"

        if (combinedText.isBlank()) {
            if (showToast) {
                Toast.makeText(this, "OCR 텍스트나 메모가 있어야 자동 감지할 수 있어요.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val riskKeywords = RiskKeywordStorageUtil.loadAllKeywords(this)

        val detectedTypeId = when {
            containsAny(combinedText, listOf("카테고리", "속하는", "분류", "어디에 해당", "카테고리를 선택")) -> {
                R.id.rbCategory
            }

            containsAny(combinedText, listOf("옵션", "옵션값", "구매 옵션", "값을 선택", "색상", "사이즈", "용량", "수량")) -> {
                R.id.rbOption
            }

            containsAny(combinedText, listOf("동일", "같은 상품", "같습니까", "일치", "동일 상품")) -> {
                R.id.rbSameProduct
            }

            containsAny(combinedText, riskKeywords) ||
                    containsAny(combinedText, listOf("위험", "보류", "민감", "금지", "선정", "가품", "짝퉁", "욱일기", "나치")) -> {
                R.id.rbRisk
            }

            else -> {
                R.id.rbEtc
            }
        }

        rgQuestionType.check(detectedTypeId)

        if (showToast) {
            val selectedRadioButton = findViewById<RadioButton>(detectedTypeId)
            Toast.makeText(this, "감지된 유형: ${selectedRadioButton.text}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun containsAny(
        text: String,
        keywords: List<String>
    ): Boolean {
        return keywords.any { keyword ->
            keyword.isNotBlank() && text.contains(keyword, ignoreCase = true)
        }
    }

    private fun analyzeCapture() {
        val selectedRadioId = rgQuestionType.checkedRadioButtonId

        if (selectedRadioId == -1) {
            Toast.makeText(this, "문제 유형을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRadioButton = findViewById<RadioButton>(selectedRadioId)
        val questionType = selectedRadioButton.text.toString()

        val visibleText = etVisibleText.text.toString().trim()
        val memo = etCaptureMemo.text.toString().trim()

        if (visibleText.isEmpty() && memo.isEmpty()) {
            Toast.makeText(this, "OCR로 글자를 읽거나 메모를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        updateDetectedChoices()

        val riskKeywords = findMatchedRiskKeywords(
            text = "$visibleText\n$memo"
        )

        if (riskKeywords.isNotEmpty()) {
            rgQuestionType.check(R.id.rbRisk)
            showRiskKeywordResult(riskKeywords)
            return
        }

        val result = CaptureJudgeUtil.judge(
            questionType = questionType,
            visibleText = visibleText,
            memo = memo
        )

        latestAnswerOnly = result.answerOnly

        tvAnswerOnly.text = "답변: ${result.answerOnly}"
        tvCaptureResultTitle.text = result.title
        tvCaptureResultReason.text = result.reason
    }

    private fun findMatchedRiskKeywords(text: String): List<String> {
        val allRiskKeywords = RiskKeywordStorageUtil.loadAllKeywords(this)

        return allRiskKeywords
            .filter { keyword ->
                keyword.isNotBlank() && text.contains(keyword, ignoreCase = true)
            }
            .distinct()
    }

    private fun showRiskKeywordResult(matchedKeywords: List<String>) {
        val keywordText = matchedKeywords.joinToString(", ")

        latestAnswerOnly = "판단 보류"
        tvAnswerOnly.text = "답변: 판단 보류"
        tvCaptureResultTitle.text = "위험 키워드 감지"
        tvCaptureResultReason.text =
            "다음 위험 키워드가 감지되었습니다.\n\n$keywordText\n\n해당 상품은 민감/금지/정책 위반 가능성이 있으므로 바로 승인하지 말고 가이드라인 기준으로 재확인하는 것이 좋습니다."
    }

    private fun updateDetectedChoices() {
        val visibleText = etVisibleText.text.toString().trim()
        val memo = etCaptureMemo.text.toString().trim()
        val choices = CaptureJudgeUtil.extractChoices("$visibleText\n$memo")

        tvDetectedChoices.text = if (choices.isEmpty()) {
            "감지된 선택지: -"
        } else {
            "감지된 선택지: ${choices.joinToString(" / ")}"
        }
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

    private fun saveCaptureResult() {
        val visibleText = etVisibleText.text.toString().trim()
        val memo = etCaptureMemo.text.toString().trim()

        if (latestAnswerOnly.isBlank()) {
            Toast.makeText(this, "먼저 정답 후보 판단을 해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRadioId = rgQuestionType.checkedRadioButtonId
        val questionType = if (selectedRadioId != -1) {
            findViewById<RadioButton>(selectedRadioId).text.toString()
        } else {
            "사진 검수"
        }

        val productName = extractSimpleTitle(visibleText)

        val review = CatalogReview(
            id = UUID.randomUUID().toString(),
            productName = productName,
            category = "사진 검수",
            optionName = questionType,
            description = visibleText,
            memo = memo,
            result = latestAnswerOnly,
            reason = tvCaptureResultReason.text.toString(),
            createdAt = getCurrentTimeText()
        )

        ReviewStorageUtil.saveReview(this, review)

        Toast.makeText(this, "사진 검수 결과가 저장되었습니다.", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, ReviewHistoryActivity::class.java).apply {
            putExtra(
                ReviewHistoryActivity.EXTRA_START_FILTER,
                ReviewHistoryActivity.FILTER_PHOTO
            )
        }

        startActivity(intent)
    }

    private fun extractSimpleTitle(text: String): String {
        if (text.isBlank()) return "사진 검수 결과"

        val lines = text.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val productLine = lines.firstOrNull { line ->
            line.contains("상품명") || line.contains("상품")
        }

        return productLine
            ?.replace("상품명:", "")
            ?.replace("상품명：", "")
            ?.trim()
            ?.take(40)
            ?: lines.first().take(40)
    }

    private fun getCurrentTimeText(): String {
        val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
        return formatter.format(Date())
    }
}