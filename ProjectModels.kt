package com.editx.pro.model

import java.util.UUID

enum class AspectRatio(val label: String, val widthRatio: Float, val heightRatio: Float) {
    RATIO_9_16("9:16", 9f, 16f),
    RATIO_16_9("16:9", 16f, 9f),
    RATIO_1_1("1:1", 1f, 1f),
    RATIO_4_5("4:5", 4f, 5f),
    RATIO_3_4("3:4", 3f, 4f)
}

data class Keyframe(
    val id: String = UUID.randomUUID().toString(),
    val timeMs: Long = 0L,
    val posX: Float = 0f,
    val posY: Float = 0f,
    val scale: Float = 1.0f,
    val opacity: Float = 1.0f,
    val rotationDegrees: Float = 0f,
    val easing: String = "EASE_IN_OUT"
)

data class MediaClip(
    val id: String = UUID.randomUUID().toString(),
    val uri: String,
    val name: String,
    val sourceDurationMs: Long = 5000L,
    val startTrimMs: Long = 0L,
    val endTrimMs: Long = 5000L,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val rotationDegrees: Int = 0,
    val isFlippedHorizontal: Boolean = false,
    val isFlippedVertical: Boolean = false,
    val keyframes: List<Keyframe> = emptyList()
) {
    val playbackDurationMs: Long
        get() = ((endTrimMs - startTrimMs) / speed).toLong().coerceAtLeast(100L)
}

data class VideoProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Project",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val aspectRatio: AspectRatio = AspectRatio.RATIO_9_16,
    val clips: List<MediaClip> = emptyList()
) {
    val totalDurationMs: Long
        get() = clips.sumOf { it.playbackDurationMs }
}