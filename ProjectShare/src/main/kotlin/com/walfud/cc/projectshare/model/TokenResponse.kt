package com.walfud.cc.projectshare.model

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val token: String
)