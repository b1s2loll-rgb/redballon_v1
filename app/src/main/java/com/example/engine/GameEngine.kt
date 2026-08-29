package com.example.engine

import androidx.compose.ui.geometry.Offset
import com.example.data.ScoreRepository
import com.example.model.AsteroidObstacle
import com.example.model.AsteroidType
import com.example.model.DandelionFluffParticle
import com.example.model.DustParticle
import com.example.model.FeatherParticle
import com.example.model.AvailableCharacterSkins
import com.example.model.CharacterSkin
import com.example.model.DefaultCharacterSkin
import com.example.model.GameParticle
import com.example.model.GameStatus
import com.example.model.HeartFloatingParticle
import com.example.model.MissionType
import com.example.model.Obstacle
import com.example.model.ObstacleTier
import com.example.model.ParallaxCloud
import com.example.model.Player
import com.example.model.PowerUpItem
import com.example.model.PowerUpType
import com.example.model.RedPopParticle
import com.example.model.RockCrater
import com.example.model.RockDustParticle
import com.example.model.RockFacet
import com.example.model.ShieldDeflectParticle
import com.example.model.SkylineBuilding
import com.example.model.SlipstreamSparkParticle
import com.example.model.SparkleParticle
import com.example.model.StormCloudObstacle
import com.example.model.StormCloudType
import com.example.model.StormRainParticle
import com.example.model.StormSparkParticle
import com.example.model.WindStreakParticle
import com.example.sound.GameSoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

data class GameUIState(
    val status: GameStatus = GameStatus.READY,
    val score: Long = 0L,
    val altitudeMeters: Long = 0L,
    val currentTier: ObstacleTier = ObstacleTier.TIER_1_ASTEROID_DRIFT,
    val tierBannerText: String? = null,
    val screenShakeIntensity: Float = 0f,
    val activeShieldTime: Float = 0f,
    val maxShieldTime: Float = 8.0f,
    val activeSpeedBoostTime: Float = 0f,
    val maxSpeedBoostTime: Float = 6.0f,
    val lives: Int = 3,
    val maxLives: Int = 3,
    val adRevivesRemaining: Int = 5,
    val maxAdRevives: Int = 5,
    val isInvulnerable: Boolean = false,
    val isElectrified: Boolean = false,
    val electrifiedTimeRemaining: Float = 0f,
    val totalCoins: Int = 0,
    val coinsEarnedThisRun: Int = 0,
    val maxAltitudeEver: Long = 0L,
    val currentSkin: CharacterSkin = DefaultCharacterSkin
)

class GameEngine(
    private val scoreRepository: ScoreRepository,
    private val soundManager: GameSoundManager
) {
    private val random = Random()

    private val _uiState = MutableStateFlow(
        GameUIState(
            totalCoins = scoreRepository.getCoins(),
            maxAltitudeEver = scoreRepository.getMaxAltitude(),
            currentSkin = AvailableCharacterSkins.find { it.id == scoreRepository.getSelectedSkinId() } ?: DefaultCharacterSkin
        )
    )
    val uiState: StateFlow<GameUIState> = _uiState.asStateFlow()

    val player = Player()
    val obstacles = mutableListOf<Obstacle>()
    val powerUpItems = mutableListOf<PowerUpItem>()
    val particles = mutableListOf<GameParticle>()
    val parallaxClouds = mutableListOf<ParallaxCloud>()
    val skylineBuildings = mutableListOf<SkylineBuilding>()

    var playerShieldTimer: Float = 0f
    var playerMaxShieldTime: Float = 8.0f
    var playerSpeedBoostTimer: Float = 0f
    var playerMaxSpeedBoostTime: Float = 6.0f
    private var powerUpSpawnTimer: Float = 10.0f

    var playerLives: Int = 3
    var adRevivesRemaining: Int = 5
    var nextHeartAltitudeMilestone: Long = 5000L
    var nextCoinAltitudeMilestone: Long = 1000L
    var coinsEarnedThisRun: Int = 0

    var screenWidth = 1080f
    var screenHeight = 1920f

    // World & Takeoff parameters
    var groundY = 0f
    var groundOffsetY = 0f
    var takeoffTimer = 0f
    val takeoffDuration = 1.4f

    var baseScrollSpeed = 160f // World moves down as balloon climbs up
    var currentScrollSpeed = 160f
    var cameraParallaxX = 0f

    private var targetTouchX: Float? = null
    private var targetTouchY: Float? = null
    private var isHoldingTouch: Boolean = false

    private var gameTime = 0f
    private var spawnTimer = 0f
    private var ambientParticleTimer = 0f
    private var streakSpawnTimer = 0f
    private var tierNotificationTimer = 0f
    private var lastTierAnnounced: ObstacleTier = ObstacleTier.TIER_1_ASTEROID_DRIFT
    private var lastAltitudeStepAnnounced: Int = 0
    private var glideSoundCooldown = 0f

    var screenShakeOffset = Offset.Zero

    init {
        val savedSkinId = scoreRepository.getSelectedSkinId()
        val skin = AvailableCharacterSkins.find { it.id == savedSkinId } ?: DefaultCharacterSkin
        player.skin = skin
        initParallaxClouds()
        initSkylineBuildings()
    }

    fun setScreenDimensions(width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return
        val isFirst = (screenWidth == 1080f && screenHeight == 1920f)
        screenWidth = width
        screenHeight = height
        groundY = screenHeight - 90f

        if (isFirst || _uiState.value.status == GameStatus.READY) {
            resetToGround()
            if (parallaxClouds.isEmpty() || isFirst) {
                initParallaxClouds()
            }
            if (skylineBuildings.isEmpty() || isFirst) {
                initSkylineBuildings()
            }
        }
    }

    fun resetToGround() {
        groundOffsetY = 0f
        takeoffTimer = 0f
        cameraParallaxX = 0f
        player.x = screenWidth * 0.5f
        player.y = groundY - 50f
        player.vx = 0f
        player.vy = 0f
        player.balloonSwayAngle = 0f
        player.balloonStretchY = 1.0f
        player.verticalSwayPhase = 0f
        player.childAngle = 0f
        player.childAngularVelocity = 0f
        player.legSwingPhase = 0f
        player.isPopped = false
        player.popTime = 0f
        player.invulnerabilityTimer = 0f
        player.electrifiedTimer = 0f
        player.electricJitterX = 0f
        player.electricJitterY = 0f
        player.isHoldingLift = false
        player.isOnGround = true
        player.takeoffProgress = 0f
        targetTouchX = null
        targetTouchY = null
        isHoldingTouch = false
        powerUpItems.clear()
        playerShieldTimer = 0f
        playerSpeedBoostTimer = 0f
        powerUpSpawnTimer = 10.0f
        playerLives = 3
        adRevivesRemaining = 5
        nextHeartAltitudeMilestone = 5000L
        nextCoinAltitudeMilestone = 1000L
        coinsEarnedThisRun = 0

        val savedSkinId = scoreRepository.getSelectedSkinId()
        val skin = AvailableCharacterSkins.find { it.id == savedSkinId } ?: DefaultCharacterSkin
        player.skin = skin
    }

    private fun initSkylineBuildings() {
        skylineBuildings.clear()
        val totalWidth = screenWidth.coerceAtLeast(1080f) + 500f
        val startX = -250f

        // Layer 0: Far Background Monumental Megatowers (Slowest parallax, tall & light)
        var curX0 = startX
        while (curX0 < totalWidth) {
            val bWidth = 60f + random.nextFloat() * 75f
            val bHeight = 320f + random.nextFloat() * 260f
            skylineBuildings.add(
                SkylineBuilding(
                    relX = curX0,
                    width = bWidth,
                    height = bHeight,
                    layer = 0,
                    hasAntenna = random.nextFloat() < 0.5f,
                    antennaHeight = 30f + random.nextFloat() * 40f,
                    hasSpire = random.nextFloat() < 0.35f,
                    windowCols = (bWidth / 13f).toInt().coerceAtLeast(2),
                    windowRows = (bHeight / 22f).toInt().coerceAtLeast(5)
                )
            )
            curX0 += bWidth - (random.nextFloat() * 12f)
        }

        // Layer 1: Midground High-Rises (Moderate parallax, mid-tones)
        var curX1 = startX
        while (curX1 < totalWidth) {
            val bWidth = 70f + random.nextFloat() * 85f
            val bHeight = 210f + random.nextFloat() * 190f
            skylineBuildings.add(
                SkylineBuilding(
                    relX = curX1,
                    width = bWidth,
                    height = bHeight,
                    layer = 1,
                    hasAntenna = random.nextFloat() < 0.4f,
                    antennaHeight = 22f + random.nextFloat() * 30f,
                    hasSpire = random.nextFloat() < 0.3f,
                    windowCols = (bWidth / 14f).toInt().coerceAtLeast(2),
                    windowRows = (bHeight / 20f).toInt().coerceAtLeast(4)
                )
            )
            curX1 += bWidth + random.nextFloat() * 8f
        }

        // Layer 2: Foreground Urban Skyline & Rooftops (Fastest parallax, dark charcoal)
        var curX2 = startX
        while (curX2 < totalWidth) {
            val bWidth = 80f + random.nextFloat() * 95f
            val bHeight = 120f + random.nextFloat() * 140f
            skylineBuildings.add(
                SkylineBuilding(
                    relX = curX2,
                    width = bWidth,
                    height = bHeight,
                    layer = 2,
                    hasAntenna = random.nextFloat() < 0.3f,
                    antennaHeight = 18f + random.nextFloat() * 22f,
                    hasSpire = random.nextFloat() < 0.2f,
                    windowCols = (bWidth / 15f).toInt().coerceAtLeast(2),
                    windowRows = (bHeight / 18f).toInt().coerceAtLeast(3)
                )
            )
            curX2 += bWidth + random.nextFloat() * 15f
        }
    }

    private fun initParallaxClouds() {
        parallaxClouds.clear()
        // Generate initial clouds spread across vertical and horizontal canvas with 3 distinct depth planes
        for (i in 0 until 21) {
            val layer = i % 3
            val w = when (layer) {
                0 -> 120f + random.nextFloat() * 80f // Distant high-altitude wispy cloud
                1 -> 190f + random.nextFloat() * 110f // Mid-altitude cumulus cloud
                else -> 280f + random.nextFloat() * 150f // Foreground rushing cloud bank
            }
            val h = w * (0.38f + random.nextFloat() * 0.16f)
            val speedX = when (layer) {
                0 -> 12f + random.nextFloat() * 14f
                1 -> 32f + random.nextFloat() * 22f
                else -> 65f + random.nextFloat() * 32f
            }
            val alpha = when (layer) {
                0 -> 0.35f
                1 -> 0.65f
                else -> 0.90f
            }
            parallaxClouds.add(
                ParallaxCloud(
                    x = random.nextFloat() * (screenWidth + 300f) - 150f,
                    y = random.nextFloat() * screenHeight,
                    width = w,
                    height = h,
                    speedX = speedX,
                    parallaxLayer = layer,
                    alpha = alpha
                )
            )
        }
    }

    fun startTakeoff() {
        if (_uiState.value.status != GameStatus.READY && 
            _uiState.value.status != GameStatus.GAME_OVER && 
            _uiState.value.status != GameStatus.PAUSED
        ) return

        obstacles.clear()
        particles.clear()
        gameTime = 0f
        spawnTimer = 2.0f
        groundOffsetY = 0f
        takeoffTimer = 0f
        targetTouchX = null
        targetTouchY = null
        isHoldingTouch = false

        resetToGround()
        player.isOnGround = false

        _uiState.value = _uiState.value.copy(
            status = GameStatus.TAKEOFF,
            score = 0L,
            altitudeMeters = 0L,
            currentTier = ObstacleTier.TIER_1_ASTEROID_DRIFT,
            tierBannerText = "TAKEOFF: Ascending into the Asteroid Field!",
            screenShakeIntensity = 0f,
            activeShieldTime = 0f,
            activeSpeedBoostTime = 0f,
            lives = 3,
            maxLives = 3,
            adRevivesRemaining = 5,
            maxAdRevives = 5,
            isInvulnerable = false
        )
        tierNotificationTimer = 2.5f
        lastTierAnnounced = ObstacleTier.TIER_1_ASTEROID_DRIFT
        lastAltitudeStepAnnounced = 0

        soundManager.playTakeoff()
        soundManager.vibrateTap()
        soundManager.startBackgroundMusic()

        // Record Daily Mission Progress for playing a flight
        val completedFlightMissions = scoreRepository.recordMissionProgress(MissionType.PLAY_FLIGHTS, 1, isIncremental = true)
        if (completedFlightMissions.isNotEmpty()) {
            val m = completedFlightMissions.first()
            _uiState.value = _uiState.value.copy(
                tierBannerText = "🎯 MISSION COMPLETE: ${m.title}! (+${m.coinReward} 🪙)"
            )
            tierNotificationTimer = 3.5f
            soundManager.playCoinEarned()
        }

        // Ground takeoff dust puffs
        for (i in 0 until 18) {
            val angle = random.nextFloat() * Math.PI.toFloat()
            val speed = 70f + random.nextFloat() * 130f
            particles.add(
                DustParticle(
                    x = player.x + (random.nextFloat() * 40f - 20f),
                    y = groundY,
                    vx = cos(angle) * speed,
                    vy = -sin(angle) * speed * 0.65f,
                    radius = 3.2f + random.nextFloat() * 3.2f,
                    alpha = 0.75f,
                    maxLife = 0.65f + random.nextFloat() * 0.4f
                )
            )
        }
    }

    fun pauseGame() {
        if (_uiState.value.status == GameStatus.PLAYING || _uiState.value.status == GameStatus.TAKEOFF) {
            _uiState.value = _uiState.value.copy(status = GameStatus.PAUSED)
            soundManager.playClick()
            soundManager.pauseBackgroundMusic()
        }
    }

    fun resumeGame() {
        if (_uiState.value.status == GameStatus.PAUSED) {
            _uiState.value = _uiState.value.copy(status = GameStatus.PLAYING)
            soundManager.playClick()
            soundManager.resumeBackgroundMusic()
        }
    }

    fun setTouchInput(isHolding: Boolean, touchX: Float? = null, touchY: Float? = null) {
        isHoldingTouch = isHolding
        player.isHoldingLift = isHolding

        if (isHolding) {
            if (touchX != null) targetTouchX = touchX
            if (touchY != null) targetTouchY = touchY

            if (_uiState.value.status == GameStatus.PLAYING && glideSoundCooldown <= 0f) {
                soundManager.playLift()
                glideSoundCooldown = 0.35f
            }
        } else {
            targetTouchX = null
            targetTouchY = null
        }
    }

    fun update(dt: Float) {
        if (_uiState.value.status == GameStatus.PAUSED) {
            return
        }

        val clampedDt = dt.coerceIn(0.001f, 0.04f)
        gameTime += clampedDt

        // Smoothly track horizontal camera parallax from player steering
        val targetCamOffset = (player.x - screenWidth * 0.5f)
        cameraParallaxX += (targetCamOffset - cameraParallaxX) * (clampedDt * 5.0f)

        // Always update ambient clouds and background motion
        updateClouds(clampedDt)
        updateParticles(clampedDt)

        when (_uiState.value.status) {
            GameStatus.READY -> {
                updateReadyScene(clampedDt)
            }
            GameStatus.TAKEOFF -> {
                updateTakeoffScene(clampedDt)
            }
            GameStatus.PLAYING -> {
                updatePlayingScene(clampedDt)
            }
            GameStatus.GAME_OVER -> {
                updateGameOverScene(clampedDt)
            }
            GameStatus.AD_REVIVE_PROMPT, GameStatus.WATCHING_AD -> {
                // Keep ambient clouds and sparkles gently drifting
                val shake = _uiState.value.screenShakeIntensity * (1f - clampedDt * 4f)
                if (shake > 0.01f) {
                    _uiState.value = _uiState.value.copy(screenShakeIntensity = shake)
                    screenShakeOffset = Offset(
                        (random.nextFloat() - 0.5f) * shake * 20f,
                        (random.nextFloat() - 0.5f) * shake * 20f
                    )
                } else {
                    screenShakeOffset = Offset.Zero
                }
            }
            GameStatus.PAUSED -> {
                // Suspended
            }
        }
    }

    private fun updateReadyScene(dt: Float) {
        // Child stands peacefully on the flat ground holding the balloon
        player.balloonBobPhase += dt * 2.5f
        player.x = screenWidth * 0.5f
        player.y = groundY - 50f + sin(player.balloonBobPhase) * 3f
        player.vx = 0f
        player.vy = 0f
        player.childAngle = sin(player.balloonBobPhase * 0.8f) * 0.04f

        // Occasional gentle breeze particle & dandelion seeds
        if (random.nextFloat() < 0.06f) {
            particles.add(
                DandelionFluffParticle(
                    x = -20f,
                    y = random.nextFloat() * (screenHeight * 0.8f),
                    vx = 80f + random.nextFloat() * 50f,
                    vy = random.nextFloat() * 20f - 10f,
                    bobPhase = random.nextFloat() * 10f
                )
            )
        }
    }

    private fun updateTakeoffScene(dt: Float) {
        takeoffTimer += dt
        val progress = (takeoffTimer / takeoffDuration).coerceIn(0f, 1f)
        player.takeoffProgress = progress

        // Smooth ease in-out takeoff ascent to center of screen
        val targetY = screenHeight * 0.5f
        val startY = groundY - 50f
        // Smooth easing curve
        val easeProgress = progress * progress * (3f - 2f * progress)
        player.y = startY + (targetY - startY) * easeProgress

        // Ground and skyscrapers drop down and off-screen
        groundOffsetY = easeProgress * (screenHeight * 0.7f)

        // Legs kick excitedly
        player.legSwingPhase += dt * 14f
        player.childAngle = sin(player.legSwingPhase * 0.5f) * 0.12f

        // Balloon bobbing and gentle liftoff sway
        player.balloonBobPhase += dt * 5f
        player.verticalSwayPhase += dt * 5.5f
        val takeoffSway = sin(player.verticalSwayPhase) * 0.12f
        player.balloonSwayAngle += (takeoffSway - player.balloonSwayAngle) * (dt * 8f)
        player.balloonStretchY += (1.07f - player.balloonStretchY) * (dt * 6f)

        // Takeoff particles
        if (progress < 0.8f && random.nextFloat() < 0.35f) {
            particles.add(
                DustParticle(
                    x = player.x + (random.nextFloat() * 30f - 15f),
                    y = groundY + groundOffsetY,
                    vx = (random.nextFloat() - 0.5f) * 60f,
                    vy = 30f + random.nextFloat() * 40f,
                    radius = 2.5f,
                    alpha = 0.5f
                )
            )
        }

        if (progress >= 1f) {
            _uiState.value = _uiState.value.copy(status = GameStatus.PLAYING)
        }
    }

    private fun updatePlayingScene(dt: Float) {
        if (glideSoundCooldown > 0f) glideSoundCooldown -= dt

        // Count down active power-ups, invulnerability, and electrified shock state
        if (player.invulnerabilityTimer > 0f) {
            player.invulnerabilityTimer = (player.invulnerabilityTimer - dt).coerceAtLeast(0f)
        }
        if (player.yellowElectrifiedTimer > 0f) {
            player.yellowElectrifiedTimer = (player.yellowElectrifiedTimer - dt).coerceAtLeast(0f)
        }
        if (player.immobilizedTimer > 0f) {
            player.immobilizedTimer = (player.immobilizedTimer - dt).coerceAtLeast(0f)
        }
        if (player.sizeGrowthTimer > 0f) {
            player.sizeGrowthTimer = (player.sizeGrowthTimer - dt).coerceAtLeast(0f)
        }
        if (player.ascentSlowTimer > 0f) {
            player.ascentSlowTimer = (player.ascentSlowTimer - dt).coerceAtLeast(0f)
        }
        if (player.electrifiedTimer > 0f) {
            player.electrifiedTimer = (player.electrifiedTimer - dt).coerceAtLeast(0f)
            // Rapid chaotic electric jitter/shivering vibration
            player.electricJitterX = (random.nextFloat() - 0.5f) * 8f
            player.electricJitterY = (random.nextFloat() - 0.5f) * 8f

            // Continually discharge crackling electric sparks from balloon and character while electrified
            if (random.nextFloat() < 0.65f) {
                val bc = player.balloonCenter()
                val cc = player.childCenter()
                val isBalloon = random.nextBoolean()
                val ox = if (isBalloon) bc.x + (random.nextFloat() - 0.5f) * player.effectiveBalloonRadius * 1.5f else cc.x + (random.nextFloat() - 0.5f) * 26f * player.sizeScale
                val oy = if (isBalloon) bc.y + (random.nextFloat() - 0.5f) * player.effectiveBalloonRadius * 1.5f else cc.y + (random.nextFloat() - 0.5f) * 34f * player.sizeScale
                val sparkColor = if (player.yellowElectrifiedTimer > 0f) 0xFFFFEA00L else (if (player.isSizeExpanded) 0xFF00E5FFL else 0xFFE040FBL)
                particles.add(
                    StormSparkParticle(
                        x = ox,
                        y = oy,
                        vx = (random.nextFloat() - 0.5f) * 90f,
                        vy = (random.nextFloat() - 0.5f) * 90f,
                        size = 3.5f + random.nextFloat() * 3.5f,
                        color = sparkColor,
                        alpha = 1f,
                        maxLife = 0.30f
                    )
                )
            }
        } else {
            player.electricJitterX = 0f
            player.electricJitterY = 0f
        }
        if (playerShieldTimer > 0f) {
            playerShieldTimer = (playerShieldTimer - dt).coerceAtLeast(0f)
        }
        if (playerSpeedBoostTimer > 0f) {
            playerSpeedBoostTimer = (playerSpeedBoostTimer - dt).coerceAtLeast(0f)
            // Emit trail of slipstream sparks while speed boosted
            if (random.nextFloat() < 0.65f) {
                val bc = player.balloonCenter()
                particles.add(
                    SlipstreamSparkParticle(
                        x = bc.x + (random.nextFloat() - 0.5f) * 26f * player.sizeScale,
                        y = bc.y + player.effectiveBalloonRadius + random.nextFloat() * 18f,
                        vx = (random.nextFloat() - 0.5f) * 50f,
                        vy = 80f + random.nextFloat() * 100f,
                        size = 3.5f + random.nextFloat() * 3.5f,
                        rotation = random.nextFloat() * 360f,
                        alpha = 0.9f
                    )
                )
            }
        }

        // 1. NEUTRAL BUOYANCY & 360° DRAG/STEER MOBILITY
        // The character floats automatically in place (no downward gravity).
        // Electrified shock slows down the character's movement and responsiveness
        val electricSlowFactor = if (player.electrifiedTimer > 0f) 0.42f else 1.0f
        val ascentSlowFactor = if (player.isAscentSlowed) 0.55f else 1.0f
        val isImmobilized = player.isImmobilized

        player.balloonBobPhase += dt * (if (playerSpeedBoostTimer > 0f) 6f else 4f) * electricSlowFactor
        val idleBobY = sin(player.balloonBobPhase) * 12f * dt * electricSlowFactor

        val speedMultiplier = (if (playerSpeedBoostTimer > 0f) 1.45f else 1.0f) * electricSlowFactor * ascentSlowFactor
        val maxSpeed = if (isImmobilized) 0f else 520f * speedMultiplier

        if (!isImmobilized && isHoldingTouch && targetTouchX != null && targetTouchY != null) {
            // Player is steering/holding the screen: Smoothly glide towards the touch target (up, down, left, right)
            val dx = targetTouchX!! - player.x
            val dy = targetTouchY!! - player.y

            val targetVx = (dx * (6.5f * speedMultiplier)).coerceIn(-maxSpeed, maxSpeed)
            val targetVy = (dy * (6.5f * speedMultiplier)).coerceIn(-maxSpeed, maxSpeed)

            player.vx += (targetVx - player.vx) * (dt * (12f * speedMultiplier))
            player.vy += (targetVy - player.vy) * (dt * (12f * speedMultiplier))
            player.legSwingPhase += dt * (12f * speedMultiplier)
        } else {
            // Player let go or immobilized: smoothly decelerate and hover/float in place where left
            player.vx *= (1f - dt * 6.0f)
            player.vy *= (1f - dt * 6.0f)
            if (isImmobilized) {
                player.vx = 0f
                player.vy = 0f
            } else {
                player.y += idleBobY
                player.legSwingPhase += dt * 4f * electricSlowFactor
            }
        }

        player.x += player.vx * dt
        player.y += player.vy * dt

        // Screen Boundaries (Safe bounds with size scale consideration)
        val minX = 40f * player.sizeScale
        val maxX = screenWidth - 40f * player.sizeScale
        if (player.x < minX) {
            player.x = minX
            player.vx = 0f
        } else if (player.x > maxX) {
            player.x = maxX
            player.vx = 0f
        }

        val minY = 80f * player.sizeScale
        val maxY = screenHeight - 80f * player.sizeScale
        if (player.y < minY) {
            player.y = minY
            player.vy = 0f
        } else if (player.y > maxY) {
            player.y = maxY
            player.vy = 0f
        }

        // Character agile lean angle based on horizontal speed
        val targetAngle = (player.vx / 450f) * 0.38f
        player.childAngle += (targetAngle - player.childAngle) * (dt * 10f)

        // Balloon Gentle Sway & Vertical Dynamics:
        val verticalSpeed = kotlin.math.abs(player.vy)
        val swayOscillationSpeed = 3.5f + (verticalSpeed / 90f).coerceAtMost(8.5f)
        player.verticalSwayPhase += dt * swayOscillationSpeed

        // Lateral flutter generated by vertical relative airflow
        val verticalAirflowSway = sin(player.verticalSwayPhase) * ((verticalSpeed / 280f).coerceIn(0f, 1f) * 0.16f)

        // Inertial drag sway from horizontal motion (tilts opposite to character glide direction)
        val horizontalDragSway = -(player.vx / 460f) * 0.18f

        // Ambient idle breathing sway
        val ambientSway = sin(player.balloonBobPhase * 1.5f) * 0.04f

        val targetBalloonSway = horizontalDragSway + verticalAirflowSway + ambientSway
        player.balloonSwayAngle += (targetBalloonSway - player.balloonSwayAngle) * (dt * 11f)

        // Vertical stretch / compression based on climb or descent:
        val targetStretchY = 1.0f - (player.vy / 520f) * 0.11f
        val clampedTargetStretch = targetStretchY.coerceIn(0.89f, 1.14f)
        player.balloonStretchY += (clampedTargetStretch - player.balloonStretchY) * (dt * 7.5f)

        // 2. Continuous Vertical Ascent & Score Tracking
        val currentAlt = _uiState.value.altitudeMeters

        // High-Altitude Ascent Speed Progression:
        // After 15,000m (15k), increase ascent speed by 5%.
        // For every 5,000m beyond 15,000m, add +1% speed.
        // Once speed gets 20% faster (+0.20), stop further ascent speed increase.
        val highAltitudeBonus = if (currentAlt >= 15000L) {
            val extra5kSteps = (currentAlt - 15000L) / 5000L
            (0.05f + extra5kSteps * 0.01f).coerceAtMost(0.20f)
        } else {
            0.0f
        }
        val highAltitudeMultiplier = 1.0f + highAltitudeBonus

        val speedBoostExtraScroll = if (playerSpeedBoostTimer > 0f) 130f else 0f
        // Stop base altitude scaling from increasing once 20% speed cap is reached
        val cappedAltForBaseScaling = currentAlt.coerceAtMost(90000L)
        val baseAltitudeScaling = (cappedAltForBaseScaling * 0.04f).coerceAtMost(200f)

        currentScrollSpeed = (baseScrollSpeed + speedBoostExtraScroll + baseAltitudeScaling) * highAltitudeMultiplier * (if (player.isAscentSlowed) 0.50f else 1.0f)

        val altitudeClimb = (currentScrollSpeed * dt * 0.14f).toLong()
        val newAltitude = _uiState.value.altitudeMeters + altitudeClimb.coerceAtLeast(1L)
        val newScore = newAltitude

        val altitudeStep = (newAltitude / 200L).toInt()

        // Coin Milestone: 1 Coin per 1,000m of altitude attained
        var awardedCoins = 0
        while (newAltitude >= nextCoinAltitudeMilestone) {
            scoreRepository.addCoins(1)
            coinsEarnedThisRun++
            awardedCoins++
            nextCoinAltitudeMilestone += 1000L
        }
        if (awardedCoins > 0) {
            soundManager.playCoinEarned()
            soundManager.vibrateTap()
        }

        // Remember highest altitude ever
        val currentMax = scoreRepository.getMaxAltitude()
        val updatedMaxAltitude = if (newAltitude > currentMax) {
            scoreRepository.saveGameResult(newScore, newAltitude, 0L)
            newAltitude
        } else {
            currentMax
        }

        // Tier Determination
        val tier = when {
            newScore >= ObstacleTier.TIER_4_DEEP_SPACE_BARRAGE.minScore -> ObstacleTier.TIER_4_DEEP_SPACE_BARRAGE
            newScore >= ObstacleTier.TIER_3_ASTEROID_BELT.minScore -> ObstacleTier.TIER_3_ASTEROID_BELT
            newScore >= ObstacleTier.TIER_2_METEOR_SHOWER.minScore -> ObstacleTier.TIER_2_METEOR_SHOWER
            else -> ObstacleTier.TIER_1_ASTEROID_DRIFT
        }

        // Daily Missions Altitude Progress
        val completedDist = scoreRepository.recordMissionProgress(MissionType.FLY_DISTANCE, newAltitude.toInt(), isIncremental = false)
        val completedRec = scoreRepository.recordMissionProgress(MissionType.BREAK_RECORD, newAltitude.toInt(), isIncremental = false)
        val justCompleted = completedDist + completedRec

        var tierBanner = _uiState.value.tierBannerText
        if (justCompleted.isNotEmpty()) {
            val m = justCompleted.first()
            tierBanner = "🎯 MISSION COMPLETE: ${m.title}! (+${m.coinReward} 🪙)"
            tierNotificationTimer = 3.2f
            soundManager.playCoinEarned()
            soundManager.vibrateTap()
        } else if (awardedCoins > 0) {
            tierBanner = "+$awardedCoins COIN${if (awardedCoins > 1) "S" else ""} EARNED! (${(nextCoinAltitudeMilestone - 1000L)}M REACHED)"
            tierNotificationTimer = 2.8f
        } else if (tier != lastTierAnnounced) {
            lastTierAnnounced = tier
            tierBanner = "${tier.displayName.uppercase()}: ${tier.subtitle}"
            tierNotificationTimer = 3.2f
            soundManager.playMilestone()
        } else if (altitudeStep > lastAltitudeStepAnnounced && altitudeStep > 0) {
            lastAltitudeStepAnnounced = altitudeStep
            tierBanner = "ALTITUDE REACHED: ${altitudeStep * 200}M"
            tierNotificationTimer = 2.4f
            soundManager.playMilestone()
        }

        if (tierNotificationTimer > 0f) {
            tierNotificationTimer -= dt
            if (tierNotificationTimer <= 0f) {
                tierBanner = null
            }
        }

        _uiState.value = _uiState.value.copy(
            score = newScore,
            altitudeMeters = newAltitude,
            currentTier = tier,
            tierBannerText = tierBanner,
            activeShieldTime = playerShieldTimer,
            maxShieldTime = playerMaxShieldTime,
            activeSpeedBoostTime = playerSpeedBoostTimer,
            maxSpeedBoostTime = playerMaxSpeedBoostTime,
            lives = playerLives,
            adRevivesRemaining = adRevivesRemaining,
            isInvulnerable = player.invulnerabilityTimer > 0f,
            isElectrified = player.isElectrified,
            electrifiedTimeRemaining = player.electrifiedTimer,
            totalCoins = scoreRepository.getCoins(),
            coinsEarnedThisRun = coinsEarnedThisRun,
            maxAltitudeEver = updatedMaxAltitude
        )

        // 3. Heart Collectible Spawning (Every 5,000 meters)
        if (newAltitude >= nextHeartAltitudeMilestone) {
            val heartX = screenWidth * 0.25f + random.nextFloat() * (screenWidth * 0.5f)
            powerUpItems.add(
                PowerUpItem(
                    x = heartX,
                    y = -50f,
                    vx = (random.nextFloat() - 0.5f) * 20f,
                    vy = 75f,
                    type = PowerUpType.HEART,
                    radius = 24f
                )
            )
            nextHeartAltitudeMilestone += 5000L
        }

        // 4. Spawning Asteroid & Rock Obstacles (Starts low, scales moderately every 200m)
        spawnTimer -= dt
        if (spawnTimer <= 0f) {
            spawnNextObstacle(altitudeStep, tier)
            // Scaling spawn interval based on altitude:
            // 0 - 199m: ~1.85s (fewer asteroids at the start)
            // 200 - 399m: ~1.55s
            // 400 - 599m: ~1.30s
            // 600 - 799m: ~1.05s
            // 800 - 999m: ~0.85s
            // 1000 - 1199m: ~0.70s
            // 1200m+: ~0.55s down to minimum 0.38s
            val altitudeStepFloat = newAltitude / 200f
            val baseInterval = (1.90f - (altitudeStepFloat * 0.22f)).coerceIn(0.38f, 2.0f)
            val jitter = baseInterval * 0.25f
            spawnTimer = baseInterval + (random.nextFloat() - 0.5f) * jitter
        }

        // 5. Rare Power-Up Spawning & Updating
        powerUpSpawnTimer -= dt
        if (powerUpSpawnTimer <= 0f) {
            if (powerUpItems.none { it.type == PowerUpType.SHIELD || it.type == PowerUpType.SPEED_BOOST }) {
                val spawnType = if (random.nextBoolean()) PowerUpType.SHIELD else PowerUpType.SPEED_BOOST
                val spawnX = screenWidth * 0.2f + random.nextFloat() * (screenWidth * 0.6f)
                powerUpItems.add(
                    PowerUpItem(
                        x = spawnX,
                        y = -40f,
                        vx = (random.nextFloat() - 0.5f) * 30f,
                        vy = 90f,
                        type = spawnType
                    )
                )
            }
            // Next rare spawn in 18-28 seconds
            powerUpSpawnTimer = 18.0f + random.nextFloat() * 10.0f
        }

        // Update Power-Up Collectibles & Check Pickup
        val powerUpIterator = powerUpItems.iterator()
        while (powerUpIterator.hasNext()) {
            val item = powerUpIterator.next()
            item.pulsePhase += dt * 3.8f
            item.rotation += dt * 1.8f
            item.x += item.vx * dt
            item.y += (item.vy + currentScrollSpeed * 0.45f) * dt

            // Clamp item horizontal drift to screen
            if (item.x < 50f) {
                item.x = 50f
                item.vx = -item.vx
            } else if (item.x > screenWidth - 50f) {
                item.x = screenWidth - 50f
                item.vx = -item.vx
            }

            // Power-up beacon sparkle particles
            if (random.nextFloat() < 0.25f) {
                particles.add(
                    SparkleParticle(
                        x = item.x + (random.nextFloat() - 0.5f) * item.radius * 1.5f,
                        y = item.y + (random.nextFloat() - 0.5f) * item.radius * 1.5f,
                        vx = (random.nextFloat() - 0.5f) * 30f,
                        vy = (random.nextFloat() - 0.5f) * 30f,
                        size = 2.5f + random.nextFloat() * 2f,
                        alpha = 0.85f,
                        maxLife = 0.45f
                    )
                )
            }

            // Collision check with player balloon or child
            if (item.checkPickup(player.balloonCenter(), player.balloonRadius, player.childCenter(), 18f)) {
                item.isCollected = true
                if (item.type == PowerUpType.HEART) {
                    // Heart collectible restores 1 life if lost, else nothing happens
                    if (playerLives < 3) {
                        playerLives++
                        tierBanner = "+1 LIFE RESTORED! ($playerLives/3 LIVES)"
                        tierNotificationTimer = 2.6f
                        soundManager.playLifeGain()
                        soundManager.vibrateLifeGain()
                    } else {
                        tierBanner = "HEART COLLECTED (MAX 3/3 LIVES)"
                        tierNotificationTimer = 2.0f
                        soundManager.playCollectBubble()
                        soundManager.vibrateTap()
                    }
                    _uiState.value = _uiState.value.copy(
                        lives = playerLives,
                        tierBannerText = tierBanner
                    )
                    // Floating heart particle burst
                    for (i in 0 until 14) {
                        val ang = (i.toFloat() / 14f) * Math.PI.toFloat() * 2f
                        val spd = 60f + random.nextFloat() * 90f
                        particles.add(
                            HeartFloatingParticle(
                                x = item.x,
                                y = item.y,
                                vx = cos(ang) * spd,
                                vy = sin(ang) * spd - 30f,
                                size = 6f + random.nextFloat() * 4f,
                                rotation = random.nextFloat() * 360f,
                                vRot = (random.nextFloat() - 0.5f) * 200f,
                                alpha = 1f,
                                maxLife = 0.85f
                            )
                        )
                    }
                } else if (item.type == PowerUpType.SHIELD) {
                    playerShieldTimer = PowerUpType.SHIELD.defaultDuration
                    playerMaxShieldTime = PowerUpType.SHIELD.defaultDuration
                    tierBanner = "KINETIC SHIELD ACTIVATED!"
                    tierNotificationTimer = 2.4f
                    soundManager.playPowerUp()
                    soundManager.vibrateTap()
                } else {
                    playerSpeedBoostTimer = PowerUpType.SPEED_BOOST.defaultDuration
                    playerMaxSpeedBoostTime = PowerUpType.SPEED_BOOST.defaultDuration
                    tierBanner = "TURBO SPEED SURGE ACTIVATED!"
                    tierNotificationTimer = 2.4f
                    soundManager.playSpeedBoost()
                    soundManager.vibrateTap()
                }

                // Collection burst particles
                for (i in 0 until 16) {
                    val ang = (i.toFloat() / 16f) * (Math.PI.toFloat() * 2f)
                    val spd = 80f + random.nextFloat() * 120f
                    particles.add(
                        SparkleParticle(
                            x = item.x,
                            y = item.y,
                            vx = cos(ang) * spd,
                            vy = sin(ang) * spd,
                            size = 3f + random.nextFloat() * 3f,
                            alpha = 1f,
                            maxLife = 0.6f
                        )
                    )
                }

                powerUpIterator.remove()
                continue
            }

            // Remove if drifted off bottom
            if (item.y > screenHeight + 100f) {
                powerUpIterator.remove()
            }
        }

        // Ambient airborne floating rock dust, cosmic flakes, and seeds
        ambientParticleTimer -= dt
        if (ambientParticleTimer <= 0f) {
            if (random.nextFloat() < 0.65f) {
                particles.add(
                    RockDustParticle(
                        x = random.nextFloat() * screenWidth,
                        y = -15f,
                        vx = (random.nextFloat() - 0.5f) * 60f,
                        vy = 50f + random.nextFloat() * 50f,
                        size = 3f + random.nextFloat() * 4f,
                        rotation = random.nextFloat() * 360f,
                        vRot = (random.nextFloat() - 0.5f) * 4f,
                        alpha = 0.7f,
                        maxLife = 1.8f
                    )
                )
            } else {
                particles.add(
                    DandelionFluffParticle(
                        x = random.nextFloat() * screenWidth,
                        y = -10f,
                        vx = (random.nextFloat() - 0.4f) * 35f,
                        vy = 25f + random.nextFloat() * 30f,
                        bobPhase = random.nextFloat() * 10f
                    )
                )
            }
            ambientParticleTimer = 0.4f + random.nextFloat() * 0.4f
        }

        // Spawn Wind & Atmospheric Speed Streaks
        streakSpawnTimer -= dt
        if (streakSpawnTimer <= 0f) {
            particles.add(
                WindStreakParticle(
                    x = random.nextFloat() * screenWidth,
                    y = -20f,
                    vx = (random.nextFloat() - 0.5f) * 50f,
                    vy = currentScrollSpeed * 1.3f + random.nextFloat() * 120f,
                    length = 60f + random.nextFloat() * 90f,
                    alpha = 0.35f,
                    maxLife = 1.1f
                )
            )
            streakSpawnTimer = 0.20f + random.nextFloat() * 0.25f
        }

        // 5. Update Obstacles & Dynamic Player-Targeting / Irregular Trajectory
        val iterator = obstacles.iterator()
        val bc = player.balloonCenter()
        val cc = player.childCenter()
        val targetPointX = bc.x * 0.6f + cc.x * 0.4f
        val targetPointY = bc.y
        val altitude = _uiState.value.altitudeMeters
        val isHighAltitudeTracking = altitude >= 50000L // Rocks and clouds follow player ONLY after 50,000m

        while (iterator.hasNext()) {
            val obs = iterator.next()
            obs.lifetime += dt

            // A. Dynamic Player-Targeting Steering (ONLY active after 50k altitude):
            // While descending toward the player above 50k, steer and adjust trajectory toward player
            val dyToPlayer = targetPointY - obs.y
            if (isHighAltitudeTracking && dyToPlayer > 80f * player.sizeScale && !obs.isTrajectoryLocked) {
                val dxToPlayer = targetPointX - obs.x
                // Lead the player slightly based on current horizontal velocity
                val leadOffset = (player.vx * 0.22f).coerceIn(-50f, 50f)
                val targetVx = ((dxToPlayer + leadOffset) * obs.trackingStrength).coerceIn(-obs.maxTrackingVx, obs.maxTrackingVx)
                obs.vx += (targetVx - obs.vx) * (obs.trackingResponse * dt)
            } else if (dyToPlayer <= 80f * player.sizeScale) {
                // Once within close range (<=80px) or having passed the player, lock trajectory
                // so the player can dodge past cleanly and fairly without sudden point-blank snapping!
                obs.isTrajectoryLocked = true
            }

            // B. Irregular Multi-Harmonic Waveform (Organic non-repeating trajectory swerves):
            val wave1 = sin(obs.lifetime * obs.wobbleFreq + obs.irregularSeed)
            val wave2 = cos(obs.lifetime * (obs.wobbleFreq * 1.63f) + obs.irregularSeed * 2.1f)
            val dynamicWobble = (wave1 * 0.70f + wave2 * 0.30f) * obs.wobbleAmp

            // C. Micro-Atmospheric Turbulence & Erratic Vector Shifts:
            obs.turbulenceTimer -= dt
            if (obs.turbulenceTimer <= 0f) {
                val turbStrength = 20f + random.nextFloat() * 35f
                obs.turbulenceVx = if (random.nextBoolean()) turbStrength else -turbStrength
                obs.turbulenceTimer = 0.8f + random.nextFloat() * 1.2f
            } else {
                obs.turbulenceVx *= (1f - dt * 2.5f)
            }

            // Apply total irregular velocity
            val totalVx = obs.vx + dynamicWobble + obs.turbulenceVx
            obs.x += totalVx * dt
            obs.y += (obs.vy + currentScrollSpeed) * dt

            if (obs is AsteroidObstacle) {
                obs.rotation += obs.vRot * dt

                if (obs.swayAmp > 0f) {
                    obs.swayPhase += obs.swayFreq * dt
                    obs.x += cos(obs.swayPhase) * obs.swayAmp * dt
                }

                // Meteors and fast asteroids emit trailing rock dust & sparks
                if (obs.type == AsteroidType.METEOR || obs.radius > 32f) {
                    obs.trailTimer -= dt
                    if (obs.trailTimer <= 0f) {
                        particles.add(
                            RockDustParticle(
                                x = obs.x + (random.nextFloat() - 0.5f) * obs.radius * 0.6f,
                                y = obs.y - obs.radius * 0.6f,
                                vx = (random.nextFloat() - 0.5f) * 30f,
                                vy = -40f - random.nextFloat() * 40f,
                                size = 2.5f + random.nextFloat() * 3f,
                                rotation = random.nextFloat() * 360f,
                                vRot = (random.nextFloat() - 0.5f) * 5f,
                                alpha = 0.75f,
                                maxLife = 0.6f + random.nextFloat() * 0.3f
                            )
                        )
                        obs.trailTimer = 0.08f + random.nextFloat() * 0.06f
                    }
                }
            } else if (obs is StormCloudObstacle) {
                obs.pulsePhase += dt * 3.5f
                obs.lightningPhase += dt * 4.5f
                obs.rainTimer -= dt
                if (obs.rainTimer <= 0f) {
                    obs.rainTimer = 0.07f + random.nextFloat() * 0.07f
                    // Drop storm rain particle
                    particles.add(
                        StormRainParticle(
                            x = obs.x + (random.nextFloat() - 0.5f) * obs.width * 0.7f,
                            y = obs.y + obs.height * 0.4f,
                            vx = obs.vx * 0.25f + (random.nextFloat() - 0.5f) * 20f,
                            vy = obs.vy + 130f + random.nextFloat() * 60f,
                            length = 12f + random.nextFloat() * 12f,
                            alpha = 0.7f,
                            maxLife = 0.35f
                        )
                    )
                    // Periodic lightning spark / crackle
                    if (random.nextFloat() < 0.4f) {
                        particles.add(
                            StormSparkParticle(
                                x = obs.x + (random.nextFloat() - 0.5f) * obs.width * 0.85f,
                                y = obs.y + (random.nextFloat() - 0.5f) * obs.height * 0.75f,
                                vx = (random.nextFloat() - 0.5f) * 70f,
                                vy = (random.nextFloat() - 0.5f) * 70f,
                                size = 3f + random.nextFloat() * 4f,
                                color = obs.lightningColor,
                                alpha = 1f,
                                maxLife = 0.3f
                            )
                        )
                    }
                }
            }

            // Check collision with player red balloon (critical) and man character
            if (obs.checkCollision(player.balloonCenter(), player.balloonRadius, player.childCenter(), 20f)) {
                if (player.invulnerabilityTimer > 0f) {
                    // Invulnerable / recovering from damage, safely ignore collision
                    continue
                }

                if (playerShieldTimer > 0f) {
                    // Shield deflects and destroys the obstacle (asteroid or storm cloud)
                    soundManager.playShieldDeflect()
                    soundManager.vibrateShieldHit()

                    if (obs is StormCloudObstacle) {
                        soundManager.playThunderZap()
                        for (i in 0 until 18) {
                            val ang = random.nextFloat() * Math.PI.toFloat() * 2f
                            val spd = 140f + random.nextFloat() * 200f
                            particles.add(
                                StormSparkParticle(
                                    x = obs.x,
                                    y = obs.y,
                                    vx = cos(ang) * spd,
                                    vy = sin(ang) * spd,
                                    size = 4f + random.nextFloat() * 4f,
                                    color = obs.lightningColor,
                                    alpha = 1f,
                                    maxLife = 0.45f
                                )
                            )
                        }
                    } else {
                        // Shield deflection sparks & cosmic blast particles
                        for (i in 0 until 14) {
                            val ang = random.nextFloat() * Math.PI.toFloat() * 2f
                            val spd = 130f + random.nextFloat() * 180f
                            particles.add(
                                ShieldDeflectParticle(
                                    x = obs.x,
                                    y = obs.y,
                                    vx = cos(ang) * spd,
                                    vy = sin(ang) * spd,
                                    radius = 3.5f + random.nextFloat() * 3.5f,
                                    alpha = 1f,
                                    maxLife = 0.45f + random.nextFloat() * 0.2f
                                )
                            )
                        }
                    }

                    // Destroy the obstacle
                    iterator.remove()
                    continue
                } else if (playerLives > 1) {
                    // Lose 1 life out of 3, gain brief recovery invulnerability
                    playerLives--
                    val isElectric = obs is StormCloudObstacle
                    player.invulnerabilityTimer = 2.5f

                    if (isElectric) {
                        val stormObs = obs as StormCloudObstacle
                        soundManager.vibrateElectricShock()
                        soundManager.playThunderZap()

                        // Differentiate behavior based on lightning cloud type:
                        // 1. Yellow lightning (0xFFFFEA00): Immobilize player for 1 sec & show electrocuted yellow lightning
                        // 2. Blue lightning (0xFF00E5FF): Make player 50% bigger (+50% size), making evading projectiles harder
                        // 3. Other lightning (e.g. 0xFFE040FB purple): Slow down player ascent and movement for 4s
                        if (stormObs.lightningColor == 0xFFFFEA00L) {
                            player.immobilizedTimer = 1.0f
                            player.yellowElectrifiedTimer = 1.0f
                            player.electrifiedTimer = 1.0f
                            player.vx = 0f
                            player.vy = 0f
                            tierBanner = "⚡ IMMOBILIZED! YELLOW LIGHTNING PARALYZED YOU (1s)! ($playerLives/3 LIVES)"
                        } else if (stormObs.lightningColor == 0xFF00E5FFL) {
                            player.sizeGrowthTimer = 5.0f
                            player.electrifiedTimer = 2.0f
                            tierBanner = "⚡ SURGE! BLUE LIGHTNING EXPANDED YOU +50% SIZE (5s)! ($playerLives/3 LIVES)"
                        } else {
                            player.ascentSlowTimer = 4.0f
                            player.electrifiedTimer = 2.5f
                            tierBanner = "⚡ DAMPENED! STORM LIGHTNING SLOWED YOUR ASCENT (4s)! ($playerLives/3 LIVES)"
                        }
                    } else {
                        soundManager.playLifeLost()
                        soundManager.vibrateLifeLost()
                        tierBanner = if (playerLives == 2) "LIFE LOST! 2 LIVES REMAINING" else "CRITICAL! 1 LIFE REMAINING!"
                    }

                    val bc = player.balloonCenter()
                    for (i in 0 until 16) {
                        val ang = random.nextFloat() * Math.PI.toFloat() * 2f
                        val spd = 100f + random.nextFloat() * 160f
                        particles.add(
                            RedPopParticle(
                                x = bc.x,
                                y = bc.y,
                                vx = cos(ang) * spd,
                                vy = sin(ang) * spd - 20f,
                                size = 3.5f + random.nextFloat() * 3.5f,
                                rotation = random.nextFloat() * 360f,
                                vRot = (random.nextFloat() - 0.5f) * 600f,
                                isGlossShard = i % 3 == 0,
                                maxLife = 0.6f
                            )
                        )
                    }

                    if (isElectric) {
                        val stormObs = obs as StormCloudObstacle
                        for (i in 0 until 22) {
                            val ang = random.nextFloat() * Math.PI.toFloat() * 2f
                            val spd = 60f + random.nextFloat() * 180f
                            particles.add(
                                StormSparkParticle(
                                    x = obs.x,
                                    y = obs.y,
                                    vx = cos(ang) * spd,
                                    vy = sin(ang) * spd,
                                    size = 4f + random.nextFloat() * 4f,
                                    color = stormObs.lightningColor,
                                    alpha = 1f,
                                    maxLife = 0.5f
                                )
                            )
                        }
                    } else {
                        for (i in 0 until 8) {
                            particles.add(
                                RockDustParticle(
                                    x = obs.x,
                                    y = obs.y,
                                    vx = (random.nextFloat() - 0.5f) * 120f,
                                    vy = (random.nextFloat() - 0.5f) * 120f,
                                    size = 4f + random.nextFloat() * 3f,
                                    rotation = random.nextFloat() * 360f,
                                    vRot = (random.nextFloat() - 0.5f) * 5f,
                                    alpha = 0.85f,
                                    maxLife = 0.6f
                                )
                            )
                        }
                    }

                    // Clear obstacles near player to allow safe recovery
                    val clearIter = obstacles.iterator()
                    while (clearIter.hasNext()) {
                        val nearby = clearIter.next()
                        val dxB = nearby.x - bc.x
                        val dyB = nearby.y - bc.y
                        if (dxB * dxB + dyB * dyB < 240f * 240f) {
                            clearIter.remove()
                        }
                    }

                    tierNotificationTimer = 2.8f
                    _uiState.value = _uiState.value.copy(
                        lives = playerLives,
                        screenShakeIntensity = if (isElectric) 0.95f else 0.75f,
                        tierBannerText = tierBanner,
                        isInvulnerable = true,
                        isElectrified = player.isElectrified,
                        electrifiedTimeRemaining = player.electrifiedTimer
                    )
                    return
                } else {
                    // Last life lost -> 0 lives remaining
                    playerLives = 0
                    if (obs is StormCloudObstacle) {
                        soundManager.vibrateElectricShock()
                        soundManager.playThunderZap()
                        player.electrifiedTimer = 2.0f
                        if (obs.lightningColor == 0xFFFFEA00L) {
                            player.yellowElectrifiedTimer = 2.0f
                        }
                    }
                    triggerPop()
                    return
                }
            }

            // Remove off-screen obstacles
            if (obs.y > screenHeight + 200f || obs.x < -300f || obs.x > screenWidth + 300f) {
                if (obs.y > screenHeight + 50f) {
                    val completedDodge = scoreRepository.recordMissionProgress(MissionType.DODGE_HAZARDS, 1, isIncremental = true)
                    if (completedDodge.isNotEmpty()) {
                        val m = completedDodge.first()
                        _uiState.value = _uiState.value.copy(
                            tierBannerText = "🎯 MISSION COMPLETE: ${m.title}! (+${m.coinReward} 🪙)"
                        )
                        tierNotificationTimer = 3.2f
                        soundManager.playCoinEarned()
                        soundManager.vibrateTap()
                    }
                }
                iterator.remove()
            }
        }
    }

    // ==========================================
    // PROCEDURAL ASTEROID FACTORY & SPAWN PATTERNS
    // ==========================================

    private fun createAsteroid(
        x: Float,
        y: Float,
        vx: Float,
        vy: Float,
        radius: Float,
        type: AsteroidType,
        swayAmp: Float = 0f,
        swayFreq: Float = 0f
    ): AsteroidObstacle {
        val numVertices = when (type) {
            AsteroidType.PEBBLE -> 8
            AsteroidType.METEOR -> 9
            AsteroidType.BOULDER -> 10
            AsteroidType.CRATERED_ASTEROID -> 12
            AsteroidType.MONOLITH_CHUNKS -> 12
        }

        // Generate jagged vertex radius ratios
        val vertexRatios = FloatArray(numVertices) {
            val minR = if (type == AsteroidType.PEBBLE) 0.80f else 0.72f
            val maxR = if (type == AsteroidType.PEBBLE) 1.20f else 1.28f
            minR + random.nextFloat() * (maxR - minR)
        }

        // Generate Craters
        val numCraters = when (type) {
            AsteroidType.PEBBLE -> if (random.nextFloat() < 0.3f) 1 else 0
            AsteroidType.METEOR -> 1 + random.nextInt(2)
            AsteroidType.BOULDER -> 1 + random.nextInt(3)
            AsteroidType.CRATERED_ASTEROID -> 3 + random.nextInt(3)
            AsteroidType.MONOLITH_CHUNKS -> 2 + random.nextInt(3)
        }
        val craters = mutableListOf<RockCrater>()
        for (i in 0 until numCraters) {
            val cAngle = random.nextFloat() * (Math.PI.toFloat() * 2f)
            val cDist = radius * (0.15f + random.nextFloat() * 0.45f)
            val cRad = radius * (0.14f + random.nextFloat() * 0.22f)
            craters.add(RockCrater(cAngle, cDist, cRad))
        }

        // Generate geometric Facets (polygonal shading planes)
        val facets = mutableListOf<RockFacet>()
        for (i in 0 until numVertices) {
            val nextI = (i + 1) % numVertices
            val shade = 0.20f + ((i * 37 + 19) % 100) / 100f * 0.70f
            facets.add(RockFacet(i, nextI, shade))
        }

        val rotSpeed = (if (random.nextBoolean()) 1f else -1f) * (0.8f + random.nextFloat() * 2.2f)

        val asteroid = AsteroidObstacle(
            x = x,
            y = y,
            vx = vx,
            vy = vy,
            radius = radius,
            type = type,
            vertexRatios = vertexRatios,
            craters = craters,
            facets = facets,
            rotation = random.nextFloat() * 6.28f,
            vRot = rotSpeed,
            baseHueDark = random.nextBoolean(),
            swayAmp = swayAmp,
            swayFreq = swayFreq
        )

        // Irregular non-repeating trajectory and dynamic player-targeting properties
        asteroid.irregularSeed = random.nextFloat() * 100f
        asteroid.wobbleFreq = 2.4f + random.nextFloat() * 2.6f
        asteroid.wobbleAmp = when (type) {
            AsteroidType.PEBBLE -> 24f + random.nextFloat() * 28f
            AsteroidType.METEOR -> 18f + random.nextFloat() * 22f
            AsteroidType.BOULDER -> 16f + random.nextFloat() * 20f
            AsteroidType.CRATERED_ASTEROID -> 12f + random.nextFloat() * 16f
            AsteroidType.MONOLITH_CHUNKS -> 14f + random.nextFloat() * 16f
        }
        asteroid.trackingStrength = when (type) {
            AsteroidType.PEBBLE -> 0.55f + random.nextFloat() * 0.20f
            AsteroidType.METEOR -> 0.60f + random.nextFloat() * 0.25f
            AsteroidType.BOULDER -> 0.42f + random.nextFloat() * 0.18f
            AsteroidType.CRATERED_ASTEROID -> 0.32f + random.nextFloat() * 0.15f
            AsteroidType.MONOLITH_CHUNKS -> 0.35f + random.nextFloat() * 0.15f
        }
        asteroid.trackingResponse = 3.2f + random.nextFloat() * 2.0f
        asteroid.maxTrackingVx = 140f + random.nextFloat() * 45f
        asteroid.turbulenceTimer = 0.4f + random.nextFloat() * 0.8f

        return asteroid
    }

    private fun spawnNextObstacle(altitudeStep: Int, tier: ObstacleTier) {
        val roll = random.nextFloat()
        val isCornerCamp = (player.x <= screenWidth * 0.22f || player.x >= screenWidth * 0.78f)

        // If player is camping in the corner/edge, actively flush them out
        if (isCornerCamp && random.nextFloat() < 0.75f) {
            spawnCornerFlusher(altitudeStep, tier)
            return
        }

        when (altitudeStep) {
            0 -> {
                // 0 - 199m: Gentle start, low obstacle volume with occasional storm squalls
                when {
                    roll < 0.65f -> spawnTargetedAsteroid(altitudeStep, tier)
                    roll < 0.85f -> spawnTwinGateAsteroids(altitudeStep, tier)
                    else -> spawnStormCloudBlitz(altitudeStep, tier)
                }
            }
            1 -> {
                // 200 - 399m: Moderate increase, introducing storm gates & dynamic sweepers
                when {
                    roll < 0.35f -> spawnTargetedAsteroid(altitudeStep, tier)
                    roll < 0.55f -> spawnTwinGateAsteroids(altitudeStep, tier)
                    roll < 0.70f -> spawnStormCloudBlitz(altitudeStep, tier)
                    roll < 0.85f -> spawnThunderheadGate(altitudeStep, tier)
                    else -> spawnDiagonalSweeper(altitudeStep, tier)
                }
            }
            2 -> {
                // 400 - 599m: Introducing squall barrages, targeted meteor swarms & vortex sweepers
                when {
                    roll < 0.25f -> spawnTargetedAsteroid(altitudeStep, tier)
                    roll < 0.45f -> spawnStormCloudBlitz(altitudeStep, tier)
                    roll < 0.65f -> spawnSquallBarrage(altitudeStep, tier)
                    roll < 0.80f -> spawnMeteorSwarm(altitudeStep, tier)
                    roll < 0.90f -> spawnThunderheadGate(altitudeStep, tier)
                    else -> spawnSlalomAsteroidPair(altitudeStep, tier)
                }
            }
            3 -> {
                // 600 - 799m: Asteroid belt density, fast vortex sweepers, squall barrages
                when {
                    roll < 0.20f -> spawnMeteorSwarm(altitudeStep, tier)
                    roll < 0.40f -> spawnSquallBarrage(altitudeStep, tier)
                    roll < 0.60f -> spawnVortexSweeper(altitudeStep, tier)
                    roll < 0.75f -> spawnMonolithWithShards(altitudeStep, tier)
                    roll < 0.88f -> spawnThunderheadGate(altitudeStep, tier)
                    else -> spawnTripleStream(altitudeStep, tier)
                }
            }
            else -> {
                // 800m+ (Step 4+): High velocity barrage with intense storm squall fronts and rapid sweepers
                when {
                    roll < 0.20f -> spawnSquallBarrage(altitudeStep, tier)
                    roll < 0.40f -> spawnVortexSweeper(altitudeStep, tier)
                    roll < 0.60f -> spawnStormCloudBlitz(altitudeStep, tier)
                    roll < 0.75f -> spawnMonolithWithShards(altitudeStep, tier)
                    roll < 0.88f -> spawnThunderheadGate(altitudeStep, tier)
                    else -> spawnTripleStream(altitudeStep, tier)
                }
            }
        }
    }

    /**
     * Targeted Asteroid: Actively tracks the player's position (including corners and edges)
     * and calculates the trajectory to intersect the player's coordinate.
     */
    private fun spawnTargetedAsteroid(altitudeStep: Int, tier: ObstacleTier) {
        val leadDistance = (player.vx * 0.5f).coerceIn(-120f, 120f)
        val targetX = (player.x + leadDistance + (random.nextFloat() - 0.5f) * 40f).coerceIn(25f, screenWidth - 25f)
        val startY = -60f - random.nextFloat() * 40f

        // 70% of time spawn directly above the target coordinate; 30% from offscreen angle
        val startX = if (random.nextFloat() < 0.70f) {
            (targetX + (random.nextFloat() - 0.5f) * 60f).coerceIn(20f, screenWidth - 20f)
        } else {
            if (random.nextBoolean()) -30f else screenWidth + 30f
        }

        val baseSpeed = 130f + (altitudeStep * 25f).coerceAtMost(160f) + random.nextFloat() * 40f
        val dy = (player.y - startY).coerceAtLeast(100f)
        val timeToPlayer = (dy / (baseSpeed + currentScrollSpeed)).coerceAtLeast(0.4f)
        val vx = (targetX - startX) / timeToPlayer

        val radius = 18f + random.nextFloat() * 16f
        val type = if (radius > 26f) AsteroidType.BOULDER else AsteroidType.PEBBLE

        obstacles.add(
            createAsteroid(
                x = startX,
                y = startY,
                vx = vx,
                vy = baseSpeed,
                radius = radius,
                type = type
            )
        )
    }

    /**
     * Corner Flusher: Specifically triggered when the player tries to hide in the left or right corner/edge,
     * sending asteroids and cascading rocks straight down or across that corner.
     */
    private fun spawnCornerFlusher(altitudeStep: Int, tier: ObstacleTier) {
        val isLeft = player.x < screenWidth * 0.5f
        val cornerX = if (isLeft) 28f + random.nextFloat() * 30f else screenWidth - (28f + random.nextFloat() * 30f)
        val baseSpeed = 150f + (altitudeStep * 25f).coerceAtMost(170f) + random.nextFloat() * 40f

        val count = if (altitudeStep == 0) 1 else 2
        for (i in 0 until count) {
            val offsetY = -60f - (i * 70f)
            val startX = cornerX + (random.nextFloat() - 0.5f) * 20f
            obstacles.add(
                createAsteroid(
                    x = startX,
                    y = offsetY,
                    vx = (if (isLeft) 10f else -10f) * (random.nextFloat()),
                    vy = baseSpeed + i * 20f,
                    radius = 20f + random.nextFloat() * 10f,
                    type = AsteroidType.BOULDER
                )
            )
        }
    }

    /**
     * Twin Asteroids falling side-by-side with an open gap between them, dynamically centered across full width
     * or aimed to force the player out of corners.
     */
    private fun spawnTwinGateAsteroids(altitudeStep: Int, tier: ObstacleTier) {
        val leadDistance = (player.vx * 0.4f).coerceIn(-80f, 80f)
        val playerTargetX = (player.x + leadDistance).coerceIn(40f, screenWidth - 40f)

        // Bias gate center around player position or full screen width
        val gateCenterX = if (random.nextFloat() < 0.60f) {
            playerTargetX
        } else {
            screenWidth * (0.18f + random.nextFloat() * 0.64f)
        }

        val gapWidth = (220f - altitudeStep * 14f).coerceIn(145f, 220f)
        val startY = -70f
        val vy = 130f + (altitudeStep * 22f).coerceAtMost(140f) + random.nextFloat() * 40f

        val leftX = (gateCenterX - gapWidth * 0.5f).coerceIn(25f, screenWidth - 25f)
        val rightX = (gateCenterX + gapWidth * 0.5f).coerceIn(25f, screenWidth - 25f)

        obstacles.add(
            createAsteroid(
                x = leftX,
                y = startY,
                vx = 0f,
                vy = vy,
                radius = 22f + random.nextFloat() * 8f,
                type = AsteroidType.BOULDER
            )
        )
        obstacles.add(
            createAsteroid(
                x = rightX,
                y = startY,
                vx = 0f,
                vy = vy,
                radius = 22f + random.nextFloat() * 8f,
                type = AsteroidType.BOULDER
            )
        )
    }

    /**
     * Meteor Swarm: fast glowing shards cutting diagonally in staggered succession,
     * targeted through the player's position.
     */
    private fun spawnMeteorSwarm(altitudeStep: Int, tier: ObstacleTier) {
        val count = (2 + (altitudeStep / 2)).coerceIn(2, 4)
        val fromLeft = if (player.x < screenWidth * 0.5f) {
            random.nextFloat() < 0.60f
        } else {
            random.nextFloat() < 0.40f
        }
        val startBaseX = if (fromLeft) -40f else screenWidth + 40f
        val targetX = player.x.coerceIn(25f, screenWidth - 25f)
        val baseSpeedY = 170f + (altitudeStep * 25f).coerceAtMost(160f)

        for (i in 0 until count) {
            val startY = -60f - (i * 65f)
            val dy = (player.y - startY).coerceAtLeast(100f)
            val timeToPlayer = (dy / (baseSpeedY + currentScrollSpeed)).coerceAtLeast(0.4f)
            val reqVx = (targetX + (i - count / 2) * 45f - startBaseX) / timeToPlayer

            obstacles.add(
                createAsteroid(
                    x = startBaseX,
                    y = startY,
                    vx = reqVx,
                    vy = baseSpeedY + i * 15f,
                    radius = 13f + random.nextFloat() * 6f,
                    type = AsteroidType.METEOR
                )
            )
        }
    }

    /**
     * Slalom Pair: Two rocks staggered relative to the player's position, forcing an S-curve weave.
     */
    private fun spawnSlalomAsteroidPair(altitudeStep: Int, tier: ObstacleTier) {
        val speedY = 140f + (altitudeStep * 20f).coerceAtMost(130f) + random.nextFloat() * 30f
        val px = player.x.coerceIn(40f, screenWidth - 40f)
        val offset = if (px < screenWidth * 0.5f) 140f else -140f
        val x1 = px
        val x2 = (px + offset).coerceIn(30f, screenWidth - 30f)

        obstacles.add(
            createAsteroid(
                x = x1,
                y = -50f,
                vx = if (offset > 0) 25f else -25f,
                vy = speedY,
                radius = 22f + random.nextFloat() * 7f,
                type = AsteroidType.BOULDER
            )
        )
        obstacles.add(
            createAsteroid(
                x = x2,
                y = -140f,
                vx = if (offset > 0) -25f else 25f,
                vy = speedY,
                radius = 22f + random.nextFloat() * 7f,
                type = AsteroidType.BOULDER
            )
        )
    }

    /**
     * Diagonal Sweeper: A high-angle asteroid cutting across the screen through the player's location.
     */
    private fun spawnDiagonalSweeper(altitudeStep: Int, tier: ObstacleTier) {
        val fromLeft = player.x >= screenWidth * 0.5f
        val startX = if (fromLeft) -40f else screenWidth + 40f
        val targetX = player.x.coerceIn(25f, screenWidth - 25f)
        val startY = -60f - random.nextFloat() * 30f
        val vy = 160f + (altitudeStep * 22f).coerceAtMost(140f) + random.nextFloat() * 40f
        val dy = (player.y - startY).coerceAtLeast(100f)
        val timeToPlayer = (dy / (vy + currentScrollSpeed)).coerceAtLeast(0.4f)
        val reqVx = (targetX - startX) / timeToPlayer

        obstacles.add(
            createAsteroid(
                x = startX,
                y = startY,
                vx = reqVx,
                vy = vy,
                radius = 24f + random.nextFloat() * 10f,
                type = AsteroidType.BOULDER
            )
        )
    }

    /**
     * Heavy Monolith Asteroid with accompanying small pebble shards across width.
     */
    private fun spawnMonolithWithShards(altitudeStep: Int, tier: ObstacleTier) {
        val leadDistance = (player.vx * 0.3f).coerceIn(-60f, 60f)
        val targetX = (player.x + leadDistance + (random.nextFloat() - 0.5f) * 60f).coerceIn(40f, screenWidth - 40f)
        val startY = -110f
        val vy = 120f + (altitudeStep * 20f).coerceAtMost(120f)

        // Heavy central asteroid
        obstacles.add(
            createAsteroid(
                x = targetX,
                y = startY,
                vx = (random.nextFloat() - 0.5f) * 40f,
                vy = vy,
                radius = 36f + random.nextFloat() * 10f,
                type = AsteroidType.CRATERED_ASTEROID
            )
        )

        val shardCount = if (altitudeStep >= 4) 2 else 1
        for (i in 0 until shardCount) {
            val offsetX = if (i % 2 == 0) -65f else 65f
            obstacles.add(
                createAsteroid(
                    x = (targetX + offsetX).coerceIn(25f, screenWidth - 25f),
                    y = startY + (random.nextFloat() - 0.5f) * 30f,
                    vx = (random.nextFloat() - 0.5f) * 50f,
                    vy = vy + 30f + random.nextFloat() * 25f,
                    radius = 12f + random.nextFloat() * 5f,
                    type = AsteroidType.PEBBLE
                )
            )
        }
    }

    /**
     * Triple stream: 3 pebbles spaced across width covering left corner, center, and right corner.
     */
    private fun spawnTripleStream(altitudeStep: Int, tier: ObstacleTier) {
        val vy = 150f + (altitudeStep * 20f).coerceAtMost(130f) + random.nextFloat() * 30f

        val col1 = 30f + random.nextFloat() * 20f // Left corner coverage
        val col2 = screenWidth * 0.50f + (random.nextFloat() - 0.5f) * 30f // Center
        val col3 = screenWidth - (30f + random.nextFloat() * 20f) // Right corner coverage

        val positions = listOf(col1, col2, col3)
        for (pos in positions) {
            obstacles.add(
                createAsteroid(
                    x = pos,
                    y = -60f - random.nextFloat() * 50f,
                    vx = (random.nextFloat() - 0.5f) * 30f,
                    vy = vy,
                    radius = 14f + random.nextFloat() * 6f,
                    type = AsteroidType.PEBBLE
                )
            )
        }
    }

    // ==========================================
    // PROCEDURAL STORM CLOUD FACTORY & PATTERNS
    // ==========================================

    private fun createStormCloud(
        x: Float,
        y: Float,
        vx: Float,
        vy: Float,
        width: Float = 84f,
        height: Float = 46f,
        type: StormCloudType = StormCloudType.THUNDERHEAD,
        lightningColor: Long = 0xFF00E5FF
    ): StormCloudObstacle {
        val cloud = StormCloudObstacle(
            x = x,
            y = y,
            vx = vx,
            vy = vy,
            width = width,
            height = height,
            type = type,
            lightningPhase = random.nextFloat() * 10f,
            pulsePhase = random.nextFloat() * 6.28f,
            rainTimer = random.nextFloat() * 0.1f,
            lightningColor = lightningColor
        )

        // Irregular non-repeating trajectory and dynamic atmospheric wind-swirl targeting properties
        cloud.irregularSeed = random.nextFloat() * 100f
        cloud.wobbleFreq = 2.0f + random.nextFloat() * 2.2f
        cloud.wobbleAmp = when (type) {
            StormCloudType.LIGHTNING_BLITZ -> 25f + random.nextFloat() * 25f
            StormCloudType.VORTEX_CYCLONE -> 28f + random.nextFloat() * 28f
            StormCloudType.SQUALL_GUST -> 22f + random.nextFloat() * 20f
            StormCloudType.THUNDERHEAD -> 16f + random.nextFloat() * 18f
        }
        cloud.trackingStrength = when (type) {
            StormCloudType.LIGHTNING_BLITZ -> 0.58f + random.nextFloat() * 0.20f
            StormCloudType.VORTEX_CYCLONE -> 0.50f + random.nextFloat() * 0.20f
            StormCloudType.SQUALL_GUST -> 0.45f + random.nextFloat() * 0.18f
            StormCloudType.THUNDERHEAD -> 0.36f + random.nextFloat() * 0.16f
        }
        cloud.trackingResponse = 3.0f + random.nextFloat() * 2.0f
        cloud.maxTrackingVx = 145f + random.nextFloat() * 40f
        cloud.turbulenceTimer = 0.5f + random.nextFloat() * 0.9f

        return cloud
    }

    /**
     * Storm Cloud Blitz: High-speed charged storm cloud cutting quickly through player's path
     * with crackling lightning arcs and fast vertical drop (+40-70% speed).
     */
    fun spawnStormCloudBlitz(altitudeStep: Int, tier: ObstacleTier) {
        val leadDistance = (player.vx * 0.45f).coerceIn(-100f, 100f)
        val targetX = (player.x + leadDistance + (random.nextFloat() - 0.5f) * 50f).coerceIn(35f, screenWidth - 35f)
        val startY = -80f - random.nextFloat() * 30f

        val startX = if (random.nextFloat() < 0.65f) {
            (targetX + (random.nextFloat() - 0.5f) * 80f).coerceIn(30f, screenWidth - 30f)
        } else {
            if (random.nextBoolean()) -40f else screenWidth + 40f
        }

        val baseSpeed = 220f + (altitudeStep * 30f).coerceAtMost(200f) + random.nextFloat() * 50f
        val dy = (player.y - startY).coerceAtLeast(100f)
        val timeToPlayer = (dy / (baseSpeed + currentScrollSpeed)).coerceAtLeast(0.35f)
        val vx = (targetX - startX) / timeToPlayer

        val width = 75f + random.nextFloat() * 30f
        val height = 42f + random.nextFloat() * 16f
        val color = when (random.nextInt(3)) {
            0 -> 0xFFFFEA00L // Yellow lightning (immobilize)
            1 -> 0xFF00E5FFL // Blue lightning (+50% size)
            else -> 0xFFE040FBL // Purple storm lightning (slowdown)
        }

        obstacles.add(
            createStormCloud(
                x = startX,
                y = startY,
                vx = vx,
                vy = baseSpeed,
                width = width,
                height = height,
                type = StormCloudType.LIGHTNING_BLITZ,
                lightningColor = color
            )
        )
    }

    /**
     * Thunderhead Gate: Twin heavy storm clouds descending with an open gap between them
     * that crackles with electricity, forcing agile positioning.
     */
    private fun spawnThunderheadGate(altitudeStep: Int, tier: ObstacleTier) {
        val leadDistance = (player.vx * 0.35f).coerceIn(-60f, 60f)
        val playerTargetX = (player.x + leadDistance).coerceIn(50f, screenWidth - 50f)

        val gateCenterX = if (random.nextFloat() < 0.60f) {
            playerTargetX
        } else {
            screenWidth * (0.22f + random.nextFloat() * 0.56f)
        }

        val gapWidth = (240f - altitudeStep * 15f).coerceIn(160f, 240f)
        val startY = -85f
        val vy = 190f + (altitudeStep * 24f).coerceAtMost(160f) + random.nextFloat() * 40f

        val leftX = (gateCenterX - gapWidth * 0.55f).coerceIn(30f, screenWidth - 30f)
        val rightX = (gateCenterX + gapWidth * 0.55f).coerceIn(30f, screenWidth - 30f)

        val leftColor = if (random.nextBoolean()) 0xFFFFEA00L else 0xFF00E5FFL
        val rightColor = if (leftColor == 0xFFFFEA00L) (if (random.nextBoolean()) 0xFF00E5FFL else 0xFFE040FBL) else 0xFFFFEA00L

        obstacles.add(
            createStormCloud(
                x = leftX,
                y = startY,
                vx = 0f,
                vy = vy,
                width = 85f + random.nextFloat() * 20f,
                height = 48f + random.nextFloat() * 12f,
                type = StormCloudType.THUNDERHEAD,
                lightningColor = leftColor
            )
        )
        obstacles.add(
            createStormCloud(
                x = rightX,
                y = startY,
                vx = 0f,
                vy = vy,
                width = 85f + random.nextFloat() * 20f,
                height = 48f + random.nextFloat() * 12f,
                type = StormCloudType.THUNDERHEAD,
                lightningColor = rightColor
            )
        )
    }

    /**
     * Squall Barrage: Staggered descending cluster of 2 to 3 storm clouds with alternating lanes,
     * requiring rapid zig-zag dodging.
     */
    private fun spawnSquallBarrage(altitudeStep: Int, tier: ObstacleTier) {
        val count = if (altitudeStep >= 3) 3 else 2
        val baseSpeed = 200f + (altitudeStep * 28f).coerceAtMost(180f)

        for (i in 0 until count) {
            val startY = -70f - (i * 80f)
            val posX = when (i % 3) {
                0 -> 50f + random.nextFloat() * 60f
                1 -> screenWidth - (50f + random.nextFloat() * 60f)
                else -> screenWidth * 0.5f + (random.nextFloat() - 0.5f) * 60f
            }

            val color = when (i % 3) {
                0 -> 0xFFFFEA00L // Yellow
                1 -> 0xFF00E5FFL // Blue
                else -> 0xFFE040FBL // Purple / Storm
            }

            obstacles.add(
                createStormCloud(
                    x = posX,
                    y = startY,
                    vx = (random.nextFloat() - 0.5f) * 40f,
                    vy = baseSpeed + (i * 20f),
                    width = 80f + random.nextFloat() * 25f,
                    height = 44f + random.nextFloat() * 14f,
                    type = StormCloudType.SQUALL_GUST,
                    lightningColor = color
                )
            )
        }
    }

    /**
     * Vortex Sweeper: Fast storm cloud swooping diagonally across the full screen with wind shear.
     */
    private fun spawnVortexSweeper(altitudeStep: Int, tier: ObstacleTier) {
        val fromLeft = player.x >= screenWidth * 0.5f
        val startX = if (fromLeft) -50f else screenWidth + 50f
        val targetX = player.x.coerceIn(30f, screenWidth - 30f)
        val startY = -75f - random.nextFloat() * 30f
        val vy = 210f + (altitudeStep * 26f).coerceAtMost(170f) + random.nextFloat() * 40f
        val dy = (player.y - startY).coerceAtLeast(100f)
        val timeToPlayer = (dy / (vy + currentScrollSpeed)).coerceAtLeast(0.35f)
        val reqVx = (targetX - startX) / timeToPlayer

        val color = when (random.nextInt(3)) {
            0 -> 0xFFFFEA00L
            1 -> 0xFF00E5FFL
            else -> 0xFFE040FBL
        }

        obstacles.add(
            createStormCloud(
                x = startX,
                y = startY,
                vx = reqVx,
                vy = vy,
                width = 95f + random.nextFloat() * 25f,
                height = 50f + random.nextFloat() * 15f,
                type = StormCloudType.VORTEX_CYCLONE,
                lightningColor = color
            )
        )
    }

    private fun updateGameOverScene(dt: Float) {
        player.popTime += dt

        // Child tumbles downwards
        player.vy += 450f * dt
        player.y += player.vy * dt
        player.childAngularVelocity += 6f * dt
        player.childAngle += player.childAngularVelocity * dt

        // Decay screen shake
        val shake = _uiState.value.screenShakeIntensity * (1f - dt * 4f)
        if (shake > 0.01f) {
            _uiState.value = _uiState.value.copy(screenShakeIntensity = shake)
            screenShakeOffset = Offset(
                (random.nextFloat() - 0.5f) * shake * 24f,
                (random.nextFloat() - 0.5f) * shake * 24f
            )
        } else {
            screenShakeOffset = Offset.Zero
        }
    }

    private fun updateClouds(dt: Float) {
        // Vertical cloud movement speed coupled to flight status and ascent rate
        val verticalAscent = when (_uiState.value.status) {
            GameStatus.PLAYING -> currentScrollSpeed
            GameStatus.TAKEOFF -> currentScrollSpeed * player.takeoffProgress
            else -> 18f
        }

        val playerVx = if (_uiState.value.status == GameStatus.PLAYING) player.vx else 0f

        for (cloud in parallaxClouds) {
            // Distinct multi-layer parallax speed multipliers
            val verticalFactor = when (cloud.parallaxLayer) {
                0 -> 0.28f  // High distant cirrus
                1 -> 0.70f  // Mid-altitude cumulus
                else -> 1.35f // Low rushing foreground cloud bank
            }

            val horizontalDriftFactor = when (cloud.parallaxLayer) {
                0 -> 0.40f
                1 -> 0.75f
                else -> 1.20f
            }

            val playerSwayFactor = when (cloud.parallaxLayer) {
                0 -> -0.04f
                1 -> -0.12f
                else -> -0.28f
            }

            // Move clouds horizontally (natural wind drift + player parallax shift)
            cloud.x += (cloud.speedX * horizontalDriftFactor + playerVx * playerSwayFactor) * dt
            // Move clouds vertically downwards (ascent parallax speed)
            cloud.y += (verticalAscent * verticalFactor) * dt

            // Wrap around horizontal borders
            if (cloud.x > screenWidth + cloud.width + 40f) {
                cloud.x = -cloud.width - 30f
            } else if (cloud.x < -cloud.width - 40f) {
                cloud.x = screenWidth + 30f
            }

            // Wrap around vertical borders
            if (cloud.y > screenHeight + cloud.height + 40f) {
                cloud.y = -cloud.height - random.nextFloat() * 100f
                cloud.x = random.nextFloat() * (screenWidth + 200f) - 100f
            } else if (cloud.y < -cloud.height - 120f) {
                cloud.y = screenHeight + 20f
            }
        }
    }

    private fun updateParticles(dt: Float) {
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.life += dt
            p.x += p.vx * dt
            p.y += p.vy * dt

            when (p) {
                is RedPopParticle -> {
                    p.rotation += p.vRot * dt
                    p.vy += 320f * dt // Gravity on shards
                    p.alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
                }
                is SparkleParticle -> {
                    p.alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
                }
                is DustParticle -> {
                    p.alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
                }
                is FeatherParticle -> {
                    p.swayPhase += dt * 3.5f
                    p.x += sin(p.swayPhase) * 20f * dt
                    p.rotation = sin(p.swayPhase) * 0.45f
                    p.alpha = ((1f - p.life / p.maxLife) * 0.75f).coerceIn(0f, 1f)
                }
                is DandelionFluffParticle -> {
                    p.bobPhase += dt * 3f
                    p.y += sin(p.bobPhase) * 8f * dt
                    p.alpha = ((1f - p.life / p.maxLife) * 0.6f).coerceIn(0f, 1f)
                }
                is WindStreakParticle -> {
                    p.alpha = ((1f - p.life / p.maxLife) * 0.35f).coerceIn(0f, 1f)
                }
                is RockDustParticle -> {
                    p.rotation += p.vRot * dt * 50f
                    p.alpha = ((1f - p.life / p.maxLife) * 0.75f).coerceIn(0f, 1f)
                }
                is ShieldDeflectParticle -> {
                    p.alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
                }
                is SlipstreamSparkParticle -> {
                    p.rotation += dt * 300f
                    p.alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
                }
                is HeartFloatingParticle -> {
                    p.rotation += p.vRot * dt
                    p.alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
                }
                is StormSparkParticle -> {
                    p.alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
                }
                is StormRainParticle -> {
                    p.alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
                }
            }

            if (!p.isAlive) {
                iter.remove()
            }
        }
    }

    fun triggerPop() {
        if (player.isPopped) return
        player.isPopped = true
        player.vy = 60f
        player.childAngularVelocity = if (random.nextBoolean()) 4f else -4f

        soundManager.playPop()
        soundManager.vibratePop()

        val balloonCenter = player.balloonCenter()

        // 1. Red Balloon Burst Shards (Vivid red fragments exploding outward)
        val shardCount = 28
        for (i in 0 until shardCount) {
            val angle = (i.toFloat() / shardCount) * (Math.PI.toFloat() * 2f) + (random.nextFloat() * 0.4f - 0.2f)
            val speed = 180f + random.nextFloat() * 260f
            particles.add(
                RedPopParticle(
                    x = balloonCenter.x,
                    y = balloonCenter.y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 60f,
                    size = 5f + random.nextFloat() * 7f,
                    rotation = random.nextFloat() * 360f,
                    vRot = (random.nextFloat() - 0.5f) * 720f,
                    isGlossShard = i % 4 == 0,
                    maxLife = 0.9f + random.nextFloat() * 0.4f
                )
            )
        }

        // If player has ad revives remaining (up to 5 chances), offer revive prompt!
        if (adRevivesRemaining > 0) {
            _uiState.value = _uiState.value.copy(
                status = GameStatus.AD_REVIVE_PROMPT,
                lives = 0,
                adRevivesRemaining = adRevivesRemaining,
                screenShakeIntensity = 0.9f,
                tierBannerText = "OUT OF LIVES!"
            )
            tierNotificationTimer = 2.0f
        } else {
            // No ad revives remaining -> Final Game Over
            soundManager.stopBackgroundMusic()
            _uiState.value = _uiState.value.copy(
                status = GameStatus.GAME_OVER,
                lives = 0,
                adRevivesRemaining = 0,
                screenShakeIntensity = 1.0f
            )

            // Save local stats quietly
            scoreRepository.saveGameResult(
                score = _uiState.value.score,
                altitudeMeters = _uiState.value.altitudeMeters,
                timeSeconds = gameTime.toLong()
            )
        }
    }

    fun startWatchingAd() {
        if (_uiState.value.status != GameStatus.AD_REVIVE_PROMPT) return
        _uiState.value = _uiState.value.copy(status = GameStatus.WATCHING_AD)
    }

    fun reviveFromAd() {
        if (adRevivesRemaining <= 0) return
        adRevivesRemaining--
        playerLives = 1
        player.isPopped = false
        player.popTime = 0f
        player.invulnerabilityTimer = 3.0f
        player.vy = 0f
        player.childAngle = 0f
        player.childAngularVelocity = 0f
        playerShieldTimer = 0f
        playerSpeedBoostTimer = 0f

        // Clear obstacles nearby
        val bc = player.balloonCenter()
        val clearIter = obstacles.iterator()
        while (clearIter.hasNext()) {
            val nearby = clearIter.next()
            val dxB = nearby.x - bc.x
            val dyB = nearby.y - bc.y
            if (dxB * dxB + dyB * dyB < 350f * 350f) {
                clearIter.remove()
            }
        }

        soundManager.playAdRevive()
        soundManager.vibrateLifeGain()
        soundManager.resumeBackgroundMusic()

        _uiState.value = _uiState.value.copy(
            status = GameStatus.PLAYING,
            lives = 1,
            adRevivesRemaining = adRevivesRemaining,
            tierBannerText = "REVIVED! +1 LIFE RESTORED ($adRevivesRemaining/5 AD REVIVES LEFT)",
            screenShakeIntensity = 0f,
            isInvulnerable = true
        )
        tierNotificationTimer = 3.2f
    }

    fun declineAdRevive() {
        if (_uiState.value.status != GameStatus.AD_REVIVE_PROMPT && _uiState.value.status != GameStatus.WATCHING_AD) return
        soundManager.stopBackgroundMusic()
        _uiState.value = _uiState.value.copy(
            status = GameStatus.GAME_OVER,
            lives = 0
        )
        scoreRepository.saveGameResult(
            score = _uiState.value.score,
            altitudeMeters = _uiState.value.altitudeMeters,
            timeSeconds = gameTime.toLong()
        )
    }

    fun equipSkin(skin: CharacterSkin) {
        if (scoreRepository.isSkinUnlocked(skin.id)) {
            scoreRepository.setSelectedSkinId(skin.id)
            player.skin = skin
            _uiState.value = _uiState.value.copy(currentSkin = skin)
            soundManager.playClick()
            soundManager.vibrateTap()
        }
    }

    fun unlockSkin(skin: CharacterSkin): Boolean {
        if (scoreRepository.isSkinUnlocked(skin.id)) {
            equipSkin(skin)
            return true
        }
        if (scoreRepository.spendCoins(skin.cost)) {
            scoreRepository.unlockSkin(skin.id)
            scoreRepository.setSelectedSkinId(skin.id)
            player.skin = skin
            soundManager.playSkinUnlock()
            soundManager.vibrateLifeGain()
            _uiState.value = _uiState.value.copy(
                totalCoins = scoreRepository.getCoins(),
                currentSkin = skin,
                tierBannerText = "UNLOCKED: ${skin.name.uppercase()}!"
            )
            tierNotificationTimer = 2.8f
            return true
        }
        return false
    }

    fun addAdBonusCoins(amount: Int = 1) {
        val updatedCoins = scoreRepository.addCoins(amount)
        soundManager.playCoinEarned()
        soundManager.vibrateLifeGain()
        _uiState.value = _uiState.value.copy(
            totalCoins = updatedCoins,
            tierBannerText = "+$amount BONUS COIN${if (amount > 1) "S" else ""} GRANTED!"
        )
        tierNotificationTimer = 2.8f
    }

    fun refreshCoins() {
        _uiState.value = _uiState.value.copy(
            totalCoins = scoreRepository.getCoins()
        )
    }

    fun goToReadyMenu() {
        soundManager.stopBackgroundMusic()
        resetToGround()
        val savedSkinId = scoreRepository.getSelectedSkinId()
        val skin = AvailableCharacterSkins.find { it.id == savedSkinId } ?: DefaultCharacterSkin
        player.skin = skin
        _uiState.value = _uiState.value.copy(
            status = GameStatus.READY,
            score = 0L,
            altitudeMeters = 0L,
            currentTier = ObstacleTier.TIER_1_ASTEROID_DRIFT,
            tierBannerText = null,
            lives = 3,
            totalCoins = scoreRepository.getCoins(),
            maxAltitudeEver = scoreRepository.getMaxAltitude(),
            currentSkin = skin
        )
    }
}
