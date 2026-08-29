package com.example.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class GameStatus {
    READY,
    TAKEOFF,
    PLAYING,
    PAUSED,
    AD_REVIVE_PROMPT,
    WATCHING_AD,
    GAME_OVER
}

enum class CharacterGender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female"),
    UNISEX("Unisex")
}

typealias GenderStyle = CharacterGender

enum class HatStyle {
    AERONAUT_CAP,
    BERET,
    HOOD,
    VISOR,
    AVIATOR_HELMET,
    GOGGLE_CAP,
    AVIATOR_GOGGLES,
    FLORAL_CROWN,
    NONE
}

data class CharacterSkin(
    val id: String,
    val name: String,
    val gender: CharacterGender,
    val tagline: String,
    val cost: Int,
    val colorCoat: Long,
    val colorTrim: Long,
    val colorScarf: Long,
    val colorHat: Long,
    val colorPants: Long = 0xFF141418L,
    val colorHair: Long = 0xFF282830L,
    val hasPonytail: Boolean = false,
    val hasGoggles: Boolean = false,
    val hasBraids: Boolean = false,
    val hatStyle: HatStyle = HatStyle.AERONAUT_CAP
) {
    val coatColor: Color get() = Color(colorCoat)
    val trimColor: Color get() = Color(colorTrim)
    val scarfColor: Color get() = Color(colorScarf)
    val hatColor: Color get() = Color(colorHat)
    val hatAccentColor: Color get() = Color(colorTrim)
    val pantsColor: Color get() = Color(colorPants)
    val bootColor: Color get() = Color(0xFF101014)
    val hairColor: Color get() = Color(colorHair)
    val skinToneColor: Color get() = if (id == "cyber_overcharge") Color(0xFF1E2838) else Color(0xFF18181C)
    val description: String get() = tagline
    val hasBraid: Boolean get() = hasBraids
}

val DefaultCharacterSkin = CharacterSkin(
    id = "classic_shadow",
    name = "Classic Shadow",
    gender = CharacterGender.MALE,
    tagline = "The iconic obsidian aeronaut coat and cap",
    cost = 0,
    colorCoat = 0xFF141418L,
    colorTrim = 0xFF646476L,
    colorScarf = 0xFF3E3E4AL,
    colorHat = 0xFF141418L,
    colorPants = 0xFF101014L,
    colorHair = 0xFF222228L,
    hatStyle = HatStyle.AERONAUT_CAP
)

val AvailableCharacterSkins = listOf(
    DefaultCharacterSkin,
    CharacterSkin(
        id = "crimson_aviator",
        name = "Crimson Pilot",
        gender = CharacterGender.MALE,
        tagline = "Bold scarlet flight coat with gilded badges",
        cost = 25,
        colorCoat = 0xFFB71C1CL,
        colorTrim = 0xFFFFD700L,
        colorScarf = 0xFFE53935L,
        colorHat = 0xFF8E0000L,
        colorPants = 0xFF1E1E24L,
        colorHair = 0xFF212121L,
        hatStyle = HatStyle.AERONAUT_CAP
    ),
    CharacterSkin(
        id = "golden_sunbeam",
        name = "Golden Sunbeam",
        gender = CharacterGender.FEMALE,
        tagline = "Radiant amber flight tunic with fluttering ponytail",
        cost = 35,
        colorCoat = 0xFFE6A100L,
        colorTrim = 0xFFFFF8E1L,
        colorScarf = 0xFFFFE082L,
        colorHat = 0xFFFFA000L,
        colorPants = 0xFF2C2216L,
        colorHair = 0xFFFFCA28L,
        hasPonytail = true,
        hatStyle = HatStyle.BERET
    ),
    CharacterSkin(
        id = "vintage_leather",
        name = "Vintage Leather",
        gender = CharacterGender.MALE,
        tagline = "Classic chestnut bomber with brass aviator goggles",
        cost = 50,
        colorCoat = 0xFF5D4037L,
        colorTrim = 0xFFD7CCC8L,
        colorScarf = 0xFF8D6E63L,
        colorHat = 0xFF4E342EL,
        colorPants = 0xFF271C18L,
        colorHair = 0xFF3E2723L,
        hasGoggles = true,
        hatStyle = HatStyle.GOGGLE_CAP
    ),
    CharacterSkin(
        id = "skyline_cadet",
        name = "Skyline Cadet",
        gender = CharacterGender.FEMALE,
        tagline = "Sleek cobalt aerodynamic suit with braided hair",
        cost = 70,
        colorCoat = 0xFF1565C0L,
        colorTrim = 0xFFBBDEFBL,
        colorScarf = 0xFF64B5F6L,
        colorHat = 0xFF0D47A1L,
        colorPants = 0xFF0A2440L,
        colorHair = 0xFF37251BL,
        hasBraids = true,
        hasPonytail = true,
        hatStyle = HatStyle.BERET
    ),
    CharacterSkin(
        id = "slate_explorer",
        name = "Slate Explorer",
        gender = CharacterGender.MALE,
        tagline = "Alpine slate-grey thermal parka and arctic visor",
        cost = 95,
        colorCoat = 0xFF455A64L,
        colorTrim = 0xFFECEFF1L,
        colorScarf = 0xFF90A4AEL,
        colorHat = 0xFF263238L,
        colorPants = 0xFF1C2428L,
        colorHair = 0xFF303038L,
        hatStyle = HatStyle.HOOD
    ),
    CharacterSkin(
        id = "emerald_voyager",
        name = "Emerald Voyager",
        gender = CharacterGender.FEMALE,
        tagline = "Deep emerald glider coat with silk jade scarf",
        cost = 125,
        colorCoat = 0xFF1B5E20L,
        colorTrim = 0xFFC8E6C9L,
        colorScarf = 0xFF4CAF50L,
        colorHat = 0xFF0D3E12L,
        colorPants = 0xFF122814L,
        colorHair = 0xFF4E342EL,
        hasPonytail = true,
        hatStyle = HatStyle.AERONAUT_CAP
    ),
    CharacterSkin(
        id = "cyber_overcharge",
        name = "Cyber Overcharge",
        gender = CharacterGender.UNISEX,
        tagline = "Electric neon cyan synth-suit with optic visor",
        cost = 165,
        colorCoat = 0xFF00E5FFL,
        colorTrim = 0xFFFFEA00L,
        colorScarf = 0xFF18FFFFL,
        colorHat = 0xFF00B0FFL,
        colorPants = 0xFF0A1420L,
        colorHair = 0xFF00E5FFL,
        hatStyle = HatStyle.VISOR
    ),
    CharacterSkin(
        id = "royal_amethyst",
        name = "Royal Velvet",
        gender = CharacterGender.FEMALE,
        tagline = "Regal deep amethyst mantle with gold filigree",
        cost = 210,
        colorCoat = 0xFF4A148CL,
        colorTrim = 0xFFFFD700L,
        colorScarf = 0xFF8E24AAL,
        colorHat = 0xFF311B92L,
        colorPants = 0xFF1F0B3AL,
        colorHair = 0xFF4A148CL,
        hasPonytail = true,
        hatStyle = HatStyle.BERET
    ),
    CharacterSkin(
        id = "solar_flare",
        name = "Solar Flare",
        gender = CharacterGender.MALE,
        tagline = "Blazing solar orange flight gear with fiery accents",
        cost = 260,
        colorCoat = 0xFFE65100L,
        colorTrim = 0xFFFFCC80L,
        colorScarf = 0xFFFF6D00L,
        colorHat = 0xFFBF360CL,
        colorPants = 0xFF2A1005L,
        colorHair = 0xFF3E2723L,
        hatStyle = HatStyle.AVIATOR_HELMET
    )
)

enum class ObstacleTier(val minScore: Long, val displayName: String, val subtitle: String) {
    TIER_1_ASTEROID_DRIFT(0L, "Low Orbit Drift", "Evade drifting meteor fragments and tumbling pebbles"),
    TIER_2_METEOR_SHOWER(350L, "Meteor Swarm", "Navigate through sharper angled falling space rocks"),
    TIER_3_ASTEROID_BELT(900L, "Asteroid Belt", "Weave through fast tumbling boulders and dual-shard clusters"),
    TIER_4_DEEP_SPACE_BARRAGE(2000L, "High-Velocity Barrage", "Dodge high-speed meteors and massive rotating asteroids")
}

data class Player(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var balloonRadius: Float = 26f,
    var isHoldingLift: Boolean = false,
    var balloonBobPhase: Float = 0f,
    var balloonSwayAngle: Float = 0f,
    var balloonStretchY: Float = 1.0f,
    var verticalSwayPhase: Float = 0f,
    var childAngle: Float = 0f,
    var childAngularVelocity: Float = 0f,
    var legSwingPhase: Float = 0f,
    var isPopped: Boolean = false,
    var popTime: Float = 0f,
    var invulnerabilityTimer: Float = 0f,
    var isOnGround: Boolean = true,
    var takeoffProgress: Float = 0f, // 0f = on ground, 1f = fully airborne
    var electrifiedTimer: Float = 0f,
    var yellowElectrifiedTimer: Float = 0f,
    var immobilizedTimer: Float = 0f,
    var sizeGrowthTimer: Float = 0f,
    var ascentSlowTimer: Float = 0f,
    var electricJitterX: Float = 0f,
    var electricJitterY: Float = 0f,
    var skin: CharacterSkin = DefaultCharacterSkin
) {
    val isElectrified: Boolean get() = electrifiedTimer > 0f || yellowElectrifiedTimer > 0f
    val isImmobilized: Boolean get() = immobilizedTimer > 0f
    val isSizeExpanded: Boolean get() = sizeGrowthTimer > 0f
    val sizeScale: Float get() = if (sizeGrowthTimer > 0f) 1.5f else 1.0f
    val isAscentSlowed: Boolean get() = ascentSlowTimer > 0f

    val effectiveBalloonRadius: Float get() = balloonRadius * sizeScale

    // Collision bounding circle for the red balloon (critical hit area)
    fun balloonCenter(): Offset = Offset(x, y - effectiveBalloonRadius)

    // Man character hand grip position (where balloon string connects)
    fun stringBottom(): Offset = Offset(x - 7f * sizeScale, y + 20f * sizeScale)

    // Regular sized man character body bounding box / center
    fun childCenter(): Offset = Offset(x, y + 36f * sizeScale)
    fun characterCenter(): Offset = Offset(x, y + 36f * sizeScale)
}

data class SkylineBuilding(
    val relX: Float,
    val width: Float,
    val height: Float,
    val layer: Int, // 0 = background light silhouette, 1 = foreground dark silhouette
    val hasAntenna: Boolean = false,
    val antennaHeight: Float = 0f,
    val hasSpire: Boolean = false,
    val windowCols: Int = 0,
    val windowRows: Int = 0
)

sealed class Obstacle {
    abstract var x: Float
    abstract var y: Float
    abstract var vx: Float
    abstract var vy: Float
    abstract var isPassed: Boolean

    // Dynamic Irregular Trajectory & Player-Targeting Parameters
    var irregularSeed: Float = 0f
    var wobbleFreq: Float = 3.2f
    var wobbleAmp: Float = 0f
    var trackingStrength: Float = 0.45f
    var trackingResponse: Float = 4.0f
    var maxTrackingVx: Float = 175f
    var turbulenceTimer: Float = 0f
    var turbulenceVx: Float = 0f
    var lifetime: Float = 0f
    var isTrajectoryLocked: Boolean = false

    abstract fun checkCollision(balloonCenter: Offset, balloonRadius: Float, childCenter: Offset, childRadius: Float): Boolean
}

enum class StormCloudType {
    THUNDERHEAD,      // Dark heavy cumulonimbus with internal flashing lightning
    SQUALL_GUST,      // Fast turbulent storm gust cloud moving at high velocity
    LIGHTNING_BLITZ,  // Super-fast charged cloud crackling with bright electric arcs
    VORTEX_CYCLONE    // Dense atmospheric vortex cloud with heavy wind shear
}

data class StormCloudObstacle(
    override var x: Float,
    override var y: Float,
    override var vx: Float = 0f,
    override var vy: Float = 220f,
    override var isPassed: Boolean = false,
    val width: Float = 84f,
    val height: Float = 46f,
    val type: StormCloudType = StormCloudType.THUNDERHEAD,
    var lightningPhase: Float = 0f,
    var pulsePhase: Float = 0f,
    var rainTimer: Float = 0f,
    var rotation: Float = 0f,
    val lightningColor: Long = 0xFF00E5FF
) : Obstacle() {
    override fun checkCollision(balloonCenter: Offset, balloonRadius: Float, childCenter: Offset, childRadius: Float): Boolean {
        val halfW = width * 0.46f
        val halfH = height * 0.44f

        // Balloon hit check against storm cloud bounding box
        val dxB = (balloonCenter.x - x).coerceIn(-halfW, halfW)
        val dyB = (balloonCenter.y - y).coerceIn(-halfH, halfH)
        val clX_B = x + dxB
        val clY_B = y + dyB
        val distBSq = (balloonCenter.x - clX_B) * (balloonCenter.x - clX_B) + (balloonCenter.y - clY_B) * (balloonCenter.y - clY_B)
        if (distBSq < (balloonRadius * 0.9f) * (balloonRadius * 0.9f)) return true

        // Man character hit check
        val dxC = (childCenter.x - x).coerceIn(-halfW, halfW)
        val dyC = (childCenter.y - y).coerceIn(-halfH, halfH)
        val clX_C = x + dxC
        val clY_C = y + dyC
        val distCSq = (childCenter.x - clX_C) * (childCenter.x - clX_C) + (childCenter.y - clY_C) * (childCenter.y - clY_C)
        return distCSq < (childRadius * 0.85f) * (childRadius * 0.85f)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StormCloudObstacle) return false
        return x == other.x && y == other.y && vx == other.vx && vy == other.vy && width == other.width
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + width.hashCode()
        return result
    }
}

enum class AsteroidType {
    PEBBLE,           // Small, fast jagged shard
    BOULDER,          // Medium jagged cratered rock
    CRATERED_ASTEROID,// Heavy tumbling asteroid with craters
    METEOR,           // Fast glowing-rim fiery/mineral meteor with trail
    MONOLITH_CHUNKS   // Large geometric faceted asteroid block
}

data class RockCrater(
    val relAngle: Float,
    val relDist: Float,
    val radius: Float
)

data class RockFacet(
    val vIndex1: Int,
    val vIndex2: Int,
    val shadeFactor: Float // 0f = darkest, 1f = brightest
)

data class AsteroidObstacle(
    override var x: Float,
    override var y: Float,
    override var vx: Float = 0f,
    override var vy: Float = 160f,
    override var isPassed: Boolean = false,
    val radius: Float = 24f,
    val type: AsteroidType = AsteroidType.BOULDER,
    // Angular vertex jitter ratios (e.g. 8-12 angles around the circle)
    val vertexRatios: FloatArray,
    val craters: List<RockCrater> = emptyList(),
    val facets: List<RockFacet> = emptyList(),
    var rotation: Float = 0f,
    val vRot: Float = 1.2f,
    val baseHueDark: Boolean = false,
    var trailTimer: Float = 0f,
    var swayPhase: Float = 0f,
    val swayAmp: Float = 0f,
    val swayFreq: Float = 0f
) : Obstacle() {
    override fun checkCollision(balloonCenter: Offset, balloonRadius: Float, childCenter: Offset, childRadius: Float): Boolean {
        // Balloon critical hit area
        val dxB = balloonCenter.x - x
        val dyB = balloonCenter.y - y
        val distBSq = dxB * dxB + dyB * dyB
        val hitRadiusB = balloonRadius + radius * 0.82f
        if (distBSq < hitRadiusB * hitRadiusB) return true

        // Child character hit area
        val dxC = childCenter.x - x
        val dyC = childCenter.y - y
        val distCSq = dxC * dxC + dyC * dyC
        val hitRadiusC = childRadius + radius * 0.75f
        return distCSq < hitRadiusC * hitRadiusC
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AsteroidObstacle) return false
        return x == other.x && y == other.y && vx == other.vx && vy == other.vy && radius == other.radius
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + radius.hashCode()
        return result
    }
}

// Particle Models
sealed class GameParticle {
    abstract var x: Float
    abstract var y: Float
    abstract var vx: Float
    abstract var vy: Float
    abstract var alpha: Float
    abstract var life: Float
    abstract var maxLife: Float
    abstract val isAlive: Boolean
}

data class RockDustParticle(
    override var x: Float,
    override var y: Float,
    override var vx: Float,
    override var vy: Float,
    var rotation: Float = 0f,
    var vRot: Float = 2f,
    val size: Float = 5f,
    override var alpha: Float = 0.8f,
    override var life: Float = 0f,
    override var maxLife: Float = 1.8f
) : GameParticle() {
    override val isAlive: Boolean get() = life < maxLife
}

data class FeatherParticle(
    override var x: Float,
    override var y: Float,
    override var vx: Float,
    override var vy: Float,
    var rotation: Float = 0f,
    var swayPhase: Float = 0f,
    val size: Float = 14f,
    override var alpha: Float = 0.75f,
    override var life: Float = 0f,
    override var maxLife: Float = 2.4f
) : GameParticle() {
    override val isAlive: Boolean get() = life < maxLife
}

data class DandelionFluffParticle(
    override var x: Float,
    override var y: Float,
    override var vx: Float,
    override var vy: Float,
    var bobPhase: Float = 0f,
    val radius: Float = 4f,
    override var alpha: Float = 0.6f,
    override var life: Float = 0f,
    override var maxLife: Float = 3.0f
) : GameParticle() {
    override val isAlive: Boolean get() = life < maxLife
}

data class RedPopParticle(
    override var x: Float,
    override var y: Float,
    override var vx: Float,
    override var vy: Float,
    var size: Float,
    var rotation: Float,
    var vRot: Float,
    override var alpha: Float = 1f,
    override var life: Float = 0f,
    override var maxLife: Float = 1.1f,
    val isGlossShard: Boolean = false
) : GameParticle() {
    override val isAlive: Boolean get() = life < maxLife
}

data class SparkleParticle(
    override var x: Float,
    override var y: Float,
    override var vx: Float,
    override var vy: Float,
    var size: Float = 4f,
    override var alpha: Float = 1f,
    override var life: Float = 0f,
    override var maxLife: Float = 0.6f
) : GameParticle() {
    override val isAlive: Boolean get() = life < maxLife
}

data class DustParticle(
    override var x: Float,
    override var y: Float,
    override var vx: Float,
    override var vy: Float,
    var radius: Float = 3f,
    override var alpha: Float = 0.8f,
    override var life: Float = 0f,
    override var maxLife: Float = 0.7f
) : GameParticle() {
    override val isAlive: Boolean get() = life < maxLife
}

data class WindStreakParticle(
    override var x: Float,
    override var y: Float,
    override var vx: Float,
    override var vy: Float,
    val length: Float,
    override var alpha: Float = 0.4f,
    override var life: Float = 0f,
    override var maxLife: Float = 1.0f
) : GameParticle() {
    override val isAlive: Boolean get() = life < maxLife
}

data class ParallaxCloud(
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    val speedX: Float,
    val parallaxLayer: Int, // 0 = background slow, 1 = midground, 2 = foreground fast
    val alpha: Float,
    val isPixelStyle: Boolean = true
)

enum class PowerUpType(
    val displayName: String,
    val defaultDuration: Float,
    val shortLabel: String
) {
    SHIELD("KINETIC SHIELD", 8.0f, "SHIELD"),
    SPEED_BOOST("TURBO SLIPSTREAM", 6.0f, "SPEED"),
    HEART("HEART CONTAINER", 0.0f, "+1 LIFE")
}

data class PowerUpItem(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 110f,
    val type: PowerUpType,
    val radius: Float = 22f,
    var pulsePhase: Float = 0f,
    var rotation: Float = 0f,
    var isCollected: Boolean = false
) {
    fun checkPickup(balloonCenter: Offset, balloonRadius: Float, childCenter: Offset, childRadius: Float): Boolean {
        val pickupRadius = radius + 14f
        val dxB = balloonCenter.x - x
        val dyB = balloonCenter.y - y
        if (dxB * dxB + dyB * dyB < (balloonRadius + pickupRadius) * (balloonRadius + pickupRadius)) return true

        val dxC = childCenter.x - x
        val dyC = childCenter.y - y
        return (dxC * dxC + dyC * dyC < (childRadius + pickupRadius) * (childRadius + pickupRadius))
    }
}

data class ShieldDeflectParticle(
    override var x: Float,
    override var y: Float,
    override var vx: Float,
    override var vy: Float,
    var radius: Float = 4f,
    override var alpha: Float = 1f,
    override var life: Float = 0f,
    override var maxLife: Float = 0.5f
) : GameParticle() {
    override val isAlive: Boolean get() = life < maxLife
}

data class SlipstreamSparkParticle(
    override var x: Float,
    override var y: Float,
    override var vx: Float,
    override var vy: Float,
    var size: Float = 5f,
    var rotation: Float = 0f,
    override var alpha: Float = 0.9f,
    override var life: Float = 0f,
    override var maxLife: Float = 0.45f
) : GameParticle() {
    override val isAlive: Boolean get() = life < maxLife
}

data class HeartFloatingParticle(
    override var x: Float,
    override var y: Float,
    override var vx: Float,
    override var vy: Float,
    var size: Float = 7f,
    var rotation: Float = 0f,
    var vRot: Float = 0f,
    override var alpha: Float = 1f,
    override var life: Float = 0f,
    override var maxLife: Float = 0.85f
) : GameParticle() {
    override val isAlive: Boolean get() = life < maxLife
}

data class StormSparkParticle(
    override var x: Float,
    override var y: Float,
    override var vx: Float,
    override var vy: Float,
    var size: Float = 4f,
    val color: Long = 0xFF00E5FF,
    override var alpha: Float = 1f,
    override var life: Float = 0f,
    override var maxLife: Float = 0.35f
) : GameParticle() {
    override val isAlive: Boolean get() = life < maxLife
}

data class StormRainParticle(
    override var x: Float,
    override var y: Float,
    override var vx: Float,
    override var vy: Float,
    var length: Float = 12f,
    override var alpha: Float = 0.7f,
    override var life: Float = 0f,
    override var maxLife: Float = 0.4f
) : GameParticle() {
    override val isAlive: Boolean get() = life < maxLife
}

// Helper: Distance from a point to a line segment
fun distToSegment(p: Offset, v: Offset, w: Offset): Float {
    val l2 = (v.x - w.x) * (v.x - w.x) + (v.y - w.y) * (v.y - w.y)
    if (l2 == 0f) {
        val dx = p.x - v.x
        val dy = p.y - v.y
        return sqrt(dx * dx + dy * dy)
    }
    val t = (((p.x - v.x) * (w.x - v.x) + (p.y - v.y) * (w.y - v.y)) / l2).coerceIn(0f, 1f)
    val projX = v.x + t * (w.x - v.x)
    val projY = v.y + t * (w.y - v.y)
    val dx = p.x - projX
    val dy = p.y - projY
    return sqrt(dx * dx + dy * dy)
}
