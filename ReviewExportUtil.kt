package com.neonloop.sangpumtok.util

import com.neonloop.sangpumtok.model.CatalogReview

object ReviewExportUtil {

    fun makeExportText(reviews: List<CatalogReview>): String {
        if (reviews.isEmpty()) {
            return "내보낼 검수 기록이 없습니다."
        }

        return buildString {
            appendLine("[상품톡 검수 내역]")
            appendLine("총 ${reviews.size}건")
            appendLine()

            reviews.forEachIndexed { index, review ->
                appendLine("===== ${index + 1}. 검수 기록 =====")
                appendLine("저장 시간: ${review.createdAt.ifBlank { "-" }}")
                appendLine("상품명: ${review.productName.ifBlank { "-" }}")
                appendLine("카테고리: ${review.category.ifBlank { "-" }}")
                appendLine("옵션/유형: ${review.optionName.ifBlank { "-" }}")
                appendLine("답변: ${review.result.ifBlank { "-" }}")
                appendLine("판단 사유:")
                appendLine(review.reason.ifBlank { "-" })
                appendLine("상품 설명:")
                appendLine(review.description.ifBlank { "-" })
                appendLine("메모:")
                appendLine(review.memo.ifBlank { "-" })
                appendLine()
            }
        }
    }
}