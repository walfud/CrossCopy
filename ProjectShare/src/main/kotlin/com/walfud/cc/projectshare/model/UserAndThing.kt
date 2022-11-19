package com.walfud.cc.projectshare.model

import com.walfud.cc.projectshare.model.Thing
import com.walfud.extention.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class UserAndThing(
    val id: String,
    val things: List<Thing>,
)