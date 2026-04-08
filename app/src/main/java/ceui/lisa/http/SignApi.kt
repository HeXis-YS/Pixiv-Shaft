package ceui.lisa.http

import ceui.lisa.models.AccountEditResponse
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface SignApi {

    @FormUrlEncoded
    @POST("/api/v2/account/edit")
    fun edit(
        @Header("Authorization") token: String,
        @Field("new_mail_address") newMailAddress: String,
        @Field("new_user_account") newUserAccount: String,
        @Field("current_password") currentPassword: String,
        @Field("new_password") newPassword: String,
    ): Observable<AccountEditResponse>

    @FormUrlEncoded
    @POST("/api/v2/account/edit")
    fun changePassword(
        @Header("Authorization") token: String,
        @Field("current_password") currentPassword: String,
        @Field("new_password") newPassword: String,
    ): Observable<AccountEditResponse>

    @FormUrlEncoded
    @POST("/api/v2/account/edit")
    fun changePasswordPixivID(
        @Header("Authorization") token: String,
        @Field("new_user_account") newUserAccount: String,
        @Field("current_password") currentPassword: String,
        @Field("new_password") newPassword: String,
    ): Observable<AccountEditResponse>

    @FormUrlEncoded
    @POST("/api/v2/account/edit")
    fun changePixivID(
        @Header("Authorization") token: String,
        @Field("new_user_account") newUserAccount: String,
        @Field("current_password") currentPassword: String,
    ): Observable<AccountEditResponse>

    @FormUrlEncoded
    @POST("/api/v2/account/edit")
    fun changeEmail(
        @Header("Authorization") token: String,
        @Field("new_mail_address") newMailAddress: String,
        @Field("current_password") currentPassword: String,
    ): Observable<AccountEditResponse>

    @FormUrlEncoded
    @POST("/api/v2/account/edit")
    fun changeEmailAndPixivID(
        @Header("Authorization") token: String,
        @Field("new_mail_address") newMailAddress: String,
        @Field("new_user_account") newUserAccount: String,
        @Field("current_password") currentPassword: String,
    ): Observable<AccountEditResponse>

    @FormUrlEncoded
    @POST("/api/v2/account/edit")
    fun changeEmailAndPassword(
        @Header("Authorization") token: String,
        @Field("new_mail_address") newMailAddress: String,
        @Field("current_password") currentPassword: String,
        @Field("new_password") newPassword: String,
    ): Observable<AccountEditResponse>

    companion object {
        const val SIGN_API = "https://accounts.pixiv.net/"
    }
}
