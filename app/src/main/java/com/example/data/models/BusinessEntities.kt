package com.example.data.models

data class HistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val date: String,
    val businessName: String,
    val nicheName: String,
    val currencyCode: String,
    val currencySymbol: String,
    val section: String,
    val revenue: Double = 0.0,
    val netProfit: Double = 0.0,
    val margin: Double = 0.0,
    val details: String = "",
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList()
)

data class TransactionItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val date: String,
    val amount: Double,
    val isIncome: Boolean,
    val category: String,
    val note: String = "",
    val businessName: String = "Кафе"
)

data class BusinessProfile(
    val id: String = "default_business",
    val name: String = "Кафе «Уют»",
    val nicheId: String = "cafe",
    val currencyCode: String = "RUB",
    val defaultTaxRate: Double = 6.0,
    val isCurrent: Boolean = true
)

data class InvoiceItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val quantity: Double,
    val unitPrice: Double,
    val discountPercent: Double = 0.0
) {
    val total: Double get() = (quantity * unitPrice) * (1.0 - discountPercent / 100.0)
}

data class Invoice(
    val id: String = "INV-" + (1001..9999).random(),
    val clientName: String,
    val clientCompany: String = "",
    val date: String,
    val dueDate: String,
    val items: List<InvoiceItem>,
    val vatRate: Double = 20.0,
    val status: String = "Выставлен"
) {
    val subtotal: Double get() = items.sumOf { it.total }
    val vatAmount: Double get() = subtotal * (vatRate / 100.0)
    val totalWithVat: Double get() = subtotal + vatAmount
}

data class CompetitorItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val price: Double,
    val strengths: String,
    val weaknesses: String,
    val rating: Float = 4.5f
)

data class GoalItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadlineWeeks: Int = 4,
    val currentStreakWeeks: Int = 3
) {
    val progress: Float get() = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f
}

data class CashFlowEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val date: String,
    val dayOffset: Int, // 0..30
    val inflow: Double,
    val outflow: Double,
    val balance: Double,
    val isGap: Boolean = false
)

data class GlossaryTerm(
    val id: String,
    val term: String,
    val shortDef: String,
    val fullDef: String,
    val formula: String = "",
    val example: String = ""
)

data class AcademyLesson(
    val id: Int,
    val title: String,
    val subtitle: String,
    val readingTimeMin: Int,
    val content: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    var isCompleted: Boolean = false
)

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val current: Int,
    val target: Int,
    val isUnlocked: Boolean = false,
    val isCrown: Boolean = false
)

data class FirstStepsChecklist(
    val nicheSelected: Boolean = true,
    val currencySelected: Boolean = true,
    val firstCalcDone: Boolean = false,
    val currencyConverted: Boolean = false,
    val invoiceCreated: Boolean = false,
    val advisorAsked: Boolean = false,
    val firstLessonDone: Boolean = false
) {
    val completedCount: Int get() = listOf(
        nicheSelected, currencySelected, firstCalcDone,
        currencyConverted, invoiceCreated, advisorAsked, firstLessonDone
    ).count { it }

    val isAllDone: Boolean get() = completedCount == 7
}
