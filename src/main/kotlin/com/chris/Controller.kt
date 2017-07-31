package com.chris

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest


@RestController
class LoginController {

    @Autowired lateinit var loginService: LoginService

    @RequestMapping("/api/v1/sendcode", method = arrayOf(RequestMethod.POST))
    fun sendCode(@RequestBody smsInfo: SmsInfo, request: HttpServletRequest) {
        smsInfo.phoneNumber ?: throw IllegalArgumentException("INVALID_PHONE_NUMBER")
        smsInfo.ipAddress = request.remoteAddr ?: throw IllegalArgumentException("INVALID_IP_ADDRESS")
        loginService.sendCode(smsInfo)
    }

    @RequestMapping("/api/v1/tokeninfo", method = arrayOf(RequestMethod.GET))
    fun tokeninfo(request: HttpServletRequest) {
    }

    //    @CrossOrigin
    @RequestMapping("/api/v1/hello", method = arrayOf(RequestMethod.POST))
    fun hello(@RequestBody smsInfo: SmsInfo, request: HttpServletRequest) {
        println("h****************************")
    }
}

@RestController
class LoanController {

    @Autowired lateinit var loanService: LoanService

    @RequestMapping("/api/v1/loan/configs")
    fun loanConfig(): List<LoanConfig> {
        return loanService.getLoanConfig()
    }

    @RequestMapping("/api/v1/loan/servicefee")
    fun computeServiceFee(@RequestParam amount: Int, @RequestParam term: Int): ServiceFee {
        val loanConfig = LoanConfig(amount, term)
        println(loanConfig)
        return loanService.computeServiceFee(loanConfig)
    }
}