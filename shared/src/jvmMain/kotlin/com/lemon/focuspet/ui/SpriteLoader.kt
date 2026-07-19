package com.lemon.focuspet.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.lemon.focuspet.model.PetState
import org.jetbrains.skia.Image
import java.net.URL

actual object SpriteLoader {

    actual fun load(
        state: PetState,
        fps: Int,
        loop: Boolean,
        maxFrames: Int,
        maxSize: Int,
    ): SpriteSheet? {
        if (maxFrames <= 0) error("maxFrames must be > 0")

        val dirName = state.name.lowercase()
        val urls = listFrameUrls(dirName) ?: return null

        val selected = if (urls.size <= maxFrames) {
            urls
        } else {
            (0 until maxFrames).map { i ->
                val idx = (i.toDouble() * (urls.size - 1) / (maxFrames - 1)).toInt()
                urls[idx]
            }
        }

        val frames = selected.mapNotNull { url ->
            try {
                val bytes = url.readBytes()
                Image.makeFromEncoded(bytes).toComposeImageBitmap()
            } catch (_: Exception) { null }
        }

        if (frames.isEmpty()) return null
        return SpriteSheet(frames, fps, loop)
    }

    private fun listFrameUrls(dirName: String): List<URL>? {
        var firstUrl: URL? = null
        for (i in 1..9999) {
            val path = "/sprites/$dirName/frame_${"%04d".format(i)}.png"
            val url = SpriteLoader::class.java.getResource(path)
            if (url != null) {
                firstUrl = url
                break
            }
        }

        val base = firstUrl ?: return null

        return when (base.protocol) {
            "file" -> listFileUrls(base)
            "jar"  -> listJarUrls(base)
            else   -> null
        }
    }

    private fun listFileUrls(firstUrl: URL): List<URL>? {
        val dir = java.io.File(firstUrl.toURI()).parentFile ?: return null
        val files = dir.listFiles { f ->
            f.name.startsWith("frame_") && f.name.endsWith(".png")
        }?.sortedBy { f ->
            f.nameWithoutExtension.drop(6).toIntOrNull() ?: 0
        } ?: return null
        if (files.isEmpty()) return null
        return files.map { it.toURI().toURL() }
    }

    private fun listJarUrls(firstUrl: URL): List<URL>? {
        val conn = firstUrl.openConnection() as java.net.JarURLConnection
        val jarFile = conn.jarFile
        val jarUrlBase = conn.jarFileURL.toString()
        val entryPrefix = conn.entryName.substringBeforeLast("/") + "/"

        val matched = mutableListOf<URL>()
        val entries = jarFile.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            if (entry.name.startsWith(entryPrefix) && entry.name.endsWith(".png")) {
                matched.add(java.net.URI("jar:$jarUrlBase!/${entry.name}").toURL())
            }
        }
        jarFile.close()

        matched.sortBy { url ->
            val name = url.toString().substringAfterLast("/").removeSuffix(".png")
            name.removePrefix("frame_").toIntOrNull() ?: 0
        }
        return matched.ifEmpty { null }
    }
}
