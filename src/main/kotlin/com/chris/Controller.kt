package com.chris

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest


@RestController
class LoginController {

    @Autowired
    lateinit var loginService: LoginService

    @CrossOrigin
    @RequestMapping("/sendcode", method = arrayOf(RequestMethod.POST))
    fun sendCode(@RequestBody smsInfo: SmsInfo, request: HttpServletRequest) {
        smsInfo.phoneNumber?:throw IllegalArgumentException("INVALID_PHONE_NUMBER")
        smsInfo.ipAddress = request.remoteAddr?:throw IllegalArgumentException("INVALID_IP_ADDRESS")
        loginService.sendCode(smsInfo)
    }

    @CrossOrigin
    @RequestMapping("/auth", method = arrayOf(RequestMethod.POST))
    fun auth(@RequestBody loginInfo: LoginInfo, request: HttpServletRequest):JWToken {
        loginInfo.phoneNumber?:throw IllegalArgumentException("INVALID_PHONE_NUMBER")
        loginInfo.code?:throw IllegalArgumentException("INVALID_SMS_CODE")
        loginInfo.agreeTos?:throw IllegalArgumentException("INVALID_TOS")
        loginInfo.ipAddress = request.remoteAddr?:throw IllegalArgumentException("INVALID_IP_ADDRESS")
        loginService.auth(loginInfo)?.let {
            return JWToken(it)
        }
    }

    @CrossOrigin
    @RequestMapping("/tokeninfo", method = arrayOf(RequestMethod.GET))
    fun tokeninfo(@RequestParam token: String, request: HttpServletRequest) {
        loginService.tokeninfo(token)
    }

}