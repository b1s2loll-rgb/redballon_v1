package com.example.model

enum class MissionType {
    FLY_DISTANCE,         // Fly for X meters in a single flight (e.g., 500m)
    BREAK_RECORD,         // "Can you break your record?" (Reach new high altitude or ascend >= target meters)
    REACH_ALTITUDE,       // Reach altitude milestone (e.g., 1,500m)
    PLAY_FLIGHTS,         // Complete X flight attempts today
    SURVIVE_DURATION,     // Stay airborne for X seconds in a single run
    DODGE_HAZARDS         // Dodge / pass X obstacles and storm clouds
}

data class DailyMission(
    val id: String,
    val title: String,
    val description: String,
    val missionType: MissionType,
    val targetProgress: Int,
    val currentProgress: Int = 0,
    val coinReward: Int = 2,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    val iconEmoji: String = "🎯"
) {
    val progressFraction: Float
        get() = if (targetProgress <= 0) 1f else (currentProgress.toFloat() / targetProgress.toFloat()).coerceIn(0f, 1f)
}
