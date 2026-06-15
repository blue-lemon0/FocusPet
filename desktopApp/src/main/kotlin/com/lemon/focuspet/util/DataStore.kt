package com.lemon.focuspet.util

import java.io.File

object DataStore {

    private val dataDir: File
        get() = File(System.getProperty("user.home"), ".focuspet")

    private val dataFile: File
        get() = File(dataDir, "data.json")

    fun load(): Int {
        return try {
            if (!dataFile.exists()) return 0
            val text = dataFile.readText()
            val regex = """"totalMinutes"\s*:\s*(\d+)""".toRegex()
            regex.find(text)?.groupValues?.get(1)?.toInt() ?: 0
        } catch (_: Exception) {
            0
        }
    }

    fun save(totalMinutes: Int) {
        try {
            dataDir.mkdirs()
            dataFile.writeText("""{"totalMinutes":$totalMinutes}""")
        } catch (_: Exception) {
            // silently ignore write errors
        }
    }
}
