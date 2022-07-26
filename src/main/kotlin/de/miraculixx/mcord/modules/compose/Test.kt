package de.miraculixx.mcord.modules.compose

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.ComposeScene
import de.miraculixx.mcord.utils.error
import org.jetbrains.skia.*

fun getImage(): ByteArray? {
    val scene = ComposeScene()
    scene.setContent {
        MaterialTheme {
            Text("Hello World!")
        }
    }

    val w = 100
    val h = 100
    val bitmap = Bitmap()
    if (!bitmap.setImageInfo(ImageInfo.makeS32(w, h, ColorAlphaType.PREMUL)))
        "Could not allocate the required resources for rendering the compose UI".error()
    scene.render(Canvas(bitmap), 1)
    return bitmap.readPixels()
}