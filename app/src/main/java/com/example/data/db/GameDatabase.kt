package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val balance: Double = 10000.0,
    val totalGamesPlayed: Int = 0,
    val biggestWinMultiplier: Double = 0.0,
    val totalWonAmount: Double = 0.0,
    val lastDailyRewardTime: Long = 0L
)

@Entity(tableName = "bet_history")
data class BetHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val betAmount: Double,
    val cashoutMultiplier: Double?, // null if did not cash out (crashed)
    val crashMultiplier: Double,
    val winAmount: Double, // 0.0 if did not cash out
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "multiplier_history")
data class MultiplierHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val multiplier: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface GameDao {
    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    fun getUserStats(): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserStats(stats: UserStats)

    @Query("SELECT * FROM bet_history ORDER BY timestamp DESC LIMIT 30")
    fun getBetHistory(): Flow<List<BetHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBet(bet: BetHistory)

    @Query("SELECT * FROM multiplier_history ORDER BY timestamp DESC LIMIT 20")
    fun getMultiplierHistory(): Flow<List<MultiplierHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiplier(multi: MultiplierHistory)
}

@Database(entities = [UserStats::class, BetHistory::class, MultiplierHistory::class], version = 1, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
