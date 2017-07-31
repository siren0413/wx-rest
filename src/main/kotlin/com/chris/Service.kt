package com.chris

import io.jsonwebtoken.*
import io.jsonwebtoken.impl.crypto.MacProvider
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

@Service
class LoginService {

    val phonePattern: Pattern = Pattern.compile("^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$")
    val ipPattern: Pattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")

    fun sendCode(smsInfo: SmsInfo) {
        val phoneNumber = if (phonePattern.matcher(smsInfo.phoneNumber)?.matches() ?: false) smsInfo.phoneNumber else throw IllegalArgumentException()
        val ipAddress = if (ipPattern.matcher(smsInfo.ipAddress)?.matches() ?: false) smsInfo.ipAddress else throw IllegalArgumentException()

        println(smsInfo)
    }
}

@Service
class UserService {

}

@Service
class LoanService {
    fun getLoanConfig(): List<LoanConfig> {
        val configs = ArrayList<LoanConfig>()
        configs.add(LoanConfig(500, 7))
        configs.add(LoanConfig(500, 14))
        configs.add(LoanConfig(1000, 7))
        configs.add(LoanConfig(1000, 14))
        configs.add(LoanConfig(1500, 7))
        configs.add(LoanConfig(1500, 14))
        return configs
    }

    fun computeServiceFee(loanConfig: LoanConfig): ServiceFee {
        when (loanConfig) {
            LoanConfig(500, 7) -> return ServiceFee(loanConfig, 30)
            LoanConfig(500, 14) ->  return ServiceFee(loanConfig, 60)
            LoanConfig(1000, 7) ->  return ServiceFee(loanConfig, 60)
            LoanConfig(1000, 14) ->  return ServiceFee(loanConfig, 120)
            LoanConfig(1500, 7) ->  return ServiceFee(loanConfig, 120)
            LoanConfig(1500, 14) ->  return ServiceFee(loanConfig, 240)
        }
        return ServiceFee(loanConfig, -1)
    }
}