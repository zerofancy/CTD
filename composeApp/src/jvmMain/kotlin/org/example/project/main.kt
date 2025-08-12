package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit
import org.drinkless.tdlib.example.Example

fun main() {
    FileKit.init("ctd")

    TDAPILoader.init()

    ClientManager().init()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "CTD",
        ) {
            App()
        }
    }
}