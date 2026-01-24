package com.lovelybible.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

@Composable
actual fun rememberBackgroundImage(): ImageBitmap? {
    return remember {
        try {
            val resourceStream = Thread.currentThread().contextClassLoader
                ?.getResourceAsStream("background.png")
            resourceStream?.use { stream ->
                Image.makeFromEncoded(stream.readBytes()).toComposeImageBitmap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
