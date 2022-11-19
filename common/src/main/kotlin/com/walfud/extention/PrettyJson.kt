package com.walfud.extention

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.EmptySerializersModule

val PrettyJson: Json = Json {
    useAlternativeNames = true
    prettyPrint = true
}