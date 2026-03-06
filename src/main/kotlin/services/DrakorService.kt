package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.DrakorRequest
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IDrakorRepository
import java.io.File
import java.util.UUID

class DrakorService(private val drakorRepository: IDrakorRepository) {

    // Mengambil semua data drakor
    suspend fun getAllDrakors(call: ApplicationCall) {
        val search = call.request.queryParameters["search"] ?: ""
        val genre  = call.request.queryParameters["genre"]  ?: ""
        val status = call.request.queryParameters["status"] ?: ""

        val drakors = drakorRepository.getDrakors(search, genre, status)

        call.respond(
            DataResponse(
                status  = "success",
                message = "Berhasil mengambil daftar drakor",
                data    = mapOf("drakors" to drakors)
            )
        )
    }

    // Mengambil data drakor berdasarkan id
    suspend fun getDrakorById(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID drakor tidak boleh kosong!")

        val drakor = drakorRepository.getDrakorById(id)
            ?: throw AppException(404, "Data drakor tidak tersedia!")

        call.respond(
            DataResponse(
                status  = "success",
                message = "Berhasil mengambil data drakor",
                data    = mapOf("drakor" to drakor)
            )
        )
    }

    // Ambil data request dari multipart form
    private suspend fun getDrakorRequest(call: ApplicationCall): DrakorRequest {
        val req = DrakorRequest()

        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "judul"    -> req.judul    = part.value.trim()
                        "genre"    -> req.genre    = part.value.trim()
                        "tahun"    -> req.tahun    = part.value.trim().toIntOrNull() ?: 0
                        "episode"  -> req.episode  = part.value.trim().toIntOrNull() ?: 0
                        "rating"   -> req.rating   = part.value.trim().toDoubleOrNull() ?: 0.0
                        "sinopsis" -> req.sinopsis  = part.value
                        "status"   -> req.status   = part.value.trim()
                    }
                }

                is PartData.FileItem -> {
                    val ext = part.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" }
                        ?: ""

                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/drakors/$fileName"

                    val file = File(filePath)
                    file.parentFile.mkdirs()

                    part.provider().copyAndClose(file.writeChannel())
                    req.pathPoster = filePath
                }

                else -> {}
            }
            part.dispose()
        }

        return req
    }

    // Validasi request
    private fun validateDrakorRequest(req: DrakorRequest) {
        val v = ValidatorHelper(req.toMap())
        v.required("judul",      "Judul tidak boleh kosong")
        v.required("genre",      "Genre tidak boleh kosong")
        v.required("sinopsis",   "Sinopsis tidak boleh kosong")
        v.required("pathPoster", "Poster tidak boleh kosong")
        v.minInt("tahun",   1990, "Tahun minimal 1990")
        v.minInt("episode",    1, "Jumlah episode minimal 1")
        v.maxDouble("rating", 10.0, "Rating maksimal 10.0")
        v.inList(
            "status",
            listOf("Ongoing", "Completed", "Upcoming"),
            "Status harus salah satu dari: Ongoing, Completed, Upcoming"
        )
        v.validate()

        val file = File(req.pathPoster)
        if (!file.exists()) {
            throw AppException(400, "Poster drakor gagal diupload!")
        }
    }

    // Menambahkan data drakor
    suspend fun createDrakor(call: ApplicationCall) {
        val req = getDrakorRequest(call)

        validateDrakorRequest(req)

        // Cek judul duplikat
        val exist = drakorRepository.getDrakorByJudul(req.judul)
        if (exist != null) {
            File(req.pathPoster).takeIf { it.exists() }?.delete()
            throw AppException(409, "Drakor dengan judul ini sudah terdaftar!")
        }

        val drakorId = drakorRepository.addDrakor(req.toEntity())

        call.respond(
            DataResponse(
                status  = "success",
                message = "Berhasil menambahkan data drakor",
                data    = mapOf("drakorId" to drakorId)
            )
        )
    }

    // Mengubah data drakor
    suspend fun updateDrakor(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID drakor tidak boleh kosong!")

        val oldDrakor = drakorRepository.getDrakorById(id)
            ?: throw AppException(404, "Data drakor tidak tersedia!")

        val req = getDrakorRequest(call)

        // Jika tidak upload poster baru, pakai yang lama
        if (req.pathPoster.isEmpty()) {
            req.pathPoster = oldDrakor.pathPoster
        }

        validateDrakorRequest(req)

        // Cek judul duplikat jika judul diubah
        if (req.judul != oldDrakor.judul) {
            val exist = drakorRepository.getDrakorByJudul(req.judul)
            if (exist != null) {
                File(req.pathPoster).takeIf { it.exists() }?.delete()
                throw AppException(409, "Drakor dengan judul ini sudah terdaftar!")
            }
        }

        // Hapus poster lama jika ada poster baru
        if (req.pathPoster != oldDrakor.pathPoster) {
            File(oldDrakor.pathPoster).takeIf { it.exists() }?.delete()
        }

        val isUpdated = drakorRepository.updateDrakor(id, req.toEntity())
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data drakor!")
        }

        call.respond(
            DataResponse<Nothing>(
                status  = "success",
                message = "Berhasil mengubah data drakor",
                data    = null
            )
        )
    }

    // Menghapus data drakor
    suspend fun deleteDrakor(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID drakor tidak boleh kosong!")

        val oldDrakor = drakorRepository.getDrakorById(id)
            ?: throw AppException(404, "Data drakor tidak tersedia!")

        val oldFile = File(oldDrakor.pathPoster)

        val isDeleted = drakorRepository.removeDrakor(id)
        if (!isDeleted) {
            throw AppException(400, "Gagal menghapus data drakor!")
        }

        if (oldFile.exists()) oldFile.delete()

        call.respond(
            DataResponse<Nothing>(
                status  = "success",
                message = "Berhasil menghapus data drakor",
                data    = null
            )
        )
    }

    // Mengambil poster drakor
    suspend fun getDrakorPoster(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: return call.respond(HttpStatusCode.BadRequest)

        val drakor = drakorRepository.getDrakorById(id)
            ?: return call.respond(HttpStatusCode.NotFound)

        val file = File(drakor.pathPoster)
        if (!file.exists()) {
            return call.respond(HttpStatusCode.NotFound)
        }

        call.respondFile(file)
    }
}
