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
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neonloop.sangpumtok.util.ReviewMemo
import com.neonloop.sangpumtok.util.ReviewMemoStorageUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewMemoActivity : Activity() {

    private lateinit var tvMemoSummary: TextView
    private lateinit var rgMemoType: RadioGroup
    private lateinit var rbGeneralMemo: RadioButton
    private lateinit var rbPendingMemo: RadioButton
    private lateinit var rbRiskMemo: RadioButton
    private lateinit var rbTipMemo: RadioButton

    private lateinit var etMemoContent: EditText

    private lateinit var btnSaveMemo: Button
    private lateinit var btnClearMemoInput: Button
    private lateinit var btnClearAllMemos: Button
    private lateinit var btnMemoBack: Button

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyMemo: TextView

    private lateinit var adapter: MemoAdapter

    private val memoList = mutableListOf<ReviewMemo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildLayout()
        setupRecyclerView()
        setupClickListeners()
        loadMemos()
    }

    override fun onResume() {
        super.onResume()
        loadMemos()
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
            text = "검수 메모"
            textSize = 29f
            setTextColor(0xFF1F1B2E.toInt())
            setTypeface(null, Typeface.BOLD)
        }

        val subTitle = TextView(this).apply {
            text = "헷갈리는 기준, 보류 사유, 위험 키워드, 업무 팁을 빠르게 저장해요."
            textSize = 14f
            setTextColor(0xFF6F6780.toInt())
            setPadding(0, dp(4), 0, dp(16))
            setLineSpacing(3f, 1.0f)
        }

        val summaryCard = makeCardLayout()

        val summaryTitle = makeSectionTitle("메모 요약")

        tvMemoSummary = TextView(this).apply {
            text = "메모를 불러오는 중입니다."
            textSize = 14f
            setTextColor(0xFF5E566F.toInt())
            setPadding(0, dp(8), 0, 0)
            setLineSpacing(3f, 1.0f)
        }

        summaryCard.addView(summaryTitle)
        summaryCard.addView(tvMemoSummary)

        val inputCard = makeCardLayout()

        val inputTitle = makeSectionTitle("새 메모 작성")

        rgMemoType = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }

        rbGeneralMemo = makeRadioButton("일반 메모")
        rbPendingMemo = makeRadioButton("판단 보류")
        rbRiskMemo = makeRadioButton("위험 키워드")
        rbTipMemo = makeRadioButton("업무 팁")

        rgMemoType.addView(rbGeneralMemo)
        rgMemoType.addView(rbPendingMemo)
        rgMemoType.addView(rbRiskMemo)
        rgMemoType.addView(rbTipMemo)

        rgMemoType.check(rbGeneralMemo.id)

        etMemoContent = EditText(this).apply {
            hint = "예: 특정 상품은 옵션명보다 이미지 색상이 우선일 수 있음 / 욱일기 유사 패턴은 보류..."
            textSize = 14f
            setTextColor(0xFF1F1B2E.toInt())
            setHintTextColor(0xFFA39BAF.toInt())
            gravity = Gravity.TOP or Gravity.START
            minLines = 5
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundResource(R.drawable.bg_input_box)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(150)
            ).apply {
                topMargin = dp(8)
            }
        }

        btnSaveMemo = makePrimaryButton("메모 저장")
        btnClearMemoInput = makeSoftButton("입력 지우기")

        val inputButtonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(12), 0, 0)
        }

        btnSaveMemo.layoutParams = LinearLayout.LayoutParams(
            0,
            dp(52),
            1f
        ).apply {
            marginEnd = dp(6)
        }

        btnClearMemoInput.layoutParams = LinearLayout.LayoutParams(
            0,
            dp(52),
            1f
        ).apply {
            marginStart = dp(6)
        }

        inputButtonLayout.addView(btnSaveMemo)
        inputButtonLayout.addView(btnClearMemoInput)

        inputCard.addView(inputTitle)
        inputCard.addView(rgMemoType)
        inputCard.addView(etMemoContent)
        inputCard.addView(inputButtonLayout)

        val listCard = makeCardLayout()

        val listTitle = makeSectionTitle("저장된 메모")

        tvEmptyMemo = TextView(this).apply {
            text = "아직 저장된 메모가 없습니다."
            textSize = 15f
            setTextColor(0xFF777777.toInt())
            gravity = Gravity.CENTER
            visibility = View.GONE
            setPadding(0, dp(34), 0, dp(34))
            setBackgroundResource(R.drawable.bg_stat_box)
        }

        recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(520)
            ).apply {
                topMargin = dp(12)
            }
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        listCard.addView(listTitle)
        listCard.addView(tvEmptyMemo)
        listCard.addView(recyclerView)

        btnClearAllMemos = makeSoftButton("전체 메모 삭제")
        btnMemoBack = makeSoftButton("뒤로가기")

        btnClearAllMemos.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(52)
        ).apply {
            topMargin = dp(2)
        }

        btnMemoBack.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(52)
        ).apply {
            topMargin = dp(10)
            bottomMargin = dp(24)
        }

        rootLayout.addView(title)
        rootLayout.addView(subTitle)
        rootLayout.addView(summaryCard)
        rootLayout.addView(inputCard)
        rootLayout.addView(listCard)
        rootLayout.addView(btnClearAllMemos)
        rootLayout.addView(btnMemoBack)

        scrollView.addView(rootLayout)
        setContentView(scrollView)
    }

    private fun setupRecyclerView() {
        adapter = MemoAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        btnSaveMemo.setOnClickListener {
            saveMemo()
        }

        btnClearMemoInput.setOnClickListener {
            etMemoContent.setText("")
            Toast.makeText(this, "입력 내용을 지웠습니다.", Toast.LENGTH_SHORT).show()
        }

        btnClearAllMemos.setOnClickListener {
            showClearAllDialog()
        }

        btnMemoBack.setOnClickListener {
            finish()
        }
    }

    private fun saveMemo() {
        val type = getSelectedMemoType()
        val content = etMemoContent.text.toString().trim()

        if (content.isBlank()) {
            Toast.makeText(this, "저장할 메모를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        ReviewMemoStorageUtil.saveMemo(
            context = this,
            type = type,
            content = content
        )

        etMemoContent.setText("")
        rgMemoType.check(rbGeneralMemo.id)

        Toast.makeText(this, "메모가 저장되었습니다.", Toast.LENGTH_SHORT).show()

        loadMemos()
    }

    private fun getSelectedMemoType(): String {
        return when (rgMemoType.checkedRadioButtonId) {
            rbPendingMemo.id -> "판단 보류"
            rbRiskMemo.id -> "위험 키워드"
            rbTipMemo.id -> "업무 팁"
            else -> "일반 메모"
        }
    }

    private fun loadMemos() {
        memoList.clear()

        val loadedMemos = ReviewMemoStorageUtil.loadAllMemos(this)
        memoList.addAll(loadedMemos)

        updateSummary()
        updateEmptyState()

        adapter.notifyDataSetChanged()
    }

    private fun updateSummary() {
        val todayText = getTodayText()

        val totalCount = memoList.size
        val todayCount = memoList.count { memo ->
            memo.dateKey == todayText
        }
        val pendingCount = memoList.count { memo ->
            memo.type == "판단 보류"
        }
        val riskCount = memoList.count { memo ->
            memo.type == "위험 키워드"
        }
        val tipCount = memoList.count { memo ->
            memo.type == "업무 팁"
        }

        tvMemoSummary.text =
            "전체 메모 ${totalCount}개 · 오늘 작성 ${todayCount}개\n" +
                    "판단 보류 ${pendingCount}개 · 위험 키워드 ${riskCount}개 · 업무 팁 ${tipCount}개"
    }

    private fun updateEmptyState() {
        if (memoList.isEmpty()) {
            tvEmptyMemo.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmptyMemo.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun showClearAllDialog() {
        if (memoList.isEmpty()) {
            Toast.makeText(this, "삭제할 메모가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("전체 메모 삭제")
            .setMessage("저장된 모든 검수 메모를 삭제할까요?")
            .setNegativeButton("취소", null)
            .setPositiveButton("삭제") { _, _ ->
                ReviewMemoStorageUtil.clearAllMemos(this)
                Toast.makeText(this, "전체 메모가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                loadMemos()
            }
            .show()
    }

    private fun showMemoDetailDialog(memo: ReviewMemo) {
        val detailText = buildString {
            appendLine("[상품톡 검수 메모]")
            appendLine()
            appendLine("유형: ${memo.type}")
            appendLine("작성 시간: ${memo.createdAt}")
            appendLine()
            appendLine(memo.content)
        }

        AlertDialog.Builder(this)
            .setTitle("메모 상세")
            .setMessage(detailText)
            .setNegativeButton("닫기", null)
            .setNeutralButton("복사") { _, _ ->
                copyText("상품톡 검수 메모", detailText)
                Toast.makeText(this, "메모가 복사되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .setPositiveButton("삭제") { _, _ ->
                ReviewMemoStorageUtil.deleteMemo(this, memo.id)
                Toast.makeText(this, "메모가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                loadMemos()
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

    private fun getTodayText(): String {
        return SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date())
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private inner class MemoAdapter :
        RecyclerView.Adapter<MemoAdapter.MemoViewHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MemoViewHolder {
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

            val tvMemoType = TextView(parent.context).apply {
                textSize = 13f
                setTextColor(0xFF4E3AD8.toInt())
                setTypeface(null, Typeface.BOLD)
            }

            val tvMemoContent = TextView(parent.context).apply {
                textSize = 15f
                setTextColor(0xFF1F1B2E.toInt())
                setPadding(0, dp(8), 0, dp(6))
                maxLines = 4
                setLineSpacing(3f, 1.0f)
            }

            val tvMemoTime = TextView(parent.context).apply {
                textSize = 12f
                setTextColor(0xFF81768F.toInt())
            }

            val tvMemoHint = TextView(parent.context).apply {
                text = "탭하면 상세 보기 / 복사 / 삭제"
                textSize = 12f
                setTextColor(0xFF9C92AA.toInt())
                gravity = Gravity.END
                setPadding(0, dp(8), 0, 0)
            }

            itemLayout.addView(tvMemoType)
            itemLayout.addView(tvMemoContent)
            itemLayout.addView(tvMemoTime)
            itemLayout.addView(tvMemoHint)

            return MemoViewHolder(
                itemView = itemLayout,
                tvMemoType = tvMemoType,
                tvMemoContent = tvMemoContent,
                tvMemoTime = tvMemoTime
            )
        }

        override fun getItemCount(): Int {
            return memoList.size
        }

        override fun onBindViewHolder(
            holder: MemoViewHolder,
            position: Int
        ) {
            val memo = memoList[position]
            holder.bind(memo)
        }

        private inner class MemoViewHolder(
            itemView: View,
            private val tvMemoType: TextView,
            private val tvMemoContent: TextView,
            private val tvMemoTime: TextView
        ) : RecyclerView.ViewHolder(itemView) {

            fun bind(memo: ReviewMemo) {
                tvMemoType.text = memo.type
                tvMemoContent.text = memo.content
                tvMemoTime.text = memo.createdAt

                itemView.setOnClickListener {
                    showMemoDetailDialog(memo)
                }
            }
        }
    }
}