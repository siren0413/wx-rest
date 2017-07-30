package com.chris

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

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException, response: HttpServletResponse) {
        response.sendError(HttpStatus.BAD_REQUEST.value())
    }
}
