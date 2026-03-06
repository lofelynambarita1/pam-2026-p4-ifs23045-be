package org.delcom.repositories

import org.delcom.entities.Drakor

interface IDrakorRepository {
    suspend fun getDrakors(search: String, genre: String, status: String): List<Drakor>
    suspend fun getDrakorById(id: String): Drakor?
    suspend fun getDrakorByJudul(judul: String): Drakor?
    suspend fun addDrakor(drakor: Drakor): String
    suspend fun updateDrakor(id: String, newDrakor: Drakor): Boolean
    suspend fun removeDrakor(id: String): Boolean
}
