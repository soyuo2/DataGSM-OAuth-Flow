package com.teamnative.backend.domain.sample.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class SampleController {

    @GetMapping("/public/ping")
    fun publicPing(): Map<String, String> = mapOf("message" to "public pong")

    @GetMapping("/private/ping")
    fun privatePing(): Map<String, String> = mapOf("message" to "private pong")
}
