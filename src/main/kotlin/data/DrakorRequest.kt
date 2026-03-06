package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.Drakor

@Serializable
data class DrakorRequest(
    var judul: String = "",
    var genre: String = "",
    var tahun: Int = 0,
    var episode: Int = 0,
    var rating: Double = 0.0,
    var sinopsis: String = "",
    var status: String = "Ongoing",
    var pathPoster: String = "",
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "judul"      to judul,
            "genre"      to genre,
            "tahun"      to tahun,
            "episode"    to episode,
            "rating"     to rating,
            "sinopsis"   to sinopsis,
            "status"     to status,
            "pathPoster" to pathPoster,
        )
    }

    fun toEntity(): Drakor {
        return Drakor(
            judul      = judul,
            genre      = genre,
            tahun      = tahun,
            episode    = episode,
            rating     = rating,
            sinopsis   = sinopsis,
            status     = status,
            pathPoster = pathPoster,
        )
    }
}
