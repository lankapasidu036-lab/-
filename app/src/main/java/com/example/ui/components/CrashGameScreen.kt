package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.BetHistory
import com.example.data.db.MultiplierHistory
import com.example.ui.GameState
import com.example.ui.GameUiState
import com.example.ui.GameViewModel
import com.example.ui.LiveBettor
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashGameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
    pastRounds: List<MultiplierHistory> = emptyList(),
    myBets: List<BetHistory> = emptyList()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Periodically update the daily claim countdown timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            viewModel.refreshDailyState()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0D0B14),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D0B14),
                    titleContentColor = Color.White
                ),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🚀 සල්ලි උල්පත CRASH",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        }
                        // Balance plate
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2130)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0x33FFD700))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💰 ",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = String.format(Locale.US, "%,.2f", uiState.balance),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50),
                                    modifier = Modifier.testTag("user_balance_label")
                                )
                                Text(
                                    text = " K-Coins",
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0B14))
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            // Live past multipliers row
            PastMultipliersRow(pastRounds = pastRounds)

            Spacer(modifier = Modifier.height(8.dp))

            // Main Arena (Canvas Flight Animation)
            FlightArena(uiState = uiState, onCashOutClick = { viewModel.cashOut() })

            Spacer(modifier = Modifier.height(12.dp))

            // Main Bet placement panel
            BetControllerPanel(
                uiState = uiState,
                onBetAmountChanged = { viewModel.setBetAmount(it) },
                onModifyBetAmount = { viewModel.modifyBetAmount(it) },
                onDoubleBet = { viewModel.doubleBet() },
                onHalveBet = { viewModel.halveBet() },
                onPlaceBetClick = { viewModel.placeBet() },
                onCashOutClick = { viewModel.cashOut() },
                onAutoCashOutMultiplierChanged = { viewModel.setAutoCashOutMultiplier(it) },
                onToggleAutoCashOut = { viewModel.toggleAutoCashOut(it) },
                onToggleAutoBet = { viewModel.toggleAutoBet(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Information details & custom simulated user history tabs
            StatsAndDetailsSection(
                uiState = uiState,
                myBets = myBets,
                onClaimDailyReward = { viewModel.claimDailyReward() },
                onDeposit = { viewModel.depositMoney(it) },
                onWithdraw = { viewModel.withdrawMoney(it) },
                onClaimHost = { viewModel.claimHostEarnings() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Immersive persistent footer navigation row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Games Tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { }
                        .alpha(0.4f)
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                ) {
                    Text("🕹️", fontSize = 20.sp)
                    Text("GAMES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Crash Tab (Active)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { }
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                ) {
                    Box {
                        Text("🔥", fontSize = 20.sp)
                        // Rose notification dot badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF43F5E))
                                .border(1.dp, Color(0xFF161421), CircleShape)
                        )
                    }
                    Text("CRASH", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
                }

                // Account Tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { }
                        .alpha(0.4f)
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                ) {
                    Text("👤", fontSize = 20.sp)
                    Text("ACCOUNT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PastMultipliersRow(pastRounds: List<MultiplierHistory>) {
    val presetMultipliers = listOf(1.34, 4.21, 1.05, 12.43, 2.10, 1.15, 1.89)
    val displayList = if (pastRounds.isEmpty()) {
        presetMultipliers.map { MultiplierHistory(multiplier = it) }
    } else {
        pastRounds
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        displayList.take(12).forEach { item ->
            val color = when {
                item.multiplier >= 10.0 -> Color(0xFFFFD700) // Gold
                item.multiplier >= 2.0 -> Color(0xFF4CAF50)  // Green
                item.multiplier >= 1.2 -> Color(0xFF2196F3)  // Blue
                else -> Color(0xFFE91E63)                     // Red
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.15f))
                    .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "%.2fx", item.multiplier),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
fun FlightArena(
    uiState: GameUiState,
    onCashOutClick: () -> Unit
) {
    // Generate particle animation offset triggers
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val starWaveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star_movement"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13111E))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Under-curve glowing radial center gradient for the cosmic scene
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF6366F1).copy(alpha = 0.12f), Color.Transparent),
                        center = center,
                        radius = size.width * 0.7f
                    )
                )
            }
            
            // Dynamic Grid Lines and Space Stars Background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw Grid lines
                val cols = 8
                val rows = 5
                val dx = w / cols
                val dy = h / rows

                for (i in 1 until cols) {
                    drawLine(
                        color = Color(0xFF6366F1).copy(alpha = 0.05f),
                        start = Offset(i * dx, 0f),
                        end = Offset(i * dx, h),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                for (j in 1 until rows) {
                    drawLine(
                        color = Color(0xFF6366F1).copy(alpha = 0.05f),
                        start = Offset(0f, j * dy),
                        end = Offset(w, j * dy),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw simulated drifting cosmic dust / stars passing back
                val randomPoints = listOf(
                    Offset(100f, 50f), Offset(350f, 120f), Offset(600f, 80f),
                    Offset(800f, 150f), Offset(150f, 220f), Offset(450f, 180f),
                    Offset(750f, 210f), Offset(900f, 100f)
                )

                randomPoints.forEach { star ->
                    var xPos = star.x - starWaveOffset
                    if (xPos < 0) {
                        xPos += w + 100f
                    }
                    val boundedX = xPos % (w + 100f)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.35f),
                        radius = 2.dp.toPx(),
                        center = Offset(boundedX, star.y)
                    )
                }
            }

            // Real-time Flight Path Rendering
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 20.dp, start = 20.dp, end = 20.dp, top = 20.dp)
            ) {
                val w = size.width
                val h = size.height

                if (uiState.gameState == GameState.FLYING || uiState.gameState == GameState.CRASHED) {
                    // Normalize progress for drawing: limit curve at 5.0x visually
                    val maxVisMulti = 5.0
                    val currentMulti = uiState.currentMultiplier
                    val progressRatio = ((currentMulti - 1.0) / (maxVisMulti - 1.0)).coerceIn(0.0, 1.0)

                    // Target coordinates of rocket flight path
                    val targetX = progressRatio.toFloat() * (w * 0.85f)
                    val targetY = h - (progressRatio.toFloat() * (h * 0.8f))

                    // Draw Bezier trail
                    val path = Path().apply {
                        moveTo(0f, h)
                        quadraticTo(
                            targetX * 0.4f, h * 0.95f, // control point
                            targetX, targetY
                        )
                    }

                    val neonColor = if (uiState.gameState == GameState.FLYING) Color(0xFF6366F1) else Color(0xFFEC4899)
                    drawPath(
                        path = path,
                        color = neonColor,
                        style = Stroke(
                            width = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Draw under-curve glowing linear gradient mask
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(targetX, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                neonColor.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )

                    if (uiState.gameState == GameState.FLYING) {
                        // Draw engine flame particles emitting from target points
                        for (i in 0..12) {
                            val rDist = (5..35).random()
                            val rAngle = 210f + (0..60).random().toFloat()
                            val rad = rAngle * (Math.PI / 180f)
                            val pX = targetX + cos(rad).toFloat() * rDist
                            val pY = targetY - sin(rad).toFloat() * rDist
                            drawCircle(
                                color = Color(0xFFFFD700).copy(alpha = 0.6f),
                                radius = (1..3).random().dp.toPx(),
                                center = Offset(pX, pY)
                            )
                        }

                        // Drawing a stylized vector rocket at header coordinate point
                        val rWidth = 14.dp.toPx()
                        val rHeight = 22.dp.toPx()

                        // Simple sleek rocket shape drawn pointing top-right (rotated ~45 deg)
                        val rocketPath = Path().apply {
                            moveTo(targetX, targetY - rHeight * 0.7f) // Nose
                            lineTo(targetX + rWidth * 0.5f, targetY) // Right side
                            lineTo(targetX + rWidth * 0.4f, targetY + rHeight * 0.3f) // Right fin
                            lineTo(targetX - rWidth * 0.4f, targetY + rHeight * 0.3f) // Left fin
                            lineTo(targetX - rWidth * 0.5f, targetY) // Left side
                            close()
                        }
                        drawPath(path = rocketPath, color = Color.White)
                        // Inner cockpit details
                        drawCircle(
                            color = Color(0xFF2196F3),
                            radius = rWidth * 0.2f,
                            center = Offset(targetX, targetY - rHeight * 0.1f)
                        )
                    } else if (uiState.gameState == GameState.CRASHED) {
                        // Explosion Animation at Crash Point
                        drawCircle(
                            color = Color(0xFFFF3D00).copy(alpha = 0.3f),
                            radius = 35.dp.toPx(),
                            center = Offset(targetX, targetY)
                        )
                        drawCircle(
                            color = Color(0xFFFFEB3B).copy(alpha = 0.7f),
                            radius = 16.dp.toPx(),
                            center = Offset(targetX, targetY)
                        )
                        
                        // Fire spark explosions flying everywhere
                        for (i in 0..18) {
                            val expDist = (15..60).random()
                            val expAngle = (0..360).random().toFloat()
                            val rad = expAngle * (Math.PI / 180f)
                            val expX = targetX + cos(rad).toFloat() * expDist
                            val expY = targetY + sin(rad).toFloat() * expDist
                            drawCircle(
                                color = Color(0xFFFF5722),
                                radius = (2..5).random().dp.toPx(),
                                center = Offset(expX, expY)
                            )
                        }
                    }
                }
            }

            // Big Centered Multiplier / Game Status Displays
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (uiState.gameState) {
                    GameState.WAITING_FOR_BETS -> {
                        Text(
                            text = "සල්ලි උල්පත රොකට් පිටත්වීමට සූදානම්!",
                            fontSize = 11.sp,
                            color = Color(0xFFFFC107),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                progress = { (uiState.countdownSeconds / 6.0).toFloat().coerceIn(0f, 1f) },
                                color = Color(0xFF4CAF50),
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = String.format(Locale.US, "තත්. %.1fs කින්", uiState.countdownSeconds),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.testTag("countdown_timer_label")
                            )
                        }
                        Text(
                            text = "ඔට්ටු තබන්න.. (PLACE BETS)",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    GameState.FLYING -> {
                        Text(
                            text = String.format(Locale.US, "%,.2f x", uiState.currentMultiplier),
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            style = androidx.compose.ui.text.TextStyle(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White, Color(0xFFC7D2FE))
                                )
                            ),
                            modifier = Modifier.testTag("flying_multiplier_label")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0x226366F1))
                                .border(1.dp, Color(0x4D6366F1), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "GROWING...",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA5B4FC),
                                letterSpacing = 1.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Show "User active bet cashout indicator"
                        if (uiState.hasPlacedBetInCurrentRound) {
                            if (uiState.hasCashedOutInCurrentRound) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xE62E7D32))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                        .animateContentSize()
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "මුදල් ලබා ගත්තා! (CASHED OUT)",
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = String.format(Locale.US, "+%,.1f K-Coins", uiState.userWinnings),
                                            fontSize = 16.sp,
                                            color = Color(0xFFFFD700),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = onCashOutClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = String.format(
                                            Locale.US,
                                            "CASH OUT: %,.1f",
                                            uiState.betAmount * uiState.currentMultiplier
                                        ),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }

                    GameState.CRASHED -> {
                        Text(
                            text = "FLEW AWAY!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF1744)
                        )
                        Text(
                            text = String.format(Locale.US, "@ %.2f x", uiState.currentMultiplier),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF1744),
                            modifier = Modifier.testTag("crashed_multiplier_label")
                        )
                        Text(
                            text = "සල්ලි උල්පත රොකට් එක කඩා වැටුනි! 💥",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BetControllerPanel(
    uiState: GameUiState,
    onBetAmountChanged: (Double) -> Unit,
    onModifyBetAmount: (Double) -> Unit,
    onDoubleBet: () -> Unit,
    onHalveBet: () -> Unit,
    onPlaceBetClick: () -> Unit,
    onCashOutClick: () -> Unit,
    onAutoCashOutMultiplierChanged: (Double) -> Unit,
    onToggleAutoCashOut: (Boolean) -> Unit,
    onToggleAutoBet: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161421))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            
            // Auto Bet & Auto Cashout Config Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Auto Bet Config Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161926)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ස්වයංක්‍රීය ඔට්ටුව", fontSize = 11.sp, color = Color.Gray)
                            Text("Auto Bet", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Switch(
                            checked = uiState.autoBetEnabled,
                            onCheckedChange = onToggleAutoBet,
                            modifier = Modifier.testTag("autobet_toggle")
                        )
                    }
                }

                // Auto Cash Out Config Card
                Card(
                    modifier = Modifier.weight(1.1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161926)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ස්වයංක්‍රීයව ගැනීම", fontSize = 10.sp, color = Color.Gray)
                            Text("Auto Cashout", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Switch(
                            checked = uiState.isAutoCashOutEnabled,
                            onCheckedChange = onToggleAutoCashOut,
                            modifier = Modifier.testTag("autocashout_toggle")
                        )
                    }
                }
            }

            // Auto Cash Out Limit Modifier (If enabled)
            AnimatedVisibility(
                visible = uiState.isAutoCashOutEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = "Auto Cashout Multiplier (ස්වයංක්‍රීය මුදල් අගය)",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F1018))
                            .border(1.dp, Color(0xFF2E324A), RoundedCornerShape(8.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onAutoCashOutMultiplierChanged(uiState.autoCashOutMultiplier - 0.1) }
                        ) {
                            Text("-", color = Color.Red, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = String.format(Locale.US, "%.2f x", uiState.autoCashOutMultiplier),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("auto_cashout_multiplier_val")
                        )
                        IconButton(
                            onClick = { onAutoCashOutMultiplierChanged(uiState.autoCashOutMultiplier + 0.1) }
                        ) {
                            Text("+", color = Color.Green, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF1E2130))

            // Bet Size controls keypad
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1.3f)) {
                    Text(
                        text = "ඔට්ටු තබන මුදල (Bet Amount):",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C0D15))
                            .border(1.dp, Color(0xFF222538), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🪙",
                            fontSize = 16.sp
                        )
                        Text(
                            text = String.format(Locale.US, "%.0f", uiState.betAmount),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.testTag("bet_amount_display")
                        )
                        Text(
                            text = " K-Coins",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Max/Min, 2x, 1/2 keypad row
                Column(modifier = Modifier.weight(1f)) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onHalveBet,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1E2B))
                        ) {
                            Text("1/2", fontSize = 11.sp, color = Color.White)
                        }
                        Button(
                            onClick = onDoubleBet,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1E2B))
                        ) {
                            Text("2x", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Presets row (+50, +100, +500, +1000)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(50, 100, 500, 1000).forEach { value ->
                    OutlinedButton(
                        onClick = { onModifyBetAmount(value.toDouble()) },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        border = BorderStroke(1.dp, Color(0xFF2C3048)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "+$value",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Giant dynamic action button for betting
            DynamicBetButton(
                uiState = uiState,
                onPlaceBetClick = onPlaceBetClick,
                onCashOutClick = onCashOutClick
            )
        }
    }
}

@Composable
fun DynamicBetButton(
    uiState: GameUiState,
    onPlaceBetClick: () -> Unit,
    onCashOutClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_glow"
    )

    when (uiState.gameState) {
        GameState.WAITING_FOR_BETS -> {
            if (uiState.hasPlacedBetInCurrentRound) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E2130),
                        disabledContainerColor = Color(0xFF1E2130)
                    ),
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "ඔට්ටුව තබන ලදී (BET PLACED) ⌛",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                val isAffordable = uiState.balance >= uiState.betAmount
                Button(
                    onClick = onPlaceBetClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAffordable) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isAffordable && uiState.betAmount >= 10.0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("gam_place_bet_btn")
                ) {
                    Text(
                        text = if (isAffordable) "ඔට්ටුව තබන්න (PLACE BET): ${uiState.betAmount.toInt()} K"
                               else "ප්‍රමාණවත් ශේෂයක් නැත (NO BALANCE)",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        GameState.FLYING -> {
            if (uiState.hasPlacedBetInCurrentRound) {
                if (uiState.hasCashedOutInCurrentRound) {
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "CASHED OUT: %,.1f K-Coins", uiState.userWinnings),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    val currentWinnings = uiState.betAmount * uiState.currentMultiplier
                    Button(
                        onClick = onCashOutClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFF97316),
                                        Color(0xFFF43F5E),
                                        Color(0xFFD946EF)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), RoundedCornerShape(24.dp))
                            .testTag("gam_cashout_btn")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "CASH OUT",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = String.format(Locale.US, "Winning: K %,.2f", currentWinnings),
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1F2C)),
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "ඊළඟ වටයට රැඳී සිටින්න (WAIT FOR NEXT ROUND)",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        GameState.CRASHED -> {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744).copy(alpha = alphaAnim)),
                enabled = false,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "කඩා වැටුනි! (CRASHED AT %.2fx) 💥", uiState.currentMultiplier),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun StatsAndDetailsSection(
    uiState: GameUiState,
    myBets: List<BetHistory>,
    onClaimDailyReward: () -> Unit,
    onDeposit: (Double) -> Unit,
    onWithdraw: (Double) -> Boolean,
    onClaimHost: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabsList = listOf("මගේ ඔට්ටු (My Bets)", "ක්‍රීඩකයින් (Live Live)", "මුදල් හා සේප්පුව (Vault & Wallet)", "නීති සහ තෑගි (Rules)")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF1E2130)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F101A))
    ) {
        Column {
            // Tab Header Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF161421),
                contentColor = Color.White
            ) {
                tabsList.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 10.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            // Tab Content Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(12.dp)
            ) {
                when (selectedTab) {
                    0 -> MyBetsTabContent(myBets = myBets)
                    1 -> LivePlayersListTab(liveBettors = uiState.liveBettors, currentMultiplier = uiState.currentMultiplier, gameState = uiState.gameState)
                    2 -> WalletAndHostTabContent(
                        uiState = uiState,
                        onDeposit = onDeposit,
                        onWithdraw = onWithdraw,
                        onClaimHost = onClaimHost
                    )
                    3 -> RulesAndGiftTabContent(uiState = uiState, onClaimDailyReward = onClaimDailyReward)
                }
            }
        }
    }
}

@Composable
fun MyBetsTabContent(myBets: List<BetHistory>) {
    if (myBets.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📭", fontSize = 42.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "තවම ඔබ ඔට්ටුවක් තබා නැත.",
                fontSize = 13.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                "ඔබේ ජයග්‍රහණ ඉතිහාසය මෙතැනින් පෙන්වයි.",
                fontSize = 11.sp,
                color = Color.DarkGray
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(myBets) { bet ->
                val won = bet.cashoutMultiplier != null
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(bet.timestamp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161826)),
                    border = BorderStroke(0.5.dp, if (won) Color(0x334CAF50) else Color(0x33D32F2F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (won) "🏆 WIN" else "💥 LOSS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (won) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                                )
                                Text(
                                    text = "  •  $timeStr",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = String.format(Locale.US, "ඔට්ටුව (Bet): %,.0f K-Coins", bet.betAmount),
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            if (won && bet.cashoutMultiplier != null) {
                                Text(
                                    text = String.format(Locale.US, "+%,.1f K", bet.winAmount),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                                Text(
                                    text = String.format(Locale.US, "ගත්තේ x%.2f", bet.cashoutMultiplier),
                                    fontSize = 10.sp,
                                    color = Color.LightGray
                                )
                            } else {
                                Text(
                                    text = String.format(Locale.US, "-%,.0f K", bet.betAmount),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF1744)
                                )
                                Text(
                                    text = String.format(Locale.US, "කැඩුනේ x%.2f", bet.crashMultiplier),
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LivePlayersListTab(liveBettors: List<LiveBettor>, currentMultiplier: Double, gameState: GameState) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("ක්‍රීඩකයා (Player)", fontSize = 10.sp, color = Color.Gray)
            Text("ඔට්ටුව (Bet)", fontSize = 10.sp, color = Color.Gray)
            Text("තත්ත්වය (Status)", fontSize = 10.sp, color = Color.Gray)
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(liveBettors) { b ->
                val stateColor = when (b.status) {
                    "WON" -> Color(0xFF4CAF50)
                    "LOST" -> Color(0xFFFF3D00).copy(alpha = 0.8f)
                    else -> Color(0xFFFFD700)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (b.status == "WON") Color(0x114CAF50)
                            else Color(0xFF131520)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(b.avatarEmoji, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = b.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "%,.0f", b.betAmount),
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(stateColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when (b.status) {
                                "WON" -> String.format(Locale.US, "x%.2f (+%,.0f)", b.cashoutMultiplier, b.winAmount)
                                "LOST" -> "CRASH 💥"
                                else -> "රැඳී සිටිනවා"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = stateColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RulesAndGiftTabContent(uiState: GameUiState, onClaimDailyReward: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Daily reward block
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1E2130))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = "🎁 දෛනික සල්ලි උල්පත ත්‍යාගය!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
                Text(
                    text = if (uiState.isDailyClaimAvailable) "නොමිලේ 1,000 K-Coins ගන්න"
                           else "ඊළඟ ත්‍යාගය ලබා ගැනීමට: ${uiState.nextDailyClaimCountdown}",
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Button(
                onClick = onClaimDailyReward,
                enabled = uiState.isDailyClaimAvailable,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("claim_reward_btn")
            ) {
                Text("ලබා ගන්න", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Game rules text guide
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF151722)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "ක්‍රීඩා කරන්නේ කෙසේද (How to Play):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1. සල්ලි උල්පත රොකට් එක පිටත්වීමට පෙර (තත්පර 6 ක කාලය තුල) ඔට්ටුවක් තබන්න.",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
                Text(
                    text = "2. රොකට්ටුව උඩ ගමන් කරන විට ගුණකය (Multiplier) ක්‍රමයෙන් වැඩි වෙයි.",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
                Text(
                    text = "3. රොකට්ටුව කඩා වැටීමට (💥 Crash) පෙර CASH OUT බොත්තම ඔබා ඔබේ මුදල් එකතු කරගන්න.",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
                Text(
                    text = "4. කඩා වැටුනහොත් ඔබට මුදල අහිමි වේ! උපරිම වාසනාව සහ පරිස්සම අවශ්‍ය වෙයි.",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun WalletAndHostTabContent(
    uiState: GameUiState,
    onDeposit: (Double) -> Unit,
    onWithdraw: (Double) -> Boolean,
    onClaimHost: () -> Unit
) {
    var amountText by remember { mutableStateOf("1000") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Balances Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Player Wallet
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13111E)),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("ඔබේ පසුම්බිය (Wallet)", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format(Locale.US, "K %,.2f", uiState.balance),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Host Vault (Owner's share!)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E142F)),
                border = BorderStroke(0.5.dp, Color(0xFFA855F7).copy(alpha = 0.2f)),
                modifier = Modifier.weight(1.2f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("හිමිකරු සේප්පුව (Vault)", fontSize = 10.sp, color = Color(0xFFA5B4FC), fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "K %,.2f", uiState.hostProfit),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFECC94B),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Transaction entry
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
            label = { Text("ගනුදෙනු මුදල (Amount K)", color = Color.LightGray, fontSize = 11.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                focusedBorderColor = Color(0xFF6366F1),
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedContainerColor = Color(0xFF181525),
                unfocusedContainerColor = Color(0xFF13111E)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Preset quick selections
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("100", "500", "1000", "5000", "10000").forEach { preset ->
                Button(
                    onClick = { amountText = preset },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF221F30)),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("+$preset", fontSize = 10.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Deposit
            Button(
                onClick = {
                    val valDouble = amountText.toDoubleOrNull() ?: 0.0
                    if (valDouble > 0.0) {
                        onDeposit(valDouble)
                    } else {
                        android.widget.Toast.makeText(context, "කරුණාකර නිවැරදි මුදලක් ඇතුලත් කරන්න", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("තැන්පත්කරන්න 📥", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            // Withdraw
            Button(
                onClick = {
                    val valDouble = amountText.toDoubleOrNull() ?: 0.0
                    if (valDouble > 0.0) {
                        val ok = onWithdraw(valDouble)
                        if (!ok) {
                            android.widget.Toast.makeText(context, "ප්‍රමාණවත් ශේෂයක් නොමැත!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        android.widget.Toast.makeText(context, "කරුණාකර නිවැරදි මුදලක් ඇතුලත් කරන්න", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("ආපසු ගන්න 📤", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Host Claim Earnings section ("මට එන්න විදිහක්")
        if (uiState.hostProfit > 0.0) {
            Button(
                onClick = onClaimHost,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF8B5CF6), Color(0xFFD946EF))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("👑 ", fontSize = 14.sp)
                    Text(
                        text = String.format(Locale.US, "මා වෙත ලාභය මාරු කරනු (+ K %,.1f)", uiState.hostProfit),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Live flowing public ledger
        Text(
            text = "ජාල ගනුදෙනු විස්තරය (Network Ledger Feed):",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF100E1C)),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            if (uiState.transactionHistory.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("ගනුදෙනු කිසිවක් නැත", fontSize = 11.sp, color = Color.DarkGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.transactionHistory) { log ->
                        Text(
                            text = log,
                            fontSize = 10.sp,
                            color = if (log.contains("ඔබ විසින්")) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                            fontWeight = if (log.contains("ඔබ විසින්")) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
