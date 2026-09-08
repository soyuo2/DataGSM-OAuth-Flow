package com.teamnative.backend.global.exception

import com.teamnative.backend.global.logging.AppLogDto
import com.teamnative.backend.global.logging.AppLogger
import com.teamnative.backend.global.logging.ErrorLogData
import com.teamnative.backend.global.logging.HttpLogData
import com.teamnative.backend.global.logging.HttpLoggingFilter
import com.teamnative.backend.global.logging.LogEvent
import com.teamnative.backend.global.logging.LogLevel
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler(
    private val appLogger: AppLogger,
) {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val message = exception.bindingResult.fieldErrors
            .firstOrNull()
            ?.defaultMessage
            ?: "Invalid request."

        logException(exception, HttpStatus.BAD_REQUEST, message, request)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(code = "VALIDATION_ERROR", message = message))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        exception: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val message = "Unexpected server error."
        logException(exception, HttpStatus.INTERNAL_SERVER_ERROR, message, request)

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(code = "INTERNAL_SERVER_ERROR", message = message))
    }

    private fun logException(
        exception: Exception,
        status: HttpStatus,
        message: String,
        request: HttpServletRequest,
    ) {
        val log = AppLogDto(
            level = if (status.is5xxServerError) LogLevel.ERROR else LogLevel.WARN,
            event = LogEvent.ERROR_OCCURRED,
            message = message,
            traceId = request.getAttribute(HttpLoggingFilter.TRACE_ID_ATTRIBUTE)?.toString(),
            http = HttpLogData(
                method = request.method,
                path = request.requestURI,
                query = request.queryString,
                status = status.value(),
                clientIp = request.getHeader("X-Forwarded-For")?.substringBefore(",")?.trim()?.takeIf { it.isNotBlank() }
                    ?: request.remoteAddr,
            ),
            error = ErrorLogData(
                type = exception::class.qualifiedName ?: exception::class.simpleName.orEmpty(),
                message = exception.message,
                status = status.value(),
            ),
        )

        if (status.is5xxServerError) {
            appLogger.error(log, exception)
        } else {
            appLogger.warn(log)
        }
    }
}
