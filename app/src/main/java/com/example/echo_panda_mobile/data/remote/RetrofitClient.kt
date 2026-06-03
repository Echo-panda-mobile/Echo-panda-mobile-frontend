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
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .cookieJar(persistentCookieJar)
            .addNetworkInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request()
                val url = request.url.toString()
                val newReqBuilder = request.newBuilder()

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
    
    // Alias for compatibility
    fun resetAll() = clearInstances()
}
