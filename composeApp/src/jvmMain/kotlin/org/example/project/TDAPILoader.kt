package org.example.project

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import org.jetbrains.skiko.hostOs

object TDAPILoader {
    private val windowsLibraryNames = listOf(
        "zlib1.dll",
        "libcrypto-3-x64.dll",
        "libssl-3-x64.dll",
        "tdjni.dll"
    )

    fun init() {
        println("files is in ${FileKit.filesDir}")
        val libPath = FileKit.filesDir / "jni"
        ResourceReleaser.releaseResource("windows-x86_64", libPath.absolutePath())

        if (hostOs.isWindows) {
            windowsLibraryNames.forEach {
                System.load((libPath / it).absolutePath())
            }
        } else {
            TODO("Not implemented")
        }
    }
}