package com.walfud.cc.projectshare.model

import com.walfud.extention.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

const val THING_FAKE_ID = "00000000-0000-0000-0000-000000000000"
val THING_FAKE_TIME = LocalDateTime.MIN!!

const val THING_TYPE_TEXT = 0
const val THING_TYPE_FILE = 1

@Serializable
data class Thing(
    val id: String,

    val userIdRef: String,
    val type: Int,
    val content: String,

    @Serializable(with = LocalDateTimeSerializer::class)
    val createTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updateTime: LocalDateTime,
) {
    companion object {
        fun new(userId: String, type: Int, content: String): Thing = Thing(
            THING_FAKE_ID,
            userId,
            type,
            content,
            THING_FAKE_TIME,
            THING_FAKE_TIME,
        )
    }
}