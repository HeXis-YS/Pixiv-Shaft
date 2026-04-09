package ceui.lisa.http

import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.fragments.FragmentLogin
import ceui.lisa.models.UserModel
import ceui.lisa.utils.Common
import ceui.lisa.utils.Local
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Call
import java.io.IOException

/**
 * 全局自动刷新Token的拦截器
 */
class TokenInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (isTokenExpired(response)) {
            Common.showLog("getNewToken 检测到是过期Token ")
            response.close()
            val newToken = getNewToken(request.header("Authorization"))
            val newRequest = chain.request()
                .newBuilder()
                .header("Authorization", newToken)
                .build()
            return chain.proceed(newRequest)
        }
        return response
    }

    /**
     * 根据Response，判断Token是否失效
     */
    private fun isTokenExpired(response: Response): Boolean {
        val body = Common.getResponseBody(response)
        Common.showLog("isTokenExpired body $body")
        return if (response.code == 400) {
            when {
                body.contains(TOKEN_ERROR_1) -> {
                    Common.showLog("isTokenExpired 000")
                    true
                }

                body.contains(TOKEN_ERROR_2) -> {
                    Shaft.sUserModel.user.setIs_login(false)
                    Local.saveUser(Shaft.sUserModel)
                    Common.showToast(R.string.string_340)
                    Common.restart()
                    Common.showLog("isTokenExpired 111")
                    false
                }

                else -> {
                    Common.showLog("isTokenExpired 222")
                    false
                }
            }
        } else {
            Common.showLog("isTokenExpired 333")
            false
        }
    }

    /**
     * 同步请求方式，获取最新的Token，解决多并发请求多次刷新token的问题
     */
    @Throws(IOException::class)
    private fun getNewToken(tokenForThisRequest: String?): String {
        @Synchronized
        fun refreshToken(): String {
            if (Shaft.sUserModel.access_token == tokenForThisRequest ||
                tokenForThisRequest?.length != TOKEN_LENGTH ||
                Shaft.sUserModel.access_token.length != TOKEN_LENGTH
            ) {
                Common.showLog("getNewToken 主动获取最新的token old:$tokenForThisRequest new:${Shaft.sUserModel.access_token}")
                val userModel = Local.getUser()
                val call: Call<UserModel> = Retro.getAccountTokenApi().newRefreshToken(
                    FragmentLogin.CLIENT_ID,
                    FragmentLogin.CLIENT_SECRET,
                    FragmentLogin.REFRESH_TOKEN,
                    userModel.refresh_token,
                    true
                )
                val newUser = call.execute().body()
                if (newUser != null) {
                    newUser.user.password = Shaft.sUserModel.user.password
                    newUser.user.setIs_login(true)
                }
                Local.saveUser(newUser)
                Common.showLog("getNewToken 获取到了最新的 token:${newUser?.access_token}")
                return newUser!!.access_token
            } else {
                Common.showLog("getNewToken 使用最新的token old:$tokenForThisRequest new:${Shaft.sUserModel.access_token}")
                return Shaft.sUserModel.access_token
            }
        }

        return refreshToken()
    }

    companion object {
        private const val TOKEN_ERROR_1 = "Error occurred at the OAuth process"
        private const val TOKEN_ERROR_2 = "Invalid refresh token"
        private const val TOKEN_LENGTH = 50
    }
}
