package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.domain.AvatarPreset
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.GoldCoin
import kotlinx.coroutines.delay

/**
 * Custom vector/SVG-style Canvas illustration for each of the 8 unlockable student avatars.
 */
@Composable
fun AvatarSvgBadge(
    avatar: AvatarPreset,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(avatar.primaryColor.copy(alpha = 0.28f))
            .border(2.dp, avatar.accentColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.78f)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f

            // Background glow disc
            drawCircle(
                color = avatar.primaryColor,
                radius = w * 0.44f,
                center = Offset(cx, cy)
            )

            when (avatar) {
                AvatarPreset.ASTRO_OWL -> {
                    // Owl ears/tufts
                    val earPath = Path().apply {
                        moveTo(w * 0.22f, h * 0.30f)
                        lineTo(w * 0.14f, h * 0.12f)
                        lineTo(w * 0.38f, h * 0.22f)
                        close()
                        moveTo(w * 0.78f, h * 0.30f)
                        lineTo(w * 0.86f, h * 0.12f)
                        lineTo(w * 0.62f, h * 0.22f)
                        close()
                    }
                    drawPath(earPath, avatar.secondaryColor)
                    // Big glasses eyes
                    drawCircle(Color.White, radius = w * 0.16f, center = Offset(w * 0.35f, h * 0.46f))
                    drawCircle(Color.White, radius = w * 0.16f, center = Offset(w * 0.65f, h * 0.46f))
                    drawCircle(Color(0xFF0F172A), radius = w * 0.08f, center = Offset(w * 0.35f, h * 0.46f))
                    drawCircle(Color(0xFF0F172A), radius = w * 0.08f, center = Offset(w * 0.65f, h * 0.46f))
                    // Golden beak
                    val beak = Path().apply {
                        moveTo(cx - w * 0.07f, h * 0.54f)
                        lineTo(cx + w * 0.07f, h * 0.54f)
                        lineTo(cx, h * 0.68f)
                        close()
                    }
                    drawPath(beak, avatar.accentColor)
                }

                AvatarPreset.VOLT_FOX -> {
                    // Pointed fox ears
                    val ears = Path().apply {
                        moveTo(w * 0.18f, h * 0.42f)
                        lineTo(w * 0.15f, h * 0.10f)
                        lineTo(w * 0.42f, h * 0.28f)
                        close()
                        moveTo(w * 0.82f, h * 0.42f)
                        lineTo(w * 0.85f, h * 0.10f)
                        lineTo(w * 0.58f, h * 0.28f)
                        close()
                    }
                    drawPath(ears, avatar.accentColor)
                    // Fox cheeks
                    val face = Path().apply {
                        moveTo(w * 0.18f, h * 0.48f)
                        lineTo(cx, h * 0.82f)
                        lineTo(w * 0.82f, h * 0.48f)
                        close()
                    }
                    drawPath(face, avatar.secondaryColor)
                    drawCircle(Color(0xFF0F172A), radius = w * 0.06f, center = Offset(w * 0.36f, h * 0.46f))
                    drawCircle(Color(0xFF0F172A), radius = w * 0.06f, center = Offset(w * 0.64f, h * 0.46f))
                    drawCircle(Color(0xFF0F172A), radius = w * 0.05f, center = Offset(cx, h * 0.74f))
                }

                AvatarPreset.CYBER_BOT -> {
                    // Antenna
                    drawLine(
                        color = avatar.accentColor,
                        start = Offset(cx, h * 0.08f),
                        end = Offset(cx, h * 0.26f),
                        strokeWidth = w * 0.06f
                    )
                    drawCircle(avatar.accentColor, radius = w * 0.07f, center = Offset(cx, h * 0.08f))
                    // Visor
                    drawRoundRect(
                        color = Color(0xFF0F172A),
                        topLeft = Offset(w * 0.22f, h * 0.34f),
                        size = Size(w * 0.56f, h * 0.22f),
                        cornerRadius = CornerRadius(w * 0.08f)
                    )
                    drawCircle(avatar.secondaryColor, radius = w * 0.06f, center = Offset(w * 0.37f, h * 0.45f))
                    drawCircle(avatar.secondaryColor, radius = w * 0.06f, center = Offset(w * 0.63f, h * 0.45f))
                    // Voice grid
                    drawRoundRect(
                        color = avatar.secondaryColor,
                        topLeft = Offset(w * 0.32f, h * 0.65f),
                        size = Size(w * 0.36f, h * 0.08f),
                        cornerRadius = CornerRadius(4f)
                    )
                }

                AvatarPreset.NEON_CAT -> {
                    // Cat ears
                    val ears = Path().apply {
                        moveTo(w * 0.20f, h * 0.38f)
                        lineTo(w * 0.18f, h * 0.14f)
                        lineTo(w * 0.42f, h * 0.26f)
                        close()
                        moveTo(w * 0.80f, h * 0.38f)
                        lineTo(w * 0.82f, h * 0.14f)
                        lineTo(w * 0.58f, h * 0.26f)
                        close()
                    }
                    drawPath(ears, avatar.secondaryColor)
                    // Glowing cat eyes
                    drawCircle(avatar.accentColor, radius = w * 0.09f, center = Offset(w * 0.35f, h * 0.46f))
                    drawCircle(avatar.accentColor, radius = w * 0.09f, center = Offset(w * 0.65f, h * 0.46f))
                    drawCircle(Color(0xFF0F172A), radius = w * 0.04f, center = Offset(w * 0.35f, h * 0.46f))
                    drawCircle(Color(0xFF0F172A), radius = w * 0.04f, center = Offset(w * 0.65f, h * 0.46f))
                    // Whiskers
                    drawLine(Color.White, Offset(w * 0.15f, h * 0.58f), Offset(w * 0.34f, h * 0.60f), strokeWidth = 3f)
                    drawLine(Color.White, Offset(w * 0.85f, h * 0.58f), Offset(w * 0.66f, h * 0.60f), strokeWidth = 3f)
                }

                AvatarPreset.QUANTUM_DRAGON -> {
                    // Dragon horns
                    val horns = Path().apply {
                        moveTo(w * 0.28f, h * 0.28f)
                        lineTo(w * 0.16f, h * 0.08f)
                        lineTo(w * 0.40f, h * 0.22f)
                        close()
                        moveTo(w * 0.72f, h * 0.28f)
                        lineTo(w * 0.84f, h * 0.08f)
                        lineTo(w * 0.60f, h * 0.22f)
                        close()
                    }
                    drawPath(horns, avatar.accentColor)
                    // Crest
                    drawCircle(avatar.secondaryColor, radius = w * 0.07f, center = Offset(cx, h * 0.24f))
                    // Dragon eyes
                    drawCircle(avatar.accentColor, radius = w * 0.08f, center = Offset(w * 0.36f, h * 0.48f))
                    drawCircle(avatar.accentColor, radius = w * 0.08f, center = Offset(w * 0.64f, h * 0.48f))
                    // Snout nostrils
                    drawCircle(Color(0xFF0F172A), radius = w * 0.035f, center = Offset(w * 0.43f, h * 0.66f))
                    drawCircle(Color(0xFF0F172A), radius = w * 0.035f, center = Offset(w * 0.57f, h * 0.66f))
                }

                AvatarPreset.ZEN_PANDA -> {
                    // Round black panda ears
                    drawCircle(Color(0xFF0F172A), radius = w * 0.13f, center = Offset(w * 0.24f, h * 0.24f))
                    drawCircle(Color(0xFF0F172A), radius = w * 0.13f, center = Offset(w * 0.76f, h * 0.24f))
                    // White panda face
                    drawCircle(avatar.secondaryColor, radius = w * 0.36f, center = Offset(cx, cy + h * 0.04f))
                    // Eye patches
                    drawCircle(Color(0xFF0F172A), radius = w * 0.10f, center = Offset(w * 0.36f, h * 0.48f))
                    drawCircle(Color(0xFF0F172A), radius = w * 0.10f, center = Offset(w * 0.64f, h * 0.48f))
                    drawCircle(Color.White, radius = w * 0.035f, center = Offset(w * 0.36f, h * 0.46f))
                    drawCircle(Color.White, radius = w * 0.035f, center = Offset(w * 0.64f, h * 0.46f))
                    // Nose
                    drawCircle(avatar.accentColor, radius = w * 0.05f, center = Offset(cx, h * 0.62f))
                }

                AvatarPreset.STAR_UNICORN -> {
                    // Golden unicorn horn
                    val horn = Path().apply {
                        moveTo(cx - w * 0.09f, h * 0.32f)
                        lineTo(cx + w * 0.09f, h * 0.32f)
                        lineTo(cx, h * 0.04f)
                        close()
                    }
                    drawPath(horn, avatar.accentColor)
                    // Sparkle eyes
                    drawCircle(Color.White, radius = w * 0.09f, center = Offset(w * 0.36f, h * 0.50f))
                    drawCircle(Color.White, radius = w * 0.09f, center = Offset(w * 0.64f, h * 0.50f))
                    drawCircle(avatar.primaryColor, radius = w * 0.045f, center = Offset(w * 0.36f, h * 0.50f))
                    drawCircle(avatar.primaryColor, radius = w * 0.045f, center = Offset(w * 0.64f, h * 0.50f))
                    // Smile arc
                    drawArc(
                        color = avatar.secondaryColor,
                        startAngle = 20f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(w * 0.35f, h * 0.56f),
                        size = Size(w * 0.30f, h * 0.18f),
                        style = Stroke(width = 4f)
                    )
                }

                AvatarPreset.CROWN_LION -> {
                    // Royal crown on top
                    val crown = Path().apply {
                        moveTo(w * 0.25f, h * 0.32f)
                        lineTo(w * 0.22f, h * 0.12f)
                        lineTo(w * 0.38f, h * 0.24f)
                        lineTo(cx, h * 0.08f)
                        lineTo(w * 0.62f, h * 0.24f)
                        lineTo(w * 0.78f, h * 0.12f)
                        lineTo(w * 0.75f, h * 0.32f)
                        close()
                    }
                    drawPath(crown, avatar.secondaryColor)
                    // Lion eyes & mane ring
                    drawCircle(
                        color = avatar.accentColor,
                        radius = w * 0.38f,
                        center = Offset(cx, cy + h * 0.05f),
                        style = Stroke(width = 5f)
                    )
                    drawCircle(Color(0xFF0F172A), radius = w * 0.06f, center = Offset(w * 0.37f, h * 0.50f))
                    drawCircle(Color(0xFF0F172A), radius = w * 0.06f, center = Offset(w * 0.63f, h * 0.50f))
                    drawCircle(avatar.secondaryColor, radius = w * 0.06f, center = Offset(cx, h * 0.64f))
                }
            }
        }
    }
}

/**
 * Clean Banner Ad Placeholder exclusively for the bottom of the Home Screen.
 * Never shown during gameplay. Ready for AdMob AdView integration.
 */
@Composable
fun HomeBannerAdPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.78f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp)
            .testTag("home_ad_banner_placeholder"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(GoldCoin.copy(alpha = 0.25f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "AD",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldCoin,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.ad_banner_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Clean Interstitial Ad Placeholder Modal triggered after every 3 completed games.
 * Ready to wrap real AdMob InterstitialAd.show().
 */
@Composable
fun InterstitialAdModal(
    onDismiss: () -> Unit
) {
    var secondsLeft by remember { mutableIntStateOf(2) }
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
    }

    Dialog(
        onDismissRequest = { if (secondsLeft <= 0) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .testTag("interstitial_ad_modal")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "Interstitial Ad Placeholder",
                    tint = GoldCoin,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "showInterstitialAd()",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldCoin
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ad Break Placeholder",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Shown automatically after every 3 games.\nConnect your AdMob Interstitial Unit ID here later.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    enabled = secondsLeft <= 0,
                    colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("close_interstitial_ad_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (secondsLeft > 0) "Continue in ${secondsLeft}s..." else "Continue to Results",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Clean Rewarded Ad Placeholder Modal for "Extra Life (+15s)" and "Double Coins (2x)".
 * Calls [onRewardEarned] upon finishing the simulated 2-second preview.
 */
@Composable
fun RewardedAdModal(
    rewardTitle: String,
    rewardSubtitle: String,
    onRewardEarned: () -> Unit,
    onCancel: () -> Unit
) {
    var progressTicks by remember { mutableIntStateOf(0) }
    val totalTicks = 20 // 2 seconds total (100ms * 20)

    LaunchedEffect(Unit) {
        while (progressTicks < totalTicks) {
            delay(100L)
            progressTicks++
        }
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, GoldCoin.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .testTag("rewarded_ad_modal")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Rewarded Ad",
                    tint = GoldCoin,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "showRewardedAd(callback)",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldCoin
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = rewardTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = rewardSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progressTicks.toFloat() / totalTicks.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = GoldCoin,
                    trackColor = Color.White.copy(alpha = 0.15f)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("cancel_rewarded_ad_button")
                    ) {
                        Text("Skip", color = Color.White)
                    }
                    Button(
                        onClick = onRewardEarned,
                        enabled = progressTicks >= totalTicks,
                        colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("claim_rewarded_ad_button")
                    ) {
                        Text(
                            text = if (progressTicks < totalTicks) "Watching Ad..." else "Claim Reward!",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
