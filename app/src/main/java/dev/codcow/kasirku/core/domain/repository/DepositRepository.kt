package dev.codcow.kasirku.core.domain.repository

import dev.codcow.kasirku.core.data.model.deposit.CreateDataDeposit
import dev.codcow.kasirku.core.data.model.deposit.DataDeposit
import dev.codcow.kasirku.core.data.model.deposit.DepositResponse
import dev.codcow.kasirku.core.data.model.deposit.DepositResponses
import dev.codcow.kasirku.core.data.model.deposit.TopUpData
import dev.codcow.kasirku.core.data.model.deposit.UpdateDataDeposit
import dev.codcow.kasirku.core.data.model.kategori.DataKategori
import retrofit2.Response

interface DepositRepository {
    suspend fun getAllDeposit(): Result<DepositResponses>
    suspend fun createDeposit(request: CreateDataDeposit): Result<DepositResponse>
    suspend fun getCustomerById(customerId: Int): Result<DepositResponse>
    suspend fun updateCustomerById(id: Int, updateDeposit: UpdateDataDeposit): Result<DepositResponse>
    suspend fun topUpByCustomerId(id: Int, topUpData: TopUpData): Result<DepositResponses>
    suspend fun deleteDeposit(id: Int): Result<Unit>
    suspend fun searchDeposit(query: String): Result<List<DataDeposit>>
}
