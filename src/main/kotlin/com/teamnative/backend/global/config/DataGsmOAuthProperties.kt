package com.teamnative.backend.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.security.oauth2.datagsm")
data class DataGsmOAuthProperties(
    val clientId: String,
    val redirectUri: String,
    val authorizationUri: String,
    val tokenUri: String,
    val userInfoUri: String,
    val frontendBaseUrl: String,
)
