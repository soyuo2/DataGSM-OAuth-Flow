package com.teamnative.backend.global.logging

object LogSanitizer {
    const val MASKED_VALUE = "***"

    val sensitiveKeys = listOf(
        "authorization",
        "password",
        "pwd",
        "token",
        "secret",
        "cookie"
    )
}
