package com.teamnative.backend.global.exception

import java.time.Instant

data class ErrorResponse(
    val code: String,
    val message: String,
    val timestamp: Instant = Instant.now(),
)
