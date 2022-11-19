package com.walfud.plugins

import com.walfud.cc.projectshare.model.Thing
import com.walfud.cc.projectshare.model.User
import com.walfud.extention.toSimpleString
import com.walfud.models.*
import com.walfud.models.ThingTable.userIdRef
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.*

fun Application.configureDatabase() {
    val dbHost = System.getenv("DB_HOST")
    val dbTable = System.getenv("DB_TABLE")
    val dbUser = System.getenv("DB_USER")
    val dbPassword = System.getenv("DB_PASSWORD")

    val jdbcURL = "jdbc:mysql://$dbHost/$dbTable"
    val driverClassName = "com.mysql.cj.jdbc.Driver"
    TransactionManager.defaultDatabase = Database.connect(
        url = jdbcURL,
        driver = driverClassName,
        user = dbUser,
        password = dbPassword,
        databaseConfig = DatabaseConfig {
            // nothing
        }
    )
}

object Database {
    suspend fun insertUser(id: String): Boolean = newSuspendedTransaction { doInsertUser(id) }

    fun doInsertUser(id: String): Boolean {
        val res = UserTable.insert {
            it[this.id] = id

            val time = LocalDateTime.now().toSimpleString()
            it[this.createTime] = time
            it[this.updateTime] = time
        }

        return res.insertedCount == 1
    }

    suspend fun touchUser(id: String): Int = newSuspendedTransaction { doTouchUser(id) }
    fun doTouchUser(id: String): Int {
        return UserTable.update({ UserTable.id eq id }, limit = 1) {
            it[this.updateTime] = LocalDateTime.now().toSimpleString()
        }
    }

    suspend fun selectUserById(id: String): User? = newSuspendedTransaction { doSelectUserById(id) }
    fun doSelectUserById(id: String): User? {
        return UserTable.select {
            UserTable.id eq id
        }
            .firstOrNull()
            ?.toUser()
    }

    suspend fun selectUsers(): List<User> = newSuspendedTransaction { doSelectUsers() }
    fun doSelectUsers(): List<User> {
        return UserTable.selectAll().map { row -> row.toUser() }
    }


    suspend fun insertThing(thing: Thing): Thing = newSuspendedTransaction { doInsertThing(thing) }
    fun doInsertThing(thing: Thing): Thing {
        val res = ThingTable.insert {
            doSelectUserById(thing.userIdRef) ?: throw RuntimeException("`insertThing`: no user(${thing.userIdRef})")

            it[this.id] = newUuid(ThingTable)

            it[this.userIdRef] = thing.userIdRef
            it[this.type] = thing.type
            it[this.content] = thing.content

            val time = LocalDateTime.now().toSimpleString()
            it[this.createTime] = time
            it[this.updateTime] = time
        }
        return res.toThing()
    }

    suspend fun updateThingById(
        id: String,
        userIdRef: String? = null,
        type: Int? = null,
        content: String? = null,
        createTime: LocalDateTime? = null,
        updateTime: LocalDateTime? = null,
    ): Thing? = newSuspendedTransaction {
        doUpdateThingById(
            id,
            userIdRef = userIdRef,
            type = type,
            content = content,
            createTime = createTime,
            updateTime = updateTime
        )
    }

    fun doUpdateThingById(
        id: String,
        userIdRef: String? = null,
        type: Int? = null,
        content: String? = null,
        createTime: LocalDateTime? = null,
        updateTime: LocalDateTime? = null,
    ): Thing? {
        ThingTable.update({
            ThingTable.id eq id
        }) {
            if (userIdRef != null) {
                it[this.userIdRef] = userIdRef
            }
            if (type != null) {
                it[this.type] = type
            }
            if (content != null) {
                it[this.content] = content
            }
            if (createTime != null) {
                it[this.createTime] = createTime.toSimpleString()
            }
            if (updateTime != null) {
                it[this.updateTime] = updateTime.toSimpleString()
            }
        }
        return ThingTable.select {
            ThingTable.id eq id
        }
            .firstOrNull()
            ?.toThing()
    }

    suspend fun selectThingByUserId(userId: String): List<Thing> = newSuspendedTransaction { doSelectThingByUserId(userId) }
    fun doSelectThingByUserId(userId: String): List<Thing> {
        return ThingTable.select {
            userIdRef eq userId
        }
            .map { row -> row.toThing() }
    }

    suspend fun selectThingByContent(content: String, userId: String? = null): List<Thing> =
        newSuspendedTransaction { doSelectThingByContent(content, userId = userId) }

    fun doSelectThingByContent(content: String, userId: String? = null): List<Thing> {
        return ThingTable.select {
            var condition = Op.TRUE eq Op.TRUE
            if (userId != null) {
                condition = condition and (ThingTable.userIdRef eq userId)
            }
            condition and (ThingTable.content eq content)
        }
            .map { row -> row.toThing() }
    }

    suspend fun deleteThingsById(ids: List<String>, userId: String? = null): List<Thing?> = newSuspendedTransaction {
        val ret = mutableListOf<Thing?>()
        ids.forEach { id ->
            ret.add(doDeleteThingById(id))
        }
        ret
    }

    suspend fun deleteThingById(id: String, userId: String? = null): Thing? = newSuspendedTransaction { doDeleteThingById(id, userId) }
    fun doDeleteThingById(id: String, userId: String? = null): Thing? {
        val thing = ThingTable.select {
            var condition = Op.TRUE eq Op.TRUE
            if (userId != null) {
                condition = condition and (ThingTable.userIdRef eq userId)
            }
            condition and (ThingTable.id eq id)
        }
            .firstOrNull()
            ?.toThing()

        ThingTable.deleteWhere {
            var condition = Op.TRUE eq Op.TRUE
            if (userId != null) {
                condition = condition and (ThingTable.userIdRef eq userId)
            }
            condition and (ThingTable.id eq id)
        }

        return thing
    }

    suspend fun deleteThingByUserId(userId: String): List<Thing> = newSuspendedTransaction { doDeleteThingByUserId(userId) }
    fun doDeleteThingByUserId(userId: String): List<Thing> {
        val things = ThingTable.select {
            userIdRef eq userId
        }
            .map { row -> row.toThing() }

        return things
    }

    fun doTidyLongTimeThing(): List<Thing> {
        val thingToDelete = ThingTable.select {
            val minUpdateTime = LocalDateTime.now().minusSeconds(THING_LIFE_TIME).toSimpleString()
            ThingTable.updateTime less minUpdateTime
        }
            .map { row -> row.toThing() }

        ThingTable.deleteWhere {
            ThingTable.id inList thingToDelete.map { it.id }
        }

        return thingToDelete
    }

    fun doTidyThingWithoutUser(): List<Thing> {
        val thingToDelete = ThingTable.select {
            ThingTable.userIdRef notInSubQuery UserTable.slice(UserTable.id).selectAll()
        }
            .map { row -> row.toThing() }

        ThingTable.deleteWhere {
            ThingTable.id inList thingToDelete.map { it.id }
        }

        return thingToDelete
    }

    private fun newUuid(table: Table): String {
        lateinit var uuid: String
        do {
            uuid = UUID.randomUUID().toString()
            val res = table.select {
                val idCol = table.columns.find { col -> col.name == "id" } as Column<String>
                idCol eq uuid
            }
            if (res.count() == 0L) {
                break
            }
        } while (true)

        return uuid
    }
}