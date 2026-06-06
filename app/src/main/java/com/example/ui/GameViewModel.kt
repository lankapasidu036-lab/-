package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.*
import com.example.data.repository.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

enum class GameState {
    WAITING_FOR_BETS,
    FLYING,
    CRASHED
}

data class LiveBettor(
    val name: String,
    val avatarEmoji: String,
    val betAmount: Double,
    val targetMultiplier: Double, // random crash cash-out point
    val cashoutMultiplier: Double? = null,
    val winAmount: Double = 0.0,
    val status: String = "ACTIVE" // "ACTIVE", "WON", "LOST"
)

data class GameUiState(
    val balance: Double = 10000.0,
    val betAmount: Double = 100.0,
    val autoCashOutMultiplier: Double = 2.0,
    val isAutoCashOutEnabled: Boolean = false,
    val autoBetEnabled: Boolean = false,
    val gameState: GameState = GameState.WAITING_FOR_BETS,
    val countdownSeconds: Double = 6.0,
    val currentMultiplier: Double = 1.00,
    val hasPlacedBetInCurrentRound: Boolean = false,
    val hasCashedOutInCurrentRound: Boolean = false,
    val userCashoutMultiplier: Double = 0.0,
    val userWinnings: Double = 0.0,
    val liveBettors: List<LiveBettor> = emptyList(),
    val totalGamesPlayed: Int = 0,
    val biggestWinMultiplier: Double = 0.0,
    val totalWonAmount: Double = 0.0,
    val isDailyClaimAvailable: Boolean = true,
    val nextDailyClaimCountdown: String = "",
    val lastCrashMultiplier: Double = 1.0,
    val hostProfit: Double = 50000.0,
    val hostTotalCollected: Double = 0.0,
    val transactionHistory: List<String> = emptyList()
)

class GameViewModel(
    application: Application,
    private val repository: GameRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var countdownJob: Job? = null
    private var lastCrashValue: Double = 1.0

    // Virtual Names to simulate live multiplier action
    private val virtualNames = listOf(
        "Pasindu" to "🚀", "Siripala" to "👳", "Amanda" to "👩", "Kamal" to "👨",
        "Nimal" to "🧔", "Suresh" to "👷", "Pasindu.L" to "🔥", "Ruwan" to "🎲",
        "Chathura" to "⚡", "Roshel" to "💎", "Dilip" to "💫", "Priyantha" to "🎖️",
        "Hansani" to "🍀", "Saman_S" to "👑"
    )

    private val sharedPrefs by lazy {
        getApplication<Application>().getSharedPreferences("SalliUlpathaPrefs", android.content.Context.MODE_PRIVATE)
    }

    private fun loadHostData() {
        val profit = sharedPrefs.getFloat("host_profit", 55430.0f).toDouble()
        val collected = sharedPrefs.getFloat("host_collected", 18500.0f).toDouble()
        
        // Setup initial transactions if list empty
        val defaultHistory = listOf(
            "Siripala විසින් 5,500.00 K තැන්පත් කරන ලදී 📥",
            "Amanda විසින් 3,400.00 K ආපසු ගන්නා ලදී 📤",
            "Nimal විසින් 10,000.00 K තැන්පත් කරන ලදී 📥",
            "Roshel විසින් 1,500.00 K තැන්පත් කරන ලදී 📥",
            "Suresh විසින් 15,000.00 K ආපසු ගන්නා ලදී 📤"
        )
        
        _uiState.update { state ->
            state.copy(
                hostProfit = profit,
                hostTotalCollected = collected,
                transactionHistory = defaultHistory
            )
        }
    }

    private fun saveHostData(profit: Double, collected: Double) {
        sharedPrefs.edit()
            .putFloat("host_profit", profit.toFloat())
            .putFloat("host_collected", collected.toFloat())
            .apply()
    }

    init {
        loadHostData()
        viewModelScope.launch {
            // Ensure User Stats row exists in database
            val stats = repository.getOrInitUserStats()
            
            // Listen to database flows
            launch {
                repository.userStats.filterNotNull().collect { latestStats ->
                    _uiState.update { state ->
                        state.copy(
                            balance = latestStats.balance,
                            totalGamesPlayed = latestStats.totalGamesPlayed,
                            biggestWinMultiplier = latestStats.biggestWinMultiplier,
                            totalWonAmount = latestStats.totalWonAmount
                        )
                    }
                    checkDailyClaimStatus(latestStats.lastDailyRewardTime)
                }
            }

            // Start main game state machine loop
            startGameCycle()
        }
    }

    private fun checkDailyClaimStatus(lastClaimTime: Long) {
        val currentTime = System.currentTimeMillis()
        val difference = currentTime - lastClaimTime
        val oneDayMillis = 24 * 60 * 60 * 1000L

        if (difference >= oneDayMillis) {
            _uiState.update { it.copy(isDailyClaimAvailable = true, nextDailyClaimCountdown = "") }
        } else {
            val remainingMillis = oneDayMillis - difference
            val hours = remainingMillis / (1000 * 60 * 60)
            val minutes = (remainingMillis % (1000 * 60 * 60)) / (1000 * 60)
            val seconds = (remainingMillis % (1000 * 60)) / 1000
            val countdownStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            _uiState.update {
                it.copy(
                    isDailyClaimAvailable = false,
                    nextDailyClaimCountdown = countdownStr
                )
            }
        }
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            val stats = repository.getOrInitUserStats()
            val currentTime = System.currentTimeMillis()
            val oneDayMillis = 24 * 60 * 60 * 1000L

            if (currentTime - stats.lastDailyRewardTime >= oneDayMillis) {
                val updatedStats = stats.copy(
                    balance = stats.balance + 1000.0,
                    lastDailyRewardTime = currentTime
                )
                repository.updateUserStats(updatedStats)
            }
        }
    }

    private fun simulateVirtualTransactions() {
        val names = virtualNames.map { it.first }
        val emojis = virtualNames.map { it.second }
        val idx = (0 until names.size).random()
        val randomName = names[idx]
        val emoji = emojis[idx]
        val isDeposit = Math.random() < 0.6
        val amount = ((100..4500).random() / 50) * 50.0
        val text = if (isDeposit) {
            "$emoji $randomName විසින් ${String.format(Locale.US, "%,.2f", amount)} K තැන්පත් කරන ලදී 📥"
        } else {
            "$emoji $randomName විසින් ${String.format(Locale.US, "%,.2f", amount)} K ආපසු ගන්නා ලදී 📤"
        }
        _uiState.update { state ->
            val updatedHistory = (listOf(text) + state.transactionHistory).take(30)
            state.copy(transactionHistory = updatedHistory)
        }
    }

    private fun startGameCycle() {
        // Reset flight state and enter countdown
        countdownJob?.cancel()
        gameLoopJob?.cancel()

        _uiState.update { state ->
            state.copy(
                gameState = GameState.WAITING_FOR_BETS,
                countdownSeconds = 6.0,
                currentMultiplier = 1.00,
                hasCashedOutInCurrentRound = false,
                userCashoutMultiplier = 0.0,
                userWinnings = 0.0,
                liveBettors = generateLiveBettors()
            )
        }

        // Simulate some community deposit/withdraw activity!
        simulateVirtualTransactions()
        if (Math.random() < 0.5) {
            simulateVirtualTransactions()
        }

        // Automatic Place Bet if autoBet is checked and space allows
        if (_uiState.value.autoBetEnabled && !_uiState.value.hasPlacedBetInCurrentRound) {
            placeBet()
        }

        countdownJob = viewModelScope.launch {
            var remaining = 60 // 6.0 seconds * 10
            while (remaining > 0) {
                delay(100)
                remaining--
                _uiState.update { it.copy(countdownSeconds = remaining / 10.0) }
            }
            // Transition to Flying
            startFlight()
        }
    }

    private fun generateLiveBettors(): List<LiveBettor> {
        val qty = (6..12).random()
        val pool = virtualNames.shuffled()
        return List(qty) { index ->
            val identity = pool[index % pool.size]
            val bet = ((100..2500).random() / 50) * 50.0 // neat multiples of 50
            // Bettor cashout logic distributions: some low risk, some high risk
            val cashoutRoll = Math.random()
            val target = when {
                cashoutRoll < 0.40 -> 1.1 + (Math.random() * 0.4) // cash out low
                cashoutRoll < 0.75 -> 1.5 + (Math.random() * 1.5) // medium
                cashoutRoll < 0.95 -> 3.0 + (Math.random() * 7.0) // high cash out target
                else -> 10.0 + (Math.random() * 40.0) // bold target!
            }
            LiveBettor(
                name = identity.first,
                avatarEmoji = identity.second,
                betAmount = bet,
                targetMultiplier = (target * 100).roundToInt() / 100.0
            )
        }
    }

    private fun startFlight() {
        countdownJob?.cancel()
        
        // Generate random multiplier for this round (House edge: 3% instant crash at 1.0x)
        val rand = Math.random()
        val rawCrashMultiplier = when {
            rand < 0.03 -> 1.00 // Intensely frustrating, but authentic crash!
            rand < 0.40 -> 1.01 + (Math.random() * 0.5) // 1.01 to 1.50
            rand < 0.85 -> 1.50 + (Math.random() * 2.5) // 1.50 to 4.00
            rand < 0.97 -> 4.00 + (Math.random() * 15.0) // 4.00 to 19.00
            else -> 19.00 + (Math.random() * 120.0) // mega flight! 19 to 139.00
        }
        val crashMultiplier = (rawCrashMultiplier * 100).roundToInt() / 100.0
        lastCrashValue = crashMultiplier

        _uiState.update { state ->
            state.copy(
                gameState = GameState.FLYING,
                currentMultiplier = 1.00
            )
        }

        gameLoopJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            var current = 1.00

            while (current < crashMultiplier) {
                delay(30) // ~33 FPS
                val elapsed = System.currentTimeMillis() - startTime
                
                // Realistic flight multi progression curve (starts slow, then rockets exponentially!)
                current = if (elapsed < 3500) {
                    1.00 + (elapsed * 0.00028)
                } else {
                    1.98 + ((elapsed - 3500) * 0.0011).pow(1.9)
                }
                
                current = (current * 100).roundToInt() / 100.0

                if (current >= crashMultiplier) {
                    current = crashMultiplier
                }

                _uiState.update { it.copy(currentMultiplier = current) }

                // Check auto cashout
                if (_uiState.value.hasPlacedBetInCurrentRound &&
                    !_uiState.value.hasCashedOutInCurrentRound &&
                    _uiState.value.isAutoCashOutEnabled &&
                    current >= _uiState.value.autoCashOutMultiplier
                ) {
                    cashOut(targetMultiplier = _uiState.value.autoCashOutMultiplier)
                }

                // Update virtual player actions at this multiplier level
                updateVirtualBettors(current)
            }

            // Crash Event!
            triggerCrashState(crashMultiplier)
        }
    }

    private fun updateVirtualBettors(current: Double) {
        _uiState.update { state ->
            val updated = state.liveBettors.map { bettor ->
                if (bettor.status == "ACTIVE" && current >= bettor.targetMultiplier) {
                    bettor.copy(
                        cashoutMultiplier = bettor.targetMultiplier,
                        winAmount = (bettor.betAmount * bettor.targetMultiplier * 100).roundToInt() / 100.0,
                        status = "WON"
                    )
                } else {
                    bettor
                }
            }
            state.copy(liveBettors = updated)
        }
    }

    private suspend fun triggerCrashState(finalMultiplier: Double) {
        gameLoopJob?.cancel()

        // Sync local users list to LOST if did not cash out, calculate host profit of Salli Ulpatha
        _uiState.update { state ->
            val finalSimUsers = state.liveBettors.map { bettor ->
                if (bettor.status == "ACTIVE") {
                    bettor.copy(status = "LOST")
                } else {
                    bettor
                }
            }

            // Calculations for Salli Ulpatha host dynamics
            val simLostAmount = finalSimUsers.filter { it.status == "LOST" }.sumOf { it.betAmount }
            val simWonAmount = finalSimUsers.filter { it.status == "WON" }.sumOf { it.winAmount }

            // Include user loss to owner if crashed
            val userLostAmount = if (state.hasPlacedBetInCurrentRound && !state.hasCashedOutInCurrentRound) {
                state.betAmount
            } else 0.0

            val netHostChange = simLostAmount + userLostAmount - simWonAmount
            val newHostProfit = maxOf(0.0, state.hostProfit + netHostChange)
            val newHostTotalCollected = state.hostTotalCollected + if (netHostChange > 0) netHostChange else 0.0
            
            saveHostData(newHostProfit, newHostTotalCollected)

            state.copy(
                gameState = GameState.CRASHED,
                currentMultiplier = finalMultiplier,
                liveBettors = finalSimUsers,
                lastCrashMultiplier = finalMultiplier,
                hostProfit = newHostProfit,
                hostTotalCollected = newHostTotalCollected
            )
        }

        // If user was betting and didn't cash out, record loss
        if (_uiState.value.hasPlacedBetInCurrentRound && !_uiState.value.hasCashedOutInCurrentRound) {
            val stats = repository.getOrInitUserStats()
            val lostAmount = _uiState.value.betAmount

            // Save lost bet in room
            val betHist = BetHistory(
                betAmount = lostAmount,
                cashoutMultiplier = null,
                crashMultiplier = finalMultiplier,
                winAmount = 0.0
            )
            repository.saveBet(betHist)

            // Update stats
            val updatedStats = stats.copy(
                totalGamesPlayed = stats.totalGamesPlayed + 1
            )
            repository.updateUserStats(updatedStats)
        }

        // Save round multiplier to history
        repository.saveMultiplier(MultiplierHistory(multiplier = finalMultiplier))

        // Reset user bet in place indicator
        _uiState.update { it.copy(hasPlacedBetInCurrentRound = false) }

        // Cooldown for 3.5 seconds
        delay(3500)
        
        // Loop back next round
        startGameCycle()
    }

    fun placeBet() {
        val state = _uiState.value
        if (state.hasPlacedBetInCurrentRound || state.gameState != GameState.WAITING_FOR_BETS) return
        if (state.balance < state.betAmount) return

        viewModelScope.launch {
            val stats = repository.getOrInitUserStats()
            // Immediately deduct the bet amount from Room balance (realistic gambling feel!)
            val updatedStats = stats.copy(
                balance = stats.balance - state.betAmount
            )
            repository.updateUserStats(updatedStats)

            _uiState.update {
                it.copy(hasPlacedBetInCurrentRound = true)
            }
        }
    }

    fun cashOut(targetMultiplier: Double? = null) {
        val state = _uiState.value
        if (!state.hasPlacedBetInCurrentRound || state.hasCashedOutInCurrentRound || state.gameState != GameState.FLYING) return

        val cashoutVal = targetMultiplier ?: state.currentMultiplier
        val winnings = (state.betAmount * cashoutVal * 100).roundToInt() / 100.0

        viewModelScope.launch {
            val stats = repository.getOrInitUserStats()

            // Credit the account
            val updatedStats = stats.copy(
                balance = stats.balance + winnings,
                totalGamesPlayed = stats.totalGamesPlayed + 1,
                biggestWinMultiplier = maxOf(stats.biggestWinMultiplier, cashoutVal),
                totalWonAmount = stats.totalWonAmount + winnings
            )
            repository.updateUserStats(updatedStats)

            // Save won bet in history
            val betHist = BetHistory(
                betAmount = state.betAmount,
                cashoutMultiplier = cashoutVal,
                crashMultiplier = lastCrashValue, // filled when round ends or stored
                winAmount = winnings
            )
            repository.saveBet(betHist)

            _uiState.update {
                it.copy(
                    hasCashedOutInCurrentRound = true,
                    userCashoutMultiplier = cashoutVal,
                    userWinnings = winnings
                )
            }
        }
    }

    // Controls Adjustments
    fun setBetAmount(amount: Double) {
        val balance = _uiState.value.balance
        val cleaned = when {
            amount < 10.0 -> 10.0
            amount > balance -> maxOf(10.0, (balance / 10).roundToInt() * 10.0) // limit comfortably
            else -> (amount * 100).roundToInt() / 100.0
        }
        _uiState.update { it.copy(betAmount = cleaned) }
    }

    fun modifyBetAmount(delta: Double) {
        setBetAmount(_uiState.value.betAmount + delta)
    }

    fun doubleBet() {
        setBetAmount(_uiState.value.betAmount * 2.0)
    }

    fun halveBet() {
        setBetAmount(_uiState.value.betAmount / 2.0)
    }

    fun setAutoCashOutMultiplier(value: Double) {
        val cleaned = if (value < 1.01) 1.01 else if (value > 500.0) 500.0 else value
        _uiState.update { it.copy(autoCashOutMultiplier = (cleaned * 100).roundToInt() / 100.0) }
    }

    fun toggleAutoCashOut(enabled: Boolean) {
        _uiState.update { it.copy(isAutoCashOutEnabled = enabled) }
    }

    fun toggleAutoBet(enabled: Boolean) {
        _uiState.update { it.copy(autoBetEnabled = enabled) }
    }

    // Tick down clock if called externally (useful for background timer checks or UI binding)
    fun refreshDailyState() {
        viewModelScope.launch {
            val stats = repository.getOrInitUserStats()
            checkDailyClaimStatus(stats.lastDailyRewardTime)
        }
    }

    fun depositMoney(amount: Double) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            val stats = repository.getOrInitUserStats()
            val updatedStats = stats.copy(balance = stats.balance + amount)
            repository.updateUserStats(updatedStats)
            _uiState.update { state ->
                val text = "ඔබ විසින් K ${String.format(Locale.US, "%,.2f", amount)} මුදලක් සාර්ථකව තැන්පත් කරන ලදී! 📥 ✅"
                state.copy(transactionHistory = (listOf(text) + state.transactionHistory).take(30))
            }
        }
    }

    fun withdrawMoney(amount: Double): Boolean {
        if (amount <= 0.0) return false
        val currentBalance = _uiState.value.balance
        if (currentBalance < amount) return false

        viewModelScope.launch {
            val stats = repository.getOrInitUserStats()
            val updatedStats = stats.copy(balance = stats.balance - amount)
            repository.updateUserStats(updatedStats)
            _uiState.update { state ->
                val text = "ඔබ විසින් K ${String.format(Locale.US, "%,.2f", amount)} මුදලක් සාර්ථකව ලබා ගන්නා ලදී! 📤 💸"
                state.copy(transactionHistory = (listOf(text) + state.transactionHistory).take(30))
            }
        }
        return true
    }

    fun claimHostEarnings() {
        val earnings = _uiState.value.hostProfit
        if (earnings <= 0.0) return

        viewModelScope.launch {
            val stats = repository.getOrInitUserStats()
            val updatedStats = stats.copy(balance = stats.balance + earnings)
            repository.updateUserStats(updatedStats)

            // Reset host balance
            saveHostData(0.0, _uiState.value.hostTotalCollected)

            _uiState.update { state ->
                val text = "ලාභය ලබා ගැනීම: සන්නාම හිමිකරුගේ අරමුදලෙන් K ${String.format(Locale.US, "%,.2f", earnings)} ක් ඔබගේ ප්‍රධාන පසුම්බියට එක්විය! 👑"
                state.copy(
                    hostProfit = 0.0,
                    transactionHistory = (listOf(text) + state.transactionHistory).take(30)
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        gameLoopJob?.cancel()
    }
}
