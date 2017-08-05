package com.chris

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat
import java.util.*
import javax.servlet.http.HttpServletRequest
import kotlin.collections.ArrayList


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
        return loanService.computeServiceFee(loanConfig)
    }

    @RequestMapping("/api/v1/loan/application/status/all")
    fun loanApplicationStatusList(): LoanApplicationStatusResponse {
        val statusList = ArrayList<LoanApplicationStatus>()
        val sdf = SimpleDateFormat("MM/dd HH:mm:ss")
        statusList.add(LoanApplicationStatus(0, "开始处理申请", "记录时间 ${sdf.format(Date())}"))
        statusList.add(LoanApplicationStatus(0, "信用记录审核", "记录时间 ${sdf.format(Date())}"))
        statusList.add(LoanApplicationStatus(0, "实名认证审核", "记录时间 ${sdf.format(Date())}"))
        statusList.add(LoanApplicationStatus(0, "个人信息审核", "记录时间 ${sdf.format(Date())}"))
        statusList.add(LoanApplicationStatus(0, "人工校验", "记录时间 ${sdf.format(Date())}"))
//        statusList.add(LoanApplicationStatus(1, "准备放款", "正在处理中，预计还需10分钟"))
        statusList.add(LoanApplicationStatus(2, "审核失败", "身份信息有误"))
//        return LoanApplicationStatusResponse(statusList)
        return LoanApplicationStatusResponse(ArrayList<LoanApplicationStatus>())
    }

    @RequestMapping("/api/v1/loan/credit/limit")
    fun currentCreditLimit():CreditLimit {
        return CreditLimit(500)
    }

    @RequestMapping("/api/v1/loan/credit/limit/increase")
    fun increaseCreditLimit():IncreaseCreditLimitResponse {
//        return IncreaseCreditLimitResponse(0, "提交成功", "我们会马上审核你的提额申请")
        return IncreaseCreditLimitResponse(1, "提交失败", "亲，请再积累一定的信用分后再尝试")
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