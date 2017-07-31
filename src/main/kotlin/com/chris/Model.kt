package com.chris

data class SmsInfo(val phoneNumber: String?, var ipAddress: String?)
data class LoginInfo(val phoneNumber: String?, val code: String?, val agreeTos: Boolean?, var ipAddress: String?)
data class JwtToken(val accessToken: String?)

data class LoanConfig(val amount:Int, val term: Int)
data class ServiceFee(val loanConfig: LoanConfig, val fee: Int)