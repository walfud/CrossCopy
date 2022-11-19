package com.walfud.plugins

import com.walfud.extention.isUuid
import com.walfud.cc.projectshare.model.Session
import io.ktor.server.sessions.*
import io.ktor.server.application.*

const val HEADER_SESSION_KEY = "CC-SESSION"

fun Application.configureSecurity() {
    install(Sessions) {
        header<Session>(HEADER_SESSION_KEY, object: SessionStorage {
            private val serializer = defaultSessionSerializer<Session>()

            override suspend fun invalidate(id: String) {
                println("invali")
            }

            override suspend fun read(id: String): String {
                if (!id.isUuid()) {
                    throw RuntimeException("`session.read`: id($id) not uuid")
                }
                if (Database.selectUserById(id) == null) {
                    throw RuntimeException("`session.read`: id($id) not in table")
                }

                return serializer.serialize(Session(id))
            }

            override suspend fun write(id: String, value: String) {
                Database.touchUser(id)
            }
        })
    }
}
