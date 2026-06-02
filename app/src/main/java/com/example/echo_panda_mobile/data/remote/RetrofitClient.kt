package com.example.echo_panda_mobile.data.remote

import com.example.echo_panda_mobile.BuildConfig
import com.example.echo_panda_mobile.data.repository.TokenStorage
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLDecoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

// Persistent CookieJar to survive OkHttpClient recreation (e.g. on logout/login)
private val persistentCookieJar = object : CookieJar {
    private val store = ConcurrentHashMap<String, MutableList<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val list = store.getOrPut(host) { mutableListOf() }
        cookies.forEach { cookie ->
            list.removeAll { it.name == cookie.name }
            list.add(cookie)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return store[url.host]?.toList() ?: emptyList()
    }

    fun getCookieByName(name: String): Cookie? {
        for (entry in store.values) {
            entry.firstOrNull { it.name.equals(name, ignoreCase = true) }?.let { return it }
        }
        return null
    }

    fun clear() {
        store.clear()
    }
}

object RetrofitClient {
    private val baseUrl: String = BuildConfig.API_BASE_URL
    
    @Volatile
    private var okHttpClient: OkHttpClient? = null

    @Volatile
    private var retrofit: Retrofit? = null

    private var authService: AuthApiService? = null
    private var musicService: MusicApiService? = null
    private var adminService: AdminApiService? = null

    /**
     * Provides a single shared OkHttpClient instance.
     */
    fun getOkHttpClient(tokenStorage: TokenStorage): OkHttpClient {
        return okHttpClient ?: synchronized(this) {
            okHttpClient ?: buildOkHttpClient(tokenStorage).also { okHttpClient = it }
        }
    }

    private fun buildOkHttpClient(tokenStorage: TokenStorage): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .cookieJar(persistentCookieJar)
            // Add logging as a NETWORK interceptor to see headers like 'Cookie' 
            // that are added by the BridgeInterceptor after application interceptors.
            .addNetworkInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request()
                val url = request.url.toString()
                val newReqBuilder = request.newBuilder()

                // If Laravel set an XSRF-TOKEN cookie, we must echo it back as a header
                // for routes that don't start with /api/ (like /admin/*).
                val xsrfCookie = persistentCookieJar.getCookieByName("XSRF-TOKEN")
                if (xsrfCookie != null && !url.contains("amazonaws.com")) {
                    try {
                        val decoded = URLDecoder.decode(xsrfCookie.value, "UTF-8")
                        newReqBuilder.header("X-XSRF-TOKEN", decoded)
                    } catch (_: Exception) {}
                }

                chain.proceed(newReqBuilder.build())
            }
            .addInterceptor(AuthInterceptor(tokenStorage))
            .authenticator(AuthAuthenticator(tokenStorage))
            .build()
    }

    private fun getRetrofit(tokenStorage: TokenStorage): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClient(tokenStorage))
                .build()
                .also { retrofit = it }
        }
    }

    fun getAuthService(tokenStorage: TokenStorage): AuthApiService {
        return authService ?: synchronized(this) {
            authService ?: getRetrofit(tokenStorage).create(AuthApiService::class.java).also { authService = it }
        }
    }

    fun getMusicService(tokenStorage: TokenStorage): MusicApiService {
        return musicService ?: synchronized(this) {
            musicService ?: getRetrofit(tokenStorage).create(MusicApiService::class.java).also { musicService = it }
        }
    }

    fun getAdminService(tokenStorage: TokenStorage): AdminApiService {
        return adminService ?: synchronized(this) {
            adminService ?: getRetrofit(tokenStorage).create(AdminApiService::class.java).also { adminService = it }
        }
    }

    fun getInstance(tokenStorage: TokenStorage): AuthApiService = getAuthService(tokenStorage)

    /**
     * Clears cached service instances. Call this on logout or role change 
     * to ensure the next request uses fresh configuration/interceptors.
     */
    fun clearInstances() {
        synchronized(this) {
            authService = null
            musicService = null
            adminService = null
            retrofit = null
            okHttpClient = null
            persistentCookieJar.clear()
        }
    }
}
