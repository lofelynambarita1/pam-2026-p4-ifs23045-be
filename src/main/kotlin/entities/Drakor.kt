package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Drakor(
    var id: String = UUID.randomUUID().toString(),
    var judul: String,
    var pathPoster: String,
    var genre: String,
    var tahun: Int,
    var episode: Int,
    var rating: Double,
    var sinopsis: String,
    var status: String,   // Ongoing | Completed | Upcoming

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)
