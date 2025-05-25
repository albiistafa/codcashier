package dev.codcow.kasirku.core.data.model.pegawai

data class CreatePegawaiRequest (
    val name: String,
    val role: String,
    val phone_number: String,
    val password: String
)