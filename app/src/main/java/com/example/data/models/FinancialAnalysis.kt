package com.example.data.models

data class FinancialInputs(
    val revenue: Double = 1500000.0,      // Выручка
    val cogs: Double = 480000.0,          // Себестоимость (переменные затраты)
    val opex: Double = 520000.0,          // Постоянные расходы
    val taxRate: Double = 6.0             // Налоговая ставка %
)

data class FinancialAnalysisResult(
    val revenue: Double,
    val cogs: Double,
    val opex: Double,
    val taxRate: Double,
    val grossProfit: Double,              // Валовая прибыль (Выручка - Себестоимость)
    val taxes: Double,                    // Налоги
    val netProfit: Double,                // Чистая прибыль (Gross - OPEX - Taxes)
    val grossMargin: Double,              // Валовая маржа %
    val netMargin: Double,                // Чистая маржа %
    val markup: Double,                   // Наценка %
    val breakEvenPoint: Double,           // Точка безубыточности в деньгах
    val breakEvenDays: Int,               // Дней в месяце до безубыточности
    val healthScore: Int,                 // Индекс здоровья бизнеса 0-100
    val healthRating: HealthRating,
    val healthBreakdown: List<HealthFactor>,
    val lossCauses: List<LossCauseDiagnosis>,
    val cogsShare: Float,                 // Доля себестоимости 0..1
    val opexShare: Float,                 // Доля опекса 0..1
    val taxShare: Float,                  // Доля налогов 0..1
    val profitShare: Float                // Доля чистой прибыли 0..1
)

enum class HealthRating(val title: String, val level: String) {
    EXCELLENT("Превосходно", "Высокая рентабельность и запас прочности"),
    GOOD("Хорошо", "Устойчивая бизнес-модель, норма прибыли"),
    WARNING("В зоне риска", "Низкая маржинальность или перегруз расходами"),
    CRITICAL("Критическое", "Убыток или кассовая угроза")
}

data class HealthFactor(
    val name: String,
    val score: Int, // 0..100
    val status: String,
    val isPositive: Boolean
)

data class LossCauseDiagnosis(
    val title: String,
    val explanation: String,
    val severity: String, // "Высокая", "Средняя", "Низкая"
    val recommendation: String
)

object FinancialCalculator {
    fun calculate(inputs: FinancialInputs, niche: Niche): FinancialAnalysisResult {
        val revenue = inputs.revenue.coerceAtLeast(0.0)
        val cogs = inputs.cogs.coerceAtLeast(0.0)
        val opex = inputs.opex.coerceAtLeast(0.0)
        val taxRate = inputs.taxRate.coerceIn(0.0, 100.0)

        val grossProfit = revenue - cogs
        val taxes = (revenue * (taxRate / 100.0)).coerceAtLeast(0.0)
        val netProfit = grossProfit - opex - taxes

        val grossMargin = if (revenue > 0) (grossProfit / revenue) * 100.0 else 0.0
        val netMargin = if (revenue > 0) (netProfit / revenue) * 100.0 else 0.0
        val markup = if (cogs > 0) ((revenue - cogs) / cogs) * 100.0 else 0.0

        val contributionMarginRatio = if (revenue > 0) grossProfit / revenue else 0.0
        val breakEvenPoint = if (contributionMarginRatio > 0) opex / contributionMarginRatio else 0.0
        val breakEvenDays = if (revenue > 0 && breakEvenPoint > 0) {
            ((breakEvenPoint / revenue) * 30).toInt().coerceIn(1, 60)
        } else {
            30
        }

        // Shares
        val totalRevenue = if (revenue > 0) revenue else 1.0
        val cogsShare = (cogs / totalRevenue).toFloat().coerceIn(0f, 1f)
        val opexShare = (opex / totalRevenue).toFloat().coerceIn(0f, 1f)
        val taxShare = (taxes / totalRevenue).toFloat().coerceIn(0f, 1f)
        val profitShare = if (netProfit > 0) (netProfit / totalRevenue).toFloat().coerceIn(0f, 1f) else 0f

        // Health Index (0-100)
        var score = 50
        // 1. Margin vs Niche Benchmark
        val marginDelta = netMargin - niche.marginNormLow
        score += when {
            netMargin >= niche.marginNormHigh -> 25
            netMargin >= niche.marginNormLow -> 15
            netMargin > 0 -> 5
            else -> -30
        }

        // 2. Break-even safety margin
        val safetyMargin = if (revenue > 0) ((revenue - breakEvenPoint) / revenue) * 100.0 else -50.0
        score += when {
            safetyMargin > 30 -> 15
            safetyMargin > 15 -> 10
            safetyMargin > 0 -> 0
            else -> -20
        }

        // 3. OPEX burden
        val opexRatio = if (revenue > 0) opex / revenue else 1.0
        score += when {
            opexRatio < 0.35 -> 10
            opexRatio < 0.50 -> 5
            else -> -10
        }

        val finalScore = score.coerceIn(5, 98)
        val rating = when {
            finalScore >= 80 -> HealthRating.EXCELLENT
            finalScore >= 60 -> HealthRating.GOOD
            finalScore >= 40 -> HealthRating.WARNING
            else -> HealthRating.CRITICAL
        }

        val factors = listOf(
            HealthFactor(
                "Рентабельность (${String.format("%.1f", netMargin)}%)",
                (netMargin.coerceIn(0.0, 40.0) * 2.5).toInt(),
                if (netMargin >= niche.marginNormLow) "В норме ниши ${niche.name}" else "Ниже нормы ${niche.marginNormLow}%",
                netMargin >= niche.marginNormLow
            ),
            HealthFactor(
                "Запас безубыточности",
                safetyMargin.coerceIn(0.0, 100.0).toInt(),
                if (safetyMargin > 20) "Высокий (+${String.format("%.0f", safetyMargin)}%)" else "Малый буфер",
                safetyMargin > 20
            ),
            HealthFactor(
                "Нагрузка расходов",
                ((1.0 - opexRatio.coerceIn(0.0, 1.0)) * 100).toInt(),
                if (opexRatio <= 0.45) "Оптимальная" else "Завышенные фикс-косты",
                opexRatio <= 0.45
            )
        )

        val lossCauses = mutableListOf<LossCauseDiagnosis>()
        if (netProfit < 0) {
            lossCauses.add(
                LossCauseDiagnosis(
                    "Бизнес в зоне операционного убытка",
                    "Текущая выручка не покрывает сумму постоянных расходов и себестоимости. Ежемесячный дефицит составляет ${String.format("%,.0f", -netProfit)}.",
                    "Высокая",
                    "Срочно сократите неприоритетные постоянные затраты и пересмотрите ценообразование."
                )
            )
        }
        if (grossMargin < 25.0 && niche.defaultCogsRatio < 0.6) {
            lossCauses.add(
                LossCauseDiagnosis(
                    "Слишком высокий фуд-кост / себестоимость",
                    "Себестоимость съедает ${String.format("%.1f", (cogs / totalRevenue) * 100)}% от чека. В нише «${niche.name}» норма около ${(niche.defaultCogsRatio * 100).toInt()}%.",
                    "Высокая",
                    "Проведите ревизию поставщиков, договоритесь об оптовой скидке или оптимизируйте списания."
                )
            )
        }
        if (opexRatio > 0.55) {
            lossCauses.add(
                LossCauseDiagnosis(
                    "Перегруз постоянными расходами (OPEX)",
                    "Аренда, оклады и коммуналка забирают ${String.format("%.1f", opexRatio * 100)}% всей выручки. Бизнес крайне уязвим к падению сезонного спроса.",
                    "Средняя",
                    "Переведите часть фиксированных окладов в процент от выручки/KPI."
                )
            )
        }
        if (lossCauses.isEmpty()) {
            lossCauses.add(
                LossCauseDiagnosis(
                    "Явных критических утечек не обнаружено",
                    "Структура финансов сбалансирована. Чистая маржа ${String.format("%.1f", netMargin)}% соответствует нормам ниши ${niche.name}.",
                    "Низкая",
                    "Сфокусируйтесь на увеличении среднего чека и повторных продаж (LTV)."
                )
            )
        }

        return FinancialAnalysisResult(
            revenue = revenue,
            cogs = cogs,
            opex = opex,
            taxRate = taxRate,
            grossProfit = grossProfit,
            taxes = taxes,
            netProfit = netProfit,
            grossMargin = grossMargin,
            netMargin = netMargin,
            markup = markup,
            breakEvenPoint = breakEvenPoint,
            breakEvenDays = breakEvenDays,
            healthScore = finalScore,
            healthRating = rating,
            healthBreakdown = factors,
            lossCauses = lossCauses,
            cogsShare = cogsShare,
            opexShare = opexShare,
            taxShare = taxShare,
            profitShare = profitShare
        )
    }
}
