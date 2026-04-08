package ceui.lisa.http

import ceui.lisa.models.UserModel
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AccountTokenApi {

    @FormUrlEncoded
    @POST("/auth/token")
    fun newRefreshToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String,
        @Field("include_policy") includePolicy: Boolean,
    ): Call<UserModel>
}
