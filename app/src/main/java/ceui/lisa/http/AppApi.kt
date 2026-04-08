package ceui.lisa.http

import ceui.lisa.model.ListArticle
import ceui.lisa.model.ListBookmarkTag
import ceui.lisa.model.ListComment
import ceui.lisa.model.ListIllust
import ceui.lisa.model.ListLive
import ceui.lisa.model.ListMangaOfSeries
import ceui.lisa.model.ListMangaSeries
import ceui.lisa.model.ListNovel
import ceui.lisa.model.ListNovelMarkers
import ceui.lisa.model.ListNovelOfSeries
import ceui.lisa.model.ListNovelSeries
import ceui.lisa.model.ListSimpleUser
import ceui.lisa.model.ListTag
import ceui.lisa.model.ListTrendingtag
import ceui.lisa.model.ListUser
import ceui.lisa.model.ListWatchlistNovel
import ceui.lisa.model.RecmdIllust
import ceui.lisa.models.CommentHolder
import ceui.lisa.models.GifResponse
import ceui.lisa.models.IllustSearchResponse
import ceui.lisa.models.MutedHistory
import ceui.lisa.models.NovelDetail
import ceui.lisa.models.NovelSearchResponse
import ceui.lisa.models.NullResponse
import ceui.lisa.models.UserDetailResponse
import ceui.lisa.models.UserFollowDetail
import ceui.lisa.models.UserState
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.List

interface AppApi {

    @GET("v1/illust/ranking?filter=for_android")
    fun getRank(
        @Header("Authorization") token: String,
        @Query("mode") mode: String,
        @Query("date") date: String,
    ): Observable<ListIllust>

    @GET("v1/novel/ranking?filter=for_android")
    fun getRankNovel(
        @Header("Authorization") token: String,
        @Query("mode") mode: String,
        @Query("date") date: String,
    ): Observable<ListNovel>

    @GET("v1/illust/recommended?include_privacy_policy=true&filter=for_android")
    fun getRecmdIllust(
        @Header("Authorization") token: String,
        @Query("include_ranking_illusts") includeRankingIllusts: Boolean,
    ): Observable<RecmdIllust>

    @GET("v1/manga/recommended?include_privacy_policy=true&filter=for_android&include_ranking_illusts=true")
    fun getRecmdManga(@Header("Authorization") token: String): Observable<RecmdIllust>

    @GET("v1/novel/recommended?include_privacy_policy=true&filter=for_android&include_ranking_novels=true")
    fun getRecmdNovel(@Header("Authorization") token: String): Observable<ListNovel>

    @GET("v1/novel/follow")
    fun getBookedUserSubmitNovel(
        @Header("Authorization") token: String,
        @Query("restrict") restrict: String,
    ): Observable<ListNovel>

    @GET("v1/trending-tags/{type}?filter=for_android&include_translated_tag_results=true")
    fun getHotTags(
        @Header("Authorization") token: String,
        @Path("type") type: String,
    ): Observable<ListTrendingtag>

    @GET("v1/search/illust?filter=for_android&include_translated_tag_results=true&merge_plain_keyword_results=true")
    fun searchIllust(
        @Header("Authorization") token: String,
        @Query("word") word: String,
        @Query("sort") sort: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("search_target") searchTarget: String,
    ): Observable<ListIllust>

    @GET("v1/search/novel?filter=for_android&include_translated_tag_results=true&merge_plain_keyword_results=true")
    fun searchNovel(
        @Header("Authorization") token: String,
        @Query("word") word: String,
        @Query("sort") sort: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("search_target") searchTarget: String,
    ): Observable<ListNovel>

    @GET("v2/illust/related?filter=for_android")
    fun relatedIllust(
        @Header("Authorization") token: String,
        @Query("illust_id") illustId: Int,
    ): Observable<ListIllust>

    @GET("v1/user/recommended?filter=for_android")
    fun getRecmdUser(@Header("Authorization") token: String): Observable<ListUser>

    @GET("v1/user/bookmarks/illust")
    fun getUserLikeIllust(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int,
        @Query("restrict") restrict: String,
        @Query("tag") tag: String,
    ): Observable<ListIllust>

    @GET("v1/user/bookmarks/illust")
    fun getUserLikeIllust(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int,
        @Query("restrict") restrict: String,
    ): Observable<ListIllust>

    @GET("v1/user/bookmarks/novel")
    fun getUserLikeNovel(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int,
        @Query("restrict") restrict: String,
        @Query("tag") tag: String,
    ): Observable<ListNovel>

    @GET("v1/user/bookmarks/novel")
    fun getUserLikeNovel(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int,
        @Query("restrict") restrict: String,
    ): Observable<ListNovel>

    @GET("v1/user/illusts?filter=for_android")
    fun getUserSubmitIllust(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int,
        @Query("type") type: String,
    ): Observable<ListIllust>

    @GET("v1/user/novels")
    fun getUserSubmitNovel(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int,
    ): Observable<ListNovel>

    @GET("v2/illust/follow")
    fun getFollowUserIllust(
        @Header("Authorization") token: String,
        @Query("restrict") restrict: String,
    ): Observable<ListIllust>

    @GET("v1/spotlight/articles?filter=for_android")
    fun getArticles(
        @Header("Authorization") token: String,
        @Query("category") category: String,
    ): Observable<ListArticle>

    @GET("v1/user/detail?filter=for_android")
    fun getUserDetail(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int,
    ): Observable<UserDetailResponse>

    @GET("v1/ugoira/metadata")
    fun getGifPackage(
        @Header("Authorization") token: String,
        @Query("illust_id") illustId: Int,
    ): Observable<GifResponse>

    @FormUrlEncoded
    @POST("v1/user/follow/add")
    fun postFollow(
        @Header("Authorization") token: String,
        @Field("user_id") userId: Int,
        @Field("restrict") followType: String,
    ): Observable<NullResponse>

    @FormUrlEncoded
    @POST("v1/user/follow/delete")
    fun postUnFollow(
        @Header("Authorization") token: String,
        @Field("user_id") userId: Int,
    ): Observable<NullResponse>

    @GET("v1/user/follow/detail")
    fun getFollowDetail(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int,
    ): Observable<UserFollowDetail>

    @GET("v1/user/following?filter=for_android")
    fun getFollowUser(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int,
        @Query("restrict") restrict: String,
    ): Observable<ListUser>

    @GET("v1/user/follower?filter=for_android")
    fun getWhoFollowThisUser(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int,
    ): Observable<ListUser>

    @GET("/v3/illust/comments")
    fun getIllustComment(
        @Header("Authorization") token: String,
        @Query("illust_id") illustId: Int,
    ): Observable<ListComment>

    @GET("v3/novel/comments")
    fun getNovelComment(
        @Header("Authorization") token: String,
        @Query("novel_id") novelId: Int,
    ): Observable<ListComment>

    @GET
    fun getNextComment(
        @Header("Authorization") token: String,
        @Url nextUrl: String,
    ): Observable<ListComment>

    @FormUrlEncoded
    @POST("v1/illust/comment/add")
    fun postIllustComment(
        @Header("Authorization") token: String,
        @Field("illust_id") illustId: Int,
        @Field("comment") comment: String,
    ): Observable<CommentHolder>

    @FormUrlEncoded
    @POST("v1/illust/comment/add")
    fun postIllustComment(
        @Header("Authorization") token: String,
        @Field("illust_id") illustId: Int,
        @Field("comment") comment: String,
        @Field("parent_comment_id") parentCommentId: Int,
    ): Observable<CommentHolder>

    @FormUrlEncoded
    @POST("v1/novel/comment/add")
    fun postNovelComment(
        @Header("Authorization") token: String,
        @Field("novel_id") novelId: Int,
        @Field("comment") comment: String,
    ): Observable<CommentHolder>

    @FormUrlEncoded
    @POST("v1/novel/comment/add")
    fun postNovelComment(
        @Header("Authorization") token: String,
        @Field("novel_id") novelId: Int,
        @Field("comment") comment: String,
        @Field("parent_comment_id") parentCommentId: Int,
    ): Observable<CommentHolder>

    @FormUrlEncoded
    @POST("v2/illust/bookmark/add")
    fun postLikeIllust(
        @Header("Authorization") token: String,
        @Field("illust_id") illustId: Int,
        @Field("restrict") restrict: String,
    ): Observable<NullResponse>

    @FormUrlEncoded
    @POST("v2/novel/bookmark/add")
    fun postLikeNovel(
        @Header("Authorization") token: String,
        @Field("novel_id") novelId: Int,
        @Field("restrict") restrict: String,
    ): Observable<NullResponse>

    @FormUrlEncoded
    @POST("v2/illust/bookmark/add")
    fun postLikeIllustWithTags(
        @Header("Authorization") token: String,
        @Field("illust_id") illustId: Int,
        @Field("restrict") restrict: String,
        @Field("tags[]") vararg tags: String,
    ): Observable<NullResponse>

    @FormUrlEncoded
    @POST("v2/novel/bookmark/add")
    fun postLikeNovelWithTags(
        @Header("Authorization") token: String,
        @Field("novel_id") novelId: Int,
        @Field("restrict") restrict: String,
        @Field("tags[]") vararg tags: String,
    ): Observable<NullResponse>

    @FormUrlEncoded
    @POST("v1/illust/bookmark/delete")
    fun postDislikeIllust(
        @Header("Authorization") token: String,
        @Field("illust_id") illustId: Int,
    ): Observable<NullResponse>

    @FormUrlEncoded
    @POST("v1/novel/bookmark/delete")
    fun postDislikeNovel(
        @Header("Authorization") token: String,
        @Field("novel_id") novelId: Int,
    ): Observable<NullResponse>

    @GET("v1/illust/detail?filter=for_android")
    fun getIllustByID(
        @Header("Authorization") token: String,
        @Query("illust_id") illustId: Long,
    ): Observable<IllustSearchResponse>

    @GET("v1/search/user?filter=for_android")
    fun searchUser(
        @Header("Authorization") token: String,
        @Query("word") word: String,
    ): Observable<ListUser>

    @GET("v1/search/popular-preview/illust?filter=for_android&include_translated_tag_results=true&merge_plain_keyword_results=true")
    fun popularPreview(
        @Header("Authorization") token: String,
        @Query("word") word: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("search_target") searchTarget: String,
    ): Observable<ListIllust>

    @GET("v1/search/popular-preview/novel?filter=for_android&include_translated_tag_results=true&merge_plain_keyword_results=true")
    fun popularNovelPreview(
        @Header("Authorization") token: String,
        @Query("word") word: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("search_target") searchTarget: String,
    ): Observable<ListNovel>

    @GET("v2/search/autocomplete?merge_plain_keyword_results=true")
    fun searchCompleteWord(
        @Header("Authorization") token: String,
        @Query("word") word: String,
    ): Observable<ListTrendingtag>

    @GET("v1/user/bookmark-tags/illust")
    fun getAllIllustBookmarkTags(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int,
        @Query("restrict") restrict: String,
    ): Observable<ListTag>

    @GET("v1/user/bookmark-tags/novel")
    fun getAllNovelBookmarkTags(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int,
        @Query("restrict") restrict: String,
    ): Observable<ListTag>

    @GET
    fun getNextTags(
        @Header("Authorization") token: String,
        @Url nextUrl: String,
    ): Observable<ListTag>

    @GET("v2/illust/bookmark/detail")
    fun getIllustBookmarkTags(
        @Header("Authorization") token: String,
        @Query("illust_id") illustId: Int,
    ): Observable<ListBookmarkTag>

    @GET("v2/novel/bookmark/detail")
    fun getNovelBookmarkTags(
        @Header("Authorization") token: String,
        @Query("novel_id") novelId: Int,
    ): Observable<ListBookmarkTag>

    @GET("v1/mute/list")
    fun getMutedHistory(@Header("Authorization") token: String): Observable<MutedHistory>

    @GET("v1/user/mypixiv?filter=for_android")
    fun getNiceFriend(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int,
    ): Observable<ListUser>

    @GET("v1/illust/new?filter=for_android")
    fun getNewWorks(
        @Header("Authorization") token: String,
        @Query("content_type") contentType: String,
    ): Observable<ListIllust>

    @GET("v1/novel/new")
    fun getNewNovels(@Header("Authorization") token: String): Observable<ListNovel>

    @GET("/webview/v2/novel")
    fun getNovelDetailV2(
        @Header("Authorization") token: String,
        @Query("id") id: Long,
    ): Call<ResponseBody>

    @GET("v1/user/me/state")
    fun getAccountState(@Header("Authorization") token: String): Observable<UserState>

    @GET("v1/live/list")
    fun getLiveList(
        @Header("Authorization") token: String,
        @Query("list_type") listType: String,
    ): Observable<ListLive>

    @GET("v1/illust/bookmark/users?filter=for_android")
    fun getUsersWhoLikeThisIllust(
        @Header("Authorization") token: String,
        @Query("illust_id") illustId: Int,
    ): Observable<ListSimpleUser>

    @GET("v2/novel/series")
    fun getNovelSeries(
        @Header("Authorization") token: String,
        @Query("series_id") seriesId: Int,
    ): Observable<ListNovelOfSeries>

    @GET("v2/novel/detail")
    fun getNovelByID(
        @Header("Authorization") token: String,
        @Query("novel_id") novelId: Long,
    ): Observable<NovelSearchResponse>

    @GET("v1/illust/series?filter=for_android")
    fun getMangaSeriesById(
        @Header("Authorization") token: String,
        @Query("illust_series_id") illustSeriesId: Int,
    ): Observable<ListMangaOfSeries>

    @GET("v1/user/illust-series")
    fun getUserMangaSeries(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int,
    ): Observable<ListMangaSeries>

    @GET("v1/user/novel-series")
    fun getUserNovelSeries(
        @Header("Authorization") token: String,
        @Query("user_id") userId: Int,
    ): Observable<ListNovelSeries>

    @GET
    fun getNextUserNovelSeries(
        @Header("Authorization") token: String,
        @Url nextUrl: String,
    ): Observable<ListNovelSeries>

    @GET
    fun getNextUserMangaSeries(
        @Header("Authorization") token: String,
        @Url nextUrl: String,
    ): Observable<ListMangaSeries>

    @GET
    fun getNextUser(
        @Header("Authorization") token: String,
        @Url nextUrl: String,
    ): Observable<ListUser>

    @GET
    fun getNextSimpleUser(
        @Header("Authorization") token: String,
        @Url nextUrl: String,
    ): Observable<ListSimpleUser>

    @GET
    fun getNextIllust(
        @Header("Authorization") token: String,
        @Url nextUrl: String,
    ): Observable<ListIllust>

    @GET
    fun getNextNovel(
        @Header("Authorization") token: String,
        @Url nextUrl: String,
    ): Observable<ListNovel>

    @GET
    fun getNextSeriesNovel(
        @Header("Authorization") token: String,
        @Url nextUrl: String,
    ): Observable<ListNovelOfSeries>

    @GET
    fun getNextArticles(
        @Header("Authorization") token: String,
        @Url nextUrl: String,
    ): Observable<ListArticle>

    @GET("web/v1/login?code_challenge_method=S256&client=pixiv-android")
    fun tryLogin(@Query("code_challenge") codeChallenge: String): Observable<String>

    @FormUrlEncoded
    @POST("v1/novel/marker/add")
    fun postAddNovelMarker(
        @Header("Authorization") token: String,
        @Field("novel_id") novelId: Int,
        @Field("page") page: Int,
    ): Observable<NullResponse>

    @FormUrlEncoded
    @POST("v1/novel/marker/delete")
    fun postDeleteNovelMarker(
        @Header("Authorization") token: String,
        @Field("novel_id") novelId: Int,
    ): Observable<NullResponse>

    @GET("v1/user/related?filter=for_android")
    fun getRelatedUsers(
        @Header("Authorization") token: String,
        @Query("seed_user_id") seedUserId: Int,
    ): Observable<ListUser>

    @GET("v1/watchlist/novel")
    fun getWatchlistNovel(@Header("Authorization") token: String): Observable<ListWatchlistNovel>

    @GET
    fun getNextWatchlistNovel(
        @Header("Authorization") token: String,
        @Url nextUrl: String,
    ): Observable<ListWatchlistNovel>

    @GET("v2/novel/markers")
    fun getNovelMarkers(@Header("Authorization") token: String): Observable<ListNovelMarkers>

    @GET
    fun getNextNovelMarkers(
        @Header("Authorization") token: String,
        @Url nextUrl: String,
    ): Observable<ListNovelMarkers>

    companion object {
        const val API_BASE_URL = "https://app-api.pixiv.net/"
    }
}
