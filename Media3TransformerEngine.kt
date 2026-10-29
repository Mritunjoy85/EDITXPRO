package com.editx.pro.engine

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Contrast
import androidx.media3.effect.Presentation
import androidx.media3.effect.RgbAdjustment
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.TransformationException
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import com.editx.pro.model.AspectRatio
import com.editx.pro.model.ExportResolution
import com.editx.pro.model.ExportSettings
import com.editx.pro.model.VideoProject
import kotlinx.coroutines.*
import java.io.File

@OptIn(UnstableApi::class)
class Media3TransformerEngine(private val context: Context) {

    interface ExportProgressListener {
        fun onProgress(progressPercent: Int)
        fun onCompleted(outputFile: File)
        fun onError(error: Throwable)
    }

    private var activeTransformer: Transformer? = null
    private var progressPollingJob: Job? = null

    fun exportProject(
        project: VideoProject,
        exportSettings: ExportSettings,
        outputFile: File,
        listener: ExportProgressListener
    ) {
        if (project.clips.isEmpty()) {
            listener.onError(IllegalArgumentException("Cannot export empty project"))
            return
        }

        try {
            val editedMediaItems = mutableListOf<EditedMediaItem>()

            for (clip in project.clips) {
                val mediaItemBuilder = MediaItem.Builder()
                    .setUri(Uri.parse(clip.uri))
                    .setClippingConfiguration(
                        MediaItem.ClippingConfiguration.Builder()
                            .setStartPositionMs(clip.startTrimMs)
                            .setEndPositionMs(clip.endTrimMs)
                            .build()
                    )

                val effectsList = mutableListOf<Effect>()

                if (clip.rotationDegrees != 0 || clip.isFlippedHorizontal || clip.isFlippedVertical) {
                    effectsList.add(
                        ScaleAndRotateTransformation.Builder()
                            .setRotationDegrees(clip.rotationDegrees.toFloat())
                            .setScale(if (clip.isFlippedHorizontal) -1f else 1f, if (clip.isFlippedVertical) -1f else 1f)
                            .build()
                    )
                }

                val targetAspect = when (project.aspectRatio) {
                    AspectRatio.RATIO_9_16 -> 9f / 16f
                    AspectRatio.RATIO_16_9 -> 16f / 9f
                    AspectRatio.RATIO_1_1 -> 1f
                    AspectRatio.RATIO_4_5 -> 4f / 5f
                    AspectRatio.RATIO_3_4 -> 3f / 4f
                }
                effectsList.add(Presentation.createForAspectRatio(targetAspect, Presentation.LAYOUT_SCALE_TO_FIT))

                val editedMediaItem = EditedMediaItem.Builder(mediaItemBuilder.build())
                    .setRemoveAudio(clip.isMuted)
                    .setEffects(androidx.media3.transformer.Effects(emptyList(), effectsList))
                    .build()

                editedMediaItems.add(editedMediaItem)
            }

            val sequence = EditedMediaItemSequence(editedMediaItems)
            val composition = Composition.Builder(listOf(sequence)).build()

            val transformationRequest = TransformationRequest.Builder()
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .build()

            val transformer = Transformer.Builder(context)
                .setTransformationRequest(transformationRequest)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(c: Composition, r: androidx.media3.transformer.ExportResult) {
                        progressPollingJob?.cancel()
                        listener.onProgress(100)
                        listener.onCompleted(outputFile)
                    }
                    override fun onError(c: Composition, r: androidx.media3.transformer.ExportResult, e: TransformationException) {
                        progressPollingJob?.cancel()
                        listener.onError(e)
                    }
                })
                .build()

            activeTransformer = transformer
            transformer.start(composition, outputFile.absolutePath)

            val progressHolder = ProgressHolder()
            progressPollingJob = CoroutineScope(Dispatchers.Main).launch {
                while (isActive) {
                    val status = transformer.getProgress(progressHolder)
                    if (status == Transformer.PROGRESS_STATE_AVAILABLE) {
                        listener.onProgress(progressHolder.progress)
                    }
                    delay(200)
                }
            }
        } catch (e: Exception) {
            listener.onError(e)
        }
    }

    fun cancelExport() {
        progressPollingJob?.cancel()
        activeTransformer?.cancel()
        activeTransformer = null
    }
}