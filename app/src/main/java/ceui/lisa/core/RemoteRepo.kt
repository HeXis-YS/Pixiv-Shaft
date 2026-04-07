package ceui.lisa.core

import ceui.lisa.fragments.NetListFragment
import ceui.lisa.http.NullCtrl
import ceui.lisa.interfaces.ListShow
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers

/**
 * The class stores response got from remote repo (pixiv) in the form of [ListShow]
 */
abstract class RemoteRepo<Response : ListShow<*>> : BaseRepo() {
    private var mApi: Observable<out Response>? = null
    private val mFunction: Function<in Response, Response> = mapper()
    @get:JvmName("getNextUrlValue")
    @set:JvmName("setNextUrlValue")
    protected var nextUrl: String = ""

    /**
     * An interface overrided in different class to init different Api depending on the response type
     *
     * For expample:
     *
     * The mRemoteRepo in [NetListFragment] of homepage is [ceui.lisa.model.RecmdIllust]
     *
     * While mRemoteRepo in [NetListFragment] of rank page is [ceui.lisa.repo.RankIllustRepo]
     */
    abstract fun initApi(): Observable<out Response>?

    abstract fun initNextApi(): Observable<out Response>?

    /**
     * Init Api and POST request to get response containing information of illustrations
     * @param nullCtrl (In doubt)In case of null
     */
    fun getFirstData(nullCtrl: NullCtrl<Response>) {
        mApi = initApi()
        mApi?.subscribeOn(Schedulers.newThread())
            ?.map(mFunction)
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(nullCtrl)
    }

    fun getNextData(nullCtrl: NullCtrl<Response>) {
        mApi = initNextApi()
        mApi?.subscribeOn(Schedulers.newThread())
            ?.map(mFunction)
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(nullCtrl)
    }

    open fun mapper(): Function<in Response, Response> {
        return Mapper()
    }

    fun getNextUrl(): String {
        return nextUrl
    }

    fun setNextUrl(nextUrl: String?) {
        this.nextUrl = nextUrl ?: ""
    }

    open fun hasEffectiveUserFollowStatus(): Boolean {
        return true
    }
}
