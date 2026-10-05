package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.SoundManager
import com.example.data.models.FinancialCalculator
import com.example.data.repository.BusinessRepository
import com.example.ui.components.*
import com.example.ui.navigation.AppScreen
import com.example.ui.screens.*
import com.example.ui.theme.BusinessAnalyticsTheme
import com.example.ui.tools.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SoundManager.init(this)

        setContent {
            val repository = remember { BusinessRepository.getInstance(applicationContext) }

            val themeMode by repository.themeMode.collectAsStateWithLifecycle()
            val isAmountsHidden by repository.isAmountsHidden.collectAsStateWithLifecycle()
            val currentNiche by repository.currentNiche.collectAsStateWithLifecycle()
            val defaultCurrency by repository.defaultCurrency.collectAsStateWithLifecycle()
            val financialInputs by repository.financialInputs.collectAsStateWithLifecycle()
            val historyItems by repository.historyItems.collectAsStateWithLifecycle()
            val transactions by repository.transactions.collectAsStateWithLifecycle()
            val invoices by repository.invoices.collectAsStateWithLifecycle()
            val competitors by repository.competitors.collectAsStateWithLifecycle()
            val firstSteps by repository.firstSteps.collectAsStateWithLifecycle()
            val achievements by repository.achievements.collectAsStateWithLifecycle()
            val soundEnabled by repository.soundEnabled.collectAsStateWithLifecycle()
            val vibrationEnabled by repository.vibrationEnabled.collectAsStateWithLifecycle()
            val businessName by repository.businessName.collectAsStateWithLifecycle()

            // Navigation states
            var currentScreen by remember { mutableStateOf(AppScreen.MAIN) }
            var currentTab by remember { mutableStateOf(MainTab.ANALYSIS) }
            val recentScreens = remember { mutableStateListOf<AppScreen>() }

            // Drawer & Modal states
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val coroutineScope = rememberCoroutineScope()
            var showNicheDialog by remember { mutableStateOf(false) }
            var showOnboardingTour by remember { mutableStateOf(false) }

            // Update SoundManager settings
            LaunchedEffect(soundEnabled, vibrationEnabled) {
                SoundManager.setSoundEnabled(soundEnabled)
                SoundManager.setVibrationEnabled(vibrationEnabled)
            }

            // BackHandler for secondary screens
            if (currentScreen != AppScreen.MAIN) {
                BackHandler {
                    currentScreen = AppScreen.MAIN
                }
            }

            BusinessAnalyticsTheme(themeMode = themeMode) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppDrawerContent(
                            businessName = businessName,
                            currentNiche = currentNiche,
                            defaultCurrency = defaultCurrency,
                            themeMode = themeMode,
                            isSoundEnabled = soundEnabled,
                            recentScreens = recentScreens,
                            onSelectScreen = { targetScreen ->
                                if (!recentScreens.contains(targetScreen) && targetScreen != AppScreen.MAIN) {
                                    recentScreens.add(0, targetScreen)
                                }
                                currentScreen = targetScreen
                                coroutineScope.launch { drawerState.close() }
                            },
                            onToggleTheme = {
                                val next = when (themeMode) {
                                    com.example.ui.theme.AppThemeMode.DARK -> com.example.ui.theme.AppThemeMode.LIGHT
                                    com.example.ui.theme.AppThemeMode.LIGHT -> com.example.ui.theme.AppThemeMode.MONOCHROME
                                    com.example.ui.theme.AppThemeMode.MONOCHROME -> com.example.ui.theme.AppThemeMode.SYSTEM
                                    com.example.ui.theme.AppThemeMode.SYSTEM -> com.example.ui.theme.AppThemeMode.DARK
                                }
                                repository.setThemeMode(next)
                            },
                            onToggleSound = {
                                repository.setSoundEnabled(!soundEnabled)
                            },
                            onSelectNiche = {
                                showNicheDialog = true
                                coroutineScope.launch { drawerState.close() }
                            },
                            onCloseDrawer = {
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            if (currentScreen == AppScreen.MAIN) {
                                AppHeader(
                                    currentNicheName = currentNiche.name,
                                    isAmountsHidden = isAmountsHidden,
                                    onMenuClick = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onNicheClick = { showNicheDialog = true },
                                    onEyeToggle = { repository.toggleAmountsHidden() },
                                    onHelpClick = { showOnboardingTour = true }
                                )
                            }
                        },
                        bottomBar = {
                            if (currentScreen == AppScreen.MAIN) {
                                AppBottomBar(
                                    currentTab = currentTab,
                                    onTabSelected = { currentTab = it }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AnimatedContent(
                                targetState = currentScreen,
                                label = "ScreenTransition"
                            ) { screen ->
                                when (screen) {
                                    AppScreen.MAIN -> {
                                        when (currentTab) {
                                            MainTab.ANALYSIS -> AnalysisScreen(
                                                inputs = financialInputs,
                                                niche = currentNiche,
                                                currency = defaultCurrency,
                                                isAmountsHidden = isAmountsHidden,
                                                firstSteps = firstSteps,
                                                onInputsChanged = { repository.updateFinancialInputs(it) },
                                                onSaveToHistory = { repository.addHistoryItem(it) },
                                                onStepClick = { stepTag ->
                                                    when (stepTag) {
                                                        "niche" -> showNicheDialog = true
                                                        "currencies" -> currentTab = MainTab.CURRENCIES
                                                        "advisor" -> currentTab = MainTab.ADVISOR
                                                        "invoice" -> currentScreen = AppScreen.INVOICES
                                                        "academy" -> currentScreen = AppScreen.ACADEMY
                                                    }
                                                }
                                            )

                                            MainTab.WHAT_IF -> WhatIfScreen(
                                                baseInputs = financialInputs,
                                                niche = currentNiche,
                                                currency = defaultCurrency,
                                                isAmountsHidden = isAmountsHidden,
                                                onSaveToHistory = { repository.addHistoryItem(it) }
                                            )

                                            MainTab.ADVISOR -> AdvisorScreen(
                                                niche = currentNiche,
                                                currency = defaultCurrency,
                                                inputs = financialInputs,
                                                onSaveToHistory = { repository.addHistoryItem(it) },
                                                onAdvisorAsked = { repository.markChecklistStep(advisor = true) }
                                            )

                                            MainTab.CURRENCIES -> CurrenciesScreen(
                                                defaultCurrency = defaultCurrency,
                                                onConverted = { repository.markChecklistStep(converted = true) }
                                            )

                                            MainTab.HISTORY -> HistoryScreen(
                                                historyItems = historyItems,
                                                transactions = transactions,
                                                isAmountsHidden = isAmountsHidden,
                                                onApplyItem = { item ->
                                                    repository.updateFinancialInputs(
                                                        com.example.data.models.FinancialInputs(
                                                            revenue = item.revenue,
                                                            cogs = item.revenue * (1.0 - item.margin / 100.0) * 0.5,
                                                            opex = item.revenue * 0.3,
                                                            taxRate = 6.0
                                                        )
                                                    )
                                                    currentTab = MainTab.ANALYSIS
                                                },
                                                onDeleteItem = { id -> repository.removeHistoryItem(id) },
                                                onToggleFavorite = { id -> repository.toggleHistoryFavorite(id) },
                                                onAddTransaction = { tx -> repository.addTransaction(tx) },
                                                onDeleteTransaction = { id -> repository.removeTransaction(id) }
                                            )
                                        }
                                    }

                                    AppScreen.DISCOUNT_PROMO -> DiscountPromoScreen(
                                        currency = defaultCurrency,
                                        onSaveToHistory = { repository.addHistoryItem(it) },
                                        onBack = { currentScreen = AppScreen.MAIN }
                                    )

                                    AppScreen.INVOICES -> InvoicesScreen(
                                        invoices = invoices,
                                        currency = defaultCurrency,
                                        onAddInvoice = { repository.addInvoice(it) },
                                        onBack = { currentScreen = AppScreen.MAIN }
                                    )

                                    AppScreen.COMPETITORS -> CompetitorsScreen(
                                        competitors = competitors,
                                        currency = defaultCurrency,
                                        onAddCompetitor = { repository.addCompetitor(it) },
                                        onDeleteCompetitor = { repository.removeCompetitor(it) },
                                        onBack = { currentScreen = AppScreen.MAIN }
                                    )

                                    AppScreen.DEFAULT_CURRENCY -> DefaultCurrencySelectorScreen(
                                        currentCurrency = defaultCurrency,
                                        onSelectCurrency = { curr, convert -> repository.setDefaultCurrency(curr, convert) },
                                        onBack = { currentScreen = AppScreen.MAIN }
                                    )

                                    AppScreen.ACADEMY -> AcademyScreen(
                                        onLessonCompleted = { repository.markChecklistStep(lesson = true) },
                                        onBack = { currentScreen = AppScreen.MAIN }
                                    )

                                    AppScreen.ACHIEVEMENTS -> AchievementsScreen(
                                        achievements = achievements,
                                        onBack = { currentScreen = AppScreen.MAIN }
                                    )

                                    AppScreen.GLOSSARY -> GlossaryScreen(
                                        onBack = { currentScreen = AppScreen.MAIN }
                                    )

                                    AppScreen.CALCULATORS -> CalculatorsScreen(
                                        currency = defaultCurrency,
                                        onBack = { currentScreen = AppScreen.MAIN }
                                    )

                                    AppScreen.TEXT_GENERATOR -> TextGeneratorScreen(
                                        onBack = { currentScreen = AppScreen.MAIN }
                                    )

                                    AppScreen.THEME_SETTINGS -> SettingsScreen(
                                        themeMode = themeMode,
                                        isSoundEnabled = soundEnabled,
                                        isVibrationEnabled = vibrationEnabled,
                                        onSetTheme = { repository.setThemeMode(it) },
                                        onSetSound = { repository.setSoundEnabled(it) },
                                        onSetVibration = { repository.setVibrationEnabled(it) },
                                        onExportBackup = { repository.exportBackupJson() },
                                        onResetData = { repository.resetToDefaults() },
                                        onBack = { currentScreen = AppScreen.MAIN }
                                    )

                                    else -> {
                                        // Generic screen fallback for other tools
                                        GenericToolScreen(
                                            screen = screen,
                                            onBack = { currentScreen = AppScreen.MAIN }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Modals
                if (showNicheDialog) {
                    NicheSelectorDialog(
                        currentNiche = currentNiche,
                        onNicheSelected = { selected ->
                            repository.setNiche(selected)
                        },
                        onDismiss = { showNicheDialog = false }
                    )
                }

                if (showOnboardingTour) {
                    OnboardingTourDialog(
                        onDismiss = { showOnboardingTour = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericToolScreen(
    screen: AppScreen,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screen.title, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                Icon(screen.icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = com.example.ui.theme.CyanBrand)
                Spacer(modifier = Modifier.height(12.dp))
                Text(screen.title, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(screen.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
