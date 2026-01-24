package com.lovelybible.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

@Composable
expect fun rememberBackgroundImage(): ImageBitmap?
