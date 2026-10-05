package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppScreen(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val group: DrawerGroup
) {
    MAIN("Главная", "Основной дашборд", Icons.Default.Dashboard, DrawerGroup.MAIN),

    // Расчёты и документы
    DISCOUNT_PROMO("Скидки и акции", "Маржа и кратность продаж", Icons.Default.LocalOffer, DrawerGroup.CALCULATIONS),
    INVOICES("Счета и сметы", "Выставление клиентам и PDF", Icons.Default.ReceiptLong, DrawerGroup.CALCULATIONS),
    COMPETITORS("Конкуренты", "Таблица цен и позиционирование", Icons.Default.Leaderboard, DrawerGroup.CALCULATIONS),
    CSV_IMPORT("Импорт выписки CSV", "Автокатегории и статьи", Icons.Default.UploadFile, DrawerGroup.CALCULATIONS),
    GOALS_HABITS("Цели и привычки", "План по прибыли и серии недель", Icons.Default.TrackChanges, DrawerGroup.CALCULATIONS),
    CALCULATORS("Калькуляторы", "Наценка, runway, окупаемость", Icons.Default.Calculate, DrawerGroup.CALCULATIONS),
    CASH_FLOW("Деньги и разрывы", "Прогноз на 30 дней и кредиты", Icons.Default.AccountBalanceWallet, DrawerGroup.CALCULATIONS),
    CLIENTS_HR_STOCK("Клиенты, кадры, склад", "Удержание, выработка, остатки", Icons.Default.Group, DrawerGroup.CALCULATIONS),
    UNIT_ECON_ABC("Юнит-экономика и ABC", "LTV, CAC, матрицы ABC/XYZ", Icons.Default.PieChart, DrawerGroup.CALCULATIONS),

    // ИИ-помощники
    TEXT_GENERATOR("Генератор текстов", "Посты, акции, SMS, слоганы", Icons.Default.AutoFixHigh, DrawerGroup.AI_HELPERS),
    CONTENT_PLAN("Контент-план на 4 недели", "Рубрики и темы с призывом", Icons.Default.CalendarMonth, DrawerGroup.AI_HELPERS),
    DIALOGUE_ANALYSIS("Разговоры с клиентами", "Оценка 0–100, разбор ошибок", Icons.Default.RecordVoiceOver, DrawerGroup.AI_HELPERS),
    REVIEW_COLLECTION("Сбор отзывов", "Шаблоны просьб и трекер", Icons.Default.StarRate, DrawerGroup.AI_HELPERS),

    // Безопасность и настройки
    PIN_LOCK("Режим просмотра (PIN)", "Защита кода и гостевой режим", Icons.Default.Lock, DrawerGroup.SETTINGS),
    BUSINESSES("Бизнесы", "Управление филиалами и смена", Icons.Default.Storefront, DrawerGroup.SETTINGS),
    BACKUP("Резервная копия", "Экспорт и импорт базы в JSON", Icons.Default.CloudSync, DrawerGroup.SETTINGS),
    REMINDERS("Напоминания", "Календарь расчетов и налогов", Icons.Default.NotificationsActive, DrawerGroup.SETTINGS),
    GLOSSARY("Словарик терминов", "Простые определения и формулы", Icons.AutoMirrored.Filled.MenuBook, DrawerGroup.SETTINGS),
    THEME_SETTINGS("Оформление и сброс", "Темы, валюта, сброс данных", Icons.Default.Palette, DrawerGroup.SETTINGS),
    DEFAULT_CURRENCY("Валюта по умолчанию", "Выбор из 256+ валют мира", Icons.Default.CurrencyExchange, DrawerGroup.SETTINGS),

    // Обучение и о приложении
    HELP_TUTORIAL("Помощь и обучение", "Интерактивный тур и FAQ", Icons.Default.Help, DrawerGroup.EDUCATION),
    ACADEMY("Академия ИИ", "12 интерактивных уроков с тестами", Icons.Default.School, DrawerGroup.EDUCATION),
    ACHIEVEMENTS("Достижения", "Награды, уровни и корона", Icons.Default.WorkspacePremium, DrawerGroup.EDUCATION),
    ABOUT_APP("О приложении", "CoreMaX AI & Instagram @tojprogs", Icons.Default.Info, DrawerGroup.EDUCATION)
}

enum class DrawerGroup(val title: String) {
    MAIN("Главная"),
    CALCULATIONS("Расчёты и документы"),
    AI_HELPERS("ИИ-помощники"),
    SETTINGS("Безопасность и настройки"),
    EDUCATION("Обучение и о приложении")
}
