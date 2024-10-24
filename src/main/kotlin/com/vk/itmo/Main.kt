package com.vk.itmo

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

@Composable
fun App() {
    val scope = rememberCoroutineScope()
    val model = remember { MandelbrotViewerModel(scope) }

    MaterialTheme {
        MandelbrotView(model)
    }
}

fun main() = application {
    val windowState = rememberWindowState(
        position = WindowPosition.Aligned(
            alignment = Alignment.Center
        ),
        size = DpSize(1200.dp, 800.dp)
    )

    Window(
        title = "Lab 2",
        state = windowState,
        onCloseRequest = ::exitApplication
    ) {
        App()
    }
}
