package ceui.lisa.http

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface ResourceApi {
    @GET("gh/CeuiLiSA/Pixiv-Shaft@master/app/src/main/assets/comment.filter.rule.txt")
    fun getCommentFilterRule(): Observable<ResponseBody>

    @GET(JSDELIVR_PROJECT_MASTER_PATH + "{path}")
    fun getByPath(@Path("path") path: String): Observable<ResponseBody>

    companion object {
        const val JSDELIVR_BASE_URL = "https://cdn.jsdelivr.net/"
        const val JSDELIVR_PROJECT_MASTER_PATH = "gh/CeuiLiSA/Pixiv-Shaft@master/"
    }
}
