package dev.codcow.kasirku.core.domain.repository

import dev.codcow.kasirku.core.data.model.kategori.DataKategori
import dev.codcow.kasirku.core.data.model.subkategori.CreateSubkategoriRequest
import dev.codcow.kasirku.core.data.model.subkategori.DataSubkategori
import dev.codcow.kasirku.core.data.model.subkategori.SubkategoriResponse
import dev.codcow.kasirku.core.data.model.subkategori.SubkategoriResponses

interface SubKategoriRepository{
    suspend fun getAllSubKategori(): Result<List<DataSubkategori>>
    suspend fun getSubKategoriById(id: Int): Result<SubkategoriResponse>
    suspend fun createSubKategori(subkategori: CreateSubkategoriRequest): Result<SubkategoriResponse>
    suspend fun updateSubKategori(id: Int, subkategori: DataSubkategori): Result<SubkategoriResponse>
    suspend fun deleteSubKategori(id: Int): Result<Unit>
    suspend fun searchSub(query: String): Result<List<DataSubkategori>>
}