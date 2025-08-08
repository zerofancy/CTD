package org.example.project

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

object ResourceReleaser {
    fun releaseResource(resourcePath: String, targetPath: String) {
        val originPath = resourcePath.let {
            if (it.endsWith("/")) {
                it
            } else {
                "$it/"
            }
        }
        // 读取dvdv文件
        javaClass.classLoader.getResourceAsStream("$originPath/checksum.dvdv")!!.use {
            val df = DocumentBuilderFactory.newInstance()
            val document = df.newDocumentBuilder().parse(it)
            val rootElement = document.documentElement
            val checksumNodes = rootElement.getElementsByTagName("checksum")
            var passCount = 0
            var failedCount = 0
            repeat(checksumNodes.length) {
                val node = checksumNodes.item(it)
                val path = node.attributes.getNamedItem("path").nodeValue
                val checksum = node.attributes.getNamedItem("checksum").nodeValue
                val algorithm = node.attributes.getNamedItem("algorithm").nodeValue
                println("path=${path}, checksum=${checksum}, algorithm=${algorithm}")
                if (algorithm.lowercase() == "md5") {
                    val targetFile = File(targetPath, path)
                    if (targetFile.canRead() && DigestUtils.getFileMD5(targetFile.canonicalPath) == checksum) {
                        println("$path [$algorithm]$checksum pass")
                        passCount++
                    } else {
                        println("$path [$algorithm]$checksum failed")
                        failedCount++
                        // extract file
                        targetFile.deleteRecursively()
                        targetFile.parentFile.mkdirs()
                        targetFile.createNewFile()
                        javaClass.classLoader.getResourceAsStream("$originPath/$path")!!.use { jarIs ->
                            targetFile.outputStream().use { fos ->
                                jarIs.transferTo(fos)
                            }
                        }
                    }
                } else {
                    //userLogger.warn("Unknown algorithm")
                    failedCount++
                }
            }
        }
    }
}