package ceui.lisa.http

import android.util.Log
import com.blankj.utilcode.util.DeviceUtils
import com.google.gson.GsonBuilder
import ceui.lisa.helper.LanguageHelper
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Collections

object Retro {

    @JvmStatic
    fun getAppApi(): AppApi {
        return get().create(AppApi::class.java)
    }

    @JvmStatic
    fun refreshAppApi() {
        Holder.appRetrofit = buildRetrofit(AppApi.API_BASE_URL)
    }

    @JvmStatic
    fun getSignApi(): SignApi {
        return buildRetrofit(SignApi.SIGN_API).create(SignApi::class.java)
    }

    @JvmStatic
    fun getAccountApi(): AccountApi {
        return buildRetrofit(AccountApi.ACCOUNT_BASE_URL).create(AccountApi::class.java)
    }

    @JvmStatic
    fun getAccountTokenApi(): AccountTokenApi {
        return buildRetrofit(AccountApi.ACCOUNT_BASE_URL).create(AccountTokenApi::class.java)
    }

    @JvmStatic
    fun getResourceApi(): ResourceApi {
        return buildPlainRetrofit(ResourceApi.JSDELIVR_BASE_URL).create(ResourceApi::class.java)
    }

    private fun addHeader(before: Request.Builder): Request.Builder {
        val pixivHeaders = PixivHeaders()
        val osVersion = DeviceUtils.getSDKVersionName()
        val phoneName = DeviceUtils.getModel()
        return before
            .addHeader("User-Agent", "PixivAndroidApp/5.0.234 (Android $osVersion; $phoneName)")
            .addHeader("accept-language", LanguageHelper.getRequestHeaderAcceptLanguageFromAppLanguage())
            .addHeader("x-client-time", pixivHeaders.xClientTime)
            .addHeader("x-client-hash", pixivHeaders.xClientHash)
    }

    private fun buildRetrofit(baseUrl: String): Retrofit {
        val builder = getLogClient()
        try {
            builder.addInterceptor { chain ->
                chain.proceed(addHeader(chain.request().newBuilder()).build())
            }
            builder.addInterceptor(TokenInterceptor())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val client = builder.build()
        val gson = GsonBuilder().setLenient().create()
        return Retrofit.Builder()
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(baseUrl)
            .build()
    }

    private fun buildPlainRetrofit(baseUrl: String): Retrofit {
        val client = getLogClient().build()
        return Retrofit.Builder()
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(baseUrl)
            .build()
    }

    @JvmStatic
    fun <T> create(baseUrl: String, service: Class<T>): T {
        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .client(
                getLogClient().addInterceptor { chain ->
                    val localRequest = chain.request().newBuilder()
                        .addHeader(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.80 Safari/537.36"
                        )
                        .addHeader("Accept-Encoding:", "gzip, deflate")
                        .addHeader("Accept:", "text/html")
                        .build()
                    chain.proceed(localRequest)
                }.build()
            )
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(baseUrl)
            .build()
        return retrofit.create(service)
    }

    private object Holder {
        var appRetrofit: Retrofit = buildRetrofit(AppApi.API_BASE_URL)
    }

    private fun get(): Retrofit {
        return Holder.appRetrofit
    }

    @JvmStatic
    fun getLogClient(): OkHttpClient.Builder {
        val loggingInterceptor = HttpLoggingInterceptor(
            object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Log.i("RetroLog", message)
                }
            }
        )
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
    }
}
