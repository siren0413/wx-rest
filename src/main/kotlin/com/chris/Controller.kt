package com.chris

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest


@RestController
class LoginController {

    @Autowired
    lateinit var smsService: SmsService

    @CrossOrigin
    @RequestMapping("/sendcode", method = arrayOf(RequestMethod.POST))
    fun sendCode(@RequestBody smsInfo: SmsInfo, request: HttpServletRequest) {
        smsInfo.phoneNumber?:throw IllegalArgumentException("INVALID_PHONE_NUMBER")
        request.remoteAddr?:throw IllegalArgumentException("INVALID_IP_ADDRESS")
        smsService.sendSms(smsInfo)
    }
}