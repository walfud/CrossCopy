package com.walfud.routes

import com.walfud.cc.projectshare.model.TokenResponse
import com.walfud.cc.projectshare.model.Session
import com.walfud.plugins.Database
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.util.*

fun Route.tokenRoute() {
    route("/token") {
        post("/new") {
            lateinit var uuid: String
            do {
                uuid = UUID.randomUUID().toString()
                if (Database.insertUser(uuid)) {
                    break
                }
            } while (true)

            call.sessions.set(Session(uuid))
            call.respond(TokenResponse(uuid))
        }
    }
}