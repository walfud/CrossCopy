package com.walfud.routes

import com.walfud.cc.projectshare.model.*
import com.walfud.extention.md5
import com.walfud.plugins.Database
import com.walfud.plugins.tidy
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.io.File
import java.nio.file.Files
import java.time.LocalDateTime

fun Route.thingRoute() {
    route("/thing") {
        route("/upload") {
            post("/text") {
                val session = call.sessions.get<Session>() ?: throw RuntimeException("`/upload/text`: no session")

                val thingUploadTextRequest = call.receive<ThingUploadTextRequest>()
                val thing = Thing.new(
                    session.id,
                    THING_TYPE_TEXT,
                    thingUploadTextRequest.content,
                )
                val newThing = Database.insertThing(thing)

                call.respond(newThing)
            }

            post("/file") {
                val session = call.sessions.get<Session>() ?: throw RuntimeException("`/upload/file`: no session")

                // upload file
                val multiparts = call.receiveMultipart().readAllParts()
                val filePars = multiparts.filterIsInstance<PartData.FileItem>()
                val filePart = filePars.singleOrNull()
                    ?: throw RuntimeException("`/thing/upload/file`: no or more file part(${filePars.size})")
                val filename = filePart.name!!
                val storageFullPath = getStorageFile(session.id, filename)
                withContext(Dispatchers.IO) {
                    Files.createDirectories(storageFullPath.parentFile.toPath())
                    storageFullPath.writeBytes(filePart.streamProvider().readBytes())
                }
                multiparts.forEach { it.dispose() }

                // persist db
                val newThing = newSuspendedTransaction {
                    val oldRow = Database.doSelectThingByContent(
                        filename,
                        userId = session.id,
                    ).firstOrNull()
                    if (oldRow == null) {
                        Database.doInsertThing(
                            Thing.new(
                                session.id,
                                THING_TYPE_FILE,
                                filename,
                            )
                        )
                    } else {
                        Database.doUpdateThingById(oldRow.id, updateTime = LocalDateTime.now())!!
                    }
                }

                call.respond(newThing)
            }
        }

        post("/download/file") {
            val session = call.sessions.get<Session>() ?: throw RuntimeException("`/thing/upload/text`: no session")

            val thingDownloadFileRequest = call.receive<ThingDownloadFileRequest>()
            val filename = thingDownloadFileRequest.filename
            call.respondFile(getStorageFile(session.id, filename))
        }

        post("/list") {
            val session = call.sessions.get<Session>() ?: throw RuntimeException("`/thing/list`: no session")
            val things = newSuspendedTransaction { Database.selectThingByUserId(session.id) }
            call.respond(things)
        }

        post("/delete") {
            val session = call.sessions.get<Session>() ?: throw RuntimeException("`/thing/delete`: no session")
            val thingDeleteRequest = call.receive<ThingDeleteRequest>()
            val deletedThings = Database.deleteThingsById(thingDeleteRequest.ids, userId = session.id)
            call.respond(deletedThings)
        }

        post("/tidy") {
            val deletedUserAndThing = tidy()
            call.respond(deletedUserAndThing)
        }
    }
}

const val STORAGE_BASE_DIR = "./files/"
private fun getStorageUserDirectory(userId: String): File {
    return File(STORAGE_BASE_DIR, userId)
}

private fun getStorageFile(userId: String, filename: String): File {
    val dir = getStorageUserDirectory(userId)
    return File(dir, filename.md5())
}

fun getStorageFile(thing: Thing): File = getStorageFile(thing.userIdRef, thing.content)