package ceui.loxia

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface API {

    @FormUrlEncoded
    @POST("/v1/user/follow/add")
    suspend fun postFollow(
        @Field("user_id") user_id: Long,
        @Field("restrict") followType: String
    )

    @FormUrlEncoded
    @POST("/v1/user/follow/delete")
    suspend fun postUnFollow(
        @Field("user_id") user_id: Long
    )
}
