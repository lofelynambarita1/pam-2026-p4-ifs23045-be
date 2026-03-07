package org.delcom

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.delcom.data.AppException
import org.delcom.data.ErrorResponse
import org.delcom.helpers.parseMessageToMap
import org.delcom.services.DrakorService
import org.delcom.services.ProfileService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val drakorService: DrakorService by inject()
    val profileService: ProfileService by inject()

    install(StatusPages) {
        // Tangkap AppException
        exception<AppException> { call, cause ->
            val dataMap: Map<String, List<String>> = parseMessageToMap(cause.message)

            call.respond(
                status = HttpStatusCode.fromValue(cause.code),
                message = ErrorResponse(
                    status = "fail",
                    message = if (dataMap.isEmpty()) cause.message else "Data yang dikirimkan tidak valid!",
                    data = if (dataMap.isEmpty()) null else dataMap.toString()
                )
            )
        }

        // Tangkap semua Throwable lainnya
        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.fromValue(500),
                message = ErrorResponse(
                    status = "error",
                    message = cause.message ?: "Unknown error",
                    data = ""
                )
            )
        }
    }

    routing {
        get("/") {
            call.respondText("API Drakor telah berjalan. Dibuat oleh [Lofelyn Enzely Ambarita].")
        }

        // Route Drakors
        route("/drakors") {
            get {
                drakorService.getAllDrakors(call)
            }
            post {
                drakorService.createDrakor(call)
            }
            get("/{id}") {
                drakorService.getDrakorById(call)
            }
            put("/{id}") {
                drakorService.updateDrakor(call)
            }
            delete("/{id}") {
                drakorService.deleteDrakor(call)
            }
            get("/{id}/poster") {
                drakorService.getDrakorPoster(call)
            }
        }

        // Route Profile
        route("/profile") {
            get {
                profileService.getProfile(call)
            }
            get("/photo") {
                profileService.getProfilePhoto(call)
            }
        }
    }
}