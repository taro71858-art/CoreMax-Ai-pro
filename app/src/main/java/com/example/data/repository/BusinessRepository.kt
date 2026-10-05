package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.models.*
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BusinessRepository private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("coremax_biz_prefs", Context.MODE_PRIVATE)

    // Current State Flows
    private val _themeMode = MutableStateFlow(AppThemeMode.DARK)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _isAmountsHidden = MutableStateFlow(false)
    val isAmountsHidden: StateFlow<Boolean> = _isAmountsHidden.asStateFlow()

    private val _currentNiche = MutableStateFlow(NicheCatalog.defaultNiche)
    val currentNiche: StateFlow<Niche> = _currentNiche.asStateFlow()

    private val _defaultCurrency = MutableStateFlow(CurrencyCatalog.defaultCurrency)
    val defaultCurrency: StateFlow<Currency> = _defaultCurrency.asStateFlow()

    private val _financialInputs = MutableStateFlow(FinancialInputs())
    val financialInputs: StateFlow<FinancialInputs> = _financialInputs.asStateFlow()

    private val _historyItems = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyItems: StateFlow<List<HistoryItem>> = _historyItems.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionItem>>(emptyList())
    val transactions: StateFlow<List<TransactionItem>> = _transactions.asStateFlow()

    private val _invoices = MutableStateFlow<List<Invoice>>(emptyList())
    val invoices: StateFlow<List<Invoice>> = _invoices.asStateFlow()

    private val _competitors = MutableStateFlow<List<CompetitorItem>>(emptyList())
    val competitors: StateFlow<List<CompetitorItem>> = _competitors.asStateFlow()

    private val _goals = MutableStateFlow<List<GoalItem>>(emptyList())
    val goals: StateFlow<List<GoalItem>> = _goals.asStateFlow()

    private val _firstSteps = MutableStateFlow(FirstStepsChecklist())
    val firstSteps: StateFlow<FirstStepsChecklist> = _firstSteps.asStateFlow()

    private val _achievements = MutableStateFlow(StaticCatalogs.initialAchievements)
    val achievements: StateFlow<List<AchievementItem>> = _achievements.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _businessName = MutableStateFlow("Кафе «Уют»")
    val businessName: StateFlow<String> = _businessName.asStateFlow()

    private val _pinCode = MutableStateFlow<String?>(null)
    val pinCode: StateFlow<String?> = _pinCode.asStateFlow()

    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        // Theme
        val themeStr = prefs.getString("theme_mode", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
        _themeMode.value = try { AppThemeMode.valueOf(themeStr) } catch (_: Exception) { AppThemeMode.DARK }

        // Privacy
        _isAmountsHidden.value = prefs.getBoolean("hide_amounts", false)

        // Niche
        val nicheId = prefs.getString("niche_id", "cafe") ?: "cafe"
        _currentNiche.value = NicheCatalog.getNiche(nicheId)

        // Currency
        val currCode = prefs.getString("curr_code", "RUB") ?: "RUB"
        _defaultCurrency.value = CurrencyCatalog.getCurrency(currCode)

        // Business Name
        _businessName.value = prefs.getString("business_name", "Кафе «Уют»") ?: "Кафе «Уют»"

        // Sound / Vib
        _soundEnabled.value = prefs.getBoolean("sound_enabled", true)
        _vibrationEnabled.value = prefs.getBoolean("vibration_enabled", true)
        _pinCode.value = prefs.getString("pin_code", null)

        // Inputs
        val rev = prefs.getFloat("input_rev", 1500000f).toDouble()
        val cogs = prefs.getFloat("input_cogs", 480000f).toDouble()
        val opex = prefs.getFloat("input_opex", 520000f).toDouble()
        val tax = prefs.getFloat("input_tax", 6f).toDouble()
        _financialInputs.value = FinancialInputs(rev, cogs, opex, tax)

        // Seed sample history if empty
        val seedHistory = listOf(
            HistoryItem(
                title = "Базовый расчет за сентябрь",
                date = "2026-09-30",
                businessName = "Кафе «Уют»",
                nicheName = "Кафе",
                currencyCode = "RUB",
                currencySymbol = "₽",
                section = "Анализ",
                revenue = 1500000.0,
                netProfit = 410000.0,
                margin = 27.3,
                details = "Выручка: 1 500 000 ₽, Себестоимость: 480 000 ₽, OPEX: 520 000 ₽, Налог: 6%",
                isFavorite = true,
                tags = listOf("факт", "сентябрь", "кафе")
            ),
            HistoryItem(
                title = "Оптимистичный прогноз на декабрь",
                date = "2026-10-01",
                businessName = "Кафе «Уют»",
                nicheName = "Кафе",
                currencyCode = "RUB",
                currencySymbol = "₽",
                section = "Что если",
                revenue = 1950000.0,
                netProfit = 680000.0,
                margin = 34.8,
                details = "Сценарий +15% чек, +20% гостей, -5% закупка",
                isFavorite = false,
                tags = listOf("прогноз", "праздники")
            )
        )
        _historyItems.value = seedHistory

        // Seed sample transactions
        _transactions.value = listOf(
            TransactionItem(date = "04 окт", amount = 65000.0, isIncome = true, category = "Выручка зала", note = "Дневная касса"),
            TransactionItem(date = "03 окт", amount = 18500.0, isIncome = false, category = "Закупка сырья", note = "Кофе и молоко"),
            TransactionItem(date = "02 окт", amount = 72000.0, isIncome = true, category = "Выручка зала", note = "Субботний трафик"),
            TransactionItem(date = "01 окт", amount = 150000.0, isIncome = false, category = "Аренда", note = "Оплата за октябрь")
        )

        // Seed sample invoices
        _invoices.value = listOf(
            Invoice(
                id = "INV-2041",
                clientName = "ООО «ТехноПарк»",
                clientCompany = "ТехноПарк БЦ",
                date = "01.10.2026",
                dueDate = "10.10.2026",
                items = listOf(
                    InvoiceItem(name = "Кейтеринг кофе-брейка", quantity = 1.0, unitPrice = 45000.0),
                    InvoiceItem(name = "Десертные наборы (50 шт)", quantity = 50.0, unitPrice = 300.0)
                ),
                vatRate = 20.0,
                status = "Оплачен"
            )
        )

        // Seed competitors
        _competitors.value = listOf(
            CompetitorItem(name = "Кофейня «Бодрый день»", price = 320.0, strengths = "Рядом с метро, быстрый навынос", weaknesses = "Нет посадочных мест"),
            CompetitorItem(name = "Братья Кофе", price = 380.0, strengths = "Стильный интерьер, крафтовые сиропы", weaknesses = "Дорогой средний чек"),
            CompetitorItem(name = "Наша точка (Кафе)", price = 340.0, strengths = "Уютные диваны, свежая выпечка, кухня", weaknesses = "Нужен маркетинг в соцсетях")
        )

        // Seed goals
        _goals.value = listOf(
            GoalItem(title = "Чистая прибыль 500 000 ₽ / мес", targetAmount = 500000.0, currentAmount = 410000.0, deadlineWeeks = 4, currentStreakWeeks = 3),
            GoalItem(title = "Резервный фонд на 3 месяца", targetAmount = 1500000.0, currentAmount = 850000.0, deadlineWeeks = 12, currentStreakWeeks = 5)
        )
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun toggleAmountsHidden() {
        val next = !_isAmountsHidden.value
        _isAmountsHidden.value = next
        prefs.edit().putBoolean("hide_amounts", next).apply()
    }

    fun setNiche(niche: Niche) {
        _currentNiche.value = niche
        prefs.edit().putString("niche_id", niche.id).apply()
        // Automatically adapt inputs based on niche if default
        val rev = _financialInputs.value.revenue
        val cogs = rev * niche.defaultCogsRatio
        val opex = rev * niche.defaultOpexRatio
        updateFinancialInputs(FinancialInputs(revenue = rev, cogs = cogs, opex = opex, taxRate = _financialInputs.value.taxRate))
        markChecklistStep(niche = true)
    }

    fun setDefaultCurrency(currency: Currency, convertExisting: Boolean = false) {
        val old = _defaultCurrency.value
        _defaultCurrency.value = currency
        prefs.edit().putString("curr_code", currency.code).apply()

        if (convertExisting && old.code != currency.code) {
            val factor = CurrencyCatalog.convert(1.0, old.code, currency.code)
            val current = _financialInputs.value
            updateFinancialInputs(
                FinancialInputs(
                    revenue = current.revenue * factor,
                    cogs = current.cogs * factor,
                    opex = current.opex * factor,
                    taxRate = current.taxRate
                )
            )
        }
        markChecklistStep(currency = true)
    }

    fun updateFinancialInputs(inputs: FinancialInputs) {
        _financialInputs.value = inputs
        prefs.edit()
            .putFloat("input_rev", inputs.revenue.toFloat())
            .putFloat("input_cogs", inputs.cogs.toFloat())
            .putFloat("input_opex", inputs.opex.toFloat())
            .putFloat("input_tax", inputs.taxRate.toFloat())
            .apply()
        markChecklistStep(firstCalc = true)
    }

    fun setBusinessName(name: String) {
        _businessName.value = name
        prefs.edit().putString("business_name", name).apply()
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _vibrationEnabled.value = enabled
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
    }

    fun setPinCode(pin: String?) {
        _pinCode.value = pin
        prefs.edit().putString("pin_code", pin).apply()
    }

    fun setGuestMode(isGuest: Boolean) {
        _isGuestMode.value = isGuest
    }

    fun addHistoryItem(item: HistoryItem) {
        val updated = listOf(item) + _historyItems.value
        _historyItems.value = updated
    }

    fun removeHistoryItem(id: String) {
        _historyItems.value = _historyItems.value.filter { it.id != id }
    }

    fun toggleHistoryFavorite(id: String) {
        _historyItems.value = _historyItems.value.map {
            if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    fun addTransaction(tx: TransactionItem) {
        _transactions.value = listOf(tx) + _transactions.value
    }

    fun removeTransaction(id: String) {
        _transactions.value = _transactions.value.filter { it.id != id }
    }

    fun addInvoice(invoice: Invoice) {
        _invoices.value = listOf(invoice) + _invoices.value
        markChecklistStep(invoice = true)
    }

    fun addCompetitor(comp: CompetitorItem) {
        _competitors.value = _competitors.value + comp
    }

    fun removeCompetitor(id: String) {
        _competitors.value = _competitors.value.filter { it.id != id }
    }

    fun markChecklistStep(
        niche: Boolean = false,
        currency: Boolean = false,
        firstCalc: Boolean = false,
        converted: Boolean = false,
        invoice: Boolean = false,
        advisor: Boolean = false,
        lesson: Boolean = false
    ) {
        val c = _firstSteps.value
        _firstSteps.value = c.copy(
            nicheSelected = c.nicheSelected || niche,
            currencySelected = c.currencySelected || currency,
            firstCalcDone = c.firstCalcDone || firstCalc,
            currencyConverted = c.currencyConverted || converted,
            invoiceCreated = c.invoiceCreated || invoice,
            advisorAsked = c.advisorAsked || advisor,
            firstLessonDone = c.firstLessonDone || lesson
        )

        // Check if crown unlocked
        if (_firstSteps.value.isAllDone) {
            _achievements.value = _achievements.value.map {
                if (it.isCrown) it.copy(isUnlocked = true, current = 7) else it
            }
        }
    }

    fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("app", "CoreMaX AI Business Analytics")
        root.put("version", "1.0")
        root.put("date", SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()))
        root.put("businessName", _businessName.value)
        root.put("nicheId", _currentNiche.value.id)
        root.put("currencyCode", _defaultCurrency.value.code)

        val inputsObj = JSONObject()
        val inp = _financialInputs.value
        inputsObj.put("revenue", inp.revenue)
        inputsObj.put("cogs", inp.cogs)
        inputsObj.put("opex", inp.opex)
        inputsObj.put("taxRate", inp.taxRate)
        root.put("inputs", inputsObj)

        val histArr = JSONArray()
        for (item in _historyItems.value) {
            val obj = JSONObject()
            obj.put("title", item.title)
            obj.put("date", item.date)
            obj.put("section", item.section)
            obj.put("revenue", item.revenue)
            obj.put("netProfit", item.netProfit)
            obj.put("margin", item.margin)
            histArr.put(obj)
        }
        root.put("history", histArr)

        return root.toString(2)
    }

    fun restoreBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (root.has("businessName")) {
                setBusinessName(root.getString("businessName"))
            }
            if (root.has("nicheId")) {
                val n = NicheCatalog.getNiche(root.getString("nicheId"))
                setNiche(n)
            }
            if (root.has("currencyCode")) {
                val c = CurrencyCatalog.getCurrency(root.getString("currencyCode"))
                setDefaultCurrency(c)
            }
            if (root.has("inputs")) {
                val inp = root.getJSONObject("inputs")
                updateFinancialInputs(
                    FinancialInputs(
                        revenue = inp.getDouble("revenue"),
                        cogs = inp.getDouble("cogs"),
                        opex = inp.getDouble("opex"),
                        taxRate = inp.getDouble("taxRate")
                    )
                )
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        loadData()
    }

    companion object {
        @Volatile
        private var INSTANCE: BusinessRepository? = null

        fun getInstance(context: Context): BusinessRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BusinessRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
