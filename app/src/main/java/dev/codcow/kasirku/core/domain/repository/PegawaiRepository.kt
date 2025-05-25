package dev.codcow.kasirku.core.domain.repository

import dev.codcow.kasirku.core.data.model.pegawai.CreatePegawaiRequest
import dev.codcow.kasirku.core.data.model.pegawai.Data
import dev.codcow.kasirku.core.data.model.pegawai.PegawaiResponse
import dev.codcow.kasirku.core.data.model.pegawai.PegawaiResponses

interface PegawaiRepository {
    suspend fun getAllPegawai(): Result<List<Data>>
    suspend fun getPegawaiById(id: Int): Result<PegawaiResponse>
    suspend fun createPegawai(pegawai: CreatePegawaiRequest): Result<PegawaiResponses>
    suspend fun updatePegawai(id: Int, pegawai: Data): Result<PegawaiResponses>
    suspend fun deletePegawai(id: Int): Result<Unit>
    suspend fun searchPegawai(query: String): Result<List<Data>>
}