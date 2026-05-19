package com.neonloop.sangpumtok

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.neonloop.sangpumtok.util.RiskKeywordStorageUtil

class RiskKeywordActivity : Activity() {

    private lateinit var etRiskKeywordInput: EditText

    private lateinit var btnAddKeyword: Button
    private lateinit var btnCopyRiskKeywords: Button
    private lateinit var btnClearCustomKeywords: Button
    private lateinit var btnRiskBack: Button

    private lateinit var tvDefaultCount: TextView
    private lateinit var tvCustomCount: TextView
    private lateinit var tvCustomEmpty: TextView

    private lateinit var llDefaultKeywordContainer: LinearLayout
    private lateinit var llCustomKeywordContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_risk_keyword)

        initViews()
        setupClickListeners()
        renderKeywords()
    }

    private fun initViews() {
        etRiskKeywordInput = findViewById(R.id.etRiskKeywordInput)

        btnAddKeyword = findViewById(R.id.btnAddKeyword)
        btnCopyRiskKeywords = findViewById(R.id.btnCopyRiskKeywords)
        btnClearCustomKeywords = findViewById(R.id.btnClearCustomKeywords)
        btnRiskBack = findViewById(R.id.btnRiskBack)

        tvDefaultCount = findViewById(R.id.tvDefaultCount)
        tvCustomCount = findViewById(R.id.tvCustomCount)
        tvCustomEmpty = findViewById(R.id.tvCustomEmpty)

        llDefaultKeywordContainer = findViewById(R.id.llDefaultKeywordContainer)
        llCustomKeywordContainer = findViewById(R.id.llCustomKeywordContainer)
    }

    private fun setupClickListeners() {
        btnAddKeyword.setOnClickListener {
            addKeyword()
        }

        btnCopyRiskKeywords.setOnClickListener {
            copyAllKeywords()
        }

        btnClearCustomKeywords.setOnClickListener {
            confirmClearCustomKeywords()
        }

        btnRiskBack.setOnClickListener {
            finish()
        }
    }

    private fun addKeyword() {
        val keyword = etRiskKeywordInput.text.toString().trim()

        if (keyword.isBlank()) {
            Toast.makeText(this, "추가할 키워드를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val isAdded = RiskKeywordStorageUtil.addKeyword(this, keyword)

        if (isAdded) {
            etRiskKeywordInput.setText("")
            Toast.makeText(this, "키워드가 추가되었습니다.", Toast.LENGTH_SHORT).show()
            renderKeywords()
        } else {
            Toast.makeText(this, "이미 있거나 추가할 수 없는 키워드입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderKeywords() {
        val defaultKeywords = RiskKeywordStorageUtil.getDefaultKeywords()
        val customKeywords = RiskKeywordStorageUtil.loadCustomKeywords(this)

        tvDefaultCount.text = "기본 키워드 ${defaultKeywords.size}개"
        tvCustomCount.text = "추가 키워드 ${customKeywords.size}개"

        llDefaultKeywordContainer.removeAllViews()
        llCustomKeywordContainer.removeAllViews()

        defaultKeywords.forEach { keyword ->
            llDefaultKeywordContainer.addView(
                createKeywordRow(
                    keyword = keyword,
                    isCustom = false
                )
            )
        }

        if (customKeywords.isEmpty()) {
            tvCustomEmpty.visibility = View.VISIBLE
            btnClearCustomKeywords.isEnabled = false
        } else {
            tvCustomEmpty.visibility = View.GONE
            btnClearCustomKeywords.isEnabled = true

            customKeywords.forEach { keyword ->
                llCustomKeywordContainer.addView(
                    createKeywordRow(
                        keyword = keyword,
                        isCustom = true
                    )
                )
            }
        }
    }

    private fun createKeywordRow(
        keyword: String,
        isCustom: Boolean
    ): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(14), dp(10), dp(14), dp(10))
            setBackgroundColor(Color.WHITE)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(0, 0, 0, dp(8))
            layoutParams = params
        }

        val tvKeyword = TextView(this).apply {
            text = if (isCustom) {
                "사용자 추가 · $keyword"
            } else {
                "기본 · $keyword"
            }

            textSize = 15f
            setTextColor(Color.parseColor("#111827"))
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        row.addView(tvKeyword)

        if (isCustom) {
            val btnDelete = Button(this).apply {
                text = "삭제"
                textSize = 12f
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#DC2626"))

                layoutParams = LinearLayout.LayoutParams(
                    dp(74),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                setOnClickListener {
                    confirmDeleteKeyword(keyword)
                }
            }

            row.addView(btnDelete)
        }

        return row
    }

    private fun confirmDeleteKeyword(keyword: String) {
        AlertDialog.Builder(this)
            .setTitle("키워드 삭제")
            .setMessage("'$keyword' 키워드를 삭제할까요?")
            .setNegativeButton("취소", null)
            .setPositiveButton("삭제") { _, _ ->
                RiskKeywordStorageUtil.deleteCustomKeyword(this, keyword)
                Toast.makeText(this, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                renderKeywords()
            }
            .show()
    }

    private fun confirmClearCustomKeywords() {
        AlertDialog.Builder(this)
            .setTitle("추가 키워드 전체 삭제")
            .setMessage("내가 추가한 위험 키워드를 모두 삭제할까요?\n기본 키워드는 삭제되지 않습니다.")
            .setNegativeButton("취소", null)
            .setPositiveButton("전체 삭제") { _, _ ->
                RiskKeywordStorageUtil.clearCustomKeywords(this)
                Toast.makeText(this, "추가 키워드가 모두 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                renderKeywords()
            }
            .show()
    }

    private fun copyAllKeywords() {
        val copyText = RiskKeywordStorageUtil.makeCopyText(this)

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("상품톡 위험 키워드", copyText)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "위험 키워드 목록이 복사되었습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}