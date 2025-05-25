package dev.codcow.kasirku.core.domain.repository

import dev.codcow.kasirku.core.data.model.warung.Data
import dev.codcow.kasirku.core.data.model.warung.RequestWarung
import dev.codcow.kasirku.core.data.model.warung.WarungResponse

interface WarungRepository {

    suspend fun getWarung(): Result<Data>
    suspend fun postWarung(name: RequestWarung): Result<WarungResponse>

}