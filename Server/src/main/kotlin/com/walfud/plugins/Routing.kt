package com.walfud.plugins

import com.walfud.routes.thingRoute
import com.walfud.routes.tokenRoute
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*

fun Application.configureRouting() {
    routing {
        tokenRoute()
        thingRoute()
    }
}
