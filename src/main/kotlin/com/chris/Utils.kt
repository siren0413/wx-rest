package com.chris

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import org.springframework.security.core.context.SecurityContextHolder
import java.security.SecureRandom

val RANDOM = SecureRandom()

fun getSubject(): String? {
    val principle = SecurityContextHolder.getContext().authentication.principal
    if (principle is JwtToken) {
        val jwsClaims: Jws<Claims> = Jwts.parser().setSigningKey(tokenSigningKey).parseClaimsJws(principle.accessToken)
        return jwsClaims.body.subject
    }
    return null
}

fun getRandomSalt(): ByteArray {
    val salt = ByteArray(16)
    RANDOM.nextBytes(salt)
    return salt
}