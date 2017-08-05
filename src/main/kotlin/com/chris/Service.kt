package com.chris

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import java.util.*
import java.util.regex.Pattern
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.collections.ArrayList

@Service
class LoginService {

    @Autowired lateinit var repository: UserRepository

    val phonePattern: Pattern = Pattern.compile("^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$")
    val ipPattern: Pattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")

    fun sendCode(smsInfo: SmsInfo) {
        val phoneNumber = if (phonePattern.matcher(smsInfo.phoneNumber)?.matches() ?: false) smsInfo.phoneNumber else throw IllegalArgumentException()
        val ipAddress = if (ipPattern.matcher(smsInfo.ipAddress)?.matches() ?: false) smsInfo.ipAddress else throw IllegalArgumentException()

        println(smsInfo)
    }

    fun login(loginInfo: LoginInfo) {

        // TODO check if code is valid

        if (!repository.existsById(loginInfo.phoneNumber)) {
            val now = Date()
            val user = User(principle = loginInfo.phoneNumber!!, _dateCreated = now, _dateModified = now)
            repository.save(user)
        }
    }
}

@Service
class UserService {
    @Autowired lateinit var repository: UserRepository
    val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")

    fun getUser(): User {
        val user = try {
            repository.findById(getSubject()).get()
        } catch (e: NoSuchElementException) {
            throw IllegalStateException("user is not exists in authenticated api")
        }
        return user
    }

    fun saveUserProfileGeneral(userProfileGeneral: UserProfileGeneral) {
        val user = getUser()
        val now = Date()
        userProfileGeneral._dateCreated = user.userProfileGeneral?._dateCreated
        if (userProfileGeneral._dateCreated == null) {
            userProfileGeneral._dateCreated = now
        }
        userProfileGeneral._dateModified = now
        user.userProfileGeneral = userProfileGeneral
        repository.save(user)
    }

    fun saveUserProfileIdentity(userProfileIdentity: UserProfileIdentity) {
        val user = getUser()
        val now = Date()
        userProfileIdentity._dateCreated = user.userProfileIdentity?._dateCreated
        if (userProfileIdentity._dateCreated == null) {
            userProfileIdentity._dateCreated = now
        }

        userProfileIdentity._dateModified = now
        user.userProfileIdentity = userProfileIdentity
        repository.save(user)
    }

    fun getUserProfileGeneral(id: String): UserProfileGeneral {
        val user = getUser()
        user.userProfileGeneral?._dateCreated?.let {
            return user.userProfileGeneral!!
        }
        throw NotFoundException("user profile general not found")
    }

    fun getUserProfileIdentity(id: String): UserProfileIdentity {
        val user = getUser()
        user.userProfileIdentity?._dateCreated?.let {
            return user.userProfileIdentity!!
        }
        throw NotFoundException("user profile identity not found")
    }

    fun getUserProfileGeneralStatus(id: String): UserProfileStatusResponse {
        val user = getUser()
        val p = user.userProfileGeneral
        p?.let {
            if (!StringUtils.isEmpty(p.residentCity) &&
                    !StringUtils.isEmpty(p.residentAddress) &&
                    !StringUtils.isEmpty(p.residentTime) &&
                    !StringUtils.isEmpty(p.education) &&
                    !StringUtils.isEmpty(p.income) &&
                    !StringUtils.isEmpty(p.job) &&
                    !StringUtils.isEmpty(p.marriageStatus) &&
                    !StringUtils.isEmpty(p.qq)) {
                return UserProfileStatusResponse(0, "已完成")
            }
        }
        return UserProfileStatusResponse(1, "未完成")
    }

    fun getUserProfileIdentityStatus(id: String): UserProfileStatusResponse {
        val user = getUser()
        val p = user.userProfileIdentity
        p?.let {
            if (!StringUtils.isEmpty(p.name) &&
                    !StringUtils.isEmpty(p.idNumber)) {
                return UserProfileStatusResponse(0, "已完成")
            }
        }
        return UserProfileStatusResponse(1, "未完成")
    }

    fun getPasswordStatus(id: String): PasswordStatusResponse {
        val user = getUser()
        user.password?.let {
            return PasswordStatusResponse(0, "已设置")
        }
        return PasswordStatusResponse(1, "未设置")
    }

    fun savePassword(id: String, password: String) {
        val user = getUser()
        val salt = getRandomSalt()
        val spec = PBEKeySpec(password.toCharArray(), salt, 5, 256)
        val key = skf.generateSecret(spec)
        val res = key.encoded
        user.password = Base64.getEncoder().encodeToString(res)
        user.salt = Base64.getEncoder().encodeToString(salt)
        repository.save(user)
    }

    fun verifyPassword(id: String, password: String): Boolean {
        val user = getUser()
        val spec = PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(user.salt), 5, 256)
        val key = skf.generateSecret(spec)
        val res = key.encoded
        val input = Base64.getEncoder().encodeToString(res)
        if (input == user.password) {
            return true
        }
        return false
    }
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
            LoanConfig(500, 14) -> return ServiceFee(loanConfig, 60)
            LoanConfig(1000, 7) -> return ServiceFee(loanConfig, 60)
            LoanConfig(1000, 14) -> return ServiceFee(loanConfig, 120)
            LoanConfig(1500, 7) -> return ServiceFee(loanConfig, 120)
            LoanConfig(1500, 14) -> return ServiceFee(loanConfig, 240)
        }
        return ServiceFee(loanConfig, -1)
    }
}