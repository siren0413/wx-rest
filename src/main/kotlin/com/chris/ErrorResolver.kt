package com.chris

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.util.*
import javax.servlet.http.HttpServletResponse



data class ErrorResponse(val message: String? = "message not available", val timestamp: Date = Date())

@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {

//    @ExceptionHandler(IllegalArgumentException::class )
//    @ResponseBody
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    fun illegalArgumentExceptionHandler(ex: IllegalArgumentException): ErrorResponse {
//        return ErrorResponse(ex.message?:"message not available")
//    }

    @ExceptionHandler(SignatureException::class)
    fun handleIllegalArgumentException(ex: SignatureException, response: HttpServletResponse) {
        response.sendError(HttpStatus.FORBIDDEN.value(),"INVALID_TOKEN")
    }

    @ExceptionHandler(ExpiredJwtException::class)
    fun handleIllegalArgumentException(ex: ExpiredJwtException, response: HttpServletResponse) {
        response.sendError(HttpStatus.FORBIDDEN.value(), "TOKEN_EXPIRED")
    }

    @ExceptionHandler(MalformedJwtException::class)
    fun handleIllegalArgumentException(ex: MalformedJwtException, response: HttpServletResponse) {
        response.sendError(HttpStatus.FORBIDDEN.value())
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException, response: HttpServletResponse) {
        response.sendError(HttpStatus.BAD_REQUEST.value())
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException, response: HttpServletResponse) {
        response.sendError(HttpStatus.BAD_REQUEST.value())
    }
}
