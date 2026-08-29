package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.North
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ScoreRepository
import com.example.engine.GameEngine
import com.example.model.GameStatus
import com.example.sound.GameSoundManager
import com.example.ui.theme.BalloonRed
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.DeepGray
import com.example.ui.theme.LightSilver
import com.example.ui.theme.MidGray
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGray
import java.text.NumberFormat
import java.util.Locale

enum class ActiveMenuDialog {
    NONE,
    DAILY_MISSIONS,
    HOW_TO_PLAY,
    WARDROBE,
    SETTINGS,
    WATCH_AD_COINS
}

@Composable
fun GameScreen() {
    val context = LocalContext.current
    val scoreRepository = remember { ScoreRepository(context) }
    val soundManager = remember {
        GameSoundManager(context).apply {
            isSoundEnabled = scoreRepository.isSoundEnabled()
            isMusicEnabled = scoreRepository.isMusicEnabled()
            isHapticsEnabled = scoreRepository.isHapticsEnabled()
        }
    }
    val engine = remember { GameEngine(scoreRepository, soundManager) }

    val uiState by engine.uiState.collectAsState()
    var soundEnabled by remember { mutableStateOf(soundManager.isSoundEnabled) }
    var musicEnabled by remember { mutableStateOf(soundManager.isMusicEnabled) }
    var hapticsEnabled by remember { mutableStateOf(soundManager.isHapticsEnabled) }
    var activeDialog by remember { mutableStateOf(ActiveMenuDialog.NONE) }

    var lastFrameTimeNanos by remember { mutableLongStateOf(0L) }
    var frameTick by remember { mutableLongStateOf(0L) }

    // Continuous 60/120 FPS game loop
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTimeNanos != 0L) {
                    val dt = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
                    engine.update(dt)
                }
                lastFrameTimeNanos = frameTimeNanos
                frameTick++
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        LaunchedEffect(widthPx, heightPx) {
            engine.setScreenDimensions(widthPx, heightPx)
        }

        // Game canvas with touch input: hold/drag to steer 360°, release to hover in place
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_touch_area")
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val currentStatus = engine.uiState.value.status
                        if (currentStatus == GameStatus.PLAYING) {
                            engine.setTouchInput(isHolding = true, touchX = down.position.x, touchY = down.position.y)
                        }

                        while (true) {
                            val event = awaitPointerEvent()
                            val anyPressed = event.changes.any { it.pressed }
                            if (!anyPressed) {
                                engine.setTouchInput(isHolding = false)
                                break
                            }
                            val active = event.changes.firstOrNull { it.pressed }
                            if (active != null) {
                                if (engine.uiState.value.status == GameStatus.PLAYING) {
                                    engine.setTouchInput(isHolding = true, touchX = active.position.x, touchY = active.position.y)
                                }
                                active.consume()
                            }
                        }
                    }
                }
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("game_canvas")
            ) {
                val tick = frameTick
                if (tick >= 0) {
                    GameRenderer.drawGame(this, engine)
                }
            }
        }

        // TOP HUD (Altitude, Score, 3 Lives Counter, Coins, Records, Pause & Sound Controls)
        TopHUD(
            score = uiState.score,
            altitudeMeters = uiState.altitudeMeters,
            lives = uiState.lives,
            adRevivesRemaining = uiState.adRevivesRemaining,
            totalCoins = uiState.totalCoins,
            maxAltitudeEver = uiState.maxAltitudeEver,
            onPauseClick = { engine.pauseGame() },
            onOpenWardrobe = { activeDialog = ActiveMenuDialog.WARDROBE },
            isGameActive = (uiState.status == GameStatus.PLAYING || uiState.status == GameStatus.TAKEOFF),
            soundEnabled = soundEnabled,
            onToggleSound = {
                soundEnabled = !soundEnabled
                soundManager.isSoundEnabled = soundEnabled
                scoreRepository.setSoundEnabled(soundEnabled)
            }
        )

        // Active Power-Ups HUD Indicators
        if (uiState.status == GameStatus.PLAYING && (uiState.activeShieldTime > 0f || uiState.activeSpeedBoostTime > 0f)) {
            ActivePowerUpsHUD(
                activeShieldTime = uiState.activeShieldTime,
                maxShieldTime = uiState.maxShieldTime,
                activeSpeedBoostTime = uiState.activeSpeedBoostTime,
                maxSpeedBoostTime = uiState.maxSpeedBoostTime,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 62.dp)
            )
        }

        // Active Electrified Status Warning Indicator
        if (uiState.status == GameStatus.PLAYING && uiState.isElectrified) {
            Surface(
                color = Color(0xFF0D1B2A).copy(alpha = 0.92f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = if (uiState.activeShieldTime > 0f || uiState.activeSpeedBoostTime > 0f) 100.dp else 66.dp)
                    .testTag("electrified_status_hud")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⚡",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "ELECTRIFIED: SLOWED DOWN (${String.format(Locale.US, "%.1fs", uiState.electrifiedTimeRemaining)})",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Tier Milestone Notification Banner
        AnimatedVisibility(
            visible = uiState.tierBannerText != null,
            enter = fadeIn() + slideInVertically { -40 },
            exit = fadeOut() + slideOutVertically { -40 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 70.dp)
        ) {
            uiState.tierBannerText?.let { bannerText ->
                TierBanner(text = bannerText)
            }
        }

        // PLAYING: Mobility Hint banner for new players
        if (uiState.status == GameStatus.PLAYING && uiState.altitudeMeters < 90L) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 110.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    color = PureBlack.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "HOLD & DRAG to move up, down, left, right • RELEASE to hover",
                        color = PureWhite,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // READY STATE: Interactive Main Menu with Start, How to Play, Skins Wardrobe & Settings
        if (uiState.status == GameStatus.READY) {
            val dailyMissions = scoreRepository.getDailyMissions()
            val completedCount = dailyMissions.count { it.isCompleted || it.isClaimed }
            val hasUnclaimed = dailyMissions.any { it.isCompleted && !it.isClaimed } || (dailyMissions.all { it.isClaimed } && !scoreRepository.isDailyGrandBonusClaimed())

            MainMenuOverlay(
                totalCoins = uiState.totalCoins,
                maxAltitudeEver = uiState.maxAltitudeEver,
                currentSkin = uiState.currentSkin,
                dailyMissionsCompleted = completedCount,
                dailyMissionsTotal = dailyMissions.size,
                hasUnclaimedDailyMissions = hasUnclaimed,
                soundEnabled = soundEnabled,
                onToggleSound = {
                    soundEnabled = !soundEnabled
                    soundManager.isSoundEnabled = soundEnabled
                    scoreRepository.setSoundEnabled(soundEnabled)
                },
                onStartFlight = { engine.startTakeoff() },
                onOpenDailyMissions = { activeDialog = ActiveMenuDialog.DAILY_MISSIONS },
                onOpenHowToPlay = { activeDialog = ActiveMenuDialog.HOW_TO_PLAY },
                onOpenWardrobe = { activeDialog = ActiveMenuDialog.WARDROBE },
                onOpenSettings = { activeDialog = ActiveMenuDialog.SETTINGS },
                onWatchAdForCoins = { activeDialog = ActiveMenuDialog.WATCH_AD_COINS }
            )
        }

        // Active Dialogs / Modals
        when (activeDialog) {
            ActiveMenuDialog.DAILY_MISSIONS -> {
                DailyMissionsDialog(
                    missions = scoreRepository.getDailyMissions(),
                    totalCoins = uiState.totalCoins,
                    isGrandBonusClaimed = scoreRepository.isDailyGrandBonusClaimed(),
                    onClaimMission = { missionId ->
                        val reward = scoreRepository.claimMissionReward(missionId)
                        if (reward > 0) {
                            engine.refreshCoins()
                            soundManager.playCoinEarned()
                            soundManager.vibrateLifeGain()
                        }
                    },
                    onClaimGrandBonus = {
                        val success = scoreRepository.claimDailyGrandBonus()
                        if (success) {
                            engine.refreshCoins()
                            soundManager.playCoinEarned()
                            soundManager.vibrateLifeGain()
                        }
                    },
                    onDismiss = { activeDialog = ActiveMenuDialog.NONE }
                )
            }
            ActiveMenuDialog.HOW_TO_PLAY -> {
                HowToPlayDialog(onDismiss = { activeDialog = ActiveMenuDialog.NONE })
            }
            ActiveMenuDialog.WARDROBE -> {
                WardrobeSkinsDialog(
                    totalCoins = uiState.totalCoins,
                    currentSkin = uiState.currentSkin,
                    unlockedSkinIds = scoreRepository.getUnlockedSkinIds(),
                    onEquipSkin = { skin -> engine.equipSkin(skin) },
                    onUnlockSkin = { skin -> engine.unlockSkin(skin) },
                    onWatchAdForCoins = { activeDialog = ActiveMenuDialog.WATCH_AD_COINS },
                    onDismiss = { activeDialog = ActiveMenuDialog.NONE }
                )
            }
            ActiveMenuDialog.SETTINGS -> {
                SettingsDialog(
                    soundEnabled = soundEnabled,
                    onToggleSound = { enabled ->
                        soundEnabled = enabled
                        soundManager.isSoundEnabled = enabled
                        scoreRepository.setSoundEnabled(enabled)
                    },
                    musicEnabled = musicEnabled,
                    onToggleMusic = { enabled ->
                        musicEnabled = enabled
                        soundManager.isMusicEnabled = enabled
                        scoreRepository.setMusicEnabled(enabled)
                    },
                    hapticsEnabled = hapticsEnabled,
                    onToggleHaptics = { enabled ->
                        hapticsEnabled = enabled
                        soundManager.isHapticsEnabled = enabled
                        scoreRepository.setHapticsEnabled(enabled)
                    },
                    maxAltitudeEver = uiState.maxAltitudeEver,
                    totalCoins = uiState.totalCoins,
                    onDismiss = { activeDialog = ActiveMenuDialog.NONE }
                )
            }
            ActiveMenuDialog.WATCH_AD_COINS -> {
                MenuAdRewardDialog(
                    onRewardEarned = {
                        engine.addAdBonusCoins(1)
                        activeDialog = ActiveMenuDialog.NONE
                    },
                    onDismiss = { activeDialog = ActiveMenuDialog.NONE }
                )
            }
            ActiveMenuDialog.NONE -> {
                // No dialog
            }
        }

        // PAUSE OVERLAY
        if (uiState.status == GameStatus.PAUSED) {
            PauseOverlay(
                score = uiState.score,
                altitudeMeters = uiState.altitudeMeters,
                lives = uiState.lives,
                onResume = { engine.resumeGame() },
                onRestart = { engine.startTakeoff() },
                onGoToMainMenu = { engine.goToReadyMenu() },
                soundEnabled = soundEnabled,
                onToggleSound = {
                    soundEnabled = !soundEnabled
                    soundManager.isSoundEnabled = soundEnabled
                    scoreRepository.setSoundEnabled(soundEnabled)
                },
                musicEnabled = musicEnabled,
                onToggleMusic = {
                    musicEnabled = !musicEnabled
                    soundManager.isMusicEnabled = musicEnabled
                    scoreRepository.setMusicEnabled(musicEnabled)
                },
                hapticsEnabled = hapticsEnabled,
                onToggleHaptics = {
                    hapticsEnabled = !hapticsEnabled
                    soundManager.isHapticsEnabled = hapticsEnabled
                    scoreRepository.setHapticsEnabled(hapticsEnabled)
                }
            )
        }

        // AD REVIVE PROMPT MODAL (3 lives lost, chance to watch ad up to 5 times)
        if (uiState.status == GameStatus.AD_REVIVE_PROMPT) {
            AdRevivePromptModal(
                altitudeMeters = uiState.altitudeMeters,
                score = uiState.score,
                adRevivesRemaining = uiState.adRevivesRemaining,
                onWatchAd = { engine.startWatchingAd() },
                onDecline = { engine.declineAdRevive() }
            )
        }

        // WATCHING AD SIMULATION MODAL
        if (uiState.status == GameStatus.WATCHING_AD) {
            WatchingAdModal(
                onAdCompleted = { engine.reviveFromAd() },
                onDecline = { engine.declineAdRevive() }
            )
        }

        // GAME OVER MODAL
        if (uiState.status == GameStatus.GAME_OVER) {
            GameOverModal(
                score = uiState.score,
                altitudeMeters = uiState.altitudeMeters,
                coinsEarnedThisRun = uiState.coinsEarnedThisRun,
                totalCoins = uiState.totalCoins,
                maxAltitudeEver = uiState.maxAltitudeEver,
                onRestart = { engine.startTakeoff() },
                onGoToMainMenu = { engine.goToReadyMenu() }
            )
        }
    }
}

@Composable
fun TopHUD(
    score: Long,
    altitudeMeters: Long,
    lives: Int,
    adRevivesRemaining: Int,
    totalCoins: Int,
    maxAltitudeEver: Long,
    onPauseClick: () -> Unit,
    onOpenWardrobe: () -> Unit,
    isGameActive: Boolean,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isGameActive) {
            // Altitude
            Surface(
                color = PureWhite.copy(alpha = 0.92f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PureBlack),
                modifier = Modifier.padding(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.North,
                        contentDescription = null,
                        tint = PureBlack,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${formatter.format(altitudeMeters)}m",
                        color = PureBlack,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Center: 3-Life Display & Score
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 3 Lives Hearts Container
                Surface(
                    color = PureWhite.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PureBlack),
                    modifier = Modifier
                        .shadow(3.dp, RoundedCornerShape(14.dp))
                        .testTag("lives_container")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..3) {
                            val isAlive = i <= lives
                            Icon(
                                imageVector = if (isAlive) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Life $i",
                                tint = if (isAlive) BalloonRed else Color(0xFFC0C0C8),
                                modifier = Modifier
                                    .size(17.dp)
                                    .testTag("life_indicator_$i")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Score Pill
                Surface(
                    color = Color(0xFF2B2B33),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = formatter.format(score),
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            }

            // Pause Button during flight
            IconButton(
                onClick = onPauseClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = PureWhite,
                    contentColor = PureBlack
                ),
                modifier = Modifier
                    .size(44.dp)
                    .border(1.5.dp, PureBlack, CircleShape)
                    .testTag("pause_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause Flight",
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            // Main Menu Top HUD
            // Coin Balance Pill (clickable to open wardrobe)
            Surface(
                onClick = onOpenWardrobe,
                color = PureWhite.copy(alpha = 0.95f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PureBlack),
                modifier = Modifier
                    .shadow(3.dp, RoundedCornerShape(14.dp))
                    .testTag("top_coins_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🪙", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$totalCoins COINS",
                        color = PureBlack,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Max Altitude Pill
            Surface(
                color = Color(0xFFF4F4F8),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCDCE4)),
                modifier = Modifier.testTag("top_max_altitude_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏆", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RECORD: ${formatter.format(maxAltitudeEver)}m",
                        color = DarkCharcoal,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Quick Sound Toggle
            IconButton(
                onClick = onToggleSound,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = PureWhite,
                    contentColor = if (soundEnabled) PureBlack else MidGray
                ),
                modifier = Modifier
                    .size(42.dp)
                    .border(1.5.dp, if (soundEnabled) PureBlack else Color(0xFFC0C0C8), CircleShape)
                    .testTag("top_sound_toggle_button")
            ) {
                Icon(
                    imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = if (soundEnabled) "Mute Sound" else "Unmute Sound",
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
fun ActivePowerUpsHUD(
    activeShieldTime: Float,
    maxShieldTime: Float,
    activeSpeedBoostTime: Float,
    maxSpeedBoostTime: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (activeShieldTime > 0f) {
            val shieldProgress = (activeShieldTime / maxShieldTime).coerceIn(0f, 1f)
            Surface(
                color = PureWhite.copy(alpha = 0.95f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PureBlack),
                modifier = Modifier.shadow(4.dp, RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF00B0FF), CircleShape)
                            .border(1.dp, PureBlack, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SHIELD ${String.format(Locale.US, "%.1fs", activeShieldTime)}",
                        color = PureBlack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        if (activeSpeedBoostTime > 0f) {
            val speedProgress = (activeSpeedBoostTime / maxSpeedBoostTime).coerceIn(0f, 1f)
            Surface(
                color = PureWhite.copy(alpha = 0.95f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PureBlack),
                modifier = Modifier.shadow(4.dp, RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFFFFB300), CircleShape)
                            .border(1.dp, PureBlack, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SPEED ${String.format(Locale.US, "%.1fs", activeSpeedBoostTime)}",
                        color = PureBlack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun TierBanner(text: String) {
    Surface(
        color = Color(0xFF2B2B33).copy(alpha = 0.95f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PureWhite.copy(alpha = 0.8f)),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .shadow(10.dp, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                color = PureWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GameOverModal(
    score: Long,
    altitudeMeters: Long,
    coinsEarnedThisRun: Int,
    totalCoins: Int,
    maxAltitudeEver: Long,
    onRestart: () -> Unit,
    onGoToMainMenu: () -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val isNewRecord = altitudeMeters >= maxAltitudeEver && altitudeMeters > 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack.copy(alpha = 0.65f))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(22.dp),
            border = androidx.compose.foundation.BorderStroke(2.5.dp, PureBlack),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp)
                .shadow(20.dp, RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BALLOON POPPED!",
                    color = PureBlack,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "The red balloon burst in the sky",
                    color = MidGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Box
                Surface(
                    color = Color(0xFFF4F4F8),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCDCE4)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("FINAL SCORE", color = MidGray, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Text(
                                formatter.format(score),
                                color = PureBlack,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E2E8)))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ALTITUDE CLIMBED", color = MidGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isNewRecord) {
                                    Surface(color = Color(0xFF2E7D32), shape = RoundedCornerShape(4.dp)) {
                                        Text(
                                            "NEW RECORD!",
                                            color = PureWhite,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    "${formatter.format(altitudeMeters)} m",
                                    color = PureBlack,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E2E8)))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("COINS REWARD", color = MidGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "+$coinsEarnedThisRun 🪙 (Total: $totalCoins)",
                                color = Color(0xFFE65100),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Play Again Button
                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2B2B33),
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("try_again_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLAY AGAIN",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Go to Main Menu
                OutlinedButton(
                    onClick = onGoToMainMenu,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PureBlack),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PureBlack),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("game_over_menu_button")
                ) {
                    Text(
                        text = "MAIN MENU & WARDROBE",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PauseOverlay(
    score: Long,
    altitudeMeters: Long,
    lives: Int,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onGoToMainMenu: () -> Unit,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    musicEnabled: Boolean,
    onToggleMusic: () -> Unit,
    hapticsEnabled: Boolean,
    onToggleHaptics: () -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    Dialog(onDismissRequest = onResume) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(22.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, PureBlack),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .shadow(16.dp, RoundedCornerShape(22.dp))
                .testTag("pause_dialog_card")
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pause Header Badge
                Surface(
                    color = Color(0xFFF4F4F8),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PureBlack),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Game Paused",
                            tint = PureBlack,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "GAME PAUSED",
                    color = PureBlack,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Flight physics and obstacle timers suspended",
                    color = MidGray,
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Current Flight Stats
                Surface(
                    color = Color(0xFFF4F4F8),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCDCE4)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ALTITUDE", color = MidGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "${formatter.format(altitudeMeters)} m",
                                    color = PureBlack,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("SCORE", color = MidGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    formatter.format(score),
                                    color = PureBlack,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E2E8)))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("LIVES REMAINING", color = MidGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (i in 1..3) {
                                    Icon(
                                        imageVector = if (i <= lives) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (i <= lives) BalloonRed else Color(0xFFB0B0B8),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sound & Audio Controls Section
                Surface(
                    color = Color(0xFFFAFAFC),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEAEAEF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sound Effects Toggle
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = onToggleSound,
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (soundEnabled) Color(0xFF2B2B33) else Color(0xFFE4E4EC),
                                    contentColor = if (soundEnabled) PureWhite else MidGray
                                ),
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("toggle_sound_button")
                            ) {
                                Icon(
                                    imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = "Toggle Sound Effects",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (soundEnabled) "SFX ON" else "SFX OFF",
                                color = DeepGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Background Music Toggle
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = onToggleMusic,
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (musicEnabled) Color(0xFF2B2B33) else Color(0xFFE4E4EC),
                                    contentColor = if (musicEnabled) PureWhite else MidGray
                                ),
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("toggle_music_button")
                            ) {
                                Icon(
                                    imageVector = if (musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                    contentDescription = "Toggle Background Music",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (musicEnabled) "Music ON" else "Music OFF",
                                color = DeepGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Haptics Toggle
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = onToggleHaptics,
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (hapticsEnabled) Color(0xFF2B2B33) else Color(0xFFE4E4EC),
                                    contentColor = if (hapticsEnabled) PureWhite else MidGray
                                ),
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("toggle_haptics_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = "Toggle Vibration",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (hapticsEnabled) "Vibe ON" else "Vibe OFF",
                                color = DeepGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Resume Flight Button
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureBlack,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("resume_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESUME FLIGHT", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, letterSpacing = 0.5.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Restart Flight Button
                OutlinedButton(
                    onClick = onRestart,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PureBlack),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PureBlack),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("restart_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESTART FLIGHT", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Go to Main Menu Button
                OutlinedButton(
                    onClick = onGoToMainMenu,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF888894)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MidGray),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("pause_main_menu_button")
                ) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("EXIT TO MAIN MENU", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AdRevivePromptModal(
    altitudeMeters: Long,
    score: Long,
    adRevivesRemaining: Int,
    onWatchAd: () -> Unit,
    onDecline: () -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    Dialog(onDismissRequest = onDecline) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(22.dp),
            border = androidx.compose.foundation.BorderStroke(2.5.dp, PureBlack),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp)
                .shadow(20.dp, RoundedCornerShape(22.dp))
                .testTag("ad_revive_prompt_card")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Heart badge
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, BalloonRed),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Out of lives",
                            tint = BalloonRed,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "OUT OF LIVES!",
                    color = PureBlack,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Watch a quick ad to revive with +1 life and continue your ascent!",
                    color = MidGray,
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats & Revive Limit Banner
                Surface(
                    color = Color(0xFFF4F4F8),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCDCE4)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("CURRENT ALTITUDE", color = MidGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${formatter.format(altitudeMeters)} m",
                                color = PureBlack,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E2E8)))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("AD REVIVES REMAINING", color = MidGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Surface(
                                color = if (adRevivesRemaining > 1) Color(0xFF2E7D32) else Color(0xFFC62828),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "$adRevivesRemaining / 5 CHANCES",
                                    color = PureWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Watch Ad Button
                Button(
                    onClick = onWatchAd,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2B2B33),
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("watch_ad_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.OndemandVideo,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WATCH AD TO REVIVE (+1 LIFE)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Decline / End Game
                OutlinedButton(
                    onClick = onDecline,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF888894)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MidGray),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("decline_ad_button")
                ) {
                    Text(
                        text = "NO THANKS, END FLIGHT",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun WatchingAdModal(
    onAdCompleted: () -> Unit,
    onDecline: () -> Unit
) {
    var secondsRemaining by remember { androidx.compose.runtime.mutableIntStateOf(3) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            kotlinx.coroutines.delay(1000L)
            secondsRemaining--
        }
        isFinished = true
        kotlinx.coroutines.delay(600L)
        onAdCompleted()
    }

    Dialog(onDismissRequest = { if (isFinished) onAdCompleted() else onDecline() }) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, PureBlack),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp)
                .shadow(24.dp, RoundedCornerShape(20.dp))
                .testTag("watching_ad_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ad header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFFFF176),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PureBlack)
                    ) {
                        Text(
                            text = "SPONSORED AD",
                            color = PureBlack,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = if (secondsRemaining > 0) "Reward in ${secondsRemaining}s" else "Reward Granted!",
                        color = if (secondsRemaining > 0) MidGray else Color(0xFF2E7D32),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { ((3 - secondsRemaining) / 3f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = Color(0xFF2B2B33),
                    trackColor = Color(0xFFE2E2E8),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Simulated Ad Creative Media Canvas
                Surface(
                    color = Color(0xFF1E1E24),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.OndemandVideo,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "STRATOSPHERE APEX",
                            color = PureWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Next-Gen Atmospheric Balloon Gliders",
                            color = LightSilver,
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Claim or Auto-close Button
                Button(
                    onClick = onAdCompleted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFinished) Color(0xFF2E7D32) else Color(0xFF55555F),
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("claim_revive_button")
                ) {
                    if (isFinished) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CLAIM +1 LIFE & CONTINUE", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    } else {
                        Text("WATCHING AD (${secondsRemaining}s)...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
