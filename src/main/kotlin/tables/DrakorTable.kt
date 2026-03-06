package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object DrakorTable : UUIDTable("drakors") {
    val judul      = varchar("judul", 200)
    val pathPoster = varchar("path_poster", 255).default("")
    val genre      = varchar("genre", 100)
    val tahun      = integer("tahun")
    val episode    = integer("episode")
    val rating     = decimal("rating", 3, 1).default(0.0.toBigDecimal())
    val sinopsis   = text("sinopsis")
    val status     = varchar("status", 50).default("Ongoing")
    val createdAt  = timestamp("created_at")
    val updatedAt  = timestamp("updated_at")
}
