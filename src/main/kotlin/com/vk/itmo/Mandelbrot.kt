package com.vk.itmo

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.skia.Bitmap
import kotlin.math.pow

fun mandelbrot(cx0: Double, cy0: Double, limit: Int): Int {
    var i = 0
    var xn = 0.0
    var yn = 0.0

    var xtemp: Double
    var ytemp: Double
    while (xn * xn + yn * yn < 4 && i < limit) {
        xtemp = xn * xn - yn * yn
        ytemp = 2 * xn * yn
        xn = xtemp + cx0
        yn = ytemp + cy0
        i++
    }

    return i
}

class MandelbrotMap(
    private val options: Options,
    private val buffer: IntArray
) {
    private val width: Int = options.xRes
    private val height: Int = options.yRes

    operator fun get(x: Int, y: Int): Int {
        require(x in 0 until width)
        require(y in 0 until height)
        return buffer[y * width + x]
    }

    fun asBitmap(colorMap: ColorMap): Bitmap = buildBitmap(width, height) {
        val min = buffer.minOrNull() ?: 0
        val max = buffer.maxOrNull()?.plus(1) ?: options.limit
        val colors = (0..(max - min)).map { colorMap[(it.toFloat() / (max - min)).pow(0.5f)] }.toTypedArray()

        buffer.forEachIndexed { index, value ->
            val color = when (value) {
                options.limit -> Color.Black
                else -> colors[value - min]
            }

            this[index] = color
        }
    }

    companion object {
        val UNIT: MandelbrotMap = MandelbrotMap(
            Options.UNIT, IntArray(1) { 0 }
        )

        suspend fun run(options: Options, parallel: Boolean) = coroutineScope {
            val buffer = IntArray(options.xRes * options.yRes) { 0 }

            (0 until options.yRes).forEach { y ->
                val block: () -> Unit = {
                    (0 until options.xRes).forEach { x ->
                        val cx0 = options.xMin + x * options.deltaX
                        val cy0 = options.yMin + y * options.deltaY
                        buffer[y * options.xRes + x] = mandelbrot(cx0, cy0, options.limit)
                    }
                }

                if (parallel) launch { block() }
                else block()
            }

            MandelbrotMap(options, buffer)
        }
    }

    data class Options(
        val xMin: Double,
        val xMax: Double,
        val xRes: Int,
        val yMin: Double,
        val yMax: Double,
        val yRes: Int,
        val limit: Int = 128
    ) {
        val deltaX = (xMax - xMin) / (xRes - 1)
        val deltaY = (yMax - yMin) / (yRes - 1)

        fun convertScreenCoordinates(x: Float, y: Float): Pair<Double, Double> {
            return Pair(
                xMin + (xMax - xMin) * x / xRes,
                yMin + (yMax - yMin) * y / yRes
            )
        }

        fun withResolution(resolution: Int): Options {
            require(resolution >= 1)
            return copy(
                xRes = xRes / resolution,
                yRes = yRes / resolution,
            )
        }

        companion object {
            val UNIT: Options = Options(
                xMin = 0.0,
                xMax = 1.0,
                yMin = 0.0,
                yMax = 1.0,
                xRes = 1,
                yRes = 1
            )

            fun fromViewport(viewport: Viewport, limit: Int) = with(viewport) {
                Options(
                    xMin = x - 0.5 * xScale,
                    xMax = x + 0.5 * xScale,
                    yMin = y - 0.5 * xScale * height / width,
                    yMax = y + 0.5 * xScale * height / width,
                    xRes = width,
                    yRes = height,
                    limit = limit
                )
            }
        }
    }
}