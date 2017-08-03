package com.chris

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.jsonwebtoken.*
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.*
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.WebAttributes
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

val objectMapper = jacksonObjectMapper()
val tokenIssuer = "cn.chris"
val tokenExpireSeconds = 1209600
val tokenSigningKey = "W2txU0U4Q2NlQmZ+eDdldlo5JlB7PCN1Uld7aEF+NmQkTl0/TkZKblRlXk5DPGFd" // [kqSE8CceBf~x7evZ9&P{<#uRW{hA~6d$N]?NFJnTe^NC<a]

val TOKEN_HEADER_PARAM = "Authorization"
val FORM_BASED_LOGIN_ENTRY_POINT = "/api/v1/auth"
val TOKEN_BASED_AUTH_ENTRY_POINT = "/**"
val TOKEN_REFRESH_ENTRY_POINT = "/api/v1/token"
val FORM_BASED_SENDCODE_ENTRY_POINT = "/api/v1/sendcode"
val STORE_FRONT_ENTRY_POINT = "/api/v1/loan/*"

@Configuration
@EnableWebSecurity
class WebSecurityConfig: WebSecurityConfigurerAdapter() {

    @Autowired lateinit var authenticationEntryPoint: RestAuthenticationEntryPoint
    @Autowired lateinit var successHandler: AuthenticationSuccessHandler
    @Autowired lateinit var failureHandler: AuthenticationFailureHandler
    @Autowired lateinit var ajaxAuthenticationProvider: AjaxAuthenticationProvider
    @Autowired lateinit var jwtAuthenticationProvider: JwtAuthenticationProvider
    @Autowired lateinit var tokenExtractor: JwtHeaderTokenExtractor
    @Autowired lateinit var authenticationManager: AuthenticationManager

    fun buildAjaxLoginProcessingFilter():AjaxLoginProcessingFilter {
        val filter = AjaxLoginProcessingFilter(FORM_BASED_LOGIN_ENTRY_POINT, this.successHandler, this.failureHandler)
        filter.setAuthenticationManager(authenticationManager)
        return filter
    }

    fun buildJwtTokenAuthenticationProcessingFilter(): JwtTokenAuthenticationProcessingFilter{
        val pathsToSkip = listOf<String>(TOKEN_REFRESH_ENTRY_POINT, FORM_BASED_LOGIN_ENTRY_POINT, FORM_BASED_SENDCODE_ENTRY_POINT, STORE_FRONT_ENTRY_POINT, "/")
        val matcher = SkipPathRequestMatcher(pathsToSkip, TOKEN_BASED_AUTH_ENTRY_POINT)
        val filter = JwtTokenAuthenticationProcessingFilter(failureHandler, tokenExtractor, matcher)
        filter.setAuthenticationManager(authenticationManager);
        return filter
    }

    @Bean
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

    @Autowired
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(ajaxAuthenticationProvider)
        auth.authenticationProvider(jwtAuthenticationProvider)
    }

//    override fun configure(web: WebSecurity) {
//        web.ignoring().antMatchers(TOKEN_REFRESH_ENTRY_POINT, FORM_BASED_LOGIN_ENTRY_POINT, FORM_BASED_SENDCODE_ENTRY_POINT, "/")
//    }

    override fun configure(http: HttpSecurity) {
        http.cors()
                .and()
                .csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)

                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .authorizeRequests()
                .antMatchers(FORM_BASED_LOGIN_ENTRY_POINT).permitAll()
                .antMatchers(TOKEN_REFRESH_ENTRY_POINT).permitAll()
                .antMatchers(FORM_BASED_SENDCODE_ENTRY_POINT).permitAll()
                .antMatchers(STORE_FRONT_ENTRY_POINT).permitAll()
                .antMatchers("/").permitAll()

                .and()
                .authorizeRequests()
                .antMatchers(TOKEN_BASED_AUTH_ENTRY_POINT).authenticated()

                .and()
                .addFilterBefore(buildAjaxLoginProcessingFilter(), UsernamePasswordAuthenticationFilter::class.java)
                .addFilterBefore(buildJwtTokenAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter::class.java)
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("*")
        configuration.allowedMethods = listOf("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH")
        configuration.allowCredentials = true;
        configuration.allowedHeaders = listOf("Authorization", "Cache-Control", "Content-Type");
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/api/v1/**", configuration);
        return source;
    }
}

// Filter
class AjaxLoginProcessingFilter(val defaultProcessUrl: String, val ajaxSuccessHandler: AuthenticationSuccessHandler, val ajaxFailureHandler: AuthenticationFailureHandler) : AbstractAuthenticationProcessingFilter(defaultProcessUrl) {

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse?): Authentication {

        val loginInfo = objectMapper.readValue<LoginInfo>(request?.reader, LoginInfo::class.java)

        loginInfo.phoneNumber ?: throw IllegalArgumentException("INVALID_PHONE_NUMBER")
        loginInfo.code ?: throw IllegalArgumentException("INVALID_SMS_CODE")
        loginInfo.agreeTos ?: throw IllegalArgumentException("INVALID_TOS")
        loginInfo.ipAddress = request?.remoteAddr ?: throw IllegalArgumentException("INVALID_IP_ADDRESS")

        val token = UsernamePasswordAuthenticationToken(loginInfo, loginInfo.code)
        return this.authenticationManager.authenticate(token)
    }

    override fun successfulAuthentication(request: HttpServletRequest?, response: HttpServletResponse?, chain: FilterChain?, authResult: Authentication?) {
        ajaxSuccessHandler.onAuthenticationSuccess(request, response, authResult)
    }

    override fun unsuccessfulAuthentication(request: HttpServletRequest?, response: HttpServletResponse?, failed: AuthenticationException?) {
        SecurityContextHolder.clearContext()
        ajaxFailureHandler.onAuthenticationFailure(request, response, failed)
    }
}

class JwtTokenAuthenticationProcessingFilter(val jwtFailureHandler: AuthenticationFailureHandler, val tokenExtractor: JwtHeaderTokenExtractor, val matcher: RequestMatcher) : AbstractAuthenticationProcessingFilter(matcher) {
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {

        val tokenPayload: String? = request.getHeader(TOKEN_HEADER_PARAM)
        val jwtToken = JwtToken(tokenExtractor.extract(tokenPayload))
        return authenticationManager.authenticate(JwtAuthenticationToken(jwtToken))
    }

    override fun successfulAuthentication(request: HttpServletRequest?, response: HttpServletResponse?, chain: FilterChain, authResult: Authentication?) {
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authResult
        SecurityContextHolder.setContext(context)
        chain.doFilter(request, response)
    }

    override fun unsuccessfulAuthentication(request: HttpServletRequest?, response: HttpServletResponse?, failed: AuthenticationException?) {
        SecurityContextHolder.clearContext()
        jwtFailureHandler.onAuthenticationFailure(request,response,failed)
    }
}


@Component
class AjaxAuthenticationProvider @Autowired constructor(val userService: UserService, val loginService: LoginService) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication?): Authentication {
        val loginInfo = authentication?.principal as LoginInfo
        val smsCode: String = authentication.credentials as String

        // TODO: validate phone number and sms code

        loginService.login(loginInfo)
        return UsernamePasswordAuthenticationToken(loginInfo.phoneNumber, smsCode, emptyList())
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}

@Component
class JwtAuthenticationProvider: AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val jwtToken = authentication.credentials as JwtToken
//        val jwsClaims: Jws<Claims> = Jwts.parser().setSigningKey(tokenSigningKey).parseClaimsJws(jwtToken.accessToken)
        try {
            Jwts.parser().setSigningKey(tokenSigningKey).parseClaimsJws(jwtToken.accessToken)
        } catch (e: Exception) {
            throw AuthenticationServiceException(e.message)
        }
        return JwtAuthenticationToken(jwtToken, emptyList())
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return JwtAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}

// handler
@Component
class AjaxAwareAuthenticationSuccessHandler(val tokenFactory: JwtTokenFactory) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication) {
        val accessToken = tokenFactory.createAccessJwtToken(authentication.principal as String)
        response.status = HttpStatus.OK.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        objectMapper.writeValue(response.writer, JwtToken(accessToken))
        clearAuthenticationAttributes(request)
    }

    fun clearAuthenticationAttributes(request: HttpServletRequest) {
        request.getSession(false)?.let {
            it.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION)
        }
    }
}

@Component
class AjaxAwareAuthenticationFailureHandler : AuthenticationFailureHandler {
    override fun onAuthenticationFailure(request: HttpServletRequest, response: HttpServletResponse, exception: AuthenticationException?) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        when (exception) {
            is BadCredentialsException -> objectMapper.writeValue(response.writer, ErrorResponse("Invalid code", Date()))
            is JwtException -> objectMapper.writeValue(response.writer, ErrorResponse("Invalid token", Date()))
            else -> objectMapper.writeValue(response.writer, ErrorResponse("Authentication failed", Date()))
        }
    }

}

class SkipPathRequestMatcher(val pathsToSkip: List<String>, processingPath: String): RequestMatcher {

    val matchers: OrRequestMatcher
    val processingMatcher: RequestMatcher

    init {
        val m = pathsToSkip.map { AntPathRequestMatcher(it) }.toList()
        this.matchers = OrRequestMatcher(m)
        this.processingMatcher = AntPathRequestMatcher(processingPath)
    }

    override fun matches(request: HttpServletRequest?): Boolean {
        if (matchers.matches(request)) {
            return false
        }
        return processingMatcher.matches(request)
    }

}

@Component
class RestAuthenticationEntryPoint: AuthenticationEntryPoint {
    override fun commence(request: HttpServletRequest?, response: HttpServletResponse?, authException: AuthenticationException?) {
        response?.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized")
    }
}

// utility
@Component
class JwtTokenFactory {
    fun createAccessJwtToken(principle: String): String {
        val currentTime: DateTime = DateTime.now()
        val token: String = Jwts.builder()
                .setSubject(principle)
                .setIssuer(tokenIssuer)
                .setIssuedAt(currentTime.toDate())
                .setExpiration(currentTime.plusSeconds(tokenExpireSeconds).toDate())
                .signWith(SignatureAlgorithm.HS512, tokenSigningKey)
                .compact()
        return token
    }
}

@Component
class JwtHeaderTokenExtractor {
    val HEADER_PREFIX = "Bearer "
    fun extract(header: String?): String? {
        if (StringUtils.isEmpty(header)) {
            throw AuthenticationServiceException("Authorization header cannot be empty")
        }
        return header?.substring(HEADER_PREFIX.length, header.length)
    }
}

class JwtAuthenticationToken : AbstractAuthenticationToken {
    val jwtToken: JwtToken

    constructor(jwtToken: JwtToken) : super(null) {
        this.jwtToken = jwtToken
        super.setAuthenticated(false)
    }

    constructor(jwtToken: JwtToken, authorities: Collection<GrantedAuthority>) : super(authorities) {
        this.jwtToken = jwtToken
        super.setAuthenticated(true)
    }

    override fun getPrincipal(): Any {
        return jwtToken
    }

    override fun getCredentials(): Any {
        return jwtToken
    }

}

