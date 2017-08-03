package com.chris

import org.mongodb.morphia.annotations.Embedded
import org.mongodb.morphia.annotations.Entity
import org.mongodb.morphia.annotations.Id

data class SmsInfo(val phoneNumber: String?, var ipAddress: String?)
@Entity("users")
data class LoginInfo(@Id val phoneNumber: String?, val code: String?, val agreeTos: Boolean?, var ipAddress: String?)
data class JwtToken(val accessToken: String?)

data class LoanConfig(val amount: Int, val term: Int)
data class ServiceFee(val loanConfig: LoanConfig, val fee: Int)


@Entity("users")
data class User(@Id val principle: String, @Embedded val userProfileGeneral: UserProfileGeneral?=null,@Embedded val userProfileIdentity: UserProfileIdentity?=null)

@Embedded
data class UserProfileGeneral(val residentCity: String?, val residentAddress: String?, val residentTime: String?, val education: String?, val job: String?, val income: String?, val marriageStatus: String?, val qq: String?)
@Embedded
data class UserProfileIdentity(val name: String?, val idNumber: String?)
