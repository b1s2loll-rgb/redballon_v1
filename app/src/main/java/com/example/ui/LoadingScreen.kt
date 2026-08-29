package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BalloonRed
import com.example.ui.theme.BalloonRedDark
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.DeepGray
import com.example.ui.theme.MidGray
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * High-polish Loading Scene with animated Game Logo, balloon physics & progress
 */
@Composable
fun LoadingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var currentTipIndex by remember { mutableStateOf(0) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "loadingProgress"
    )

    val tips = listOf(
        "Checking troposphere winds...",
        "Inflating red balloon & checking lift...",
        "Polishing aeronaut goggles...",
        "Calibrating altitude altimeter...",
        "Preparing flight corridor...",
        "Ready for ascent!"
    )

    // Progress sequencing timer
    LaunchedEffect(Unit) {
        // Step 1: 0% -> 30%
        delay(150)
        progress = 0.35f
        currentTipIndex = 1

        // Step 2: 30% -> 70%
        delay(250)
        progress = 0.75f
        currentTipIndex = 3

        // Step 3: 70% -> 100%
        delay(250)
        progress = 1.0f
        currentTipIndex = 5

        // Brief hold at 100% then transition to Main Menu
        delay(200)
        onFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "loadingTransition")
    
    // Floating bobbing effect for balloon
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "balloonFloat"
    )

    // String sway rotation
    val stringSway by infiniteTransition.animateFloat(
        initialValue = -0.06f,
        targetValue = 0.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stringSway"
    )

    // Background particle drift
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PureWhite,
                        Color(0xFFF9F9FC),
                        Color(0xFFF0F0F6)
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Allow tapping to skip loading instantly
                onFinished()
            }
            .testTag("loading_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Atmospheric ascent background particles (clouds/dust rising/falling)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // Draw floating air particles moving downwards to simulate upward balloon ascent
            val particleCount = 18
            for (i in 0 until particleCount) {
                val seed = (i * 137.5f)
                val x = (seed % w)
                val speed = 0.7f + ((i % 5) * 0.3f)
                val y = ((seed * 3.1f + particlePhase * h * speed) % (h + 80f)) - 40f
                val radius = 2f + (i % 4) * 1.2f
                val alpha = (0.25f + 0.35f * sin((particlePhase * 6.28f + i).toDouble()).toFloat())
                    .coerceIn(0.1f, 0.6f)

                drawCircle(
                    color = MidGray.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(x, y)
                )
            }
        }

        // Center Content: Game Logo, Animated Balloon, Title & Progress Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Game Logo Canvas
            GameLogoEmblem(
                floatOffset = floatOffset,
                stringSway = stringSway,
                modifier = Modifier
                    .size(190.dp, 210.dp)
                    .testTag("game_logo_emblem")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Game Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "RED",
                    color = BalloonRed,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BALLOON",
                    color = PureBlack,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
            }

            // Subtitle Badge
            Surface(
                color = PureBlack,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Text(
                    text = "ASCENT TO THE STRATOSPHERE",
                    color = PureWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.6.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.5.dp)
                )
            }

            Spacer(modifier = Modifier.height(42.dp))

            // Loading Progress Bar & Status
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Bar Card
                Surface(
                    color = PureWhite,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PureBlack),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LOADING FLIGHT ENGINE",
                                color = DeepGray,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                color = PureBlack,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom Styled Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFFE5E5EB))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                BalloonRed,
                                                BalloonRedDark
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Loading dynamic tip / status
                Text(
                    text = tips[currentTipIndex],
                    color = MidGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Tap anywhere to skip",
                    color = Color(0xFFA0A0AA),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

/**
 * Animated Vector Canvas Logo for Red Balloon
 */
@Composable
fun GameLogoEmblem(
    floatOffset: Float,
    stringSway: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = (size.height / 2f) - 15f + floatOffset

        val balloonRadiusX = 54f
        val balloonRadiusY = 64f

        // 1. Balloon String (dangling down with curve)
        val stringPath = Path().apply {
            val startY = centerY + balloonRadiusY - 4f
            moveTo(centerX, startY)
            val ctrlX = centerX + (stringSway * 180f)
            val ctrlY = startY + 45f
            val endX = centerX + (stringSway * 90f)
            val endY = startY + 85f
            quadraticTo(ctrlX, ctrlY, endX, endY)
        }
        drawPath(
            path = stringPath,
            color = PureBlack,
            style = Stroke(width = 2.4f, cap = StrokeCap.Round)
        )

        // 2. Character Silhouette holding the string at bottom
        val charY = centerY + balloonRadiusY + 82f
        val charX = centerX + (stringSway * 90f)

        // Pilot Body Silhouette
        drawCircle(
            color = PureBlack,
            radius = 8.5f,
            center = Offset(charX, charY)
        )
        // Pilot Goggles highlight on head
        drawCircle(
            color = Color(0xFF80DEEA),
            radius = 2.2f,
            center = Offset(charX + 3.5f, charY - 1f)
        )
        // Pilot coat body
        val coatPath = Path().apply {
            moveTo(charX - 5f, charY + 8f)
            lineTo(charX + 5f, charY + 8f)
            lineTo(charX + 7f, charY + 22f)
            lineTo(charX - 7f, charY + 22f)
            close()
        }
        drawPath(coatPath, color = PureBlack)
        // Pilot flowing red scarf
        val scarfPath = Path().apply {
            moveTo(charX - 2f, charY + 9f)
            quadraticTo(charX - 14f + stringSway * 40f, charY + 14f, charX - 20f + stringSway * 60f, charY + 18f)
            lineTo(charX - 18f + stringSway * 60f, charY + 21f)
            quadraticTo(charX - 12f + stringSway * 40f, charY + 15f, charX + 2f, charY + 10f)
            close()
        }
        drawPath(scarfPath, color = BalloonRed)

        // 3. Balloon Knot & Collar
        val knotY = centerY + balloonRadiusY - 5f
        val knotPath = Path().apply {
            moveTo(centerX - 8f, knotY + 6f)
            lineTo(centerX + 8f, knotY + 6f)
            lineTo(centerX + 4f, knotY)
            lineTo(centerX - 4f, knotY)
            close()
        }
        drawPath(knotPath, color = BalloonRedDark)
        drawPath(knotPath, color = PureBlack, style = Stroke(width = 2f))

        // 4. Main Red Balloon Shape
        val balloonPath = Path().apply {
            moveTo(centerX, centerY - balloonRadiusY)
            // Top right curve
            cubicTo(
                centerX + balloonRadiusX * 1.15f, centerY - balloonRadiusY,
                centerX + balloonRadiusX * 1.15f, centerY + balloonRadiusY * 0.4f,
                centerX + 6f, centerY + balloonRadiusY - 2f
            )
            // Bottom knot connection
            lineTo(centerX - 6f, centerY + balloonRadiusY - 2f)
            // Top left curve
            cubicTo(
                centerX - balloonRadiusX * 1.15f, centerY + balloonRadiusY * 0.4f,
                centerX - balloonRadiusX * 1.15f, centerY - balloonRadiusY,
                centerX, centerY - balloonRadiusY
            )
            close()
        }

        // Draw balloon drop shadow
        drawCircle(
            color = Color(0x22000000),
            radius = balloonRadiusX + 4f,
            center = Offset(centerX + 4f, centerY + 4f)
        )

        // Draw Balloon Gradient Body
        drawPath(
            path = balloonPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF5252),
                    BalloonRed,
                    BalloonRedDark,
                    Color(0xFF8B0000)
                ),
                center = Offset(centerX - 18f, centerY - 20f),
                radius = balloonRadiusX * 1.6f
            )
        )

        // Bold black contour outline
        drawPath(
            path = balloonPath,
            color = PureBlack,
            style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 5. Specular 3D Gloss / Light Reflection (curved crescent)
        val glossPath = Path().apply {
            moveTo(centerX - 24f, centerY - 42f)
            cubicTo(
                centerX - 36f, centerY - 28f,
                centerX - 38f, centerY - 6f,
                centerX - 28f, centerY + 14f
            )
            cubicTo(
                centerX - 33f, centerY - 4f,
                centerX - 31f, centerY - 24f,
                centerX - 20f, centerY - 40f
            )
            close()
        }
        drawPath(
            path = glossPath,
            color = PureWhite.copy(alpha = 0.72f)
        )

        // Small gloss dot
        drawCircle(
            color = PureWhite.copy(alpha = 0.85f),
            radius = 3.5f,
            center = Offset(centerX - 14f, centerY - 42f)
        )
    }
}
