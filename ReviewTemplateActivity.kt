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
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ReviewTemplateActivity : Activity() {

    private lateinit var tvTemplateSummary: TextView

    private lateinit var btnFilterAll: Button
    private lateinit var btnFilterCategory: Button
    private lateinit var btnFilterOption: Button
    private lateinit var btnFilterSame: Button
    private lateinit var btnFilterRisk: Button
    private lateinit var btnFilterReason: Button

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnCopyAllTemplates: Button
    private lateinit var btnTemplateBack: Button

    private lateinit var adapter: TemplateAdapter

    private val filteredTemplates = mutableListOf<TemplateItem>()

    private var currentFilter: String = FILTER_ALL

    companion object {
        private const val FILTER_ALL = "전체"
        private const val FILTER_CATEGORY = "카테고리"
        private const val FILTER_OPTION = "옵션"
        private const val FILTER_SAME = "동일상품"
        private const val FILTER_RISK = "위험"
        private const val FILTER_REASON = "판단사유"
    }

    private val templateList = listOf(
        TemplateItem(
            type = FILTER_CATEGORY,
            title = "카테고리 판단 - 일반",
            content = "해당 상품은 상품명/이미지/상세 설명 기준으로 볼 때 선택된 카테고리와 연관성이 높아 현재 카테고리로 판단 가능합니다."
        ),
        TemplateItem(
            type = FILTER_CATEGORY,
            title = "카테고리 판단 - 보류",
            content = "상품 정보만으로는 정확한 카테고리 판단이 어렵습니다. 상품명, 대표 이미지, 상세 설명 간 정보가 부족하거나 불일치하여 판단 보류가 필요합니다."
        ),
        TemplateItem(
            type = FILTER_OPTION,
            title = "옵션값 판단 - 일치",
            content = "구매 옵션과 옵션값이 상품 정보와 자연스럽게 연결되어 있어 현재 선택값으로 판단 가능합니다."
        ),
        TemplateItem(
            type = FILTER_OPTION,
            title = "옵션값 판단 - 불일치",
            content = "옵션명 또는 옵션값이 상품명/이미지/상세 설명과 일치하지 않아 수정 또는 재확인이 필요합니다."
        ),
        TemplateItem(
            type = FILTER_SAME,
            title = "동일 상품 판단 - 동일",
            content = "이미지, 상품명, 구성, 주요 옵션이 동일하거나 실질적으로 같은 상품으로 보여 동일 상품으로 판단 가능합니다."
        ),
        TemplateItem(
            type = FILTER_SAME,
            title = "동일 상품 판단 - 다름",
            content = "상품 이미지, 구성, 옵션 또는 주요 속성이 달라 동일 상품으로 보기 어렵습니다."
        ),
        TemplateItem(
            type = FILTER_RISK,
            title = "위험/민감 상품 - 판단 보류",
            content = "민감하거나 정책 위반 가능성이 있는 요소가 확인되어 바로 승인하기 어렵습니다. 가이드라인 기준 재확인이 필요합니다."
        ),
        TemplateItem(
            type = FILTER_RISK,
            title = "위험 키워드 감지",
            content = "상품 정보 내 위험/민감 키워드가 포함되어 있습니다. 해당 키워드가 실제 상품 속성과 관련 있는지 확인 후 판단 보류 또는 정책 기준 검토가 필요합니다."
        ),
        TemplateItem(
            type = FILTER_REASON,
            title = "판단 사유 - 정보 부족",
            content = "현재 제공된 정보만으로는 명확한 판단이 어렵습니다. 상품명, 이미지, 상세 설명, 옵션 정보가 추가로 필요합니다."
        ),
        TemplateItem(
            type = FILTER_REASON,
            title = "판단 사유 - 이미지 기준",
            content = "대표 이미지에서 확인되는 상품 형태와 속성을 기준으로 판단했습니다."
        ),
        TemplateItem(
            type = FILTER_REASON,
            title = "판단 사유 - 상품명 기준",
            content = "상품명에 포함된 핵심 키워드와 상품 속성을 기준으로 판단했습니다."
        ),
        TemplateItem(
            type = FILTER_REASON,
            title = "판단 사유 - 보수적 판단",
            content = "정확한 승인 여부가 애매한 경우이므로, 오류 방지를 위해 보수적으로 판단 보류하는 것이 적절합니다."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildLayout()
        setupRecyclerView()
        setupClickListeners()
        applyFilter(FILTER_ALL)
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
            text = "검수 템플릿"
            textSize = 29f
            setTextColor(0xFF1F1B2E.toInt())
            setTypeface(null, Typeface.BOLD)
        }

        val subTitle = TextView(this).apply {
            text = "자주 쓰는 판단 문장을 빠르게 복사해서 검수 답변에 활용해요."
            textSize = 14f
            setTextColor(0xFF6F6780.toInt())
            setPadding(0, dp(4), 0, dp(16))
            setLineSpacing(3f, 1.0f)
        }

        val summaryCard = makeCardLayout()

        val summaryTitle = makeSectionTitle("템플릿 요약")

        tvTemplateSummary = TextView(this).apply {
            text = "템플릿을 불러오는 중입니다."
            textSize = 14f
            setTextColor(0xFF5E566F.toInt())
            setPadding(0, dp(8), 0, 0)
            setLineSpacing(3f, 1.0f)
        }

        summaryCard.addView(summaryTitle)
        summaryCard.addView(tvTemplateSummary)

        val filterCard = makeCardLayout()

        val filterTitle = makeSectionTitle("분류 필터")

        val filterRow1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(12), 0, 0)
        }

        val filterRow2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8), 0, 0)
        }

        btnFilterAll = makeFilterButton("전체")
        btnFilterCategory = makeFilterButton("카테고리")
        btnFilterOption = makeFilterButton("옵션")
        btnFilterSame = makeFilterButton("동일")
        btnFilterRisk = makeFilterButton("위험")
        btnFilterReason = makeFilterButton("사유")

        filterRow1.addView(btnFilterAll)
        filterRow1.addView(btnFilterCategory)
        filterRow1.addView(btnFilterOption)

        filterRow2.addView(btnFilterSame)
        filterRow2.addView(btnFilterRisk)
        filterRow2.addView(btnFilterReason)

        filterCard.addView(filterTitle)
        filterCard.addView(filterRow1)
        filterCard.addView(filterRow2)

        val listCard = makeCardLayout()

        val listTitle = makeSectionTitle("템플릿 목록")

        recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(560)
            ).apply {
                topMargin = dp(12)
            }
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        listCard.addView(listTitle)
        listCard.addView(recyclerView)

        btnCopyAllTemplates = makePrimaryButton("현재 필터 템플릿 전체 복사")
        btnCopyAllTemplates.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(52)
        ).apply {
            topMargin = dp(2)
        }

        btnTemplateBack = makeSoftButton("뒤로가기")
        btnTemplateBack.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(52)
        ).apply {
            topMargin = dp(10)
            bottomMargin = dp(24)
        }

        rootLayout.addView(title)
        rootLayout.addView(subTitle)
        rootLayout.addView(summaryCard)
        rootLayout.addView(filterCard)
        rootLayout.addView(listCard)
        rootLayout.addView(btnCopyAllTemplates)
        rootLayout.addView(btnTemplateBack)

        scrollView.addView(rootLayout)
        setContentView(scrollView)
    }

    private fun setupRecyclerView() {
        adapter = TemplateAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        btnFilterAll.setOnClickListener {
            applyFilter(FILTER_ALL)
        }

        btnFilterCategory.setOnClickListener {
            applyFilter(FILTER_CATEGORY)
        }

        btnFilterOption.setOnClickListener {
            applyFilter(FILTER_OPTION)
        }

        btnFilterSame.setOnClickListener {
            applyFilter(FILTER_SAME)
        }

        btnFilterRisk.setOnClickListener {
            applyFilter(FILTER_RISK)
        }

        btnFilterReason.setOnClickListener {
            applyFilter(FILTER_REASON)
        }

        btnCopyAllTemplates.setOnClickListener {
            copyCurrentTemplates()
        }

        btnTemplateBack.setOnClickListener {
            finish()
        }
    }

    private fun applyFilter(filter: String) {
        currentFilter = filter

        filteredTemplates.clear()

        val targetList = if (filter == FILTER_ALL) {
            templateList
        } else {
            templateList.filter { item ->
                item.type == filter
            }
        }

        filteredTemplates.addAll(targetList)

        updateFilterButtons()
        updateSummary()

        adapter.notifyDataSetChanged()
    }

    private fun updateFilterButtons() {
        updateFilterButton(btnFilterAll, currentFilter == FILTER_ALL, "전체")
        updateFilterButton(btnFilterCategory, currentFilter == FILTER_CATEGORY, "카테고리")
        updateFilterButton(btnFilterOption, currentFilter == FILTER_OPTION, "옵션")
        updateFilterButton(btnFilterSame, currentFilter == FILTER_SAME, "동일")
        updateFilterButton(btnFilterRisk, currentFilter == FILTER_RISK, "위험")
        updateFilterButton(btnFilterReason, currentFilter == FILTER_REASON, "사유")
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
        val categoryCount = templateList.count { it.type == FILTER_CATEGORY }
        val optionCount = templateList.count { it.type == FILTER_OPTION }
        val sameCount = templateList.count { it.type == FILTER_SAME }
        val riskCount = templateList.count { it.type == FILTER_RISK }
        val reasonCount = templateList.count { it.type == FILTER_REASON }

        tvTemplateSummary.text =
            "현재 필터: $currentFilter ${filteredTemplates.size}개\n" +
                    "전체 ${templateList.size}개 · 카테고리 ${categoryCount}개 · 옵션 ${optionCount}개 · 동일 ${sameCount}개 · 위험 ${riskCount}개 · 사유 ${reasonCount}개"
    }

    private fun copyCurrentTemplates() {
        if (filteredTemplates.isEmpty()) {
            Toast.makeText(this, "복사할 템플릿이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val text = buildString {
            appendLine("[상품톡 검수 템플릿]")
            appendLine("분류: $currentFilter")
            appendLine()

            filteredTemplates.forEachIndexed { index, item ->
                appendLine("${index + 1}. ${item.title}")
                appendLine(item.content)
                appendLine()
            }
        }

        copyText("상품톡 검수 템플릿", text)
        Toast.makeText(this, "현재 필터 템플릿이 복사되었습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun showTemplateDetailDialog(template: TemplateItem) {
        val detailText = buildString {
            appendLine("[${template.type}]")
            appendLine(template.title)
            appendLine()
            appendLine(template.content)
        }

        AlertDialog.Builder(this)
            .setTitle(template.title)
            .setMessage(template.content)
            .setNegativeButton("닫기", null)
            .setPositiveButton("복사") { _, _ ->
                copyText("상품톡 템플릿", template.content)
                Toast.makeText(this, "템플릿 문장이 복사되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("전체 복사") { _, _ ->
                copyText("상품톡 템플릿 상세", detailText)
                Toast.makeText(this, "템플릿 상세가 복사되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun copyText(
        label: String,
        text: String
    ) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
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

    private fun makeFilterButton(textValue: String): Button {
        return Button(this).apply {
            text = textValue
            textSize = 13f
            setTextColor(0xFF4E3AD8.toInt())
            setTypeface(null, Typeface.BOLD)
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

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    data class TemplateItem(
        val type: String,
        val title: String,
        val content: String
    )

    private inner class TemplateAdapter :
        RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): TemplateViewHolder {
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

            val tvType = TextView(parent.context).apply {
                textSize = 13f
                setTextColor(0xFF4E3AD8.toInt())
                setTypeface(null, Typeface.BOLD)
            }

            val tvTitle = TextView(parent.context).apply {
                textSize = 16f
                setTextColor(0xFF1F1B2E.toInt())
                setTypeface(null, Typeface.BOLD)
                setPadding(0, dp(6), 0, dp(6))
            }

            val tvContent = TextView(parent.context).apply {
                textSize = 14f
                setTextColor(0xFF5E566F.toInt())
                setLineSpacing(3f, 1.0f)
                maxLines = 4
            }

            val tvHint = TextView(parent.context).apply {
                text = "탭하면 상세 보기 / 복사"
                textSize = 12f
                setTextColor(0xFF9C92AA.toInt())
                gravity = Gravity.END
                setPadding(0, dp(8), 0, 0)
            }

            itemLayout.addView(tvType)
            itemLayout.addView(tvTitle)
            itemLayout.addView(tvContent)
            itemLayout.addView(tvHint)

            return TemplateViewHolder(
                itemView = itemLayout,
                tvType = tvType,
                tvTitle = tvTitle,
                tvContent = tvContent
            )
        }

        override fun getItemCount(): Int {
            return filteredTemplates.size
        }

        override fun onBindViewHolder(
            holder: TemplateViewHolder,
            position: Int
        ) {
            val item = filteredTemplates[position]
            holder.bind(item)
        }

        private inner class TemplateViewHolder(
            itemView: View,
            private val tvType: TextView,
            private val tvTitle: TextView,
            private val tvContent: TextView
        ) : RecyclerView.ViewHolder(itemView) {

            fun bind(item: TemplateItem) {
                tvType.text = item.type
                tvTitle.text = item.title
                tvContent.text = item.content

                itemView.setOnClickListener {
                    showTemplateDetailDialog(item)
                }
            }
        }
    }
}