package com.chris

import org.springframework.stereotype.Service

@Service
class SmsService {
    fun sendSms(smsInfo: SmsInfo) {
        println(smsInfo)
    }
}