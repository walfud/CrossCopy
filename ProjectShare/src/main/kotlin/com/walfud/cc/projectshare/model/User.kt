package com.walfud.cc.projectshare.model

import com.walfud.extention.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Serializable
data class User(
    val id: String,

    @Serializable(with = LocalDateTimeSerializer::class)
    val createTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updateTime: LocalDateTime,
)