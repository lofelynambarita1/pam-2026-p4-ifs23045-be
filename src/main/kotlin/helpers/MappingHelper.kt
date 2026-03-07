package org.delcom.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.dao.DrakorDAO
import org.delcom.entities.Drakor
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun daoToModel(dao: DrakorDAO) = Drakor(
    id         = dao.id.value.toString(),
    judul      = dao.judul,
    pathPoster = dao.pathPoster,
    genre      = dao.genre,
    tahun      = dao.tahun,
    episode    = dao.episode,
    rating     = dao.rating.toDouble(),
    sinopsis   = dao.sinopsis,
    status     = dao.status,
    createdAt  = dao.createdAt,
    updatedAt  = dao.updatedAt,
)