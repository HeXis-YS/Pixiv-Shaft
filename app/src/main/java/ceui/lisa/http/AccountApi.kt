package ceui.lisa.http

import ceui.lisa.models.UserModel
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AccountApi {

    @FormUrlEncoded
    @POST("/auth/token")
    fun newLogin(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String,
        @Field("code") code: String,
        @Field("code_verifier") codeVerifier: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("include_policy") includePolicy: Boolean,
    ): Observable<UserModel>

    @GET("login?prompt=select_account&source=pixiv-android&ref=&client=pixiv-android")
    fun tryLogin(@Query("return_to") returnTo: String): Observable<String>

    companion object {
        const val ACCOUNT_BASE_URL = "https://oauth.secure.pixiv.net/"
    }
}
