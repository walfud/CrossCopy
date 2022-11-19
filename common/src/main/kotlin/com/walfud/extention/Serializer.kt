package com.walfud.extention

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("java.time.LocalDateTime")

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toSimpleString())
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return parseLocalDateTimeFromSimpleFormat(decoder.decodeString())
    }
}