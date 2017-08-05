package com.chris

import org.slf4j.LoggerFactory
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

    // @CrossOrigin
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

@RestController
class UserController {
    @Autowired lateinit var userService: UserService

    @RequestMapping("/api/v1/user/profile/general", method = arrayOf(RequestMethod.POST))
    fun postProfileGeneral(@RequestBody userProfileGeneral: UserProfileGeneral) {
        userService.saveUserProfileGeneral(userProfileGeneral)
    }

    @RequestMapping("/api/v1/user/profile/identity", method = arrayOf(RequestMethod.POST))
    fun postProfileIdentity(@RequestBody userProfileIdentity: UserProfileIdentity) {
        userService.saveUserProfileIdentity(userProfileIdentity)
    }

    @RequestMapping("/api/v1/user/profile/general", method = arrayOf(RequestMethod.GET))
    fun getProfileGeneral(): UserProfileGeneral {
        return userService.getUserProfileGeneral(getSubject()!!)
    }

    @RequestMapping("/api/v1/user/profile/identity", method = arrayOf(RequestMethod.GET))
    fun getProfileIdentity(): UserProfileIdentity {
        return userService.getUserProfileIdentity(getSubject()!!)
    }

    @RequestMapping("/api/v1/user/profile/general/status", method = arrayOf(RequestMethod.GET))
    fun getProfileGeneralStatus(): UserProfileStatusResponse {
        return userService.getUserProfileGeneralStatus(getSubject()!!)
    }

    @RequestMapping("/api/v1/user/profile/identity/status", method = arrayOf(RequestMethod.GET))
    fun getProfileIdentityStatus(): UserProfileStatusResponse {
        return userService.getUserProfileIdentityStatus(getSubject()!!)
    }
}