package com.walfud.cc.projectshare.model

import kotlinx.serialization.Serializable

@Serializable
data class ThingUploadTextRequest(
    val content: String,
)