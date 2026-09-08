package com.teamnative.backend.domain.auth.controller

import com.teamnative.backend.global.config.DataGsmOAuthProperties
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient
import org.springframework.web.servlet.view.RedirectView
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

@RestController
@RequestMapping("/auth")
class DataGsmOAuthController(
    private val properties: DataGsmOAuthProperties,
    private val restClient: RestClient,
) {
    @GetMapping("/login")
    fun login(session: HttpSession): RedirectView {
        val verifier = randomUrlSafe(32)
        val state = randomUrlSafe(24)
        session.setAttribute(VERIFIER, verifier)
        session.setAttribute(STATE, state)

        val challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(
            MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray()),
        )
        val uri = "${properties.authorizationUri}?" + listOf(
            "client_id" to properties.clientId,
            "redirect_uri" to properties.redirectUri,
            "response_type" to "code",
            "state" to state,
            "code_challenge" to challenge,
            "code_challenge_method" to "S256",
        ).joinToString("&") { "${it.first}=${java.net.URLEncoder.encode(it.second, Charsets.UTF_8)}" }
        return RedirectView(uri)
    }

    @GetMapping("/callback")
    fun callback(request: HttpServletRequest, session: HttpSession): RedirectView {
        val params = request.parameterMap.mapValues { it.value.firstOrNull().orEmpty() }
        check(params["state"] == session.getAttribute(STATE)) { "Invalid OAuth state" }
        val verifier = session.getAttribute(VERIFIER) as? String ?: error("Missing PKCE verifier")
        val code = params["code"] ?: error("Missing authorization code")

        val body = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("code", code)
            add("client_id", properties.clientId)
            add("redirect_uri", properties.redirectUri)
            add("code_verifier", verifier)
        }
        val token = restClient.post().uri(properties.tokenUri)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("grant_type" to "authorization_code", "code" to code, "client_id" to properties.clientId, "redirect_uri" to properties.redirectUri, "code_verifier" to verifier))
            .retrieve().body(Map::class.java) ?: error("Empty token response")
        session.setAttribute(ACCESS_TOKEN, token["access_token"] as? String ?: error("Missing access token"))
        session.removeAttribute(VERIFIER)
        session.removeAttribute(STATE)
        return RedirectView("/")
    }

    @GetMapping("/logout")
    fun logout(request: HttpServletRequest): RedirectView {
        request.getSession(false)?.invalidate()
        return RedirectView("/")
    }

    @GetMapping("/me")
    fun me(session: HttpSession): Any = restClient.get().uri(properties.userInfoUri)
        .header("Authorization", "Bearer ${session.getAttribute(ACCESS_TOKEN) ?: error("Not authenticated")}")
        .retrieve().body(Any::class.java) ?: error("Empty user response")

    private fun randomUrlSafe(bytes: Int): String = Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(bytes).also(SecureRandom()::nextBytes))

    companion object { const val VERIFIER = "datagsm.oauth.verifier"; const val STATE = "datagsm.oauth.state"; const val ACCESS_TOKEN = "datagsm.oauth.access-token" }
}
