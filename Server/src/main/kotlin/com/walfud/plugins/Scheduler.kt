package com.walfud.plugins

import com.walfud.cc.projectshare.model.THING_TYPE_FILE
import com.walfud.cc.projectshare.model.Thing
import com.walfud.cc.projectshare.model.UserAndThing
import com.walfud.routes.getStorageFile
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun Application.configureScheduler() {
    val executor = Executors.newScheduledThreadPool(1)
    executor.scheduleAtFixedRate(
        {
            runBlocking {
                tidy()
            }
        },
        TimeUnit.DAYS.toSeconds(1) - LocalTime.now().toSecondOfDay(),
        TimeUnit.DAYS.toSeconds(1),
        TimeUnit.SECONDS,
    )
}

suspend fun tidy(): List<UserAndThing> {
    val deletedThings = mutableListOf<Thing>()
    newSuspendedTransaction {
        deletedThings.addAll(Database.doTidyLongTimeThing())
        deletedThings.addAll(Database.doTidyThingWithoutUser())
    }

    // user: thing
    val deletedUserAndThing = mutableMapOf<String, MutableList<Thing>>()
    withContext(Dispatchers.IO) {
        deletedThings.forEach { thingToDel ->
            if (!deletedUserAndThing.containsKey(thingToDel.userIdRef)) {
                deletedUserAndThing[thingToDel.userIdRef] = mutableListOf()
            }
            deletedUserAndThing[thingToDel.userIdRef]!!.add(thingToDel)

            // delete real file
            if (thingToDel.type == THING_TYPE_FILE) {
                val storageFile = getStorageFile(thingToDel)
                storageFile.delete()
            }
        }
    }

    // res
    return deletedUserAndThing.map { kv ->
        val (userId, deletedThing) = kv
        UserAndThing(
            userId,
            deletedThing,
        )
    }.toList()
}