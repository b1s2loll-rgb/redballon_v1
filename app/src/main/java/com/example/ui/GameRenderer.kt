package com.example.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.example.engine.GameEngine
import com.example.model.AsteroidObstacle
import com.example.model.AsteroidType
import com.example.model.CharacterSkin
import com.example.model.DandelionFluffParticle
import com.example.model.DustParticle
import com.example.model.FeatherParticle
import com.example.model.GameParticle
import com.example.model.GameStatus
import com.example.model.HatStyle
import com.example.model.HeartFloatingParticle
import com.example.model.Obstacle
import com.example.model.ParallaxCloud
import com.example.model.Player
import com.example.model.PowerUpItem
import com.example.model.PowerUpType
import com.example.model.RedPopParticle
import com.example.model.RockDustParticle
import com.example.model.ShieldDeflectParticle
import com.example.model.SkylineBuilding
import com.example.model.SlipstreamSparkParticle
import com.example.model.SparkleParticle
import com.example.model.StormCloudObstacle
import com.example.model.StormCloudType
import com.example.model.StormRainParticle
import com.example.model.StormSparkParticle
import com.example.model.WindStreakParticle
import com.example.ui.theme.BalloonRed
import com.example.ui.theme.BalloonRedBright
import com.example.ui.theme.BalloonRedDark
import com.example.ui.theme.BalloonRedGloss
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.DeepGray
import com.example.ui.theme.LightSilver
import com.example.ui.theme.MidGray
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGray
import kotlin.math.cos
import kotlin.math.sin

object GameRenderer {

    fun drawGame(drawScope: DrawScope, engine: GameEngine) {
        with(drawScope) {
            val shake = engine.screenShakeOffset
            translate(shake.x, shake.y) {
                // 1. Sky Canvas Background
                drawSkyBackground(size.width, size.height)

                // 2. Cityscape & Skyscrapers with multi-layered 3D parallax scrolling
                drawCitySkyline(
                    buildings = engine.skylineBuildings,
                    groundY = engine.groundY,
                    groundOffsetY = engine.groundOffsetY,
                    cameraParallaxX = engine.cameraParallaxX,
                    status = engine.uiState.value.status
                )

                // 3. Parallax Drifting Clouds across 3 depth strata
                drawParallaxClouds(engine.parallaxClouds)

                // 4. Ground: Completely flat surface (a clean line with no spikes)
                drawGround(size.width, engine.groundY, engine.groundOffsetY, engine.uiState.value.status)

                // 5. Ambient Wind & Mist Streaks
                drawWindStreaks(engine.particles)

                // 6. Obstacles (Asteroids, Meteors, Space Boulders)
                drawObstacles(engine.obstacles)

                // 7. Rare Power-Up Collectibles (Kinetic Shield, Turbo Speed Surge)
                drawPowerUpCollectibles(engine.powerUpItems)

                // 8. Player (Monochrome Child + THE ONLY RED OBJECT: THE VIVID RED BALLOON + Power-Up Auras)
                drawPlayer(
                    player = engine.player,
                    status = engine.uiState.value.status,
                    shieldTimer = engine.playerShieldTimer,
                    speedBoostTimer = engine.playerSpeedBoostTimer,
                    invulnerabilityTimer = engine.player.invulnerabilityTimer
                )

                // 9. Dynamic Particles (Shards, Dust, Sparks, Feathers, Shield Blast)
                drawParticles(engine.particles)
            }
        }
    }

    private fun DrawScope.drawSkyBackground(width: Float, height: Float) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFFFFFFF),
                    Color(0xFFF7F7FA)
                ),
                startY = 0f,
                endY = height
            ),
            size = Size(width, height)
        )
    }

    private fun DrawScope.drawParallaxClouds(clouds: List<ParallaxCloud>) {
        // Sort clouds by layer so background renders first, foreground in front
        val sortedClouds = clouds.sortedBy { it.parallaxLayer }

        for (cloud in sortedClouds) {
            val cloudColor = when (cloud.parallaxLayer) {
                0 -> Color(0xFFE8E8F2).copy(alpha = cloud.alpha)
                1 -> Color(0xFFD4D4E0).copy(alpha = cloud.alpha)
                else -> Color(0xFFBFBFCB).copy(alpha = cloud.alpha)
            }

            val outlineColor = when (cloud.parallaxLayer) {
                0 -> Color(0xFFDCDCE8).copy(alpha = cloud.alpha * 0.7f)
                1 -> Color(0xFFA6A6B6).copy(alpha = cloud.alpha * 0.85f)
                else -> Color(0xFF525260).copy(alpha = cloud.alpha)
            }

            val strokeWidth = when (cloud.parallaxLayer) {
                0 -> 1.0f
                1 -> 1.4f
                else -> 1.8f
            }

            val cx = cloud.x
            val cy = cloud.y
            val w = cloud.width
            val h = cloud.height

            val cloudPath = Path().apply {
                moveTo(cx - w * 0.42f, cy + h * 0.3f)
                cubicTo(
                    cx - w * 0.58f, cy + h * 0.3f,
                    cx - w * 0.58f, cy - h * 0.12f,
                    cx - w * 0.36f, cy - h * 0.16f
                )
                cubicTo(
                    cx - w * 0.26f, cy - h * 0.58f,
                    cx + w * 0.26f, cy - h * 0.58f,
                    cx + w * 0.36f, cy - h * 0.16f
                )
                cubicTo(
                    cx + w * 0.58f, cy - h * 0.12f,
                    cx + w * 0.58f, cy + h * 0.3f,
                    cx + w * 0.42f, cy + h * 0.3f
                )
                close()
            }

            drawPath(path = cloudPath, color = cloudColor, style = Fill)
            drawPath(path = cloudPath, color = outlineColor, style = Stroke(width = strokeWidth))

            // For foreground and midground clouds, add top highlight rim to accentuate depth
            if (cloud.parallaxLayer >= 1) {
                val rimPath = Path().apply {
                    moveTo(cx - w * 0.22f, cy - h * 0.48f)
                    cubicTo(
                        cx - w * 0.1f, cy - h * 0.54f,
                        cx + w * 0.1f, cy - h * 0.54f,
                        cx + w * 0.22f, cy - h * 0.48f
                    )
                }
                drawPath(
                    path = rimPath,
                    color = Color.White.copy(alpha = cloud.alpha * 0.6f),
                    style = Stroke(width = 2.0f, cap = StrokeCap.Round)
                )
            }
        }
    }

    private fun DrawScope.drawCitySkyline(
        buildings: List<SkylineBuilding>,
        groundY: Float,
        groundOffsetY: Float,
        cameraParallaxX: Float,
        status: GameStatus
    ) {
        // 1. Draw Layer 0: Far Distance Monumental Skyscrapers (Slowest vertical sink & slowest horizontal shift)
        val groundY0 = groundY + groundOffsetY * 0.28f
        val offsetX0 = -cameraParallaxX * 0.12f
        if (groundY0 <= size.height + 400f || status == GameStatus.READY) {
            for (b in buildings) {
                if (b.layer != 0) continue
                val bTop = groundY0 - b.height
                val bLeft = b.relX + offsetX0

                // Building body
                drawRect(
                    color = Color(0xFFD6D6E2),
                    topLeft = Offset(bLeft, bTop),
                    size = Size(b.width, b.height + 60f)
                )
                drawRect(
                    color = Color(0xFFB8B8C8),
                    topLeft = Offset(bLeft, bTop),
                    size = Size(b.width, b.height + 60f),
                    style = Stroke(width = 1.2f)
                )

                // Spire / Antenna
                if (b.hasSpire) {
                    val spirePath = Path().apply {
                        moveTo(bLeft + b.width * 0.3f, bTop)
                        lineTo(bLeft + b.width * 0.5f, bTop - 40f)
                        lineTo(bLeft + b.width * 0.7f, bTop)
                        close()
                    }
                    drawPath(path = spirePath, color = Color(0xFFB8B8C8), style = Fill)
                } else if (b.hasAntenna) {
                    drawLine(
                        color = Color(0xFF9898A8),
                        start = Offset(bLeft + b.width * 0.5f, bTop),
                        end = Offset(bLeft + b.width * 0.5f, bTop - b.antennaHeight),
                        strokeWidth = 1.8f
                    )
                }

                // Window Grid Pattern
                val winW = 5f
                val winH = 7f
                val padX = (b.width - (b.windowCols * winW)) / (b.windowCols + 1)
                val padY = 14f

                for (row in 1 until b.windowRows) {
                    val winY = bTop + row * padY
                    if (winY > groundY0 - 10f) break
                    for (col in 0 until b.windowCols) {
                        val winX = bLeft + padX + col * (winW + padX)
                        drawRect(
                            color = Color(0xFFEAEAFA),
                            topLeft = Offset(winX, winY),
                            size = Size(winW, winH)
                        )
                    }
                }
            }
        }

        // 2. Draw Layer 1: Midground City High-Rises (Moderate vertical & horizontal parallax)
        val groundY1 = groundY + groundOffsetY * 0.62f
        val offsetX1 = -cameraParallaxX * 0.32f
        if (groundY1 <= size.height + 400f || status == GameStatus.READY) {
            for (b in buildings) {
                if (b.layer != 1) continue
                val bTop = groundY1 - b.height
                val bLeft = b.relX + offsetX1

                // Building body
                drawRect(
                    color = Color(0xFF888898),
                    topLeft = Offset(bLeft, bTop),
                    size = Size(b.width, b.height + 60f)
                )
                drawRect(
                    color = Color(0xFF555562),
                    topLeft = Offset(bLeft, bTop),
                    size = Size(b.width, b.height + 60f),
                    style = Stroke(width = 1.4f)
                )

                // Rooftop structures (spire or antennae)
                if (b.hasSpire) {
                    drawRect(
                        color = Color(0xFF555562),
                        topLeft = Offset(bLeft + b.width * 0.2f, bTop - 18f),
                        size = Size(b.width * 0.6f, 18f)
                    )
                } else if (b.hasAntenna) {
                    drawLine(
                        color = Color(0xFF40404C),
                        start = Offset(bLeft + b.width * 0.5f, bTop),
                        end = Offset(bLeft + b.width * 0.5f, bTop - b.antennaHeight),
                        strokeWidth = 2.0f
                    )
                }

                // Window Grid Pattern
                val winW = 5.5f
                val winH = 8f
                val padX = (b.width - (b.windowCols * winW)) / (b.windowCols + 1)
                val padY = 15f

                for (row in 1 until b.windowRows) {
                    val winY = bTop + row * padY
                    if (winY > groundY1 - 10f) break
                    for (col in 0 until b.windowCols) {
                        val winX = bLeft + padX + col * (winW + padX)
                        drawRect(
                            color = Color(0xFFE4E4F0),
                            topLeft = Offset(winX, winY),
                            size = Size(winW, winH)
                        )
                    }
                }
            }
        }

        // 3. Draw Layer 2: Foreground Urban Skyline & Rooftops (Fastest vertical & horizontal shift)
        val groundY2 = groundY + groundOffsetY * 1.0f
        val offsetX2 = -cameraParallaxX * 0.60f
        if (groundY2 <= size.height + 400f || status == GameStatus.READY) {
            for (b in buildings) {
                if (b.layer != 2) continue
                val bTop = groundY2 - b.height
                val bLeft = b.relX + offsetX2

                // Building body
                drawRect(
                    color = Color(0xFF383844),
                    topLeft = Offset(bLeft, bTop),
                    size = Size(b.width, b.height + 60f)
                )
                drawRect(
                    color = Color(0xFF1E1E26),
                    topLeft = Offset(bLeft, bTop),
                    size = Size(b.width, b.height + 60f),
                    style = Stroke(width = 1.6f)
                )

                // Rooftop detail (setback or water tower)
                if (b.hasSpire) {
                    drawRect(
                        color = Color(0xFF24242E),
                        topLeft = Offset(bLeft + b.width * 0.25f, bTop - 14f),
                        size = Size(b.width * 0.5f, 14f)
                    )
                } else if (b.hasAntenna) {
                    drawLine(
                        color = Color(0xFF181820),
                        start = Offset(bLeft + b.width * 0.5f, bTop),
                        end = Offset(bLeft + b.width * 0.5f, bTop - b.antennaHeight),
                        strokeWidth = 2.2f
                    )
                }

                // Foreground Windows
                val winW = 6f
                val winH = 9f
                val padX = (b.width - (b.windowCols * winW)) / (b.windowCols + 1)
                val padY = 16f

                for (row in 1 until b.windowRows) {
                    val winY = bTop + row * padY
                    if (winY > groundY2 - 10f) break
                    for (col in 0 until b.windowCols) {
                        val winX = bLeft + padX + col * (winW + padX)
                        drawRect(
                            color = Color(0xFFF2F2FC),
                            topLeft = Offset(winX, winY),
                            size = Size(winW, winH)
                        )
                    }
                }
            }
        }
    }

    private fun DrawScope.drawGround(
        width: Float,
        groundY: Float,
        groundOffsetY: Float,
        status: GameStatus
    ) {
        val currentY = groundY + groundOffsetY
        if (currentY > size.height + 100f && status != GameStatus.READY) return

        // Completely Flat Ground Platform Base (Dark Charcoal / Black)
        drawRect(
            color = Color(0xFF1E1E24),
            topLeft = Offset(0f, currentY),
            size = Size(width, size.height - currentY + 150f)
        )

        // Flat Top Border Line (Pure Flat Line with No Spikes)
        drawLine(
            color = PureBlack,
            start = Offset(0f, currentY),
            end = Offset(width, currentY),
            strokeWidth = 3.5f
        )
    }

    private fun DrawScope.drawWindStreaks(particles: List<GameParticle>) {
        for (p in particles) {
            if (p is WindStreakParticle) {
                drawLine(
                    color = Color(0xFF888896).copy(alpha = p.alpha),
                    start = Offset(p.x, p.y),
                    end = Offset(p.x + p.vx * 0.08f, p.y + p.length),
                    strokeWidth = 1.6f,
                    cap = StrokeCap.Round
                )
            }
        }
    }

    private fun DrawScope.drawObstacles(obstacles: List<Obstacle>) {
        for (obs in obstacles) {
            when (obs) {
                is AsteroidObstacle -> drawAsteroid(obs)
                is StormCloudObstacle -> drawStormCloud(obs)
            }
        }
    }

    private fun DrawScope.drawStormCloud(cloud: StormCloudObstacle) {
        val cx = cloud.x
        val cy = cloud.y
        val w = cloud.width
        val h = cloud.height
        val pulse = sin(cloud.pulsePhase) * 2.5f

        // 1. Dark atmospheric thunder aura
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF14141E).copy(alpha = 0.45f),
                    Color(cloud.lightningColor).copy(alpha = 0.15f),
                    Color(0x00000000)
                ),
                center = Offset(cx, cy),
                radius = w * 0.75f
            ),
            topLeft = Offset(cx - w * 0.75f, cy - h * 0.8f),
            size = Size(w * 1.5f, h * 1.6f)
        )

        // 2. Storm Cloud Body - Multi-lobed billowing mass
        val cloudBodyPath = Path().apply {
            // Base bottom lobe
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = cx - w * 0.48f,
                    top = cy - h * 0.15f + pulse * 0.3f,
                    right = cx + w * 0.48f,
                    bottom = cy + h * 0.45f,
                    radiusX = 14f,
                    radiusY = 14f
                )
            )
        }
        drawPath(
            path = cloudBodyPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2E2E3C),
                    Color(0xFF161620)
                ),
                startY = cy - h * 0.5f,
                endY = cy + h * 0.5f
            ),
            style = Fill
        )

        // Billowing circular puffs/lobes
        data class CloudLobe(val x: Float, val y: Float, val radius: Float)
        val lobes = listOf(
            CloudLobe(cx - w * 0.28f, cy - h * 0.10f, w * 0.26f),
            CloudLobe(cx - w * 0.05f, cy - h * 0.25f, w * 0.32f + pulse * 0.4f),
            CloudLobe(cx + w * 0.22f, cy - h * 0.12f, w * 0.25f),
            CloudLobe(cx + w * 0.36f, cy + h * 0.05f, w * 0.20f),
            CloudLobe(cx - w * 0.38f, cy + h * 0.05f, w * 0.20f)
        )

        for (lobe in lobes) {
            val lx = lobe.x
            val ly = lobe.y
            val lr = lobe.radius
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF38384A),
                        Color(0xFF222230),
                        Color(0xFF12121A)
                    ),
                    center = Offset(lx - lr * 0.2f, ly - lr * 0.25f),
                    radius = lr * 1.1f
                ),
                radius = lr,
                center = Offset(lx, ly)
            )
            drawCircle(
                color = PureBlack,
                radius = lr,
                center = Offset(lx, ly),
                style = Stroke(width = 2.2f)
            )
        }

        // Silver-white rim highlights along top lobes
        for (lobe in lobes) {
            val lx = lobe.x
            val ly = lobe.y
            val lr = lobe.radius
            drawArc(
                color = Color(0xFFD4D4E8).copy(alpha = 0.55f),
                startAngle = 200f,
                sweepAngle = 130f,
                useCenter = false,
                topLeft = Offset(lx - lr, ly - lr),
                size = Size(lr * 2f, lr * 2f),
                style = Stroke(width = 2.0f, cap = StrokeCap.Round)
            )
        }

        // 3. Electric Lightning Discharge (periodic intense flash)
        val flashCycle: Float = cloud.lightningPhase % 2.6f
        if (flashCycle < 0.45f) {
            val flashAlpha: Float = (1f - flashCycle / 0.45f).coerceIn(0f, 1f)
            val boltColor = Color(cloud.lightningColor).copy(alpha = flashAlpha)
            val boltCore = PureWhite.copy(alpha = flashAlpha)

            // Dynamic lightning zig-zag path
            val boltPath = Path().apply {
                val startX = cx + (sin(cloud.lightningPhase * 12f) * w * 0.25f)
                val startY = cy - h * 0.15f
                moveTo(startX, startY)
                lineTo(startX - 10f, startY + h * 0.35f)
                lineTo(startX + 6f, startY + h * 0.40f)
                lineTo(startX - 14f, startY + h * 0.85f)
                lineTo(startX + 2f, startY + h * 0.90f)
                lineTo(startX - 12f, startY + h * 1.35f)
            }

            // Outer electric bloom
            drawPath(
                path = boltPath,
                color = boltColor,
                style = Stroke(width = 5.0f, cap = StrokeCap.Round, join = StrokeJoin.Miter)
            )
            // Inner incandescent core
            drawPath(
                path = boltPath,
                color = boltCore,
                style = Stroke(width = 2.0f, cap = StrokeCap.Round, join = StrokeJoin.Miter)
            )

            // Electric terminal spark at tip
            drawCircle(
                color = boltCore,
                radius = 3.5f,
                center = Offset(cx - 12f, cy + h * 1.35f)
            )
        }

        // Heavy bottom dark base stroke
        drawRoundRect(
            color = PureBlack,
            topLeft = Offset(cx - w * 0.48f, cy - h * 0.15f + pulse * 0.3f),
            size = Size(w * 0.96f, h * 0.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f),
            style = Stroke(width = 2.4f)
        )
    }

    private fun DrawScope.drawAsteroid(asteroid: AsteroidObstacle) {
        val cx = asteroid.x
        val cy = asteroid.y
        val r = asteroid.radius
        val numVertices = asteroid.vertexRatios.size

        rotate(degrees = Math.toDegrees(asteroid.rotation.toDouble()).toFloat(), pivot = Offset(cx, cy)) {
            // 1. Meteor special effects: Fiery/glowing shockwave aura and blazing tail trail
            if (asteroid.type == AsteroidType.METEOR) {
                // Atmospheric entry shock cone
                val conePath = Path().apply {
                    moveTo(cx - r * 1.5f, cy - r * 0.4f)
                    lineTo(cx, cy + r * 1.6f)
                    lineTo(cx + r * 1.5f, cy - r * 0.4f)
                    close()
                }
                drawPath(
                    path = conePath,
                    color = Color(0xFFE8E8EE).copy(alpha = 0.28f),
                    style = Fill
                )

                // High-contrast trailing heat streaks
                for (i in -2..2) {
                    val sx = cx + (i * r * 0.45f)
                    val streakLen = r * (1.8f + kotlin.math.abs(i) * 0.4f)
                    drawLine(
                        color = Color(0xFF6A6A78).copy(alpha = 0.65f),
                        start = Offset(sx, cy - r * 0.5f),
                        end = Offset(sx, cy - r * 0.5f - streakLen),
                        strokeWidth = 2.0f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 2. Compute Polygon Vertex Offsets
            val vertices = Array(numVertices) { i ->
                val angle = (i.toFloat() / numVertices) * (Math.PI.toFloat() * 2f)
                val vertexR = r * asteroid.vertexRatios[i]
                Offset(
                    cx + cos(angle) * vertexR,
                    cy + sin(angle) * vertexR
                )
            }

            // 3. Draw Polygon Outer Silhouette Fill
            val baseColor = if (asteroid.baseHueDark) Color(0xFF1E1E26) else Color(0xFF2C2C36)
            val outerPath = Path().apply {
                moveTo(vertices[0].x, vertices[0].y)
                for (i in 1 until numVertices) {
                    lineTo(vertices[i].x, vertices[i].y)
                }
                close()
            }
            drawPath(path = outerPath, color = baseColor, style = Fill)

            // 4. Draw Chiseled Geometric Facets (3D rock crystalline plane shading)
            val centerPoint = Offset(cx, cy)
            for (facet in asteroid.facets) {
                val v1 = vertices[facet.vIndex1.coerceIn(0, numVertices - 1)]
                val v2 = vertices[facet.vIndex2.coerceIn(0, numVertices - 1)]

                val facetPath = Path().apply {
                    moveTo(centerPoint.x, centerPoint.y)
                    lineTo(v1.x, v1.y)
                    lineTo(v2.x, v2.y)
                    close()
                }

                // Interpolate shade from dark charcoal to silver
                val shadeAlpha = (facet.shadeFactor * 0.65f).coerceIn(0.1f, 0.75f)
                val facetColor = if (facet.shadeFactor > 0.55f) {
                    Color(0xFF888898).copy(alpha = shadeAlpha)
                } else {
                    Color(0xFF121218).copy(alpha = shadeAlpha)
                }

                drawPath(path = facetPath, color = facetColor, style = Fill)

                // Facet edge line
                drawLine(
                    color = PureBlack.copy(alpha = 0.55f),
                    start = centerPoint,
                    end = v1,
                    strokeWidth = 1.2f
                )
            }

            // 5. Draw Craters with depth shading & rims
            for (crater in asteroid.craters) {
                val crX = cx + cos(crater.relAngle) * crater.relDist
                val crY = cy + sin(crater.relAngle) * crater.relDist
                val crR = crater.radius

                // Crater dark cavity
                drawCircle(
                    color = Color(0xFF0F0F14),
                    radius = crR,
                    center = Offset(crX, crY)
                )

                // Crater rim highlight (monochrome top/bottom bevel)
                drawArc(
                    color = Color(0xFF9090A0),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(crX - crR, crY - crR),
                    size = Size(crR * 2f, crR * 2f),
                    style = Stroke(width = 1.4f)
                )

                // Outer crater outline
                drawCircle(
                    color = PureBlack,
                    radius = crR,
                    center = Offset(crX, crY),
                    style = Stroke(width = 1.6f)
                )
            }

            // 6. Draw Crisp Heavy Ink Silhouette Outline
            drawPath(path = outerPath, color = PureBlack, style = Stroke(width = 2.4f, join = StrokeJoin.Round))

            // 7. Specular Rim Highlight along top facets for depth
            if (asteroid.type != AsteroidType.PEBBLE) {
                val topRimPath = Path().apply {
                    val half = numVertices / 2
                    val startIdx = (numVertices * 3 / 4) % numVertices
                    moveTo(vertices[startIdx].x, vertices[startIdx].y)
                    for (step in 1..half) {
                        val idx = (startIdx + step) % numVertices
                        lineTo(vertices[idx].x, vertices[idx].y)
                    }
                }
                drawPath(
                    path = topRimPath,
                    color = Color(0xFFD0D0DC).copy(alpha = 0.45f),
                    style = Stroke(width = 1.6f, cap = StrokeCap.Round)
                )
            }
        }
    }

    private fun DrawScope.drawPlayer(
        player: Player,
        status: GameStatus,
        shieldTimer: Float = 0f,
        speedBoostTimer: Float = 0f,
        invulnerabilityTimer: Float = 0f
    ) {
        scale(player.sizeScale, pivot = Offset(player.x, player.y + 20f)) {
            if (player.isPopped) {
                translate(player.electricJitterX, player.electricJitterY) {
                    drawPlayerManCharacter(player, status, holdingString = false)
                }
                return@scale
            }

            translate(player.electricJitterX, player.electricJitterY) {
                val balloonRadius = player.balloonRadius
                val stretchY = player.balloonStretchY.coerceIn(0.85f, 1.20f)
                val stretchX = (2f - stretchY).coerceIn(0.85f, 1.15f)
                val dynamicRadiusX = balloonRadius * stretchX
                val dynamicRadiusY = balloonRadius * stretchY
                val swayDeg = player.balloonSwayAngle * (180f / Math.PI.toFloat())

                val effectiveBalloonCenter = player.balloonCenter()

                // Invulnerability recovery blink (when recovering after losing a life or revived)
                val isInvulnerableFlashing = invulnerabilityTimer > 0f && ((invulnerabilityTimer * 14).toInt() % 2 == 0)
                val globalAlpha = if (isInvulnerableFlashing) 0.35f else 1.0f

                // 0. Electrified Shock Corona & High-Voltage Arcs (when hit by electric hazard)
                if (player.isElectrified) {
                    val isYellowShock = player.yellowElectrifiedTimer > 0f || player.isImmobilized
                    val electricPulse = sin(player.balloonBobPhase * 16f) * 7f
                    val isFlashBright = ((player.electrifiedTimer * 24).toInt() % 2 == 0)

                    val primaryElecColor = if (isYellowShock) {
                        if (isFlashBright) Color(0xFFFFEA00) else Color(0xFFFFF59D)
                    } else {
                        if (isFlashBright) Color(0xFF00E5FF) else Color(0xFFFFEA00)
                    }

                    val secondaryElecColor = if (isYellowShock) {
                        if (isFlashBright) Color(0xFFFFD600) else Color(0xFFFFEA00)
                    } else {
                        if (isFlashBright) Color(0xFFFFEA00) else Color(0xFF00E5FF)
                    }

                    // Electric radial corona discharge around balloon
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryElecColor.copy(alpha = 0.55f),
                                secondaryElecColor.copy(alpha = 0.30f),
                                Color.Transparent
                            ),
                            center = effectiveBalloonCenter,
                            radius = balloonRadius * 2.3f + electricPulse
                        ),
                        radius = balloonRadius * 2.3f + electricPulse,
                        center = effectiveBalloonCenter
                    )

                    // High-voltage jagged lightning arcs across the balloon
                    val arcCount = if (isYellowShock) 6 else 4
                    for (a in 0 until arcCount) {
                        val angle1 = (a * (360f / arcCount) + player.balloonBobPhase * 30f + a * 37f) * (Math.PI.toFloat() / 180f)
                        val angle2 = angle1 + 1.2f
                        val p1 = Offset(effectiveBalloonCenter.x + cos(angle1) * (dynamicRadiusX * 1.15f), effectiveBalloonCenter.y + sin(angle1) * (dynamicRadiusY * 1.15f))
                        val p2 = Offset(effectiveBalloonCenter.x + cos(angle2) * (dynamicRadiusX * 1.25f), effectiveBalloonCenter.y + sin(angle2) * (dynamicRadiusY * 1.25f))
                        val mid = Offset(
                            (p1.x + p2.x) * 0.5f + (if (a % 2 == 0) 10f else -10f),
                            (p1.y + p2.y) * 0.5f + (if (a % 2 == 0) -8f else 8f)
                        )

                        val boltPath = Path().apply {
                            moveTo(p1.x, p1.y)
                            lineTo(mid.x, mid.y)
                            lineTo(p2.x, p2.y)
                        }
                        drawPath(
                            path = boltPath,
                            color = if (isYellowShock) Color(0xFFFFEA00) else (if (a % 2 == 0) Color(0xFF00E5FF) else Color(0xFFFFF9C4)),
                            style = Stroke(width = if (isYellowShock) 3.0f else 2.4f, cap = StrokeCap.Round, join = StrokeJoin.Miter)
                        )
                    }
                }

            // 0. Invulnerability protective shimmering aura
        if (invulnerabilityTimer > 0f) {
            val auraPulse = sin(player.balloonBobPhase * 6f) * 4f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF5252).copy(alpha = 0.35f),
                        Color(0xFFFF1744).copy(alpha = 0.15f),
                        Color(0xFFFF1744).copy(alpha = 0f)
                    ),
                    center = effectiveBalloonCenter,
                    radius = balloonRadius * 1.6f + auraPulse
                ),
                radius = balloonRadius * 1.6f + auraPulse,
                center = effectiveBalloonCenter
            )
            drawOval(
                color = Color(0xFFFF5252).copy(alpha = 0.7f),
                topLeft = Offset(effectiveBalloonCenter.x - dynamicRadiusX * 1.15f, effectiveBalloonCenter.y - dynamicRadiusY * 1.3f),
                size = Size(dynamicRadiusX * 2.3f, dynamicRadiusY * 2.6f),
                style = Stroke(width = 1.8f)
            )
        }

        // 0. Turbo Speed Boost Atmospheric Aura
        if (speedBoostTimer > 0f) {
            val auraAlpha = (speedBoostTimer / 1.5f).coerceIn(0.2f, 0.75f)
            val speedPulse = sin(player.balloonBobPhase * 2.5f) * 4f

            // Golden sonic trailing slipstream cones
            val conePath = Path().apply {
                moveTo(effectiveBalloonCenter.x - dynamicRadiusX * 1.2f, effectiveBalloonCenter.y + dynamicRadiusY * 0.5f)
                lineTo(effectiveBalloonCenter.x + dynamicRadiusX * 1.2f, effectiveBalloonCenter.y + dynamicRadiusY * 0.5f)
                lineTo(effectiveBalloonCenter.x + dynamicRadiusX * 0.6f, effectiveBalloonCenter.y + dynamicRadiusY * 2.8f + speedPulse)
                lineTo(effectiveBalloonCenter.x - dynamicRadiusX * 0.6f, effectiveBalloonCenter.y + dynamicRadiusY * 2.8f + speedPulse)
                close()
            }
            drawPath(
                path = conePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFD54F).copy(alpha = auraAlpha * 0.5f),
                        Color(0xFFFFB300).copy(alpha = auraAlpha * 0.25f),
                        Color(0xFFFF8F00).copy(alpha = 0f)
                    ),
                    startY = effectiveBalloonCenter.y,
                    endY = effectiveBalloonCenter.y + dynamicRadiusY * 3.2f
                ),
                style = Fill
            )

            // Aerodynamic slipstream chevron arcs
            for (step in 1..3) {
                val stepOffset = step * 14f
                val chevY = effectiveBalloonCenter.y + dynamicRadiusY * 1.2f + stepOffset
                val chevAlpha = (auraAlpha * (1f - step * 0.25f)).coerceIn(0f, 1f)
                val chevPath = Path().apply {
                    moveTo(effectiveBalloonCenter.x - 22f - step * 4f, chevY)
                    lineTo(effectiveBalloonCenter.x, chevY + 10f)
                    lineTo(effectiveBalloonCenter.x + 22f + step * 4f, chevY)
                }
                drawPath(
                    path = chevPath,
                    color = Color(0xFFFFC107).copy(alpha = chevAlpha),
                    style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                )
            }
        }

        // 1. Balloon String Connection Points
        val knotY = effectiveBalloonCenter.y + dynamicRadiusY * 1.15f
        val knotBottom = Offset(effectiveBalloonCenter.x, knotY + 6f)
        val childHand = player.stringBottom()

        // 2. Dynamic curved tether string connecting character hand to balloon knot
        val stringMidX = (knotBottom.x + childHand.x) * 0.5f + sin(player.balloonSwayAngle) * 7f + sin(player.childAngle) * 5f
        val stringMidY = (knotBottom.y + childHand.y) * 0.5f

        val stringPath = Path().apply {
            moveTo(knotBottom.x, knotBottom.y)
            quadraticTo(stringMidX, stringMidY, childHand.x, childHand.y)
        }
        drawPath(
            path = stringPath,
            color = PureBlack,
            style = Stroke(width = 2.2f, cap = StrokeCap.Round)
        )

        // 3, 4, 5. THE RED BALLOON (Swinging gently on its string tether pivot)
        rotate(degrees = swayDeg, pivot = knotBottom) {
            // 3. Balloon Knot
            val knotTop = knotY - 1f
            val knotPath = Path().apply {
                moveTo(effectiveBalloonCenter.x - 4.5f, knotTop)
                lineTo(effectiveBalloonCenter.x + 4.5f, knotTop)
                lineTo(effectiveBalloonCenter.x + 6.5f, knotTop + 7f)
                lineTo(effectiveBalloonCenter.x - 6.5f, knotTop + 7f)
                close()
            }
            drawPath(path = knotPath, color = BalloonRed, style = Fill)
            drawPath(path = knotPath, color = PureBlack, style = Stroke(width = 2f, join = StrokeJoin.Round))

            // 4. THE RED BALLOON BODY
            val balloonBrush = Brush.radialGradient(
                colors = listOf(
                    BalloonRedBright,
                    BalloonRed,
                    BalloonRedDark
                ),
                center = Offset(effectiveBalloonCenter.x - dynamicRadiusX * 0.25f, effectiveBalloonCenter.y - dynamicRadiusY * 0.3f),
                radius = dynamicRadiusX * 1.35f
            )

            drawOval(
                brush = balloonBrush,
                topLeft = Offset(effectiveBalloonCenter.x - dynamicRadiusX, effectiveBalloonCenter.y - dynamicRadiusY * 1.15f),
                size = Size(dynamicRadiusX * 2f, dynamicRadiusY * 2.3f)
            )

            // Crisp bold outline
            drawOval(
                color = PureBlack,
                topLeft = Offset(effectiveBalloonCenter.x - dynamicRadiusX, effectiveBalloonCenter.y - dynamicRadiusY * 1.15f),
                size = Size(dynamicRadiusX * 2f, dynamicRadiusY * 2.3f),
                style = Stroke(width = 2.4f)
            )

            // 5. Specular Highlights
            val arcHighlightPath = Path().apply {
                moveTo(effectiveBalloonCenter.x - dynamicRadiusX * 0.65f, effectiveBalloonCenter.y - dynamicRadiusY * 0.15f)
                cubicTo(
                    effectiveBalloonCenter.x - dynamicRadiusX * 0.72f, effectiveBalloonCenter.y - dynamicRadiusY * 0.55f,
                    effectiveBalloonCenter.x - dynamicRadiusX * 0.45f, effectiveBalloonCenter.y - dynamicRadiusY * 0.85f,
                    effectiveBalloonCenter.x - dynamicRadiusX * 0.15f, effectiveBalloonCenter.y - dynamicRadiusY * 0.88f
                )
            }
            drawPath(
                path = arcHighlightPath,
                color = PureWhite.copy(alpha = 0.85f),
                style = Stroke(width = 3.2f, cap = StrokeCap.Round)
            )

            drawCircle(
                color = PureWhite.copy(alpha = 0.85f),
                radius = 2.4f,
                center = Offset(effectiveBalloonCenter.x - dynamicRadiusX * 0.68f, effectiveBalloonCenter.y - dynamicRadiusY * 0.02f)
            )
        }

        // 6. Character Suspended Below
        drawPlayerManCharacter(player, status, holdingString = true)

        // 7. Kinetic Shield Forcefield Bubble (Glowing Electric Azure Envelope)
        if (shieldTimer > 0f) {
            val isFlashing = shieldTimer < 2.0f && ((shieldTimer * 8).toInt() % 2 == 0)
            if (!isFlashing) {
                val shieldAlpha = (shieldTimer / 1.5f).coerceIn(0.4f, 0.9f)
                val shieldRadius = balloonRadius * 1.85f
                val shieldCenter = Offset(effectiveBalloonCenter.x, (effectiveBalloonCenter.y + player.childCenter().y) * 0.5f)
                val pulse = sin(player.balloonBobPhase * 3.5f) * 3f

                // Outer glow halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = shieldAlpha * 0.22f),
                            Color(0xFF00B0FF).copy(alpha = shieldAlpha * 0.12f),
                            Color(0xFF0091EA).copy(alpha = 0f)
                        ),
                        center = shieldCenter,
                        radius = shieldRadius + 18f + pulse
                    ),
                    radius = shieldRadius + 18f + pulse,
                    center = shieldCenter
                )

                // Forcefield sphere outline
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = shieldAlpha * 0.85f),
                    radius = shieldRadius + pulse,
                    center = shieldCenter,
                    style = Stroke(width = 2.2f)
                )

                // Concentric inner hexagonal grid accents
                for (hex in 0 until 6) {
                    val hexAngle = (hex * 60f + player.balloonBobPhase * 20f) * (Math.PI.toFloat() / 180f)
                    val nodeX = shieldCenter.x + cos(hexAngle) * (shieldRadius + pulse)
                    val nodeY = shieldCenter.y + sin(hexAngle) * (shieldRadius + pulse)

                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = shieldAlpha * 0.95f),
                        radius = 3.2f,
                        center = Offset(nodeX, nodeY)
                    )
                    drawCircle(
                        color = PureWhite.copy(alpha = shieldAlpha * 0.95f),
                        radius = 1.6f,
                        center = Offset(nodeX, nodeY)
                    )
                }

                // Rotating shield equator ring
                rotate(degrees = player.balloonBobPhase * 40f, pivot = shieldCenter) {
                    drawOval(
                        color = Color(0xFF00B0FF).copy(alpha = shieldAlpha * 0.55f),
                        topLeft = Offset(shieldCenter.x - shieldRadius - pulse, shieldCenter.y - (shieldRadius + pulse) * 0.35f),
                        size = Size((shieldRadius + pulse) * 2f, (shieldRadius + pulse) * 0.7f),
                        style = Stroke(width = 1.5f)
                    )
                }
            }
        }
    }
}
}

    private fun DrawScope.drawPowerUpCollectibles(powerUps: List<PowerUpItem>) {
        for (item in powerUps) {
            val cx = item.x
            val cy = item.y
            val baseR = item.radius
            val pulse = sin(item.pulsePhase) * 3f
            val r = baseR + pulse

            when (item.type) {
                PowerUpType.SHIELD -> {
                    // Kinetic Shield: Electric Cyan Orb with Rotating Hexagon Core & Crest
                    // 1. Radiant outer energy glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00E5FF).copy(alpha = 0.45f),
                                Color(0xFF00B0FF).copy(alpha = 0.20f),
                                Color(0xFF00E5FF).copy(alpha = 0f)
                            ),
                            center = Offset(cx, cy),
                            radius = r + 22f
                        ),
                        radius = r + 22f,
                        center = Offset(cx, cy)
                    )

                    // 2. Outer pulsing shockwave ring
                    val waveR = (r + (item.pulsePhase * 8f) % 24f)
                    val waveAlpha = (1f - ((item.pulsePhase * 8f) % 24f) / 24f).coerceIn(0f, 0.7f)
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = waveAlpha),
                        radius = waveR,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.6f)
                    )

                    // 3. Hexagonal Crystal Body
                    rotate(degrees = item.rotation * 45f, pivot = Offset(cx, cy)) {
                        val hexPath = Path().apply {
                            for (i in 0 until 6) {
                                val ang = (i * 60f) * (Math.PI.toFloat() / 180f)
                                val hx = cx + cos(ang) * r
                                val hy = cy + sin(ang) * r
                                if (i == 0) moveTo(hx, hy) else lineTo(hx, hy)
                            }
                            close()
                        }
                        // Fill
                        drawPath(
                            path = hexPath,
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFE0F7FA),
                                    Color(0xFF00E5FF),
                                    Color(0xFF0091EA)
                                ),
                                center = Offset(cx - r * 0.2f, cy - r * 0.2f),
                                radius = r * 1.2f
                            ),
                            style = Fill
                        )
                        // Bold ink border
                        drawPath(path = hexPath, color = PureBlack, style = Stroke(width = 2.4f, join = StrokeJoin.Round))
                    }

                    // 4. Central Shield Emblem Crest
                    val crestPath = Path().apply {
                        moveTo(cx - 8f, cy - 8f)
                        lineTo(cx + 8f, cy - 8f)
                        lineTo(cx + 8f, cy + 2f)
                        lineTo(cx, cy + 10f)
                        lineTo(cx - 8f, cy + 2f)
                        close()
                    }
                    drawPath(path = crestPath, color = PureWhite, style = Fill)
                    drawPath(path = crestPath, color = PureBlack, style = Stroke(width = 1.8f, join = StrokeJoin.Round))

                    // 5. Specular glint
                    drawCircle(
                        color = PureWhite,
                        radius = 2.5f,
                        center = Offset(cx - 5f, cy - 5f)
                    )
                }

                PowerUpType.SPEED_BOOST -> {
                    // Turbo Speed Surge: Radiant Gold Orb with Double Aerodynamic Chevrons
                    // 1. Radiant solar corona
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFE082).copy(alpha = 0.50f),
                                Color(0xFFFFB300).copy(alpha = 0.25f),
                                Color(0xFFFF8F00).copy(alpha = 0f)
                            ),
                            center = Offset(cx, cy),
                            radius = r + 22f
                        ),
                        radius = r + 22f,
                        center = Offset(cx, cy)
                    )

                    // 2. Pulsing speed ring
                    val ringR = (r + (item.pulsePhase * 9f) % 22f)
                    val ringAlpha = (1f - ((item.pulsePhase * 9f) % 22f) / 22f).coerceIn(0f, 0.75f)
                    drawCircle(
                        color = Color(0xFFFFC107).copy(alpha = ringAlpha),
                        radius = ringR,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.6f)
                    )

                    // 3. Main Circular Sphere Body
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFF9C4),
                                Color(0xFFFFCA28),
                                Color(0xFFFF8F00)
                            ),
                            center = Offset(cx - r * 0.25f, cy - r * 0.25f),
                            radius = r * 1.2f
                        ),
                        radius = r,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = PureBlack,
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.4f)
                    )

                    // 4. Double Upward Speed Chevrons (<<< / >>>)
                    val chevOffset = sin(item.pulsePhase * 2f) * 2f
                    for (c in listOf(-4f, 4f)) {
                        val chevY = cy + c + chevOffset
                        val chevPath = Path().apply {
                            moveTo(cx - 7f, chevY + 4f)
                            lineTo(cx, chevY - 4f)
                            lineTo(cx + 7f, chevY + 4f)
                        }
                        drawPath(path = chevPath, color = PureWhite, style = Stroke(width = 2.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        drawPath(path = chevPath, color = PureBlack, style = Stroke(width = 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }

                    // 5. Specular highlight
                    drawCircle(
                        color = PureWhite,
                        radius = 2.5f,
                        center = Offset(cx - 6f, cy - 6f)
                    )
                }

                PowerUpType.HEART -> {
                    // Radiant Life-Restoring Heart Collectible (Spawns every 5000m)
                    // 1. Radiant Ruby/Rose halo
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF4081).copy(alpha = 0.55f),
                                Color(0xFFE91E63).copy(alpha = 0.25f),
                                Color(0xFFC2185B).copy(alpha = 0f)
                            ),
                            center = Offset(cx, cy),
                            radius = r + 24f
                        ),
                        radius = r + 24f,
                        center = Offset(cx, cy)
                    )

                    // 2. Pulsing heartbeat shockwave ring
                    val heartPulse = sin(item.pulsePhase * 3.5f) * 2.5f
                    val waveR = (r + (item.pulsePhase * 7f) % 20f)
                    val waveAlpha = (1f - ((item.pulsePhase * 7f) % 20f) / 20f).coerceIn(0f, 0.7f)
                    drawCircle(
                        color = Color(0xFFFF4081).copy(alpha = waveAlpha),
                        radius = waveR,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.6f)
                    )

                    // 3. Heart Gem Body
                    val hr = (r + heartPulse)
                    val heartPath = Path().apply {
                        val topY = cy - hr * 0.45f
                        val bottomY = cy + hr * 0.85f
                        val leftX = cx - hr * 0.95f
                        val rightX = cx + hr * 0.95f
                        val midTopY = cy - hr * 0.15f

                        moveTo(cx, bottomY)
                        // Left lobe
                        cubicTo(leftX, cy + hr * 0.3f, leftX, topY, cx - hr * 0.48f, topY)
                        cubicTo(cx - hr * 0.2f, topY, cx, midTopY, cx, midTopY)
                        // Right lobe
                        cubicTo(cx, midTopY, cx + hr * 0.2f, topY, cx + hr * 0.48f, topY)
                        cubicTo(rightX, topY, rightX, cy + hr * 0.3f, cx, bottomY)
                        close()
                    }

                    drawPath(
                        path = heartPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF80AB),
                                Color(0xFFFF1744),
                                Color(0xFF880E4F)
                            ),
                            center = Offset(cx - hr * 0.2f, cy - hr * 0.2f),
                            radius = hr * 1.3f
                        ),
                        style = Fill
                    )
                    drawPath(path = heartPath, color = PureBlack, style = Stroke(width = 2.4f, join = StrokeJoin.Round))

                    // 4. Specular glint on left heart lobe
                    drawCircle(
                        color = PureWhite.copy(alpha = 0.9f),
                        radius = 2.8f,
                        center = Offset(cx - hr * 0.4f, cy - hr * 0.22f)
                    )
                }
            }
        }
    }

    private fun DrawScope.drawPlayerManCharacter(player: Player, status: GameStatus, holdingString: Boolean) {
        val cx = player.x
        val cy = player.y + 40f
        val angleDeg = player.childAngle * (180f / Math.PI.toFloat())
        val skin = player.skin
        val isElectrified = player.isElectrified
        val isYellowShock = player.yellowElectrifiedTimer > 0f || player.isImmobilized

        val isElectricFlash = isElectrified && (((player.electrifiedTimer * 22).toInt() % 2) == 0)
        val elecNeonCyan = Color(0xFF00E5FF)
        val elecNeonYellow = Color(0xFFFFEA00)
        val elecFlashColor = if (isYellowShock) {
            if (((player.electrifiedTimer * 28).toInt() % 2) == 0) Color(0xFFFFEA00) else Color(0xFFFFF9C4)
        } else {
            if (((player.electrifiedTimer * 30).toInt() % 2) == 0) elecNeonCyan else elecNeonYellow
        }

        val coatColor = if (isElectricFlash) elecFlashColor else Color(skin.colorCoat)
        val trimColor = if (isElectricFlash) PureWhite else Color(skin.colorTrim)
        val scarfColor = if (isElectricFlash) (if (isYellowShock) Color(0xFFFFD600) else elecNeonYellow) else Color(skin.colorScarf)
        val hatColor = if (isElectricFlash) (if (isYellowShock) Color(0xFFFFEA00) else elecNeonCyan) else Color(skin.colorHat)
        val hairColor = if (isElectricFlash) (if (isYellowShock) Color(0xFFFFF176) else Color(0xFF80D8FF)) else Color(skin.colorHair)
        val pantsColor = if (isElectricFlash) (if (isYellowShock) Color(0xFFFFD54F) else Color(0xFF00B0FF)) else Color(skin.colorPants)

        rotate(degrees = angleDeg, pivot = Offset(cx, cy)) {
            val isAirborne = status == GameStatus.PLAYING || status == GameStatus.TAKEOFF
            val legSwing = sin(player.legSwingPhase) * 6f
            val windFlutter = sin(player.balloonBobPhase * 2.5f) * 4f

            val headCenterY = cy - 14f

            // Ponytail (rendered behind head if enabled)
            if (skin.hasPonytail) {
                val ponyPath = Path().apply {
                    moveTo(cx - 7f, headCenterY - 2f)
                    quadraticTo(
                        cx - 18f,
                        headCenterY + 4f + (if (isAirborne) windFlutter else 0f),
                        cx - 24f,
                        headCenterY + 12f + (if (isAirborne) windFlutter * 1.4f else 0f)
                    )
                    lineTo(cx - 22f, headCenterY + 14f)
                    quadraticTo(
                        cx - 14f,
                        headCenterY + 7f,
                        cx - 6f,
                        headCenterY + 4f
                    )
                    close()
                }
                drawPath(path = ponyPath, color = hairColor, style = Fill)
                drawPath(path = ponyPath, color = PureBlack, style = Stroke(width = 1.2f))
            }

            // Braids (rendered on side)
            if (skin.hasBraids) {
                val braidPath = Path().apply {
                    moveTo(cx + 6f, headCenterY + 2f)
                    quadraticTo(cx + 12f, headCenterY + 10f, cx + 10f, headCenterY + 20f)
                }
                drawPath(path = braidPath, color = hairColor, style = Stroke(width = 2.4f, cap = StrokeCap.Round))
            }

            // 1. Head (Base Face / Silhouette)
            drawCircle(
                color = if (skin.id == "cyber_overcharge") Color(0xFF1E2838) else Color(0xFF18181C),
                radius = 9f,
                center = Offset(cx, headCenterY)
            )

            // Hat & Headgear styling
            when (skin.hatStyle) {
                HatStyle.AERONAUT_CAP -> {
                    // Cap Crown
                    val capCrown = Path().apply {
                        moveTo(cx - 9.5f, headCenterY - 1f)
                        quadraticTo(cx, headCenterY - 11f, cx + 9.5f, headCenterY - 1f)
                        close()
                    }
                    drawPath(path = capCrown, color = hatColor, style = Fill)
                    drawPath(path = capCrown, color = PureBlack, style = Stroke(width = 1.2f))

                    // Cap Brim / Visor
                    val hatBrim = Path().apply {
                        moveTo(cx - 12f, headCenterY - 2f)
                        lineTo(cx + 8f, headCenterY - 3f)
                        lineTo(cx + 13f, headCenterY - 1f)
                        lineTo(cx - 8f, headCenterY)
                        close()
                    }
                    drawPath(path = hatBrim, color = hatColor, style = Fill)
                    drawPath(path = hatBrim, color = trimColor, style = Stroke(width = 1.0f))
                }
                HatStyle.BERET -> {
                    // Chic angled Beret
                    val beretPath = Path().apply {
                        moveTo(cx - 11f, headCenterY - 2f)
                        cubicTo(cx - 12f, headCenterY - 11f, cx + 5f, headCenterY - 13f, cx + 13f, headCenterY - 6f)
                        quadraticTo(cx + 8f, headCenterY - 1f, cx - 11f, headCenterY - 2f)
                        close()
                    }
                    drawPath(path = beretPath, color = hatColor, style = Fill)
                    drawPath(path = beretPath, color = PureBlack, style = Stroke(width = 1.2f))
                    // Beret tip button
                    drawCircle(color = trimColor, radius = 1.8f, center = Offset(cx + 1f, headCenterY - 10f))
                }
                HatStyle.HOOD -> {
                    // Alpine Thermal Cowl / Hood
                    val hoodPath = Path().apply {
                        moveTo(cx - 11f, headCenterY + 4f)
                        cubicTo(cx - 13f, headCenterY - 12f, cx + 13f, headCenterY - 12f, cx + 11f, headCenterY + 4f)
                        close()
                    }
                    drawPath(path = hoodPath, color = hatColor, style = Fill)
                    drawPath(path = hoodPath, color = trimColor, style = Stroke(width = 1.5f))
                }
                HatStyle.VISOR -> {
                    // Cyber Optic Visor
                    val cyberBand = Path().apply {
                        moveTo(cx - 10f, headCenterY - 6f)
                        quadraticTo(cx, headCenterY - 10f, cx + 10f, headCenterY - 6f)
                        lineTo(cx + 10f, headCenterY - 1f)
                        lineTo(cx - 10f, headCenterY - 1f)
                        close()
                    }
                    drawPath(path = cyberBand, color = hatColor, style = Fill)
                    // Neon Visor Bar
                    drawLine(
                        color = trimColor,
                        start = Offset(cx - 8f, headCenterY - 3f),
                        end = Offset(cx + 9f, headCenterY - 3f),
                        strokeWidth = 3.0f,
                        cap = StrokeCap.Round
                    )
                }
                HatStyle.AVIATOR_HELMET -> {
                    // Dome flight helmet with ear pads
                    val helmetDome = Path().apply {
                        moveTo(cx - 10.5f, headCenterY + 2f)
                        cubicTo(cx - 11f, headCenterY - 12f, cx + 11f, headCenterY - 12f, cx + 10.5f, headCenterY + 2f)
                        close()
                    }
                    drawPath(path = helmetDome, color = hatColor, style = Fill)
                    drawPath(path = helmetDome, color = PureBlack, style = Stroke(width = 1.4f))
                    // Ear muff
                    drawCircle(color = trimColor, radius = 3.5f, center = Offset(cx - 8.5f, headCenterY))
                }
                HatStyle.GOGGLE_CAP -> {
                    // Cap Crown
                    val capCrown = Path().apply {
                        moveTo(cx - 9.5f, headCenterY - 1f)
                        quadraticTo(cx, headCenterY - 10f, cx + 9.5f, headCenterY - 1f)
                        close()
                    }
                    drawPath(path = capCrown, color = hatColor, style = Fill)
                    drawPath(path = capCrown, color = PureBlack, style = Stroke(width = 1.2f))
                }
                else -> {
                    // Other styles or NONE
                }
            }

            // Pilot Goggles overlay (if enabled)
            if (skin.hasGoggles) {
                // Brass goggle frame
                val goggleY = headCenterY - 4f
                drawCircle(color = Color(0xFFD4AF37), radius = 3.8f, center = Offset(cx - 4.5f, goggleY))
                drawCircle(color = Color(0xFFD4AF37), radius = 3.8f, center = Offset(cx + 4.5f, goggleY))
                // Lens glint
                drawCircle(color = Color(0xFFB3E5FC), radius = 2.4f, center = Offset(cx - 4.5f, goggleY))
                drawCircle(color = Color(0xFFB3E5FC), radius = 2.4f, center = Offset(cx + 4.5f, goggleY))
                // Strap
                drawLine(color = Color(0xFF3E2723), start = Offset(cx - 9f, goggleY), end = Offset(cx + 9f, goggleY), strokeWidth = 1.5f)
            }

            // 2. Collar & Scarf trailing in the wind
            val scarfOffset = if (isAirborne) legSwing * 0.5f + windFlutter else 0f
            val scarfPath = Path().apply {
                moveTo(cx - 3f, cy - 5f)
                quadraticTo(cx + 12f, cy - 3f + scarfOffset, cx + 19f, cy + 3f + scarfOffset * 0.7f)
                lineTo(cx + 17f, cy + 6f + scarfOffset * 0.7f)
                quadraticTo(cx + 8f, cy, cx - 1f, cy - 2f)
                close()
            }
            drawPath(path = scarfPath, color = scarfColor, style = Fill)
            drawPath(path = scarfPath, color = PureBlack, style = Stroke(width = 1.2f))

            // 3. Tailored Coat / Torso
            val coatPath = Path().apply {
                moveTo(cx - 9f, cy - 4f)
                lineTo(cx + 9f, cy - 4f)
                lineTo(cx + 8f, cy + 18f)
                lineTo(cx - 8f, cy + 18f)
                close()
            }
            drawPath(path = coatPath, color = coatColor, style = Fill)
            drawPath(path = coatPath, color = PureBlack, style = Stroke(width = 1.8f))

            // Coat seam / buttons / zipper
            drawLine(
                color = trimColor,
                start = Offset(cx, cy - 2f),
                end = Offset(cx, cy + 16f),
                strokeWidth = 1.5f
            )

            // 4. Arms
            if (holdingString) {
                // Left arm raised high overhead grasping the balloon string
                val raisedArm = Path().apply {
                    moveTo(cx - 6f, cy - 1f)
                    lineTo(cx - 10f, cy - 12f)
                    lineTo(cx - 7f, cy - 20f)
                }
                drawPath(path = raisedArm, color = coatColor, style = Stroke(width = 4.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawPath(path = raisedArm, color = PureBlack, style = Stroke(width = 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))

                // Hand grasping ring
                drawCircle(
                    color = trimColor,
                    radius = 3.2f,
                    center = Offset(cx - 7f, cy - 20f)
                )

                // Right arm trailing downward for balance
                val restArm = Path().apply {
                    moveTo(cx + 7f, cy - 1f)
                    lineTo(cx + 14f, cy + 8f)
                    lineTo(cx + 12f, cy + 18f)
                }
                drawPath(path = restArm, color = coatColor, style = Stroke(width = 4.0f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawPath(path = restArm, color = PureBlack, style = Stroke(width = 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            } else {
                // Tumbling / free fall arms
                val armL = Path().apply {
                    moveTo(cx - 7f, cy - 1f)
                    lineTo(cx - 18f, cy - 10f)
                }
                val armR = Path().apply {
                    moveTo(cx + 7f, cy - 1f)
                    lineTo(cx + 18f, cy - 10f)
                }
                drawPath(path = armL, color = coatColor, style = Stroke(width = 4.2f, cap = StrokeCap.Round))
                drawPath(path = armR, color = coatColor, style = Stroke(width = 4.2f, cap = StrokeCap.Round))
            }

            // 5. Trousers & Boots
            val leftLegX = cx - 4.5f
            val rightLegX = cx + 4.5f
            val leftLegYEnd = if (isAirborne) cy + 34f + legSwing else cy + 32f
            val rightLegYEnd = if (isAirborne) cy + 34f - legSwing else cy + 32f

            // Left leg
            drawLine(
                color = pantsColor,
                start = Offset(leftLegX, cy + 17f),
                end = Offset(leftLegX - (if (isAirborne) legSwing * 0.4f else 0f), leftLegYEnd),
                strokeWidth = 4.6f,
                cap = StrokeCap.Round
            )
            // Left shoe
            drawLine(
                color = PureBlack,
                start = Offset(leftLegX - (if (isAirborne) legSwing * 0.4f else 0f), leftLegYEnd),
                end = Offset(leftLegX - (if (isAirborne) legSwing * 0.4f else 0f) + 4.5f, leftLegYEnd + 1f),
                strokeWidth = 4.2f,
                cap = StrokeCap.Round
            )

            // Right leg
            drawLine(
                color = pantsColor,
                start = Offset(rightLegX, cy + 17f),
                end = Offset(rightLegX + (if (isAirborne) legSwing * 0.4f else 0f), rightLegYEnd),
                strokeWidth = 4.6f,
                cap = StrokeCap.Round
            )
            // Right shoe
            drawLine(
                color = PureBlack,
                start = Offset(rightLegX + (if (isAirborne) legSwing * 0.4f else 0f), rightLegYEnd),
                end = Offset(rightLegX + (if (isAirborne) legSwing * 0.4f else 0f) + 4.5f, rightLegYEnd + 1f),
                strokeWidth = 4.2f,
                cap = StrokeCap.Round
            )

            // 6. Electrified Crackling Lightning Arcs & Overcharge Aura
            if (isElectrified) {
                // Shimmering electric corona around child
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            (if (isYellowShock) Color(0xFFFFEA00) else elecNeonCyan).copy(alpha = 0.50f),
                            (if (isYellowShock) Color(0xFFFFD600) else elecNeonYellow).copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy + 8f),
                        radius = 42f
                    ),
                    radius = 42f,
                    center = Offset(cx, cy + 8f)
                )

                // High-voltage lightning bolt zigzag paths across the character
                val boltPhase = (player.electrifiedTimer * 35).toInt()
                val boltPath1 = Path().apply {
                    val jx1 = if (boltPhase % 2 == 0) 6f else -6f
                    val jx2 = if (boltPhase % 3 == 0) -7f else 7f
                    moveTo(cx - 3f, headCenterY - 6f)
                    lineTo(cx + jx1, cy)
                    lineTo(cx + jx2, cy + 14f)
                    lineTo(leftLegX, leftLegYEnd)
                }
                drawPath(
                    path = boltPath1,
                    color = if (isYellowShock) Color(0xFFFFEA00) else elecNeonCyan,
                    style = Stroke(width = if (isYellowShock) 3.2f else 2.4f, cap = StrokeCap.Round, join = StrokeJoin.Miter)
                )

                val boltPath2 = Path().apply {
                    val jx1 = if (boltPhase % 2 == 0) -8f else 8f
                    val jx2 = if (boltPhase % 3 == 0) 9f else -9f
                    moveTo(cx + 4f, headCenterY)
                    lineTo(cx + jx1, cy + 6f)
                    lineTo(cx + jx2, cy + 18f)
                    lineTo(rightLegX, rightLegYEnd)
                }
                drawPath(
                    path = boltPath2,
                    color = if (isYellowShock) Color(0xFFFFF9C4) else elecNeonYellow,
                    style = Stroke(width = if (isYellowShock) 2.8f else 2.0f, cap = StrokeCap.Round, join = StrokeJoin.Miter)
                )

                // Additional horizontal arcing yellow lightning over body when paralyzed
                if (isYellowShock) {
                    val boltCross = Path().apply {
                        val jy = if (boltPhase % 2 == 0) 3f else -3f
                        moveTo(cx - 16f, cy + jy)
                        lineTo(cx - 4f, cy - jy * 1.5f)
                        lineTo(cx + 6f, cy + jy * 1.5f)
                        lineTo(cx + 16f, cy - jy)
                    }
                    drawPath(
                        path = boltCross,
                        color = Color(0xFFFFEA00),
                        style = Stroke(width = 2.6f, cap = StrokeCap.Round, join = StrokeJoin.Miter)
                    )
                }

                // Eye / visor electric high-intensity spark
                val eyeColor = if (isYellowShock) Color(0xFFFFEA00) else elecNeonCyan
                drawCircle(
                    color = eyeColor,
                    radius = 2.4f,
                    center = Offset(cx - 3f, headCenterY - 2f)
                )
                drawCircle(
                    color = PureWhite,
                    radius = 1.2f,
                    center = Offset(cx - 3f, headCenterY - 2f)
                )
                drawCircle(
                    color = eyeColor,
                    radius = 2.4f,
                    center = Offset(cx + 3f, headCenterY - 2f)
                )
                drawCircle(
                    color = PureWhite,
                    radius = 1.2f,
                    center = Offset(cx + 3f, headCenterY - 2f)
                )

                // Spark nodes at hands and feet
                val nodeColor = if (isYellowShock) Color(0xFFFFEA00) else elecNeonYellow
                drawCircle(color = nodeColor, radius = 3.5f, center = Offset(cx - 7f, cy - 20f))
                drawCircle(color = nodeColor, radius = 3.5f, center = Offset(cx + 12f, cy + 18f))
                drawCircle(color = nodeColor, radius = 4f, center = Offset(leftLegX, leftLegYEnd))
                drawCircle(color = nodeColor, radius = 4f, center = Offset(rightLegX, rightLegYEnd))
            }
        }
    }

    private fun DrawScope.drawParticles(particles: List<GameParticle>) {
        for (p in particles) {
            when (p) {
                is RedPopParticle -> {
                    val shardColor = if (p.isGlossShard) BalloonRedBright else BalloonRed
                    rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                        val path = Path().apply {
                            moveTo(p.x, p.y - p.size)
                            lineTo(p.x + p.size * 0.7f, p.y + p.size * 0.6f)
                            lineTo(p.x - p.size * 0.7f, p.y + p.size * 0.6f)
                            close()
                        }
                        drawPath(path = path, color = shardColor.copy(alpha = p.alpha), style = Fill)
                        drawPath(path = path, color = PureBlack.copy(alpha = p.alpha * 0.7f), style = Stroke(width = 1f))
                    }
                }
                is FeatherParticle -> {
                    rotate(degrees = p.rotation * (180f / Math.PI.toFloat()), pivot = Offset(p.x, p.y)) {
                        val shaft = Path().apply {
                            moveTo(p.x, p.y - p.size * 0.8f)
                            quadraticTo(p.x + 2f, p.y, p.x, p.y + p.size * 0.8f)
                        }
                        drawPath(path = shaft, color = PureBlack.copy(alpha = p.alpha), style = Stroke(width = 1.2f))

                        val vane = Path().apply {
                            moveTo(p.x, p.y - p.size * 0.7f)
                            cubicTo(p.x + p.size * 0.4f, p.y - p.size * 0.3f, p.x + p.size * 0.3f, p.y + p.size * 0.4f, p.x, p.y + p.size * 0.6f)
                            cubicTo(p.x - p.size * 0.3f, p.y + p.size * 0.4f, p.x - p.size * 0.4f, p.y - p.size * 0.3f, p.x, p.y - p.size * 0.7f)
                            close()
                        }
                        drawPath(path = vane, color = Color(0xFFC0C0C8).copy(alpha = p.alpha * 0.6f), style = Fill)
                        drawPath(path = vane, color = PureBlack.copy(alpha = p.alpha * 0.7f), style = Stroke(width = 1f))
                    }
                }
                is DandelionFluffParticle -> {
                    drawCircle(
                        color = Color(0xFFA0A0AA).copy(alpha = p.alpha),
                        radius = p.radius,
                        center = Offset(p.x, p.y)
                    )
                    drawCircle(
                        color = PureWhite.copy(alpha = p.alpha * 0.8f),
                        radius = p.radius * 0.5f,
                        center = Offset(p.x, p.y)
                    )
                }
                is SparkleParticle -> {
                    rotate(degrees = (p.life * 360f), pivot = Offset(p.x, p.y)) {
                        drawLine(
                            color = PureBlack.copy(alpha = p.alpha),
                            start = Offset(p.x - p.size, p.y),
                            end = Offset(p.x + p.size, p.y),
                            strokeWidth = 1.8f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = PureBlack.copy(alpha = p.alpha),
                            start = Offset(p.x, p.y - p.size),
                            end = Offset(p.x, p.y + p.size),
                            strokeWidth = 1.8f,
                            cap = StrokeCap.Round
                        )
                    }
                }
                is DustParticle -> {
                    drawCircle(
                        color = Color(0xFF6B6B78).copy(alpha = p.alpha),
                        radius = p.radius,
                        center = Offset(p.x, p.y)
                    )
                }
                is RockDustParticle -> {
                    rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                        val rockDust = Path().apply {
                            moveTo(p.x, p.y - p.size)
                            lineTo(p.x + p.size * 0.8f, p.y + p.size * 0.5f)
                            lineTo(p.x - p.size * 0.7f, p.y + p.size * 0.7f)
                            close()
                        }
                        drawPath(path = rockDust, color = Color(0xFF4A4A56).copy(alpha = p.alpha), style = Fill)
                        drawPath(path = rockDust, color = PureBlack.copy(alpha = p.alpha * 0.8f), style = Stroke(width = 1f))
                    }
                }
                is ShieldDeflectParticle -> {
                    // Brilliant Cyan forcefield deflection burst
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PureWhite.copy(alpha = p.alpha),
                                Color(0xFF00E5FF).copy(alpha = p.alpha * 0.8f),
                                Color(0xFF00B0FF).copy(alpha = 0f)
                            ),
                            center = Offset(p.x, p.y),
                            radius = p.radius * 2.2f
                        ),
                        radius = p.radius * 2.2f,
                        center = Offset(p.x, p.y)
                    )
                    drawCircle(
                        color = PureWhite.copy(alpha = p.alpha),
                        radius = p.radius * 0.6f,
                        center = Offset(p.x, p.y)
                    )
                }
                is SlipstreamSparkParticle -> {
                    // Golden supersonic diamond spark
                    val spkR = p.size
                    val sparkPath = Path().apply {
                        moveTo(p.x, p.y - spkR * 1.5f)
                        lineTo(p.x + spkR * 0.6f, p.y)
                        lineTo(p.x, p.y + spkR * 1.5f)
                        lineTo(p.x - spkR * 0.6f, p.y)
                        close()
                    }
                    drawPath(path = sparkPath, color = Color(0xFFFFD54F).copy(alpha = p.alpha), style = Fill)
                    drawPath(path = sparkPath, color = PureBlack.copy(alpha = p.alpha * 0.7f), style = Stroke(width = 0.8f))
                }
                is HeartFloatingParticle -> {
                    // Floating glowing ruby hearts
                    rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                        val hr = p.size
                        val hPath = Path().apply {
                            val topY = p.y - hr * 0.5f
                            val bottomY = p.y + hr * 0.8f
                            val leftX = p.x - hr * 0.9f
                            val rightX = p.x + hr * 0.9f
                            val midTopY = p.y - hr * 0.2f

                            moveTo(p.x, bottomY)
                            cubicTo(leftX, p.y + hr * 0.25f, leftX, topY, p.x - hr * 0.45f, topY)
                            cubicTo(p.x - hr * 0.2f, topY, p.x, midTopY, p.x, midTopY)
                            cubicTo(p.x, midTopY, p.x + hr * 0.2f, topY, p.x + hr * 0.45f, topY)
                            cubicTo(rightX, topY, rightX, p.y + hr * 0.25f, p.x, bottomY)
                            close()
                        }
                        drawPath(path = hPath, color = Color(0xFFFF1744).copy(alpha = p.alpha), style = Fill)
                        drawPath(path = hPath, color = PureBlack.copy(alpha = p.alpha * 0.8f), style = Stroke(width = 1.2f))
                    }
                }
                is StormSparkParticle -> {
                    // Crackling diamond lightning spark
                    rotate(degrees = (p.life * 540f), pivot = Offset(p.x, p.y)) {
                        val spkR = p.size
                        val diamondPath = Path().apply {
                            moveTo(p.x, p.y - spkR * 1.4f)
                            lineTo(p.x + spkR * 0.8f, p.y)
                            lineTo(p.x, p.y + spkR * 1.4f)
                            lineTo(p.x - spkR * 0.8f, p.y)
                            close()
                        }
                        drawPath(path = diamondPath, color = Color(p.color).copy(alpha = p.alpha), style = Fill)
                        drawPath(path = diamondPath, color = PureWhite.copy(alpha = p.alpha), style = Stroke(width = 0.8f))
                    }
                }
                is StormRainParticle -> {
                    // Angled storm rain streak
                    drawLine(
                        color = Color(0xFF64B5F6).copy(alpha = p.alpha),
                        start = Offset(p.x, p.y),
                        end = Offset(p.x + p.vx * 0.04f, p.y + p.length),
                        strokeWidth = 2.0f,
                        cap = StrokeCap.Round
                    )
                }
                else -> {}
            }
        }
    }
}
