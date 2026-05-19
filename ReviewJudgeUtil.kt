package com.neonloop.sangpumtok.util

object ReviewJudgeUtil {

    data class JudgeResult(
        val result: String,
        val reason: String
    )

    private val highRiskKeywords = listOf(
        "욱일기",
        "나치",
        "히틀러",
        "마약",
        "대마",
        "성인용",
        "선정적",
        "음란",
        "도박",
        "무기",
        "총",
        "칼",
        "자살",
        "혐오",
        "차별",
        "가짜",
        "짝퉁",
        "정품급",
        "명품st",
        "이미테이션"
    )

    private val cautionKeywords = listOf(
        "아동",
        "유아",
        "어린이",
        "키즈",
        "수입",
        "해외",
        "브랜드",
        "정품",
        "한정판",
        "병행수입",
        "환불불가",
        "무료배송",
        "최저가"
    )

    fun judge(
        productName: String,
        category: String,
        optionName: String,
        description: String,
        memo: String
    ): JudgeResult {
        val combinedText = "$productName $category $optionName $description $memo"

        val foundHighRisk = highRiskKeywords.filter { keyword ->
            combinedText.contains(keyword, ignoreCase = true)
        }

        val foundCaution = cautionKeywords.filter { keyword ->
            combinedText.contains(keyword, ignoreCase = true)
        }

        val optionWarning = checkOptionWarning(productName, optionName)
        val categoryWarning = checkCategoryWarning(productName, category)

        return when {
            foundHighRisk.isNotEmpty() -> {
                JudgeResult(
                    result = "위험 / 보류 권장",
                    reason = "다음 위험 키워드가 발견되었습니다: ${foundHighRisk.joinToString(", ")}\n\n정책 위반, 고객 오인, 민감 이슈 가능성이 있으므로 즉시 승인보다 보류 후 추가 검토가 필요합니다."
                )
            }

            optionWarning.isNotEmpty() -> {
                JudgeResult(
                    result = "검토 필요",
                    reason = optionWarning
                )
            }

            categoryWarning.isNotEmpty() -> {
                JudgeResult(
                    result = "검토 필요",
                    reason = categoryWarning
                )
            }

            foundCaution.isNotEmpty() -> {
                JudgeResult(
                    result = "검토 필요",
                    reason = "다음 주의 키워드가 발견되었습니다: ${foundCaution.joinToString(", ")}\n\n상품 정보가 실제 상품과 일치하는지, 고객이 오해할 표현은 없는지 추가 확인이 필요합니다."
                )
            }

            else -> {
                JudgeResult(
                    result = "통과 가능",
                    reason = "현재 입력된 상품명, 카테고리, 옵션, 설명, 메모 기준으로 즉시 위험 키워드는 발견되지 않았습니다.\n\n단, 최종 검수 전 이미지와 상세페이지 내용이 실제 상품과 일치하는지 확인해주세요."
                )
            }
        }
    }

    private fun checkOptionWarning(productName: String, optionName: String): String {
        if (optionName.isBlank()) return ""

        val shoesWords = listOf("운동화", "신발", "구두", "슬리퍼", "샌들")
        val volumeWords = listOf("ml", "mL", "리터", "L")

        val isShoes = shoesWords.any { productName.contains(it, ignoreCase = true) }
        val hasVolumeOption = volumeWords.any { optionName.contains(it, ignoreCase = true) }

        if (isShoes && hasVolumeOption) {
            return "상품명은 신발류로 보이지만 옵션에 용량 단위가 포함되어 있습니다.\n\n상품 옵션값이 실제 상품 속성과 맞지 않을 가능성이 있으므로 검토가 필요합니다."
        }

        val clothesWords = listOf("티셔츠", "바지", "원피스", "자켓", "코트", "맨투맨")
        val foodUnitWords = listOf("g", "kg", "그램", "킬로", "개입")

        val isClothes = clothesWords.any { productName.contains(it, ignoreCase = true) }
        val hasFoodUnitOption = foodUnitWords.any { optionName.contains(it, ignoreCase = true) }

        if (isClothes && hasFoodUnitOption) {
            return "상품명은 의류로 보이지만 옵션에 식품/수량형 단위가 포함되어 있습니다.\n\n옵션명 오류 또는 상품 정보 불일치 가능성이 있어 검토가 필요합니다."
        }

        return ""
    }

    private fun checkCategoryWarning(productName: String, category: String): String {
        if (category.isBlank()) return ""

        val childWords = listOf("아동", "유아", "어린이", "키즈")
        val adultWords = listOf("성인", "19금", "섹시", "란제리")

        val isChildProduct = childWords.any { productName.contains(it, ignoreCase = true) }
        val hasAdultCategory = adultWords.any { category.contains(it, ignoreCase = true) }

        if (isChildProduct && hasAdultCategory) {
            return "상품명은 아동/유아 관련 상품으로 보이지만 카테고리에 성인 또는 선정적 표현이 포함되어 있습니다.\n\n카테고리 오류 또는 민감 상품 분류 문제가 있을 수 있어 보류 검토가 필요합니다."
        }

        return ""
    }
}