package com.chris

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.crypto.MacProvider
import org.springframework.stereotype.Service
import java.security.Key
import java.util.regex.Pattern

@Service
class LoginService {

    val phonePattern: Pattern = Pattern.compile("^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$")
    val ipPattern: Pattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")

    fun sendCode(smsInfo: SmsInfo) {
        val phoneNumber = if (phonePattern.matcher(smsInfo.phoneNumber)?.matches() ?: false) smsInfo.phoneNumber else throw IllegalArgumentException()
        val ipAddress = if (ipPattern.matcher(smsInfo.ipAddress)?.matches() ?: false) smsInfo.ipAddress else throw IllegalArgumentException()

        println(smsInfo)
    }

    fun auth(loginInfo: LoginInfo): String {
        // verify code

        // generate token
        val key = MacProvider.generateKey()
        return Jwts.builder()
                .setSubject("cn.loan")
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
    }
}