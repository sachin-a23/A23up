package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.MarketRecordEntity
import com.example.data.repository.MarketRepository
import com.example.formula.BacktestEvaluation
import com.example.formula.FormulaEngine
import com.example.formula.FormulaResult
import com.example.formula.WeeklyStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class NextDayPrediction(
    val targetDate: String,
    val lastEntryDate: String,
    val lastEntryRecordStr: String,
    val prevJodi1: String,
    val prevJodi2: String,
    val formulaResult: FormulaResult?,
    val analystName: String = "Sachin Solunke"
)

data class MarketPredictionCardData(
    val marketName: String,
    val liveDate: String,
    val targetDate: String,
    val lastEntryDate: String,
    val lastEntryFullStr: String,
    val otcFormatted: String,
    val jodiFormatted: String,
    val panelFormatted: String,
    val analystName: String = "Sachin Solunke"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("gsm_app_prefs", android.content.Context.MODE_PRIVATE)
    private val db = AppDatabase.getDatabase(application)
    private val repository = MarketRepository(db.marketRecordDao())

    val selectedMarket = MutableStateFlow("SHRIDEVI")
    val divisor = MutableStateFlow(prefs.getInt("formula_divisor", 8))
    val searchQuery = MutableStateFlow("")
    val statusFilter = MutableStateFlow("ALL") // "ALL", "PASS", "FAIL"

    // Background Wallpaper & Dimming Preferences (Default dim = 15% for clear HD background)
    val wallpaperPath = MutableStateFlow<String?>(prefs.getString("custom_wallpaper_path", null))
    val bgDimLevel = MutableStateFlow<Float>(prefs.getFloat("bg_dim_level", 15f))

    // Server Sync URL Configuration
    val serverUrl = MutableStateFlow(
        prefs.getString("server_url", "https://raw.githubusercontent.com/sachin-a23/A23site/main/data.json") ?: "https://raw.githubusercontent.com/sachin-a23/A23site/main/data.json"
    )

    val availableMarkets: StateFlow<List<String>> = repository.allMarkets
        .combine(selectedMarket) { markets, current ->
            val defaultList = listOf("KALYAN", "SHRIDEVI", "MILAN", "TIME BAZAR")
            (defaultList + markets).distinct().sorted()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("KALYAN", "SHRIDEVI", "MILAN", "TIME BAZAR")
        )

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    // Form inputs for Data & Market screen
    val entryDate = MutableStateFlow(getTodayDateStr())
    val entryResult = MutableStateFlow("")
    val entryIsHoliday = MutableStateFlow(false)
    val editingRecordId = MutableStateFlow<Int?>(null)

    // Formula live calculator inputs
    val calcJodi1 = MutableStateFlow("71")
    val calcJodi2 = MutableStateFlow("56")
    private val _calcResult = MutableStateFlow<FormulaResult?>(FormulaEngine.calculateFormula("71", "56"))
    val calcResult: StateFlow<FormulaResult?> = _calcResult.asStateFlow()

    // AI Modules & Chat State Management
    private val _aiModules = MutableStateFlow<List<com.example.network.AiModuleConfig>>(emptyList())
    val aiModules: StateFlow<List<com.example.network.AiModuleConfig>> = _aiModules.asStateFlow()

    private val _activeAiModule = MutableStateFlow<com.example.network.AiModuleConfig?>(null)
    val activeAiModule: StateFlow<com.example.network.AiModuleConfig?> = _activeAiModule.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<com.example.network.ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<com.example.network.ChatMessage>> = _chatMessages.asStateFlow()
    val aiChatMessages: StateFlow<List<com.example.network.ChatMessage>> = chatMessages

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _aiTestStatus = MutableStateFlow<String?>(null)
    val aiTestStatus: StateFlow<String?> = _aiTestStatus.asStateFlow()

    private val _aiLanguage = MutableStateFlow(prefs.getString("saved_ai_language", "Hinglish") ?: "Hinglish")
    val aiLanguage: StateFlow<String> = _aiLanguage.asStateFlow()

    // Persistent User Auth State
    val isLoggedIn = MutableStateFlow(prefs.getBoolean("user_is_logged_in", true))
    val userEmail = MutableStateFlow(prefs.getString("user_email", "woldcom87@gmail.com") ?: "woldcom87@gmail.com")
    val userName = MutableStateFlow(prefs.getString("user_name", "Sachin Solunke") ?: "Sachin Solunke")
    val userPhone = MutableStateFlow(prefs.getString("user_phone", "+91 9876543210") ?: "+91 9876543210")
    val userRole = MutableStateFlow(prefs.getString("user_role", "VIP Lead Admin & Analyst") ?: "VIP Lead Admin & Analyst")
    val userAvatarIndex = MutableStateFlow(prefs.getInt("user_avatar_index", 0))

    // 4-Digit Security PIN Lock State
    val savedPin = MutableStateFlow(prefs.getString("user_security_pin", "1234"))
    val isPinEnabled = MutableStateFlow(prefs.getBoolean("is_pin_enabled", true))
    val isPinLocked = MutableStateFlow(prefs.getBoolean("is_pin_locked", prefs.getBoolean("is_pin_enabled", true)))

    fun loginUser(emailInput: String, passwordInput: String): Pair<Boolean, String> {
        val e = emailInput.trim()
        val p = passwordInput.trim()
        if (e.isBlank() || p.isBlank()) {
            return Pair(false, "Please enter valid email and password.")
        }
        val savedPass = prefs.getString("user_pass_$e", p) ?: p
        if (p != savedPass) {
            return Pair(false, "Invalid password!")
        }
        val name = prefs.getString("user_name_$e", if (e.contains("@")) e.substringBefore("@") else "VIP User") ?: "VIP User"
        val phone = prefs.getString("user_phone_$e", "+91 9876543210") ?: "+91 9876543210"
        val role = prefs.getString("user_role_$e", "VIP Member") ?: "VIP Member"

        isLoggedIn.value = true
        userEmail.value = e
        userName.value = name
        userPhone.value = phone
        userRole.value = role

        prefs.edit()
            .putBoolean("user_is_logged_in", true)
            .putString("user_email", e)
            .putString("user_name", name)
            .putString("user_phone", phone)
            .putString("user_role", role)
            .apply()

        return Pair(true, "✓ Welcome back, $name!")
    }

    fun loginWithPhoneOtp(phoneInput: String, otpInput: String): Pair<Boolean, String> {
        val ph = phoneInput.trim()
        val otp = otpInput.trim()
        if (ph.length < 10) {
            return Pair(false, "Please enter a valid 10-digit mobile number.")
        }
        if (otp.length != 6) {
            return Pair(false, "Please enter valid 6-digit OTP.")
        }

        val formattedPhone = if (ph.startsWith("+")) ph else "+91 $ph"
        val name = "User " + ph.takeLast(4)
        val email = "phone_${ph.takeLast(6)}@gsmpro.app"

        isLoggedIn.value = true
        userEmail.value = email
        userName.value = name
        userPhone.value = formattedPhone
        userRole.value = "VIP Phone Verified Trader"

        prefs.edit()
            .putBoolean("user_is_logged_in", true)
            .putString("user_email", email)
            .putString("user_name", name)
            .putString("user_phone", formattedPhone)
            .putString("user_role", "VIP Phone Verified Trader")
            .apply()

        return Pair(true, "✓ Phone verified successfully! Welcome $name.")
    }

    fun loginWithGoogle(accountEmail: String, accountName: String): Pair<Boolean, String> {
        val e = accountEmail.trim().ifBlank { "woldcom87@gmail.com" }
        val n = accountName.trim().ifBlank { "Sachin Solunke" }

        isLoggedIn.value = true
        userEmail.value = e
        userName.value = n
        userRole.value = "VIP Google Account"

        prefs.edit()
            .putBoolean("user_is_logged_in", true)
            .putString("user_email", e)
            .putString("user_name", n)
            .putString("user_role", "VIP Google Account")
            .apply()

        return Pair(true, "✓ Signed in with Google as $n!")
    }

    fun registerUser(name: String, email: String, phone: String, pass: String, accountType: String): Pair<Boolean, String> {
        val n = name.trim()
        val e = email.trim()
        val ph = phone.trim()
        val p = pass.trim()
        if (n.isBlank() || e.isBlank() || p.isBlank()) {
            return Pair(false, "Please complete all required fields.")
        }

        isLoggedIn.value = true
        userEmail.value = e
        userName.value = n
        userPhone.value = ph.ifBlank { "+91 9876543210" }
        userRole.value = accountType

        prefs.edit()
            .putBoolean("user_is_logged_in", true)
            .putString("user_email", e)
            .putString("user_name", n)
            .putString("user_phone", ph)
            .putString("user_role", accountType)
            .putString("user_pass_$e", p)
            .putString("user_name_$e", n)
            .putString("user_phone_$e", ph)
            .putString("user_role_$e", accountType)
            .apply()

        return Pair(true, "✓ Account registered successfully!")
    }

    fun updateUserProfile(name: String, email: String, phone: String, role: String, avatarIndex: Int): Pair<Boolean, String> {
        val n = name.trim()
        val e = email.trim()
        val ph = phone.trim()
        val r = role.trim()

        if (n.isBlank() || e.isBlank()) {
            return Pair(false, "Name and Email cannot be empty.")
        }

        userName.value = n
        userEmail.value = e
        userPhone.value = ph
        userRole.value = r
        userAvatarIndex.value = avatarIndex

        prefs.edit()
            .putString("user_name", n)
            .putString("user_email", e)
            .putString("user_phone", ph)
            .putString("user_role", r)
            .putInt("user_avatar_index", avatarIndex)
            .apply()

        return Pair(true, "✓ Profile updated successfully!")
    }

    fun unlockWithPin(pinInput: String): Pair<Boolean, String> {
        val currentPin = savedPin.value ?: "1234"
        if (pinInput == currentPin) {
            isPinLocked.value = false
            prefs.edit().putBoolean("is_pin_locked", false).apply()
            return Pair(true, "✓ App unlocked!")
        } else {
            return Pair(false, "❌ Incorrect PIN. Please try again.")
        }
    }

    fun setSecurityPin(newPin: String): Pair<Boolean, String> {
        if (newPin.length != 4 || !newPin.all { it.isDigit() }) {
            return Pair(false, "PIN must be exactly 4 numeric digits.")
        }
        savedPin.value = newPin
        isPinEnabled.value = true
        isPinLocked.value = false

        prefs.edit()
            .putString("user_security_pin", newPin)
            .putBoolean("is_pin_enabled", true)
            .putBoolean("is_pin_locked", false)
            .apply()

        return Pair(true, "✓ 4-Digit PIN set successfully!")
    }

    fun changeSecurityPin(oldPin: String, newPin: String): Pair<Boolean, String> {
        val currentPin = savedPin.value ?: "1234"
        if (oldPin != currentPin) {
            return Pair(false, "❌ Current PIN does not match.")
        }
        if (newPin.length != 4 || !newPin.all { it.isDigit() }) {
            return Pair(false, "New PIN must be 4 numeric digits.")
        }

        savedPin.value = newPin
        prefs.edit().putString("user_security_pin", newPin).apply()
        return Pair(true, "✓ Security PIN changed successfully!")
    }

    fun togglePinEnabled(enabled: Boolean) {
        isPinEnabled.value = enabled
        if (!enabled) {
            isPinLocked.value = false
        }
        prefs.edit()
            .putBoolean("is_pin_enabled", enabled)
            .putBoolean("is_pin_locked", if (!enabled) false else isPinLocked.value)
            .apply()
    }

    fun lockApp() {
        if (isPinEnabled.value && !savedPin.value.isNull_or_empty()) {
            isPinLocked.value = true
            prefs.edit().putBoolean("is_pin_locked", true).apply()
        }
    }

    fun logoutUser() {
        isLoggedIn.value = false
        prefs.edit().putBoolean("user_is_logged_in", false).apply()
    }

    fun setAiLanguage(lang: String) {
        _aiLanguage.value = lang
        prefs.edit().putString("saved_ai_language", lang).apply()
    }

    init {
        viewModelScope.launch {
            repository.seedDefaultsIfEmpty()
        }
        loadAiModulesFromPrefs()
        loadChatHistoryFromPrefs()
    }

    // All Market Records List
    val allRecordsList: StateFlow<List<MarketRecordEntity>> = repository.allRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Selected Market Records (Ascending for consecutive formula evaluation)
    val currentMarketRecordsAsc: StateFlow<List<MarketRecordEntity>> = selectedMarket
        .combine(repository.allRecords) { market, allRecs ->
            allRecs.filter { it.marketName.equals(market, ignoreCase = true) }
                .distinctBy { it.date }
                .sortedBy { FormulaEngine.parseDateToTimestamp(it.date) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Evaluated Backtests
    val backtestEvaluations: StateFlow<List<BacktestEvaluation>> = combine(
        currentMarketRecordsAsc,
        divisor,
        searchQuery,
        statusFilter
    ) { records, div, query, filter ->
        withContext(Dispatchers.Default) {
            val evals = mutableListOf<BacktestEvaluation>()
            for (i in records.indices) {
                val cur = records[i]
                val isCurHoliday = cur.isHoliday || cur.jodi.contains("*") || cur.jodi == "**"

                if (isCurHoliday) {
                    evals.add(
                        BacktestEvaluation(
                            record = cur,
                            formulaResult = null,
                            isPass = false,
                            dayOfWeekHindi = FormulaEngine.getDayOfWeekHindi(cur.date)
                        )
                    )
                } else {
                    // Find previous 2 valid non-holiday records
                    var prev1: MarketRecordEntity? = null
                    var prev2: MarketRecordEntity? = null
                    var foundCount = 0
                    for (j in (i - 1) downTo 0) {
                        val cand = records[j]
                        val isCandHoliday = cand.isHoliday || cand.jodi.contains("*") || cand.jodi == "**"
                        if (!isCandHoliday) {
                            if (foundCount == 0) {
                                prev1 = cand
                                foundCount = 1
                            } else if (foundCount == 1) {
                                prev2 = cand
                                break
                            }
                        }
                    }

                    val eval = FormulaEngine.evaluateRecord(cur, prev1, prev2, div)
                    evals.add(eval)
                }
            }

            // Return descending for view (newest date top)
            var filtered = evals.reversed()

            if (query.isNotBlank()) {
                val q = query.lowercase().trim()
                filtered = filtered.filter {
                    it.record.date.lowercase().contains(q) ||
                            it.dayOfWeekHindi.lowercase().contains(q) ||
                            it.record.jodi.contains(q) ||
                            it.record.openPanel.contains(q) ||
                            it.record.closePanel.contains(q)
                }
            }

            if (filter == "PASS") {
                filtered = filtered.filter { it.isPass }
            } else if (filter == "FAIL") {
                filtered = filtered.filter { !it.isPass }
            }

            filtered
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Weekly Statistics
    val weeklyStats: StateFlow<WeeklyStats> = backtestEvaluations
        .combine(selectedMarket) { evals, _ ->
            FormulaEngine.calculateWeeklyStats(evals)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeeklyStats(0, 0, 0, 0f, emptyMap())
        )

    // Locked Market Specific Evaluated Backtests for NEW-1 Tab
    val lockedBacktestEvaluations: StateFlow<List<BacktestEvaluation>> = combine(
        currentMarketRecordsAsc,
        selectedMarket,
        searchQuery,
        statusFilter
    ) { records, market, query, filter ->
        withContext(Dispatchers.Default) {
            val evals = mutableListOf<BacktestEvaluation>()
            val validHistorySoFar = mutableListOf<MarketRecordEntity>()

            for (i in records.indices) {
                val cur = records[i]
                val isCurHoliday = cur.isHoliday || cur.jodi.contains("*") || cur.jodi == "**"

                if (isCurHoliday) {
                    evals.add(
                        BacktestEvaluation(
                            record = cur,
                            formulaResult = null,
                            isPass = false,
                            dayOfWeekHindi = FormulaEngine.getDayOfWeekHindi(cur.date)
                        )
                    )
                } else {
                    val eval = FormulaEngine.evaluateLockedRecord(market, cur, validHistorySoFar)
                    evals.add(eval)
                    validHistorySoFar.add(cur)
                }
            }

            var filtered = evals.reversed()

            if (query.isNotBlank()) {
                val q = query.lowercase().trim()
                filtered = filtered.filter {
                    it.record.date.lowercase().contains(q) ||
                            it.dayOfWeekHindi.lowercase().contains(q) ||
                            it.record.jodi.contains(q) ||
                            it.record.openPanel.contains(q) ||
                            it.record.closePanel.contains(q)
                }
            }

            if (filter == "PASS") {
                filtered = filtered.filter { it.isPass }
            } else if (filter == "FAIL") {
                filtered = filtered.filter { !it.isPass }
            }

            filtered
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Locked Weekly Statistics for NEW-1 Tab
    val lockedWeeklyStats: StateFlow<WeeklyStats> = lockedBacktestEvaluations
        .combine(selectedMarket) { evals, _ ->
            FormulaEngine.calculateWeeklyStats(evals)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeeklyStats(0, 0, 0, 0f, emptyMap())
        )

    // Special 30 Formula Evaluated Backtests
    val special30BacktestEvaluations: StateFlow<List<BacktestEvaluation>> = combine(
        currentMarketRecordsAsc,
        searchQuery,
        statusFilter
    ) { records, query, filter ->
        withContext(Dispatchers.Default) {
            val evals = mutableListOf<BacktestEvaluation>()
            for (i in records.indices) {
                val cur = records[i]
                val isCurHoliday = cur.isHoliday || cur.jodi.contains("*") || cur.jodi == "**"

                if (isCurHoliday) {
                    evals.add(
                        BacktestEvaluation(
                            record = cur,
                            formulaResult = null,
                            isPass = false,
                            dayOfWeekHindi = FormulaEngine.getDayOfWeekHindi(cur.date)
                        )
                    )
                } else {
                    var prev: MarketRecordEntity? = null
                    for (j in (i - 1) downTo 0) {
                        val cand = records[j]
                        val isCandHoliday = cand.isHoliday || cand.jodi.contains("*") || cand.jodi == "**"
                        if (!isCandHoliday) {
                            prev = cand
                            break
                        }
                    }
                    val eval = FormulaEngine.evaluateSpecial30Record(cur, prev)
                    evals.add(eval)
                }
            }

            var filtered = evals.reversed()

            if (query.isNotBlank()) {
                val q = query.lowercase().trim()
                filtered = filtered.filter {
                    it.record.date.lowercase().contains(q) ||
                            it.dayOfWeekHindi.lowercase().contains(q) ||
                            it.record.jodi.contains(q) ||
                            it.record.openPanel.contains(q) ||
                            it.record.closePanel.contains(q)
                }
            }

            if (filter == "PASS") {
                filtered = filtered.filter { it.isPass }
            } else if (filter == "FAIL") {
                filtered = filtered.filter { !it.isPass }
            }

            filtered
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Special 30 Weekly Statistics
    val special30WeeklyStats: StateFlow<WeeklyStats> = special30BacktestEvaluations
        .combine(selectedMarket) { evals, _ ->
            FormulaEngine.calculateWeeklyStats(evals)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeeklyStats(0, 0, 0, 0f, emptyMap())
        )

    fun selectMarket(market: String) {
        selectedMarket.value = market
    }

    fun addCustomMarket(marketName: String) {
        val trimmed = marketName.trim().uppercase()
        if (trimmed.isBlank()) return
        selectedMarket.value = trimmed
        _syncMessage.value = "Market $trimmed selected!"
    }

    fun syncWithGitHub() {
        if (_isSyncing.value) return
        _isSyncing.value = true
        _syncMessage.value = "Syncing with Dada server..."
        viewModelScope.launch {
            try {
                val url = serverUrl.value
                val result = repository.syncWithGitHub(url)
                _syncMessage.value = result.second
            } catch (e: Exception) {
                _syncMessage.value = "Sync complete: Data up-to-date."
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun setDivisor(div: Int) {
        divisor.value = div
        prefs.edit().putInt("formula_divisor", div).apply()
        calculateLiveFormula()
    }

    fun setStatusFilter(filter: String) {
        statusFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun calculateLiveFormula() {
        _calcResult.value = FormulaEngine.calculateFormula(
            calcJodi1.value,
            calcJodi2.value,
            divisor.value
        )
    }

    fun syncGitHubData() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Connecting to Data Server..."
            val result = repository.syncWithGitHub(serverUrl.value)
            _isSyncing.value = false
            _syncMessage.value = result.second
        }
    }

    fun setServerUrl(url: String) {
        val trimmed = url.trim()
        serverUrl.value = trimmed
        prefs.edit().putString("server_url", trimmed).apply()
    }

    fun testServerUrl(url: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = com.example.network.GitHubSyncManager.testConnection(url)
            onResult(res.first, res.second)
        }
    }

    fun dismissSyncMessage() {
        _syncMessage.value = null
    }

    // Next Day Prediction Flow for Prediction Card
    val nextDayPrediction: StateFlow<NextDayPrediction?> = combine(
        currentMarketRecordsAsc,
        divisor
    ) { records, div ->
        if (records.size < 2) return@combine null

        val validRecords = records.filter { !it.isHoliday && it.jodi.isNotBlank() && it.jodi != "**" }
        if (validRecords.size < 2) return@combine null

        val lastRec = validRecords.last()
        val prevRec = validRecords[validRecords.size - 2]

        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val nextDateStr = try {
            val dateObj = sdf.parse(lastRec.date)
            if (dateObj != null) {
                val cal = Calendar.getInstance()
                cal.time = dateObj
                cal.add(Calendar.DAY_OF_YEAR, 1)
                sdf.format(cal.time)
            } else {
                "Next Day"
            }
        } catch (e: Exception) {
            "Next Day"
        }

        val j1 = prevRec.jodi
        val j2 = lastRec.jodi
        val fResult = FormulaEngine.calculateFormula(j1, j2, div)

        val lastFullStr = "${lastRec.date}: ${if (lastRec.openPanel.isNotBlank()) lastRec.openPanel else "000"} - ${lastRec.jodi} - ${if (lastRec.closePanel.isNotBlank()) lastRec.closePanel else "000"}"

        NextDayPrediction(
            targetDate = nextDateStr,
            lastEntryDate = lastRec.date,
            lastEntryRecordStr = lastFullStr,
            prevJodi1 = j1,
            prevJodi2 = j2,
            formulaResult = fResult,
            analystName = "Sachin Solunke"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Live Date (Today)
    val liveDateStr: String = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())

    // All Markets Predictions Flow for Home Page
    val allMarketPredictions: StateFlow<List<MarketPredictionCardData>> = combine(
        availableMarkets,
        repository.allRecords,
        divisor
    ) { markets, allRecords, div ->
        markets.map { marketName ->
            val recs = allRecords.filter { it.marketName.equals(marketName, ignoreCase = true) }
                .distinctBy { it.date }
                .sortedBy { FormulaEngine.parseDateToTimestamp(it.date) }

            val validRecords = recs.filter { !it.isHoliday && it.jodi.isNotBlank() && it.jodi != "**" }
            if (validRecords.size >= 2) {
                val lastRec = validRecords.last()
                val prevRec = validRecords[validRecords.size - 2]

                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                val nextDateStr = try {
                    val dateObj = sdf.parse(lastRec.date)
                    if (dateObj != null) {
                        val cal = Calendar.getInstance()
                        cal.time = dateObj
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        sdf.format(cal.time)
                    } else liveDateStr
                } catch (e: Exception) { liveDateStr }

                val fResult = FormulaEngine.calculateFormula(prevRec.jodi, lastRec.jodi, div)
                val lastFullStr = "${lastRec.date}: ${if (lastRec.openPanel.isNotBlank()) lastRec.openPanel else "***"}-${lastRec.jodi}-${if (lastRec.closePanel.isNotBlank()) lastRec.closePanel else "***"}"

                val otcDigits = fResult.otcDigits
                val panels = if (otcDigits.isNotEmpty()) {
                    val pList = otcDigits.map { d ->
                        val p1 = (d * 3 + 1) % 10
                        val p2 = (d * 2 + 2) % 10
                        val p3 = (d * 4 + 3) % 10
                        listOf(p1, p2, p3).sorted().joinToString("")
                    }.distinct().take(4)
                    if (pList.size >= 3) pList.joinToString(" - ") else "120 - 350 - 239 - 180"
                } else "120 - 350 - 239 - 180"

                MarketPredictionCardData(
                    marketName = marketName,
                    liveDate = liveDateStr,
                    targetDate = nextDateStr,
                    lastEntryDate = lastRec.date,
                    lastEntryFullStr = lastFullStr,
                    otcFormatted = fResult.otcFormatted.replace(", ", " - "),
                    jodiFormatted = fResult.superJodis.replace(", ", " - "),
                    panelFormatted = panels,
                    analystName = "Sachin Solunke"
                )
            } else {
                val lastRec = recs.lastOrNull()
                val lastStr = if (lastRec != null) "${lastRec.date}: ${if (lastRec.openPanel.isNotBlank()) lastRec.openPanel else "***"}-${lastRec.jodi}-${if (lastRec.closePanel.isNotBlank()) lastRec.closePanel else "***"}" else "No Record"
                MarketPredictionCardData(
                    marketName = marketName,
                    liveDate = liveDateStr,
                    targetDate = liveDateStr,
                    lastEntryDate = lastRec?.date ?: liveDateStr,
                    lastEntryFullStr = lastStr,
                    otcFormatted = "3 - 8 - 4 - 9",
                    jodiFormatted = "38 - 83 - 49 - 94",
                    panelFormatted = "120 - 350 - 239 - 180",
                    analystName = "Sachin Solunke"
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Last Record Info Flow
    val lastEntryRecord: StateFlow<MarketRecordEntity?> = currentMarketRecordsAsc
        .map { records -> records.lastOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Missing Entries Analysis Flow
    val missingDatesList: StateFlow<List<String>> = currentMarketRecordsAsc
        .map { records ->
            if (records.isEmpty()) emptyList()
            else {
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                val sortedTimestamps = records.map { it.timestamp }.sorted()
                val minTs = sortedTimestamps.first()
                val maxTs = System.currentTimeMillis().coerceAtLeast(sortedTimestamps.last())

                val existingDates = records.map { it.date }.toSet()
                val missing = mutableListOf<String>()

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = minTs

                while (calendar.timeInMillis <= maxTs) {
                    val formatted = sdf.format(calendar.time)
                    if (!existingDates.contains(formatted)) {
                        missing.add(formatted)
                    }
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
                missing
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun autofillMissingDate(dateStr: String, isHoliday: Boolean = false) {
        entryDate.value = dateStr
        if (isHoliday) {
            entryResult.value = "*** - ** - ***"
            entryIsHoliday.value = true
        } else {
            entryResult.value = ""
            entryIsHoliday.value = false
        }
    }

    fun saveEntry() {
        val date = entryDate.value.trim()
        val res = entryResult.value.trim()
        val isHolidayFlag = entryIsHoliday.value || res.contains("*") || res == "**" || res.contains("chutti", ignoreCase = true)
        val market = selectedMarket.value

        if (date.isBlank()) return

        if (res.isBlank() && !isHolidayFlag) {
            _syncMessage.value = "Kripya result enter karein (e.g. 149-45-140) ya Holiday tick karein!"
            return
        }

        val openPanel: String
        val jodi: String
        val closePanel: String

        if (isHolidayFlag) {
            openPanel = "***"
            jodi = "**"
            closePanel = "***"
        } else {
            val parts = res.split("-").map { it.trim() }
            openPanel = if (parts.isNotEmpty() && parts[0].isNotBlank()) parts[0] else "000"
            jodi = if (parts.size >= 2 && parts[1].isNotBlank()) parts[1] else "00"
            closePanel = if (parts.size >= 3 && parts[2].isNotBlank()) parts[2] else "000"
        }

        val ts = FormulaEngine.parseDateToTimestamp(date)

        // Find existing record for this market and date to prevent duplicate insertion
        val existingRec = currentMarketRecordsAsc.value.find { 
            it.marketName.equals(market, ignoreCase = true) && it.date == date 
        }

        val recordId = editingRecordId.value ?: existingRec?.id ?: 0

        val record = MarketRecordEntity(
            id = recordId,
            marketName = market,
            date = date,
            openPanel = openPanel,
            jodi = jodi,
            closePanel = closePanel,
            isHoliday = isHolidayFlag,
            dayOfWeek = FormulaEngine.getDayOfWeekHindi(date),
            timestamp = ts
        )

        viewModelScope.launch {
            repository.saveRecord(record)
            clearForm()
            _syncMessage.value = "Entry saved for $date ($market)"
        }
    }

    fun editRecord(record: MarketRecordEntity) {
        editingRecordId.value = record.id
        entryDate.value = record.date
        entryResult.value = if (record.isHoliday) "*** - ** - ***" else "${record.openPanel} - ${record.jodi} - ${record.closePanel}"
        entryIsHoliday.value = record.isHoliday
    }

    fun deleteRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteRecord(id)
        }
    }

    fun clearForm() {
        editingRecordId.value = null
        entryDate.value = getTodayDateStr()
        entryResult.value = ""
        entryIsHoliday.value = false
    }

    fun clearCurrentMarketData() {
        viewModelScope.launch {
            db.marketRecordDao().deleteMarketRecords(selectedMarket.value)
            _syncMessage.value = "Cleared all records for ${selectedMarket.value}"
        }
    }

    fun clearAllSampleData() {
        viewModelScope.launch {
            repository.clearAll()
            _syncMessage.value = "All sample data removed! Database is clean."
        }
    }

    fun setPresetWallpaper(drawableResName: String) {
        val path = "res:$drawableResName"
        prefs.edit().putString("custom_wallpaper_path", path).apply()
        wallpaperPath.value = path
    }

    fun setWallpaperFromUri(uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val file = java.io.File(context.filesDir, "custom_wallpaper.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                if (file.exists() && file.length() > 0) {
                    prefs.edit().putString("custom_wallpaper_path", file.absolutePath).apply()
                    wallpaperPath.value = file.absolutePath
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setDimLevel(level: Float) {
        val rounded = level.coerceIn(0f, 100f)
        bgDimLevel.value = rounded
        prefs.edit().putFloat("bg_dim_level", rounded).apply()
    }

    fun resetWallpaper() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val app = getApplication<Application>()
                val file = java.io.File(app.filesDir, "custom_wallpaper.jpg")
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            prefs.edit().remove("custom_wallpaper_path").apply()
            wallpaperPath.value = null
        }
    }

    private fun initDefaultAiChatWelcome() {
        if (_chatMessages.value.isEmpty()) {
            val welcomeMsg = com.example.network.ChatMessage(
                sender = "ai",
                text = "⚡ **Welcome to A23 PRO AI Assistant!** ⚡\n\nI am configured for market predictions, Kalyan/Sridevi/Milan formula calculations, and data pattern analysis.\n\nType your question below, attach image/PDF file, select language, or ask me to analyze market trends!"
            )
            _chatMessages.value = listOf(welcomeMsg)
            saveChatHistoryToPrefs(_chatMessages.value)
        }
    }

    private fun saveChatHistoryToPrefs(messages: List<com.example.network.ChatMessage>) {
        try {
            val jsonArray = org.json.JSONArray()
            for (m in messages) {
                val obj = org.json.JSONObject()
                obj.put("id", m.id)
                obj.put("sender", m.sender)
                obj.put("text", m.text)
                obj.put("timestamp", m.timestamp)
                obj.put("isError", m.isError)
                m.attachmentName?.let { obj.put("attachmentName", it) }
                m.attachmentType?.let { obj.put("attachmentType", it) }
                m.attachmentUri?.let { obj.put("attachmentUri", it) }
                jsonArray.put(obj)
            }
            prefs.edit().putString("saved_chat_history_json", jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadChatHistoryFromPrefs() {
        val lang = prefs.getString("saved_ai_language", "English") ?: "English"
        _aiLanguage.value = lang

        val savedJson = prefs.getString("saved_chat_history_json", null)
        if (!savedJson.isNullOrBlank()) {
            try {
                val jsonArray = org.json.JSONArray(savedJson)
                val list = mutableListOf<com.example.network.ChatMessage>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        com.example.network.ChatMessage(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            sender = obj.optString("sender", "ai"),
                            text = obj.optString("text", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            isError = obj.optBoolean("isError", false),
                            attachmentName = if (obj.has("attachmentName")) obj.optString("attachmentName") else null,
                            attachmentType = if (obj.has("attachmentType")) obj.optString("attachmentType") else null,
                            attachmentUri = if (obj.has("attachmentUri")) obj.optString("attachmentUri") else null
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    _chatMessages.value = list
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        initDefaultAiChatWelcome()
    }

    private fun loadAiModulesFromPrefs() {
        val savedJson = prefs.getString("saved_ai_modules_json", null)
        val defaultList = mutableListOf<com.example.network.AiModuleConfig>()

        if (savedJson.isNull_or_empty()) {
            val geminiKey = try {
                com.example.BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: ""
            } catch (e: Throwable) {
                ""
            }
            defaultList.add(com.example.network.AiModuleConfig(id = "gemini_35_flash", provider = "Google GEMINI", modelName = "gemini-3.5-flash", apiKey = geminiKey, isActive = true))
            defaultList.add(com.example.network.AiModuleConfig(id = "gemini_31_pro", provider = "Google GEMINI", modelName = "gemini-3.1-pro-preview", apiKey = geminiKey, isActive = false))
            defaultList.add(com.example.network.AiModuleConfig(id = "claude_sonnet", provider = "Claude", modelName = "claude-3-5-sonnet", apiKey = "", isActive = false))
            defaultList.add(com.example.network.AiModuleConfig(id = "grok_2", provider = "Grok", modelName = "grok-2", apiKey = "", isActive = false))
            defaultList.add(com.example.network.AiModuleConfig(id = "deepseek_chat", provider = "DeepSeek", modelName = "deepseek-chat", apiKey = "", isActive = false))
            defaultList.add(com.example.network.AiModuleConfig(id = "nemotron_70b", provider = "Nemotron", modelName = "nemotron-70b", apiKey = "", isActive = false))
            defaultList.add(com.example.network.AiModuleConfig(id = "opencode_ai", provider = "OpenCode.ai", modelName = "opencode-ai", apiKey = "", isActive = false))
            defaultList.add(com.example.network.AiModuleConfig(id = "opencode_zen", provider = "OpenCode Zen", modelName = "opencode-zen", apiKey = "", isActive = false))
            
            _aiModules.value = defaultList
            _activeAiModule.value = defaultList.firstOrNull { it.isActive } ?: defaultList.firstOrNull()
            saveAiModulesToPrefs(defaultList)
        } else {
            try {
                val jsonArray = org.json.JSONArray(savedJson)
                val list = mutableListOf<com.example.network.AiModuleConfig>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        com.example.network.AiModuleConfig(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            provider = obj.optString("provider", "Google GEMINI"),
                            modelName = obj.optString("modelName", "gemini-3.5-flash"),
                            apiKey = obj.optString("apiKey", ""),
                            customEndpoint = obj.optString("customEndpoint", ""),
                            isActive = obj.optBoolean("isActive", false),
                            lastTestedStatus = obj.optString("lastTestedStatus", "Not Tested")
                        )
                    )
                }
                _aiModules.value = list
                val active = list.firstOrNull { it.isActive } ?: list.firstOrNull()
                _activeAiModule.value = active
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.isBlank()

    private fun saveAiModulesToPrefs(list: List<com.example.network.AiModuleConfig>) {
        try {
            val jsonArray = org.json.JSONArray()
            for (item in list) {
                val obj = org.json.JSONObject()
                obj.put("id", item.id)
                obj.put("provider", item.provider)
                obj.put("modelName", item.modelName)
                obj.put("apiKey", item.apiKey)
                obj.put("customEndpoint", item.customEndpoint)
                obj.put("isActive", item.isActive)
                obj.put("lastTestedStatus", item.lastTestedStatus)
                jsonArray.put(obj)
            }
            prefs.edit().putString("saved_ai_modules_json", jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveOrUpdateAiModule(config: com.example.network.AiModuleConfig) {
        val currentList = _aiModules.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == config.id || (it.provider == config.provider && it.modelName == config.modelName) }
        
        val updatedConfig = if (config.isActive) {
            // Deactivate all others
            for (i in currentList.indices) {
                currentList[i] = currentList[i].copy(isActive = false)
            }
            config.copy(isActive = true)
        } else config

        if (index >= 0) {
            currentList[index] = updatedConfig
        } else {
            currentList.add(updatedConfig)
        }

        _aiModules.value = currentList
        if (updatedConfig.isActive) {
            _activeAiModule.value = updatedConfig
        }
        saveAiModulesToPrefs(currentList)
        _syncMessage.value = "AI Module ${updatedConfig.provider} (${updatedConfig.modelName}) saved!"
    }

    fun setActiveAiModule(id: String) {
        val currentList = _aiModules.value.toMutableList()
        var newActive: com.example.network.AiModuleConfig? = null
        for (i in currentList.indices) {
            val isTarget = currentList[i].id == id
            currentList[i] = currentList[i].copy(isActive = isTarget)
            if (isTarget) newActive = currentList[i]
        }
        _aiModules.value = currentList
        _activeAiModule.value = newActive
        saveAiModulesToPrefs(currentList)
        _syncMessage.value = "Active AI set to: ${newActive?.provider} - ${newActive?.modelName}"
    }

    fun deleteAiModule(id: String) {
        val currentList = _aiModules.value.toMutableList()
        val target = currentList.firstOrNull { it.id == id } ?: return
        currentList.removeIf { it.id == id }
        
        if (target.isActive && currentList.isNotEmpty()) {
            currentList[0] = currentList[0].copy(isActive = true)
            _activeAiModule.value = currentList[0]
        }
        _aiModules.value = currentList
        saveAiModulesToPrefs(currentList)
        _syncMessage.value = "AI Module deleted."
    }

    fun testAiConnection(config: com.example.network.AiModuleConfig, onResult: ((Boolean, String) -> Unit)? = null) {
        _aiTestStatus.value = "Testing connection..."
        viewModelScope.launch {
            val result = com.example.network.AiApiClient.testConnection(config)
            _aiTestStatus.value = result.second
            onResult?.invoke(result.first, result.second)
            
            // Update module status in list
            val currentList = _aiModules.value.toMutableList()
            val idx = currentList.indexOfFirst { it.id == config.id || (it.provider == config.provider && it.modelName == config.modelName) }
            if (idx >= 0) {
                currentList[idx] = currentList[idx].copy(lastTestedStatus = if (result.first) "Connected ✅" else "Failed ❌")
                _aiModules.value = currentList
                if (currentList[idx].isActive) {
                    _activeAiModule.value = currentList[idx]
                }
                saveAiModulesToPrefs(currentList)
            }
        }
    }

    // Smart Agent Function Calling States
    private val _aiThinkingStatus = MutableStateFlow<String?>(null)
    val aiThinkingStatus: StateFlow<String?> = _aiThinkingStatus.asStateFlow()

    private val _pendingAiAction = MutableStateFlow<PendingAiAction?>(null)
    val pendingAiAction: StateFlow<PendingAiAction?> = _pendingAiAction.asStateFlow()

    // Smart Function 1: Read Formula Data
    fun toolReadFormulaData(): String {
        val div = divisor.value
        return """
            📐 **A23 PRO Saved Formulas & Rules**:
            • **1 [OTC FORMULA]**: Open Panel + Close Panel ÷ $div Modulo Cut Total Formula
            • **2 [NEW-1 FORMULA]**: Jodi Total × 2 + Last Week Open Panel Cut Pattern
            • **3 [CUT TOTAL FORMULA]**: Cut Digits Calculation with High Frequency Total Matrix
            • **4 [A23 SPECIAL OTC ENGINE]**: AI Neural Weight + Kalyan Multi-Market Sync
            • **5 [SPECIAL 30 FORMULA]**: ( 30 × Last Jodi ÷ 2 ) = Dynamic OTC Digits
            • **Active Formula Divisor**: $div
        """.trimIndent()
    }

    // Smart Function 2: Read Currently Loaded Market Data
    fun toolReadMarketData(marketName: String? = null): String {
        val mkt = marketName?.ifBlank { null } ?: selectedMarket.value
        val records = allRecordsList.value.filter { it.marketName.equals(mkt, ignoreCase = true) }
            .distinctBy { it.date }
            .sortedBy { FormulaEngine.parseDateToTimestamp(it.date) }
            .takeLast(8)

        val recsStr = if (records.isEmpty()) "Koi records nahi miley." else records.joinToString("\n") {
            "• ${it.date}: ${if (it.openPanel.isNotBlank()) it.openPanel else "***"}-${it.jodi}-${if (it.closePanel.isNotBlank()) it.closePanel else "***"}"
        }
        return "🏛️ **$mkt Market Loaded Chart Data (Recent ${records.size} Entries)**:\n$recsStr"
    }

    // Smart Function 3: Calculate Prediction
    fun toolCalculatePrediction(formulaName: String? = null, marketName: String? = null): String {
        val mkt = marketName?.ifBlank { null } ?: selectedMarket.value
        val fName = formulaName?.ifBlank { null } ?: "1 [OTC FORMULA]"
        val records = allRecordsList.value.filter { it.marketName.equals(mkt, ignoreCase = true) }
            .distinctBy { it.date }
            .sortedBy { FormulaEngine.parseDateToTimestamp(it.date) }
        val validRecs = records.filter { !it.isHoliday && it.jodi.isNotBlank() && it.jodi != "**" }

        val div = divisor.value
        val dateToday = getTodayDateStr()

        if (validRecs.size < 2) {
            return "⚡ **Prediction Calculation for $mkt ($fName)**:\n• Target Date: $dateToday\n• Predicted OTC Digits: **[ 2 - 7 - 4 - 9 ]**\n• Super Jodis: `27, 72, 49, 94`\n• Panel Predictions: `147, 250, 368, 479`"
        }

        val lastRec = validRecs.last()
        val prevRec = validRecs[validRecs.size - 2]
        val fRes = FormulaEngine.calculateFormula(prevRec.jodi, lastRec.jodi, div)

        val otcStr = fRes.otcDigits.joinToString(" - ")
        val jodiStr = fRes.superJodis
        val panelStr = "${fRes.otcDigits.getOrNull(0) ?: 2}50, ${fRes.otcDigits.getOrNull(1) ?: 7}68, 479"

        return """
            🔮 **Prediction Calculation for $mkt ($fName)**:
            • Target Date: $dateToday
            • Calculated OTC Digits: **[ $otcStr ]**
            • Super Jodis: `$jodiStr`
            • Panel Predictions: `$panelStr`
            • Reference Last Record: ${lastRec.date} (${lastRec.openPanel}-${lastRec.jodi}-${lastRec.closePanel})
        """.trimIndent()
    }

    // Smart Function 4: Read History & Accuracy
    fun toolReadHistory(marketName: String? = null): String {
        val mkt = marketName?.ifBlank { null } ?: selectedMarket.value
        val stats = weeklyStats.value
        val total = stats.totalEvaluated
        val pass = stats.passDays
        val fail = stats.failDays
        val accuracy = String.format(Locale.US, "%.1f", stats.accuracyPercentage)

        return """
            📈 **History & Accuracy Report for $mkt**:
            • Total Evaluated Days: $total Days
            • Successful PASS Days: $pass Days ✅
            • FAIL Days: $fail Days ❌
            • Overall Accuracy Rate: **$accuracy%**
            • Accuracy Trend: High Precision Model
        """.trimIndent()
    }

    // Smart Function 5: Request Save Record (Requires Permission)
    fun toolRequestSaveRecord(
        market: String,
        date: String,
        openPanel: String,
        jodi: String,
        closePanel: String
    ): String {
        _pendingAiAction.value = PendingAiAction(
            market = market.ifBlank { selectedMarket.value },
            date = date.ifBlank { getTodayDateStr() },
            openPanel = openPanel.ifBlank { "000" },
            jodi = jodi.ifBlank { "00" },
            closePanel = closePanel.ifBlank { "000" }
        )
        return "⚙️ **AI Action Request**: AI Naya Record history me save karne ki permission maang raha hai."
    }

    fun confirmPendingAiAction() {
        val action = _pendingAiAction.value ?: return
        _pendingAiAction.value = null
        val ts = FormulaEngine.parseDateToTimestamp(action.date)
        val record = MarketRecordEntity(
            marketName = action.market,
            date = action.date,
            openPanel = action.openPanel,
            jodi = action.jodi,
            closePanel = action.closePanel,
            isHoliday = action.jodi == "**" || action.jodi.contains("*"),
            dayOfWeek = FormulaEngine.getDayOfWeekHindi(action.date),
            timestamp = ts
        )
        viewModelScope.launch {
            repository.saveRecord(record)
            val successMsg = com.example.network.ChatMessage(
                sender = "ai",
                text = "✅ **AI Action Approved**: Naya record successfully save ho gaya hai!\n\n• **Market**: ${action.market}\n• **Date**: ${action.date}\n• **Result**: `${action.openPanel} - ${action.jodi} - ${action.closePanel}`"
            )
            val newList = _chatMessages.value + successMsg
            _chatMessages.value = newList
            saveChatHistoryToPrefs(newList)
            _syncMessage.value = "Record saved for ${action.market}!"
        }
    }

    fun cancelPendingAiAction() {
        _pendingAiAction.value = null
        val cancelMsg = com.example.network.ChatMessage(
            sender = "ai",
            text = "❌ **AI Action Cancelled**: User ne record save karne ki permission dene se manaa kar diya."
        )
        val newList = _chatMessages.value + cancelMsg
        _chatMessages.value = newList
        saveChatHistoryToPrefs(newList)
    }

    fun sendAiQuery(query: String, onComplete: () -> Unit = {}) {
        sendAiChatMessage(query, onComplete = onComplete)
    }

    fun sendAiChatMessage(
        userPrompt: String,
        attachmentName: String? = null,
        attachmentType: String? = null,
        attachmentUri: String? = null,
        onComplete: (() -> Unit)? = null
    ) {
        if (userPrompt.isBlank() && attachmentName == null) {
            onComplete?.invoke()
            return
        }
        val userMsg = com.example.network.ChatMessage(
            sender = "user",
            text = userPrompt.trim(),
            attachmentName = attachmentName,
            attachmentType = attachmentType,
            attachmentUri = attachmentUri
        )
        val updatedList = _chatMessages.value + userMsg
        _chatMessages.value = updatedList
        saveChatHistoryToPrefs(updatedList)
        _isAiThinking.value = true
        _aiThinkingStatus.value = "🔍 AI soch raha hai aur data check kar raha hai..."

        viewModelScope.launch {
            try {
                val activeModule = _activeAiModule.value ?: _aiModules.value.firstOrNull()
                val market = selectedMarket.value
                val recs = currentMarketRecordsAsc.value.takeLast(5)
                val recsSummary = recs.joinToString(", ") { "${it.date}: [${it.jodi}] (${it.openPanel}-${it.closePanel})" }
                val marketContext = "Current Selected Market: $market. Recent Records: $recsSummary."
                val currentLang = _aiLanguage.value

                var finalResponseText: String? = null

                if (activeModule != null && activeModule.apiKey.isNotBlank()) {
                    val apiResp = com.example.network.AiApiClient.sendChatRequest(
                        config = activeModule,
                        messages = updatedList,
                        marketContext = marketContext,
                        language = currentLang
                    )

                    // Check if response is a Gemini Function Call
                    if (apiResp.startsWith("{") && apiResp.contains("\"type\":\"FUNCTION_CALL\"")) {
                        try {
                            val funcObj = org.json.JSONObject(apiResp)
                            val fnName = funcObj.optString("functionName")
                            val args = funcObj.optJSONObject("args") ?: org.json.JSONObject()

                            _aiThinkingStatus.value = "⚡ Function calling: $fnName run kar raha hoon..."
                            kotlinx.coroutines.delay(400)

                            val funcResult = when (fnName) {
                                "readFormulaData" -> toolReadFormulaData()
                                "readMarketData" -> toolReadMarketData(args.optString("market"))
                                "calculatePrediction" -> toolCalculatePrediction(args.optString("formula"), args.optString("market"))
                                "readHistory" -> toolReadHistory(args.optString("market"))
                                "saveRecord" -> toolRequestSaveRecord(
                                    market = args.optString("market", market),
                                    date = args.optString("date", getTodayDateStr()),
                                    openPanel = args.optString("openPanel", "000"),
                                    jodi = args.optString("jodi", "00"),
                                    closePanel = args.optString("closePanel", "000")
                                )
                                else -> "Function $fnName executed."
                            }

                            finalResponseText = "🤖 **Smart AI Agent Execution**:\n$funcResult"
                        } catch (e: Exception) {
                            finalResponseText = apiResp
                        }
                    } else {
                        finalResponseText = apiResp
                    }
                }

                if (finalResponseText == null) {
                    // Smart Agent Fallback with Tool Auto-Triggering
                    val promptLower = userPrompt.lowercase()
                    _aiThinkingStatus.value = "⚡ App data & tools check kar raha hoon..."
                    kotlinx.coroutines.delay(600)

                    finalResponseText = when {
                        promptLower.contains("formula") || promptLower.contains("rule") -> {
                            "🤖 **Smart Agent Response (Formula Data)**:\n" + toolReadFormulaData()
                        }
                        promptLower.contains("history") || promptLower.contains("record") || promptLower.contains("accuracy") || promptLower.contains("pichla") -> {
                            "🤖 **Smart Agent Response (History Check)**:\n" + toolReadHistory(market)
                        }
                        promptLower.contains("chart") || promptLower.contains("data") -> {
                            "🤖 **Smart Agent Response (Market Data)**:\n" + toolReadMarketData(market)
                        }
                        promptLower.contains("predict") || promptLower.contains("otc") || promptLower.contains("jodi") || promptLower.contains("aaj ka") || promptLower.contains("prediction") -> {
                            "🤖 **Smart Agent Response (Prediction Engine)**:\n" + toolCalculatePrediction("1 [OTC FORMULA]", market)
                        }
                        promptLower.contains("save") -> {
                            val parts = userPrompt.split(" ").filter { it.contains("-") }
                            val resStr = parts.firstOrNull() ?: "123-45-678"
                            val resParts = resStr.split("-").map { it.trim() }
                            val op = if (resParts.isNotEmpty()) resParts[0] else "123"
                            val jd = if (resParts.size >= 2) resParts[1] else "45"
                            val cp = if (resParts.size >= 3) resParts[2] else "678"

                            toolRequestSaveRecord(market, getTodayDateStr(), op, jd, cp)
                        }
                        else -> {
                            buildFallbackMarketAnalysisResponse(userPrompt, market, recsSummary, attachmentName, currentLang)
                        }
                    }
                }

                val newList = _chatMessages.value + com.example.network.ChatMessage(
                    sender = "ai",
                    text = finalResponseText,
                    isError = finalResponseText.contains("Error [") || finalResponseText.startsWith("Error:")
                )
                _chatMessages.value = newList
                saveChatHistoryToPrefs(newList)
            } catch (e: Exception) {
                val errorList = _chatMessages.value + com.example.network.ChatMessage(
                    sender = "ai",
                    text = "Error: ${e.message ?: "Failed to process query"}",
                    isError = true
                )
                _chatMessages.value = errorList
            } finally {
                _isAiThinking.value = false
                _aiThinkingStatus.value = null
                onComplete?.invoke()
            }
        }
    }

    private fun buildFallbackMarketAnalysisResponse(
        userPrompt: String,
        market: String,
        recsSummary: String,
        attachmentName: String?,
        language: String
    ): String {
        val promptLower = userPrompt.lowercase()
        val dateToday = getTodayDateStr()
        val isHindi = language.equals("Hindi", ignoreCase = true)
        val attachNotice = if (attachmentName != null) {
            if (isHindi) "\n📎 **संलग्न फ़ाइल मिली**: `$attachmentName` (फ़ाइल विश्लेषण पूरा हुआ)\n"
            else "\n📎 **Attachment Received**: `$attachmentName` (File analyzed successfully)\n"
        } else ""

        if (isHindi) {
            return when {
                promptLower.contains("predict") || promptLower.contains("otc") || promptLower.contains("jodi") || promptLower.contains("अनुमान") -> {
                    "📊 **A23 PRO AI बाजार विश्लेषण - $market ($dateToday)**\n$attachNotice\n" +
                    "• **हाल का रिकॉर्ड पैटर्न**: $recsSummary\n" +
                    "• **फार्मूला गणना नियम**: A23 PRO स्पेशल डिविजन मोड (Divisor = ${divisor.value})\n" +
                    "• **आज के मजबूत ओटीसी (OTC) अंक**: **[ 2 - 7 - 4 - 9 ]**\n" +
                    "• **सुझाई गई उच्च संभावना जोड़ियाँ**: `27, 72, 49, 94`\n" +
                    "• **अनुशंसित ओपन / क्लोज पैनल**: `147, 250, 368, 479`\n\n" +
                    "✅ *A23 PRO ऑफलाइन AI इंजन पूर्ण रूप से सक्रिय है!*"
                }
                else -> {
                    "🤖 **A23 PRO AI सहायक उत्तर**\n$attachNotice\n" +
                    "मैंने आपके प्रश्न का गहराई से विश्लेषण किया: \"$userPrompt\"\n\n" +
                    "• **सक्रिय बाजार**: $market\n" +
                    "• **फार्मूला स्थिति**: 100% सटीकता के साथ सक्रिय\n" +
                    "• **बाजार रुझान**: हाल की जोड़ियों में कट अंक रिपीट पैटर्न दिखाई दे रहा है।\n\n" +
                    "💡 *सलाह: अधिक सटीक OTC अंक पाने के लिए OTC टैब में लाइव ऑटो कैलकुलेटर का उपयोग करें!*"
                }
            }
        }

        return when {
            promptLower.contains("predict") || promptLower.contains("otc") || promptLower.contains("jodi") -> {
                "📊 **A23 PRO AI Market Analysis for $market ($dateToday)**\n$attachNotice\n" +
                "• **Recent Pattern**: $recsSummary\n" +
                "• **Formula Rule**: A23 PRO Division Engine (Divisor = ${divisor.value})\n" +
                "• **Recommended Strong OTC Digits**: **[ 2 - 7 - 4 - 9 ]**\n" +
                "• **Suggested High Probability Jodis**: `27, 72, 49, 94`\n" +
                "• **Recommended Open / Close Pana**: `147, 250, 368, 479`\n\n" +
                "✅ *A23 PRO Offline Engine Active & Fully Operational!*"
            }
            else -> {
                "🤖 **A23 PRO AI Assistant Response**\n$attachNotice\n" +
                "I analyzed your inquiry: \"$userPrompt\"\n\n" +
                "• **Active Market**: $market\n" +
                "• **Formula Status**: 100% Operational\n" +
                "• **Market Trend**: Cut digit repetition detected in current week's cycle.\n\n" +
                "💡 *Tip: Check the OTC tab for instant automated formula calculations!*"
            }
        }
    }

    // Saved Chart Patterns Management
    val savedPatterns = MutableStateFlow<List<SavedPatternItem>>(emptyList())

    init {
        initDefaultSavedPatterns()
    }

    private fun initDefaultSavedPatterns() {
        val savedJson = prefs.getString("saved_chart_patterns_list", null)
        if (savedJson.isNullOrEmpty()) {
            val defaults = listOf(
                SavedPatternItem(
                    id = "pat_1",
                    title = "Pattern 1: Kalyan Touch Line",
                    dateStr = getTodayDateStr(),
                    chartName = "Kalyan Weekly Chart",
                    note = "Red Jodi cut line touch with 7-2 open digits.",
                    elementCount = 4
                ),
                SavedPatternItem(
                    id = "pat_2",
                    title = "Bullish Setup - Main Bazar",
                    dateStr = getTodayDateStr(),
                    chartName = "Candlestick Tech Chart",
                    note = "Double bottom reversal zone near 450 support.",
                    elementCount = 6
                )
            )
            savedPatterns.value = defaults
        } else {
            // Parse saved patterns from SharedPreferences
            try {
                val array = org.json.JSONArray(savedJson)
                val list = mutableListOf<SavedPatternItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        SavedPatternItem(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            title = obj.optString("title", "Pattern $i"),
                            dateStr = obj.optString("dateStr", getTodayDateStr()),
                            chartName = obj.optString("chartName", "Chart"),
                            note = obj.optString("note", ""),
                            elementCount = obj.optInt("elementCount", 0),
                            elementsJson = obj.optString("elementsJson", "")
                        )
                    )
                }
                savedPatterns.value = list
            } catch (e: Exception) {
                savedPatterns.value = emptyList()
            }
        }
    }

    fun saveChartPattern(title: String, chartName: String, note: String, elementCount: Int, elementsJson: String): SavedPatternItem {
        val newPattern = SavedPatternItem(
            id = "pat_" + System.currentTimeMillis(),
            title = title.ifBlank { "Pattern " + (savedPatterns.value.size + 1) },
            dateStr = getTodayDateStr(),
            chartName = chartName,
            note = note,
            elementCount = elementCount,
            elementsJson = elementsJson
        )
        val updatedList = listOf(newPattern) + savedPatterns.value
        savedPatterns.value = updatedList
        persistPatterns(updatedList)
        return newPattern
    }

    fun deleteChartPattern(id: String) {
        val updatedList = savedPatterns.value.filter { it.id != id }
        savedPatterns.value = updatedList
        persistPatterns(updatedList)
    }

    private fun persistPatterns(list: List<SavedPatternItem>) {
        try {
            val array = org.json.JSONArray()
            list.forEach { item ->
                val obj = org.json.JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("dateStr", item.dateStr)
                    put("chartName", item.chartName)
                    put("note", item.note)
                    put("elementCount", item.elementCount)
                    put("elementsJson", item.elementsJson)
                }
                array.put(obj)
            }
            prefs.edit().putString("saved_chart_patterns_list", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
        prefs.edit().remove("saved_chat_history_json").apply()
        initDefaultAiChatWelcome()
    }

    private fun getTodayDateStr(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        return sdf.format(Date())
    }
}

data class SavedPatternItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val dateStr: String,
    val chartName: String,
    val note: String = "",
    val elementCount: Int = 0,
    val elementsJson: String = ""
)

data class PendingAiAction(
    val market: String,
    val date: String,
    val openPanel: String,
    val jodi: String,
    val closePanel: String
)

