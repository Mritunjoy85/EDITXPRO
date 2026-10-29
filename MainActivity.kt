package com.editx.pro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.editx.pro.model.AspectRatio
import com.editx.pro.model.MediaClip
import com.editx.pro.model.VideoProject
import com.editx.pro.ui.screens.EditorScreen
import com.editx.pro.ui.theme.EditXProTheme

class MainActivity : ComponentActivity() {

    private val requiredPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.RECORD_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            )
        }

    private val permissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()

        val sampleProject = VideoProject(
            title = "My EditX Reel",
            aspectRatio = AspectRatio.RATIO_9_16,
            clips = listOf(
                MediaClip(
                    name = "Intro Scene.mp4",
                    uri = "android.resource://$packageName/raw/sample_intro",
                    sourceDurationMs = 4000L,
                    endTrimMs = 4000L
                ),
                MediaClip(
                    name = "Action Scene.mp4",
                    uri = "android.resource://$packageName/raw/sample_action",
                    sourceDurationMs = 6000L,
                    endTrimMs = 6000L
                )
            )
        )

        setContent {
            EditXProTheme {
                EditorScreen(initialProject = sampleProject)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            permissionRequestLauncher.launch(missing.toTypedArray())
        }
    }
}