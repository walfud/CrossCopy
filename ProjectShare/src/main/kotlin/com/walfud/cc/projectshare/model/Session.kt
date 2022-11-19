package com.walfud.cc.projectshare.model

import kotlinx.serialization.Serializable

@Serializable
data class Session(val id: String) {
    constructor() : this("")
}