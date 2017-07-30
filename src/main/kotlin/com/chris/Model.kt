package com.chris

data class SmsInfo(val phoneNumber: String?, var ipAddress: String?)
data class LoginInfo(val phoneNumber: String?, val code: String?, val agreeTos: Boolean?, var ipAddress: String?)
data class JWToken(val token: String)