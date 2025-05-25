package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.adbpurrytify.ui.components.MiniPlayer
import com.example.adbpurrytify.ui.theme.SpotifyGreen
import com.example.adbpurrytify.ui.viewmodels.TimeListenedData
import com.example.adbpurrytify.ui.viewmodels.TimeListenedViewModel
import com.example.adbpurrytify.ui.viewmodels.WeeklyListeningData

@Composable
fun TimeListenedScreen(
    navController: NavHostController,
    month: String,
    viewModel: TimeListenedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(month) {
        viewModel.loadTimeListenedData(month)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Custom Top Bar (positioned higher)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .statusBarsPadding(), // This pushes it to the very top
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "Time listened",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        when (val state = uiState) {
            is TimeListenedViewModel.TimeListenedUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SpotifyGreen)
                }
            }
            is TimeListenedViewModel.TimeListenedUiState.Success -> {
                TimeListenedContent(
                    data = state.data,
                    modifier = Modifier.weight(1f)
                )
            }
            is TimeListenedViewModel.TimeListenedUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        color = Color.White
                    )
                }
            }
        }

        MiniPlayer(navController = navController)
    }
}

@Composable
private fun TimeListenedContent(
    data: TimeListenedData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Month header
        Text(
            text = data.displayMonth,
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Main stats
        Text(
            text = "You listened to music for",
            color = Color.White,
            fontSize = 24.sp
        )
        Text(
            text = "${data.totalMinutes} minutes this month.",
            color = SpotifyGreen,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Show both daily and weekly averages
        Text(
            text = "Daily average: ${data.dailyAverage} min",
            color = Color.Gray,
            fontSize = 16.sp
        )
        Text(
            text = "Weekly average: ${data.weeklyAverage} min",
            color = Color.Gray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Weekly Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Weekly Chart",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                WeeklyListeningChart(
                    data = data.weeklyData,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun WeeklyListeningChart(
    data: List<WeeklyListeningData>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Reduced chart margins for more chart space
        val marginLeft = 50.dp.toPx() // Reduced from 80dp
        val marginRight = 16.dp.toPx()
        val marginTop = 20.dp.toPx()
        val marginBottom = 70.dp.toPx()

        // Chart area
        val chartWidth = canvasWidth - marginLeft - marginRight
        val chartHeight = canvasHeight - marginTop - marginBottom
        val chartLeft = marginLeft
        val chartTop = marginTop
        val chartRight = chartLeft + chartWidth
        val chartBottom = chartTop + chartHeight

        // Data processing
        val maxMinutes = data.maxOfOrNull { it.minutes } ?: 500
        val minMinutes = 0
        val maxWeeks = data.size

        // Draw background grid first (behind everything)
        val ySteps = 4 // Reduced steps for cleaner look
        val yStepValue = maxMinutes / ySteps
        for (i in 1 until ySteps) {
            val yValue = i * yStepValue
            val yPosition = chartBottom - (yValue.toFloat() / maxMinutes) * chartHeight
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(chartLeft, yPosition),
                end = Offset(chartRight, yPosition),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw main axes with rounded corners effect
        drawLine(
            color = Color.Gray.copy(alpha = 0.8f),
            start = Offset(chartLeft, chartBottom),
            end = Offset(chartRight, chartBottom),
            strokeWidth = 2.dp.toPx()
        )

        drawLine(
            color = Color.Gray.copy(alpha = 0.8f),
            start = Offset(chartLeft, chartTop),
            end = Offset(chartLeft, chartBottom),
            strokeWidth = 2.dp.toPx()
        )

        // Draw Y-axis labels with better formatting
        for (i in 0..ySteps) {
            val yValue = i * yStepValue
            val yPosition = chartBottom - (yValue.toFloat() / maxMinutes) * chartHeight

            // Y-axis labels with shorter format
            val labelText = when {
                yValue >= 1000 -> "${yValue / 1000}k"
                yValue >= 100 -> "${yValue}"
                else -> "${yValue}"
            }

            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    labelText,
                    chartLeft - 8.dp.toPx(), // Closer to axis
                    yPosition + 4.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 11.sp.toPx()
                        textAlign = android.graphics.Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                )
            }
        }

        // Draw X-axis labels with improved styling
        val xStepSize = chartWidth / maxWeeks

        data.forEachIndexed { index, weekData ->
            val xPosition = chartLeft + (index + 0.5f) * xStepSize

            // X-axis labels with better styling
            drawContext.canvas.nativeCanvas.apply {
                // Week number
                drawText(
                    "W${weekData.weekNumber}",
                    xPosition,
                    chartBottom + 18.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 12.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                )

                // Date range with smaller, lighter text
                drawText(
                    weekData.dateRange,
                    xPosition,
                    chartBottom + 32.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 9.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                )
            }
        }

        // Draw axis labels with better positioning
        drawContext.canvas.nativeCanvas.apply {
            // Y-axis label (rotated) - positioned closer
            save()
            rotate(-90f, 15.dp.toPx(), canvasHeight / 2)
            drawText(
                "Minutes",
                15.dp.toPx(),
                canvasHeight / 2 + 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 13.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                }
            )
            restore()

            // X-axis label
            drawText(
                "Week",
                canvasWidth / 2,
                canvasHeight - 8.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 13.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                }
            )
        }

        // Draw the styled bar chart
        data.forEachIndexed { index, weekData ->
            val barWidth = xStepSize * 0.7f // Slightly wider bars
            val barHeight = (weekData.minutes.toFloat() / maxMinutes) * chartHeight
            val xPosition = chartLeft + (index + 0.5f) * xStepSize
            val barLeft = xPosition - barWidth / 2
            val barTop = chartBottom - barHeight

            // Draw bar with gradient effect (simulate with multiple rectangles)
            val gradientSteps = 10
            for (i in 0 until gradientSteps) {
                val stepHeight = barHeight / gradientSteps
                val stepTop = barTop + (i * stepHeight)
                val alpha = 1.0f - (i * 0.1f) // Fade from top to bottom

                drawRect(
                    color = SpotifyGreen.copy(alpha = alpha),
                    topLeft = Offset(barLeft, stepTop),
                    size = androidx.compose.ui.geometry.Size(barWidth, stepHeight)
                )
            }

            // Draw bar outline for definition
            drawRect(
                color = SpotifyGreen,
                topLeft = Offset(barLeft, barTop),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )

            // Draw value on top of bar with background
            val valueText = "${weekData.minutes}"
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 11.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
                isAntiAlias = true
            }

            // Draw text background (rounded rectangle effect)
            val textBounds = android.graphics.Rect()
            textPaint.getTextBounds(valueText, 0, valueText.length, textBounds)
            val textWidth = textBounds.width()
            val textHeight = textBounds.height()

            drawRect(
                color = Color.Black.copy(alpha = 0.7f),
                topLeft = Offset(
                    xPosition - textWidth / 2 - 4.dp.toPx(),
                    barTop - textHeight - 8.dp.toPx()
                ),
                size = androidx.compose.ui.geometry.Size(
                    textWidth + 8.dp.toPx(),
                    textHeight + 4.dp.toPx()
                )
            )

            // Draw the text
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    valueText,
                    xPosition,
                    barTop - 6.dp.toPx(),
                    textPaint
                )
            }
        }

        // Add subtle shadow effect to bars
        data.forEachIndexed { index, weekData ->
            val barWidth = xStepSize * 0.7f
            val barHeight = (weekData.minutes.toFloat() / maxMinutes) * chartHeight
            val xPosition = chartLeft + (index + 0.5f) * xStepSize
            val barLeft = xPosition - barWidth / 2
            val barTop = chartBottom - barHeight

            // Shadow
            drawRect(
                color = Color.Black.copy(alpha = 0.2f),
                topLeft = Offset(barLeft + 2.dp.toPx(), barTop + 2.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}

