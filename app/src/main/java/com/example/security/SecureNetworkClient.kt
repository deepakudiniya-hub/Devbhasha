package com.example.security

import com.example.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Production-Grade Secure Network Client enforcing military-grade security.
 * Features:
 * 1. Strict SSL / Certificate Pinning against MitM attacks.
 * 2. Mandatory HTTPS enforcement on all outbound requests (throws SecurityException on cleartext HTTP).
 * 3. Automatic Bearer Authentication token injection via Firebase Auth.
 * 4. Standard defensive security headers (X-Content-Type-Options, X-Frame-Options, X-XSS-Protection, X-App-Platform, Version).
 * 5. Production log stripping: Logging is completely encapsulated within BuildConfig.DEBUG.
 */
object SecureNetworkClient {

    private const val CONNECT_TIMEOUT_SEC = 15L
    private const val READ_TIMEOUT_SEC = 20L
    private const val WRITE_TIMEOUT_SEC = 20L

    /**
     * Production Certificate Pinning configuration mapped explicitly to production domains.
     */
    val certificatePinner: CertificatePinner = CertificatePinner.Builder()
        .add("*.googleapis.com", "sha256/hxqRlPTuQEKatdogHEZmVqCz3Upph10L015Jo/pFoxY=")
        .add("*.googleapis.com", "sha256/Vfd95BwDeSQooUBguRmnjfSvkIpETtuUbzbukBlDLZs=")
        .add("*.cloudfunctions.net", "sha256/hxqRlPTuQEKatdogHEZmVqCz3Upph10L015Jo/pFoxY=")
        .add("*.cloudfunctions.net", "sha256/Vfd95BwDeSQooUBguRmnjfSvkIpETtuUbzbukBlDLZs=")
        .add("*.firebaseio.com", "sha256/hxqRlPTuQEKatdogHEZmVqCz3Upph10L015Jo/pFoxY=")
        .add("*.razorpay.com", "sha256/kIdp6NNEd8wsugYyyIYFsi1ylmcED3hZbSRMa9gqkgU=")
        .add("*.razorpay.com", "sha256/WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18=")
        .build()

    /**
     * Security & Auth Interceptor:
     * - Intercepts all outbound requests and checks URL protocol scheme. Throws java.security.SecurityException on cleartext HTTP.
     * - Injects global defensive security headers (X-Content-Type-Options, X-Frame-Options, X-XSS-Protection, X-App-Platform, Version).
     * - Attaches Firebase Auth Bearer JWT tokens securely.
     */
    private val securityInterceptor = Interceptor { chain ->
        val original = chain.request()

        // Rule 2: Intercept outbound requests and verify HTTPS protocol scheme
        if (!original.url.isHttps) {
            throw SecurityException("Insecure cleartext HTTP connections are strictly blocked by military-grade security policy: ${original.url}")
        }

        // Rule 3: Inject global defensive security headers
        val requestBuilder = original.newBuilder()
            .header("X-Content-Type-Options", "nosniff")
            .header("X-Frame-Options", "DENY")
            .header("X-XSS-Protection", "1; mode=block")
            .header("X-App-Platform", "Android")
            .header("X-App-Version", BuildConfig.VERSION_NAME)
            .header("X-Client-Build", BuildConfig.VERSION_CODE.toString())

        // Attach Firebase Auth Token if available and not already set
        if (original.header("Authorization") == null) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                try {
                    val tokenResult = kotlinx.coroutines.runBlocking {
                        try {
                            currentUser.getIdToken(false).await()?.token
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (!tokenResult.isNullOrBlank()) {
                        requestBuilder.header("Authorization", "Bearer $tokenResult")
                    }
                } catch (_: Exception) {
                    // Do not block request if auth token retrieval fails
                }
            }
        }

        chain.proceed(requestBuilder.build())
    }

    /**
     * Hardened OkHttpClient instance.
     */
    val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
            .certificatePinner(certificatePinner)
            .addInterceptor(securityInterceptor)

        // Rule 4: Enforce production logging stripping: encapsulate HttpLoggingInterceptor within explicit BuildConfig.DEBUG block
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            builder.addInterceptor(logging)
        }

        builder.build()
    }

    /**
     * Creates a hardened Retrofit instance bound to the secure OkHttpClient.
     */
    fun createRetrofit(baseUrl: String): Retrofit {
        require(baseUrl.startsWith("https://")) {
            "Base URL must use secure HTTPS: $baseUrl"
        }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
