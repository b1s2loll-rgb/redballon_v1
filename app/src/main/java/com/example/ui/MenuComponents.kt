package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.North
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AvailableCharacterSkins
import com.example.model.CharacterSkin
import com.example.model.DailyMission
import com.example.model.GenderStyle
import com.example.model.HatStyle
import com.example.model.MissionType
import com.example.ui.theme.BalloonRed
import com.example.ui.theme.BalloonRedDark
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.DeepGray
import com.example.ui.theme.LightSilver
import com.example.ui.theme.MidGray
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGray
import java.text.NumberFormat
import java.util.Locale

/**
 * Live canvas preview avatar for a CharacterSkin
 */
@Composable
fun SkinAvatarPreview(
    skin: CharacterSkin,
    modifier: Modifier = Modifier.size(54.dp, 66.dp)
) {
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0F0F5))
            .border(1.5.dp, Color(0xFFDCDCE4), RoundedCornerShape(12.dp))
    ) {
        val centerX = size.width * 0.5f
        val centerY = size.height * 0.52f

        // Draw mini balloon string and small balloon hint
        val balloonX = centerX + 10f
        val balloonY = size.height * 0.14f
        drawCircle(
            color = BalloonRed,
            radius = 11f,
            center = Offset(balloonX, balloonY)
        )
        drawLine(
            color = PureBlack,
            start = Offset(balloonX, balloonY + 11f),
            end = Offset(centerX + 6f, centerY - 2f),
            strokeWidth = 1.5f
        )

        // Character Legs & Boots
        drawLine(
            color = skin.pantsColor,
            start = Offset(centerX - 4f, centerY + 12f),
            end = Offset(centerX - 5f, centerY + 22f),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = skin.pantsColor,
            start = Offset(centerX + 4f, centerY + 12f),
            end = Offset(centerX + 5f, centerY + 22f),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        // Boots
        drawCircle(color = skin.bootColor, radius = 2.5f, center = Offset(centerX - 5f, centerY + 23f))
        drawCircle(color = skin.bootColor, radius = 2.5f, center = Offset(centerX + 5f, centerY + 23f))

        // Coat Body
        val coatPath = Path().apply {
            moveTo(centerX - 7f, centerY - 2f)
            lineTo(centerX + 7f, centerY - 2f)
            lineTo(centerX + 9f, centerY + 14f)
            lineTo(centerX - 9f, centerY + 14f)
            close()
        }
        drawPath(coatPath, color = skin.coatColor)
        drawPath(coatPath, color = PureBlack, style = Stroke(width = 1.5f, join = StrokeJoin.Round))

        // Scarf
        val scarfPath = Path().apply {
            moveTo(centerX - 6f, centerY - 4f)
            lineTo(centerX + 6f, centerY - 4f)
            lineTo(centerX + 5f, centerY + 2f)
            lineTo(centerX - 5f, centerY + 2f)
            close()
        }
        drawPath(scarfPath, color = skin.scarfColor)
        drawPath(scarfPath, color = PureBlack, style = Stroke(width = 1.2f))

        // Scarf tail flutter
        drawLine(
            color = skin.scarfColor,
            start = Offset(centerX - 3f, centerY),
            end = Offset(centerX - 10f, centerY + 5f),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )

        // Arms holding string
        drawLine(
            color = skin.coatColor,
            start = Offset(centerX + 5f, centerY),
            end = Offset(centerX + 7f, centerY - 3f),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )

        // Head & Hair
        val headCenter = Offset(centerX, centerY - 11f)
        val headRadius = 7.5f

        // Hair back / ponytail if applicable
        if (skin.hasPonytail) {
            drawLine(
                color = skin.hairColor,
                start = Offset(centerX - 5f, centerY - 11f),
                end = Offset(centerX - 11f, centerY - 6f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        } else if (skin.hasBraid) {
            drawLine(
                color = skin.hairColor,
                start = Offset(centerX - 4f, centerY - 11f),
                end = Offset(centerX - 9f, centerY - 2f),
                strokeWidth = 2.8f,
                cap = StrokeCap.Round
            )
        }

        // Head face
        drawCircle(color = skin.skinToneColor, radius = headRadius, center = headCenter)
        drawCircle(color = PureBlack, radius = headRadius, center = headCenter, style = Stroke(width = 1.2f))

        // Hair front
        val hairPath = Path().apply {
            moveTo(headCenter.x - headRadius, headCenter.y - 1f)
            cubicTo(
                headCenter.x - headRadius * 0.5f, headCenter.y - headRadius * 1.3f,
                headCenter.x + headRadius * 0.5f, headCenter.y - headRadius * 1.3f,
                headCenter.x + headRadius, headCenter.y - 1f
            )
            lineTo(headCenter.x + headRadius * 0.8f, headCenter.y - 3f)
            lineTo(headCenter.x - headRadius * 0.8f, headCenter.y - 3f)
            close()
        }
        drawPath(hairPath, color = skin.hairColor)

        // Hat Accessories
        when (skin.hatStyle) {
            HatStyle.AERONAUT_CAP -> {
                drawCircle(color = skin.hatColor, radius = headRadius * 0.95f, center = Offset(headCenter.x, headCenter.y - 2f))
                drawLine(
                    color = skin.hatAccentColor,
                    start = Offset(headCenter.x - 5f, headCenter.y - 3f),
                    end = Offset(headCenter.x + 5f, headCenter.y - 3f),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }
            HatStyle.BERET -> {
                val beretPath = Path().apply {
                    moveTo(headCenter.x - headRadius * 1.2f, headCenter.y - 2f)
                    cubicTo(
                        headCenter.x - headRadius * 0.5f, headCenter.y - headRadius * 1.7f,
                        headCenter.x + headRadius * 1.3f, headCenter.y - headRadius * 1.4f,
                        headCenter.x + headRadius * 0.9f, headCenter.y - 2f
                    )
                    close()
                }
                drawPath(beretPath, color = skin.hatColor)
            }
            HatStyle.HOOD -> {
                drawCircle(color = skin.hatColor, radius = headRadius * 1.2f, center = headCenter, style = Stroke(width = 2.5f))
            }
            HatStyle.VISOR -> {
                drawLine(
                    color = skin.hatAccentColor,
                    start = Offset(headCenter.x - headRadius * 0.8f, headCenter.y - 1f),
                    end = Offset(headCenter.x + headRadius * 1.1f, headCenter.y - 1f),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }
            HatStyle.AVIATOR_HELMET -> {
                drawCircle(color = skin.hatColor, radius = headRadius * 1.05f, center = Offset(headCenter.x, headCenter.y - 1f))
            }
            HatStyle.GOGGLE_CAP -> {
                drawCircle(color = skin.hatColor, radius = headRadius * 0.95f, center = Offset(headCenter.x, headCenter.y - 2f))
                drawCircle(color = Color(0xFFD4AF37), radius = 2f, center = Offset(headCenter.x - 2.5f, headCenter.y - 2f))
                drawCircle(color = Color(0xFFD4AF37), radius = 2f, center = Offset(headCenter.x + 2.5f, headCenter.y - 2f))
            }
            HatStyle.AVIATOR_GOGGLES -> {
                drawCircle(color = skin.hatColor, radius = headRadius * 0.95f, center = Offset(headCenter.x, headCenter.y - 2f))
                drawCircle(color = Color(0xFF80DEEA), radius = 2f, center = Offset(headCenter.x - 2.5f, headCenter.y - 2f))
                drawCircle(color = Color(0xFF80DEEA), radius = 2f, center = Offset(headCenter.x + 2.5f, headCenter.y - 2f))
            }
            HatStyle.FLORAL_CROWN -> {
                drawCircle(color = Color(0xFFFF4081), radius = 1.8f, center = Offset(headCenter.x - 4f, headCenter.y - 5f))
                drawCircle(color = Color(0xFFFFD54F), radius = 1.8f, center = Offset(headCenter.x, headCenter.y - 6f))
                drawCircle(color = Color(0xFF64B5F6), radius = 1.8f, center = Offset(headCenter.x + 4f, headCenter.y - 5f))
            }
            HatStyle.NONE -> {
                // Natural hair
            }
            else -> {
                // Default
            }
        }
    }
}

/**
 * Main Menu Overlay Component (Ready state)
 */
@Composable
fun MainMenuOverlay(
    totalCoins: Int,
    maxAltitudeEver: Long,
    currentSkin: CharacterSkin,
    dailyMissionsCompleted: Int,
    dailyMissionsTotal: Int,
    hasUnclaimedDailyMissions: Boolean,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onStartFlight: () -> Unit,
    onOpenDailyMissions: () -> Unit,
    onOpenHowToPlay: () -> Unit,
    onOpenWardrobe: () -> Unit,
    onOpenSettings: () -> Unit,
    onWatchAdForCoins: () -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Branding Hero Card
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite.copy(alpha = 0.96f)),
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, PureBlack),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(22.dp))
                    .testTag("main_menu_card")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Balloon Emblem & Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            color = BalloonRed,
                            shape = CircleShape,
                            modifier = Modifier.size(16.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RED BALLOON",
                            color = PureBlack,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }

                    Text(
                        text = "ASCENT TO THE STRATOSPHERE",
                        color = MidGray,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                    )

                    // Equipped Character Bar
                    Surface(
                        onClick = onOpenWardrobe,
                        color = Color(0xFFF6F6FA),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCDCE4)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("equipped_character_bar")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SkinAvatarPreview(skin = currentSkin, modifier = Modifier.size(42.dp, 50.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentSkin.name,
                                        color = PureBlack,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = if (currentSkin.gender == GenderStyle.FEMALE) Color(0xFFFCE4EC) else Color(0xFFE3F2FD),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = currentSkin.gender.displayName.uppercase(),
                                            color = if (currentSkin.gender == GenderStyle.FEMALE) Color(0xFFC2185B) else Color(0xFF1976D2),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Tap to change character outfit",
                                    color = MidGray,
                                    fontSize = 10.5.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Wardrobe",
                                tint = PureBlack,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Daily Missions Entry Banner Card
                    Surface(
                        onClick = onOpenDailyMissions,
                        color = if (hasUnclaimedDailyMissions) Color(0xFFFFFDE7) else Color(0xFFF7F7FB),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (hasUnclaimedDailyMissions) Color(0xFFF9A825) else Color(0xFFDCDCE8)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("daily_missions_banner_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (hasUnclaimedDailyMissions) Color(0xFFFFD54F) else Color(0xFFECECF4),
                                shape = CircleShape,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🎯", fontSize = 17.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "DAILY MISSIONS",
                                        color = PureBlack,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = if (hasUnclaimedDailyMissions) Color(0xFFFFD54F) else Color(0xFFE2E2EC),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (hasUnclaimedDailyMissions) "CLAIM 🪙" else "$dailyMissionsCompleted/$dailyMissionsTotal DONE",
                                            color = if (hasUnclaimedDailyMissions) Color(0xFF4E342E) else MidGray,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Can you break your record?",
                                    color = if (hasUnclaimedDailyMissions) Color(0xFFE65100) else Color(0xFF555566),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open Missions",
                                tint = PureBlack,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Primary Start Flight Button
                    Button(
                        onClick = onStartFlight,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2B2B33),
                            contentColor = PureWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp))
                            .testTag("start_flight_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START ASCENT",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Menu Buttons Row 1: How To Play & Skins Wardrobe
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenHowToPlay,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, PureBlack),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PureBlack),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("how_to_play_button")
                        ) {
                            Icon(imageVector = Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("HOW TO PLAY", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onOpenWardrobe,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, PureBlack),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PureBlack),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("wardrobe_skins_button")
                        ) {
                            Icon(imageVector = Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SKINS", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary Menu Buttons Row 2: Settings & Free Coins
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenSettings,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF6B6B78)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF33333F)),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("settings_button")
                        ) {
                            Icon(imageVector = Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("SETTINGS", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onWatchAdForCoins,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF9A825),
                                contentColor = PureBlack
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("free_coins_ad_button")
                        ) {
                            Icon(imageVector = Icons.Filled.OndemandVideo, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("FREE +1 🪙", fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Comprehensive "How to Play" presentation dialog
 */
@Composable
fun HowToPlayDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(22.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, PureBlack),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(600.dp)
                .shadow(24.dp, RoundedCornerShape(22.dp))
                .testTag("how_to_play_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HOW TO PLAY",
                            color = PureBlack,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Flight Guide, Hazards & Rewards",
                            color = MidGray,
                            fontSize = 11.5.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFF0F0F4), contentColor = PureBlack),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFE2E2E8))
                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Controls & Balloon Physics
                    GuideSectionCard(
                        title = "🎈 FLIGHT CONTROLS & PHYSICS",
                        accentColor = Color(0xFFD32F2F)
                    ) {
                        Text(
                            text = "• HOLD & DRAG anywhere on the screen to steer the balloon in full 360° motion (up, down, left, right).\n" +
                                    "• RELEASE touch to hover at your current position and ride natural airflow.\n" +
                                    "• Realistic Aerodynamics: The balloon realistically bobs and sways with wind resistance and stretches as you climb.",
                            color = DarkCharcoal,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }

                    // 2. 3 Lives System & Ad Revives
                    GuideSectionCard(
                        title = "❤️ 3 LIVES & AD REVIVES",
                        accentColor = BalloonRed
                    ) {
                        Text(
                            text = "• You begin each ascent with 3 Lives.\n" +
                                    "• Colliding with an obstacle pops 1 life and gives brief invulnerability.\n" +
                                    "• Rare Extra Life Hearts spawn every 5,000 meters to restore lost lives.\n" +
                                    "• If all 3 lives pop, you get up to 5 chances per flight to watch a quick ad and revive (+1 life) to continue ascending!",
                            color = DarkCharcoal,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }

                    // 3. Lightning Clouds Hazard Effects
                    GuideSectionCard(
                        title = "⚡ STORM CLOUDS & LIGHTNING EFFECTS",
                        accentColor = Color(0xFFFFB300)
                    ) {
                        Text(
                            text = "• 🟡 YELLOW LIGHTNING CLOUD: Emits high-voltage golden electricity that IMMOBILIZES the player for 1 second, showing yellow lightning crackling all over you!\n" +
                                    "• 🔵 BLUE LIGHTNING CLOUD: Emits cyan overcharge energy making the character 50% BIGGER for 5 seconds (making it harder to evade projectiles)!\n" +
                                    "• 🟣 OTHER LIGHTNING / STORMS: All other lightning strikes SLOW DOWN your ascent speed for 4 seconds as heavy turbulent downdrafts drag you.",
                            color = DarkCharcoal,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }

                    // 4. Coin Rewards & Character Skins
                    GuideSectionCard(
                        title = "🪙 COIN REWARDS & WARDROBE",
                        accentColor = Color(0xFFF57F17)
                    ) {
                        Text(
                            text = "• Earn 1 COIN for every 1,000 meters of altitude attained during each flight!\n" +
                                    "• Collect FREE BONUS COINS (+1 coin) anytime by watching sponsored ads on the main menu.\n" +
                                    "• Spend your coins in the SKINS WARDROBE (starting at 25 coins for custom outfits) to customize your aviator with stylish male and female outfits, colorful coats, scarves, hats, and hairstyles!",
                            color = DarkCharcoal,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }

                    // 5. Highest Altitude Record Tracker
                    GuideSectionCard(
                        title = "🏆 HIGHEST ALTITUDE RECORD",
                        accentColor = Color(0xFF388E3C)
                    ) {
                        Text(
                            text = "• The game permanently tracks and showcases your Highest Altitude Ever Attained on the main menu and HUD header.\n" +
                                    "• Push past your previous record with each flight to conquer the upper stratosphere and deep space!",
                            color = DarkCharcoal,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }

                    // 6. Asteroid & Space Debris Hazards
                    GuideSectionCard(
                        title = "☄️ ASTEROIDS & ORBITAL DEBRIS",
                        accentColor = Color(0xFF5D4037)
                    ) {
                        Text(
                            text = "• Low-Orbit Pebbles & Meteors: Drifting high-speed rock fragments.\n" +
                                    "• Tumbling Boulders: Jagged rotating rocky masses.\n" +
                                    "• Cratered Asteroids & Monoliths: Massive cosmic space rocks appearing in deep space layers.",
                            color = DarkCharcoal,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }

                    // 7. Power-Up Collectibles
                    GuideSectionCard(
                        title = "🛡️ POWER-UP ITEMS",
                        accentColor = Color(0xFF7B1FA2)
                    ) {
                        Text(
                            text = "• Kinetic Shield (Cyan Orb): Absorbs and deflects a direct hazard impact.\n" +
                                    "• Sonic Speed Boost (Golden Orb): Rocket-boosts upward climb speed.\n" +
                                    "• Heart Capsule (Red Heart): Restores +1 lost life (spawns at 5,000m milestones).",
                            color = DarkCharcoal,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFE2E2E8))
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PureBlack, contentColor = PureWhite),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("close_how_to_play_button")
                ) {
                    Text("GOT IT! LET'S FLY", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
            }
        }
    }
}

@Composable
private fun GuideSectionCard(
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        color = Color(0xFFF7F7FA),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2E8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = accentColor,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.size(8.dp, 16.dp)
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = PureBlack,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

/**
 * Character Skins Customization & Coin Shop Dialog
 */
@Composable
fun WardrobeSkinsDialog(
    totalCoins: Int,
    currentSkin: CharacterSkin,
    unlockedSkinIds: Set<String>,
    onEquipSkin: (CharacterSkin) -> Unit,
    onUnlockSkin: (CharacterSkin) -> Boolean,
    onWatchAdForCoins: () -> Unit,
    onDismiss: () -> Unit
) {
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(22.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, PureBlack),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(640.dp)
                .shadow(24.dp, RoundedCornerShape(22.dp))
                .testTag("wardrobe_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header: Title, Coin Balance Badge & Ad Reward Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SKINS WARDROBE",
                            color = PureBlack,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Unlock & equip custom characters",
                            color = MidGray,
                            fontSize = 11.5.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFF0F0F4), contentColor = PureBlack),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Coin Balance and Watch Ad Bar
                Surface(
                    color = Color(0xFFFFF9C4),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBC02D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$totalCoins COINS",
                                color = PureBlack,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = onWatchAdForCoins,
                            colors = ButtonDefaults.buttonColors(containerColor = PureBlack, contentColor = PureWhite),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("wardrobe_ad_bonus_button")
                        ) {
                            Icon(imageVector = Icons.Default.OndemandVideo, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+5 COINS", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Temporary feedback message (e.g. "Not enough coins!")
                feedbackMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = Color(0xFFC62828),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE2E2E8))
                Spacer(modifier = Modifier.height(8.dp))

                // Skin Items List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(AvailableCharacterSkins) { skin ->
                        val isEquipped = skin.id == currentSkin.id
                        val isUnlocked = unlockedSkinIds.contains(skin.id) || skin.cost == 0

                        SkinCardItem(
                            skin = skin,
                            isEquipped = isEquipped,
                            isUnlocked = isUnlocked,
                            totalCoins = totalCoins,
                            onEquip = {
                                onEquipSkin(skin)
                                feedbackMessage = null
                            },
                            onUnlock = {
                                val success = onUnlockSkin(skin)
                                if (!success) {
                                    feedbackMessage = "Need ${skin.cost - totalCoins} more coins! Play or watch ads."
                                } else {
                                    feedbackMessage = null
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PureBlack, contentColor = PureWhite),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("close_wardrobe_button")
                ) {
                    Text("DONE", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
            }
        }
    }
}

@Composable
private fun SkinCardItem(
    skin: CharacterSkin,
    isEquipped: Boolean,
    isUnlocked: Boolean,
    totalCoins: Int,
    onEquip: () -> Unit,
    onUnlock: () -> Unit
) {
    Surface(
        color = if (isEquipped) Color(0xFFE8F5E9) else Color(0xFFF7F7FA),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (isEquipped) 2.dp else 1.dp,
            if (isEquipped) Color(0xFF4CAF50) else Color(0xFFDCDCE4)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("skin_item_${skin.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Skin visual thumbnail
            SkinAvatarPreview(skin = skin, modifier = Modifier.size(50.dp, 60.dp))

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = skin.name,
                        color = PureBlack,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = if (skin.gender == GenderStyle.FEMALE) Color(0xFFFCE4EC) else Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = skin.gender.displayName.uppercase(),
                            color = if (skin.gender == GenderStyle.FEMALE) Color(0xFFC2185B) else Color(0xFF1976D2),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                        )
                    }
                }

                Text(
                    text = skin.description,
                    color = MidGray,
                    fontSize = 10.5.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                // Color swatch dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(color = skin.coatColor, shape = CircleShape, modifier = Modifier.size(8.dp)) {}
                    Surface(color = skin.scarfColor, shape = CircleShape, modifier = Modifier.size(8.dp)) {}
                    Surface(color = skin.hatColor, shape = CircleShape, modifier = Modifier.size(8.dp)) {}
                    Surface(color = skin.hairColor, shape = CircleShape, modifier = Modifier.size(8.dp)) {}
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Button (Equipped / Equip / Unlock with Coins)
            when {
                isEquipped -> {
                    Surface(
                        color = Color(0xFF2E7D32),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "EQUIPPED",
                            color = PureWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
                isUnlocked -> {
                    OutlinedButton(
                        onClick = onEquip,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, PureBlack),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PureBlack),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("EQUIP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    val canAfford = totalCoins >= skin.cost
                    Button(
                        onClick = onUnlock,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canAfford) Color(0xFFF9A825) else Color(0xFF9E9EA8),
                            contentColor = if (canAfford) PureBlack else PureWhite
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${skin.cost} 🪙", fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

/**
 * Settings and Audio/Vibration control dialog
 */
@Composable
fun SettingsDialog(
    soundEnabled: Boolean,
    onToggleSound: (Boolean) -> Unit,
    musicEnabled: Boolean,
    onToggleMusic: (Boolean) -> Unit,
    hapticsEnabled: Boolean,
    onToggleHaptics: (Boolean) -> Unit,
    maxAltitudeEver: Long,
    totalCoins: Int,
    onDismiss: () -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(22.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, PureBlack),
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .widthIn(max = 380.dp)
                .shadow(20.dp, RoundedCornerShape(22.dp))
                .testTag("settings_dialog")
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SETTINGS",
                        color = PureBlack,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFF0F0F4), contentColor = PureBlack),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sound FX Toggle
                SettingToggleRow(
                    title = "Sound Effects (SFX)",
                    subtitle = "Balloon pop, wind, pickups & milestones",
                    icon = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    isChecked = soundEnabled,
                    onCheckedChange = onToggleSound
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Background Music Toggle
                SettingToggleRow(
                    title = "Background Music",
                    subtitle = "Arcade soundtrack & cosmic groove",
                    icon = if (musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                    isChecked = musicEnabled,
                    onCheckedChange = onToggleMusic
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Vibration / Haptics Toggle
                SettingToggleRow(
                    title = "Haptic Vibration",
                    subtitle = "Tactile impact & collision vibrations",
                    icon = Icons.Default.Vibration,
                    isChecked = hapticsEnabled,
                    onCheckedChange = onToggleHaptics
                )

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = Color(0xFFE2E2E8))
                Spacer(modifier = Modifier.height(14.dp))

                // Lifetime Stats Card
                Surface(
                    color = Color(0xFFF4F4F8),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCDCE4)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "LIFETIME RECORDS",
                            color = MidGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Highest Altitude Record", color = PureBlack, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "${formatter.format(maxAltitudeEver)} m",
                                color = PureBlack,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Coins Accumulated", color = PureBlack, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "$totalCoins 🪙",
                                color = PureBlack,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PureBlack, contentColor = PureWhite),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("close_settings_button")
                ) {
                    Text("SAVE & CLOSE", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = Color(0xFFF7F7FA),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2E8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isChecked) PureBlack else MidGray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = title, color = PureBlack, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = subtitle, color = MidGray, fontSize = 10.5.sp)
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PureWhite,
                    checkedTrackColor = Color(0xFF2B2B33),
                    uncheckedThumbColor = PureWhite,
                    uncheckedTrackColor = Color(0xFFC0C0C8)
                )
            )
        }
    }
}

/**
 * Quick sponsored ad reward dialog from the main menu (+5 coins reward)
 */
@Composable
fun MenuAdRewardDialog(
    onRewardEarned: () -> Unit,
    onDismiss: () -> Unit
) {
    var secondsRemaining by remember { mutableIntStateOf(3) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            kotlinx.coroutines.delay(1000L)
            secondsRemaining--
        }
        isFinished = true
        kotlinx.coroutines.delay(500L)
        onRewardEarned()
    }

    Dialog(onDismissRequest = { if (isFinished) onRewardEarned() else onDismiss() }) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, PureBlack),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .shadow(20.dp, RoundedCornerShape(20.dp))
                .testTag("menu_ad_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                        text = if (secondsRemaining > 0) "Reward in ${secondsRemaining}s" else "+1 Coin Unlocked!",
                        color = if (secondsRemaining > 0) MidGray else Color(0xFF2E7D32),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { ((3 - secondsRemaining) / 3f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = Color(0xFFF9A825),
                    trackColor = Color(0xFFE2E2E8)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = Color(0xFF1E1E24),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🪙 +1 FREE COIN", color = Color(0xFFFFD54F), fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Thanks for supporting the Balloon Ascent!",
                            color = PureWhite,
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onRewardEarned,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFinished) Color(0xFF2E7D32) else Color(0xFF55555F),
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    if (isFinished) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CLAIM +1 COIN", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    } else {
                        Text("WATCHING (${secondsRemaining}s)...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/**
 * Full-screen styled Daily Missions dialog
 */
@Composable
fun DailyMissionsDialog(
    missions: List<DailyMission>,
    totalCoins: Int,
    isGrandBonusClaimed: Boolean,
    onClaimMission: (String) -> Unit,
    onClaimGrandBonus: () -> Unit,
    onDismiss: () -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }
    val completedCount = missions.count { it.isCompleted || it.isClaimed }
    val allClaimed = missions.all { it.isClaimed }
    val grandBonusCoins = 5

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(22.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, PureBlack),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 480.dp)
                .shadow(16.dp, RoundedCornerShape(22.dp))
                .testTag("daily_missions_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top bar with Title, Coin Counter & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFFFF8E1),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF9A825)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🎯", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "DAILY MISSIONS",
                                color = PureBlack,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "New challenges every 24 hours",
                                color = MidGray,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFF5F5F9),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCDCE4)),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🪙", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatter.format(totalCoins),
                                    color = PureBlack,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("close_daily_missions_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = PureBlack,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Signature Phrase & Motivation Banner
                Surface(
                    color = Color(0xFF2B2B33),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Can you break your record?",
                            color = Color(0xFFFFD54F),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Complete your daily flight objectives to earn bonus coins for custom aviator uniforms and hats!",
                            color = Color(0xFFE2E2EC),
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Daily Progress Bar & Grand Bonus Card
                Surface(
                    color = Color(0xFFF7F7FC),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E2EC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DAILY COMPLETION",
                                color = DeepGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$completedCount / ${missions.size} Completed",
                                color = PureBlack,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (completedCount.toFloat() / missions.size.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF2E7D32),
                            trackColor = Color(0xFFE0E0E8)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Grand completion bonus row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎁", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "All-Clear Daily Bonus",
                                        color = PureBlack,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "+$grandBonusCoins Bonus Coins for all 4",
                                        color = MidGray,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            if (isGrandBonusClaimed) {
                                Surface(
                                    color = Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784))
                                ) {
                                    Text(
                                        text = "CLAIMED ✅",
                                        color = Color(0xFF2E7D32),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else if (allClaimed) {
                                Button(
                                    onClick = onClaimGrandBonus,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFF9A825),
                                        contentColor = PureBlack
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("claim_grand_bonus_button")
                                ) {
                                    Text("CLAIM +$grandBonusCoins 🪙", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            } else {
                                Surface(
                                    color = Color(0xFFECECF2),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "LOCKED",
                                        color = MidGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "TODAY'S MISSIONS",
                    color = PureBlack,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // List of Mission Cards
                missions.forEach { mission ->
                    val isDistOrAlt = mission.missionType == MissionType.FLY_DISTANCE || mission.missionType == MissionType.BREAK_RECORD
                    val unit = if (isDistOrAlt) "m" else ""

                    Surface(
                        color = PureWhite,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            if (mission.isCompleted && !mission.isClaimed) 1.5.dp else 1.dp,
                            if (mission.isCompleted && !mission.isClaimed) Color(0xFFF9A825) else Color(0xFFE2E2EC)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("mission_card_${mission.id}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (mission.isClaimed) Color(0xFFF0F0F4) else if (mission.isCompleted) Color(0xFFFFF9C4) else Color(0xFFF4F4F8),
                                    shape = CircleShape,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(mission.iconEmoji, fontSize = 16.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mission.title,
                                        color = PureBlack,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = mission.description,
                                        color = MidGray,
                                        fontSize = 10.5.sp,
                                        lineHeight = 13.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Progress Bar & Action Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "PROGRESS",
                                            color = MidGray,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${formatter.format(mission.currentProgress)} / ${formatter.format(mission.targetProgress)}$unit",
                                            color = PureBlack,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { mission.progressFraction },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (mission.isCompleted) Color(0xFF2E7D32) else Color(0xFF1E88E5),
                                        trackColor = Color(0xFFEAEAEE)
                                    )
                                }

                                // Claim / Status Button
                                if (mission.isClaimed) {
                                    Surface(
                                        color = Color(0xFFF1F1F4),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "CLAIMED ✅",
                                            color = MidGray,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                } else if (mission.isCompleted) {
                                    Button(
                                        onClick = { onClaimMission(mission.id) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFF9A825),
                                            contentColor = PureBlack
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("claim_mission_${mission.id}")
                                    ) {
                                        Text(
                                            text = "CLAIM +${mission.coinReward} 🪙",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                } else {
                                    Surface(
                                        color = Color(0xFFF4F4F8),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "+${mission.coinReward} 🪙",
                                                color = DeepGray,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ready to Fly Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2B2B33),
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("daily_missions_fly_button")
                ) {
                    Icon(imageVector = Icons.Default.FlightTakeoff, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LET'S FLY & BREAK RECORDS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
