package com.walfud.cc.clientshare

import com.walfud.cc.projectshare.model.CONFIG_VERSION
import com.walfud.cc.projectshare.model.CcConfig
import com.walfud.extention.PrettyJson
import com.walfud.extention.isUuid
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.time.LocalDateTime

private const val CONFIG_FILENAME = ".cc.json"
lateinit var configFile: File

fun setupConfigDir(dir: String) {
    configFile = File(dir, CONFIG_FILENAME)
}

fun initializeConfigIfNeededSync() = runBlocking { initializeConfigIfNeeded() }
suspend fun initializeConfigIfNeeded(): CcConfig {
    val localConfig = getConfigFromLocal()
    return if (localConfig == null) {
        val newCcConfig = newConfigFromServer()
        configFile.writeText(PrettyJson.encodeToString(newCcConfig))

        newCcConfig
    } else {
        localConfig
    }
}

suspend fun newConfigFromServer(): CcConfig {
    val newToken = getTokenFromServer()
    val time = LocalDateTime.now()
    return CcConfig(CONFIG_VERSION, newToken, time, time)
}

fun getConfigFromLocal(): CcConfig? {
    return try {
        val config = configFile.readText()
        PrettyJson.decodeFromString(config)
    } catch (err: Exception) {
        null
    }
}

fun saveConfigToLocal(ccConfig: CcConfig) {
    configFile.writeText(PrettyJson.encodeToString(ccConfig))
}