package com.neonloop.sangpumtok.util

object CaptureJudgeUtil {

    data class CaptureJudgeResult(
        val title: String,
        val reason: String,
        val answerOnly: String
    )

    private val highRiskKeywords = listOf(
        "욱일기",
        "나치",
        "히틀러",
        "마약",
        "대마",
        "성인용",
        "19금",
        "음란",
        "선정적",
        "혐오",
        "차별",
        "도박",
        "무기",
        "총",
        "칼",
        "짝퉁",
        "가품",
        "정품급",
        "명품st",
        "이미테이션"
    )

    private val childKeywords = listOf(
        "아동",
        "유아",
        "어린이",
        "키즈",
        "주니어",
        "베이비",
        "아기"
    )

    private val fashionKeywords = listOf(
        "티셔츠",
        "반팔",
        "원피스",
        "바지",
        "자켓",
        "맨투맨",
        "후드",
        "가디건",
        "신발",
        "운동화",
        "가방",
        "양말",
        "의류"
    )

    private val optionKeywords = listOf(
        "색상",
        "컬러",
        "사이즈",
        "용량",
        "수량",
        "옵션",
        "구성",
        "개입",
        "세트",
        "cm",
        "mm",
        "ml",
        "g",
        "kg"
    )

    fun judge(
        questionType: String,
        visibleText: String,
        memo: String
    ): CaptureJudgeResult {
        val combinedText = "$questionType\n$visibleText\n$memo"

        val foundHighRisk = highRiskKeywords.filter {
            combinedText.contains(it, ignoreCase = true)
        }

        if (foundHighRisk.isNotEmpty() && !questionType.contains("동일")) {
            return CaptureJudgeResult(
                title = "보류 / 추가 검토 권장",
                answerOnly = "보류",
                reason = "위험 키워드가 발견되었습니다: ${foundHighRisk.joinToString(", ")}\n\n민감 상징, 정책 위반, 고객 오인, 브랜드 위조 의심 가능성이 있으므로 즉시 통과보다 보류 또는 상위 검토가 안전합니다."
            )
        }

        return when {
            questionType.contains("카테고리") -> judgeCategory(visibleText, memo)
            questionType.contains("옵션") -> judgeOption(visibleText, memo)
            questionType.contains("동일") -> judgeSameProduct(visibleText, memo)
            questionType.contains("위험") -> judgeRisk(visibleText, memo)
            else -> judgeEtc()
        }
    }

    fun extractChoices(text: String): List<String> {
        val candidates = mutableListOf<String>()

        val lines = text
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        lines.forEach { line ->
            val cleaned = line
                .replace("선택지:", "")
                .replace("선택지：", "")
                .replace("보기:", "")
                .replace("보기：", "")
                .replace("옵션:", "")
                .replace("옵션：", "")
                .trim()

            val normalized = cleaned
                .replace("|", "/")
                .replace(",", "/")
                .replace("，", "/")

            if (normalized.contains("/")) {
                normalized.split("/")
                    .map { cleanChoicePrefix(it.trim()) }
                    .filter { isValidChoice(it) }
                    .forEach { candidates.add(it) }
            }

            val numberedPrefixes = listOf(
                "①", "②", "③", "④", "⑤",
                "1)", "2)", "3)", "4)", "5)",
                "1.", "2.", "3.", "4.", "5."
            )

            numberedPrefixes.forEach { prefix ->
                if (cleaned.startsWith(prefix)) {
                    val value = cleanChoicePrefix(cleaned.removePrefix(prefix).trim())
                    if (isValidChoice(value)) {
                        candidates.add(value)
                    }
                }
            }
        }

        return candidates
            .map { it.trim() }
            .distinct()
            .filter { !it.contains("상품명") }
            .filter { !it.contains("문제") }
            .filter { !it.contains("선택하세요") }
            .filter { !it.contains("선택하시오") }
    }

    private fun cleanChoicePrefix(value: String): String {
        var result = value.trim()

        val prefixes = listOf(
            "①", "②", "③", "④", "⑤",
            "1)", "2)", "3)", "4)", "5)",
            "1.", "2.", "3.", "4.", "5.",
            "-", "ㆍ", "•"
        )

        prefixes.forEach { prefix ->
            if (result.startsWith(prefix)) {
                result = result.removePrefix(prefix).trim()
            }
        }

        return result
    }

    private fun isValidChoice(value: String): Boolean {
        if (value.length < 2) return false
        if (value.length > 30) return false
        if (value.contains("해당 상품")) return false
        if (value.contains("선택")) return false
        return true
    }

    private fun chooseFromChoices(
        choices: List<String>,
        preferredKeywords: List<String>
    ): String {
        if (choices.isEmpty()) return ""

        preferredKeywords.forEach { keyword ->
            val found = choices.firstOrNull {
                it.contains(keyword, ignoreCase = true)
            }

            if (found != null) {
                return found
            }
        }

        return ""
    }

    private fun judgeCategory(
        visibleText: String,
        memo: String
    ): CaptureJudgeResult {
        val combinedText = "$visibleText\n$memo"
        val choices = extractChoices(combinedText)

        val hasChild = childKeywords.any {
            combinedText.contains(it, ignoreCase = true)
        }

        val hasFashion = fashionKeywords.any {
            combinedText.contains(it, ignoreCase = true)
        }

        if (hasChild && hasFashion) {
            val exactChoice = chooseFromChoices(
                choices,
                listOf("유아동패션", "유아동", "아동", "키즈", "베이비")
            )

            val answer = exactChoice.ifBlank { "유아동패션" }

            return CaptureJudgeResult(
                title = "추천 답변: $answer",
                answerOnly = answer,
                reason = if (exactChoice.isNotBlank()) {
                    "상품명 또는 설명에 아동/유아/키즈 관련 표현과 의류 관련 표현이 함께 보입니다.\n\n감지된 선택지 중 \"$exactChoice\"가 상품 속성과 가장 잘 맞습니다."
                } else {
                    "상품명 또는 설명에 아동/유아/키즈 관련 표현과 의류 관련 표현이 함께 보입니다.\n\n선택지에 유아동패션 또는 키즈 의류 계열이 있다면 해당 항목이 가장 적절할 가능성이 높습니다."
                }
            )
        }

        if (hasFashion) {
            val exactChoice = chooseFromChoices(
                choices,
                listOf("패션", "의류", "잡화", "신발", "가방")
            )

            val answer = exactChoice.ifBlank { "패션 / 의류 / 잡화 계열" }

            return CaptureJudgeResult(
                title = "추천 답변: $answer",
                answerOnly = answer,
                reason = if (exactChoice.isNotBlank()) {
                    "상품 정보에 의류 또는 패션잡화로 볼 수 있는 표현이 포함되어 있습니다.\n\n감지된 선택지 중 \"$exactChoice\"가 상품 속성과 가장 가깝습니다."
                } else {
                    "상품 정보에 의류 또는 패션잡화로 볼 수 있는 표현이 포함되어 있습니다.\n\n선택지 중 의류, 신발, 가방, 잡화 계열을 우선 확인하는 것이 좋습니다."
                }
            )
        }

        if (combinedText.contains("식품") ||
            combinedText.contains("과자") ||
            combinedText.contains("음료") ||
            combinedText.contains("커피") ||
            combinedText.contains("라면")
        ) {
            val exactChoice = chooseFromChoices(
                choices,
                listOf("식품", "음료", "가공식품", "신선식품")
            )

            val answer = exactChoice.ifBlank { "식품 계열" }

            return CaptureJudgeResult(
                title = "추천 답변: $answer",
                answerOnly = answer,
                reason = "상품 정보에 식품으로 볼 수 있는 단서가 있습니다.\n\n선택지 중 식품 관련 카테고리를 우선 확인하는 것이 좋습니다."
            )
        }

        if (combinedText.contains("화장품") ||
            combinedText.contains("크림") ||
            combinedText.contains("로션") ||
            combinedText.contains("샴푸") ||
            combinedText.contains("선크림")
        ) {
            val exactChoice = chooseFromChoices(
                choices,
                listOf("뷰티", "화장품", "생활용품", "헤어", "스킨케어")
            )

            val answer = exactChoice.ifBlank { "뷰티 / 생활용품 계열" }

            return CaptureJudgeResult(
                title = "추천 답변: $answer",
                answerOnly = answer,
                reason = "상품 정보에 화장품 또는 생활용품으로 볼 수 있는 표현이 있습니다.\n\n사용 부위와 상품 형태를 기준으로 뷰티 또는 생활용품 계열을 확인하는 것이 좋습니다."
            )
        }

        return CaptureJudgeResult(
            title = "판단 보류: 선택지 추가 확인 필요",
            answerOnly = "판단 보류",
            reason = "현재 입력된 텍스트만으로는 카테고리를 확정하기 어렵습니다.\n\n상품명, 이미지 속 실제 물건, 선택지의 상위/하위 카테고리를 비교한 뒤 가장 구체적인 카테고리를 선택하는 것이 좋습니다."
        )
    }

    private fun judgeOption(
        visibleText: String,
        memo: String
    ): CaptureJudgeResult {
        val combinedText = "$visibleText\n$memo"
        val foundOption = optionKeywords.filter {
            combinedText.contains(it, ignoreCase = true)
        }

        return if (foundOption.isNotEmpty()) {
            CaptureJudgeResult(
                title = "추천 답변: 상품 속성과 직접 연결된 옵션값",
                answerOnly = "상품 속성과 직접 연결된 옵션값",
                reason = "옵션 단서가 발견되었습니다: ${foundOption.joinToString(", ")}\n\n색상, 사이즈, 용량, 수량, 구성처럼 구매자가 실제로 선택하는 값이 옵션값에 해당합니다."
            )
        } else {
            CaptureJudgeResult(
                title = "검토 필요: 옵션값 근거 부족",
                answerOnly = "검토 필요",
                reason = "현재 입력된 내용에서 명확한 옵션 단서가 부족합니다.\n\n색상, 사이즈, 용량, 수량, 구성품처럼 구매자가 선택 가능한 값이 있는지 캡쳐를 다시 확인해주세요."
            )
        }
    }

    private fun judgeSameProduct(
        visibleText: String,
        memo: String
    ): CaptureJudgeResult {
        val combinedText = "$visibleText\n$memo"

        val differentSignals = listOf(
            "다름",
            "다른",
            "상이",
            "불일치",
            "용량 다름",
            "색상 다름",
            "모델명 다름",
            "구성 다름",
            "사이즈 다름",
            "브랜드 다름"
        )

        val sameSignals = listOf(
            "같음",
            "동일",
            "일치",
            "같은 상품",
            "동일 상품"
        )

        val hasDifferentSignal = differentSignals.any {
            combinedText.contains(it, ignoreCase = true)
        }

        val hasSameSignal = sameSignals.any {
            combinedText.contains(it, ignoreCase = true)
        }

        return when {
            hasDifferentSignal -> {
                CaptureJudgeResult(
                    title = "추천 답변: 동일 상품 아님",
                    answerOnly = "동일 상품 아님",
                    reason = "모델명, 색상, 용량, 구성, 사이즈, 브랜드 등이 다르다는 단서가 있습니다.\n\n동일 상품 여부 판단에서는 핵심 속성이 다르면 같은 상품으로 보기 어렵습니다."
                )
            }

            hasSameSignal -> {
                CaptureJudgeResult(
                    title = "추천 답변: 동일 상품 가능성 높음",
                    answerOnly = "동일 상품",
                    reason = "상품명, 이미지, 옵션, 구성 등이 일치한다는 단서가 있습니다.\n\n다만 최종 판단 전 모델명, 용량, 구성품, 색상 차이가 없는지 한 번 더 확인하는 것이 좋습니다."
                )
            }

            else -> {
                CaptureJudgeResult(
                    title = "판단 보류: 핵심 속성 비교 필요",
                    answerOnly = "판단 보류",
                    reason = "현재 입력된 내용만으로 동일 상품 여부를 확정하기 어렵습니다.\n\n상품명, 브랜드, 모델명, 용량, 색상, 구성품, 사이즈가 모두 같은지 비교해주세요."
                )
            }
        }
    }

    private fun judgeRisk(
        visibleText: String,
        memo: String
    ): CaptureJudgeResult {
        val combinedText = "$visibleText\n$memo"

        val cautionSignals = listOf(
            "아동",
            "유아",
            "선정",
            "민감",
            "오인",
            "브랜드",
            "해외",
            "수입",
            "환불불가",
            "정품",
            "로고"
        )

        val foundCaution = cautionSignals.filter {
            combinedText.contains(it, ignoreCase = true)
        }

        return if (foundCaution.isNotEmpty()) {
            CaptureJudgeResult(
                title = "검토 필요",
                answerOnly = "검토 필요",
                reason = "주의 단서가 발견되었습니다: ${foundCaution.joinToString(", ")}\n\n정책 위반으로 단정할 수는 없지만 고객 오인, 민감 표현, 브랜드 관련 이슈가 있을 수 있으므로 추가 확인이 필요합니다."
            )
        } else {
            CaptureJudgeResult(
                title = "통과 가능성 있음",
                answerOnly = "통과 가능",
                reason = "현재 입력된 내용에서는 명확한 고위험 키워드는 발견되지 않았습니다.\n\n단, 이미지 안의 문양, 로고, 문구, 아동 관련 요소는 텍스트만으로 놓칠 수 있으므로 캡쳐를 직접 확인한 뒤 최종 판단해주세요."
            )
        }
    }
    fun judgeImageOnly(
        imageLabelText: String,
        memo: String
    ): CaptureJudgeResult {
        val combinedText = "$imageLabelText\n$memo"

        val lowerText = combinedText.lowercase()

        val characterSignals = listOf(
            "cartoon",
            "animation",
            "fictional character",
            "toy",
            "game",
            "illustration",
            "pixel",
            "art"
        )

        val fashionSignals = listOf(
            "clothing",
            "shirt",
            "dress",
            "shoe",
            "footwear",
            "bag",
            "fashion"
        )

        val foodSignals = listOf(
            "food",
            "snack",
            "drink",
            "beverage",
            "fruit",
            "meal"
        )

        val beautySignals = listOf(
            "cosmetics",
            "cream",
            "lotion",
            "makeup",
            "skin care",
            "perfume"
        )

        val electronicsSignals = listOf(
            "electronics",
            "computer",
            "mobile phone",
            "phone",
            "camera",
            "gadget"
        )

        val petSignals = listOf(
            "dog",
            "cat",
            "pet",
            "animal"
        )

        return when {
            characterSignals.any { lowerText.contains(it) } -> {
                CaptureJudgeResult(
                    title = "추천 답변: 캐릭터 / 완구 / 취미 계열 가능성",
                    answerOnly = "캐릭터 / 완구 / 취미 계열",
                    reason = "이미지에서 캐릭터, 일러스트, 게임, 장난감 계열로 볼 수 있는 라벨이 감지되었습니다.\n\n텍스트가 없는 이미지 단독 검수에서는 상품명이나 선택지가 부족하므로, 캐릭터 굿즈/완구/취미 카테고리 후보로 보고 추가 확인하는 것이 좋습니다.\n\n감지 라벨:\n$imageLabelText"
                )
            }

            fashionSignals.any { lowerText.contains(it) } -> {
                CaptureJudgeResult(
                    title = "추천 답변: 패션 / 의류 / 잡화 계열 가능성",
                    answerOnly = "패션 / 의류 / 잡화 계열",
                    reason = "이미지에서 의류, 신발, 가방 등 패션 관련 라벨이 감지되었습니다.\n\n선택지가 있다면 의류/잡화/신발 계열을 우선 확인해주세요.\n\n감지 라벨:\n$imageLabelText"
                )
            }

            foodSignals.any { lowerText.contains(it) } -> {
                CaptureJudgeResult(
                    title = "추천 답변: 식품 계열 가능성",
                    answerOnly = "식품 계열",
                    reason = "이미지에서 음식, 음료, 간식 계열 라벨이 감지되었습니다.\n\n선택지가 있다면 식품/음료 관련 카테고리를 우선 확인하는 것이 좋습니다.\n\n감지 라벨:\n$imageLabelText"
                )
            }

            beautySignals.any { lowerText.contains(it) } -> {
                CaptureJudgeResult(
                    title = "추천 답변: 뷰티 / 화장품 계열 가능성",
                    answerOnly = "뷰티 / 화장품 계열",
                    reason = "이미지에서 화장품 또는 스킨케어 관련 라벨이 감지되었습니다.\n\n선택지가 있다면 뷰티/화장품/생활용품 계열을 우선 확인해주세요.\n\n감지 라벨:\n$imageLabelText"
                )
            }

            electronicsSignals.any { lowerText.contains(it) } -> {
                CaptureJudgeResult(
                    title = "추천 답변: 디지털 / 전자제품 계열 가능성",
                    answerOnly = "디지털 / 전자제품 계열",
                    reason = "이미지에서 전자기기 관련 라벨이 감지되었습니다.\n\n선택지가 있다면 디지털/가전/전자제품 계열을 우선 확인해주세요.\n\n감지 라벨:\n$imageLabelText"
                )
            }

            petSignals.any { lowerText.contains(it) } -> {
                CaptureJudgeResult(
                    title = "추천 답변: 반려동물 / 펫용품 계열 가능성",
                    answerOnly = "반려동물 / 펫용품 계열",
                    reason = "이미지에서 동물 또는 반려동물 관련 라벨이 감지되었습니다.\n\n선택지가 있다면 펫용품 또는 반려동물 관련 카테고리를 우선 확인해주세요.\n\n감지 라벨:\n$imageLabelText"
                )
            }

            else -> {
                CaptureJudgeResult(
                    title = "이미지 단독 판단 보류",
                    answerOnly = "판단 보류",
                    reason = "이미지에서 일부 라벨은 감지되었지만 상품 카테고리를 확정하기에는 근거가 부족합니다.\n\n텍스트가 없는 이미지는 상품명, 옵션, 선택지를 함께 확인해야 정확도가 올라갑니다.\n\n감지 라벨:\n$imageLabelText"
                )
            }
        }
    }
    private fun judgeEtc(): CaptureJudgeResult {
        return CaptureJudgeResult(
            title = "판단 보류 / 가이드라인 확인 권장",
            answerOnly = "판단 보류",
            reason = "문제 유형이 명확하지 않거나 입력된 내용만으로 정답을 확정하기 어렵습니다.\n\n이 경우 임의로 통과시키기보다 문제에서 요구하는 기준을 다시 확인하고, 애매하면 보류 또는 상위 검토로 넘기는 것이 안전합니다."
        )
    }
}