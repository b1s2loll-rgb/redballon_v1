package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.DailyMission
import com.example.model.MissionType
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GameStats(
    val highScore: Long = 0L,
    val maxAltitudeMeters: Long = 0L,
    val gamesPlayed: Int = 0,
    val totalTimeSeconds: Long = 0L,
    val totalCoins: Int = 0
)

class ScoreRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pop_the_balloon_prefs", Context.MODE_PRIVATE)

    fun getStats(): GameStats {
        return GameStats(
            highScore = prefs.getLong(KEY_HIGH_SCORE, 0L),
            maxAltitudeMeters = prefs.getLong(KEY_MAX_ALTITUDE, 0L),
            gamesPlayed = prefs.getInt(KEY_GAMES_PLAYED, 0),
            totalTimeSeconds = prefs.getLong(KEY_TOTAL_TIME, 0L),
            totalCoins = prefs.getInt(KEY_TOTAL_COINS, 0)
        )
    }

    fun getMaxAltitude(): Long = prefs.getLong(KEY_MAX_ALTITUDE, 0L)

    fun saveGameResult(score: Long, altitudeMeters: Long, timeSeconds: Long): Boolean {
        val currentHigh = prefs.getLong(KEY_HIGH_SCORE, 0L)
        val currentMaxAlt = prefs.getLong(KEY_MAX_ALTITUDE, 0L)
        val currentPlayed = prefs.getInt(KEY_GAMES_PLAYED, 0)
        val currentTime = prefs.getLong(KEY_TOTAL_TIME, 0L)

        val isNewHighScore = score > currentHigh

        prefs.edit().apply {
            if (isNewHighScore) {
                putLong(KEY_HIGH_SCORE, score)
            }
            if (altitudeMeters > currentMaxAlt) {
                putLong(KEY_MAX_ALTITUDE, altitudeMeters)
            }
            putInt(KEY_GAMES_PLAYED, currentPlayed + 1)
            putLong(KEY_TOTAL_TIME, currentTime + timeSeconds)
            apply()
        }

        return isNewHighScore
    }

    fun getCoins(): Int = prefs.getInt(KEY_TOTAL_COINS, 0)

    fun addCoins(amount: Int): Int {
        val current = getCoins()
        val updated = (current + amount).coerceAtLeast(0)
        prefs.edit().putInt(KEY_TOTAL_COINS, updated).apply()
        return updated
    }

    fun spendCoins(amount: Int): Boolean {
        val current = getCoins()
        if (current >= amount) {
            prefs.edit().putInt(KEY_TOTAL_COINS, current - amount).apply()
            return true
        }
        return false
    }

    fun getUnlockedSkinIds(): Set<String> {
        val raw = prefs.getStringSet(KEY_UNLOCKED_SKINS, null)
        return raw?.toSet() ?: setOf("classic_shadow")
    }

    fun unlockSkin(skinId: String): Boolean {
        val current = getUnlockedSkinIds().toMutableSet()
        current.add(skinId)
        prefs.edit().putStringSet(KEY_UNLOCKED_SKINS, current).apply()
        return true
    }

    fun isSkinUnlocked(skinId: String): Boolean {
        if (skinId == "classic_shadow") return true
        return getUnlockedSkinIds().contains(skinId)
    }

    fun getSelectedSkinId(): String = prefs.getString(KEY_SELECTED_SKIN, "classic_shadow") ?: "classic_shadow"

    fun setSelectedSkinId(skinId: String) {
        prefs.edit().putString(KEY_SELECTED_SKIN, skinId).apply()
    }

    fun isSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND_ENABLED, true)
    fun setSoundEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()

    fun isMusicEnabled(): Boolean = prefs.getBoolean(KEY_MUSIC_ENABLED, true)
    fun setMusicEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply()

    fun isHapticsEnabled(): Boolean = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
    fun setHapticsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()

    // ==========================================
    // DAILY MISSIONS SYSTEM
    // ==========================================

    private fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun getDailyMissions(): List<DailyMission> {
        val today = getTodayDateKey()
        val savedDate = prefs.getString(KEY_DAILY_DATE, "")

        if (savedDate != today) {
            // New day: generate fresh daily missions
            val freshMissions = generateFreshDailyMissions()
            saveDailyMissions(today, freshMissions, grandBonusClaimed = false)
            return freshMissions
        }

        val jsonStr = prefs.getString(KEY_DAILY_MISSIONS_JSON, null)
        if (jsonStr.isNullOrEmpty()) {
            val freshMissions = generateFreshDailyMissions()
            saveDailyMissions(today, freshMissions, grandBonusClaimed = false)
            return freshMissions
        }

        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<DailyMission>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    DailyMission(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.getString("description"),
                        missionType = MissionType.valueOf(obj.getString("missionType")),
                        targetProgress = obj.getInt("targetProgress"),
                        currentProgress = obj.getInt("currentProgress"),
                        coinReward = obj.getInt("coinReward"),
                        isCompleted = obj.getBoolean("isCompleted"),
                        isClaimed = obj.getBoolean("isClaimed"),
                        iconEmoji = obj.optString("iconEmoji", "🎯")
                    )
                )
            }
            list
        } catch (e: Exception) {
            val freshMissions = generateFreshDailyMissions()
            saveDailyMissions(today, freshMissions, grandBonusClaimed = false)
            freshMissions
        }
    }

    private fun generateFreshDailyMissions(): List<DailyMission> {
        val currentMaxAlt = getMaxAltitude()
        val recordTarget = if (currentMaxAlt > 0) {
            (currentMaxAlt + 50).toInt().coerceAtLeast(600)
        } else {
            500
        }

        return listOf(
            DailyMission(
                id = "mission_fly_500m",
                title = "Fly for 500 meters",
                description = "Ascend to an altitude of at least 500 meters in a single flight",
                missionType = MissionType.FLY_DISTANCE,
                targetProgress = 500,
                currentProgress = 0,
                coinReward = 2,
                iconEmoji = "🚀"
            ),
            DailyMission(
                id = "mission_break_record",
                title = "Can you break your record?",
                description = if (currentMaxAlt > 0) {
                    "Surpass your personal record of ${currentMaxAlt}m (reach ${recordTarget}m)!"
                } else {
                    "Set your very first aeronaut high altitude record beyond 500m!"
                },
                missionType = MissionType.BREAK_RECORD,
                targetProgress = recordTarget,
                currentProgress = 0,
                coinReward = 3,
                iconEmoji = "🏆"
            ),
            DailyMission(
                id = "mission_dodge_hazards",
                title = "Atmospheric Navigator",
                description = "Evade 12 storm clouds, asteroids, and weather hazards today",
                missionType = MissionType.DODGE_HAZARDS,
                targetProgress = 12,
                currentProgress = 0,
                coinReward = 2,
                iconEmoji = "⚡"
            ),
            DailyMission(
                id = "mission_play_flights",
                title = "Persistent Aviator",
                description = "Launch 3 separate balloon flights today",
                missionType = MissionType.PLAY_FLIGHTS,
                targetProgress = 3,
                currentProgress = 0,
                coinReward = 2,
                iconEmoji = "🎈"
            )
        )
    }

    private fun saveDailyMissions(dateKey: String, missions: List<DailyMission>, grandBonusClaimed: Boolean) {
        val array = JSONArray()
        for (m in missions) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("title", m.title)
                put("description", m.description)
                put("missionType", m.missionType.name)
                put("targetProgress", m.targetProgress)
                put("currentProgress", m.currentProgress)
                put("coinReward", m.coinReward)
                put("isCompleted", m.isCompleted)
                put("isClaimed", m.isClaimed)
                put("iconEmoji", m.iconEmoji)
            }
            array.put(obj)
        }

        prefs.edit().apply {
            putString(KEY_DAILY_DATE, dateKey)
            putString(KEY_DAILY_MISSIONS_JSON, array.toString())
            putBoolean(KEY_DAILY_GRAND_BONUS_CLAIMED, grandBonusClaimed)
            apply()
        }
    }

    /**
     * Updates mission progress for a specific mission type.
     * Returns a list of missions that were newly completed by this update.
     */
    fun recordMissionProgress(type: MissionType, progressValue: Int, isIncremental: Boolean = true): List<DailyMission> {
        val missions = getDailyMissions().toMutableList()
        val newlyCompleted = mutableListOf<DailyMission>()
        var modified = false

        for (i in missions.indices) {
            val mission = missions[i]
            if (mission.missionType == type && !mission.isCompleted) {
                val newProgress = if (isIncremental) {
                    (mission.currentProgress + progressValue).coerceAtMost(mission.targetProgress)
                } else {
                    progressValue.coerceAtLeast(mission.currentProgress).coerceAtMost(mission.targetProgress)
                }

                if (newProgress != mission.currentProgress) {
                    val completed = newProgress >= mission.targetProgress
                    val updatedMission = mission.copy(
                        currentProgress = newProgress,
                        isCompleted = completed
                    )
                    missions[i] = updatedMission
                    modified = true

                    if (completed) {
                        newlyCompleted.add(updatedMission)
                    }
                }
            }
        }

        if (modified) {
            saveDailyMissions(getTodayDateKey(), missions, isDailyGrandBonusClaimed())
        }

        return newlyCompleted
    }

    /**
     * Claims a completed mission's coin reward.
     * Returns the number of coins granted (or 0 if already claimed/not completed).
     */
    fun claimMissionReward(missionId: String): Int {
        val missions = getDailyMissions().toMutableList()
        val index = missions.indexOfFirst { it.id == missionId }
        if (index == -1) return 0

        val mission = missions[index]
        if (mission.isCompleted && !mission.isClaimed) {
            missions[index] = mission.copy(isClaimed = true)
            saveDailyMissions(getTodayDateKey(), missions, isDailyGrandBonusClaimed())
            addCoins(mission.coinReward)
            return mission.coinReward
        }
        return 0
    }

    fun isDailyGrandBonusClaimed(): Boolean {
        val today = getTodayDateKey()
        val savedDate = prefs.getString(KEY_DAILY_DATE, "")
        if (savedDate != today) return false
        return prefs.getBoolean(KEY_DAILY_GRAND_BONUS_CLAIMED, false)
    }

    fun claimDailyGrandBonus(bonusCoins: Int = 5): Boolean {
        val missions = getDailyMissions()
        val allClaimed = missions.all { it.isClaimed }
        if (allClaimed && !isDailyGrandBonusClaimed()) {
            prefs.edit().putBoolean(KEY_DAILY_GRAND_BONUS_CLAIMED, true).apply()
            addCoins(bonusCoins)
            return true
        }
        return false
    }

    fun hasUnclaimedDailyMissions(): Boolean {
        val missions = getDailyMissions()
        val hasUnclaimedRegular = missions.any { it.isCompleted && !it.isClaimed }
        val canClaimGrand = missions.all { it.isClaimed } && !isDailyGrandBonusClaimed()
        return hasUnclaimedRegular || canClaimGrand
    }

    fun getDailyCompletionCount(): Pair<Int, Int> {
        val missions = getDailyMissions()
        val completed = missions.count { it.isCompleted || it.isClaimed }
        return Pair(completed, missions.size)
    }

    companion object {
        private const val KEY_HIGH_SCORE = "key_high_score"
        private const val KEY_MAX_ALTITUDE = "key_max_alt"
        private const val KEY_GAMES_PLAYED = "key_games_played"
        private const val KEY_TOTAL_TIME = "key_total_time"
        private const val KEY_TOTAL_COINS = "key_total_coins"
        private const val KEY_UNLOCKED_SKINS = "key_unlocked_skins"
        private const val KEY_SELECTED_SKIN = "key_selected_skin"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_MUSIC_ENABLED = "key_music_enabled"
        private const val KEY_HAPTICS_ENABLED = "key_haptics_enabled"
        private const val KEY_DAILY_DATE = "key_daily_date"
        private const val KEY_DAILY_MISSIONS_JSON = "key_daily_missions_json"
        private const val KEY_DAILY_GRAND_BONUS_CLAIMED = "key_daily_grand_bonus_claimed"
    }
}
