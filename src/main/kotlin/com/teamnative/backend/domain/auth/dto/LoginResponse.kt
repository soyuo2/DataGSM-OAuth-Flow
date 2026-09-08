package com.teamnative.backend.domain.auth.dto

data class LoginResponse(
    val accessToken: String,
    val tokenType: String,
    val username: String,
)
