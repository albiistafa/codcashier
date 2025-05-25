package dev.codcow.kasirku.core.domain.repository

import dev.codcow.kasirku.core.data.model.kategori.CreateKategoriRequest
import dev.codcow.kasirku.core.data.model.kategori.DataKategori
import dev.codcow.kasirku.core.data.model.kategori.KategoriResponse
import dev.codcow.kasirku.core.data.model.kategori.KategoriResponses

interface KategoriRepository{
    suspend fun getAllKategori(): Result<List<DataKategori>>
    suspend fun getKategoriById(id: Int): Result<KategoriResponse>
    suspend fun createKategori(kategori: CreateKategoriRequest): Result<KategoriResponses>
    suspend fun updateKategori(id: Int, kategori: DataKategori): Result<KategoriResponses>
    suspend fun deleteKategori(id: Int): Result<Unit>
    suspend fun searchKategori(query: String): Result<List<DataKategori>>
}