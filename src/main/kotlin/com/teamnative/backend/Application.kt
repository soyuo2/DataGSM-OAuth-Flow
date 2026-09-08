package com.teamnative.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import com.teamnative.backend.global.config.DataGsmOAuthProperties

@SpringBootApplication
@EnableConfigurationProperties(DataGsmOAuthProperties::class)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
