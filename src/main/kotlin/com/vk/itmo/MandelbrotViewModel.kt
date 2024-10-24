package com.vk.itmo

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

class MandelbrotViewerModel(scope: CoroutineScope, dispatcher: CoroutineDispatcher = Dispatchers.Default) {
    private var currentCalculationJob: Job = Job()
    private var minResolution = 1

    private val viewPorts = MutableStateFlow(
        Viewport(
            width = 1,
            height = 1,
            x = -1.141,
            y = -0.2678,
            xScale = 0.1
        )
    )

    private val limits = MutableStateFlow(512)
    private val parallel = MutableStateFlow(true)
    private val colorMaps = MutableStateFlow(ColorMap.DEFAULT_COLOR_MAP)
    private val mandelbrots = MutableStateFlow(MandelbrotMap.UNIT)

    private val options = combine(viewPorts, limits) { viewPort: Viewport, limit: Int ->
        MandelbrotMap.Options.fromViewport(viewPort, limit)
    }

    val bitmaps = combine(mandelbrots, colorMaps) { mandelbrot, colorMap -> mandelbrot.asBitmap(colorMap) }

    init {
        scope.launch(dispatcher) {
            combine(options, parallel) { options: MandelbrotMap.Options, parallel: Boolean ->
                calculateMandelbrotMaps(options, parallel, resolutions = listOf(256, 64, 4, minResolution))
            }.collect()
        }
    }

    fun updateSize(size: IntSize) {
        this.viewPorts.update { it.copy(width = size.width, height = size.height) }
    }

    fun updatePosition(offset: Offset) {
        this.viewPorts.update { viewPort ->
            val (x, y) = MandelbrotMap.Options.fromViewport(viewPort, limits.value)
                .convertScreenCoordinates(offset.x, offset.y)
            viewPort.copy(x = x, y = y)
        }
    }

    fun zoom(amount: Float) {
        this.viewPorts.update { viewPort -> viewPort.copy(xScale = viewPort.xScale * amount) }
    }

    fun setParallel(parallel: Boolean) {
        this.parallel.update { parallel }
    }

    fun setMinResolution(resolution: Int) {
        minResolution = resolution
    }

    private suspend fun calculateMandelbrotMaps(
        options: MandelbrotMap.Options,
        parallel: Boolean,
        resolutions: List<Int>
    ) = coroutineScope {
        currentCalculationJob.cancel()

        currentCalculationJob = launch {
            resolutions.forEach { resolution ->
                mandelbrots.update {
                    MandelbrotMap.run(options.withResolution(resolution), parallel)
                }
            }
        }
    }
}