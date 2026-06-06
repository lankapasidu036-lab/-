package com.example.data.repository

import com.example.data.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GameRepository(private val gameDao: GameDao) {

    val userStats: Flow<UserStats?> = gameDao.getUserStats()
    val betHistory: Flow<List<BetHistory>> = gameDao.getBetHistory()
    val multiplierHistory: Flow<List<MultiplierHistory>> = gameDao.getMultiplierHistory()

    suspend fun getOrInitUserStats(): UserStats {
        val current = gameDao.getUserStats().first()
        if (current == null) {
            val fresh = UserStats()
            gameDao.insertOrUpdateUserStats(fresh)
            return fresh
        }
        return current
    }

    suspend fun updateUserStats(stats: UserStats) {
        gameDao.insertOrUpdateUserStats(stats)
    }

    suspend fun saveBet(bet: BetHistory) {
        gameDao.insertBet(bet)
    }

    suspend fun saveMultiplier(multi: MultiplierHistory) {
        gameDao.insertMultiplier(multi)
    }
}
