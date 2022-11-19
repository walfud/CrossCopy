package com.walfud.models

import com.walfud.cc.projectshare.model.User
import com.walfud.extention.parseLocalDateTimeFromSimpleFormat
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import java.time.LocalDateTime

const val USER_TABLE_FAKE_ID = "00000000-0000-0000-0000-000000000000"
val USER_TABLE_FAKE_TIME = LocalDateTime.MIN

object UserTable : Table("user") {
    val id = char("id", length = 36)

    val createTime = char("create_time", length = 19)
    val updateTime = char("update_time", length = 19)
}

fun ResultRow.toUser(): User {
    val id = this[UserTable.id]
    val createTime = this[UserTable.createTime]
    val updateTime = this[UserTable.updateTime]

    return User(
        id,
        parseLocalDateTimeFromSimpleFormat(createTime),
        parseLocalDateTimeFromSimpleFormat(updateTime),
    )
}