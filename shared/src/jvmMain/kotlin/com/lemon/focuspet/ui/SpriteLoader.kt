package com.lemon.focuspet.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.lemon.focuspet.model.PetState
import org.jetbrains.skia.Image
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.imageio.ImageIO

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
                decode(bytes, maxSize)
            } catch (_: Exception) { null }
        }

        if (frames.isEmpty()) return null
        return SpriteSheet(frames, fps, loop)
    }

    /**
     * 找到所有帧图片的 URL，不依赖循环扫描。
     * - 先找第一个存在的帧（最多几十次 getResource）
     * - file: 协议 → 直接列目录
     * - jar: 协议 → 用 JarFile 列出 JAR 内所有条目
     */
    private fun listFrameUrls(dirName: String): List<URL>? {
        // find first existing frame to determine protocol & location
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

        val entries = jarFile.entries()
        val matched = mutableListOf<URL>()
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

    private fun decode(bytes: ByteArray, maxSize: Int): ImageBitmap {
        val skiaImage = Image.makeFromEncoded(bytes)
        if (maxSize <= 0) return skiaImage.toComposeImageBitmap()

        val w = skiaImage.width
        val h = skiaImage.height
        val scale = minOf(1f, maxSize.toFloat() / maxOf(w, h))
        if (scale >= 1f) return skiaImage.toComposeImageBitmap()

        // Downscale with AWT bilinear
        val src = ImageIO.read(ByteArrayInputStream(bytes))
            ?: return skiaImage.toComposeImageBitmap()

        val dw = (w * scale).toInt()
        val dh = (h * scale).toInt()
        val dst = BufferedImage(dw, dh, BufferedImage.TYPE_INT_ARGB)
        val g = dst.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.drawImage(src, 0, 0, dw, dh, null)
        g.dispose()

        val out = ByteArrayOutputStream()
        ImageIO.write(dst, "PNG", out)
        return Image.makeFromEncoded(out.toByteArray()).toComposeImageBitmap()
    }
}
