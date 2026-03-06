package org.delcom.dao

import org.delcom.tables.DrakorTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class DrakorDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, DrakorDAO>(DrakorTable)

    var judul      by DrakorTable.judul
    var pathPoster by DrakorTable.pathPoster
    var genre      by DrakorTable.genre
    var tahun      by DrakorTable.tahun
    var episode    by DrakorTable.episode
    var rating     by DrakorTable.rating
    var sinopsis   by DrakorTable.sinopsis
    var status     by DrakorTable.status
    var createdAt  by DrakorTable.createdAt
    var updatedAt  by DrakorTable.updatedAt
}
