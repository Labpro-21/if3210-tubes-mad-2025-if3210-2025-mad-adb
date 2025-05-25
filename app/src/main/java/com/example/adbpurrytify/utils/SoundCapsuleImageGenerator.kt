package com.example.adbpurrytify.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.model.DayStreak
import com.example.adbpurrytify.data.model.SoundCapsule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.io.InputStream
import android.graphics.drawable.GradientDrawable

object SoundCapsuleImageGenerator {

    private const val CARD_WIDTH = 800
    private const val CARD_HEIGHT = 1200
    private const val PADDING = 48
    private const val CORNER_RADIUS = 24f
    private const val INNER_PADDING = 32

    suspend fun generateSoundCapsuleImage(
        context: Context,
        soundCapsule: SoundCapsule
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Draw enhanced background
            drawEnhancedSoundCapsuleBackground(canvas, context, soundCapsule)

            // Draw improved content
            drawEnhancedSoundCapsuleContent(canvas, context, soundCapsule)

            // Save to file
            saveBitmapToFile(context, bitmap, "sound_capsule_${System.currentTimeMillis()}.png")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun generateDayStreakImage(
        context: Context,
        dayStreak: DayStreak
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Draw enhanced background
            drawEnhancedDayStreakBackground(canvas, context, dayStreak)

            // Draw improved content
            drawEnhancedDayStreakContent(canvas, context, dayStreak)

            // Save to file
            saveBitmapToFile(context, bitmap, "day_streak_${System.currentTimeMillis()}.png")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun drawEnhancedSoundCapsuleBackground(
        canvas: Canvas,
        context: Context,
        soundCapsule: SoundCapsule
    ) = withContext(Dispatchers.IO) {
        val rect = RectF(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat())

        // Create sophisticated gradient background based on data availability
        val gradientColors = if (soundCapsule.hasData) {
            intArrayOf(
                Color.parseColor("#191414"), // Spotify dark
                Color.parseColor("#2A2A2A"), // Medium dark
                Color.parseColor("#121212"), // Deep black
                Color.parseColor("#000000")  // Pure black
            )
        } else {
            intArrayOf(
                Color.parseColor("#404040"), // Gray for no data
                Color.parseColor("#2A2A2A"), // Medium gray
                Color.parseColor("#1A1A1A"), // Dark gray
                Color.parseColor("#121212")  // Black
            )
        }

        val gradient = LinearGradient(
            0f, 0f, 0f, CARD_HEIGHT.toFloat(),
            gradientColors,
            floatArrayOf(0f, 0.3f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )

        val backgroundPaint = Paint().apply {
            shader = gradient
            isAntiAlias = true
        }

        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, backgroundPaint)

        // Add subtle noise texture
        drawNoiseTexture(canvas, rect)

        // Add gradient overlay for depth
        drawGradientOverlay(canvas, rect)
    }

    private suspend fun drawEnhancedDayStreakBackground(
        canvas: Canvas,
        context: Context,
        dayStreak: DayStreak
    ) = withContext(Dispatchers.IO) {
        val rect = RectF(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat())

        // Create modern gradient background
        val gradientColors = intArrayOf(
            Color.parseColor("#667eea"), // Purple-blue
            Color.parseColor("#764ba2"), // Purple
            Color.parseColor("#f093fb"), // Pink
            Color.parseColor("#f5576c")  // Red-pink
        )

        val gradient = LinearGradient(
            0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(),
            gradientColors,
            floatArrayOf(0f, 0.33f, 0.66f, 1f),
            Shader.TileMode.CLAMP
        )

        val backgroundPaint = Paint().apply {
            shader = gradient
            isAntiAlias = true
        }

        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, backgroundPaint)

        // Add decorative elements
        drawFloatingShapes(canvas, rect)
    }

    private fun drawNoiseTexture(canvas: Canvas, rect: RectF) {
        val noisePaint = Paint().apply {
            color = Color.WHITE
            alpha = 8
            isAntiAlias = true
        }

        // Add subtle noise dots
        for (i in 0..500) {
            val x = (Math.random() * rect.width()).toFloat()
            val y = (Math.random() * rect.height()).toFloat()
            canvas.drawCircle(x, y, 1f, noisePaint)
        }
    }

    private fun drawGradientOverlay(canvas: Canvas, rect: RectF) {
        // Add subtle vignette effect
        val centerX = rect.width() / 2
        val centerY = rect.height() / 2
        val radius = Math.max(rect.width(), rect.height()) * 0.8f

        val radialGradient = RadialGradient(
            centerX, centerY, radius,
            intArrayOf(Color.TRANSPARENT, Color.parseColor("#20000000")),
            floatArrayOf(0.3f, 1f),
            Shader.TileMode.CLAMP
        )

        val overlayPaint = Paint().apply {
            shader = radialGradient
            isAntiAlias = true
        }

        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, overlayPaint)
    }

    private fun drawFloatingShapes(canvas: Canvas, rect: RectF) {
        val shapePaint = Paint().apply {
            color = Color.WHITE
            alpha = 20
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        // Draw floating circles
        for (i in 0..12) {
            val x = (Math.random() * rect.width()).toFloat()
            val y = (Math.random() * rect.height()).toFloat()
            val radius = (Math.random() * 40 + 10).toFloat()
            canvas.drawCircle(x, y, radius, shapePaint)
        }
    }

    private suspend fun drawEnhancedSoundCapsuleContent(
        canvas: Canvas,
        context: Context,
        soundCapsule: SoundCapsule
    ) = withContext(Dispatchers.IO) {
        var yPos = PADDING.toFloat()

        // Header section
        yPos = drawModernHeader(canvas, soundCapsule, yPos)
        yPos += 60

        // Main title with enhanced styling
        yPos = drawEnhancedTitle(canvas, soundCapsule, yPos)
        yPos += 80

        // Featured album art (large) - using actual top song data
        yPos = drawFeaturedAlbumArt(canvas, context, soundCapsule, yPos)
        yPos += 60

        // Top artists and songs in cards - using real data
        yPos = drawContentCards(canvas, context, soundCapsule, yPos)
        yPos += 60

        // Statistics section - using real time data
        drawStatisticsSection(canvas, soundCapsule, yPos)
    }

    private suspend fun drawEnhancedDayStreakContent(
        canvas: Canvas,
        context: Context,
        dayStreak: DayStreak
    ) = withContext(Dispatchers.IO) {
        var yPos = PADDING.toFloat()

        // Header
        yPos = drawStreakHeader(canvas, dayStreak, yPos)
        yPos += 80

        // Streak title with better typography
        yPos = drawStreakTitle(canvas, dayStreak, yPos)
        yPos += 100

        // Large album art - using real streak data
        yPos = drawLargeAlbumArt(canvas, context, dayStreak, yPos)
        yPos += 60

        // Song info with enhanced styling - using real streak data
        drawEnhancedSongInfo(canvas, dayStreak, yPos)
    }

    private fun drawModernHeader(canvas: Canvas, soundCapsule: SoundCapsule, yPos: Float): Float {
        // App icon and name
        val iconPaint = Paint().apply {
            color = Color.parseColor("#1DB954")
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val whitePaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val grayPaint = Paint().apply {
            color = Color.parseColor("#B3B3B3")
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        // Draw Spotify-like icon
        canvas.drawText("♪", PADDING.toFloat(), yPos + 35, iconPaint)
        canvas.drawText(" Purrytify", PADDING + 40f, yPos + 35, whitePaint)

        // Date
        canvas.drawText(soundCapsule.displayMonth, CARD_WIDTH - PADDING.toFloat(), yPos + 35, grayPaint)

        return yPos + 60
    }

    private fun drawEnhancedTitle(canvas: Canvas, soundCapsule: SoundCapsule, yPos: Float): Float {
        val titlePaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(6f, 2f, 2f, Color.parseColor("#40000000"))
        }

        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#1DB954")
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            textSize = 56f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(6f, 2f, 2f, Color.parseColor("#40000000"))
        }

        val monthName = soundCapsule.displayMonth.split(" ")[0]
        canvas.drawText("My $monthName", PADDING.toFloat(), yPos + 50, titlePaint)
        canvas.drawText("Sound Capsule", PADDING.toFloat(), yPos + 115, subtitlePaint)

        // Add premium badge
        drawPremiumBadge(canvas, CARD_WIDTH - PADDING - 80f, yPos + 40)

        return yPos + 140
    }

    private fun drawPremiumBadge(canvas: Canvas, x: Float, y: Float) {
        val badgePaint = Paint().apply {
            color = Color.parseColor("#FFD700")
            isAntiAlias = true
            setShadowLayer(8f, 2f, 2f, Color.parseColor("#40000000"))
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val rect = RectF(x, y, x + 60, y + 30)
        canvas.drawRoundRect(rect, 15f, 15f, badgePaint)
        canvas.drawText("♪", x + 30, y + 22, textPaint)
    }

    private suspend fun drawFeaturedAlbumArt(
        canvas: Canvas,
        context: Context,
        soundCapsule: SoundCapsule,
        yPos: Float
    ): Float {
        val artSize = 200f
        val artX = (CARD_WIDTH - artSize) / 2

        // Get the top song's album art or fallback - using real data
        val imageUrl = soundCapsule.topSong?.imageUrl ?: ""
        val albumBitmap = loadImageFromUrl(context, imageUrl, artSize.toInt(), false)

        if (albumBitmap != null) {
            // Add shadow behind album art
            val shadowPaint = Paint().apply {
                color = Color.parseColor("#40000000")
                maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
            }

            val shadowRect = RectF(artX + 8, yPos + 8, artX + artSize + 8, yPos + artSize + 8)
            canvas.drawRoundRect(shadowRect, 16f, 16f, shadowPaint)

            canvas.drawBitmap(albumBitmap, artX, yPos, null)
        } else {
            drawFallbackAlbumArt(canvas, artX, yPos, artSize)
        }

        return yPos + artSize + 20
    }

    private fun drawFallbackAlbumArt(canvas: Canvas, x: Float, y: Float, size: Float) {
        val fallbackPaint = Paint().apply {
            color = Color.parseColor("#333333")
            isAntiAlias = true
        }

        val iconPaint = Paint().apply {
            color = Color.parseColor("#666666")
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = 60f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val rect = RectF(x, y, x + size, y + size)
        canvas.drawRoundRect(rect, 16f, 16f, fallbackPaint)
        canvas.drawText("♪", x + size/2, y + size/2 + 20, iconPaint)
    }

    private suspend fun drawContentCards(
        canvas: Canvas,
        context: Context,
        soundCapsule: SoundCapsule,
        yPos: Float
    ): Float {
        val cardHeight = 280f
        val cardWidth = (CARD_WIDTH - PADDING * 2 - 20) / 2f

        // Top Artists Card - using real data
        drawTopArtistsCard(canvas, context, soundCapsule, PADDING.toFloat(), yPos, cardWidth, cardHeight)

        // Top Songs Card - using real data
        drawTopSongsCard(canvas, context, soundCapsule, PADDING + cardWidth + 20, yPos, cardWidth, cardHeight)

        return yPos + cardHeight
    }

    private suspend fun drawTopArtistsCard(
        canvas: Canvas,
        context: Context,
        soundCapsule: SoundCapsule,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        // Card background
        val cardPaint = Paint().apply {
            color = Color.parseColor("#1A1A1A")
            isAntiAlias = true
            setShadowLayer(8f, 2f, 4f, Color.parseColor("#20000000"))
        }

        val cardRect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(cardRect, 16f, 16f, cardPaint)

        // Header
        val headerPaint = Paint().apply {
            color = Color.parseColor("#1DB954")
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawText("Top artists", x + INNER_PADDING, y + 40, headerPaint)

        // List items - using real data from soundCapsule
        val itemPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val numberPaint = Paint().apply {
            color = Color.parseColor("#B3B3B3")
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        if (soundCapsule.hasData && soundCapsule.topArtist != null) {
            // Display the top artist first
            val itemY = y + 70
            canvas.drawText("1", x + INNER_PADDING, itemY, numberPaint)
            canvas.drawText(truncateText(soundCapsule.topArtist.name, 15), x + INNER_PADDING + 30, itemY, itemPaint)

            // Show placeholder for other artists since we only have top artist data
            for (i in 2..5) {
                val placeholderY = y + 70 + ((i - 1) * 35)
                canvas.drawText("$i", x + INNER_PADDING, placeholderY, numberPaint)
                canvas.drawText("---", x + INNER_PADDING + 30, placeholderY, itemPaint)
            }
        } else {
            // No data available
            val noDataPaint = Paint().apply {
                color = Color.parseColor("#666666")
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            }
            canvas.drawText("No data yet", x + width/2, y + height/2, noDataPaint)
        }
    }

    private suspend fun drawTopSongsCard(
        canvas: Canvas,
        context: Context,
        soundCapsule: SoundCapsule,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        // Card background
        val cardPaint = Paint().apply {
            color = Color.parseColor("#1A1A1A")
            isAntiAlias = true
            setShadowLayer(8f, 2f, 4f, Color.parseColor("#20000000"))
        }

        val cardRect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(cardRect, 16f, 16f, cardPaint)

        // Header
        val headerPaint = Paint().apply {
            color = Color.parseColor("#1DB954")
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawText("Top songs", x + INNER_PADDING, y + 40, headerPaint)

        // List items - using real data from soundCapsule
        val itemPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val numberPaint = Paint().apply {
            color = Color.parseColor("#B3B3B3")
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        if (soundCapsule.hasData && soundCapsule.topSong != null) {
            // Display the top song first
            val itemY = y + 70
            canvas.drawText("1", x + INNER_PADDING, itemY, numberPaint)
            canvas.drawText(truncateText(soundCapsule.topSong.title, 12), x + INNER_PADDING + 30, itemY, itemPaint)

            // Show placeholder for other songs since we only have top song data
            for (i in 2..5) {
                val placeholderY = y + 70 + ((i - 1) * 35)
                canvas.drawText("$i", x + INNER_PADDING, placeholderY, numberPaint)
                canvas.drawText("---", x + INNER_PADDING + 30, placeholderY, itemPaint)
            }
        } else {
            // No data available
            val noDataPaint = Paint().apply {
                color = Color.parseColor("#666666")
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            }
            canvas.drawText("No data yet", x + width/2, y + height/2, noDataPaint)
        }
    }

    private fun drawStatisticsSection(canvas: Canvas, soundCapsule: SoundCapsule, yPos: Float) {
        // Time listened section with modern styling
        val sectionPaint = Paint().apply {
            color = Color.parseColor("#1A1A1A")
            isAntiAlias = true
            setShadowLayer(8f, 2f, 4f, Color.parseColor("#20000000"))
        }

        val sectionRect = RectF(PADDING.toFloat(), yPos, CARD_WIDTH - PADDING.toFloat(), yPos + 120)
        canvas.drawRoundRect(sectionRect, 16f, 16f, sectionPaint)

        val labelPaint = Paint().apply {
            color = Color.parseColor("#B3B3B3")
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val valuePaint = Paint().apply {
            color = Color.parseColor("#1DB954")
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawText("Time listened", PADDING + INNER_PADDING.toFloat(), yPos + 35, labelPaint)

        // Use real time listened data
        val timeText = if (soundCapsule.hasData) {
            "${soundCapsule.timeListened} minutes"
        } else {
            "0 minutes"
        }
        canvas.drawText(timeText, PADDING + INNER_PADDING.toFloat(), yPos + 80, valuePaint)
    }

    private fun drawStreakHeader(canvas: Canvas, dayStreak: DayStreak, yPos: Float): Float {
        val iconPaint = Paint().apply {
            color = Color.parseColor("#1DB954")
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val whitePaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val grayPaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
            textSize = 18f
        }

        canvas.drawText("♪", PADDING.toFloat(), yPos + 35, iconPaint)
        canvas.drawText(" Purrytify", PADDING + 40f, yPos + 35, whitePaint)
        canvas.drawText(dayStreak.dateRange, CARD_WIDTH - PADDING.toFloat(), yPos + 35, grayPaint)

        return yPos + 80
    }

    private fun drawStreakTitle(canvas: Canvas, dayStreak: DayStreak, yPos: Float): Float {
        val titlePaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(6f, 2f, 2f, Color.parseColor("#40000000"))
        }

        canvas.drawText("My ${dayStreak.streakDays}-day", CARD_WIDTH / 2f, yPos + 50, titlePaint)
        canvas.drawText("streak", CARD_WIDTH / 2f, yPos + 110, titlePaint)

        return yPos + 140
    }

    private suspend fun drawLargeAlbumArt(
        canvas: Canvas,
        context: Context,
        dayStreak: DayStreak,
        yPos: Float
    ): Float {
        val artSize = 320f
        val artX = (CARD_WIDTH - artSize) / 2

        // Use real image URL from dayStreak
        val albumBitmap = loadImageFromUrl(context, dayStreak.imageUrl, artSize.toInt(), false)

        if (albumBitmap != null) {
            // Enhanced shadow
            val shadowPaint = Paint().apply {
                color = Color.parseColor("#60000000")
                maskFilter = BlurMaskFilter(25f, BlurMaskFilter.Blur.NORMAL)
            }

            val shadowRect = RectF(artX + 12, yPos + 12, artX + artSize + 12, yPos + artSize + 12)
            canvas.drawRoundRect(shadowRect, 20f, 20f, shadowPaint)

            canvas.drawBitmap(albumBitmap, artX, yPos, null)
        } else {
            drawFallbackAlbumArt(canvas, artX, yPos, artSize)
        }

        return yPos + artSize + 40
    }

    private fun drawEnhancedSongInfo(canvas: Canvas, dayStreak: DayStreak, yPos: Float) {
        val artistPaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            setShadowLayer(4f, 1f, 1f, Color.parseColor("#40000000"))
        }

        val songPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(4f, 1f, 1f, Color.parseColor("#40000000"))
        }

        // Use real artist and song data from dayStreak
        canvas.drawText(truncateText(dayStreak.artist, 20), CARD_WIDTH / 2f, yPos, artistPaint)
        canvas.drawText(truncateText(dayStreak.songTitle, 18), CARD_WIDTH / 2f, yPos + 50, songPaint)
    }

    // Keep all the existing utility methods (loadImageFromUrl, createCircularBitmap, etc.)
    private suspend fun loadImageFromUrl(
        context: Context,
        imageUrl: String,
        size: Int,
        isCircular: Boolean
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            if (imageUrl.isEmpty() || !imageUrl.startsWith("http")) {
                return@withContext null
            }

            val url = URL(imageUrl)
            val inputStream: InputStream = url.openConnection().getInputStream()
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap != null) {
                val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, size, size, true)

                if (isCircular) {
                    createCircularBitmap(scaledBitmap)
                } else {
                    getRoundedCornerBitmap(scaledBitmap, 16f)
                }
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint().apply {
            isAntiAlias = true
        }

        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return output
    }

    private fun getRoundedCornerBitmap(bitmap: Bitmap, cornerRadius: Float): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint().apply {
            isAntiAlias = true
        }

        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    private fun truncateText(text: String, maxLength: Int): String {
        return if (text.length > maxLength) {
            text.take(maxLength - 3) + "..."
        } else {
            text
        }
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        return try {
            val cacheDir = File(context.cacheDir, "shared_images")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val file = File(cacheDir, fileName)
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }
}