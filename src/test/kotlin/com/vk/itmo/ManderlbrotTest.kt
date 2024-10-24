package com.vk.itmo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.test.Test

class ManderlbrotTest {
    @Test
    fun mandelbrotTest() = runTest {
        val outputFile = File("tmp/results.txt")
        outputFile.writeText("")

        val viewport = Viewport(
            width = 2400,
            height = 1544,
            x = -1.141,
            y = -0.2678,
            xScale = 0.1
        )
        val limits = 512
        val options = MandelbrotMap.Options.fromViewport(viewport, limits)

        val serialTime = executeSerialAndGetTime(options)
        val parallelTime = executeParallelAndGetTime(options)

        outputFile.appendText("Serial: $serialTime\n")
        outputFile.appendText("Parallel: $parallelTime\n")
        outputFile.appendText("--------\n")
    }

    private suspend fun executeSerialAndGetTime(options: MandelbrotMap.Options): Long {
        val startTime = System.currentTimeMillis()
        MandelbrotMap.run(options, false)
        val endTime = System.currentTimeMillis()

        return endTime - startTime
    }

    private suspend fun executeParallelAndGetTime(options: MandelbrotMap.Options): Long {
        val startTime = System.currentTimeMillis()

        withContext(Dispatchers.Default) {
            MandelbrotMap.run(options, true)
        }

        val endTime = System.currentTimeMillis()

        return endTime - startTime
    }
}
