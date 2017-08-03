package com.chris

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

data class SmsInfo(val phoneNumber: String?, var ipAddress: String?)
@Document(collection = "users")
data class LoginInfo(@Id val phoneNumber: String?, val code: String?, val agreeTos: Boolean?, var ipAddress: String?)
data class JwtToken(val accessToken: String?)

data class LoanConfig(val amount: Int, val term: Int)
data class ServiceFee(val loanConfig: LoanConfig, val fee: Int)


@Document(collection = "users")
data class User(@Id val principle: String, var userProfileGeneral: UserProfileGeneral?=null, var userProfileIdentity: UserProfileIdentity?=null)

data class UserProfileGeneral(val residentCity: String?, val residentAddress: String?, val residentTime: String?, val education: String?, val job: String?, val income: String?, val marriageStatus: String?, val qq: String?)

data class UserProfileIdentity(val name: String?, val idNumber: String?)
