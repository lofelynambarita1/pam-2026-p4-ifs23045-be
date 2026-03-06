package org.delcom.repositories

import org.delcom.dao.DrakorDAO
import org.delcom.entities.Drakor
import org.delcom.helpers.daoToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.DrakorTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class DrakorRepository : IDrakorRepository {

    override suspend fun getDrakors(search: String, genre: String, status: String): List<Drakor> =
        suspendTransaction {
            var query = DrakorTable.selectAll()

            if (search.isNotBlank()) {
                val keyword = "%${search.lowercase()}%"
                query = query.andWhere { DrakorTable.judul.lowerCase() like keyword }
            }
            if (genre.isNotBlank()) {
                query = query.andWhere { DrakorTable.genre.lowerCase() like "%${genre.lowercase()}%" }
            }
            if (status.isNotBlank()) {
                query = query.andWhere { DrakorTable.status eq status }
            }

            query.orderBy(DrakorTable.createdAt to SortOrder.DESC)
                .limit(20)
                .map { row ->
                    Drakor(
                        id         = row[DrakorTable.id].value.toString(),
                        judul      = row[DrakorTable.judul],
                        pathPoster = row[DrakorTable.pathPoster],
                        genre      = row[DrakorTable.genre],
                        tahun      = row[DrakorTable.tahun],
                        episode    = row[DrakorTable.episode],
                        rating     = row[DrakorTable.rating].toDouble(),
                        sinopsis   = row[DrakorTable.sinopsis],
                        status     = row[DrakorTable.status],
                        createdAt  = row[DrakorTable.createdAt],
                        updatedAt  = row[DrakorTable.updatedAt],
                    )
                }
        }

    override suspend fun getDrakorById(id: String): Drakor? = suspendTransaction {
        DrakorDAO
            .find { DrakorTable.id eq UUID.fromString(id) }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun getDrakorByJudul(judul: String): Drakor? = suspendTransaction {
        DrakorDAO
            .find { DrakorTable.judul eq judul }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun addDrakor(drakor: Drakor): String = suspendTransaction {
        val dao = DrakorDAO.new {
            judul      = drakor.judul
            pathPoster = drakor.pathPoster
            genre      = drakor.genre
            tahun      = drakor.tahun
            episode    = drakor.episode
            rating     = drakor.rating.toBigDecimal()
            sinopsis   = drakor.sinopsis
            status     = drakor.status
            createdAt  = drakor.createdAt
            updatedAt  = drakor.updatedAt
        }
        dao.id.value.toString()
    }

    override suspend fun updateDrakor(id: String, newDrakor: Drakor): Boolean = suspendTransaction {
        val dao = DrakorDAO
            .find { DrakorTable.id eq UUID.fromString(id) }
            .limit(1)
            .firstOrNull()

        if (dao != null) {
            dao.judul      = newDrakor.judul
            dao.pathPoster = newDrakor.pathPoster
            dao.genre      = newDrakor.genre
            dao.tahun      = newDrakor.tahun
            dao.episode    = newDrakor.episode
            dao.rating     = newDrakor.rating.toBigDecimal()
            dao.sinopsis   = newDrakor.sinopsis
            dao.status     = newDrakor.status
            dao.updatedAt  = newDrakor.updatedAt
            true
        } else {
            false
        }
    }

    override suspend fun removeDrakor(id: String): Boolean = suspendTransaction {
        val rowsDeleted = DrakorTable.deleteWhere {
            DrakorTable.id eq UUID.fromString(id)
        }
        rowsDeleted == 1
    }
}
