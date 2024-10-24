package com.vk.itmo

import androidx.compose.ui.graphics.Color
import org.jetbrains.skia.*

fun buildBitmap(width: Int, height: Int, block: BitmapBuilder.() -> Unit = {}): Bitmap {
    val builder = BitmapBuilder(width, height)
    block(builder)
    return builder.build()
}

class BitmapBuilder(private val width: Int, private val height: Int) {
    private val bytes = ByteArray(width * height * ColorType.RGBA_8888.bytesPerPixel)

    operator fun set(index: Int, color: Color) {
        bytes[index * ColorType.RGBA_8888.bytesPerPixel + 0] = (color.red * 255).toInt().toByte()
        bytes[index * ColorType.RGBA_8888.bytesPerPixel + 1] = (color.green * 255).toInt().toByte()
        bytes[index * ColorType.RGBA_8888.bytesPerPixel + 2] = (color.blue * 255).toInt().toByte()
        bytes[index * ColorType.RGBA_8888.bytesPerPixel + 3] = (255).toByte()
    }

    operator fun set(x: Int, y: Int, color: Color) {
        require(x in 0 until width)
        require(y in 0 until height)

        val index = y * width + x

        set(index, color)
    }

    fun build(): Bitmap {
        val bitmap = Bitmap()
        val info = ImageInfo(
            colorInfo = ColorInfo(
                colorType = ColorType.RGBA_8888,
                alphaType = ColorAlphaType.PREMUL,
                colorSpace = ColorSpace.sRGB
            ),
            width = width,
            height = height
        )

        bitmap.allocPixels(info)
        bitmap.installPixels(bytes)

        return bitmap
    }
}