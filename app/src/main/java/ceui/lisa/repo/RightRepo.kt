package ceui.lisa.repo

import ceui.lisa.core.FilterMapper
import ceui.lisa.core.RemoteRepo
import ceui.lisa.http.Retro
import ceui.lisa.model.ListIllust
import io.reactivex.Observable
import io.reactivex.functions.Function

class RightRepo(var restrict: String?) : RemoteRepo<ListIllust>() {

    override fun initApi(): Observable<ListIllust> {
        return Retro.getAppApi().getFollowUserIllust(token(), restrict)
    }

    override fun initNextApi(): Observable<ListIllust> {
        return Retro.getAppApi().getNextIllust(token(), nextUrl)
    }

    override fun mapper(): Function<ListIllust, ListIllust> {
        return FilterMapper()
    }

    override fun localData(): Boolean {
        return false
    }
}
