package com.walfud.models

import com.walfud.cc.projectshare.model.Thing
import com.walfud.extention.parseLocalDateTimeFromSimpleFormat
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.util.concurrent.TimeUnit

val THING_LIFE_TIME = 7 * TimeUnit.DAYS.toSeconds(1)

object ThingTable : Table("thing") {
    val id = char("id", length = 36)

    val userIdRef = char("user_id_ref", length = 36)
    val type = integer("type")          // 0: 文字, 1: 文件
    val content = largeText("content")

    val createTime = char("create_time", length = 19)
    val updateTime = char("update_time", length = 19)
}

fun ResultRow.toThing(): Thing {
    val id = this[ThingTable.id]
    val userIdRef = this[ThingTable.userIdRef]
    val type = this[ThingTable.type]
    val content = this[ThingTable.content]
    val createTime = this[ThingTable.createTime]
    val updateTime = this[ThingTable.updateTime]

    return Thing(
        id,
        userIdRef,
        type,
        content,
        parseLocalDateTimeFromSimpleFormat(createTime),
        parseLocalDateTimeFromSimpleFormat(updateTime),
    )
}

fun InsertStatement<Number>.toThing(): Thing {
    val id = this[ThingTable.id]
    val userIdRef = this[ThingTable.userIdRef]
    val type = this[ThingTable.type]
    val content = this[ThingTable.content]
    val createTime = this[ThingTable.createTime]
    val updateTime = this[ThingTable.updateTime]

    return Thing(
        id,
        userIdRef,
        type,
        content,
        parseLocalDateTimeFromSimpleFormat(createTime),
        parseLocalDateTimeFromSimpleFormat(updateTime),
    )
}