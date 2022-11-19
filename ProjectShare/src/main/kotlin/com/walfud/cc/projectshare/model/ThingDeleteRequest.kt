package com.walfud.cc.projectshare.model

import kotlinx.serialization.Serializable

@Serializable
data class ThingDeleteRequest(
    val ids: List<String>,
)