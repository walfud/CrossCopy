package com.walfud.cc.projectshare.model

import com.walfud.extention.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

const val CONFIG_VERSION = 1

@Serializable
data class CcConfig(
    val version: Int,
    val token: String,

    @Serializable(with = LocalDateTimeSerializer::class)
    val createTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updateTime: LocalDateTime,
)